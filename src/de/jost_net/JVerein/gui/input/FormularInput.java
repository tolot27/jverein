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

package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SelectInput;

/**
 * Combo-Box, fuer die Auswahl von Formularen
 */
public class FormularInput extends SelectInput
{
  public FormularInput(FormularArt art, String id) throws RemoteException
  {
    super(init(art) != null ? PseudoIterator.asList(init(art)) : null, initdefault(id));
  }

  public FormularInput(FormularArt mahnung) throws RemoteException
  {
    super(init(mahnung) != null ? PseudoIterator.asList(init(mahnung)) : null, null);
  }

  /**
   * @return initialisiert die Liste der Optionen.
   * @throws RemoteException
   */
  private static GenericIterator<Formular> init(FormularArt art)
      throws RemoteException
  {
    DBIterator<Formular> it = Einstellungen.getDBService()
        .createList(Formular.class);
    // Add filter only if needed
    if (art == null)
    {
      return it;
    }
    it.addFilter("art = ?", art.getKey());
    return it;
  }

  private static Object initdefault(String id)
  {
    if (id == null || id.isEmpty())
    {
      return null;
    }
    try
    {
      Formular f = (Formular) Einstellungen.getDBService()
          .createObject(Formular.class, id);
      return f;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

}
