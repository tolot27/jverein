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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class SollbuchungNeuAction implements Action
{

  private Mitglied m;

  public SollbuchungNeuAction(Mitglied m)
  {
    super();
    this.m = m;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitgliedskonto mk = null;

    try
    {
      mk = (Mitgliedskonto) Einstellungen.getDBService()
          .createObject(Mitgliedskonto.class, null);
      mk.setBetrag(0.0);
      if (m != null)
      {
        if (m.getID() == null)
        {
          throw new ApplicationException(
              "Neues Mitglied bitte erst speichern. Dann k�nnen Zusatzbetr�ge aufgenommen werden.");
        }
        mk.setMitglied(m);
        mk.setZahlungsweg(m.getZahlungsweg());
        mk.setZahler(m.getZahler());
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung einer neuen Sollbuchung", e);
    }
    GUI.startView(new SollbuchungDetailView(), mk);
  }
}
