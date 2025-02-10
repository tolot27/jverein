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

import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.SollbuchungPositionDeleteAction;
import de.jost_net.JVerein.gui.view.SollbuchungPositionView;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Formularen.
 */
public class SollbuchungPositionMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Formulare.
   */
  public SollbuchungPositionMenu()
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new EditAction(SollbuchungPositionView.class), "text-x-generic.png"));
    addItem(new CheckedContextMenuItem("L�schen",
        new SollbuchungPositionDeleteAction(), "user-trash-full.png"));
  }
}
