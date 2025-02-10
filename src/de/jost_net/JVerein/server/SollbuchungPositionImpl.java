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

import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SollbuchungPositionImpl extends AbstractDBObject
    implements SollbuchungPosition
{

  private static final long serialVersionUID = 1L;

  public SollbuchungPositionImpl() throws RemoteException
  {
    super();
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
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Buchung kann nicht gespeichert werden. Siehe system log");
    }
  }

  public void plausi() throws RemoteException, ApplicationException
  {
    if (getDatum() == null)
    {
      throw new ApplicationException("Bitte Datum eingeben");
    }
    if (getBetrag() == null)
    {
      throw new ApplicationException("Bitte Betrag eingeben");
    }
    if (getZweck() == null || getZweck().length() == 0)
    {
      throw new ApplicationException("Bitte Verwendungszweck eingeben");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  protected String getTableName()
  {
    return "sollbuchungposition";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    if ("sollbuchung".equals(arg0))
    {
      return Mitgliedskonto.class;
    }
    return null;
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
  public Double getSteuersatz() throws RemoteException
  {
    return (Double) getAttribute("steuersatz");
  }

  @Override
  public void setSteuersatz(Double satz) throws RemoteException
  {
    setAttribute("steuersatz", satz);
  }

  @Override
  public Double getNettobetrag() throws RemoteException
  {
    Double betrag = (Double) getAttribute("betrag");
    Double steuersatz = (Double) getAttribute("steuersatz");
    if (steuersatz == null || betrag == null)
    {
      return betrag;
    }
    return betrag / (1 + steuersatz / 100);
  }

  @Override
  public Double getSteuerbetrag() throws RemoteException
  {
    Double betrag = (Double) getAttribute("betrag");
    Double steuersatz = (Double) getAttribute("steuersatz");
    if (steuersatz == null || betrag == null)
    {
      return 0d;
    }
    return betrag * steuersatz / (100 + steuersatz);
  }

  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsart");
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object o = super.getAttribute("buchungsart");
    if (o == null)
      return null;

    if (o instanceof Buchungsart)
      return (Buchungsart) o;

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(o);
  }

  @Override
  public void setBuchungsartId(Long buchungsart) throws RemoteException
  {
    setAttribute("buchungsart", buchungsart);
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object o = super.getAttribute("buchungsklasse");
    if (o == null)
      return null;

    if (o instanceof Buchungsklasse)
      return (Buchungsklasse) o;

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(o);
  }

  @Override
  public Long getBuchungsklasseId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsklasse");
  }

  @Override
  public void setBuchungsklasseId(Long buchungsklasse) throws RemoteException
  {
    setAttribute("buchungsklasse", buchungsklasse);
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  @Override
  public void setDatum(Date datum) throws RemoteException
  {
    setAttribute("datum", datum);
  }

  @Override
  public void setSollbuchung(String id) throws RemoteException
  {
    setAttribute("sollbuchung", id);
  }

  @Override
  public Mitgliedskonto getSollbuchung() throws RemoteException
  {
    return (Mitgliedskonto) getAttribute("sollbuchung");
  }

  @Override
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck", zweck);
  }

  @Override
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }


  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("buchungsart".equals(fieldName))
    {
        return getBuchungsart();
    }
    else if ("buchungsklasse".equals(fieldName))
    {
        return getBuchungsklasse();
    }
    else if ("steuerbetrag".equals(fieldName))
    {
      return getSteuerbetrag();
    }
    else if ("nettobetrag".equals(fieldName))
    {
      return getNettobetrag();
    }
    return super.getAttribute(fieldName);
  }
}
