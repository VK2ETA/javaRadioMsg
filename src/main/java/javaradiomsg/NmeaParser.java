/*
 * 
 *  Copyright (C) 2009 Pär Crusefalk (SM0RWO)  
 *    
 *  This program is distributed in the hope that it will be useful,  
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 *  GNU General Public License for more details.  
 *    
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 * 
 */


package javaradiomsg;

import java.io.UnsupportedEncodingException;
import static java.lang.Math.abs;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
/**
 *
 * @author per
 */
public class NmeaParser {
    private String innmea="";   // Holds incoming data string    
    public String fixat="";
    public String latitude="";
    public String longitude="";
    private String lathemisphere="";
    private String longitudeside="";
    public boolean validfix=false;
    public String speed="000.0";
    public String course="0";

    // nmea messages handled at this point
    private String nmeagga="$GPGGA";
    private String nmeaprc="$GPRMC";
    
    /**
     * Constructor, accepts an nmea data string and will handle that right away
     * @param nmeadata
     */
    public void nmeaparser(String nmeadata){
        innmea = nmeadata;
        // Work if there is something to do
        if (innmea.length()>1) {
            digest();
        }
    }

    /**
     * Fetch the parsed latitude
     * @return a latitude string
     */
    public String getLatitude( )    {
            return latitude;
    }

    /**
     * Fetch the parsed longitude
     * @return
     */
    public String getLongitude(){
        return longitude;
    }
    
    /**
     * Get the current speed
     * @return
     */
    public String getSpeed(){
        String myResult="000";
        try {
            if (speed != null) {
                myResult = getintstr(speed,3);
            }
            return myResult;
        } catch (Exception num) {
            System.err.println("getspeed failed.");
            return "000";
        }
        
    }
    
    /**
     * Get the course
     * @return
     */
    public String getCourse(){
        String myResult="000";
        
        try {
            if (course != null) {
                myResult = getintstr(course,3);
                //myResult = course.substring(0, 3);
            }
            return myResult;
        } catch (Exception num) {
            System.err.println("Getcourse failed");
            return "000";
        }
    }
    
    /**
     * Fix taken at, the gps time
     * @return
     */
    public String getFixTime(){
        try {
            if (fixat != null) {
                String myResult = getintstr(fixat,6);
                //myResult = FormatTime(myResult);
                return myResult;
            } else {
                return "000000";
            }
        } catch (Exception Ex) {
            System.err.println("getFixTime failed.");
            return "000000";
        }
    }

    /**
     * Gets a time string from the gps fix and creates a formatted time string
     * @param fix
     * @return
     */
    private String FormatTime(String fix){
        // Make sure we always return 6 characters
        Integer num = 6-fix.length();
        if (num<0) num=0;
        for (int i=1; i<num+1; i++){
            fix="0"+fix;
        }
        return fix;
    }


    /**
     * Get the latest gps fix status, valid position fix?
     * @return  true or false, the fix is valid
     */
    public boolean getFixStatus(){
        return validfix;
    }
    
    /**
     * Refill the data and update the object
     * @param nmeadata
     */
    public void newdata(String nmeadata){
        try{
            innmea = nmeadata;
            // Work if there is something to do
            if (innmea != null && innmea.length()>1) {
                digest();
                //System.out.println(nmeadata);
            }
        }
        catch (Exception ex){
           Logger.getLogger(NmeaParser.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }    

    /**
     * Take care of the incoming data
     */
    private void digest(){
        String msgtype="";
        String [] indata = null;    // The array that will hold the message
        
        // get the kind of msg we are dealing with 
        msgtype = innmea.substring(0, 6);
        if (msgtype.equals(nmeagga)) {
            indata = innmea.split(",");
            // Check if the message count is ok
            if (indata.length==15){
                // Check if its a valid fix
                if (indata[6].equals("1")){
                    validfix = true;
                    // The fix is valid so get the data
                    fixat = indata[1];
                    latitude = ConvertLatitude(indata[2],indata[3]);
                    longitude = ConvertLongitude(indata[4],indata[5]);
                }
                else
                    validfix = false;
            }
        } else if (msgtype.equals(nmeaprc))
        {
            indata = innmea.split(",");
            // Check if the message count is ok
            if (indata.length>10)
            {
                // Check if its a valid fix
                if (indata[2].equals("A"))
                {
                    validfix = true;
                    // The fix is valid so get the data
                    fixat = indata[1];
                    latitude = ConvertLatitude(indata[3],indata[4]);
                    longitude = ConvertLongitude(indata[5],indata[6]);
                    if (indata[8].equals("")){
                        course = "0";
                    } else
                        course = indata[8];

                    if (indata[7].equals("")){
                        speed="000.0";
                    } else
                        speed = indata[7];
                }
                else
                    validfix = false;
            
            } 
        }
    }

    /**
     * Convert GPS latitude format, sometimes ddmm.mmm, into decimal format for APRS
     * @param Decimal the latitude from the gps
     * @return  A decimal latitude
     */
    private String ConvertLatitude(String inlat, String hemi){
        Double mydouble;  // Work with this
        Double result;   // Put the finished one here
        String myLatitude="0.0";
        
        try {
            // Get the degrees
            //Deprecated result = new Double(inlat.substring(0, 2)).doubleValue();
            result = Double.parseDouble(inlat.substring(0, 2));
            // Get only the minutes 
            //Deprecated mydouble = new Double(inlat.substring(2)).doubleValue();
            mydouble = Double.parseDouble(inlat.substring(2));
            // Convert to decimal fraction of degrees
            mydouble = mydouble / 60;
            // Put it back together
            result = result + mydouble;
            // Make sure the format is proper
            DecimalFormat latform = new DecimalFormat("00.0000");
            myLatitude = latform.format(result);
            // Make sure there is a period in there
            myLatitude = myLatitude.replace(",", ".");

            // If its in the south then its negative
            if (hemi.equals("S")) {
                myLatitude = "-" + myLatitude;
            }
        } catch (NumberFormatException numberFormatException) {
            Main.log.writelog("Latitude conversion fail.", numberFormatException);
        }       
        // Return the complete value
        return myLatitude;
    }

    /**
     * Convert nmea position format into clean decimal for APRS
     * @param inlon
     * @param hemi
     * @return
     */
    private String ConvertLongitude(String inlon, String hemi){
        Double mydouble;  // Work with this
        Double result;   // Put the finished one here
        String myLongitude="0.0";
        
        try {
            // Get the degrees
            //Deprecated result = new Double(inlon.substring(0, 3)).doubleValue();
            result = Double.parseDouble(inlon.substring(0, 3));
            // Get only the minutes 
            //Deprecated mydouble = new Double(inlon.substring(3)).doubleValue();
            mydouble = Double.parseDouble(inlon.substring(3));
            // Convert to decimal fraction of degrees
            mydouble = mydouble / 60;
            // Put it back together
            result = result + mydouble;
            // Make sure the format is ok
            DecimalFormat lonform = new DecimalFormat("000.0000");
            myLongitude = lonform.format(result);
            // Make sure there is a period in there
            myLongitude = myLongitude.replace(",", ".");

            // Add a zero if less than 100
            String mypoint = myLongitude.substring(2, 3);
            if (mypoint.equals(".")) {
                myLongitude = "0" + myLongitude;
            }

            // If its west then its negative
            if (hemi.equals("W")) {
                myLongitude = "-" + myLongitude;
            }
        } catch (NumberFormatException numberFormatException) {
            Main.log.writelog("Longitude conversion fail", numberFormatException);
        }
       
        // Return the complete value
        return myLongitude;
    }

    /**
     * Takes a string and looks for an int in a string, returns that
     * @param inval
     * @return 
     */
    private String getintstr(String inval,Integer reqlength){
        String retur="";
        try {
            if (inval != null && inval.length() > 0) {
                int dot = inval.indexOf('.');
                if (dot>0){
                    retur = inval.substring(0, dot);
                }
                else
                {
                    retur = inval;
                }
                
                int le = retur.length();
                if (le < reqlength) {
                    for (int a = 0; a < reqlength - le; a++) {
                        retur = "0" + retur;
                    }
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Failed to format gps data.", e);
        }
        return retur;
    }
    
    /**
     * Write a hex string, good for getting a gps out of sirf mode
     * @param hexstr
     */
    public void writehextogps(String hexstr){
       String hex=""; 
        int len = hexstr.length();
        int i=0;
        while (i<len-1){
            hex = hexstr.substring(i, i+2);
            int intValue = Integer.parseInt(hex, 16);  
            Main.gpsPort.writechar(intValue);
            i=i+2;
        }
    }

    /**
     * This should be changed from the static behaviour, later...
     * @param Command
     */
    public void writetogps(String Command){
        String complete="";
        complete = Command + getChecksum(Command);
        Main.gpsPort.writestring(complete);
    }

    /**
     * Write sirf message to gps, only give it the payload
     * @param hexmsg payload only
     */
    public void writehexsirfmsg(String hexmsg){
        /*
        Format of a SiRF packet :

        ****************************************************
        * begin ** length ** payload ** checksum ** end *
        ****************************************************

        begin (2 bytes) = 0xa0a2
        length (2 bytes) = number of bytes in payload
        checksum (2 bytes) = XOR of bytes in payload AND 0×7fff
        end (2 bytes) = 0xb0b3

        Example of SIRF message dump :
        a0a2 0009 000000010203040506 0007 b0b3        
        */
        
        String startmsg = "a0a2";
        String endmsg = "b0b3";
        Integer mylen = (hexmsg.length()/2);
        String msglen = Integer.toHexString(mylen);
         if (msglen.length()==2) msglen="00"+msglen;
         if (msglen.length()==3) msglen="0"+msglen;

        String message="";
        String checksum="";
        
        checksum = this.getSirfChecksum(hexmsg);
        
        message = startmsg + msglen + hexmsg + checksum + endmsg;
        writehextogps(message);
        //System.out.println(message);
        
    }
    
     /** Calculates the checksum for an nmea sentence */
     public static String getChecksum(String sentence) {
          // Loop through all chars to get a checksum
          char character;
          int checksum = 0;
          int length = sentence.length();
          for (int i = 0; i < length; i++) {
               character = sentence.charAt(i);
               switch (character) {
                    case '$':
                         // Ignore the dollar sign
                         break;
                    case '*':
                         // Stop processing before the asterisk
                         break;
                    default:
                         // Is this the first value for the checksum?
                         if (checksum == 0) {
                              // Yes. Set the checksum to the value
                              checksum = (byte) character;
                         } else {
                              // No. XOR the checksum with this character's value
                              checksum = checksum ^ ((byte) character);
                         }
               }
          }
          // Return the checksum formatted as a two-character hexadecimal
          return Integer.toHexString(checksum);
     }

/**
 * This creates a sirf message checksum, pse see message descritption from sirf.
 * @param payload only
 * @return a sirf formatted checksum
 */
     private String getSirfChecksum(String payload){
        String myc="";
            Integer mylen = payload.length();
            Integer checkint=0;
        
            int i=0;
            while (i<mylen){
                myc = payload.substring(i, i+2);
                checkint = checkint + Integer.valueOf(myc, 16).intValue();
                i=i+2;
            }
        checkint = checkint & 0x7fff;
        String mysum = Integer.toHexString(checkint);
        if (mysum.length()==2) mysum="00"+mysum;
        if (mysum.length()==3) mysum="0"+mysum;
        return mysum;
    }
     
    private static float Round(float Rval, int Rpl) 
     {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }
    
    
    //Converts a 6 characters Grid Square, with all characters in uppercase, to a latitude and 
    //  longiture string in the format <Degrees><DecimalMinutes>, with E/W and N/S lagging characters 
    //E.g. 2700.00S/13300.00E
    public static String grid6UpperToGps(String gridSquare) {
        String latLon = "";
        byte[] gsb = null;
        
        if (gridSquare.length() != 6) {
            return latLon;
        }
        try {
            gsb = gridSquare.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return latLon;
        }
        //Double lat = (Asc(Mid(m, 2, 1)) - 65) * 10 + Val(Mid(m, 4, 1)) + (Asc(Mid(m, 6, 1)) - 97) / 24 + 1 / 48 - 90;
        double lat = (((double)gsb[1] - 65) * 10) + ((double)gsb[3] - 48) + (((double)gsb[5] - 65) / 24) + (1/48) - 90;
        //double lon = (Asc(Mid(m, 1, 1)) - 65) * 20 + Val(Mid(m, 3, 1)) * 2 + (Asc(Mid(m, 5, 1)) - 97) / 12 + 1 / 24 - 180;
        double lon = (((double)gsb[0] - 65) * 20) + (((double)gsb[2] -48) * 2) + (((double)gsb[4] - 65) / 12) + (1/24) - 180;
        String NS = "N";
        if (lat < 0) {
            NS = "S";
        }
        lat = abs(lat);
        String EW = "E";
        if (lon < 0) {
            EW = "W";
        }
        lon = abs(lon);
        //Format the decimal degrees
        int latDeg = (int)(lat);
        int lonDeg = (int)lon;
        double latMin = (lat - latDeg) * 60;
        double lonMin = (lon - lonDeg) * 60;
        //Make sure we use a dot and not a comma as decimal separator
        DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US"));
        decimalFormat.applyPattern("00.00");
        String latMinStr = decimalFormat.format(latMin);
        String lonMinStr = decimalFormat.format(lonMin);
        decimalFormat.applyPattern("00");
        String latDegStr = decimalFormat.format(latDeg);
        decimalFormat.applyPattern("000");
        String lonDegStr = decimalFormat.format(lonDeg);
        latLon = latDegStr + latMinStr + NS + "/" + lonDegStr + lonMinStr + EW;
        return latLon;
    }
    
}