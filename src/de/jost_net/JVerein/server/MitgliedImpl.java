/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedDokument;
import de.jost_net.JVerein.rmi.Mitgliedfoto;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.EmailValidator;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedImpl extends AbstractDBObject implements Mitglied
{

  private static String FEHLER_ZAHLUNGSWEG = ": Der Zahlungsweg ist nicht Basislastschrift.";

  private static String FEHLER_MANDAT = ": Es ist kein Mandat-Datum vorhanden.";

  private static String FEHLER_ALTER = ": Das Mandat-Datum ist �lter als 36 Monate und es sind in JVerein keine Lastschriften f�r die letzten 3 Jahre vorhanden.";

  private transient Map<String, String> variable;

  private static final long serialVersionUID = 1L;

  private static MitgliedDummy DUMMY_INSTANCE;

  public MitgliedImpl() throws RemoteException
  {
    super();
    variable = new HashMap<>();
  }

  @Override
  protected String getTableName()
  {
    return "mitglied";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "namevorname";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      // Falls das Mitglied f�r andere zahlt kann man nicht l�schen
      DBIterator<Mitglied> famang = Einstellungen.getDBService()
          .createList(Mitglied.class);
      famang.addFilter("zahlerid = " + getID());
      if (famang.hasNext())
      {
        throw new ApplicationException(
            "Dieses Mitglied zahlt noch f�r andere Mitglieder. Zun�chst Beitragsart der Angeh�rigen �ndern!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Mitglied kann nicht gel�scht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      plausi();
    }
    catch (RemoteException e)
    {
      String fehler = "Mitglied kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @SuppressWarnings("unused")
  private void plausi() throws RemoteException, ApplicationException
  {
    checkExterneMitgliedsnummer();

    if (getPersonenart() == null || (!getPersonenart().equalsIgnoreCase(
        "n") && !getPersonenart().equalsIgnoreCase("j")))
    {
      throw new ApplicationException("Personenstatus ist nicht 'N' oder 'J'");
    }
    if (getName() == null || getName().length() == 0)
    {
      throw new ApplicationException("Bitte Namen eingeben");
    }
    if (getPersonenart().equalsIgnoreCase(
        "n") && (getVorname() == null || getVorname().length() == 0))
    {
      throw new ApplicationException("Bitte Vornamen eingeben");
    }
    if (getAdresstyp().getJVereinid() == 1 && getPersonenart().equalsIgnoreCase(
        "n") && getGeburtsdatum().getTime() == Einstellungen.NODATE.getTime() && Einstellungen.getEinstellung()
        .getGeburtsdatumPflicht())
    {
      throw new ApplicationException("Bitte Geburtsdatum eingeben");
    }
    if (getAdresstyp().getJVereinid() == 1 && getPersonenart().equalsIgnoreCase(
        "n") && Einstellungen.getEinstellung().getGeburtsdatumPflicht())
    {
      Calendar cal1 = Calendar.getInstance();
      cal1.setTime(getGeburtsdatum());
      Calendar cal2 = Calendar.getInstance();
      if (cal1.after(cal2))
      {
        throw new ApplicationException("Geburtsdatum liegt in der Zukunft");
      }
      if (getSterbetag() != null)
      {
        cal2.setTime(getSterbetag());
      }
      cal2.add(Calendar.YEAR, -150);
      if (cal1.before(cal2))
      {
        throw new ApplicationException(
            "Ist das Mitglied wirklich �lter als 150 Jahre?");
      }
    }
    if (getPersonenart().equalsIgnoreCase("n") && getGeschlecht() == null)
    {
      throw new ApplicationException("Bitte Geschlecht ausw�hlen");
    }
    if (getEmail() != null && getEmail().length() > 0)
    {
      if (!EmailValidator.isValid(getEmail()))
      {
        throw new ApplicationException("Ung�ltige Email-Adresse.");
      }
    }

    if (getAdresstyp().getJVereinid() == 1 && getEintritt().getTime() == Einstellungen.NODATE.getTime() && Einstellungen.getEinstellung()
        .getEintrittsdatumPflicht())
    {
      throw new ApplicationException("Bitte Eintrittsdatum eingeben");
    }
    if (getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
    {
      if (getIban() == null || getIban().length() == 0)
      {
        throw new ApplicationException("Bitte IBAN eingeben");
      }
      if (getMandatDatum() == Einstellungen.NODATE)
      {
        throw new ApplicationException("Bitte Datum des Mandat eingeben");
      }
      else if (getMandatDatum().after(new Date()))
      {
        throw new ApplicationException("Datum des Mandat liegt in der Zukunft!");
      }
    }
    if (getIban() != null && getIban().length() != 0)
    {
      try
      {
        new IBAN(getIban());
      }
      catch (SEPAException e)
      {
        throw new ApplicationException("Ung�ltige IBAN");
      }
    }
    if (getBic() != null && getBic().length() != 0)
    {
      try
      {
        new BIC(getBic());
      }
      catch (SEPAException e)
      {
        throw new ApplicationException("Ung�ltige BIC");
      }
    }
    if (getZahlungsrhythmus() == null)
    {
      throw new ApplicationException(
          "Ung�ltiger Zahlungsrhytmus: " + getZahlungsrhythmus());
    }
    if (getSterbetag() != null && getAustritt() == null)
    {
      throw new ApplicationException(
          "Bei verstorbenem Mitglied muss das Austrittsdatum gef�llt sein!");
    }
    if (getAustritt() != null)
    {
      // Person ist ausgetreten
      // Ist das Mitglied Vollzahler in einem Familienverband?
      if (getBeitragsgruppe() != null && getBeitragsgruppe().getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        DBIterator<Mitglied> famang = Einstellungen.getDBService()
            .createList(Mitglied.class);
        famang.addFilter("zahlerid = " + getID());
        famang.addFilter("(austritt is null or austritt > ?)", getAustritt());
        if (famang.hasNext())
        {
          throw new ApplicationException(
              "Dieses Mitglied ist Vollzahler f�r andere. Zun�chst Beitragsart der Angeh�rigen �ndern!");
        }
      }
    }
    // Ist das Mitglied Teil eines Familienverbandes?
    if (getBeitragsgruppe() != null && getBeitragsgruppe().getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER && getZahlerID() != null)
    {
      // ja, suche Vollzahler. Er darf nicht, bzw nicht fr�her, ausgetreten sein!
      DBIterator<Mitglied> zahler = Einstellungen.getDBService()
          .createList(Mitglied.class);
      zahler.addFilter("id = " + getZahlerID());
      if (getAustritt() != null)
        zahler.addFilter("(austritt is not null and austritt < ?)",
            getAustritt());
      Mitglied z = null;
      if (zahler.hasNext())
        z = zahler.next();
      if (z != null && ((Mitglied) z).getAustritt() != null)
      {
        throw new ApplicationException(
            "Der ausgew�hlte Vollzahler ist ausgetreten zu " + z.getAustritt() + ". Bitte anderen Vollzahler w�hlen!");
      }
      if (z != null && ((Mitglied) z).getEintritt()
          .after(new Date()) && ((Mitglied) z).getEintritt()
          .after(getEintritt()))
      {
        throw new ApplicationException(
            "Der ausgew�hlte Vollzahler tritt erst ein zu " + z.getEintritt() + ". Bitte anderen Vollzahler w�hlen!");
      }
    }
    // Check ob das Mitglied vorher ein Vollzahler eines Familienverbandes war
    if (getBeitragsgruppe() != null && getBeitragsgruppe().getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
    {
      // Es darf keine Familienangeh�rigen geben
      DBIterator<Mitglied> famang = Einstellungen.getDBService()
          .createList(Mitglied.class);
      famang.addFilter("zahlerid = " + getID());
      if (famang.hasNext())
      {
        throw new ApplicationException(
            "Dieses Mitglied ist Vollzahler in einem Familienverband.. Zun�chst Beitragsart der Angeh�rigen �ndern!");
      }
    }
    if (getBeitragsgruppe() != null && getBeitragsgruppe().getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER && getZahlerID() == null)
    {
      throw new ApplicationException("Bitte Vollzahler ausw�hlen!");
    }

    // Individueller Beitrag darf nicht kleiner als 0 sein
    if (getIndividuellerBeitrag() != null && getIndividuellerBeitrag() < 0)
    {
      throw new ApplicationException(
          "Individueller Beitrag darf nicht negativ sein!");
    }
  }

  /***
   * Pr�fe die externe Mitgliedsnummer. Ist es ein Mitgliedssatz und ist in den
   * Einstellungen die externe Mitgliedsnummer aktiviert, dann muss eine
   * vorhanden sein und diese muss eindeutig sein.
   *
   * @throws RemoteException
   * @throws ApplicationException
   */
  private void checkExterneMitgliedsnummer()
      throws RemoteException, ApplicationException
  {
    if (getAdresstyp().getJVereinid() != 1)
      return;
    if (Einstellungen.getEinstellung().getExterneMitgliedsnummer() == false)
      return;

    if (getExterneMitgliedsnummer() == null || getExterneMitgliedsnummer().isEmpty())
    {
      throw new ApplicationException("Externe Mitgliedsnummer fehlt");
    }

    DBIterator<Mitglied> mitglieder = Einstellungen.getDBService()
        .createList(Mitglied.class);
    mitglieder.addFilter("id != ?", getID());
    mitglieder.addFilter("externemitgliedsnummer = ?",
        getExterneMitgliedsnummer());
    if (mitglieder.hasNext())
    {
      Mitglied mitglied = (Mitglied) mitglieder.next();
      throw new ApplicationException(
          "Die externe Mitgliedsnummer wird bereits verwendet f�r Mitglied : " + mitglied.getAttribute(
              "namevorname"));
    }

  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      plausi();
    }
    catch (RemoteException e)
    {
      String fehler = "Mitglied kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("foto".equals(field))
    {
      return Mitgliedfoto.class;
    }
    if ("adresstyp".equals(field))
    {
      return Adresstyp.class;
    }
    return null;
  }

  @Override
  public void setAdresstyp(Integer adresstyp) throws RemoteException
  {
    setAttribute("adresstyp", adresstyp);
  }

  @Override
  public Adresstyp getAdresstyp() throws RemoteException
  {
    return (Adresstyp) getAttribute("adresstyp");
  }

  @Override
  public void setExterneMitgliedsnummer(String extnr) throws RemoteException
  {
    setAttribute("externemitgliedsnummer", extnr);
  }

  @Override
  public String getExterneMitgliedsnummer() throws RemoteException
  {
    return (String) getAttribute("externemitgliedsnummer");
  }

  @Override
  public String getPersonenart() throws RemoteException
  {
    return (String) getAttribute("personenart");
  }

  @Override
  public void setPersonenart(String personenart) throws RemoteException
  {
    setAttribute("personenart", personenart);
  }

  @Override
  public String getAnrede() throws RemoteException
  {
    return (String) getAttribute("anrede");
  }

  @Override
  public void setAnrede(String anrede) throws RemoteException
  {
    setAttribute("anrede", anrede);
  }

  @Override
  public String getTitel() throws RemoteException
  {
    String t = (String) getAttribute("titel");
    if (t == null)
    {
      t = "";
    }
    return t;
  }

  @Override
  public void setTitel(String titel) throws RemoteException
  {
    setAttribute("titel", titel);
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public String getVorname() throws RemoteException
  {
    return (String) getAttribute("vorname");
  }

  @Override
  public void setVorname(String vorname) throws RemoteException
  {
    setAttribute("vorname", vorname);
  }

  @Override
  public String getAdressierungszusatz() throws RemoteException
  {
    if (getAttribute("adressierungszusatz") != null)
    {
      return (String) getAttribute("adressierungszusatz");
    }
    else
    {
      return "";
    }
  }

  @Override
  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException
  {
    setAttribute("adressierungszusatz", adressierungszusatz);
  }

  @Override
  public String getStrasse() throws RemoteException
  {
    if (getAttribute("strasse") != null)
    {
      return (String) getAttribute("strasse");
    }
    else
    {
      return "";
    }
  }

  @Override
  public void setStrasse(String strasse) throws RemoteException
  {
    setAttribute("strasse", strasse);
  }

  @Override
  public String getPlz() throws RemoteException
  {
    if (getAttribute("plz") != null)
    {

      return (String) getAttribute("plz");
    }
    else
    {
      return "";
    }
  }

  @Override
  public void setPlz(String plz) throws RemoteException
  {
    setAttribute("plz", plz);
  }

  @Override
  public String getOrt() throws RemoteException
  {
    if (getAttribute("ort") != null)
    {
      return (String) getAttribute("ort");
    }
    else
    {
      return "";
    }
  }

  @Override
  public void setOrt(String ort) throws RemoteException
  {
    setAttribute("ort", ort);
  }

  @Override
  public String getStaat() throws RemoteException
  {
    return Staat.getStaat(getStaatCode());
  }

  @Override
  public String getStaatCode() throws RemoteException
  {
    String code = (String) getAttribute("staat");
    return Staat.getStaatCode(code);
  }

  @Override
  public void setStaat(String staat) throws RemoteException
  {
    setAttribute("staat", staat);
  }

  @Override
  public String getLeitwegID() throws RemoteException
  {
    return (String) getAttribute("leitwegid");
  }

  @Override
  public void setLeitwegID(String leitwegid) throws RemoteException
  {
    setAttribute("leitwegid", leitwegid);
  }

  @Override
  public Integer getZahlungsweg() throws RemoteException
  {
    return (Integer) getAttribute("zahlungsweg");
  }

  @Override
  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException
  {
    setAttribute("zahlungsweg", zahlungsweg);
  }

  @Override
  public Zahlungsrhythmus getZahlungsrhythmus() throws RemoteException
  {
    if (getAttribute("zahlungsrhytmus") != null)
    {
      return new Zahlungsrhythmus((Integer) getAttribute("zahlungsrhytmus"));
    }
    else
    {
      return null;
    }
  }

  @Override
  public void setZahlungsrhythmus(Integer zahlungsrhythmus)
      throws RemoteException
  {
    setAttribute("zahlungsrhytmus", zahlungsrhythmus);
  }

  @Override
  public void setZahlungstermin(Integer zahlungstermin) throws RemoteException
  {
    setAttribute("zahlungstermin", zahlungstermin);
  }

  @Override
  public Zahlungstermin getZahlungstermin() throws RemoteException
  {
    Integer i = (Integer) getAttribute("zahlungstermin");
    if (i == null)
    {
      return null;
    }
    return Zahlungstermin.getByKey(i);
  }

  @Override
  public Date getMandatDatum() throws RemoteException
  {
    Date d = (Date) getAttribute("mandatdatum");
    if (d == null)
    {
      return Einstellungen.NODATE;
    }
    return d;
  }

  @Override
  public void setMandatDatum(Date mandatdatum) throws RemoteException
  {
    setAttribute("mandatdatum", mandatdatum);
  }

  @Override
  public Integer getMandatVersion() throws RemoteException
  {
    Integer vers = (Integer) getAttribute("mandatversion");
    if (vers == null)
    {
      vers = Integer.valueOf(0);
    }
    return vers;
  }

  @Override
  public void setMandatVersion(Integer mandatversion) throws RemoteException
  {
    setAttribute("mandatversion", mandatversion);
  }

  @Override
  public String getMandatID() throws RemoteException
  {
    int sepaMandatIdSource = Einstellungen.getEinstellung()
        .getSepaMandatIdSource();
    if (sepaMandatIdSource == SepaMandatIdSource.EXTERNE_MITGLIEDSNUMMER)
    {
      return getExterneMitgliedsnummer() + "-" + getMandatVersion();
    }
    else
    {
      return getID() + "-" + getMandatVersion();
    }
  }

  @Override
  public Date getLetzteLastschrift() throws RemoteException
  {
    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        Date letzteLastschrift = Einstellungen.NODATE;
        while (rs.next())
        {
          letzteLastschrift = rs.getDate(1);
        }
        return letzteLastschrift;
      }
    };

    String sql = "select max(abrechnungslauf.FAELLIGKEIT) from lastschrift, abrechnungslauf " + "where lastschrift.ABRECHNUNGSLAUF = abrechnungslauf.id and lastschrift.MITGLIED = ? and lastschrift.mandatid = ?";
    Date d = (Date) Einstellungen.getDBService()
        .execute(sql, new Object[] { getID(), getMandatID() }, rs);

    return d;
  }

  @Override
  public String getBic() throws RemoteException
  {
    String ret = (String) getAttribute("bic");
    if (ret == null)
    {
      return "";
    }
    return ret;
  }

  @Override
  public void setBic(String bic) throws RemoteException
  {
    setAttribute("bic", bic);
  }

  @Override
  public String getIban() throws RemoteException
  {
    String ret = (String) getAttribute("iban");
    if (ret == null)
    {
      return "";
    }
    return ret;
  }

  @Override
  public void setIban(String iban) throws RemoteException
  {
    setAttribute("iban", iban);
  }

  @Override
  public String getKtoiPersonenart() throws RemoteException
  {
    String ret = (String) getAttribute("ktoipersonenart");
    if (ret == null)
    {
      ret = "n";
    }
    return ret;
  }

  @Override
  public void setKtoiPersonenart(String ktoipersonenart) throws RemoteException
  {
    setAttribute("ktoipersonenart", ktoipersonenart);
  }

  @Override
  public String getKtoiAnrede() throws RemoteException
  {
    return (String) getAttribute("ktoianrede");
  }

  @Override
  public void setKtoiAnrede(String ktoianrede) throws RemoteException
  {
    setAttribute("ktoianrede", ktoianrede);
  }

  @Override
  public String getKtoiTitel() throws RemoteException
  {
    return (String) getAttribute("ktoititel");
  }

  @Override
  public void setKtoiTitel(String ktoititel) throws RemoteException
  {
    setAttribute("ktoititel", ktoititel);
  }

  @Override
  public String getKtoiName() throws RemoteException
  {
    return (String) getAttribute("ktoiname");
  }

  @Override
  public void setKtoiName(String ktoiname) throws RemoteException
  {
    setAttribute("ktoiname", ktoiname);
  }

  @Override
  public String getKtoiVorname() throws RemoteException
  {
    return (String) getAttribute("ktoivorname");
  }

  @Override
  public void setKtoiVorname(String ktoivorname) throws RemoteException
  {
    setAttribute("ktoivorname", ktoivorname);
  }

  @Override
  public String getKtoiStrasse() throws RemoteException
  {
    return (String) getAttribute("ktoistrasse");
  }

  @Override
  public void setKtoiStrasse(String ktoistrasse) throws RemoteException
  {
    setAttribute("ktoistrasse", ktoistrasse);
  }

  @Override
  public String getKtoiAdressierungszusatz() throws RemoteException
  {
    return (String) getAttribute("ktoiadressierungszusatz");
  }

  @Override
  public void setKtoiAdressierungszusatz(String ktoiadressierungszusatz)
      throws RemoteException
  {
    setAttribute("ktoiadressierungszusatz", ktoiadressierungszusatz);
  }

  @Override
  public String getKtoiPlz() throws RemoteException
  {
    return (String) getAttribute("ktoiplz");
  }

  @Override
  public void setKtoiPlz(String ktoiplz) throws RemoteException
  {
    setAttribute("ktoiplz", ktoiplz);
  }

  @Override
  public String getKtoiOrt() throws RemoteException
  {
    return (String) getAttribute("ktoiort");
  }

  @Override
  public void setKtoiOrt(String ktoiort) throws RemoteException
  {
    setAttribute("ktoiort", ktoiort);
  }

  @Override
  public String getKtoiStaat() throws RemoteException
  {
    return Staat.getStaat(getKtoiStaatCode());
  }

  @Override
  public String getKtoiStaatCode() throws RemoteException
  {
    String code = (String) getAttribute("ktoistaat");
    return Staat.getStaatCode(code);
  }

  @Override
  public void setKtoiStaat(String ktoistaat) throws RemoteException
  {
    setAttribute("ktoistaat", ktoistaat);
  }

  @Override
  public String getKtoiEmail() throws RemoteException
  {
    return (String) getAttribute("ktoiemail");
  }

  @Override
  public void setKtoiEmail(String ktoiemail) throws RemoteException
  {
    setAttribute("ktoiemail", ktoiemail);
  }

  @Override
  public String getKtoiGeschlecht() throws RemoteException
  {
    return (String) getAttribute("ktoigeschlecht");
  }

  @Override
  public void setKtoiGeschlecht(String ktoigeschlecht) throws RemoteException
  {
    setAttribute("ktoigeschlecht", ktoigeschlecht);
  }

  /**
   * art = 1: Name, Vorname
   */
  @Override
  public String getKontoinhaber(int art) throws RemoteException
  {
    Mitglied m2 = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, getID());
    if (m2.getKtoiVorname() != null && m2.getKtoiVorname().length() > 0)
    {
      m2.setVorname(getKtoiVorname());
      m2.setPersonenart(getKtoiPersonenart());
    }
    if (m2.getKtoiName() != null && m2.getKtoiName().length() > 0)
    {
      m2.setName(getKtoiName());
      m2.setPersonenart(getKtoiPersonenart());
    }
    if (m2.getKtoiAnrede() != null && m2.getKtoiAnrede().length() > 0)
    {
      m2.setAnrede(getKtoiAnrede());
    }
    if (m2.getKtoiTitel() != null && m2.getKtoiTitel().length() > 0)
    {
      m2.setTitel(getKtoiTitel());
    }
    switch (art)
    {
      case 1:
        return Adressaufbereitung.getNameVorname(m2);
    }
    return null;
  }

  @Override
  public Date getGeburtsdatum() throws RemoteException
  {
    Date d = (Date) getAttribute("geburtsdatum");
    if (d == null)
    {
      return Einstellungen.NODATE;
    }
    return d;
  }

  @Override
  public Integer getAlter() throws RemoteException
  {
    Date geburtstag = getGeburtsdatum();
    int altersmodel = Einstellungen.getEinstellung().getAltersModel();
    return Datum.getAlter(geburtstag, altersmodel);
  }

  @Override
  public void setGeburtsdatum(Date geburtsdatum) throws RemoteException
  {
    setAttribute("geburtsdatum", geburtsdatum);
  }

  @Override
  public void setGeburtsdatum(String geburtsdatum) throws RemoteException
  {
    setAttribute("geburtsdatum", toDate(geburtsdatum));
  }

  @Override
  public String getGeschlecht() throws RemoteException
  {
    return (String) getAttribute("geschlecht");
  }

  @Override
  public void setGeschlecht(String geschlecht) throws RemoteException
  {
    setAttribute("geschlecht", geschlecht);
  }

  @Override
  public String getTelefonprivat() throws RemoteException
  {
    String telefon = (String) getAttribute("telefonprivat");
    if (telefon == null)
    {
      telefon = "";
    }
    return telefon;
  }

  @Override
  public void setTelefonprivat(String telefonprivat) throws RemoteException
  {
    setAttribute("telefonprivat", telefonprivat);
  }

  @Override
  public String getTelefondienstlich() throws RemoteException
  {
    String telefon = (String) getAttribute("telefondienstlich");
    if (telefon == null)
    {
      telefon = "";
    }
    return telefon;
  }

  @Override
  public void setTelefondienstlich(String telefondienstlich)
      throws RemoteException
  {
    setAttribute("telefondienstlich", telefondienstlich);
  }

  @Override
  public String getHandy() throws RemoteException
  {
    String telefon = (String) getAttribute("handy");
    if (telefon == null)
    {
      telefon = "";
    }
    return telefon;
  }

  @Override
  public void setHandy(String handy) throws RemoteException
  {
    setAttribute("handy", handy);
  }

  @Override
  public String getEmail() throws RemoteException
  {
    String email = (String) getAttribute("email");
    if (email == null)
    {
      email = "";
    }
    return email;
  }

  @Override
  public void setEmail(String email) throws RemoteException
  {
    setAttribute("email", email);
  }

  @Override
  public Date getEintritt() throws RemoteException
  {
    Date d = (Date) getAttribute("eintritt");
    if (d == null)
    {
      return Einstellungen.NODATE;
    }
    return d;
  }

  @Override
  public void setEintritt(Date eintritt) throws RemoteException
  {
    setAttribute("eintritt", eintritt);
  }

  @Override
  public void setEintritt(String eintritt) throws RemoteException
  {
    setAttribute("eintritt", toDate(eintritt));
  }

  @Override
  public Beitragsgruppe getBeitragsgruppe() throws RemoteException
  {
    Object o = (Object) super.getAttribute("beitragsgruppe");
    if (o == null)
      return null;

    Cache cache = Cache.get(Beitragsgruppe.class, true);
    return (Beitragsgruppe) cache.get(o);
  }

  @Override
  public int getBeitragsgruppeId() throws RemoteException
  {
    return Integer.parseInt(getBeitragsgruppe().getID());
  }

  @Override
  public void setBeitragsgruppe(Integer beitragsgruppe) throws RemoteException
  {
    setAttribute("beitragsgruppe", beitragsgruppe);
  }

  @Override
  public Double getIndividuellerBeitrag() throws RemoteException
  {
    return (Double) getAttribute("individuellerbeitrag");
  }

  @Override
  public void setIndividuellerBeitrag(Double d) throws RemoteException
  {
    setAttribute("individuellerbeitrag", d);
  }

  @Override
  public Mitgliedfoto getFoto() throws RemoteException
  {
    return (Mitgliedfoto) getAttribute("foto");
  }

  @Override
  public void setFoto(Mitgliedfoto foto) throws RemoteException
  {
    setAttribute("foto", foto);
  }

  @Override
  public Mitglied getZahler() throws RemoteException
  {
    Object o = (Object) super.getAttribute("zahlerid");
    if (o == null)
      return null;

    if (o instanceof Mitglied)
      return (Mitglied) o;

    Cache cache = Cache.get(Mitglied.class, true);
    return (Mitglied) cache.get(o);
  }

  @Override
  public Long getZahlerID() throws RemoteException
  {
    Long zahlerid = (Long) getAttribute("zahlerid");
    return zahlerid;
  }

  @Override
  public void setZahlerID(Long id) throws RemoteException
  {
    setAttribute("zahlerid", id);
  }

  @Override
  public Date getAustritt() throws RemoteException
  {
    return (Date) getAttribute("austritt");
  }

  @Override
  public void setAustritt(Date austritt) throws RemoteException
  {
    setAttribute("austritt", austritt);
  }

  @Override
  public void setAustritt(String austritt) throws RemoteException
  {
    setAttribute("austritt", toDate(austritt));
  }

  @Override
  public Date getKuendigung() throws RemoteException
  {
    return (Date) getAttribute("kuendigung");
  }

  @Override
  public void setKuendigung(Date kuendigung) throws RemoteException
  {
    setAttribute("kuendigung", kuendigung);
  }

  @Override
  public void setKuendigung(String kuendigung) throws RemoteException
  {
    setAttribute("kuendigung", toDate(kuendigung));
  }

  @Override
  public Date getSterbetag() throws RemoteException
  {
    return (Date) getAttribute("sterbetag");
  }

  @Override
  public void setSterbetag(Date sterbetag) throws RemoteException
  {
    setAttribute("sterbetag", sterbetag);
  }

  @Override
  public void setSterbetag(String sterbetag) throws RemoteException
  {
    setAttribute("sterbetag", toDate(sterbetag));
  }

  @Override
  public String getVermerk1() throws RemoteException
  {
    return (String) getAttribute("vermerk1");
  }

  @Override
  public void setVermerk1(String vermerk1) throws RemoteException
  {
    setAttribute("vermerk1", vermerk1);
  }

  @Override
  public String getVermerk2() throws RemoteException
  {
    return (String) getAttribute("vermerk2");
  }

  @Override
  public void setVermerk2(String vermerk2) throws RemoteException
  {
    setAttribute("vermerk2", vermerk2);
  }

  @Override
  public void setEingabedatum() throws RemoteException
  {
    setAttribute("eingabedatum", new Date());
  }

  @Override
  public Date getEingabedatum() throws RemoteException
  {
    return (Date) getAttribute("eingabedatum");
  }

  @Override
  public void setLetzteAenderung() throws RemoteException
  {
    setAttribute("letzteaenderung", new Date());
  }

  @Override
  public Date getLetzteAenderung() throws RemoteException
  {
    return (Date) getAttribute("letzteaenderung");
  }

  @Override
  public boolean isAngemeldet(Date stichtag) throws RemoteException
  {
    return getEintritt() != null && !stichtag.before(
        getEintritt()) && (getAustritt() == null || getAustritt().after(
        stichtag));
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("idint"))
    {
      return Integer.valueOf(getID());
    }
    if (fieldName.equals("namevorname"))
    {
      return Adressaufbereitung.getNameVorname(this);
    }
    else if (fieldName.equals("vornamename"))
    {
      return Adressaufbereitung.getVornameName(this);
    }
    else if (fieldName.equals("empfaenger"))
    {
      return Adressaufbereitung.getAdressfeld(this);
    }
    else if (fieldName.startsWith("zusatzfelder_"))
    {
      Long l = (Long) super.getAttribute("beitragsgruppe");
      if (l == null)
        return null;

      Cache cache = Cache.get(Beitragsgruppe.class, true);
      cache.get(l);

      DBIterator<Felddefinition> it = Einstellungen.getDBService()
          .createList(Felddefinition.class);
      it.addFilter("name = ?", new Object[] { fieldName.substring(13) });
      Felddefinition fd = (Felddefinition) it.next();
      it = Einstellungen.getDBService().createList(Zusatzfelder.class);
      it.addFilter("felddefinition = ? AND mitglied = ?",
          new Object[] { fd.getID(), getID() });
      if (it.hasNext())
      {
        Zusatzfelder zf = (Zusatzfelder) it.next();
        switch (fd.getDatentyp())
        {
          case Datentyp.ZEICHENFOLGE:
            return zf.getFeld();
          case Datentyp.DATUM:
            return zf.getFeldDatum();
          case Datentyp.GANZZAHL:
            return zf.getFeldGanzzahl();
          case Datentyp.JANEIN:
            return zf.getFeldJaNein();
          case Datentyp.WAEHRUNG:
            return zf.getFeldWaehrung();
        }
      }
      else
      {
        switch (fd.getDatentyp())
        {
          case Datentyp.GANZZAHL:
            return null;
          default:
            return "";
        }
      }
    }
    else if ("alter".equals(fieldName))
    {
      return getAlter();
    }
    else if ("beitragsgruppe".equals(fieldName))
      return getBeitragsgruppe();
    return super.getAttribute(fieldName);
  }

  @Override
  public void addVariable(String name, String wert)
  {
    variable.put(name, wert);
  }

  @Override
  public Map<String, String> getVariablen()
  {
    return variable;
  }

  private Date toDate(String datum)
  {
    Date d = null;

    try
    {
      d = new JVDateFormatTTMMJJJJ().parse(datum);
    }
    catch (Exception e)
    {
      //
    }
    return d;
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<MitgliedDokument> docs = service.createList(
        MitgliedDokument.class);
    docs.addFilter("referenz = ?", new Object[] { this.getID() });
    while (docs.hasNext())
    {
      QueryMessage qm = new QueryMessage(docs.next().getUUID(), null);
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
    }
    super.delete();
  }

  public static MitgliedDummy getDummy()
  {
    if (DUMMY_INSTANCE == null)
    {
      try
      {
        DUMMY_INSTANCE = new MitgliedDummy();
      }
      catch (RemoteException e)
      {
        throw new RuntimeException(e);
      }
    }

    return DUMMY_INSTANCE;
  }

  private static final class MitgliedDummy extends MitgliedImpl
  {
    private static final long serialVersionUID = 1L;

    private final static String PERSONENART = "n";

    private final static String ANREDE = "Herrn";

    private final static String TITEL = "Dr. Dr.";

    private final static String NAME = "Wichtig";

    private final static String VORNAME = "Willi";

    private final static String STRASSE = "Bahnhofstr. 22";

    private final static String ADRESSZUSATZ = "Hinterhof bei M�ller";

    private final static String PLZ = "12345";

    private final static String ORT = "Testenhausen";

    private final static String STAAT = "Deutschland";

    private final static String BIC = "XXXXXXXXXXX";

    private final static String IBAN = "DE89370400440532013000";

    private final static Integer ZAHLUNGSWEG = 1;

    private final static Date MANDATDATUM = new Date();

    private final static String MANDATID = "12345";

    private final static Date LETZTE_LASTSCHRIFT = new Date();

    private final static String EXTERNE_MITGLIEDSNUMMER = "123456";

    private final static String ID = "1";

    private final static Zahlungsrhythmus ZAHLUNGSRHYTHMUS = new Zahlungsrhythmus(
        12);

    private final static Zahlungstermin ZAHLUNGSTERMIN = Zahlungstermin.HALBJAEHRLICH4;

    private final static Integer MANDATVERSION = 1;

    private final static String MAIL = "willi.wichtig@jverein.de";

    private final static String GESCHLECHT = GeschlechtInput.MAENNLICH;

    private final static Date GEBURTSDATUM = toDate("02.03.1980");

    private final static String TELEFON_PRIVAT = "011/123456";

    private final static String TELEFON_DIENSTLICH = "011/123456789";

    private final static String HANDY = "0170/123456789";

    private final static Date EINTRITT = toDate("01.01.2010");

    private final static Beitragsgruppe BEITRAGSGRUPPE = beitragsgruppe();

    private final static double INDIVIDUELLER_BEITRAG = 123.45;

    private final static long ZAHLERID = 123456;

    private final static Date AUSTRITT = toDate("01.01.2025");

    private final static Date KUENDIGUNG = toDate("01.11.2024");

    private final static Date STERBETAG = toDate(("31.12.2024"));

    private final static String VERMERK1 = "Vermerk 1";

    private final static String VERMERK2 = "Vermerk 2";

    private final static Date EINGABEDATUM = toDate("01.02.2010");

    private final static Date LETTZTE_AENDERUNG = new Date();

    private final static boolean IS_ANGEMELDET = true;

    private final static Map<String, String> VARIABLEN = new HashMap<>();

    private final static Adresstyp ADRESSTYP = new Adresstyp()
    {
      @Override
      public void transactionBegin() throws RemoteException
      {

      }

      @Override
      public void transactionCommit() throws RemoteException
      {

      }

      @Override
      public void transactionRollback() throws RemoteException
      {

      }

      @Override
      public String getBezeichnung() throws RemoteException
      {
        return "Mitglied";
      }

      @Override
      public void setBezeichnung(String bezeichnung) throws RemoteException
      {

      }

      @Override
      public String getBezeichnungPlural() throws RemoteException
      {
        return "Mitglieder";
      }

      @Override
      public void setBezeichnungPlural(String bezeichnungplural)
          throws RemoteException
      {

      }

      @Override
      public int getJVereinid() throws RemoteException
      {
        return 1;
      }

      @Override
      public void setJVereinid(int jvereinid) throws RemoteException
      {

      }

      @Override
      public void load(String s) throws RemoteException
      {

      }

      @Override
      public Object getAttribute(String s) throws RemoteException
      {
        return null;
      }

      @Override
      public String getAttributeType(String s) throws RemoteException
      {
        return null;
      }

      @Override
      public String getPrimaryAttribute() throws RemoteException
      {
        return null;
      }

      @SuppressWarnings("rawtypes")
      @Override
      public DBIterator getList() throws RemoteException
      {
        return null;
      }

      @Override
      public boolean equals(GenericObject genericObject) throws RemoteException
      {
        return false;
      }

      @Override
      public void addDeleteListener(Listener listener) throws RemoteException
      {

      }

      @Override
      public void addStoreListener(Listener listener) throws RemoteException
      {

      }

      @Override
      public void removeDeleteListener(Listener listener) throws RemoteException
      {

      }

      @Override
      public void removeStoreListener(Listener listener) throws RemoteException
      {

      }

      @Override
      public String[] getAttributeNames() throws RemoteException
      {
        return new String[0];
      }

      @Override
      public String getID() throws RemoteException
      {
        return ID;
      }

      @Override
      public void store() throws RemoteException
      {

      }

      @Override
      public void delete() throws RemoteException
      {

      }

      @Override
      public void clear() throws RemoteException
      {

      }

      @Override
      public boolean isNewObject() throws RemoteException
      {
        return false;
      }

      @Override
      public void overwrite(DBObject dbObject) throws RemoteException
      {

      }
    };

    private MitgliedDummy() throws RemoteException
    {
    }

    @Override
    public String getPersonenart() throws RemoteException
    {
      return PERSONENART;
    }

    @Override
    public String getAnrede() throws RemoteException
    {
      return ANREDE;
    }

    @Override
    public String getTitel() throws RemoteException
    {
      return TITEL;
    }

    @Override
    public String getName() throws RemoteException
    {
      return NAME;
    }

    @Override
    public String getVorname() throws RemoteException
    {
      return VORNAME;
    }

    @Override
    public String getStrasse() throws RemoteException
    {
      return STRASSE;
    }

    @Override
    public String getAdressierungszusatz() throws RemoteException
    {
      return ADRESSZUSATZ;
    }

    @Override
    public String getPlz() throws RemoteException
    {
      return PLZ;
    }

    @Override
    public String getOrt() throws RemoteException
    {
      return ORT;
    }

    @Override
    public String getStaat() throws RemoteException
    {
      return STAAT;
    }

    @Override
    public String getBic() throws RemoteException
    {
      return BIC;
    }

    @Override
    public String getIban() throws RemoteException
    {
      return IBAN;
    }

    @Override
    public Integer getZahlungsweg() throws RemoteException
    {
      return ZAHLUNGSWEG;
    }

    @Override
    public Date getMandatDatum() throws RemoteException
    {
      return MANDATDATUM;
    }

    @Override
    public String getMandatID() throws RemoteException
    {
      return MANDATID;
    }

    @Override
    public Date getLetzteLastschrift() throws RemoteException
    {
      return LETZTE_LASTSCHRIFT;
    }

    @Override
    public String getExterneMitgliedsnummer() throws RemoteException
    {
      return EXTERNE_MITGLIEDSNUMMER;
    }

    @Override
    public Adresstyp getAdresstyp() throws RemoteException
    {
      return ADRESSTYP;
    }

    @Override
    public Zahlungsrhythmus getZahlungsrhythmus() throws RemoteException
    {
      return ZAHLUNGSRHYTHMUS;
    }

    @Override
    public Zahlungstermin getZahlungstermin() throws RemoteException
    {
      return ZAHLUNGSTERMIN;
    }

    @Override
    public void setMandatDatum(Date mandatdatum) throws RemoteException
    {

    }

    @Override
    public Integer getMandatVersion() throws RemoteException
    {
      return MANDATVERSION;
    }

    @Override
    public String getKtoiPersonenart() throws RemoteException
    {
      return PERSONENART;
    }

    @Override
    public void setKtoiPersonenart(String ktoipersonenart)
        throws RemoteException
    {

    }

    @Override
    public String getKtoiAnrede() throws RemoteException
    {
      return ANREDE;
    }

    @Override
    public String getKtoiTitel() throws RemoteException
    {
      return TITEL;
    }

    @Override
    public String getKtoiName() throws RemoteException
    {
      return NAME;
    }

    @Override
    public String getKtoiVorname() throws RemoteException
    {
      return VORNAME;
    }

    @Override
    public String getKtoiStrasse() throws RemoteException
    {
      return STRASSE;
    }

    @Override
    public String getKtoiAdressierungszusatz() throws RemoteException
    {
      return ADRESSZUSATZ;
    }

    @Override
    public String getKtoiPlz() throws RemoteException
    {
      return PLZ;
    }

    @Override
    public String getKtoiOrt() throws RemoteException
    {
      return ORT;
    }

    @Override
    public String getKtoiStaat() throws RemoteException
    {
      return STAAT;
    }

    @Override
    public String getKtoiEmail() throws RemoteException
    {
      return MAIL;
    }

    @Override
    public String getKtoiGeschlecht() throws RemoteException
    {
      return GESCHLECHT;
    }

    @Override
    public String getKontoinhaber(int art) throws RemoteException
    {
      return null;
    }

    @Override
    public Date getGeburtsdatum() throws RemoteException
    {
      return GEBURTSDATUM;
    }

    @Override
    public Integer getAlter() throws RemoteException
    {
      return null;
    }

    @Override
    public String getGeschlecht() throws RemoteException
    {
      return GESCHLECHT;
    }

    @Override
    public String getTelefonprivat() throws RemoteException
    {
      return TELEFON_PRIVAT;
    }

    @Override
    public String getTelefondienstlich() throws RemoteException
    {
      return TELEFON_DIENSTLICH;
    }

    @Override
    public String getHandy() throws RemoteException
    {
      return HANDY;
    }

    @Override
    public String getEmail() throws RemoteException
    {
      return MAIL;
    }

    @Override
    public Date getEintritt() throws RemoteException
    {
      return EINTRITT;
    }

    @Override
    public Beitragsgruppe getBeitragsgruppe() throws RemoteException
    {
      return BEITRAGSGRUPPE;
    }

    @Override
    public int getBeitragsgruppeId() throws RemoteException
    {
      return 0;
    }

    @Override
    public Double getIndividuellerBeitrag() throws RemoteException
    {
      return INDIVIDUELLER_BEITRAG;
    }

    @Override
    public Long getZahlerID() throws RemoteException
    {
      return ZAHLERID;
    }

    @Override
    public Date getAustritt() throws RemoteException
    {
      return AUSTRITT;
    }

    @Override
    public Date getKuendigung() throws RemoteException
    {
      return KUENDIGUNG;
    }

    @Override
    public Date getSterbetag() throws RemoteException
    {
      return STERBETAG;
    }

    @Override
    public String getVermerk1() throws RemoteException
    {
      return VERMERK1;
    }

    @Override
    public String getVermerk2() throws RemoteException
    {
      return VERMERK2;
    }

    @Override
    public Date getEingabedatum() throws RemoteException
    {
      return EINGABEDATUM;
    }

    @Override
    public Date getLetzteAenderung() throws RemoteException
    {
      return LETTZTE_AENDERUNG;
    }

    @Override
    public Mitgliedfoto getFoto() throws RemoteException
    {
      return null;
    }

    @Override
    public boolean isAngemeldet(Date stichtag) throws RemoteException
    {
      return IS_ANGEMELDET;
    }

    @Override
    public void addVariable(String name, String wert)
    {
      VARIABLEN.put(name, wert);
    }

    @Override
    public Map<String, String> getVariablen()
    {
      return VARIABLEN;
    }

    @Override
    public Object getAttribute(String s) throws RemoteException
    {
      return null;
    }

    @Override
    public String getPrimaryAttribute()
    {
      return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DBIterator getList() throws RemoteException
    {
      return null;
    }

    @Override
    public boolean equals(GenericObject genericObject) throws RemoteException
    {
      return genericObject instanceof MitgliedDummy;
    }

    @Override
    public void store() throws RemoteException
    {

    }

    @Override
    public void delete() throws RemoteException
    {

    }

    private static Date toDate(String datum)
    {
      Date d = null;

      try
      {
        d = new JVDateFormatTTMMJJJJ().parse(datum);
      }
      catch (Exception ignored)
      {
      }
      return d;
    }

    private static Beitragsgruppe beitragsgruppe()
    {
      try
      {
        DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
            .createList(Beitragsgruppe.class);
        return it.next();
      }
      catch (RemoteException rmi)
      {
        return null;
      }
    }
  }

  public boolean checkSEPA() throws RemoteException, ApplicationException
  {
    if (getZahlungsweg() == null
        || getZahlungsweg() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      throw new ApplicationException(Adressaufbereitung.getNameVorname(this)
          + FEHLER_ZAHLUNGSWEG);
    }
    // Ohne Mandat keine Lastschrift
    if (getMandatDatum() == Einstellungen.NODATE)
    {
      throw new ApplicationException(Adressaufbereitung.getNameVorname(this)
          + FEHLER_MANDAT);
    }
    // Bei Mandaten �lter als 3 Jahre muss es eine Lastschrift
    // innerhalb der letzten 3 Jahre geben
    Calendar sepagueltigkeit = Calendar.getInstance();
    sepagueltigkeit.add(Calendar.MONTH, -36);
    if (getMandatDatum().before(sepagueltigkeit.getTime()))
    {
      Date letzte_lastschrift = getLetzteLastschrift();
      if (letzte_lastschrift == null
          || letzte_lastschrift.before(sepagueltigkeit.getTime()))
      {
        throw new ApplicationException(Adressaufbereitung.getNameVorname(this)
            + FEHLER_ALTER);
      }
    }
    return true;
  }

}
