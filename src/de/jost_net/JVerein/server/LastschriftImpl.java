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
import java.util.Date;

import de.jost_net.JVerein.io.IAdresse;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;

public class LastschriftImpl extends AbstractDBObject implements Lastschrift
{

  private static final long serialVersionUID = 380278347818535726L;

  public LastschriftImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "lastschrift";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "id";
  }

  @Override
  protected void deleteCheck()
  {
    //
  }

  @Override
  protected void insertCheck()
  {
    updateCheck();
  }

  @Override
  protected void updateCheck()
  {
    //
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    if ("mitglied".equals(arg0))
    {
      return Mitglied.class;
    }
    if ("kursteilnehmer".equals(arg0))
    {
      return Kursteilnehmer.class;
    }

    return null;
  }

  @Override
  public Abrechnungslauf getAbrechnungslauf() throws RemoteException
  {
	  Object o = super.getAttribute("abrechnungslauf");
	  if (o == null)
	  {
	    return null;
	  }

	  Cache cache = Cache.get(Abrechnungslauf.class, true);
	  return (Abrechnungslauf) cache.get(o);
  }

  @Override
  public void setAbrechnungslauf(int abrechnungslauf) throws RemoteException
  {
    setAttribute("abrechnungslauf", Integer.valueOf(abrechnungslauf));
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public void setMitglied(int mitglied) throws RemoteException
  {
    setAttribute("mitglied", Integer.valueOf(mitglied));
  }

  @Override
  public Kursteilnehmer getKursteilnehmer() throws RemoteException
  {
    return (Kursteilnehmer) getAttribute("kursteilnehmer");
  }

  @Override
  public void setKursteilnehmer(int kursteilnehmer) throws RemoteException
  {
    setAttribute("kursteilnehmer", Integer.valueOf(kursteilnehmer));
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
  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException
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
  public String getEmail() throws RemoteException
  {
    return (String) getAttribute("email");
  }

  @Override
  public void setEmail(String email) throws RemoteException
  {
    setAttribute("email", email);
  }

  @Override
  public Date getMandatDatum() throws RemoteException
  {
    return (Date) getAttribute("mandatdatum");
  }

  @Override
  public void setMandatDatum(Date mandatdatum) throws RemoteException
  {
    setAttribute("mandatdatum", mandatdatum);
  }

  @Override
  public String getMandatID() throws RemoteException
  {
    return (String) getAttribute("mandatid");
  }

  @Override
  public void setMandatID(String mandatid) throws RemoteException
  {
    setAttribute("mandatid", mandatid);
  }

  @Override
  public String getMandatSequence() throws RemoteException
  {
    return (String) getAttribute("mandatsequence");
  }

  @Override
  public void setMandatSequence(String mandatsequence) throws RemoteException
  {
    setAttribute("mandatsequence", mandatsequence);
  }

  @Override
  public String getVerwendungszweck() throws RemoteException
  {
    return (String) getAttribute("verwendungszweck");
  }

  @Override
  public void setVerwendungszweck(String verwendungszweck)
      throws RemoteException
  {
    setAttribute("verwendungszweck", verwendungszweck);
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
  public Double getBetrag() throws RemoteException
  {
    return (Double) getAttribute("betrag");
  }

  @Override
  public void setBetrag(Double betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
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
  public void set(IAdresse adr) throws RemoteException
  {
    this.setAdressierungszusatz(adr.getAdressierungszusatz());
    this.setAnrede(adr.getAnrede());
    this.setName(adr.getName());
    this.setOrt(adr.getOrt());
    this.setPersonenart(adr.getPersonenart());
    this.setPlz(adr.getPlz());
    this.setStaat(adr.getStaat());
    this.setStrasse(adr.getStrasse());
    this.setTitel(adr.getTitel());
    this.setVorname(adr.getVorname());
    this.setGeschlecht(adr.getGeschlecht());
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("faelligkeit"))
    {
      return (Date) getAbrechnungslauf().getFaelligkeit();
    }
    else if(fieldName.equals("abrechnungslauf"))
    {
    	return getAbrechnungslauf();
    }
    else if ("id-int".equals(fieldName))
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
    else
    {
      return super.getAttribute(fieldName);
    }
  }

}
