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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragAbrechnungslauf;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * L�schen eines Abrechnungslaufes
 */
public class AbrechnungslaufDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Abrechnungslauf))
    {
      throw new ApplicationException("Keinen Abrechnungslauf ausgew�hlt");
    }
    try
    {
      Abrechnungslauf abrl = (Abrechnungslauf) context;
      if (abrl.isNewObject())
      {
        return;
      }

      // Pr�fe, ob der Abrechnungslauf als abgeschlossen gekennzeichnet ist.
      // In diesem Fall darf er nicht gel�scht werden!
      if (abrl.getAbgeschlossen())
      {
        throw new ApplicationException(
            "Abgeschlossene Abrechnungsl�ufe k�nnen nicht gel�scht werden!");
      }

      // Pr�fe, ob einer der erzeugten Buchungen bereits abgeschlossen ist
      final DBService service = Einstellungen.getDBService();
      String sql1 = "SELECT jahresabschluss.bis from jahresabschluss "
          + "order by jahresabschluss.bis desc";      
      Date bis = (Date) service.execute(sql1,
          new Object[] { }, new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs)
            throws RemoteException, SQLException
        {
          if (rs.next())
          {
            return rs.getDate(1);
          }
          return null;
        }
      });
      if (bis != null)
        // Es gibt Jahresabschl�sse und bis ist das letzte Datum
      {
        // Check ob eine Buchung des Abrechnungslaufen vor dem bis Datum liegt
        String sql2 = "SELECT DISTINCT buchung.id from buchung "
            + "WHERE (abrechnungslauf = ? and datum <= ?) ";
        boolean abgeschlossen = (boolean) service.execute(sql2,
            new Object[] { abrl.getID(), bis }, new ResultSetExtractor()
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
        if (abgeschlossen)
        {
          throw new ApplicationException(
              "Der Abrechnungslauf enth�lt abgeschlossene Buchungen und kann darum nicht gel�scht werden!");
        }
      }
      
      // Check ob einer der Buchungen des Abrechnungslaufs
      // eine Spendenbescheinigung zugeordnet ist
      String sql3 = "SELECT DISTINCT buchung.id from buchung "
          + "WHERE (abrechnungslauf = ? and spendenbescheinigung IS NOT NULL) ";
      boolean spendenbescheinigung = (boolean) service.execute(sql3,
          new Object[] { abrl.getID() }, new ResultSetExtractor()
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

      // Check ob einer der Buchungen des Abrechnungslaufs
      // einer Rechnung zugeordnet ist
      String sql4 = "SELECT DISTINCT mitgliedskonto.id from mitgliedskonto "
          + "WHERE (abrechnungslauf = ? and rechnung IS NOT NULL) ";
      boolean rechnung = (boolean) service.execute(sql4,
          new Object[] { abrl.getID() }, new ResultSetExtractor()
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
      
      String text = "";
      if (!spendenbescheinigung && !rechnung)
      {
        text = "Wollen Sie diesen Abrechnungslauf wirklich l�schen?";
      }
      else if(!spendenbescheinigung && rechnung)
      {
        text = "Der Abrechnungslauf enth�lt Sollbuchungen denen eine "
            + "Rechnung zugeordnet ist.\n"
            + "Sie k�nnen nur zusammen gel�scht werden.\n"
            + "Abrechnungslauf und Rechnungen l�schen?";
      }
      else if(spendenbescheinigung && !rechnung)
      {
        text = "Der Abrechnungslauf enth�lt Buchungen denen eine "
            + "Spendenbescheinigung zugeordnet ist.\n"
            + "Sie k�nnen nur zusammen gel�scht werden.\n"
            + "Abrechnungslauf und Spendenbescheinigungen l�schen?";
      }
      else if(spendenbescheinigung && rechnung)
      {
        text = "Der Abrechnungslauf enth�lt Buchungen denen eine "
            + "Spendenbescheinigung zugeordnet ist\n"
            + "und Sollbuchungen denen eine Rechnung zugeordnet ist.\n"
            + "Sie k�nnen nur zusammen gel�scht werden.\n"
            + "Abrechnungslauf, Spendenbescheinigungen und Rechnungen l�schen?";
      }

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(String.format("Abrechnungslauf %s l�schen", abrl.getID()));
      d.setText(text);

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim L�schen eines Abrechnungslaufes", e);
        return;
      }
      
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("abrechnungslauf = ?", new Object[] { abrl.getID() });
      while (it.hasNext())
      {
        Buchung bu = it.next();
        if (bu.getSpendenbescheinigung() != null)
          bu.getSpendenbescheinigung().delete();
        try {
          bu.delete();
        }
        catch (RemoteException ignore)
        {
          // Ignorieren, da die Exception auftritt, wenn die Buchung bereits
          // gel�scht wurde, z. B. bei Splitbuchungen.
        }
      }
      DBIterator<Mitgliedskonto> mitgliedskontoIt = Einstellungen.getDBService()
          .createList(Mitgliedskonto.class);
      mitgliedskontoIt.addFilter("abrechnungslauf = ?", new Object[] { abrl.getID() });
      while (mitgliedskontoIt.hasNext())
      {
        Mitgliedskonto mk = mitgliedskontoIt.next();
        if (mk.getRechnung() != null)
          mk.getRechnung().delete();
        mk.delete();
      }
      it = Einstellungen.getDBService()
          .createList(ZusatzbetragAbrechnungslauf.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      while (it.hasNext())
      {
        ZusatzbetragAbrechnungslauf za = (ZusatzbetragAbrechnungslauf) it
            .next();
        Zusatzbetrag z = (Zusatzbetrag) Einstellungen.getDBService()
            .createObject(Zusatzbetrag.class, za.getZusatzbetrag().getID());
        try
        {
          z.vorherigeFaelligkeit();
        }
        catch (RemoteException e)
        {
          //Ignorieren, da die Exeption auftritt wenn das F�lligkeitsdatum
          //nicht weiter zur�ckgesetzt werden kann
        }
        z.setAusfuehrung(za.getLetzteAusfuehrung());
        z.store();
      }
      it = Einstellungen.getDBService().createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      it.addFilter("kursteilnehmer IS NOT NULL");
      while (it.hasNext())
      {
        Lastschrift la = (Lastschrift) it.next();
        Kursteilnehmer kt = la.getKursteilnehmer();
        if (kt != null)
        {
          kt.resetAbbudatum();
          kt.store();
        }
      }
      abrl.delete();
      GUI.getStatusBar().setSuccessText("Abrechnungslauf gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen eines Abrechnungslaufes";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
