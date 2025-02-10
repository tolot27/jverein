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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.rmi.Mitgliedskonto;

public class SollbuchungMap
{

  public SollbuchungMap()
  {
  }

  public Map<String, Object> getMap(Mitgliedskonto mk, Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }
    map.put(SollbuchungVar.BUCHUNGSDATUM.getName(), mk.getDatum());
    map.put(SollbuchungVar.ZAHLUNGSGRUND.getName(), mk.getZweck1());
    map.put(SollbuchungVar.BETRAG.getName(), mk.getBetrag());
    map.put(SollbuchungVar.IST.getName(), mk.getIstSumme());
    map.put(SollbuchungVar.DIFFERENZ.getName(),
        mk.getBetrag() - mk.getIstSumme());
    return map;
  }
}
