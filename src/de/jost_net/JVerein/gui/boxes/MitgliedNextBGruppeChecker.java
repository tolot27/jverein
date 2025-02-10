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
package de.jost_net.JVerein.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedNextBGruppe;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author Rolf Mamat
 */
public class MitgliedNextBGruppeChecker extends AbstractBox
{
  private boolean isAktiv;

  private TablePart aenderungsListenPart;

  private DBObject transaktionObjekt;

  public MitgliedNextBGruppeChecker()
  {
    try
    {
      DBIterator<MitgliedNextBGruppe> it = Einstellungen.getDBService()
          .createList(MitgliedNextBGruppe.class);
      while (it.hasNext())
      {
        MitgliedNextBGruppe mnb = it.next();
        if (mnb.getMitglied() == null)
        {
          MitgliedNextBGruppe mnb2 = Einstellungen.getDBService()
              .createObject(MitgliedNextBGruppe.class, mnb.getID());
          mnb2.delete();
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim l�schen ", e);
      e.printStackTrace();
    }
    isAktiv = mussMitgliedGeaendertWerden();
  }

  private boolean mussMitgliedGeaendertWerden()
  {
    try
    {
      DBIterator<MitgliedNextBGruppe> dbIterator = selektiereMitgliederZumAendern();
      return dbIterator.hasNext();
    }
    catch (RemoteException ex)
    {
      Logger.error("MitgliedsChecker kann nicht initalisiert werden!", ex);
    }
    return false;
  }

  private DBIterator<MitgliedNextBGruppe> selektiereMitgliederZumAendern()
      throws RemoteException
  {
    DBIterator<MitgliedNextBGruppe> dbIterator = Einstellungen.getDBService()
        .createList(MitgliedNextBGruppe.class);
    dbIterator.addFilter(MitgliedNextBGruppe.COL_AB_DATUM + " <= current_date");
    dbIterator
        .setOrder("order by " + MitgliedNextBGruppe.COL_AB_DATUM + " desc");
    return dbIterator;
  }

  @Override
  public String getName()
  {
    return "JVerein Mitgliedsdaten-Check";
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return isAktiv;
  }

  @Override
  public boolean isActive()
  {
    return isAktiv;
  }

  @Override
  public int getDefaultIndex()
  {
    return 0;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup labelGroup = new LabelGroup(parent,
        "Mitgliederdaten wollen angepasst werden:", true);
    labelGroup.addPart(getListeZuAendern());
    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton(new Button("Alle �nderungen durchf�hren",
        new AlleAenderungenDurchfuehrenAction(), null, false, "ok.png"));
    labelGroup.addButtonArea(buttonArea);
  }

  private TablePart getListeZuAendern() throws RemoteException
  {
    if (null == aenderungsListenPart)
    {
      aenderungsListenPart = new TablePart(null);
      aenderungsListenPart.addColumn("Datum", MitgliedNextBGruppe.COL_AB_DATUM,
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      aenderungsListenPart.addColumn("Mitglied",
          MitgliedNextBGruppe.VIEW_NAME_VORNAME);
      aenderungsListenPart.addColumn("Von Beitragsgruppe",
          MitgliedNextBGruppe.VIEW_AKT_BEITRAGSGRUPPE);
      aenderungsListenPart.addColumn("Nach Beitragsgruppe",
          MitgliedNextBGruppe.VIEW_BEITRAGSGRUPPE);

      aenderungsListenPart.setRememberColWidths(true);
      aenderungsListenPart.setRememberOrder(true);
      aenderungsListenPart.setContextMenu(new ListenContextMenu());
    }
    listeAktuallisieren();
    return aenderungsListenPart;
  }

  private void listeAktuallisieren() throws RemoteException
  {
    boolean datenVorhanden = false;
    aenderungsListenPart.removeAll();
    DBIterator<MitgliedNextBGruppe> dbIterator = selektiereMitgliederZumAendern();
    while (dbIterator.hasNext())
    {
      MitgliedNextBGruppe mitgliedBeitraege = dbIterator.next();
      aenderungsListenPart.addItem(mitgliedBeitraege);
      datenVorhanden = true;
    }
    aenderungsListenPart.sort();
    this.isAktiv = datenVorhanden;
  }

  class ListenContextMenu extends ContextMenu
  {
    public ListenContextMenu()
    {
      addItem(new CheckedContextMenuItem("�nderung durchf�hren",
          new AenderungDurchfuehrenAction()));
      addItem(ContextMenuItem.SEPARATOR);
      addItem(new ContextMenuItem("Alle �nderungen durchf�hren",
          new AlleAenderungenDurchfuehrenAction()));
    }
  }

  class AenderungDurchfuehrenAction implements Action
  {
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof MitgliedNextBGruppe))
        throw new ApplicationException("Keine Beitragsgruppe ausgew�hlt");

      aendernMitglied((MitgliedNextBGruppe) context);
    }
  }

  class AlleAenderungenDurchfuehrenAction implements Action
  {
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      aendernAlleMitglieder();
    }
  }

  private void aendernMitglied(MitgliedNextBGruppe mitgliedBeitraege)
      throws ApplicationException
  {
    try
    {
      String name = (String) mitgliedBeitraege
          .getAttribute(MitgliedNextBGruppe.VIEW_NAME_VORNAME);
      if (fragen("Soll Beitragsgruppe ge�ndert werden f�r " + name))
      {
        startTransaktion();

        aenderBeitragsGruppe(mitgliedBeitraege);

        commitTransaktion();
      }
    }
    catch (RemoteException ex)
    {
      rollbackTransaktion();
      Logger.error("Ein Mitglied kann nicht ge�ndert werden", ex);
      throw new ApplicationException("�nderung kann nicht durchgef�hrt werden",
          ex);
    }
    catch (ApplicationException ex)
    {
      rollbackTransaktion();
      throw ex;
    }
  }

  private void aendernAlleMitglieder() throws ApplicationException
  {
    try
    {
      if (fragen("Sollen alle gezeigten Mitglieder ge�ndert werden ?"))
      {
        startTransaktion();

        @SuppressWarnings("unchecked")
        List<MitgliedNextBGruppe> liste = aenderungsListenPart.getItems();
        for (MitgliedNextBGruppe mitgliedBeitraege : liste)
        {
          aenderBeitragsGruppe(mitgliedBeitraege);
        }

        commitTransaktion();
      }
    }
    catch (RemoteException ex)
    {
      rollbackTransaktion();
      Logger.error("Mitglieder k�nnen nicht ge�ndert werden", ex);
      throw new ApplicationException("�nderung kann nicht durchgef�hrt werden",
          ex);
    }
    catch (ApplicationException ex)
    {
      rollbackTransaktion();
      throw ex;
    }
  }

  private boolean fragen(String text) throws ApplicationException
  {
    try
    {
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Beitragsgruppe �ndern");
      d.setText(text);
      Boolean choice = (Boolean) d.open();
      return choice.booleanValue();
    }
    catch (Exception ex)
    {
      Logger.error("YesNoDialog kann nicht gestartet werden", ex);
      throw new ApplicationException(
          "Fehler in der Benutzerschnittstelle. Aktion kann nicht durchgef�hrt werden",
          ex);
    }
  }

  private void aenderBeitragsGruppe(MitgliedNextBGruppe mitgliedBeitraege)
      throws RemoteException, ApplicationException
  {
    Mitglied mitglied = mitgliedBeitraege.getMitglied();
    Beitragsgruppe beitragsGruppe = mitgliedBeitraege.getBeitragsgruppe();
    //Wenn es bisher ein angeh�riger war und jetzt nicht mehr zahlerid entfernen
    if(mitglied.getBeitragsgruppe().getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER && beitragsGruppe.getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
    {
      mitglied.setZahlerID(null);
      //Bei Zahlung �ber Vollzahler Zahlungsweg umstellen
      if(mitglied.getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
        mitglied.setZahlungsweg(Einstellungen.getEinstellung().getZahlungsweg());
    }
    mitglied.setBeitragsgruppe(Integer.valueOf(beitragsGruppe.getID()));
    mitglied.store();
    mitgliedBeitraege.delete();
    aenderungsListenPart.removeItem(mitgliedBeitraege);
    if (aenderungsListenPart.size() == 0)
      this.isAktiv = false;
  }

  private void startTransaktion() throws ApplicationException
  {
    try
    {
      transaktionObjekt = Einstellungen.getDBService()
          .createObject(MitgliedNextBGruppe.class, null);
      transaktionObjekt.transactionBegin();
    }
    catch (RemoteException ex)
    {
      throw new ApplicationException("Transaktion kann nicht gestartet werden",
          ex);
    }
  }

  private void commitTransaktion() throws ApplicationException
  {
    try
    {
      if (null != transaktionObjekt)
      {
        transaktionObjekt.transactionCommit();
        transaktionObjekt = null;
      }
    }
    catch (RemoteException ex)
    {
      throw new ApplicationException("Transaktion kann nicht commmited werden",
          ex);
    }

  }

  private void rollbackTransaktion() throws ApplicationException
  {
    try
    {
      if (null != transaktionObjekt)
      {
        transaktionObjekt.transactionRollback();
        transaktionObjekt = null;
      }
    }
    catch (RemoteException ex)
    {
      throw new ApplicationException(
          "Transaktion kann nicht zur�ck gerollt werden", ex);
    }
  }
}
