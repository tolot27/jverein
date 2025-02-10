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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl.DIFFERENZ;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.input.PersonenartInput;
import de.jost_net.JVerein.gui.menu.RechnungMenu;
import de.jost_net.JVerein.gui.parts.SollbuchungPositionListPart;
import de.jost_net.JVerein.gui.view.MahnungMailView;
import de.jost_net.JVerein.gui.view.RechnungMailView;
import de.jost_net.JVerein.gui.view.RechnungView;
import de.jost_net.JVerein.io.Rechnungsausgabe;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungControl extends DruckMailControl
{

  private TablePart rechnungList;

  private Rechnung rechnung;

  private DateInput rechnungsDatum;

  private TextInput mitglied;

  private DecimalInput betrag;

  private TablePart buchungList;

  private TextInput anrede;

  private TextInput titel;

  private TextInput name;

  private TextInput vorname;

  private TextInput strasse;

  private TextInput adressierungszusatz;

  private TextInput ort;

  private TextInput plz;

  private TextInput staat;

  private GeschlechtInput geschlecht;

  private FormularInput rechnungFormular;

  private TextInput nummer;

  private IBANInput iban;

  private BICInput bic;

  private TextInput mandatid;

  private DateInput mandatdatum;

  private TextInput personenart;

  private TextInput zahlungsweg;

  private TextInput leitwegID;

  public enum TYP
  {
    RECHNUNG, MAHNUNG
  }

  public RechnungControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  @SuppressWarnings("unchecked")
  public Part getRechnungList() throws RemoteException
  {
    if (rechnungList != null)
    {
      return rechnungList;
    }
    GenericIterator<Rechnung> rechnungen = getRechnungIterator();
    rechnungList = new TablePart(rechnungen,
        new EditAction(RechnungView.class));
    rechnungList.addColumn("Nr", "id-int");
    rechnungList.addColumn("Rechnungsdatum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    rechnungList.addColumn("Mitglied", "mitglied");
    rechnungList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Ist", "ist",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Differenz", "differenz",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Zahlungsweg", "zahlungsweg", new ZahlungswegFormatter());
    // Dummy Spalte, damit Zahlungsweg nicht am rechten Rand klebt
    rechnungList.addColumn(" ", " ",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
        Column.ALIGN_LEFT);

    rechnungList.setRememberColWidths(true);
    rechnungList.setContextMenu(new RechnungMenu());
    rechnungList.setRememberOrder(true);
    rechnungList.addFeature(new FeatureSummary());
    rechnungList.setMulti(true);
    return rechnungList;
  }

  public Button getStartRechnungButton(final Object currentObject)
  {
    final RechnungControl control = this;
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          new Rechnungsausgabe(control, RechnungControl.TYP.RECHNUNG);
        }
        catch (Exception e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  public Button getStartMahnungButton(final Object currentObject)
  {
    final RechnungControl control = this;
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          new Rechnungsausgabe(control, RechnungControl.TYP.MAHNUNG);
        }
        catch (Exception e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  @SuppressWarnings("unchecked")
  public void TabRefresh()
  {
    try
    {
      if (rechnungList != null)
      {
        rechnungList.removeAll();
        GenericIterator<Rechnung> rechnungen = getRechnungIterator();
        while (rechnungen.hasNext())
        {
          rechnungList.addItem(rechnungen.next());
        }
        rechnungList.sort();
      }
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  @SuppressWarnings("rawtypes")
  public GenericIterator getRechnungIterator() throws RemoteException
  {
    DBIterator<Rechnung> rechnungenIt = Einstellungen.getDBService()
        .createList(Rechnung.class);

    if (datumvon != null && datumvon.getValue() != null)
    {
      rechnungenIt.addFilter("rechnung.datum >= ? ",
          new Object[] { datumvon.getValue() });
    }
    if (datumbis != null && datumbis.getValue() != null)
    {
      rechnungenIt.addFilter("rechnung.datum <= ? ",
          new Object[] { datumbis.getValue() });
    }

    // Wenn Filtern nach Name oder Mail JOIN mitglied
    if ((suchname != null && suchname.getValue() != null
        && !((String) suchname.getValue()).isEmpty())
        || (mailAuswahl != null
            && (Integer) mailAuswahl.getValue() != MailAuswertungInput.ALLE))
    {
      rechnungenIt.join("mitglied");
      rechnungenIt.addFilter("mitglied.id = rechnung.mitglied");
    }

    if (suchname != null && suchname.getValue() != null
        && !((String) suchname.getValue()).isEmpty())
    {
      rechnungenIt.addFilter(
          "((lower(mitglied.name) like ?) OR (lower(mitglied.vorname) like ?))",
          new Object[] { "%" + ((String) suchname.getValue()).toLowerCase() + "%",
              "%" + ((String) suchname.getValue()).toLowerCase() + "%" });
    }

    if (mailAuswahl != null
        && (Integer) mailAuswahl.getValue() != MailAuswertungInput.ALLE)
    {
      if ((Integer) mailAuswahl.getValue() == MailAuswertungInput.OHNE)
        rechnungenIt.addFilter(
            "(mitglied.email is null or length(mitglied.email) = 0)");
      else
        rechnungenIt.addFilter(
            "(mitglied.email is  not null and length(mitglied.email) > 0)");
    }

    if (ohneabbucher != null && (Boolean) ohneabbucher.getValue())
    {
      rechnungenIt.addFilter("rechnung.zahlungsweg <> ?",
          Zahlungsweg.BASISLASTSCHRIFT);
    }

    if (isDifferenzAktiv() && getDifferenz().getValue() != DIFFERENZ.EGAL)
    {
      String sql = "SELECT DISTINCT mitgliedskonto.rechnung, mitgliedskonto.betrag, "
          + "sum(buchung.betrag) FROM mitgliedskonto "
          + "LEFT JOIN buchung on mitgliedskonto.id = buchung.mitgliedskonto "
          + "WHERE mitgliedskonto.rechnung is not null "
          + "group by mitgliedskonto.id ";
      if (getDifferenz().getValue() == DIFFERENZ.FEHLBETRAG)
      {
        sql += "having sum(buchung.betrag) < mitgliedskonto.betrag or "
            + "(sum(buchung.betrag) is null and mitgliedskonto.betrag > 0) ";
      }
      else
      {
        sql += "having sum(buchung.betrag) > mitgliedskonto.betrag or "
            + "(sum(buchung.betrag) is null and mitgliedskonto.betrag < 0) ";
      }

      @SuppressWarnings("unchecked")
      ArrayList<String> diffIds = (ArrayList<String>) Einstellungen
          .getDBService().execute(sql, null, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              ArrayList<String> list = new ArrayList<>();
              while (rs.next())
              {
                list.add(rs.getString(1));
              }
              return list;
            }
          });
      if (diffIds.size() == 0)
        return PseudoIterator.fromArray(new GenericObject[] {});
      rechnungenIt
          .addFilter("rechnung.id in (" + String.join(",", diffIds) + ")");
    }

    return rechnungenIt;
  }

  @Override
  public String getInfoText(Object selection)
  {
    Rechnung[] rechnungen = null;
    String text = "";

    if (selection instanceof Rechnung)
    {
      rechnungen = new Rechnung[] { (Rechnung) selection };
    }
    else if (selection instanceof Rechnung[])
    {
      rechnungen = (Rechnung[]) selection;
    }
    else
    {
      return "";
    }

    try
    {
      if (rechnungen != null)
      {
        text = "Es wurden " + rechnungen.length + " Rechnungen ausgew�hlt"
            + "\nFolgende Mitglieder haben keine Mailadresse:";
        for (Rechnung re : rechnungen)
        {
          Mitglied m = re.getMitglied();
          if (m != null && (m.getEmail() == null || m.getEmail().isEmpty()))
          {
            text = text + "\n - " + m.getName() + ", " + m.getVorname();
          }
        }
      }
    }
    catch (Exception ex)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Ermitteln der Info");
    }
    return text;
  }

  private Rechnung getRechnung()
  {
    if (rechnung != null)
    {
      return rechnung;
    }
    rechnung = (Rechnung) getCurrentObject();
    return rechnung;
  }

  public DateInput getRechnungsdatum() throws RemoteException
  {
    if (rechnungsDatum != null)
    {
      return rechnungsDatum;
    }

    Date d = getRechnung().getDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    rechnungsDatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    rechnungsDatum.setName("Rechnungsdatum");
    rechnungsDatum.disable();
    return rechnungsDatum;
  }

  public TextInput getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    mitglied = new TextInput(getRechnung().getMitglied().getName() + ", "
        + getRechnung().getMitglied().getVorname());
    mitglied.setName("Mitglied");
    mitglied.disable();
    return mitglied;
  }

  public FormularInput getRechnungFormular() throws RemoteException
  {
    if (rechnungFormular != null)
    {
      return rechnungFormular;
    }

    rechnungFormular = new FormularInput(FormularArt.RECHNUNG,
        getRechnung().getFormular().getID());
    rechnungFormular.setName("Formular");
    rechnungFormular.disable();
    return rechnungFormular;
  }

  public TextInput getNummer() throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }

    nummer = new TextInput(StringTool.lpad(getRechnung().getID(),
        Einstellungen.getEinstellung().getZaehlerLaenge(), "0"));
    nummer.setName("Rechnungsnummer");
    nummer.disable();
    ;
    return nummer;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }

    betrag = new DecimalInput(getRechnung().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setName("Betrag");
    betrag.disable();
    return betrag;
  }

  public TextInput getAnrede() throws RemoteException
  {
    if (anrede != null)
    {
      return anrede;
    }

    anrede = new TextInput(getRechnung().getAnrede());
    anrede.setName("Anrede");
    anrede.disable();
    return anrede;
  }

  public TextInput getTitel() throws RemoteException
  {
    if (titel != null)
    {
      return titel;
    }

    titel = new TextInput(getRechnung().getTitel());
    titel.setName("Titel");
    titel.disable();
    return titel;
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }

    name = new TextInput(getRechnung().getName());
    name.setName("Name");
    name.disable();
    return name;
  }

  public TextInput getVorname() throws RemoteException
  {
    if (vorname != null)
    {
      return vorname;
    }

    vorname = new TextInput(getRechnung().getVorname());
    vorname.setName("Vorname");
    vorname.disable();
    return vorname;
  }

  public TextInput getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }

    strasse = new TextInput(getRechnung().getStrasse());
    strasse.setName("Strasse");
    strasse.disable();
    return strasse;
  }

  public TextInput getAdressierungszusatz() throws RemoteException
  {
    if (adressierungszusatz != null)
    {
      return adressierungszusatz;
    }

    adressierungszusatz = new TextInput(getRechnung().getAdressierungszusatz());
    adressierungszusatz.setName("Adressierungszusatz");
    adressierungszusatz.disable();
    return adressierungszusatz;
  }

  public TextInput getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }

    ort = new TextInput(getRechnung().getOrt());
    ort.setName("Ort");
    ort.disable();
    return ort;
  }

  public TextInput getPlz() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }

    plz = new TextInput(getRechnung().getPlz());
    plz.setName("Plz");
    plz.disable();
    return plz;
  }

  public TextInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }

    staat = new TextInput(getRechnung().getStaat());
    staat.setName("Staat");
    staat.disable();
    return staat;
  }

  public GeschlechtInput getGeschlecht() throws RemoteException
  {
    if (geschlecht != null)
    {
      return geschlecht;
    }

    geschlecht = new GeschlechtInput(getRechnung().getGeschlecht());
    geschlecht.setName("Geschlecht");
    geschlecht.disable();
    return geschlecht;
  }

  public TextInput getPersonenart() throws RemoteException
  {
    if (personenart != null)
    {
      return personenart;
    }

    personenart = new TextInput(
        getRechnung().getPersonenart().equalsIgnoreCase("n")
            ? PersonenartInput.NATUERLICHE_PERSON
            : PersonenartInput.JURISTISCHE_PERSON);
    personenart.setName("Personenart");
    personenart.disable();
    return personenart;
  }

  public DateInput getMandatdatum() throws RemoteException
  {
    if (mandatdatum != null)
    {
      return mandatdatum;
    }

    Date d = getRechnung().getMandatDatum();

    mandatdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    mandatdatum.setName("Mandatdatum");
    mandatdatum.disable();
    return mandatdatum;
  }

  public TextInput getMandatid() throws RemoteException
  {
    if (mandatid != null)
    {
      return mandatid;
    }

    mandatid = new TextInput(getRechnung().getMandatID());
    mandatid.setName("Mandatid");
    mandatid.disable();
    return mandatid;
  }

  public BICInput getBic() throws RemoteException
  {
    if (bic != null)
    {
      return bic;
    }

    bic = new BICInput(getRechnung().getBIC());
    bic.setName("BIC");
    bic.disable();
    return bic;
  }

  public IBANInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }

    iban = new IBANInput(HBCIProperties.formatIban(getRechnung().getIBAN()),
        getBic());
    iban.setName("IBAN");
    iban.disable();
    return iban;
  }

  public TextInput getLeitwegID() throws RemoteException
  {
    if (leitwegID != null)
    {
      return leitwegID;
    }
    leitwegID = new TextInput(getRechnung().getLeitwegID());
    leitwegID.setName("LeitwegID");
    leitwegID.disable();
    return leitwegID;
  }

  public Part getSollbuchungPositionListPart() throws RemoteException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    buchungList = new SollbuchungPositionListPart(
        getRechnung().getSollbuchungPositionList(), null);
    return buchungList;
  }
  
  public TextInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }

    zahlungsweg = new TextInput(getRechnung().getZahlungsweg().getText());
    zahlungsweg.setName("Zahlungsweg");
    zahlungsweg.disable();
    return zahlungsweg;
  }

  public Button getRechnungDruckUndMailButton()
  {

    Button b = new Button("Druck und Mail", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Rechnung re = getRechnung();
        GUI.startView(RechnungMailView.class, new Rechnung[] { (Rechnung) re });
      }
    }, getRechnung(), false, "document-print.png");
    return b;
  }

  public Button getMahnungDruckUndMailButton()
  {

    Button b = new Button("Mahnung Druck und Mail", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Rechnung re = getRechnung();
        GUI.startView(MahnungMailView.class, new Rechnung[] { (Rechnung) re });
      }
    }, getRechnung(), false, "document-print.png");
    return b;
  }
}
