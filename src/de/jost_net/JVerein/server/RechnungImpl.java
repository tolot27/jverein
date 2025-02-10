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
import java.util.ArrayList;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.IAdresse;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungImpl extends AbstractDBObject implements Rechnung, IAdresse
{

  /**
   * 
   */
  private static final long serialVersionUID = -286067581211521888L;
  private Double ist;

  public RechnungImpl() throws RemoteException
  {
    super();
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public void setMitglied(int mitglied) throws RemoteException
  {
    setAttribute("mitglied", mitglied);
  }

  @Override
  public double getBetrag() throws RemoteException
  {
    return (double) getAttribute("betrag");
  }

  @Override
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
  }

  @Override
  protected String getTableName()
  {
    return "rechnung";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void setFormular(Formular formular) throws RemoteException
  {
    setAttribute("formular", Long.valueOf(formular.getID()));
  }

  @Override
  public Formular getFormular() throws RemoteException
  {
    return (Formular) getAttribute("formular");
  }

  @Override
  public void setDatum(Date date) throws RemoteException
  {
    setAttribute("datum", date);
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }
  
  @Override
  public void setAnrede(String anrede) throws RemoteException
  {
    setAttribute("anrede", anrede);
  }
  
  @Override
  public String getAnrede() throws RemoteException
  {
    return (String) getAttribute("anrede");
  }

  @Override
  public String getTitel() throws RemoteException
  {
    return (String) getAttribute("titel");
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
  public String getStrasse() throws RemoteException
  {
    return (String) getAttribute("strasse");
  }
  
  @Override
  public void setStrasse(String strasse) throws RemoteException
  {
    setAttribute("strasse", strasse);
  }

  @Override
  public String getAdressierungszusatz() throws RemoteException
  {
    return (String) getAttribute("adressierungszusatz");
  }
  
  @Override
  public void setAdressierungszusatz(String adressierungszusatz) throws RemoteException
  {
    setAttribute("adressierungszusatz", adressierungszusatz);
  }

  @Override
  public String getPlz() throws RemoteException
  {
    return (String) getAttribute("plz");
  }
  
  @Override
  public void setPlz(String plz) throws RemoteException
  {
    setAttribute("plz", plz);
  }

  @Override
  public String getOrt() throws RemoteException
  {
    return (String) getAttribute("ort");
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
  public Double getIstSumme() throws RemoteException
  {
    if (ist != null)
    {
      return ist;
    }
    DBService service = Einstellungen.getDBService();
    String sql = "select sum(buchung.betrag) from buchung "
        + "join mitgliedskonto on mitgliedskonto.id = buchung.mitgliedskonto "
        + "where mitgliedskonto.rechnung = "
        + this.getID();

    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        if (!rs.next())
        {
          return Double.valueOf(0.0d);
        }
        return Double.valueOf(rs.getDouble(1));
      }
    };
    ist = Double.valueOf((Double) service.execute(sql, new Object[] {}, rs));
    return ist;
  }
  
  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("id-int".equals(fieldName))
    {
      try
      {
        return Integer.valueOf(getID());
      }
      catch (Exception e)
      {
        Logger.error("unable to parse id: " + getID());
        return getID();
      }
    }
    if ("ist".equals(fieldName))
    {
      return getIstSumme();
    }
    if ("differenz".equals(fieldName))
    {
      return getBetrag() - getIstSumme();
    }
    return super.getAttribute(fieldName);
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
  protected Class<?> getForeignObject(String field)
  {
    if ("formular".equals(field))
    {
      return Formular.class;
    }
    if ("mitglied".equals(field))
    {
      return Mitglied.class;
    }
    return null;
  }
  
  @Override
  public Mitgliedskonto getMitgliedskonto() throws RemoteException
  {
    DBIterator<Mitgliedskonto> it = Einstellungen.getDBService()
        .createList(Mitgliedskonto.class);
    it.addFilter("mitgliedskonto.rechnung = ?", getID());
    if (it.hasNext())
    {
      return it.next();
    }
    return null;
  }

  @Override
  public ArrayList<SollbuchungPosition> getSollbuchungPositionList()
      throws RemoteException
  {
    ArrayList<SollbuchungPosition> sps = new ArrayList<>();
    DBIterator<SollbuchungPosition> it = Einstellungen.getDBService()
        .createList(SollbuchungPosition.class);
    it.join("mitgliedskonto");
    it.addFilter("mitgliedskonto.id = sollbuchungposition.sollbuchung");
    it.addFilter("mitgliedskonto.rechnung = ?", getID());
    it.setOrder("ORDER BY datum");
    while (it.hasNext())
    {
      sps.add((SollbuchungPosition) it.next());
    }
    return sps;
  }

  @Override
  public void fill(Mitgliedskonto mk)
      throws RemoteException, ApplicationException
  {
    Mitglied mitglied = mk.getMitglied();

    if (mitglied == null)
    {
      throw new ApplicationException("Sollbuchung enth�lt kein Mitglied.");
    }
    setMitglied(Integer.parseInt(mitglied.getID()));

    if (mitglied.getKtoiName() == null || mitglied.getKtoiName().length() == 0)
    {
      setPersonenart(mitglied.getPersonenart());
      setAnrede(mitglied.getAnrede());
      setTitel(mitglied.getTitel());
      setName(mitglied.getName());
      setVorname(mitglied.getVorname());
      setStrasse(mitglied.getStrasse());
      setAdressierungszusatz(mitglied.getAdressierungszusatz());
      setPlz(mitglied.getPlz());
      setOrt(mitglied.getOrt());
      setStaat(mitglied.getStaat());
      setGeschlecht(mitglied.getGeschlecht());
    }
    else
    {
      setPersonenart(mitglied.getKtoiPersonenart());
      setAnrede(mitglied.getKtoiAnrede());
      setTitel(mitglied.getKtoiTitel());
      setName(mitglied.getKtoiName());
      setVorname(mitglied.getKtoiVorname());
      setStrasse(mitglied.getKtoiStrasse());
      setAdressierungszusatz(mitglied.getKtoiAdressierungszusatz());
      setPlz(mitglied.getKtoiPlz());
      setOrt(mitglied.getKtoiOrt());
      setStaat(mitglied.getKtoiStaat());
      setGeschlecht(mitglied.getKtoiGeschlecht());
    }
    if (!mitglied.getMandatDatum().equals(Einstellungen.NODATE))
    {
      setMandatDatum(mitglied.getMandatDatum());
    }
    setLeitwegID(mitglied.getLeitwegID());
    setMandatID(mitglied.getMandatID());
    setBIC(mitglied.getBic());
    setIBAN(mitglied.getIban());
    setZahlungsweg(mk.getZahlungsweg());
    setBetrag(mk.getBetrag());
  }

  @Override
  public String getMandatID() throws RemoteException
  {
    return (String) getAttribute("mandatid");
  }

  @Override
  public void setMandatID(String id) throws RemoteException
  {
    setAttribute("mandatid", id); 
  }

  @Override
  public Date getMandatDatum() throws RemoteException
  {
    return (Date) getAttribute("mandatdatum");
  }

  @Override
  public void setMandatDatum(Date datum) throws RemoteException
  {
    setAttribute("mandatdatum", datum);
  }

  @Override
  public String getBIC() throws RemoteException
  {
    return (String) getAttribute("bic");
  }

  @Override
  public void setBIC(String bic) throws RemoteException
  {
    setAttribute("bic", bic); 
  }

  @Override
  public String getIBAN() throws RemoteException
  {
    return (String) getAttribute("iban");
  }

  @Override
  public void setIBAN(String iban) throws RemoteException
  {
    setAttribute("iban", iban);
  }

  @Override
  public Zahlungsweg getZahlungsweg() throws RemoteException {
    if(getAttribute("zahlungsweg") == null)
    {
      return null;
    }
    return new Zahlungsweg((Integer)getAttribute("zahlungsweg"));
  }

  @Override
  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException {
    setAttribute("zahlungsweg", zahlungsweg);
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
}
