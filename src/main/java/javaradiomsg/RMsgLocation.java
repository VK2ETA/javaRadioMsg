/*
 * RMsgLocation.java
 *
 * Copyright (C) 2017-2021 John Douyere (VK2ETA) 
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

public class RMsgLocation {
    private double latitude;
    private double longitude;
    private double speed;
    
    public RMsgLocation(String locString) {
        
    }
    
    public void setSpeed(double mySpeed) {
        this.speed = mySpeed;
    }
    public void setLatitude(double myLatitude) {
        this.latitude = myLatitude;
    }
    public void setLongitude(double myLongitude) {
        this.longitude = myLongitude;
    }
    
    
    public double getLatitude() {
        return this.latitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public double getSpeed() {
        return this.speed;
    }
    
    
    
}
