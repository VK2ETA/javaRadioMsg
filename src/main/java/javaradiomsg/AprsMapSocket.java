/*
 * aprsmapsocket.java
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
import java.util.ArrayList;

/**
 * Class that handles a socket that external aprs mapping applications can connect to
 * @author per
 */
public class AprsMapSocket extends Thread {

    // Local variables
    private int port = 8063;                // What ip port to use
    private boolean portopen = true;        // Set to reflect the status
    private boolean connected = false;      // Have we got a connected mapping app?
    private Socket mapSocket = null;        // The socket to and from the mapping app
    private ServerSocket serverSocket = null;   // Server socket that listens for connects
    private Socket clientSocket = null;     // Handles a connected client
    private ArrayList<AprsMapClient> list;         // This is a list of client ocket handlers
    private AprsMapClient client=null;

  //
  // Accessor methods
  //

    /**
     * Get the set server socket port number
     * @return
     */
    public int getPort() {
        return port;
    }
    /**
     * Set the aprs server socket port, where to listen for connections
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

  /**
   * Set this to false to close the server socket
   * @param newVar the new value of portopen
   */
  public void setPortopen ( boolean newVar ) {
    portopen = newVar;
  }

  public void setPort() {
      port = 8063;
  }

  /**
   * Is the server socket open for business?
   * @return the value of portopen
   */
  public boolean getPortopen ( ) {
    return portopen;
  }

  /**
   * Check if we have a connected aprs map application
   * @return
   */
  public boolean isConnected() {
        return connected;
    }

  //
  // Other methods
  //

  /**
   * Open the socket that allows an external aprs mapping application to get, and
   * possibly later , input aprs data
   */

  @Override
  public void run(  )
  {
    if (openserver()) {
        list = new ArrayList<AprsMapClient>();
        mainloop();
    }
  }


  /**
   * Main handling loop
   */
  private void mainloop(){
    String inputLine, outputLine;
    try {
        while (portopen){
            connected = false;
            // Hold here and wait for a client to connect
            clientSocket = serverSocket.accept();
            // Here we have an existing client connection
            connected = true;
            // Create a new object that handles this connection
            client = new AprsMapClient();
            client.setClientSocket(clientSocket);
            client.start();
            client.setName("APRSmapClient");
            list.add(client);
            // And wait for the next one
        }
      }
      catch (Exception e) {
                Main.log.writelog("Error when writing to external aprs mapper", e, true);
      }
  }

  /**
   * Open the server socket
   * @return
   */
  private boolean openserver(){
        try {
            serverSocket = new ServerSocket(port);
            return true;
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+port);
            return false;
        }
  }

  /**
   * Send aprs message from jpskmail to external mapping application.
   * Make sure this is thread safe.
   */
  public synchronized void sendmessage(String aprsmessage)
  {
     // Send stuff if we are connected
     try {   
        if (clientSocket != null & clientSocket.isConnected()){
                // for each all the client objects and hit sendmessage on them
                for (AprsMapClient my : list){
                    my.SendMessage(aprsmessage);
                }
        }
     } catch (Exception e) {
                Main.log.writelog("Error when writing to external aprs mapper", e, false);
     }

  }


  /**
   * Send whatever data we have to the external mapping application
   * @return       boolean
   */
  private boolean senddata(  )
  {
    boolean res=true;

    // Return how this operation worked out
    return res;
  }


  /**
   * Data received from the external aprs mapper, now hand it off for parsing and
   * handling
   * @return       boolean
   */
  private boolean receiveddata(  )
  {
    boolean res=true;

    // Return how this operation worked out
    return res;
  }

}
