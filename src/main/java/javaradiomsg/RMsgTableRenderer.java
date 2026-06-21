/*
 * RMsgTableRenderer.java
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jdouyere
 */

public class RMsgTableRenderer extends JTextArea implements TableCellRenderer
{
    RMsgTableRenderer()
    {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    Border paddingSent = BorderFactory.createEmptyBorder(0, 150, 0, 0);
    Border paddingReceived = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    Color badCrcColor = new Color(255, 150, 100);
    Color badCrcColorSelected = new Color(210, 58, 36);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value != null) {
            RMsgDisplayItem mItem = (RMsgDisplayItem) value;
            setText((mItem == null) ? "" : mItem.mMessage.formatForList(false));//No tracking for now
            setSize(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight(row));
            //  Recalculate the preferred height now that the text and renderer width have been set.
            int preferredHeight = getPreferredSize().height;
            if (table.getRowHeight(row) != preferredHeight && preferredHeight > 1) {
                table.setRowHeight(row, preferredHeight);
            }
            setBorder(mItem.myOwn ? paddingSent : paddingReceived);
            if (isSelected) {
                if (mItem.mMessage.crcValid || mItem.mMessage.crcValidWithRelayPW || mItem.mMessage.crcValidWithIotPW) {
                    this.setBackground(table.getSelectionBackground());
                } else {
                    this.setBackground(badCrcColorSelected);
                }
            } else {
                if (mItem.mMessage.crcValid || mItem.mMessage.crcValidWithRelayPW || mItem.mMessage.crcValidWithIotPW) {
                    this.setBackground(table.getBackground());
                } else {
                    this.setBackground(badCrcColor);
                }
            }
        }
        return this;
    }
}