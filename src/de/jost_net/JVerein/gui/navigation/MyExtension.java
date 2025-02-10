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
package de.jost_net.JVerein.gui.navigation;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.schlevoigt.JVerein.gui.view.BuchungsTexteKorrigierenView;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.AboutAction;
import de.jost_net.JVerein.gui.action.BackupCreateAction;
import de.jost_net.JVerein.gui.action.BackupRestoreAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.KontenrahmenExportAction;
import de.jost_net.JVerein.gui.action.KontenrahmenImportAction;
import de.jost_net.JVerein.gui.action.LesefelddefinitionenAction;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.view.AbrechnungslaufListView;
import de.jost_net.JVerein.gui.view.AnfangsbestandListView;
import de.jost_net.JVerein.gui.view.AnlagenbuchungenListeView;
import de.jost_net.JVerein.gui.view.AnlagenlisteView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzListeView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzUeberpruefungView;
import de.jost_net.JVerein.gui.view.AuswertungKursteilnehmerView;
import de.jost_net.JVerein.gui.view.AuswertungMitgliedView;
import de.jost_net.JVerein.gui.view.AuswertungNichtMitgliedView;
import de.jost_net.JVerein.gui.view.BeitragsgruppeSucheView;
import de.jost_net.JVerein.gui.view.BuchungsartListView;
import de.jost_net.JVerein.gui.view.BuchungsklasseListView;
import de.jost_net.JVerein.gui.view.BuchungsklasseSaldoView;
import de.jost_net.JVerein.gui.view.BuchungslisteView;
import de.jost_net.JVerein.gui.view.DbBereinigenView;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeListeView;
import de.jost_net.JVerein.gui.view.EigenschaftListeView;
import de.jost_net.JVerein.gui.view.EinstellungenAbrechnungView;
import de.jost_net.JVerein.gui.view.EinstellungenAllgemeinView;
import de.jost_net.JVerein.gui.view.EinstellungenAnzeigeView;
import de.jost_net.JVerein.gui.view.EinstellungenBuchfuehrungView;
import de.jost_net.JVerein.gui.view.EinstellungenDateinamenView;
import de.jost_net.JVerein.gui.view.EinstellungenMailView;
import de.jost_net.JVerein.gui.view.EinstellungenMitgliedAnsichtView;
import de.jost_net.JVerein.gui.view.EinstellungenMitgliederSpaltenView;
import de.jost_net.JVerein.gui.view.EinstellungenRechnungenView;
import de.jost_net.JVerein.gui.view.EinstellungenSpendenbescheinigungenView;
import de.jost_net.JVerein.gui.view.EinstellungenStatistikView;
import de.jost_net.JVerein.gui.view.FamilienbeitragView;
import de.jost_net.JVerein.gui.view.FelddefinitionenUebersichtView;
import de.jost_net.JVerein.gui.view.FormularListeView;
import de.jost_net.JVerein.gui.view.FreieFormulareView;
import de.jost_net.JVerein.gui.view.JahresabschlussListView;
import de.jost_net.JVerein.gui.view.JubilaeenView;
import de.jost_net.JVerein.gui.view.KontensaldoView;
import de.jost_net.JVerein.gui.view.KontoListView;
import de.jost_net.JVerein.gui.view.KontoauszugView;
import de.jost_net.JVerein.gui.view.KursteilnehmerSucheView;
import de.jost_net.JVerein.gui.view.LastschriftListeView;
import de.jost_net.JVerein.gui.view.LehrgaengeListeView;
import de.jost_net.JVerein.gui.view.LehrgangsartListeView;
import de.jost_net.JVerein.gui.view.MahnungMailView;
import de.jost_net.JVerein.gui.view.MailUebersichtView;
import de.jost_net.JVerein.gui.view.MailVorlagenUebersichtView;
import de.jost_net.JVerein.gui.view.MigrationView;
import de.jost_net.JVerein.gui.view.MitgliederSucheView;
import de.jost_net.JVerein.gui.view.MitgliedstypenListView;
import de.jost_net.JVerein.gui.view.MittelverwendungListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliederSucheView;
import de.jost_net.JVerein.gui.view.PreNotificationView;
import de.jost_net.JVerein.gui.view.ProjektListView;
import de.jost_net.JVerein.gui.view.ProjektSaldoView;
import de.jost_net.JVerein.gui.view.QIFBuchungsImportView;
import de.jost_net.JVerein.gui.view.RechnungListeView;
import de.jost_net.JVerein.gui.view.RechnungMailView;
import de.jost_net.JVerein.gui.view.SollbuchungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungListeView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.gui.view.StatistikJahrgaengeView;
import de.jost_net.JVerein.gui.view.StatistikMitgliedView;
import de.jost_net.JVerein.gui.view.WiedervorlagelisteView;
import de.jost_net.JVerein.gui.view.ZusatzbetraegelisteView;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.logging.Logger;

public class MyExtension implements Extension
{

  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  @Override
  public void extend(Extendable extendable)
  {
    try
    {
      // Check ob es ein Anlagenkonto gibt
      boolean anlagenkonto = false;
      try
      {
        DBService service = Einstellungen.getDBService();
        String sql = "SELECT konto.id from konto "
            + "WHERE (kontoart = ?) ";
        anlagenkonto = (boolean) service.execute(sql,
            new Object[] { Kontoart.ANLAGE.getKey() }, new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            if (rs.next())
            {
              return true;
            }
            return false;
          }
        });
      }
      catch (Exception e)
      {
        ;
      }
      NavigationItem jverein = (NavigationItem) extendable;
      
      NavigationItem mitglieder = null;
      mitglieder = new MyItem(mitglieder, "Mitglieder", null);
      
      mitglieder.addChild(new MyItem(mitglieder, "Mitglieder",
          new StartViewAction(MitgliederSucheView.class), "user-friends.png"));
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Nicht-Mitglieder",
            new StartViewAction(NichtMitgliederSucheView.class),
            "user-friends.png"));
      }
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Kursteilnehmer",
            new StartViewAction(KursteilnehmerSucheView.class),
            "user-friends.png"));
      }
      DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      it.addFilter("beitragsart = ?",
          new Object[] { ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey() });
      if (it.size() > 0)
      {
        mitglieder.addChild(new MyItem(mitglieder, "Familienbeitrag",
            new StartViewAction(FamilienbeitragView.class), "users.png"));
      }
      
      mitglieder.addChild(new MyItem(mitglieder, "Sollbuchungen",
          new StartViewAction(SollbuchungListeView.class), "calculator.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Rechnungen",
          new StartViewAction(RechnungListeView.class), "file-invoice.png"));
      mitglieder.addChild(new MyItem(mitglieder, "Spendenbescheinigungen",
          new StartViewAction(SpendenbescheinigungListeView.class),
          "file-invoice.png"));
      if (Einstellungen.getEinstellung().getZusatzbetrag())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Zusatzbetr�ge",
            new StartViewAction(ZusatzbetraegelisteView.class),
            "euro-sign.png"));
      }
      if (Einstellungen.getEinstellung().getWiedervorlage())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Wiedervorlagen",
            new StartViewAction(WiedervorlagelisteView.class),
            "office-calendar.png"));
      }
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Lehrg�nge",
            new StartViewAction(LehrgaengeListeView.class),
            "chalkboard-teacher.png"));
      }
      if (Einstellungen.getEinstellung().getArbeitseinsatz())
      {
        mitglieder.addChild(new MyItem(mitglieder, "Arbeitseins�tze",
            new StartViewAction(ArbeitseinsatzListeView.class),
            "screwdriver.png"));
      }
      jverein.addChild(mitglieder);

      NavigationItem buchfuehrung = null;
      buchfuehrung = new MyItem(buchfuehrung, "Buchf�hrung", null);
      // Konten
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Konten",
          new StartViewAction(KontoListView.class), "system-file-manager.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Anfangsbest�nde",
          new StartViewAction(AnfangsbestandListView.class),
          "system-file-manager.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Kontensaldo",
          new StartViewAction(KontensaldoView.class),
          "system-file-manager.png"));
      // Buchungen
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungen",
          new StartViewAction(BuchungslisteView.class),
          "emblem-documents.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungskorrektur",
          new StartViewAction(BuchungsTexteKorrigierenView.class),
          "emblem-documents.png"));
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Buchungsklassensaldo",
          new StartViewAction(BuchungsklasseSaldoView.class),
          "emblem-documents.png"));
      // Projekte
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Projektsaldo",
          new StartViewAction(ProjektSaldoView.class), "screwdriver.png"));
      // Anlagen
      if (anlagenkonto)
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenbuchungen",
            new StartViewAction(AnlagenbuchungenListeView.class),
            "office-chart-area.png"));
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Anlagenverzeichnis",
            new StartViewAction(AnlagenlisteView.class),
            "office-chart-area.png"));
      }
      // Mittelverwendung
      if (Einstellungen.getEinstellung().getMittelverwendung())
      {
        buchfuehrung.addChild(new MyItem(buchfuehrung, "Mittelverwendung",
            new StartViewAction(MittelverwendungListeView.class),
            "gnome-session-switch.png"));
      }
      // Jahresabschluss
      buchfuehrung.addChild(new MyItem(buchfuehrung, "Jahresabschl�sse",
          new StartViewAction(JahresabschlussListView.class),
          "office-calendar.png"));
      jverein.addChild(buchfuehrung);
      
      NavigationItem abrechnung = null;
      abrechnung = new MyItem(abrechnung, "Abrechnung", null);
      abrechnung.addChild(new MyItem(abrechnung, "Abrechnungsl�ufe",
          new StartViewAction(AbrechnungslaufListView.class),
          "calculator.png"));
      abrechnung.addChild(new MyItem(abrechnung, "Lastschriften",
          new StartViewAction(LastschriftListeView.class), "file-invoice.png"));
      jverein.addChild(abrechnung);

      NavigationItem auswertung = null;
      auswertung = new MyItem(auswertung, "Auswertungen", null);
      auswertung.addChild(new MyItem(auswertung, "Mitglieder",
          new StartViewAction(AuswertungMitgliedView.class), "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Nicht-Mitglieder",
          new StartViewAction(AuswertungNichtMitgliedView.class),
          "receipt.png"));
      auswertung.addChild(new MyItem(auswertung, "Jubil�en",
          new StartViewAction(JubilaeenView.class), "receipt.png"));
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        auswertung.addChild(new MyItem(auswertung, "Kursteilnehmer",
            new StartViewAction(AuswertungKursteilnehmerView.class),
            "receipt.png"));
      }
      auswertung.addChild(new MyItem(auswertung, "Mitgliederstatistik",
          new StartViewAction(StatistikMitgliedView.class), "chart-line.png"));
      auswertung.addChild(new MyItem(auswertung, "Jahrgangsstatistik",
          new StartViewAction(StatistikJahrgaengeView.class),
          "chart-line.png"));
      if (Einstellungen.getEinstellung().getArbeitseinsatz())
      {
        auswertung.addChild(new MyItem(mitglieder, "Arbeitseins�tze",
            new StartViewAction(ArbeitseinsatzUeberpruefungView.class),
            "screwdriver.png"));
      }
      jverein.addChild(auswertung);

      NavigationItem mail = null;
      mail = new MyItem(mail, "Druck & Mail", null);
      mail.addChild(new MyItem(mail, "Rechnungen",
          new StartViewAction(RechnungMailView.class), "document-print.png"));
      mail.addChild(new MyItem(mail, "Mahnungen",
          new StartViewAction(MahnungMailView.class), "document-print.png"));
      mail.addChild(new MyItem(mail, "Kontoausz�ge",
          new StartViewAction(KontoauszugView.class), "document-print.png"));
      mail.addChild(new MyItem(mail, "Freie Formulare",
          new StartViewAction(FreieFormulareView.class), "document-print.png"));
      mail.addChild(new MyItem(mail, "Pre-Notification",
          new StartViewAction(PreNotificationView.class),
          "document-print.png"));
      mail.addChild(new MyItem(mail, "Spendenbescheinigungen",
          new StartViewAction(SpendenbescheinigungMailView.class),
          "document-print.png"));
      mail.addChild(
          new MyItem(mail, "Mails",
              new StartViewAction(MailUebersichtView.class),
              "envelope-open.png"));
      mail.addChild(new MyItem(mail, "Mail-Vorlagen",
          new StartViewAction(MailVorlagenUebersichtView.class),
          "envelope-open.png"));
      jverein.addChild(mail);
      
      NavigationItem administration = null;
      administration = new MyItem(administration, "Administration", null);

      NavigationItem administrationEinstellungen = null;
      administrationEinstellungen = new MyItem(administrationEinstellungen,
          "Einstellungen", null);
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Allgemein",
              new StartViewAction(EinstellungenAllgemeinView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Anzeige",
              new StartViewAction(EinstellungenAnzeigeView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Spalten",
              new StartViewAction(EinstellungenMitgliederSpaltenView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Mitglieder Ansicht",
              new StartViewAction(EinstellungenMitgliedAnsichtView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Abrechnung",
              new StartViewAction(EinstellungenAbrechnungView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Dateinamen",
              new StartViewAction(EinstellungenDateinamenView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(
          new MyItem(administrationEinstellungen, "Spendenbescheinigungen",
              new StartViewAction(
                  EinstellungenSpendenbescheinigungenView.class),
              "wrench.png"));
      administrationEinstellungen.addChild(new MyItem(
          administrationEinstellungen, "Buchf�hrung",
          new StartViewAction(EinstellungenBuchfuehrungView.class),
          "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Rechnungen",
              new StartViewAction(EinstellungenRechnungenView.class),
              "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Mail",
              new StartViewAction(EinstellungenMailView.class), "wrench.png"));
      administrationEinstellungen
          .addChild(new MyItem(administrationEinstellungen, "Statistik",
              new StartViewAction(EinstellungenStatistikView.class),
              "wrench.png"));
      administration.addChild(administrationEinstellungen);

      NavigationItem einstellungenmitglieder = null;
      einstellungenmitglieder = new MyItem(einstellungenmitglieder,
          "Mitglieder", null);
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Beitragsgruppen",
          new StartViewAction(BeitragsgruppeSucheView.class), "clone.png"));
      einstellungenmitglieder
          .addChild(new MyItem(einstellungenmitglieder, "Eigenschaftengruppen",
              new StartViewAction(EigenschaftGruppeListeView.class),
              "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Eigenschaften",
          new StartViewAction(EigenschaftListeView.class),
          "document-properties.png"));
      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Zusatzfelder",
          new StartViewAction(FelddefinitionenUebersichtView.class),
          "list.png"));
      if (Einstellungen.getEinstellung().getUseLesefelder())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lesefelder",
            new LesefelddefinitionenAction(null), "list.png"));
      }

      einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Formulare",
          new StartViewAction(FormularListeView.class), "columns.png"));
      if (Einstellungen.getEinstellung().getLehrgaenge())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Lehrgangsarten",
            new StartViewAction(LehrgangsartListeView.class),
            "chalkboard-teacher.png"));
      }
      if (Einstellungen.getEinstellung().getZusatzadressen())
      {
        einstellungenmitglieder.addChild(new MyItem(einstellungenmitglieder, "Mitgliedstypen",
            new StartViewAction(MitgliedstypenListView.class),
            "user-friends.png"));
      }
      administration.addChild(einstellungenmitglieder);
      
      NavigationItem einstellungenbuchfuehrung = null;
      einstellungenbuchfuehrung = new MyItem(einstellungenbuchfuehrung,
          "Buchf�hrung", null);
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsklassen", new StartViewAction(BuchungsklasseListView.class),
          "ellipsis-v.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Buchungsarten", new StartViewAction(BuchungsartListView.class),
          "ellipsis-v.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Export",
              new KontenrahmenExportAction(), "document-save.png"));
      einstellungenbuchfuehrung
          .addChild(new MyItem(einstellungenbuchfuehrung, "Kontenrahmen-Import",
              new KontenrahmenImportAction(), "file-import.png"));
      einstellungenbuchfuehrung.addChild(new MyItem(einstellungenbuchfuehrung,
          "Projekte", new StartViewAction(ProjektListView.class),
          "screwdriver.png"));
      administration.addChild(einstellungenbuchfuehrung);
      
      NavigationItem einstellungenerweitert = null;
      einstellungenerweitert = new MyItem(einstellungenerweitert, "Erweitert",
          null);
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert, "Migration",
          new StartViewAction(MigrationView.class), "file-import.png"));
      einstellungenerweitert
      .addChild(new MyItem(einstellungenerweitert, "QIF-Datei-Import",
              new StartViewAction(QIFBuchungsImportView.class),
              "file-import.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Datenbank-Bereinigung", new StartViewAction(DbBereinigenView.class),
          "placeholder-loading.png"));
      einstellungenerweitert.addChild(new MyItem(einstellungenerweitert,
          "Diagnose-Backup-Export", new BackupCreateAction(), "document-save.png"));
      einstellungenerweitert.addChild(
          new MyItem(einstellungenerweitert, "Diagnose-Backup-Import",
              new BackupRestoreAction(), "file-import.png"));
      administration.addChild(einstellungenerweitert);
      jverein.addChild(administration);
      jverein.addChild(new MyItem(jverein, "Dokumentation",
          new DokumentationAction(), "question-circle.png"));
      jverein.addChild(
          new MyItem(jverein, "�ber", new AboutAction(), "gtk-info.png"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend navigation");
    }

  }
}
