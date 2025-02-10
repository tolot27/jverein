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

import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

public interface Konto extends DBObject
{
  public String getNummer() throws RemoteException;

  public void setNummer(String nummer) throws RemoteException;

  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public Date getEroeffnung() throws RemoteException;

  public void setEroeffnung(Date eroeffnung) throws RemoteException;

  public Date getAufloesung() throws RemoteException;

  public void setAufloesung(Date aufloesungsdatum) throws RemoteException;

  public Integer getHibiscusId() throws RemoteException;

  public void setHibiscusId(Integer HibiscusId) throws RemoteException;
  
  public Buchungsart getBuchungsart() throws RemoteException;

  public Long getBuchungsartId() throws RemoteException;

  public void setBuchungsartId(Long buchungsartId) throws RemoteException;
  
  public Kontoart getKontoArt() throws RemoteException;

  public void setKontoArt(Kontoart kontoart) throws RemoteException;

  public Buchungsart getAnlagenart() throws RemoteException;

  public Long getAnlagenartId() throws RemoteException;

  public void setAnlagenartId(Long anlagensartId) throws RemoteException;
  
  public Buchungsklasse getBuchungsklasse() throws RemoteException;

  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long anlagenklasseId) throws RemoteException;
  
  public Buchungsart getAfaart() throws RemoteException;

  public Long getAfaartId() throws RemoteException;

  public void setAfaartId(Long afaartId) throws RemoteException;
  
  public Integer getNutzungsdauer() throws RemoteException;

  public void setNutzungsdauer(Integer auszugsnummer) throws RemoteException;
  
  public Double getBetrag() throws RemoteException;

  public void setBetrag(Double betrag) throws RemoteException;
  
  public String getKommentar() throws RemoteException;

  public void setKommentar(String kommentar) throws RemoteException;
  
  public Date getAnschaffung() throws RemoteException;

  public void setAnschaffung(Date anschaffung) throws RemoteException;
  
  public Double getAfaStart() throws RemoteException;

  public void setAfaStart(Double afastart) throws RemoteException;
  
  public Double getAfaDauer() throws RemoteException;

  public void setAfaDauer(Double afadauer) throws RemoteException;
  
  public Double getAfaRestwert() throws RemoteException;

  public void setAfaRestwert(Double afarestwert) throws RemoteException;
  
  public Integer getAfaMode() throws RemoteException;

  public void setAfaMode(Integer afamode) throws RemoteException;

  public Double getSaldo() throws RemoteException;

  public DBIterator<Konto> getKontenEinesJahres(Geschaeftsjahr gj)
      throws RemoteException;
  
  public DBIterator<Konto> getKontenVonBis(Date von, Date bis)
      throws RemoteException;

  public Anlagenzweck getAnlagenzweck() throws RemoteException;

  public void setAnlagenzweck(Anlagenzweck zweck) throws RemoteException;
}
