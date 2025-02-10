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

import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeDeleteAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeNaechsteFaelligkeitAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeResetAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeVorherigeFaelligkeitAction;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Zusatzbetr�gen.
 */
public class ZusatzbetraegeMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Zusatzbetr�ge.
   */
  public ZusatzbetraegeMenu(TablePart table)
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten", new ZusatzbetraegeAction(null),
        "text-x-generic.png"));
    addItem(new ZusatzbetragWiederholtItem("Vorheriges F�lligkeitsdatum",
        new ZusatzbetraegeVorherigeFaelligkeitAction(table),
        "office-calendar.png"));
    addItem(new ZusatzbetragWiederholtItem("N�chstes F�lligkeitsdatum",
        new ZusatzbetraegeNaechsteFaelligkeitAction(table),
        "office-calendar.png"));
    addItem(new ZusatzbetragEinmaligItem("Erneut ausf�hren",
        new ZusatzbetraegeResetAction(table), "view-refresh.png"));
    addItem(new CheckedContextMenuItem("L�schen",
        new ZusatzbetraegeDeleteAction(), "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
  }
  
  private static class ZusatzbetragEinmaligItem extends CheckedSingleContextMenuItem
  {
    private ZusatzbetragEinmaligItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Zusatzbetrag)
      {
        Zusatzbetrag z = (Zusatzbetrag) o;
        try
        {
          return z.getIntervall() == IntervallZusatzzahlung.KEIN
              && z.getAusfuehrung() != null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }
  
  private static class ZusatzbetragWiederholtItem extends CheckedSingleContextMenuItem
  {
    private ZusatzbetragWiederholtItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Zusatzbetrag)
      {
        Zusatzbetrag z = (Zusatzbetrag) o;
        try
        {
          return z.getIntervall() != IntervallZusatzzahlung.KEIN ;
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
