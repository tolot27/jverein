package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstyp;
import de.jost_net.JVerein.gui.control.FreieFormulareControl;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatJJJJMMTT;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class FreiesFormularAusgabe
{
  FreieFormulareControl control;

  File file = null;

  FormularAufbereitung formularaufbereitung = null;

  ZipOutputStream zos = null;

  public FreiesFormularAusgabe(FreieFormulareControl control)
      throws IOException, ApplicationException
  {
    this.control = control;
    Formular formular = (Formular) control
        .getFormular(FormularArt.FREIESFORMULAR).getValue();
    if(formular == null)
    {
      GUI.getStatusBar().setErrorText("Kein Formular ausgew�hlt.");
      return;
    }

    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        file = getDateiAuswahl("pdf", formular.getBezeichnung());
        formularaufbereitung = new FormularAufbereitung(file, false, false);
        break;
      case MAIL:
        file = getDateiAuswahl("zip", formular.getBezeichnung());
        zos = new ZipOutputStream(new FileOutputStream(file));
        break;
    }
    Adresstyp adresstype = (Adresstyp) control.getSuchAdresstyp(Mitgliedstyp.ALLE)
        .getValue();
    int type = -1;
    if (adresstype != null)
      type = Integer.parseInt(adresstype.getID());
    ArrayList<Mitglied> mitglieder = new MitgliedQuery(control).get(type, null);

    if (mitglieder.size() == 0)
    {
      GUI.getStatusBar().setErrorText("Keine passenden Mitglieder gefunden.");
      file.delete();
      return;
    }
    aufbereitung(formular, mitglieder);
  }

  public void aufbereitung(Formular formular, ArrayList<Mitglied> mitglieder)
      throws IOException, ApplicationException
  {
    for (Mitglied m : mitglieder)
    {
      switch ((Ausgabeart) control.getAusgabeart().getValue())
      {
        case DRUCK:
          aufbereitenFormular(m, formularaufbereitung, formular);
          break;
        case MAIL:
          if (m.getEmail() == null || m.getEmail().isEmpty())
          {
            continue;
          }
          File f = File.createTempFile(getDateiname(m), ".pdf");
          formularaufbereitung = new FormularAufbereitung(f, false, false);
          aufbereitenFormular(m, formularaufbereitung, formular);
          formularaufbereitung.closeFormular();
          zos.putNextEntry(new ZipEntry(getDateiname(m) + ".pdf"));
          FileInputStream in = new FileInputStream(f);
          // buffer size
          byte[] b = new byte[1024];
          int count;
          while ((count = in.read(b)) > 0)
          {
            zos.write(b, 0, count);
          }
          in.close();
          break;
      }
    }
    switch ((Ausgabeart) control.getAusgabeart().getValue())
    {
      case DRUCK:
        formularaufbereitung.showFormular();
        break;
      case MAIL:
        zos.close();
        new ZipMailer(file, (String) control.getBetreff().getValue(),
            (String) control.getTxt().getValue(),
            formular.getBezeichnung() + ".pdf");
        break;
    }

  }

  File getDateiAuswahl(String extension, String name) throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei w�hlen.");
    String path = control.getSettings().getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname(name, "",
        Einstellungen.getEinstellung().getDateinamenmuster(), extension).get());
    fd.setFilterExtensions(new String[] { "*." + extension });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return null;
    }
    if (!s.toLowerCase().endsWith("." + extension))
    {
      s = s + "." + extension;
    }
    final File file = new File(s);
    control.getSettings().setAttribute("lastdir", file.getParent());
    return file;
  }

  void aufbereitenFormular(Mitglied m, FormularAufbereitung fa, Formular fo)
      throws RemoteException, ApplicationException
  {
    Map<String, Object> map = new MitgliedMap().getMap(m, null);
    map = new AllgemeineMap().getMap(map);
    fa.writeForm(fo, map);
    fo.store();
  }

  String getDateiname(Mitglied m) throws RemoteException
  {
    String filename = m.getID() + "#"
        + new JVDateFormatJJJJMMTT().format(new Date()) + "#";
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename;
  }

}
