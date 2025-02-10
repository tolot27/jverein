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
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.QIFDateiEinlesenAction;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.control.QIFBuchungsImportControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * @author Rolf Mamat Dieser View unterst�tzt beim Import von Buchungen aus
 *         anderen Programmen wie z.B. Quicken. Im Ersten Schritt wird die
 *         Exportdatei des externen Programmes in die Datenbank Tabellen
 *         ImportKontoHead und ImportKontoPos eingelesen. Danach kann die
 *         KontoHead einem JVerein Konto zugeordnet werden und die externen
 *         Buchungsklassen den JVerein Buchungsklassen. Sind alle Zuordnungen in
 *         Ordnung kann aus den Import Tabellen in die Buchungen der JVerein
 *         Konten importiert werden.
 */
public class QIFBuchungsImportView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("QIF-Datei-Import");

    QIFBuchungsImportControl control = new QIFBuchungsImportControl(this);
    LabelGroup group = new LabelGroup(getParent(), "Konto Kopfdaten");
    group.addLabelPair("Externes Konto", control.getAuswahlExternesKonto());

    ColumnLayout colLayout = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer bankLinks = new SimpleContainer(colLayout.getComposite());

    bankLinks.addLabelPair("Beschreibung", control.getInputBeschreibungKonto());
    bankLinks.addLabelPair("Er�ffnet", control.getInputEroeffnungsDatum());
    bankLinks.addLabelPair("Eingelesen", control.getInputImportDatum());
    bankLinks.addLabelPair("Gebucht", control.getInputProcessDatum());

    SimpleContainer bankRechts = new SimpleContainer(colLayout.getComposite());
    bankRechts.addLabelPair("Export Datei", control.getInputImportDatei());
    bankRechts.addLabelPair("Er�ffnungs Saldo", control.getInputStartSaldo());
    bankRechts.addLabelPair("Gesamt Saldo", control.getInputEndSaldo());
    bankRechts.addLabelPair("Anzahl Buchungen",
        control.getInputAnzahlBuchungen());

    group.addSeparator();
    group.addHeadline("Zugeordnetes Konto in JVerein..");
    group.addLabelPair("JVerein Konto", control.getAuswahlJVereinKonto());

    LabelGroup poslistGroup = new LabelGroup(getParent(),
        "Importierbare Buchungen im gew�hlten Konto", true);
    control
        .getImportKontoPosList(new EditAction(QIFBuchungsartZuordnenView.class))
        .paint(poslistGroup.getComposite());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.QIFIMPORT, false, "question-circle.png");
    buttons.addButton("Import", new QIFDateiEinlesenAction(), null,
        false, "file-import.png");
    buttons.addButton("Import l�schen",
        control.getAktuellenImportLoeschenAction(), null, false,
        "user-trash-full.png");
    buttons.addButton("Imports l�schen", control.getAlleImportsLoeschenAction(),
        null, false, "list-remove.png");
    buttons.addButton("Buchungsarten zuordnen",
        new EditAction(QIFBuchungsartZuordnenView.class),
        null, false, "zuordnung.png");
    buttons.addButton("Mitglieder zuordnen",
        new StartViewAction(QIFMitgliederZuordnenView.class), null,
        false, "contact-new.png");
    buttons.addButton("Buchungen �bernehmen", control.getPIFPosBuchenAction(),
        null, false, "document-new.png");
    buttons.paint(this.getParent());
  }
}
