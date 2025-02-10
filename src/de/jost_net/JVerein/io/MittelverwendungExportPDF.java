/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MittelverwendungControl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MittelverwendungExportPDF
{

  public MittelverwendungExportPDF(ArrayList<MittelverwendungZeile> zeile,
      final File file, Date datumvon, Date datumbis, int tab) throws ApplicationException
  {
    try
    {
      String title = "";
      switch (tab)
      {
        case MittelverwendungControl.FLOW_REPORT:
          title = "Mittelverwendungsrechnung (Zufluss-basiert)";
          break;
        case MittelverwendungControl.SALDO_REPORT:
          title = "Mittelverwendungsrechnung (Saldo-basiert)";
          break;
      }
      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = "Gesch�ftsjahr: " + new JVDateFormatTTMMJJJJ().format(datumvon)
          + " - " + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, title, subtitle,
          zeile.size());
      makeHeader(reporter, tab);

      for (MittelverwendungZeile mvz : zeile)
      {
        switch (mvz.getStatus())
        {
          case MittelverwendungZeile.LEERZEILE:
          {
            String position = " ";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            reporter.addColumn(position, Element.ALIGN_RIGHT);
            reporter.addColumn((String) mvz.getAttribute("bezeichnung"),
                Element.ALIGN_LEFT);
            reporter.addColumn(" ", Element.ALIGN_LEFT);
            reporter.addColumn(" ", Element.ALIGN_LEFT);
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(" ", Element.ALIGN_LEFT);
            }
            break;
          }
          case MittelverwendungZeile.EINNAHME:
          case MittelverwendungZeile.AUSGABE:
          {
            String position = " ";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            reporter.addColumn(position, Element.ALIGN_RIGHT);
            reporter.addColumn(
                (String) mvz.getAttribute("bezeichnung"),
                Element.ALIGN_LEFT);
            reporter.addColumn((Double) mvz.getAttribute("betrag"));
            reporter.addColumn(" ", Element.ALIGN_LEFT);
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn((String) mvz.getAttribute("kommentar"),
                  Element.ALIGN_LEFT);
            }
            break;
          }
          case MittelverwendungZeile.SUMME:
          case MittelverwendungZeile.ART:
          {
            String position = " ";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            String art = (String) mvz.getAttribute("art");
            if (mvz.getStatus() == MittelverwendungZeile.ART)
            {
              reporter.addColumn(art, Element.ALIGN_LEFT);
            }
            else
            {
              reporter.addColumn(position, Element.ALIGN_RIGHT);
            }
            PdfPCell cell = null;
            cell = new PdfPCell(new Phrase(new Chunk(
                reporter.notNull((String) mvz.getAttribute("bezeichnung")),
                Reporter.getFreeSansBold(8))));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            reporter.addColumn(cell);
            reporter.addColumn(" ", Element.ALIGN_LEFT);
            Font f = null;
            Double value = (Double) mvz.getAttribute("summe");
            if (value != null)
            {
              if (value >= 0)
              {
                f = Reporter.getFreeSansBold(8, BaseColor.BLACK);
              }
              else
              {
                f = Reporter.getFreeSansBold(8, BaseColor.RED);
              }
              cell = new PdfPCell(
                  new Phrase(Einstellungen.DECIMALFORMAT.format(value), f));
              cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
              reporter.addColumn(cell);
            }
            else
            {
              reporter.addColumn(" ", Element.ALIGN_RIGHT);
            }
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn((String) mvz.getAttribute("kommentar"),
                  Element.ALIGN_LEFT);
            }
            break;
          }
        }
      }
      GUI.getStatusBar().setSuccessText("Export fertig.");
      reporter.closeTable();
      reporter.close();
      fos.close();
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  private void makeHeader(Reporter reporter, int tab) throws DocumentException
  {
    switch (tab)
    {
      case MittelverwendungControl.FLOW_REPORT:
        reporter.addHeaderColumn("Nr", Element.ALIGN_CENTER, 5,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Mittel", Element.ALIGN_CENTER, 69,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 13,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Summe", Element.ALIGN_CENTER, 13,
            BaseColor.LIGHT_GRAY);
        reporter.createHeader();
        break;
      case MittelverwendungControl.SALDO_REPORT:
        reporter.addHeaderColumn("Art", Element.ALIGN_CENTER, 25,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 27,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 13,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Summe", Element.ALIGN_CENTER, 13,
            BaseColor.LIGHT_GRAY);
        reporter.addHeaderColumn("Kommentar", Element.ALIGN_CENTER, 20,
            BaseColor.LIGHT_GRAY);
        reporter.createHeader();
        break;
    }
  }
}
