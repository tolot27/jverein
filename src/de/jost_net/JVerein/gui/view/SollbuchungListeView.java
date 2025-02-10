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
import de.jost_net.JVerein.gui.action.SollbuchungEditAction;
import de.jost_net.JVerein.gui.action.SollbuchungExportAction;
import de.jost_net.JVerein.gui.action.SollbuchungExportAction.EXPORT_TYP;
import de.jost_net.JVerein.gui.action.SollbuchungNeuAction;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SollbuchungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchungen");

    final MitgliedskontoControl control = new MitgliedskontoControl(this);
    control.init("sollbuchung.", null, null);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Zahler", control.getSuchname());
    left.addLabelPair("Mitglied", control.getSuchtext());
    left.addInput(control.getDifferenz());
    left.addLabelPair("Ohne Abbucher", control.getOhneAbbucher());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getDatumvon());
    right.addInput(control.getDatumbis());
    right.addLabelPair("Zahler Mail", control.getMailauswahl());

    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(vor);
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck.setToolTipText("Datumsbereich zur�ck");
    vor.setToolTipText("Datumsbereich vow�rts");

    control.getMitgliedskontoList(new SollbuchungEditAction(),
        new SollbuchungMenu(), false).paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");
    buttons.addButton(new Button("Export",
        new SollbuchungExportAction(EXPORT_TYP.MITGLIEDSKONTO), control, false,
        "document-save.png"));
    buttons.addButton("Neu", new SollbuchungNeuAction(null), control, false,
        "document-new.png");
    buttons.paint(this.getParent());
  }
}
