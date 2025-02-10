/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0461 extends AbstractDDLUpdate
{
  public Update0461(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    //Spalte zahler in mitgliedskonto
    Column col = new Column("zahler", COLTYPE.BIGINT, 0, null, false,
        false);
    execute(addColumn("mitgliedskonto", col));
    
    //Index und ForeignKey in mitgliedskonto
    Index idx = new Index("ixMitgliedskonto6", false);
    idx.add(col);
    execute(idx.getCreateIndex("mitgliedskonto"));
    
    execute(createForeignKey("fkMitgliedskonto6",
            "mitgliedskonto", "zahler", "mitglied", "id",
            "SET NULL", "NO ACTION"));
    
    // Den Zahler auf Mitglied setzen bei bestehenden Sollbuchungen
    execute("update mitgliedskonto set zahler = mitglied ");
    
    // Spendenbescheinigung nicht l�schen wenn Zahler gel�scht wird
    execute(dropForeignKey("fkSpendenbescheinigung2", "spendenbescheinigung"));
    execute(createForeignKey("fkSpendenbescheinigung2", "spendenbescheinigung",
        "mitglied", "mitglied", "id", "SET NULL", "NO ACTION"));
  }
}
