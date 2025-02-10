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
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class NewAction implements Action
{
  private Class<? extends AbstractView> viewClass;

  private Class<? extends DBObject> objectClass;

  public NewAction(Class<? extends AbstractView> viewClass,
      Class<? extends DBObject> objectClass)
  {
    this.viewClass = viewClass;
    this.objectClass = objectClass;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      DBObject object = Einstellungen.getDBService().createObject(objectClass,
          null);
      GUI.startView(viewClass, object);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung eines neuen " + objectClass.getSimpleName(),
          e);
    }
  }
}
