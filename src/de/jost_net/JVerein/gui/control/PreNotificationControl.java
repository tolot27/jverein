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
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.io.Ct1Ueberweisung;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.keys.Ct1Ausgabe;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class PreNotificationControl extends DruckMailControl
{

  private TabFolder folder = null;

  private FormularAufbereitung fa;

  private DateInput ausfuehrungsdatum;

  private SelectInput ct1ausgabe;

  private TextInput verwendungszweck;

  public PreNotificationControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public TabFolder getFolder(Composite parent)
  {
    if (folder != null)
    {
      return folder;
    }
    folder = new TabFolder(parent, SWT.NONE);
    folder.setSelection(settings.getInt(settingsprefix + "tab.selection", 0));
    return folder;
  }

  public DateInput getAusfuehrungsdatum()
  {
    if (ausfuehrungsdatum != null)
    {
      return ausfuehrungsdatum;
    }
    ausfuehrungsdatum = new DateInput();
    ausfuehrungsdatum.setName("Ausf�hrungsdatum");
    return ausfuehrungsdatum;
  }

  public SelectInput getct1Ausgabe()
  {
    if (ct1ausgabe != null)
    {
      return ct1ausgabe;
    }
    Ct1Ausgabe aus = Ct1Ausgabe.getByKey(
        settings.getInt(settingsprefix + "ct1ausgabe", 
            Ct1Ausgabe.SEPA_DATEI.getKey()));
    if (aus != Ct1Ausgabe.SEPA_DATEI
        && aus != Ct1Ausgabe.HIBISCUS)
    {
      aus = Ct1Ausgabe.HIBISCUS;
    }
    ct1ausgabe = new SelectInput(Ct1Ausgabe.values(), aus);
    ct1ausgabe.setName("Ausgabe");
    return ct1ausgabe;
  }

  public TextInput getVerwendungszweck()
  {
    if (verwendungszweck != null)
    {
      return verwendungszweck;
    }
    verwendungszweck = new TextInput(
        settings.getString("verwendungszweck", ""));
    verwendungszweck.setName("Verwendungszweck");
    return verwendungszweck;
  }

  public Button getStartButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          Object object = currentObject;
          if (object == null)
          {
            if (abrechnungslaufausw != null && abrechnungslaufausw.getValue() != null)
            {
              object = abrechnungslaufausw.getValue();
            }
            else
            {
              GUI.getStatusBar().setErrorText(
                  "Kein Abrechnungslauf oder keine Lastschrift ausgew�hlt");
              return;
            }
          }
          String val = (String) getOutput().getValue();
          String pdfMode = (String) getPdfModus().getValue();

          settings.setAttribute(settingsprefix + "tab.selection", 
              folder.getSelectionIndex());
          saveDruckMailSettings();

          if (val.equals(PDF1))
          {
            generierePDF(object, false, pdfMode);
          }
          if (val.equals(PDF2))
          {
            generierePDF(object, true, pdfMode);
          }
          if (val.equals(EMAIL))
          {
            generiereEMail(object);
          }
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  public Button getStart1ctUeberweisungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          Object object = currentObject;
          if (object == null)
          {
            if (abrechnungslaufausw != null && abrechnungslaufausw.getValue() != null)
            {
              object = abrechnungslaufausw.getValue();
            }
            else
            {
              GUI.getStatusBar().setErrorText(
                  "Kein Abrechnungslauf oder keine Lastschrift ausgew�hlt");
              return;
            }
          }
          Ct1Ausgabe aa = (Ct1Ausgabe) ct1ausgabe.getValue();
          settings.setAttribute(settingsprefix + "ct1ausgabe", aa.getKey());
          if (ausfuehrungsdatum.getValue() == null)
          {
            GUI.getStatusBar().setErrorText("Ausf�hrungsdatum fehlt");
            return;
          }
          Date d = (Date) ausfuehrungsdatum.getValue();
          settings.setAttribute(settingsprefix + "faelligkeitsdatum",
              new JVDateFormatDATETIME().format(d));
          settings.setAttribute(settingsprefix + "verwendungszweck",
              (String) getVerwendungszweck().getValue());
          settings.setAttribute(settingsprefix + "tab.selection", 
              folder.getSelectionIndex());
          generiere1ct(object);
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void generierePDF(Object currentObject, boolean mitMail,
      String pdfMode) throws IOException, ApplicationException
  {
    ArrayList<Lastschrift> lastschriften = new ArrayList<>();
    if (currentObject instanceof Abrechnungslauf)
    {
      Abrechnungslauf abrl = (Abrechnungslauf) currentObject;
      DBIterator<Lastschrift> it = Einstellungen.getDBService()
          .createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      if (!mitMail)
      {
        it.addFilter("(email is null or length(email)=0)");
      }
      it.setOrder("order by name, vorname");
      while (it.hasNext())
      {
        lastschriften.add((Lastschrift) it.next());
      }
      
      if (lastschriften.size() == 0 && !mitMail)
      {
        GUI.getStatusBar().setErrorText(
            "Der Abrechnungslauf hat keine Lastschriften ohne Mailadresse");
        return;
      }
      if (lastschriften.size() == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Der Abrechnungslauf hat keine Lastschriften");
        return;
      }
    }
    else if (currentObject instanceof Lastschrift)
    {
      Lastschrift lastschrift = (Lastschrift) currentObject;
      Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
      if (abrl.getAbgeschlossen())
      {
        GUI.getStatusBar().setErrorText(
            "Die ausgew�hlte Lastschrift ist bereits abgeschlossen");
        return;
      }
      if (!mitMail && lastschrift.getEmail() != null && !lastschrift.getEmail().isEmpty())
      {
        GUI.getStatusBar().setErrorText(
            "Die ausgew�hlte Lastschrift hat eine Mail Adresse");
        return;
      }
      else
      {
        lastschriften.add(lastschrift);
      }
    }
    else if (currentObject instanceof Lastschrift[])
    {
      Lastschrift[] lastschriftarray = (Lastschrift[]) currentObject;
      for (Lastschrift lastschrift : lastschriftarray)
      {
        Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
        if (abrl.getAbgeschlossen())
        {
          GUI.getStatusBar().setErrorText(
              "Die ausgew�hlte Lastschrift mit der Nr " + lastschrift.getID() 
              + " ist bereits abgeschlossen");
          return;
        }
        if (!(!mitMail && lastschrift.getEmail() != null && !lastschrift.getEmail().isEmpty()))
        {
          lastschriften.add(lastschrift);
        }
      }
      if (lastschriften.size() == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Alle ausgew�hlten Lastschriften haben eine Mail Adresse");
        return;
      }
    }
    else
    {
      GUI.getStatusBar().setErrorText(
          "Kein Abrechnungslauf oder keine Lastschrift ausgew�hlt");
      return;
    }
    
    boolean einzelnePdfs = false;
    if (pdfMode.equals(EINZELN_NUMMERIERT)
        || pdfMode.equals(EINZELN_MITGLIEDSNUMMER)
        || pdfMode.equals(EINZELN_NUMMERIERT_UND_MNR))
    {
      einzelnePdfs = true;
    }

    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei w�hlen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("prenotification", "",
        Einstellungen.getEinstellung().getDateinamenmuster(), "pdf").get());
    fd.setFilterExtensions(new String[] { "*.pdf" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".pdf"))
    {
      s = s + ".pdf";
    }
    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
    Formular form = (Formular) getFormular(FormularArt.SEPA_PRENOTIFICATION)
        .getValue();
    if (form == null)
    {
      throw new IOException("kein SEPA Pre-Notification-Formular ausgew�hlt");
    }
    Formular fo = (Formular) Einstellungen.getDBService()
        .createObject(Formular.class, form.getID());
    if (!einzelnePdfs)
    {
      fa = new FormularAufbereitung(file, false, false);
    }
    
    int dateinummer = 0;
    String postfix = ".pdf";
    String prefix = s.substring(0, s.length() - postfix.length());

    for (Lastschrift ls : lastschriften)
    {
      if (einzelnePdfs)
      {
        // schalte Dateinamen um
        StringBuilder sb = new StringBuilder(prefix);
        if (pdfMode.equals(EINZELN_MITGLIEDSNUMMER)
            || pdfMode.equals(EINZELN_NUMMERIERT_UND_MNR))
        {
          sb.append("_");
          sb.append(ls.getMitglied().getID());
        }
        if (pdfMode.equals(EINZELN_NUMMERIERT)
            || pdfMode.equals(EINZELN_NUMMERIERT_UND_MNR))
        {
          sb.append(String.format("_%05d", dateinummer));
        }
        sb.append(postfix);

        final File fx = new File(sb.toString());
        fa = new FormularAufbereitung(fx, false, false);
      }

      aufbereitenFormular(ls, fo);

      if (einzelnePdfs)
      {
        fa.closeFormular();
      }
      dateinummer++;
    }

    fa.showFormular();

  }

  private void generiere1ct(Object currentObject) throws Exception
  {
    ArrayList<Lastschrift> lastschriften = new ArrayList<>();
    if (currentObject instanceof Abrechnungslauf)
    {
      Abrechnungslauf abrl = (Abrechnungslauf) currentObject;
      DBIterator<Lastschrift> it = Einstellungen.getDBService()
          .createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      it.setOrder("order by name, vorname");
      while (it.hasNext())
      {
        lastschriften.add((Lastschrift) it.next());
      }
      if (lastschriften.size() == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Der Abrechnungslauf hat keine Lastschriften");
        return;
      }
    }
    else if (currentObject instanceof Lastschrift)
    {
      Lastschrift lastschrift = (Lastschrift) currentObject;
      Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
      if (abrl.getAbgeschlossen())
      {
        GUI.getStatusBar().setErrorText(
            "Die ausgew�hlte Lastschrift ist bereits abgeschlossen");
        return;
      }
      lastschriften.add((Lastschrift) currentObject);
    }
    else if (currentObject instanceof Lastschrift[])
    {
      Lastschrift[] lastschriftarray = (Lastschrift[]) currentObject;
      for (Lastschrift lastschrift : lastschriftarray)
      {
        Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
        if (abrl.getAbgeschlossen())
        {
          GUI.getStatusBar().setErrorText(
              "Die ausgew�hlte Lastschrift mit der Nr " + lastschrift.getID() 
              + " ist bereits abgeschlossen");
          return;
        }
        lastschriften.add(lastschrift);
      }
    }
    else
    {
      GUI.getStatusBar().setErrorText(
          "Kein Abrechnungslauf oder keine Lastschrift ausgew�hlt");
      return;
    }

    File file = null;
    Ct1Ausgabe aa = Ct1Ausgabe.getByKey(
        settings.getInt(settingsprefix + "ct1ausgabe", 
            Ct1Ausgabe.SEPA_DATEI.getKey()));
    if (aa == Ct1Ausgabe.SEPA_DATEI)
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("SEPA-Ausgabedatei w�hlen.");
      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("1ctueberweisung", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), "xml").get());
      fd.setFilterExtensions(new String[] { "*.xml" });

      String s = fd.open();
      if (s == null || s.length() == 0)
      {
        return;
      }
      settings.setAttribute(settingsprefix + "ausgabedateiname", s);
      if (!s.toLowerCase().endsWith(".xml"))
      {
        s = s + ".xml";
      }
      file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
    }
    String faelligkeitsdatum = settings.getString(settingsprefix + "faelligkeitsdatum", null);
    Date faell = Datum.toDate(faelligkeitsdatum);
    Ct1Ausgabe ct1ausgabe = Ct1Ausgabe.getByKey(
        settings.getInt(settingsprefix + "ct1ausgabe", Ct1Ausgabe.SEPA_DATEI.getKey()));
    String verwendungszweck = settings.getString(settingsprefix + "verwendungszweck", "");
    Ct1Ueberweisung ct1ueberweisung = new Ct1Ueberweisung();
    int anzahl = ct1ueberweisung.write(lastschriften, file, faell, ct1ausgabe,
        verwendungszweck);
    GUI.getStatusBar().setSuccessText("Anzahl �berweisungen: " + anzahl);
  }

  private void generiereEMail(Object currentObject) throws IOException
  {
    ArrayList<Lastschrift> lastschriften = new ArrayList<>();
    if (currentObject instanceof Abrechnungslauf)
    {
      Abrechnungslauf abrl = (Abrechnungslauf) currentObject;
      DBIterator<Lastschrift> it = Einstellungen.getDBService()
          .createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      it.addFilter("email is not null and length(email) > 0");
      it.setOrder("order by name, vorname");
      while (it.hasNext())
      {
        lastschriften.add((Lastschrift) it.next());
      }
      if (lastschriften.size() == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Der Abrechnungslauf hat keine Lastschriften mit Mail Adresse");
        return;
      }
    }
    else if (currentObject instanceof Lastschrift)
    {
      Lastschrift lastschrift = (Lastschrift) currentObject;
      Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
      if (abrl.getAbgeschlossen())
      {
        GUI.getStatusBar().setErrorText(
            "Die ausgew�hlte Lastschrift ist bereits abgeschlossen");
        return;
      }
      if (lastschrift.getEmail() != null && !lastschrift.getEmail().isEmpty())
      {
        lastschriften.add(lastschrift);
      }
      else
      {
        GUI.getStatusBar().setErrorText(
            "Die ausgew�hlte Lastschrift hat keine Mail Adresse");
        return;
      }
    }
    else if (currentObject instanceof Lastschrift[])
    {
      Lastschrift[] lastschriftarray = (Lastschrift[]) currentObject;
      for (Lastschrift lastschrift : lastschriftarray)
      {
        Abrechnungslauf abrl = (Abrechnungslauf) lastschrift.getAbrechnungslauf();
        if (abrl.getAbgeschlossen())
        {
          GUI.getStatusBar().setErrorText(
              "Die ausgew�hlte Lastschrift mit der Nr " + lastschrift.getID() 
              + " ist bereits abgeschlossen");
          return;
        }
        if (lastschrift.getEmail() != null && !lastschrift.getEmail().isEmpty())
        {
          lastschriften.add(lastschrift);
        }
      }
      if (lastschriften.size() == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Keine der ausgew�hlten Lastschriften hat eine Mail Adresse");
        return;
      }
    }
    else
    {
      GUI.getStatusBar().setErrorText(
          "Kein Abrechnungslauf oder keine Lastschrift ausgew�hlt");
      return;
    }

    String betr = (String) getBetreff().getValue();
    String text = (String) getTxt().getValue();
    sendeMail(lastschriften, betr, text);
  }

  private void aufbereitenFormular(Lastschrift ls, Formular fo)
      throws RemoteException, ApplicationException
  {
    Map<String, Object> map = new LastschriftMap().getMap(ls, null);
    map = new AllgemeineMap().getMap(map);
    fa.writeForm(fo, map);
    fo.store();
  }

  private void sendeMail(final ArrayList<Lastschrift> lastschriften, final String betr,
      final String txt) throws RemoteException
  {

    BackgroundTask t = new BackgroundTask()
    {

      private boolean cancel = false;

      @Override
      public void run(ProgressMonitor monitor)
      {
        try
        {
          MailSender sender = new MailSender(
              Einstellungen.getEinstellung().getSmtpServer(),
              Einstellungen.getEinstellung().getSmtpPort(),
              Einstellungen.getEinstellung().getSmtpAuthUser(),
              Einstellungen.getEinstellung().getSmtpAuthPwd(),
              Einstellungen.getEinstellung().getSmtpFromAddress(),
              Einstellungen.getEinstellung().getSmtpFromAnzeigename(),
              Einstellungen.getEinstellung().getMailAlwaysBcc(),
              Einstellungen.getEinstellung().getMailAlwaysCc(),
              Einstellungen.getEinstellung().getSmtpSsl(),
              Einstellungen.getEinstellung().getSmtpStarttls(),
              Einstellungen.getEinstellung().getMailVerzoegerung(),
              Einstellungen.getImapCopyData());

          Velocity.init();
          Logger.debug("preparing velocity context");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          int sentCount = 0;
          int zae = 0;
          int size = lastschriften.size();
          for (Lastschrift ls : lastschriften)
          {
            if(isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Mailversand abgebrochen");
              monitor.setPercentComplete(100);
              return;
            }
            
            VelocityContext context = new VelocityContext();
            context.put("dateformat", new JVDateFormatTTMMJJJJ());
            context.put("decimalformat", Einstellungen.DECIMALFORMAT);
            context.put("email", ls.getEmail());

            Map<String, Object> map = new AllgemeineMap().getMap(null);
            map = new LastschriftMap().getMap(ls, map);
            VarTools.add(context, map);

            StringWriter wtext1 = new StringWriter();
            Velocity.evaluate(context, wtext1, "LOG", betr);

            StringWriter wtext2 = new StringWriter();
            Velocity.evaluate(context, wtext2, "LOG", txt);

            try
            {
              sender.sendMail(ls.getEmail(), wtext1.getBuffer().toString(),
                  wtext2.getBuffer().toString(), new TreeSet<MailAnhang>());

              // Mail in die Datenbank schreiben
              if (ls.getMitglied() != null)
              {
                Mail mail = (Mail) Einstellungen.getDBService()
                    .createObject(Mail.class, null);
                Timestamp ts = new Timestamp(new Date().getTime());
                mail.setBearbeitung(ts);
                mail.setBetreff(wtext1.getBuffer().toString());
                mail.setTxt(wtext2.getBuffer().toString());
                mail.setVersand(ts);
                mail.store();
                MailEmpfaenger empf = (MailEmpfaenger) Einstellungen
                    .getDBService().createObject(MailEmpfaenger.class, null);
                empf.setMail(mail);
                empf.setMitglied(ls.getMitglied());
                empf.setVersand(ts);
                empf.store();
              }

              sentCount++;
              monitor.log(ls.getEmail() + " - versendet");
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Mailversand", e);
              monitor.log(ls.getEmail() + " - " + e.getMessage());
            }
            zae++;
            double proz = (double) zae / (double) size * 100d;
            monitor.setPercentComplete((int) proz);
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") + " verschickt");
        }
        catch (Exception re)
        {
          Logger.error("", re);
          monitor.log(re.getMessage());
        }
      }

      @Override
      public void interrupt()
      {
        this.cancel = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return this.cancel;
      }
    };
    Application.getController().start(t);
  }

}
