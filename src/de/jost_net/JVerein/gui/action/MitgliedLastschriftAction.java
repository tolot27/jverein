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
 * 
 * Erstellt von R�diger Wurth
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.util.ApplicationException;

public class MitgliedLastschriftAction implements Action
{
  private static String CONFIRM_TITLE = "SEPA-Check Fehler";

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Mitglied))
    {
      throw new ApplicationException("Kein Mitglied ausgew�hlt");
    }
    Mitglied m = null; // Mitglied
    Mitglied mZ = null; // Zahler
    SepaLastschrift sl = null;
    try
    {
      m = (Mitglied) context;

      // pruefe wer der Zahler ist
      if (m.getZahlungsweg() == Zahlungsweg.VOLLZAHLER && m.getZahlerID() != null)
      {
        // Mitglied ist Familienangehoeriger, hat also anderen Zahler
        mZ = (Mitglied) Einstellungen.getDBService().createObject(
            Mitglied.class, m.getZahlerID() + "");

        if (!AbrechnungSEPAControl.confirmDialog("Familienangeh�riger",
            "Dieses Mitglied ist ein Familienangeh�riger.\n\n"
                + "Als Konto wird das Konto des Zahlers belastet:\n"
                + "Zahler: " + mZ.getName() + "," + mZ.getVorname() + "\n"
                + "Kontoinhaber des Zahlers: " + mZ.getKontoinhaber(1)))
        {
          return;
        }

      }
      else
      {
        // Mitglied zahlt selbst
        mZ = m;
      }

      // pruefe Kontoinformationen
      if (checkSEPA(mZ))
      {
        sl = (SepaLastschrift) Settings.getDBService().createObject(
            SepaLastschrift.class, null);

        // Gl�ubiger-ID
        sl.setCreditorId(Einstellungen.getEinstellung().getGlaeubigerID());

        // Kontodaten: Name, BIC, IBAN
        sl.setGegenkontoName(mZ.getKontoinhaber(1));
        sl.setGegenkontoBLZ(mZ.getBic());
        sl.setGegenkontoNummer(mZ.getIban());

        // Mandat: ID, Datum, Typ
        sl.setMandateId(mZ.getMandatID());
        sl.setSignatureDate(mZ.getMandatDatum());
        sl.setSequenceType(SepaLastSequenceType.RCUR);

        // Verwendungszweck vorbelegen: "Mitgliedsnummer/Mitgliedsname"
        // Voranstellen eines Strings der zwingend ge?ndert werden muss,
        // damit der Anwender nicht vergisst den Verwendungszweck
        // korrekt einzugeben
        String verwendungszweck = "#ANPASSEN# "
            + (Einstellungen.getEinstellung().getExterneMitgliedsnummer() ? m
                .getExterneMitgliedsnummer() : m.getID()) + "/"
            + Adressaufbereitung.getNameVorname(m);
        sl.setZweck(verwendungszweck);

        GUI.startView(
            de.willuhn.jameica.hbci.gui.views.SepaLastschriftNew.class, sl);
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException("Fehler bei manueller Lastschrift", e);
    }
  }

  private boolean checkSEPA(Mitglied m) throws RemoteException
  {
    try
    {
      return m.checkSEPA();
    }
    catch (ApplicationException ae)
    {
      if (!AbrechnungSEPAControl.confirmDialog(CONFIRM_TITLE,
          ae.getLocalizedMessage() + "\nWeiter?"))
      {
        return false;
      }
    }
    return true;
  }

}
