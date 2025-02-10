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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0453 extends AbstractDDLUpdate
{
  public Update0453(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("konto", new Column("kontoart",
        COLTYPE.INTEGER, 0, null, false, false)));

    execute("update konto set kontoart = 1 where anlagenkonto IS NULL");
    execute("update konto set kontoart = 1 where anlagenkonto IS FALSE");
    execute("update konto set kontoart = 2 where anlagenkonto IS TRUE");
    
    // Das kann man sp�ter machen, so kann man auch wieder in der SW zur�ck gehen
    // execute(dropColumn("konto", "anlagenkonto"));
  }
}
