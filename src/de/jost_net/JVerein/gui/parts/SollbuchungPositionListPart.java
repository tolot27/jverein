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
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;

public class SollbuchungPositionListPart extends TablePart
{
  public SollbuchungPositionListPart(Action action)
  {
    super(action);
  }

  public SollbuchungPositionListPart(List<SollbuchungPosition> list,
      Action action) throws RemoteException
  {
    super(list, action);

    addColumn("Datum", "datum", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    addColumn("Zweck", "zweck");
    addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if (Einstellungen.getEinstellung().getOptiert())
    {
      addColumn("Nettobetrag", "nettobetrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      addColumn("Steuersatz", "steuersatz");
      addColumn("Steuerbetrag", "steuerbetrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    }
    addColumn("Buchungsart", "buchungsart", new BuchungsartFormatter());
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }

    setRememberColWidths(true);
    setRememberOrder(true);
    addFeature(new FeatureSummary());
  }
}
