/*
 * aprsmapclient.java
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

import java.io.*;
import java.net.*;

/**
 *
 * @author per
 */
public class AprsMapClient  extends Thread{
    // Local variables
    private Socket clientSocket = null;     // Handles a connected client
    private PrintWriter sout = null;        // out to the aprs client
    private BufferedReader sin = null;      // data from the aprs client
    private String inputLine;               // From the client socket
    private String outputLine;              // To the client socket
    private AprsServerProtocol protocol = null; // Class that handles aprs protocol answers

    // Give a client socket to this thread
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

  @Override
  public void run(  )
  {
   if (clientSocket.isConnected())
       try{
            // Create the protocol object
            protocol = new AprsServerProtocol();

            // Get streams from the clientconnection
            sout = new PrintWriter(clientSocket.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Push the helo out to the client
            outputLine = protocol.processInput(null);
            sout.println(outputLine);
            // This will never detect a disconnect...?
            while (clientSocket.isConnected()) {
                  if ((inputLine = sin.readLine()) != null){
                    outputLine = protocol.processInput(inputLine);
                    sout.println(outputLine);  
                  }
                   Thread.sleep (100); // give another thread a chance...
            }
            sout.close();
            sin.close();
            sout = null;
            sin = null;
            clientSocket.close();
        }
       catch(Exception ex){
            Main.log.writelog("Client socket error ", ex);
       }
  }

  /**
   * Check wether we are still connected. used to remove the object
   * @return
   */
  public boolean GetConnectStatus(){
    Boolean mystat=false;
      if (clientSocket != null)
        mystat=true;
      return mystat;
  }

  /**
   * Send a message to the aprs map
   * @param aprsmessage
   */
  public void SendMessage(String aprsmessage){
        // Send stuff if we are connected
        try{
            if (clientSocket != null & clientSocket.isConnected()){
                sout.println(aprsmessage);
                }
           }   
            catch (Exception e) {
                Main.log.writelog("Error when writing to external aprs mapper", e, false);
            }
       }
  
  }
