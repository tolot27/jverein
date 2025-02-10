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
import de.jost_net.JVerein.gui.control.EinstellungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ScrolledContainer;

public class EinstellungenAbrechnungView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Abrechnung");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer cont = new ScrolledContainer(getParent());

    cont.addLabelPair("Beitragsmodell", control.getBeitragsmodel());
    cont.addInput(control.getZahlungsrhytmus());
    cont.addInput(control.getZahlungsweg());
    cont.addInput(control.getSEPADatumOffset());
    cont.addInput(control.getDefaultSEPALand());
    cont.addLabelPair("SEPA XML-Version - 1ct �berweisung",
        control.getCt1SepaVersion());
    cont.addLabelPair("SEPA XML-Version - Lastschrift",
        control.getSepaVersion());
    cont.addLabelPair("Verrechnungskonto f�r Lastschriften",
        control.getVerrechnungskonto());
    cont.addLabelPair("Arbeitsstunden Modell",
        control.getArbeitsstundenmodel());
    cont.addLabelPair("Altersstufen f�r gestaffelte Beitr�ge",control.getBeitragAltersgruppen());
    cont.addLabelPair("Abrechnungslauf abschlie�en",
        control.getAbrlAbschliessen());
    cont.addSeparator();
    cont.addHeadline(
        "ACHTUNG! Nur �ndern, wenn noch keine SEPA-Lastschriften durchgef�hrt wurden!");
    cont.addLabelPair("Quelle f�r SEPA-Mandatsreferenz (*)",
        control.getSepamandatidsourcemodel());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_ABRECHNUNG, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreAbrechnung();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
