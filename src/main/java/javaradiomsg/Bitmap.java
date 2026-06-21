/*
 * Bitmap.java
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
 * This is a contact manager that handles...well, contacts.
 * Its pretty simple but simplicity is also powerful :-)
 * 
 */

package javaradiomsg;

import java.nio.ByteBuffer;

/**
 *
 * @author jdouyere
 */
public class Bitmap {
    private int width;
    private int height;
    
    public int getWidth() {
        return width;
    }
     
    public int getHeight() {
        return height;
    }
     
    public int getXYZ() {
        return 0;
    }
    
    public static Bitmap decodeFile(String fullPicturePath) {
        
        Bitmap bitmap = new Bitmap();
        return bitmap;
    }
    
    
    public void copyPixelsToBuffer(ByteBuffer byteBuffer) {
        
    }
}
