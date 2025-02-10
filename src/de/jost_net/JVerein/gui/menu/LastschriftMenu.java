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
package de.jost_net.JVerein.gui.menu;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.LastschriftDeleteAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.PreNotificationAction;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Lastschriften
 */
public class LastschriftMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Lastschriften.
   */
  public LastschriftMenu()
  {
    addItem(new CheckedSingleContextMenuItem("Anzeigen",
        new EditAction(LastschriftDetailView.class),
        "text-x-generic.png"));
    addItem(new CheckedContextMenuItem("Pre-Notification",
        new PreNotificationAction(), "document-new.png"));
    addItem(new CheckedContextMenuItem("L�schen", new LastschriftDeleteAction(),
        "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new MitgliedAnzeigenMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
  }
  
  private static class MitgliedAnzeigenMenuItem extends CheckedSingleContextMenuItem
  {
    private MitgliedAnzeigenMenuItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Lastschrift)
      {
        Lastschrift la = (Lastschrift) o;
        try
        {
          return la.getMitglied() != null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }
}
