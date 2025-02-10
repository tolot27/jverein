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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.view.RechnungView;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class SollbuchungRechnungAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof MitgliedskontoNode)
    {
      MitgliedskontoNode mkn = (MitgliedskontoNode) context;

      if (mkn.getType() == MitgliedskontoNode.SOLL)
      {
        try
        {
          context = Einstellungen.getDBService()
              .createObject(Mitgliedskonto.class, mkn.getID());
        }
        catch (RemoteException e)
        {
          throw new ApplicationException("Fehler beim anzeigen der Rechnung!");
        }
      }
    }
    if (context instanceof Mitgliedskonto)
    {
      Mitgliedskonto mk = (Mitgliedskonto) context;
      try
      {
        if (mk.getRechnung() == null)
        {
          throw new ApplicationException(
              "Keine Rechnung zu ausgew�hlter Sollbuchung vorhanden!");
        }
        GUI.startView(RechnungView.class.getName(), mk.getRechnung());
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim anzeigen der Rechnung!");
      }
    }

  }
}
