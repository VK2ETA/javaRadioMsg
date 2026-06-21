/*
 * MessageOutViewTableModel.java
 *
 * Copyright (C) 2010 Pär Crusefalk (SM0RWO)
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package javaradiomsg;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author per
 */
public class MessageOutViewTableModel extends AbstractTableModel{
     public static final int TO_INDEX = 0;
     public static final int SUBJECT_INDEX = 1;
     public static final int DATE_INDEX = 2;
     public static final int SIZE_INDEX = 3;

     protected String[] columnNames;
     protected Vector dataVector;

     public MessageOutViewTableModel(String[] columnNames) {
         this.columnNames = columnNames;
         dataVector = new Vector();
     }

    @Override
     public String getColumnName(int column) {
         return columnNames[column];
     }

    @Override
     public boolean isCellEditable(int row, int column) {
         return false;
     }

    @Override
    public Class getColumnClass(int column) {
         switch (column) {
             case TO_INDEX:
             case SUBJECT_INDEX:
             case DATE_INDEX:
             case SIZE_INDEX:
                 return String.class;
             default:
                return Object.class;
         }
     }

     public Object getValueAt(int row, int column) {
         Email mymail = (Email)dataVector.get(row);
         switch (column) {
             case TO_INDEX:
                return mymail.getTo();
             case SUBJECT_INDEX:
                return mymail.getSubject();
             case DATE_INDEX:
                return mymail.getDatestr();
             case SIZE_INDEX:
                 return mymail.getSize();
             default:
                return new Object();
         }
     }

    @Override
     public void setValueAt(Object value, int row, int column) {
         Email mymail = (Email)dataVector.get(row);
         fireTableCellUpdated(row, column);
     }

     public int getRowCount() {
         return dataVector.size();
     }

     public int getColumnCount() {
         return columnNames.length;
     }

     public void addRow(Email mymail) {
         dataVector.add(mymail);
         fireTableRowsInserted(
            dataVector.size() - 1,
            dataVector.size() - 1);
     }

     public Object getRowObject(int row){
        Email mymail = (Email)dataVector.get(row);
        return mymail;
     }

     /**
      * Remove all data
      */
     public void clear(){
        dataVector.clear();
        fireTableRowsDeleted(
            dataVector.size() - 1,
            dataVector.size() - 1);
     }
}
