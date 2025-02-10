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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.NotizFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.menu.BeitragsgruppeMenu;
import de.jost_net.JVerein.gui.view.BeitragsgruppeDetailView;
import de.jost_net.JVerein.io.AltersgruppenParser;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.VonBis;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeitragsgruppeControl extends AbstractControl
{
  private Input[] alterstaffel;
  
  private CheckboxInput isAltersstaffel;

  private TablePart beitragsgruppeList;

  private Input bezeichnung;

  private CheckboxInput sekundaer;

  private DecimalInput betrag;

  private DecimalInput betragmonatlich;

  private DecimalInput betragvierteljaehrlich;

  private DecimalInput betraghalbjaehrlich;

  private DecimalInput betragjaehrlich;

  private SelectInput beitragsart;
  
  private SelectInput buchungsklasse;

  private Beitragsgruppe beitrag;

  private DecimalInput arbeitseinsatzstunden;

  private DecimalInput arbeitseinsatzbetrag;

  private AbstractInput buchungsart;
  
  

  private TextAreaInput notiz;

  public BeitragsgruppeControl(AbstractView view)
  {
    super(view);
  }

  private Beitragsgruppe getBeitragsgruppe()
  {
    if (beitrag != null)
    {
      return beitrag;
    }
    beitrag = (Beitragsgruppe) getCurrentObject();
    return beitrag;
  }

  public Input getBezeichnung(boolean withFocus) throws RemoteException
  {
    if (bezeichnung != null)
      return bezeichnung;
    bezeichnung = new TextInput(getBeitragsgruppe().getBezeichnung(), 30);
    bezeichnung.setMandatory(true);
    if (withFocus)
    {
      bezeichnung.focus();
    }
    return bezeichnung;
  }

  public CheckboxInput getSekundaer() throws RemoteException
  {
    if (sekundaer != null)
    {
      return sekundaer;
    }
    sekundaer = new CheckboxInput(getBeitragsgruppe().getSekundaer());
    return sekundaer;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getBeitragsgruppe().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    if(getBeitragsgruppe().getID() != null && getBeitragsgruppe().getHasAltersstaffel())
      betrag.disable();
    return betrag;
  }

  public DecimalInput getBetragMonatlich() throws RemoteException
  {
    if (betragmonatlich != null)
    {
      return betragmonatlich;
    }
    betragmonatlich = new DecimalInput(getBeitragsgruppe().getBetragMonatlich(),
        Einstellungen.DECIMALFORMAT);
    if(getBeitragsgruppe().getHasAltersstaffel())
      betragmonatlich.disable();
    return betragmonatlich;
  }

  public DecimalInput getBetragVierteljaehrlich() throws RemoteException
  {
    if (betragvierteljaehrlich != null)
    {
      return betragvierteljaehrlich;
    }
    betragvierteljaehrlich = new DecimalInput(
        getBeitragsgruppe().getBetragVierteljaehrlich(),
        Einstellungen.DECIMALFORMAT);
    if(getBeitragsgruppe().getHasAltersstaffel())
      betragvierteljaehrlich.disable();
    return betragvierteljaehrlich;
  }

  public DecimalInput getBetragHalbjaehrlich() throws RemoteException
  {
    if (betraghalbjaehrlich != null)
    {
      return betraghalbjaehrlich;
    }
    betraghalbjaehrlich = new DecimalInput(
        getBeitragsgruppe().getBetragHalbjaehrlich(),
        Einstellungen.DECIMALFORMAT);
    if(getBeitragsgruppe().getHasAltersstaffel())
      betraghalbjaehrlich.disable();
    return betraghalbjaehrlich;
  }

  public DecimalInput getBetragJaehrlich() throws RemoteException
  {
    if (betragjaehrlich != null)
    {
      return betragjaehrlich;
    }
    betragjaehrlich = new DecimalInput(getBeitragsgruppe().getBetragJaehrlich(),
        Einstellungen.DECIMALFORMAT);
    if(getBeitragsgruppe().getHasAltersstaffel())
        betragjaehrlich.disable();
    return betragjaehrlich;
  }


  public CheckboxInput getIsAltersstaffel() throws RemoteException
  {
    if (isAltersstaffel != null)
    {
      return isAltersstaffel;
    }
    isAltersstaffel = new CheckboxInput(getBeitragsgruppe().getHasAltersstaffel());
    isAltersstaffel.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        if (event != null && event.type == SWT.Selection)
        {
          Boolean b = (Boolean) isAltersstaffel.getValue();
          
          if(betrag != null)
            betrag.setEnabled(!b);
          if(betragjaehrlich != null)
            betragjaehrlich.setEnabled(!b);
          if(betraghalbjaehrlich != null)
            betraghalbjaehrlich.setEnabled(!b);
          if(betragvierteljaehrlich != null)
            betragvierteljaehrlich.setEnabled(!b);
          if(betragmonatlich != null)
            betragmonatlich.setEnabled(!b);
          
          if (alterstaffel != null)
          {
            for (Input i : alterstaffel)
            {
              i.setEnabled(b);
            }
          }
        }
      }
    });
    return isAltersstaffel;
  }
  
  public Input[] getAltersstaffel() throws RemoteException
  {
    if (alterstaffel != null)
    {
      return alterstaffel;
    }
    String stufen = Einstellungen.getEinstellung().getBeitragAltersstufen();
    if(stufen == null || stufen == "")
      return null;
    AltersgruppenParser ap = new AltersgruppenParser(stufen);
 
    int i = 0;
    List<Input> list = new ArrayList<Input>();
    while (ap.hasNext())
    {
      VonBis vb = ap.getNext();
      double betrag = 0;
      Altersstaffel a = beitrag.getAltersstaffel(i);
      if(a != null)
        betrag = a.getBetrag();
      DecimalInput d = new DecimalInput(betrag,
          Einstellungen.DECIMALFORMAT);
      if(!getBeitragsgruppe().getHasAltersstaffel())
        d.disable();
      d.setData("nummer", Integer.valueOf(i));
      d.setName(vb.getVon() + "-" + vb.getBis() + " Jahre");
      list.add(d);
      i++;
    }
    alterstaffel = list.toArray(new Input[0]);
    return alterstaffel;
  }
  
  public SelectInput getBeitragsArt() throws RemoteException
  {
    if (beitragsart != null)
    {
      return beitragsart;
    }
    beitragsart = new SelectInput(ArtBeitragsart.values(),
        getBeitragsgruppe().getBeitragsArt());
    return beitragsart;
  }

  public DecimalInput getArbeitseinsatzStunden() throws RemoteException
  {
    if (arbeitseinsatzstunden != null)
    {
      return arbeitseinsatzstunden;
    }
    arbeitseinsatzstunden = new DecimalInput(
        getBeitragsgruppe().getArbeitseinsatzStunden(),
        new DecimalFormat("###,###.##"));
    return arbeitseinsatzstunden;
  }

  public DecimalInput getArbeitseinsatzBetrag() throws RemoteException
  {
    if (arbeitseinsatzbetrag != null)
    {
      return arbeitseinsatzbetrag;
    }
    arbeitseinsatzbetrag = new DecimalInput(
        getBeitragsgruppe().getArbeitseinsatzBetrag(),
        new DecimalFormat("###,###.##"));
    return arbeitseinsatzbetrag;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        getBeitragsgruppe().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        Einstellungen.getEinstellung().getBuchungBuchungsartAuswahl());
    buchungsart.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Buchungsart bua = (Buchungsart) buchungsart.getValue();
          if (buchungsklasse != null && buchungsklasse.getValue() == null &&
              bua != null)
            buchungsklasse.setValue(bua.getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    return buchungsart;
  }
  
  public SelectInput getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    buchungsklasse = new BuchungsklasseInput().getBuchungsklasseInput(buchungsklasse,
        getBeitragsgruppe().getBuchungsklasse());
    return buchungsklasse;
  }
  
  private Long getSelectedBuchungsKlasseId() throws ApplicationException
  {
    try
    {
      if (null == buchungsklasse)
        return null;
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse().getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gew�hlte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public TextAreaInput getNotiz() throws RemoteException
  {
    if (notiz != null)
    {
      return notiz;
    }
    notiz = new TextAreaInput(getBeitragsgruppe().getNotiz(), 200);
    notiz.setName("Notiz");
    notiz.setHeight(50);
    return notiz;
  }

  public void handleStore()
  {
    try
    {
      Beitragsgruppe b = getBeitragsgruppe();
      b.setBezeichnung((String) getBezeichnung(false).getValue());
      if (Einstellungen.getEinstellung().getSekundaereBeitragsgruppen())
      {
        b.setSekundaer((Boolean) sekundaer.getValue());
      }
      else
      {
        b.setSekundaer(false);
      }
      ArtBeitragsart ba = (ArtBeitragsart) getBeitragsArt().getValue();

      b.setBeitragsArt(ba.getKey());
      b.setBuchungsart((Buchungsart) getBuchungsart().getValue());
      b.setBuchungsklasseId(getSelectedBuchungsKlasseId());
      Double d = (Double) getArbeitseinsatzStunden().getValue();
      b.setArbeitseinsatzStunden(d.doubleValue());
      d = (Double) getArbeitseinsatzBetrag().getValue();
      b.setArbeitseinsatzBetrag(d.doubleValue());
      b.setNotiz((String) getNotiz().getValue());
      //Die Beitragsgruppe in die DB schreiben damit sie evtl f�r Altersstaffel verf�gbar ist
      b.setHasAltersstaffel(false);
      b.store();
      
      switch (Einstellungen.getEinstellung().getBeitragsmodel())
      {
        case GLEICHERTERMINFUERALLE:
        case MONATLICH12631:
          if(isAltersstaffel != null && (Boolean)isAltersstaffel.getValue() && alterstaffel != null)
          {
            for (Input i : alterstaffel)
            {
              Altersstaffel a = null;
              Double betrag = (Double)i.getValue();
              a = beitrag.getAltersstaffel((Integer)i.getData("nummer"));
              if(betrag != null && a != null) {
                a.setBetrag(betrag);
               }
              else
              {
                a = (Altersstaffel)Einstellungen.getDBService().createObject(Altersstaffel.class, null);
                a.setBeitragsgruppe(beitrag);
                a.setBetrag(d);
                a.setNummer((Integer)i.getData("nummer"));
              }
              a.store();
            }
            b.setHasAltersstaffel(true);
          }
          else
          {
            Double betrag = (Double) getBetrag().getValue();
            b.setBetrag(betrag.doubleValue());
            b.setHasAltersstaffel(false);
          }
          break;
        case FLEXIBEL:
          Double d1 = (Double) getBetragMonatlich().getValue();
          b.setBetragMonatlich(d1.doubleValue());
          Double d3 = (Double) getBetragVierteljaehrlich().getValue();
          b.setBetragVierteljaehrlich(d3.doubleValue());
          Double d6 = (Double) getBetragHalbjaehrlich().getValue();
          b.setBetragHalbjaehrlich(d6.doubleValue());
          Double d12 = (Double) getBetragJaehrlich().getValue();
          b.setBetragJaehrlich(d12.doubleValue());
          b.setHasAltersstaffel(false);
          break;
      }
      b.store();
      GUI.getStatusBar().setSuccessText("Beitragsgruppe gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Beitragsgruppe";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public TablePart getBeitragsgruppeTable() throws RemoteException
  {
    if (beitragsgruppeList != null)
    {
      return beitragsgruppeList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Beitragsgruppe> beitragsgruppen = service
        .createList(Beitragsgruppe.class);
    beitragsgruppeList = new TablePart(beitragsgruppen,
        new EditAction(BeitragsgruppeDetailView.class));
    beitragsgruppeList.addColumn("Bezeichnung", "bezeichnung");
    switch (Einstellungen.getEinstellung().getBeitragsmodel())
    {
      case GLEICHERTERMINFUERALLE:
      case MONATLICH12631:
        beitragsgruppeList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        break;
      case FLEXIBEL:
        beitragsgruppeList.addColumn("Betrag monatlich", "betragmonatlich",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag viertelj.",
            "betragvierteljaehrlich",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag halbj.", "betraghalbjaehrlich",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        beitragsgruppeList.addColumn("Betrag j�hrlich", "betragjaehrlich",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        break;
    }
    if (Einstellungen.getEinstellung().getArbeitseinsatz())
    {
      beitragsgruppeList.addColumn("Arbeitseinsatz-Stunden",
          "arbeitseinsatzstunden",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      beitragsgruppeList.addColumn("Arbeitseinsatz-Stundensatz",
          "arbeitseinsatzbetrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    }
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      beitragsgruppeList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    beitragsgruppeList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    beitragsgruppeList.addColumn("Altersstaffel", "altersstaffel",
        new JaNeinFormatter());
    beitragsgruppeList.addColumn("Sekund�r", "sekundaer",
        new JaNeinFormatter());
    beitragsgruppeList.addColumn("Beitragsart", "beitragsart",
        new Formatter() {

          @Override
          public String format(Object o)
          {
            return ArtBeitragsart.getByKey((Integer)o).getText();
          }
    });
    beitragsgruppeList.addColumn("Notiz", "notiz", new NotizFormatter(40));
    beitragsgruppeList.setContextMenu(new BeitragsgruppeMenu());
    beitragsgruppeList.setFormatter(new TableFormatter() {
      @Override
      public void format(TableItem item)
      {
        Beitragsgruppe b = (Beitragsgruppe) item.getData();
        try
        {
          if (b.getHasAltersstaffel())
          {
            AltersgruppenParser ap = new AltersgruppenParser(Einstellungen.getEinstellung().getBeitragAltersstufen());
            String text = "";
            DBIterator<Altersstaffel> it = b.getAltersstaffelIterator();
            while (it.hasNext() && ap.hasNext())
            {
              ap.getNext();
              Altersstaffel a = it.next();
              text = text + "|"
                  + Einstellungen.DECIMALFORMAT.format(a.getBetrag());
            }
            item.setText(1, text.substring(1));
          }
        }
        catch (RemoteException e)
        {
          Logger.error("unable to format line", e);
        }
      }
    });
    return beitragsgruppeList;
  }
}
