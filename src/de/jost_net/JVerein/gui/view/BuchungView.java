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
import de.jost_net.JVerein.gui.action.SplitbuchungNeuAction;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.BuchungsControl.Kontenfilter;
import de.jost_net.JVerein.gui.parts.BuchungPart;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class BuchungView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    Kontenfilter art = Kontenfilter.GELDKONTO;
    if (this.getCurrentObject() != null && this.getCurrentObject() instanceof Buchung)
    {
      Buchung bu = (Buchung) this.getCurrentObject();
      if (bu.getKonto() != null && bu.getKonto().getKontoArt() == Kontoart.ANLAGE)
        art = Kontenfilter.ANLAGEKONTO;
    }
    final BuchungsControl control = new BuchungsControl(this, art);

    final boolean buchungabgeschlossen = control.isBuchungAbgeschlossen();

    BuchungPart part = new BuchungPart(control, this, buchungabgeschlossen);
    part.paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGEN, false, "question-circle.png");

    Button saveButton = null;
    if (!control.getBuchung().getSpeicherung())
    {
      saveButton = new Button("Speichern", new Action()
      {
        @Override
        public void handleAction(Object context)
        {
          try
          {
            control.getBuchungSpeichernAction().handleAction(context);
            GUI.startView(SplitBuchungView.class.getName(),
                SplitbuchungsContainer.getMaster());
          }
          catch (Exception e)
          {
            GUI.getStatusBar().setErrorText(e.getMessage());
          }
        }
      }, null, true, "document-save.png");
      saveButton.setEnabled(!buchungabgeschlossen);
      buttons.addButton(saveButton);

      Button saveNextButton = new Button("Speichern und n�chste", new Action()
      {
        @Override
        public void handleAction(Object context)
        {
          try
          {
            control.getBuchungSpeichernAction().handleAction(context);
            if (Math.abs(SplitbuchungsContainer.getSumme(SplitbuchungTyp.HAUPT)
                .doubleValue()
                - SplitbuchungsContainer.getSumme(SplitbuchungTyp.SPLIT)
                    .doubleValue()) >= .01d)
              new SplitbuchungNeuAction().handleAction(context);
            else
              GUI.startView(SplitBuchungView.class.getName(),
                  SplitbuchungsContainer.getMaster());
          }
          catch (Exception e)
          {
            GUI.getStatusBar().setErrorText(e.getMessage());
          }
        }
      }, null, true, "go-next.png");
      saveNextButton.setEnabled(!buchungabgeschlossen);
      buttons.addButton(saveNextButton);
    }
    else
    {
      saveButton = new Button("Speichern", control.getBuchungSpeichernAction(),
          null, true, "document-save.png");
      saveButton.setEnabled(!buchungabgeschlossen);
      buttons.addButton(saveButton);
    }
    buttons.paint(getParent());
  }
}
