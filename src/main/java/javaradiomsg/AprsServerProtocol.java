/*
 * AprsServerProtocol.java
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

/**
 *
 * @author per
 */
public class AprsServerProtocol {
    private static final int WAITING = 0;
    private static final int CONNECTED = 1;
    private static final int ESTABLISHED =2;
    private static final int POSITIONRCVD=3;
    private static final int MESSAGERCVD=4;
    private static final int DISCONNECTED=5;
    private String serverhelo = "# "+Main.application;
    private String authhelo = "# Local APRS server in jpskmail";
    private String errtext = "# Not implemented yet";
    private String msgcontent="";
    private String msgheader="";

    private int state = WAITING;

    public String processInput(String theInput) {
        String theOutput = null;
        if (state == WAITING) {
            theOutput = serverhelo;
            state = CONNECTED;
        } 
        else if (state == CONNECTED) {
            theOutput = authhelo;
            state = ESTABLISHED;
        }
        else if (state == ESTABLISHED) {
            msgcontent = GetContent(theInput);
            if (msgcontent.length()>2) {
                Main.mainui.SendAprsMsgDeSocket(msgcontent);
            }
            theOutput = "";
            state = ESTABLISHED;
        }
        return theOutput;
    }

    /**
     * Split the incoming APRS data and get the load
     * @return
     */
    private String GetContent(String InText){
        String myText="";
        if (!InText.equals("")){
            String[] mystr = InText.split(":", 2);
            if (mystr.length>1){
                myText = mystr[1]; 
            }
        }
        return myText;
    }

}