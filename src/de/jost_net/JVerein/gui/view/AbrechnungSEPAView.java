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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class AbrechnungSEPAView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Abrechnung");

    final AbrechnungSEPAControl control = new AbrechnungSEPAControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Parameter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    SimpleContainer rigth = new SimpleContainer(cl.getComposite());

    left.addLabelPair("Modus", control.getAbbuchungsmodus());
    left.addLabelPair("F�lligkeit", control.getFaelligkeit());
    if (Einstellungen.getEinstellung()
        .getBeitragsmodel() == Beitragsmodel.FLEXIBEL)
    {
      left.addLabelPair("Abrechnungsmonat", control.getAbrechnungsmonat());
    }
    left.addLabelPair("Stichtag�", control.getStichtag());
    left.addLabelPair("Von Eintrittsdatum", control.getVondatum());
    left.addLabelPair("Bis Austrittsdatum", control.getBisdatum());
    left.addLabelPair("Zahlungsgrund f�r Beitr�ge",
        control.getZahlungsgrund());
    if (Einstellungen.getEinstellung().getZusatzbetrag())
    {
      left.addLabelPair("Zusatzbetr�ge", control.getZusatzbetrag());
    }
    if (Einstellungen.getEinstellung().getKursteilnehmer())
    {
      left.addLabelPair("Kursteilnehmer", control.getKursteilnehmer());
    }
    left.addLabelPair("Sollbuchung(en) zusammenfassen",
        control.getSollbuchungenZusammenfassen());

    rigth.addHeadline("Lastschriften");
    rigth.addLabelPair("Kompakte Abbuchung(en)",
        control.getKompakteAbbuchung());
    rigth.addLabelPair("SEPA-Check tempor�r deaktivieren", control.getSEPACheck());
    rigth.addLabelPair("Lastschrift-PDF erstellen", control.getSEPAPrint());
    rigth.addLabelPair("Abbuchungsausgabe", control.getAbbuchungsausgabe());
    
    rigth.addHeadline("Rechnungen");
    rigth.addLabelPair("Rechnung(en) erstellen�", control.getRechnung());
    rigth.addLabelPair("Rechnung Formular", control.getRechnungFormular());
    rigth.addLabelPair("Rechnung Text", control.getRechnungstext());
    rigth.addLabelPair("Rechnung Datum", control.getRechnungsdatum());

    group.addSeparator();
    group.addText(
        "�) F�r die Berechnung, ob ein Mitglied bereits eingetreten oder ausgetreten ist. "
            + "Und f�r Berechnung ob Zusatzbetr�ge f�llig sind.\n"
            + "�) Es wird f�r jede (zusammengefasste) Sollbuchung eine separate Rechnung erstellt.",
        true);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ABRECHNUNG, false, "question-circle.png");
    buttons.addButton("Fehler/Warnungen/Hinweise", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        GUI.startView(SEPABugsView.class.getName(), null);
      }
    }, null, false, "bug.png");
    buttons.addButton(control.getStartButton());
    buttons.paint(this.getParent());
  }
}
