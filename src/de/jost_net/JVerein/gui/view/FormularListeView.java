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
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.FormularImportAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.control.FormularControl;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class FormularListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Formulare");

    FormularControl control = new FormularControl(this, null);

    control.getFormularList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FORMULARE, false, "question-circle.png");
    buttons.addButton("Importieren", new FormularImportAction(), null, false,
        "file-import.png");
    buttons.addButton("Neu",
        new NewAction(FormularDetailView.class, Formular.class), null, false,
        "document-new.png");
    buttons.paint(this.getParent());
  }
}
