/*
 * serialport.java
 *
 * Copyright (C) 2008 Pär Crusefalk
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
import java.util.*;
import java.util.ArrayList;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 *
 * @author per
 */

public class GpsSerialPort {
   static CommPortIdentifier portId;
   static CommPortIdentifier saveportId;
   static Enumeration        portList;
   InputStream              inputStream;
   OutputStream             outputStream;
   SerialPort               serialPort;
   Thread                   readThread;
   BufferedReader           reader; 
   CommPort                 commPort;
   CommPortIdentifier       portIdentifier;
   SerialPort               serport;
   SerialReader             thereader;
   boolean                  curstate=false;

   /**
    * GpsSerialPort constructor, only calls its superclass constructor for now.
    */
   public GpsSerialPort()
   {
        super();
   }
    
   /**
    * Connect to the GPS and try to get data, update nmeaparser when data is received
    * @param portName
    * @param speed
    * @throws java.lang.Exception
    */ 
   void connect (String portName, int speed) throws Exception
    {
        try{
            if (!checkComPort(portName)){
                Main.log.writelog("Serial port "+portName+" does not exist! Was the GPS removed?", true);
            }
            else
            {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if ( portIdentifier.isCurrentlyOwned() )
            {
                Main.log.writelog("Port is currently in use!", true);
            }
            else
            {
                commPort = portIdentifier.open(this.getClass().getName(),2000);
            
                if (commPort!=null && commPort instanceof SerialPort )
                {
                    serport = (SerialPort) commPort;
                    serport.setSerialPortParams(speed,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                    InputStream in =  serport.getInputStream();
                    BufferedReader bufread = new BufferedReader(new InputStreamReader(in));

                    OutputStream out =  serport.getOutputStream();
                    outputStream = out;
                    // not used nw (new Thread(new SerialWriter(out))).start();
                 
                    thereader = new SerialReader(bufread,in);
                    serport.addEventListener(thereader);
                    serport.notifyOnDataAvailable(true);
                 
                    // Save the state
                    curstate = true;
                }
                else
                {
                    Main.log.writelog("Unknown port!", true);
                }
            }
            }
        }
        catch(Exception ex)
        {
            Main.log.writelog("Could not connect to GPS port.",  true);
        }
        finally{
            if (commPort == null){
                Main.log.writelog("Could not open GPS port!",  true);
            }
        }
    } 

   public Boolean checkComPort(String myPort){
        GpsSerialPort mySerial;
        Enumeration portlist;
        CommPortIdentifier portId;
        Boolean checksout=false;

        try {
            // Only do this if the port is closed
            if (!Main.gpsPort.getconnectstate()) {
                mySerial = Main.gpsPort;
                portlist = CommPortIdentifier.getPortIdentifiers();

                while (portlist.hasMoreElements()) {
                    portId = (CommPortIdentifier) portlist.nextElement();
                    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        if (portId.getName().equals(myPort)) {
                            checksout = true;
                        }
                    }
                }

            }
            return checksout;
        } catch (Exception e) {
            Main.log.writelog("Problem fetching serial ports from the system!", e, true);
            return checksout;
        }
    }

    /*
    * Disconnect the port
    */
   void disconnect(){
        try{
           // Save the state
           curstate = false;
           if (serport != null){
                // Remove the listeners
                serport.removeEventListener();

                //Close the port
                serport.close();
            
            }
        }
        catch(Exception ex){
            Main.log.writelog("Failed when closing GPS port.",  true);
        }
   }
   
   /**
    * Return the port status
    * @return
    */
   boolean getconnectstate(){
        return curstate;
   }
   
    /**
     * Write a string to the serial port
     * @param command What to send
     */
    public void writestring(String command){
            try
            {                
                // Add line end
                command = command + "\r\n";
                outputStream.write(command.getBytes());
            }
            catch ( Exception e )
            {
                Main.log.writelog("Error when writing data to serial port", e, true);
            }            
    }

     /**
     * Write a character to the serial port
     * @param command What to send
     */
    public void writechar(int chr){
            try
            {                
                // Add line end
                outputStream.write(chr);
            }
            catch ( Exception e )
            {
                Main.log.writelog("Error when writing data to serial port", e, true);
            }            
    }

   /**
          * Have the serialport class get the available ports on this system
          * using the current operating system.
          * @return
          */
         public ArrayList<String> getCommports()
         {
            ArrayList<String> myarr = new ArrayList<String>();

            try
            {
                java.util.Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
                while ( portEnum.hasMoreElements() ) 
                {
                    CommPortIdentifier porthandle = (CommPortIdentifier) portEnum.nextElement();
                    String portName = porthandle.getName();
                    myarr.add(portName);
                }        
                return myarr;            
            }
            catch(Exception ex)
            {
                Main.log.writelog("Could not get GPS ports.", ex, true);
                return myarr;
            }
            finally{
                return myarr;
            }
         }
 
    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
         
     /**
     * Handles the input coming from the serial port. A new line character
     * is treated as the end of a block in this example. 
     */
    public static class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        BufferedReader inreader;
        private byte[] buffer = new byte[1024];
        private String mytest="";
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }

        public SerialReader ( BufferedReader reader, InputStream in )
        {
            this.inreader = reader;
            this.in = in;
        }
        
        @Override
        public void serialEvent(SerialPortEvent arg0) {
            // nmea messages handled at this point
            String nmeagga="$GPGGA";
            String nmeaprc="$GPRMC";

             switch (arg0.getEventType()) {
                    case SerialPortEvent.BI:
                    case SerialPortEvent.OE:
                    case SerialPortEvent.FE:
                    case SerialPortEvent.PE:
                    case SerialPortEvent.CD:
                    case SerialPortEvent.CTS:
                    case SerialPortEvent.DSR:
                    case SerialPortEvent.RI:
                    case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                        break;
                    case SerialPortEvent.DATA_AVAILABLE:
                        try
                        {
                            String myRead="";
                            while (in.available()>0)
                            {
                                myRead = inreader.readLine();
                                //System.out.println(myRead);
                            }
                            // Check for position message
                            if (myRead.startsWith(nmeaprc)) {
                                   parsenmeadata(myRead);
                            } 
                        }
                        catch(Exception ex)
                        {
                            Main.log.writelog("Exception when reading from GPS!", ex, false);
                        }
               }
                                
         }

        
        
        /**
         * Get one of the wanted nmea lines and parse it
         * @param nmeadata Raw nmea data string
         */
        public void parsenmeadata(String nmeadata){
            Main.gpsData.newdata(nmeadata);
            if (Main.gpsData.getFixStatus()){
                Main.configuration.setLatitude(Main.gpsData.getLatitude());
                Main.configuration.setLongitude(Main.gpsData.getLongitude());
                Main.configuration.setSpeed(Main.gpsData.getSpeed());
                Main.configuration.setCourse(Main.gpsData.getCourse());
            }
        }

    }
    

}