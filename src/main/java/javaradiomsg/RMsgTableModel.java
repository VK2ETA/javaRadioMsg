/*
 * RMsgTableModel.java
 *
 * Copyright (C) 2021 John Douyere (VK2ETA)
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

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jdouyere
 */
public class RMsgTableModel extends DefaultTableModel{
    
    @Override
     public boolean isCellEditable(int row, int column) {
         return false;
     }
    
}
