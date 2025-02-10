/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.util.ApplicationException;

/**
 * Action zur Generierung von Spendenbescheinigungen aus der Datenbank.<br>
 * Diese Klasse kapselt die Generierung des Standard-Formulars und wird auch bei
 * der Generierung eines Dokuments aus der Detailansicht der
 * Spendenbescheinigung heraus verwendet.
 */
public class SpendenbescheinigungPrintAction implements Action
{

  private Adressblatt adressblatt = Adressblatt.OHNE_ADRESSBLATT;

  private String fileName = null;

  private String text = null;

  private boolean open = false;

  private de.willuhn.jameica.system.Settings settings;

  /**
   * Konstruktor ohne Parameter. Es wird angenommen, dass das Standard-Dokument
   * aufbereitet werden soll.
   */
  public SpendenbescheinigungPrintAction()
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Konstruktor. �ber den Parameter kann festgelegt werden, ob das Standard-
   * oder das individuelle Dokument aufbereitet werden soll.
   * 
   * @param txt
   *          Anschreiben auf PDF
   * @param standard
   *          true=Standard-Dokument, false=individuelles Dokument
   * @param adressblatt
   *          enum Adressblatt
   */
  public SpendenbescheinigungPrintAction(String text, Adressblatt adressblatt, boolean open)
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.adressblatt = adressblatt;
    this.text = text;
    this.open = open;
  }

  /**
   * Konstruktor. �ber den Parameter kann festgelegt werden, ob das Standard-
   * oder das individuelle Dokument aufbereitet werden soll.
   * 
   * @param adressblatt
   *          enum Adressblatt
   */
  public SpendenbescheinigungPrintAction(Adressblatt adressblatt, boolean open)
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.adressblatt = adressblatt;
    this.open = open;
  }

  /**
   * Konstruktor. �ber den Parameter kann festgelegt werden, ob das Standard-
   * oder das individuelle Dokument aufbereitet werden soll.
   * 
   * @param adressblatt
   *          enum Adressblatt
   * @param fileName
   *          Dateiname als Vorgabe inklusive Pfad
   */
  public SpendenbescheinigungPrintAction(Adressblatt adressblatt, String fileName)
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.fileName = fileName;
    this.adressblatt = adressblatt;
  }

  /**
   * Aufbereitung der Spendenbescheinigungen
   * Hinweis: Das bzw. die generierten Formulare werden nicht im Acrobat Reader
   * angezeigt.
   * 
   * @param context
   *          Die Spendenbescheinigung(en)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Spendenbescheinigung[] spbArr = null;
    // Pr�fung des Contexs, vorhanden, eine oder mehrere
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Spendenbescheinigung ausgew�hlt");
    }
    else if (context instanceof Spendenbescheinigung)
    {
      spbArr = new Spendenbescheinigung[] { (Spendenbescheinigung) context };
    }
    else if (context instanceof Spendenbescheinigung[])
    {
      spbArr = (Spendenbescheinigung[]) context;
    }
    else
    {
      return;
    }
    // Aufbereitung
    try
    {
      String path = Einstellungen.getEinstellung()
          .getSpendenbescheinigungverzeichnis();
      if (path == null || path.length() == 0)
      {
        path = settings.getString("lastdir", System.getProperty("user.home"));
      }

      settings.setAttribute("lastdir", path);
      path = path.endsWith(File.separator) ? path : path + File.separator;

      // Check ob Standard Spendenbescheinigungen mit Datum vor 2014
      // gedruckt werden sollen
      GregorianCalendar gc = new GregorianCalendar();
      for (Spendenbescheinigung spb : spbArr)
      {
        gc.setTime(spb.getBescheinigungsdatum());
        Formular spendeformular = spb.getFormular();
        if (spendeformular == null && (gc.get(GregorianCalendar.YEAR) <= 2013))
        {
          String text = "Standard Spendenbescheinigungen vor 2014 werden nicht mehr unterst�tzt!";
          throw new ApplicationException(text);
        }
      }
      File file = null;
      // Start der Aufbereitung der Dokumente
      for (Spendenbescheinigung spb : spbArr)
      {
        String fileName = null;
        if (spbArr.length > 1 || this.fileName == null)
        {
          // Dokumentennamen aus konfiguriertem Verzeichnis und dem
          // DateinamenmusterSpende
          // zusammensetzen, wenn mehr als eine Spendenbescheinigung
          // aufzubereiten
          // oder keine Vorgabe f�r einen Dateinamen gemacht wurde.
          if (spb.getMitglied() != null)
          {
            fileName = new Dateiname(spb.getMitglied(),
                spb.getSpendedatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get();
          }
          else
          {
            fileName = new Dateiname(spb.getZeile1(), spb.getZeile2(),
                spb.getSpendedatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get();
          }
          fileName = path + fileName;
        }
        else
        {
          fileName = this.fileName;
        }
        file = new File(fileName);
        // Aufbereitung des Dokumentes
        if (spb.getFormular() == null)
        {
          generiereSpendenbescheinigungStandardAb2014(spb, fileName, adressblatt);
        }
        else
        {
          Formular fo = (Formular) Einstellungen.getDBService()
              .createObject(Formular.class, spb.getFormular().getID());
          Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);
          map = new AllgemeineMap().getMap(map);
          if(spb.getMitglied() != null)
            map = new MitgliedMap().getMap(spb.getMitglied(), map);
          boolean encrypt = Einstellungen.getEinstellung()
              .getUnterschriftdrucken();
          FormularAufbereitung fa = new FormularAufbereitung(file, false,
              encrypt);
          fa.writeForm(fo, map);
          if (adressblatt != Adressblatt.OHNE_ADRESSBLATT)
          {
            // Neue Seite f�r Anschrift in Fenster in querem Brief
            // oder f�r Anschreiben
            fa.printNeueSeite();
          }
          // Brieffenster drucken bei Spendenbescheinigung
          if (adressblatt == Adressblatt.MIT_ADRESSE ||
              adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
          {
            fa.printAdressfenster(getAussteller(), 
                (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()));
          }
          // Anschreiben drucken
          if (adressblatt == Adressblatt.MIT_ANSCHREIBEN ||
              adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
          {
            fa.printAnschreiben(spb, text);
          }
          fa.closeFormular();
          fo.store();
        }
      }
      String erfolg = (spbArr.length > 1) ? "Die Spendenbescheinigungen wurden erstellt und unter " + path + " gespeichert."
          : "Die Spendenbescheinigung wurde erstellt und unter " + path + " gespeichert.";
      GUI.getStatusBar().setSuccessText(erfolg);
      if (file != null && spbArr.length == 1 && open)
        FileViewer.show(file);
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Aufbereiten der Spendenbescheinigung ("
          + e.getMessage() + ")";
      throw new ApplicationException(fehler);
    }
  }

  /**
   * Generierung des Standard-Dokumentes zu verwenden f�r Spendenbescheinigungen
   * ab 01.01.2014
   * 
   * @param spb
   *          Die Spendenbescheinigung aus der Datenbank
   * @param fileName
   *          Der Dateiname, wohin das Dokument geschrieben werden soll
   * @throws IOException
   * @throws DocumentException
   */
  private void generiereSpendenbescheinigungStandardAb2014(
      Spendenbescheinigung spb, String fileName, Adressblatt adressblatt)
          throws IOException, DocumentException
  {
    final File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);

    Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);
    map = new AllgemeineMap().getMap(map);
    boolean isSammelbestaetigung = spb.isSammelbestaetigung();
    Reporter rpt = new Reporter(fos, 80, 50, 50, 50, true);

    // Aussteller, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn("Aussteller (Bezeichnung und Anschrift der steuerbeg�nstigten Einrichtung)"
        + "\n\n" + getAussteller() + "\n ", Element.ALIGN_LEFT);
    rpt.closeTable();

    rpt.add(new Paragraph(" ", Reporter.getFreeSans(4)));
    if (isSammelbestaetigung)
    {
      rpt.add("Sammelbest�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 9);
    }
    else
    {
      rpt.add("Best�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 9);
    }
    rpt.addLight(
        "im Sinne des � 10b des Einkommenssteuergesetzes an eine der in � 5 Abs. 1 Nr. 9 des "
            + "K�rperschaftssteuergesetzes bezeichneten K�rperschaften, Personenvereinigungen "
            + "oder Verm�gensmassen\n",  8);

    // Name und Anschrift, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn("Name und Anschrift des Zuwendenden\n\n" +
        (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),
        Element.ALIGN_LEFT);
    rpt.closeTable();

    // Betrag und Tag der Zuwendeung, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 150, BaseColor.LIGHT_GRAY);
    if (!isSammelbestaetigung)
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 50, BaseColor.LIGHT_GRAY);
    }
    else
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    }
    rpt.createHeader();
    if (spb.getSpendenart() == Spendenart.SACHSPENDE)
    {
      rpt.addColumn("Wert der Zuwendung -in Ziffern-\n" +
          "-" + Einstellungen.DECIMALFORMAT
          .format(map.get(SpendenbescheinigungVar.BETRAG.getName())) + "-",
          Element.ALIGN_CENTER);
    }
    else
    {
      rpt.addColumn("Betrag der Zuwendung -in Ziffern-\n" +
          "-" + Einstellungen.DECIMALFORMAT
          .format(map.get(SpendenbescheinigungVar.BETRAG.getName())) + "-",
          Element.ALIGN_CENTER);
    }
    rpt.addColumn("-in Buchstaben-\n" +
        (String) map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()),
        Element.ALIGN_CENTER);
    if (!isSammelbestaetigung)
    {
      rpt.addColumn("Tag der Zuwendung\n" +
          (String) map.get(SpendenbescheinigungVar.SPENDEDATUM.getName()),
          Element.ALIGN_LEFT);
    }
    else
    {
      rpt.addColumn("Zeitraum der Sammelbest�tigung\n" +
          (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          Element.ALIGN_LEFT);
    }
    rpt.closeTable();

    if (spb.getSpendenart() == Spendenart.SACHSPENDE)
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      rpt.addColumn("Genaue Bezeichnung der Sachzuwendung mit Alter, Zustand, Kaufpreis usw.\n\n"
          + spb.getBezeichnungSachzuwendung(), Element.ALIGN_LEFT);
      rpt.closeTable();

      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.BETRIEBSVERMOEGEN)
        p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add("     Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsverm�gen. "
          + "Die Zuwendung wurde mit dem Wert der Entnahme (ggf. mit dem niedrigeren gemeinen "
          + "Wert) und nach der Umsatzsteuer, die auf die Entnahme entf�llt, bewertet.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.PRIVATVERMOEGEN)
        p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add("     Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatverm�gen.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.KEINEANGABEN)
        p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add("     Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getUnterlagenWertermittlung())
        p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add("     Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.\n");
      rpt.add(p);
    }

    /*
     * Bei Sammelbest�tigungen ist der Verweis auf Verzicht in der Anlage
     * vermerkt
     */
    String verzicht = "";
    char verzichtJa = (char) 113; // box leer
    char verzichtNein = (char) 53; // X

    if (spb.getAutocreate())
    {
      if (!isSammelbestaetigung && spb.getSpendenart() != Spendenart.SACHSPENDE)
      {
        if (spb.getBuchungen().get(0).getVerzicht().booleanValue())
        {
          verzichtJa = (char) 53; // X
          verzichtNein = (char) 113; // box leer
        }
      }
    }
    else
    {
      if (spb.getErsatzAufwendungen())
      {
        verzichtJa = (char) 53; // X
        verzichtNein = (char) 113; // box leer
      }
    }

    if (!isSammelbestaetigung && spb.getSpendenart() != Spendenart.SACHSPENDE )
    {
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk(
          "Es handelt sich um den Verzicht auf Erstattung von Aufwendungen "));
      p.add(new Chunk(" Ja ", Reporter.getFreeSansBold(8)));
      p.add(new Chunk(verzichtJa,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk("   Nein ", Reporter.getFreeSansBold(8)));
      p.add(new Chunk(verzichtNein,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk("\n\n"));
      rpt.add(p);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(8)));
    }
    if (Einstellungen.getEinstellung().getVorlaeufig())
    {
      // Verein neu gegr�ndet, hat noch keinen Bescheid
      String txt = "     Wir sind wegen "
          + "F�rderung (Angabe des beg�nstigten Zweck / der beg�nstigten Zwecke) ...............\n"
          + "nach dem Freistellungsbescheid bzw. nach der Anlage zum K�rperschaftssteuerbescheid "
          + "des Finanzamtes ..........\n"
          + ", StNr. .........."
          + ", vom ..........."
          + " f�r den letzten Veranlagungszeitraum ........"
          + " nach � 5 Abs. 1 Nr. 9 des K�rperschaftsteuergesetzes von der K�rperschaftsteuer und nach "
          + "� 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.\n ";
      Paragraph p = new Paragraph();
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(txt);
      rpt.add(p);

      txt = "     Die Einhaltung der satzungsgem��en Voraussetzungen nach den �� 51, 59, 60 und 61 "
          + "AO wurde vom Finanzamt "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer()
          + ", mit Bescheid vom "
          + new JVDateFormatTTMMJJJJ()
          .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " nach � 60a AO gesondert festgestellt. Wir f�rdern nach unserer Satzung "
          + Einstellungen.getEinstellung().getBeguenstigterzweck() + ".";
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      p.add(txt);
      rpt.add(p);
    }
    else
    {
      // Verein existiert und hat einen Bescheid bekommen
      String txt = "     Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " nach dem Freistellungsbescheid bzw. nach der Anlage zum K�rperschaftssteuerbescheid "
          + "des Finanzamtes " + Einstellungen.getEinstellung().getFinanzamt()
          + ", StNr. " + Einstellungen.getEinstellung().getSteuernummer()
          + ", vom "
          + new JVDateFormatTTMMJJJJ()
          .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " f�r den letzten Veranlagungszeitraum "
          + new JVDateFormatJJJJ()
          .format(Einstellungen.getEinstellung().getVeranlagungVon())
          + " bis "
          + new JVDateFormatJJJJ()
          .format(Einstellungen.getEinstellung().getVeranlagungBis())
          + " nach � 5 Abs. 1 Nr. 9 des K�rperschaftsteuergesetzes von der K�rperschaftsteuer und nach "
          + "� 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.\n ";
      Paragraph p = new Paragraph();
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 53, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      p.add(txt);
      rpt.add(p);
      txt = "     Die Einhaltung der satzungsgem��en Voraussetzungen nach den �� 51, 59, 60 und 61 "
          + "AO wurde vom Finanzamt ..........\n"
          + ", StNr. ..........."
          + ", mit Bescheid vom ............"
          + " nach � 60a AO gesondert festgestellt. Wir f�rdern nach unserer Satzung "
          + "(Angabe des beg�nstigten Zweck / der beg�nstigten Zwecke) ............. .";
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 113, FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(txt);
      rpt.add(p);

    }

    // Rahmen �ber Unterschrift
    PdfPCell cell = new PdfPCell();
    Paragraph p = new Paragraph();
    p.setFont(Reporter.getFreeSans(8));
    p.setAlignment(Element.ALIGN_LEFT);
    p.setMultipliedLeading(1.5f);
    p.add(new Chunk(
        "Es wird best�tigt, dass die Zuwendung nur zur "
            + Einstellungen.getEinstellung().getBeguenstigterzweck()
            + " verwendet wird.\n  "));
    cell.addElement(p);

    if (spb.getSpendenart() != Spendenart.SACHSPENDE)
    {
      p = new Paragraph();
      p.setFont(Reporter.getFreeSansBold(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk(
          "Nur f�r steuerbeg�nstigte Einrichtungen, bei denen die Mitgliedsbeitr�ge "
              + "steuerlich nicht abziehbar sind:\n"));
      cell.addElement(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      char mitgliedBetraege = (char) 113; // box leer
      if (!Einstellungen.getEinstellung().getMitgliedsbetraege())
      {
        mitgliedBetraege = (char) 53; // X
      }
      p.add(new Chunk(mitgliedBetraege,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk(
          "   Es wird best�tigt, dass es sich nicht um einen Mitgliedsbeitrag handelt, "
              + "dessen Abzug nach � 10b Abs. 1 des Einkommensteuergesetzes ausgeschlossen ist."));
      cell.addElement(p);
    }

    rpt.add(new Paragraph(" ", Reporter.getFreeSans(8)));
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(cell);
    rpt.closeTable();

    if (isSammelbestaetigung)
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(6)));
      rpt.addLight(
          "Es wird best�tigt, dass �ber die in der Gesamtsumme enthaltenen Zuwendungen keine weiteren Best�tigungen, weder formelle Zuwendungsbest�tigungen noch Beitragsquittungen oder �hnliches ausgestellt wurden und werden.\n",
          8);
      rpt.addLight(
          "\nOb es sich um den Verzicht auf Erstattung von Aufwendungen handelt, ist der Anlage zur Sammelbest�tigung zu entnehmen.",
          8);
    }

    boolean unterschriftDrucken = false;
    if (Einstellungen.getEinstellung().getUnterschriftdrucken()
        && Einstellungen.getEinstellung().getUnterschrift() != null
        && spb.isEchteGeldspende())
    {
      unterschriftDrucken = true;
    }

    if (unterschriftDrucken)
    {
      rpt.add("\n", 8);
      rpt.add(Einstellungen.getEinstellung().getUnterschrift(), 400, 55, 0);
    }
    else
    {
      rpt.add("\n\n\n\n", 8);
    }
    rpt.add(
        "\n" + Einstellungen.getEinstellung().getOrt() + ", "
            + new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
            9);

    rpt.addLight(
        "..............................................................................."
            + "...............................................................................\n"
            + "(Ort, Datum und Unterschrift des Zuwendungsempf�ngers)",
            8);

    if (unterschriftDrucken)
    {
      rpt.addLight("\nDie maschinelle Erstellung der Zuwendungsbest�tigung wurde dem "
          + "zust�ndigen Finanzamt " + Einstellungen.getEinstellung().getFinanzamt()
          + " angezeigt.", 8);
    }

    rpt.add("\nHinweis:", 8);
    rpt.addLight(
        "Wer vors�tzlich oder grob fahrl�ssig eine unrichtige Zuwendungsbest�tigung erstellt "
            + "oder veranlasst, dass Zuwendungen nicht zu den in der Zuwendungsbest�tigung "
            + "angegebenen steuerbeg�nstigten Zwecken verwendet werden, haftet f�r die entgangene "
            + "Steuer (� 10b Absatz 4 EStG, � 9 Absatz 3 KStG, � 9 Nummer 5 GewStG).\n"
            + "\n"
            + "Diese Best�tigung wird nicht als Nachweis f�r die steuerliche Ber�cksichtigung der "
            + "Zuwendung anerkannt, wenn das Datum des Freistellungsbescheides l�nger als 5 Jahre "
            + "bzw. das Datum der Feststellung der Einhaltung der satzungsm��igen Voraussetzungen "
            + "nach � 60a Abs. 1 AO l�nger als 3 Jahre seit Ausstellung des Bescheides zur�ckliegt "
            + "(� 63 Abs. 5 AO).",
            7);

    /* Es sind mehrere Spenden f�r diese Spendenbescheinigung vorhanden */
    if (isSammelbestaetigung)
    {
      List<Buchung> buchungen = spb.getBuchungen();

      rpt.newPage();
      rpt.add(getAussteller(), 10);
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n", 12);
      rpt.add("Anlage zur Sammelbest�tigung vom " + (String) map
          .get(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName()), 8);
      rpt.add("f�r den Zeitraum vom "
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          8);

      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));

      /* Kopfzeile */
      rpt.addHeaderColumn("Datum der\nZuwendung", Element.ALIGN_LEFT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Art der\nZuwendung", Element.ALIGN_LEFT, 400,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Verzicht auf die\nErstattung von Aufwendungen",
          Element.ALIGN_LEFT, 300, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();

      boolean printBuchungsart = Einstellungen.getEinstellung()
          .getSpendenbescheinigungPrintBuchungsart();

      /* Buchungszeilen */
      for (Buchung buchung : buchungen)
      {
        rpt.addColumn(buchung.getDatum(), Element.ALIGN_RIGHT);
        String verwendung = "";
        if (printBuchungsart)
        {
          verwendung = buchung.getBuchungsart().getBezeichnung();
        }
        else
        {
          verwendung = buchung.getZweck();
        }
        rpt.addColumn(verwendung, Element.ALIGN_LEFT);
        if (buchung.getVerzicht().booleanValue())
        {
          verzicht = "ja";
        }
        else
        {
          verzicht = "nein";
        }
        rpt.addColumn(verzicht, Element.ALIGN_CENTER);
        rpt.addColumn(Double.valueOf(buchung.getBetrag()));
      }

      /* Summenzeile */
      // String sumString =
      // Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
      rpt.addColumn("Gesamtsumme", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn(Double.valueOf(spb.getBetrag()));
      // rpt.addColumn(sumString, Element.ALIGN_RIGHT,
      // BaseColor.LIGHT_GRAY);

      rpt.closeTable();      
    }

    if (adressblatt != Adressblatt.OHNE_ADRESSBLATT)
    {
      // Neue Seite f�r Anschrift in Fenster in querem Brief
      // oder f�r Anschreiben
      rpt.newPage();
    }

    if (adressblatt == Adressblatt.MIT_ADRESSE ||
        adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
    {
      // Anschrift f�r Fenster in querem Brief
      rpt.add("\n\n\n\n\n\n", 11);
      rpt.addUnderline(getAussteller(),8);
      rpt.addLight((String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),10);
    }

    if (adressblatt == Adressblatt.MIT_ANSCHREIBEN ||
        adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
    {
      // Anschreiben
      rpt.add("\n\n\n", 12);
      Mitglied m = spb.getMitglied();
      if (m != null)
      {
        VelocityContext context = new VelocityContext();
        context.put("dateformat", new JVDateFormatTTMMJJJJ());
        context.put("decimalformat", Einstellungen.DECIMALFORMAT);
        if (m.getEmail() != null)
          context.put("email", m.getEmail());
        Map<String, Object> mmap = new MitgliedMap().getMap(m, null);
        mmap = new AllgemeineMap().getMap(mmap);
        VarTools.add(context, mmap);
        StringWriter wtext = new StringWriter();
        Velocity.evaluate(context, wtext, "LOG", text);
        rpt.addLight(wtext.getBuffer().toString(), 10);
      }
      else
      {
        rpt.addLight(text, 10);
      }
    }

    rpt.close();
    fos.close();
  }

  private String getAussteller() throws RemoteException
  {
    return Einstellungen.getEinstellung().getName() + ", "
        + Einstellungen.getEinstellung().getStrasse() + ", "
        + Einstellungen.getEinstellung().getPlz() + " "
        + Einstellungen.getEinstellung().getOrt();
  }
}