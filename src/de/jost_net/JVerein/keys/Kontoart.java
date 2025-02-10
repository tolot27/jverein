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
package de.jost_net.JVerein.keys;

public enum Kontoart
{
  // LIMIT ist keine Kontoart sondern dient zur Abgrenzung.
  // Ids unter dem Limit werden regul�r im Buchungsklassensaldo und Kontensaldo
  // ber�cksichtigt.
  // Ids �ber dem Limit werden in beiden Salden ignoriert.
  GELD(1, "Geldkonto"),
  ANLAGE(2, "Anlagenkonto"),
  SCHULDEN(3, "Fremdkapital"),
  LIMIT(100, "-- Limit --"),
  RUECKLAGE_ZWECK_GEBUNDEN(101, "Zweckgebundene R�cklage nach � 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_BETRIEBSMITTEL(102, "Betriebsmittelr�cklage nach � 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INVESTITION(103, "Investitionsr�cklage nach � 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INSTANDHALTUNG(104, "Instandhaltungsr�cklage nach � 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_WIEDERBESCHAFFUNG(105, "Wiederbeschaffungsr�cklage nach � 62 Abs. 1 Nr. 2 AO"),
  RUECKLAGE_FREI(106, "Freie R�cklage nach � 62 Abs. 1 Nr. 3 AO"),
  RUECKLAGE_ERWERB(107, "R�cklage f�r Gesellschaftsrechte nach � 62 Abs. 1 Nr. 4 AO"),
  VERMOEGEN(108, "Verm�gen nach � 62 Abs. 3 und 4 AO"),
  RUECKLAGE_SONSTIG(109, "Sonstige R�cklage");

  private final String text;

  private final int key;
  
  Kontoart(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public static Kontoart getByKey(int key)
  {
    for (Kontoart art : Kontoart.values())
    {
      if (art.getKey() == key)
      {
        return art;
      }
    }
    return null;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
