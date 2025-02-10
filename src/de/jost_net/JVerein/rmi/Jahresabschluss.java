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
package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

public interface Jahresabschluss extends DBObject
{
  public Date getVon() throws RemoteException;

  public void setVon(Date von) throws RemoteException;

  public Date getBis() throws RemoteException;

  public void setBis(Date von) throws RemoteException;

  public Date getDatum() throws RemoteException;

  public void setDatum(Date datum) throws RemoteException;

  public void setName(String name) throws RemoteException;

  public String getName() throws RemoteException;

  public Double getVerwendungsrueckstand() throws RemoteException;

  public void setVerwendungsrueckstand(Double rueckstand)
      throws RemoteException;

  public Double getZwanghafteWeitergabe() throws RemoteException;

  public void setZwanghafteWeitergabe(Double weitergabe) throws RemoteException;

}
