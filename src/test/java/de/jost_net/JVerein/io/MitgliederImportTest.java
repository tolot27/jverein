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
package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.MitgliederImport.ZeilenDurchlauf;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ProgressMonitor;

class MitgliederImportTest
{
  /** Gespeicherte Mitglieder des Imports, in Speicherreihenfolge. */
  private List<Mitglied> gespeichert;

  private MockedStatic<Einstellungen> einstellungen;

  /**
   * JVereinDBService und die GUI-Klassen (TreePart) legen beim Laden statisch
   * ein Jameica-Settings an, das ohne laufende Application nicht existiert.
   */
  private MockedConstruction<Settings> settings;

  /** TreePart lädt im Konstruktor SWT-Images, was ohne GUI nicht geht. */
  private MockedConstruction<TreePart> treePart;

  private ProgressMonitor monitor;

  @BeforeEach
  void setUp() throws Exception
  {
    settings = Mockito.mockConstruction(Settings.class);
    treePart = Mockito.mockConstruction(TreePart.class);
    gespeichert = new ArrayList<>();
    monitor = mock(ProgressMonitor.class);

    Mitgliedstyp mitgliedstyp = mock(Mitgliedstyp.class);
    Mockito.doReturn(Mitgliedstyp.MITGLIED).when(mitgliedstyp).getID();

    List<Beitragsgruppe> beitragsgruppen = List.of(
        beitragsgruppe("1", "Vollzahler", ArtBeitragsart.NORMAL),
        beitragsgruppe("2", "Angehoeriger", ArtBeitragsart.FAMILIE_ANGEHOERIGER));

    // Fake-DBService: Mitglieder werden im Speicher gehalten, andere Listen
    // sind leer.
    JVereinDBService db = mock(JVereinDBService.class, invocation ->
    {
      String name = invocation.getMethod().getName();
      if (name.equals("createObject"))
      {
        Class<?> klasse = invocation.getArgument(0);
        if (klasse == Mitglied.class)
        {
          return neuesMitglied(mitgliedstyp);
        }
        return mock(klasse);
      }
      if (name.equals("createList"))
      {
        Class<?> klasse = invocation.getArgument(0);
        if (klasse == Mitglied.class)
        {
          return iterator(gespeichert, (m, spalte) -> {
            try
            {
              return m.getExterneMitgliedsnummer();
            }
            catch (Exception e)
            {
              throw new IllegalStateException(e);
            }
          });
        }
        if (klasse == Beitragsgruppe.class)
        {
          return iterator(beitragsgruppen, (bg, spalte) -> {
            try
            {
              return bg.getBezeichnung();
            }
            catch (Exception e)
            {
              throw new IllegalStateException(e);
            }
          });
        }
        return iterator(List.of(), (o, spalte) -> null);
      }
      return null;
    });

    einstellungen = Mockito.mockStatic(Einstellungen.class);
    einstellungen.when(Einstellungen::getDBService).thenReturn(db);
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, true);
    einstellung(Property.EINTRITTSDATUMPFLICHT, false);
    einstellung(Property.GEBURTSDATUMPFLICHT, false);
    einstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT, false);
    einstellung(Property.INDIVIDUELLEBEITRAEGE, false);
    einstellung(Property.ZAHLUNGSWEG, Zahlungsweg.ÜBERWEISUNG);
    einstellung(Property.ZAHLUNGSRHYTMUS, 12);
    einstellung(Property.BEITRAGSMODEL,
        Beitragsmodel.GLEICHERTERMINFUERALLE.getKey());
    einstellung(Property.SEPAMANDATIDSOURCE, SepaMandatIdSource.DBID);
  }

  @AfterEach
  void tearDown()
  {
    if (einstellungen != null)
    {
      einstellungen.close();
    }
    if (treePart != null)
    {
      treePart.close();
    }
    if (settings != null)
    {
      settings.close();
    }
  }

  private void einstellung(Property property, Object wert)
  {
    einstellungen.when(() -> Einstellungen.getEinstellung(property))
        .thenReturn(wert);
  }

  private Beitragsgruppe beitragsgruppe(String id, String bezeichnung,
      ArtBeitragsart art) throws Exception
  {
    Beitragsgruppe bg = mock(Beitragsgruppe.class);
    Mockito.doReturn(id).when(bg).getID();
    Mockito.doReturn(bezeichnung).when(bg).getBezeichnung();
    Mockito.doReturn(art).when(bg).getBeitragsArt();
    return bg;
  }

  /**
   * Mitglied-Mock, der gesetzte Werte über die passenden Getter
   * zurückliefert. store() vergibt eine ID und merkt das Mitglied.
   */
  private Mitglied neuesMitglied(Mitgliedstyp mitgliedstyp)
  {
    Map<String, Object> werte = new HashMap<>();
    Answer<Object> answer = invocation -> {
      String name = invocation.getMethod().getName();
      Class<?> typ = invocation.getMethod().getReturnType();
      if (name.equals("store"))
      {
        werte.put("ID", String.valueOf(gespeichert.size() + 1));
        gespeichert.add((Mitglied) invocation.getMock());
        return null;
      }
      if (name.equals("getMitgliedstyp"))
      {
        return mitgliedstyp;
      }
      if (name.startsWith("set") && invocation.getArguments().length == 1)
      {
        werte.put(name.substring(3), invocation.getArgument(0));
        return null;
      }
      if (name.startsWith("get") && werte.containsKey(name.substring(3)))
      {
        return werte.get(name.substring(3));
      }
      if (name.equals("getPersonenart"))
      {
        return "N";
      }
      if (typ == String.class)
      {
        return "";
      }
      if (typ == int.class)
      {
        return 0;
      }
      if (typ == boolean.class)
      {
        return false;
      }
      return null;
    };
    return mock(Mitglied.class, answer);
  }

  /**
   * Liefert einen DBIterator über die Liste. addFilter("spalte = ?", wert)
   * schränkt die Liste auf Einträge ein, deren Wert (aus wert.apply(eintrag,
   * spalte)) gleich dem Parameter ist; Filter ohne Parameter werden ignoriert.
   */
  @SuppressWarnings("unchecked")
  private <T extends DBObject> DBIterator<T> iterator(List<T> alle,
      BiFunction<T, String, Object> wert)
  {
    List<T> treffer = new ArrayList<>(alle);
    int[] position = { 0 };
    return mock(DBIterator.class, invocation -> {
      switch (invocation.getMethod().getName())
      {
        case "addFilter":
          if (invocation.getArguments().length > 1)
          {
            String spalte = ((String) invocation.getArgument(0)).split(" ")[0];
            Object parameter = invocation.getArgument(1);
            treffer.removeIf(
                e -> !Objects.equals(wert.apply(e, spalte), parameter));
          }
          return null;
        case "hasNext":
          return position[0] < treffer.size();
        case "next":
          return treffer.get(position[0]++);
        case "size":
          return treffer.size();
        default:
          return null;
      }
    });
  }

  private Mitglied gespeichert(String externeMitgliedsnummer) throws Exception
  {
    for (Mitglied m : gespeichert)
    {
      if (externeMitgliedsnummer.equals(m.getExterneMitgliedsnummer()))
      {
        return m;
      }
    }
    throw new AssertionError(
        "Mitglied nicht gespeichert: " + externeMitgliedsnummer);
  }

  private void importieren(String resource) throws Exception
  {
    File file = new File(getClass().getResource("/" + resource).toURI());
    new MitgliederImport().doImport(null, null, file, "UTF-8", monitor);
  }

  /**
   * Öffnet die CSV-Resource so wie MitgliederImport.doImport und liefert pro
   * Zeile in der Reihenfolge des ZeilenDurchlaufs "externemitgliedsnummer:Zeilennummer".
   */
  private List<String> durchlaufen(String resource)
      throws SQLException, ClassNotFoundException, URISyntaxException
  {
    File file = new File(getClass().getResource("/" + resource).toURI());
    String fil = file.getName();
    int pos = fil.lastIndexOf('.');

    Properties props = new Properties();
    props.put("separator", ";");
    props.put("suppressHeaders", "false");
    props.put("charset", "UTF-8");
    props.put("fileExtension", fil.substring(pos));

    Class.forName("org.relique.jdbc.csv.CsvDriver");
    try (Connection conn = DriverManager
        .getConnection("jdbc:relique:csv:" + file.getParent(), props);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
        ResultSet results = stmt
            .executeQuery("SELECT * FROM \"" + fil.substring(0, pos) + "\""))
    {
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results);
      List<String> reihenfolge = new ArrayList<>();
      while (durchlauf.next())
      {
        reihenfolge.add(results.getString("externemitgliedsnummer") + ":"
            + results.getRow());
      }
      return reihenfolge;
    }
  }

  @Test
  void zeilenMitExternerZahlerIdKommenZuletzt() throws Exception
  {
    // Datei: 2 (Angehöriger), 1 (Vollzahler), 3 (Angehöriger), 4 (Einzelperson)
    assertEquals(List.of("1:2", "4:4", "2:1", "3:3"),
        durchlaufen("mitglieder-import-familie.csv"));
  }

  @Test
  void ohneSpalteExternezahleridBleibtReihenfolge() throws Exception
  {
    assertEquals(List.of("2:1", "1:2", "3:3"),
        durchlaufen("mitglieder-import-ohne-externezahlerid.csv"));
  }

  @Test
  void leereZelleZaehltAlsOhneExternezahlerid() throws Exception
  {
    File file = new File(
        getClass().getResource("/mitglieder-import-familie.csv").toURI());
    Properties props = new Properties();
    props.put("separator", ";");
    props.put("suppressHeaders", "false");
    props.put("charset", "UTF-8");
    props.put("fileExtension", ".csv");
    Class.forName("org.relique.jdbc.csv.CsvDriver");
    try (Connection conn = DriverManager
        .getConnection("jdbc:relique:csv:" + file.getParent(), props);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
        ResultSet results = stmt.executeQuery(
            "SELECT * FROM \"mitglieder-import-familie\""))
    {
      results.next(); // Zeile 1: Angehöriger A
      assertEquals("1", ZeilenDurchlauf.getExterneZahlerId(results));
      results.next(); // Zeile 2: Vollzahler, Zelle leer
      assertNull(ZeilenDurchlauf.getExterneZahlerId(results));
    }
  }

  @Test
  void angehoerigeWerdenMitIhremVollzahlerVerknuepft() throws Exception
  {
    // Angehörige stehen in der Datei vor ihren Vollzahlern
    importieren("mitglieder-import-verknuepfung.csv");

    // doImport meldet Fehler nur über das Monitor-Log
    verify(monitor, never()).log(anyString());
    assertEquals(4, gespeichert.size());

    assertEquals(Long.valueOf(gespeichert("1").getID()),
        gespeichert("2").getVollZahlerID());
    assertEquals(Long.valueOf(gespeichert("4").getID()),
        gespeichert("3").getVollZahlerID());
    assertNull(gespeichert("1").getVollZahlerID());
    assertNull(gespeichert("4").getVollZahlerID());
  }

  @Test
  void unbekannterVollzahlerBrichtImportAb() throws Exception
  {
    importieren("mitglieder-import-unbekannter-vollzahler.csv");

    verify(monitor).log(contains("Vollzahler mit externer Mitgliedsnummer nicht gefunden: 99"));
    assertTrue(gespeichert.stream().noneMatch(m -> {
      try
      {
        return m.getVollZahlerID() != null;
      }
      catch (Exception e)
      {
        return false;
      }
    }));
  }
}
