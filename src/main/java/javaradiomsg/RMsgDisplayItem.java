/*
 * RMsgDisplayItem.java
 *
 * Copyright (C) 2018-2021 John Douyere (VK2ETA) 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package javaradiomsg;

public class RMsgDisplayItem {
    

    public RMsgObject mMessage;
    public float currentDistance;
    public float previousDistance;
    public boolean inRange = false;
    public boolean myOwn = false;

    public RMsgDisplayItem (RMsgObject mNewMessage, float currentDistance, float previousDistance, boolean inRange, boolean myOwn) {
        this.mMessage = mNewMessage;
        this.currentDistance = currentDistance;
        this.previousDistance = previousDistance;
        this.inRange = inRange;
        this.myOwn = myOwn;
    }

}
