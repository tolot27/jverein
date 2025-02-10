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

public class EinstellungenRechnungenView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Rechnungen");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer cont = new ScrolledContainer(getParent());

    cont.addLabelPair("Text Abbuchung", control.getRechnungTextAbbuchung());
    cont.addLabelPair("Text �berweisung",
        control.getRechnungTextUeberweisung());
    cont.addLabelPair("Text Bar", control.getRechnungTextBar());
    cont.addLabelPair("L�nge Rechnungsnummer", control.getZaehlerLaenge());

    cont.addHeadline("QR-Code");
    cont.addLabelPair("Kantenl�nge QR-Code", control.getQRCodeSizeInMm());
    cont.addLabelPair("Verwendungszweck", control.getQRCodeVerwendungszweck());
    cont.addLabelPair("Verwendungszweck hinzuf�gen",
        control.getQRCodePrintVerwendungszweck());
    cont.addLabelPair("Bei einzelner Position Verwendungszweck ersetzen",
        control.getQRCodeSingle());
    cont.addLabelPair("Rechnungsdatum in QR-Code", control.getQRCodeReDa());
    cont.addLabelPair("Rechnungsnummer in QR-Code", control.getQRCodeReNr());
    cont.addLabelPair("Mitgliedsnummer in QR-Code",
        control.getQRCodeMemberNr());
    cont.addLabelPair("Information an Mitglied in QR-Code",
        control.getQRCodeInfoToMember());
    cont.addLabelPair("Texte in QR-Code k�rzen", control.getQRCodeKuerzen());
    cont.addLabelPair("Beschreibungstext f�r QR-Code",
        control.getQRCodeIntro());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_RECHNUNGEN, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreRechnungen();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
