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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.MitgliedskontoFormatter;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

public class BuchungListPart extends BuchungListTablePart
{
  public BuchungListPart(Action action)
  {
    super(action);
  }

  public BuchungListPart(List<Buchung> list, Action action, ContextMenu menu)
  {
    super(list, action);

    addColumn("Nr", "id-int");
    addColumn("Konto", "konto", new Formatter()
    {

      @Override
      public String format(Object o)
      {
        Konto k = (Konto) o;
        if (k != null)
        {
          try
          {
            return k.getBezeichnung();
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
        }
        return "";
      }
    });
    addColumn("Datum", "datum", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    addColumn("Auszug", "auszugsnummer");
    addColumn("Blatt", "blattnummer");
    addColumn("Name", "name");
    addColumn("Verwendungszweck", "zweck", new Formatter()
    {

      @Override
      public String format(Object value)
      {
        if (value == null)
        {
          return null;
        }
        String s = value.toString();
        s = s.replaceAll("\r\n", " ");
        s = s.replaceAll("\r", " ");
        s = s.replaceAll("\n", " ");
        return s;
      }
    });
    addColumn("Buchungsart", "buchungsart", new BuchungsartFormatter());
    addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    addColumn("Mitglied", "mitgliedskonto", new MitgliedskontoFormatter());
    addColumn("Ersatz f�r Aufwendungen", "verzicht", new JaNeinFormatter());
    setContextMenu(menu);
    setRememberColWidths(true);
    setRememberOrder(true);
    setRememberState(true);
  }
}
