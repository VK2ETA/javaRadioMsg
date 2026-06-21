/*
 * config.java  
 *   
 * Copyright (C) 2008 Pär Crusefalk (SM0RWO)  
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author per
 */
public class Config {
// Private variables 
    // options dialogue

    private String callsign;
    private String callsignAsServer;
    private String linktoserver;
    private String blocklength;
    private String latitude;
    private String longitude;
    private String course;
    private String speed;
    private String beaconqrg;
    private String beacon;
    private String beacontime; // beacon timer interval
    private String beaconcomp; // Compressed beacon
    private String autolink;
    // email settings
    private String pophost;
    private String popuser;
    private String poppassword;
    private String replyto;
    private String findupassword;
    // Configuration
    private String logfile;
    private int maxretries;
    private int idletime;
    private int txdelay;
    private int offsetminute;
    private int offsetsecond;

    private String WebLabel1 = "";    
    private String Webpage1 = "none";
    private String Webpage1b = "";
    private String Webpage1e = "";
    private String WebLabel2 = ""; 
    private String Webpage2 = "none";
    private String Webpage2b = "";
    private String Webpage2e = "";
    private String WebLabel3 = "";
    private String Webpage3 = "none";
    private String Webpage3b = "";
    private String Webpage3e = "";
    private String WebLabel4 = "";
    private String Webpage4 = "none";
    private String Webpage4b = "";
    private String Webpage4e = "";
    private String WebLabel5 = "";
    private String Webpage5 = "none";
    private String Webpage5b = "";
    private String Webpage5e = "";
    private String WebLabel6 = "";
    private String Webpage6 = "none";
    private String Webpage6b = "";
    private String Webpage6e = "";

    public String qrg0 = "";
    public String qrg1 = "";
    public String qrg2 = "";
    public String qrg3 = "";
    public String qrg4 = "";
    public String RigOffset = "";
    // APRS Server
    private String APRSServer = "yes";
    private String APRSServerPort = "8063";

    // various values
    private String statustxt;

    private String filepath;

    // Common properties file object
    Properties configFile;

    //enums for the config file, use these instead of hardcoded strings, less confusion for all then!
    public enum user {
        CALL, SERVER, BLOCKLENGTH, LATITUDE, LONGITUDE, BEACON, BEACONTIME, BEACONQRG, BEACONCOMP, AUTOLINK, ICON, ICON2, STATUS, CALLSIGNASSERVER
    }

    public enum email {
        POPHOST, POPUSER, POPPASS, RETURNADDRESS, COMPRESSED
    }

    public enum configuration {
        LOGFILE, DCD, RETRIES, IDLETIME, TXDELAY, OFFSETMINUTE, OFFSETSECONDS
    }

    public enum devices {
        GPSD, GPSPORT, GPSSPEED, GPSENABLED, APRSSERVER, APRSSERVERPORT
    }

    public enum aprs {
        APRSCALL
    }

    public enum rigctrl {
        RIGCTL, SCANNER, RIGOFFSET, QRG0, QRG1, QRG2, QRG3, QRG4
    }

    public enum state {
        yes, no
    }

    public enum web {
        URL1, URL1B, URL1E, URL2, URL2B, URL2E, URL3, URL3B, URL3E, URL4, URL4B, URL4E, URL5, URL5B, URL5E, URL6, URL6B, URL6E
    }

    public enum modem {
        MODEMIP, MODEMIPPORT, MODEMPOSTAMBLE, MODEMPREAMBLE, MULTI
    }

    // constructor
    /**
     *
     */
    public Config(String path) {

        filepath = path;
        configFile = new Properties();
        // Check the config file, create and fill if necessary
        initialcheckconfigfile();
        try {
            statustxt = getPreference(user.STATUS.toString());
            callsign = getPreference(user.CALL.toString());
            callsignAsServer = getPreference(user.CALLSIGNASSERVER.toString());
            linktoserver = getPreference(user.SERVER.toString());
            latitude = getPreference(user.LATITUDE.toString());
            longitude = getPreference(user.LONGITUDE.toString());
            blocklength = getPreference(user.BLOCKLENGTH.toString());
            Main.currentModemProfile = blocklength;
            beaconqrg = getPreference(user.BEACONQRG.toString(), "0");
            beaconcomp = getPreference(user.BEACONCOMP.toString(), "1");
            beacon = getPreference(user.BEACON.toString(), "1");
            beacontime = getPreference(user.BEACONTIME.toString(), "30");
            autolink = getPreference(user.AUTOLINK.toString(), "1");
            Webpage1 = getPreference(web.URL1.toString());
            Webpage1b = getPreference(web.URL1B.toString());
            Webpage1e = getPreference(web.URL1E.toString());
            Webpage2 = getPreference(web.URL2.toString());
            Webpage2b = getPreference(web.URL2B.toString());
            Webpage2e = getPreference(web.URL2E.toString());
            Webpage3 = getPreference(web.URL3.toString());
            Webpage3b = getPreference(web.URL3B.toString());
            Webpage3e = getPreference(web.URL3E.toString());
            Webpage4 = getPreference(web.URL4.toString());
            Webpage4b = getPreference(web.URL4B.toString());
            Webpage4e = getPreference(web.URL4E.toString());
            Webpage5 = getPreference(web.URL5.toString());
            Webpage5b = getPreference(web.URL5B.toString());
            Webpage5e = getPreference(web.URL5E.toString());
            Webpage6 = getPreference(web.URL6.toString());
            Webpage6b = getPreference(web.URL6B.toString());
            Webpage6e = getPreference(web.URL6E.toString());
            qrg0 = getPreference(rigctrl.QRG0.toString(), "1048000");
            qrg1 = getPreference(rigctrl.QRG1.toString(), "1048000");
            qrg2 = getPreference(rigctrl.QRG2.toString(), "1048000");
            qrg3 = getPreference(rigctrl.QRG3.toString(), "1048000");
            qrg4 = getPreference(rigctrl.QRG4.toString(), "1048000");
            RigOffset = getPreference(rigctrl.RIGOFFSET.toString(), "1000");
            APRSServer = getPreference(devices.APRSSERVER.toString(), state.yes.toString());
            APRSServerPort = getPreference(devices.APRSSERVERPORT.toString(), "8063");
            Main.aprsCall = getPreference(aprs.APRSCALL.toString(), "N0CAL");
        } catch (Exception e) {
            callsign = "N0CALL";
            linktoserver = "N0CALL";
            blocklength = "6";
            latitude = "0.0";
            longitude = "0.0";
            beaconqrg = "0";
            beaconcomp = "1";
            beacon = "1";
            beacontime = "30";
            autolink = "1";
            statustxt = " ";
            Webpage1 = "none";
            Webpage2 = "none";
            Webpage3 = "none";
            Webpage4 = "none";
            Webpage5 = "none";
            Webpage6 = "none";
            APRSServer = state.yes.toString();
            APRSServerPort = "8063";
        }
    }

    /**
     * If there is no config file then one should be created, it should have
     * good defaults.
     */
    private void initialcheckconfigfile() {

        try {
            // Check if there is a configuration file
            boolean exists = (new File(filepath + "configuration.xml")).exists();
//      System.out.println(filepath + "configuration.xml");
            // There is no file, we must create one
            if (!exists) {
                OutputStream fo = new FileOutputStream(filepath + "configuration.xml");
                configFile.setProperty(user.CALL.toString(), "N0CAL");
                configFile.setProperty(user.SERVER.toString(), "N0CAL");
                configFile.setProperty(user.BLOCKLENGTH.toString(), "7");
                configFile.setProperty(user.LATITUDE.toString(), "0.0");
                configFile.setProperty(user.LONGITUDE.toString(), "0.0");
                configFile.setProperty(user.BEACONQRG.toString(), "0");
                configFile.setProperty(user.BEACONCOMP.toString(), "1");
                configFile.setProperty(user.BEACON.toString(), "1");
                configFile.setProperty(user.BEACONTIME.toString(), "30");
                configFile.setProperty(user.AUTOLINK.toString(), "1");
                configFile.setProperty(user.STATUS.toString(), Main.application);
                // Gps defaults
                configFile.setProperty(devices.GPSPORT.toString(), "/dev/ttyS0");
                configFile.setProperty(devices.GPSSPEED.toString(), "4800");
                configFile.setProperty(devices.GPSENABLED.toString(), state.no.toString());
                // ICON and DCD
                configFile.setProperty(configuration.DCD.toString(), "3");
                configFile.setProperty(user.ICON.toString(), "y");
                configFile.setProperty(user.ICON2.toString(), "/");
                // Mail options
                configFile.setProperty(email.POPHOST.toString(), "none");
                configFile.setProperty(email.POPUSER.toString(), "none");
                configFile.setProperty(email.POPPASS.toString(), "none");
                configFile.setProperty(email.RETURNADDRESS.toString(), "myself@myemail.com");

                configFile.setProperty(web.URL1.toString(), "none");
                configFile.setProperty(web.URL1B.toString(), "");
                configFile.setProperty(web.URL1E.toString(), "");
                configFile.setProperty(web.URL2.toString(), "none");
                configFile.setProperty(web.URL2B.toString(), "");
                configFile.setProperty(web.URL2E.toString(), "");
                configFile.setProperty(web.URL3.toString(), "none");
                configFile.setProperty(web.URL3B.toString(), "");
                configFile.setProperty(web.URL3E.toString(), "");
                configFile.setProperty(web.URL4.toString(), "none");
                configFile.setProperty(web.URL4B.toString(), "");
                configFile.setProperty(web.URL4E.toString(), "");
                configFile.setProperty(web.URL5.toString(), "none");
                configFile.setProperty(web.URL5B.toString(), "");
                configFile.setProperty(web.URL5E.toString(), "");
                configFile.setProperty(web.URL6.toString(), "none");
                configFile.setProperty(web.URL6B.toString(), "");
                configFile.setProperty(web.URL6E.toString(), "");

                configFile.setProperty(modem.MODEMIP.toString(), "localhost");
                configFile.setProperty(modem.MODEMIPPORT.toString(), "7322");
                configFile.setProperty(modem.MODEMPOSTAMBLE.toString(), "");
                configFile.setProperty(modem.MODEMPREAMBLE.toString(), "");
                configFile.setProperty(modem.MULTI.toString(), "YES");

                configFile.setProperty(devices.APRSSERVER.toString(), "yes");
                configFile.setProperty(devices.APRSSERVERPORT.toString(), "8063");
                configFile.setProperty(aprs.APRSCALL.toString(), "N0CAL");
                configFile.storeToXML(fo, "Configuration file for JPSKmail client");
                fo.close();
            }
        } catch (Exception e) {
            Main.log.writelog("Could not create settings file, directory permission trouble?", true);
        }
    }

    /**
     *
     * @return
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     *
     * @return
     */
    public String getCallsignAsServer() {
        return callsignAsServer;
    }

    /**
     *
     * @param newcall
     */
    public void setCallsign(String newcall) {
        callsign = newcall;
    }

    /**
     *
     * @return
     */
    public String getServer() {
        return linktoserver;
    }

    /**
     *
     * @param newcall
     */
    public void setServer(String newcall) {
        linktoserver = newcall;
        setPreference(user.SERVER.toString(), newcall);
    }

    /**
     *
     * @param newlat
     */
    public void setLatitude(String newlat) {
        latitude = newlat;
    }

    /**
     *
     * @return
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     *
     * @param newlon
     */
    public void setLongitude(String newlon) {
        longitude = newlon;
    }

    /**
     *
     * @return
     */
    public String getLongitude() {
        return longitude;
    }

    public void setSpeed(String newspeed) {
        speed = newspeed;
    }

    public String getSpeed() {
        return speed;
    }

    public void setCourse(String newcourse) {
        course = newcourse;
    }

    public String getCourse() {
        return course;
    }

    /**
     *
     * @param newlength
     */
    public void setBlocklength(String newlength) {
        blocklength = newlength;
    }

    /**
     *
     * @return
     */
    public String getBlocklength() {
        return blocklength;
    }

    /**
     *
     * @param newqrg
     */
    public void setBeaconqrg(String newqrg) {
        beaconqrg = newqrg;
        setPreference(user.BEACONQRG.toString(), newqrg);
    }

    /**
     *
     * @return
     */
    public String getBeaconqrg() {
        return beaconqrg;
    }

    public void SetBeacon(String newbeacon) {
        beacon = newbeacon;
        setPreference(user.BEACON.toString(), beacon);
    }

    public String getBeacon() {
        return beacon;
    }

    /**
     * Compressed beacon setting
     *
     * @return
     */
    public String getBeaconcomp() {
        return beaconcomp;
    }

    /**
     * Compressed beacon setting
     *
     * @param beaconcomp
     */
    public void setBeaconcomp(String beaconcomp) {
        this.beaconcomp = beaconcomp;
        setPreference(user.BEACONCOMP.toString(), beaconcomp);
    }

    public void setAutolink(String newautolink) {
        autolink = newautolink;
        setPreference(user.AUTOLINK.toString(), autolink);
    }

    public String getAutolink() {
        return autolink;
    }

    /**
     *
     * @return
     */
    public String getStatus() {
        return statustxt;
    }

    public void SetWebLabels(String url1, String url2, String url3, String url4, String url5, String url6) {
        WebLabel1 = url1;
        WebLabel2 = url2;
        WebLabel3 = url3;
        WebLabel4 = url4;
        WebLabel5 = url5;
        WebLabel6 = url6;
    }

    public void SetWebPages(String url1, String url2, String url3, String url4, String url5, String url6) {
        Webpage1 = url1;
        Webpage2 = url2;
        Webpage3 = url3;
        Webpage4 = url4;
        Webpage5 = url5;
        Webpage6 = url6;
    }

    public void SetWebPagesB(String url1, String url2, String url3, String url4, String url5, String url6) {
        Webpage1b = url1;
        Webpage2b = url2;
        Webpage3b = url3;
        Webpage4b = url4;
        Webpage5b = url5;
        Webpage6b = url6;
    }

    public void SetWebPagesE(String url1, String url2, String url3, String url4, String url5, String url6) {
        Webpage1e = url1;
        Webpage2e = url2;
        Webpage3e = url3;
        Webpage4e = url4;
        Webpage5e = url5;
        Webpage6e = url6;
    }

    /**
     *
     * @param newstatus
     */
    public void setStatus(String newstatus) {
        statustxt = newstatus;
    }

    public void saveURLs() {
            setPreference("LABELURL1", WebLabel1);
            setPreference("LABELURL2", WebLabel2);
            setPreference("LABELURL3", WebLabel3);
            setPreference("LABELURL4", WebLabel4);
            setPreference("LABELURL5", WebLabel5);
            setPreference("LABELURL6", WebLabel6);
            setPreference("URL1", Webpage1);
            setPreference("URL2", Webpage2);
            setPreference("URL3", Webpage3);
            setPreference("URL4", Webpage4);
            setPreference("URL5", Webpage5);
            setPreference("URL6", Webpage6);
            setPreference("URL1B", Webpage1b);
            setPreference("URL2B", Webpage2b);
            setPreference("URL3B", Webpage3b);
            setPreference("URL4B", Webpage4b);
            setPreference("URL5B", Webpage5b);
            setPreference("URL6B", Webpage6b);
            setPreference("URL1E", Webpage1e);
            setPreference("URL2E", Webpage2e);
            setPreference("URL3E", Webpage3e);
            setPreference("URL4E", Webpage4e);
            setPreference("URL5E", Webpage5e);
            setPreference("URL6E", Webpage6e);
    }

    /**
     * /
     * Load properties and set config object
     *
     * @param Key
     * @param Value
     * @return
     */
    public String getAPRSServer() {
        return APRSServer;
    }

    public void setAPRSServer(String APRSServer) {
        this.APRSServer = APRSServer;
    }

    public String getAPRSServerPort() {
        return APRSServerPort;
    }

    public void setAPRSServerPort(String APRSServerPort) {
        this.APRSServerPort = APRSServerPort;
    }

    public String[] getQRGs() {
        String[] qrgs = {qrg0, qrg1, qrg2, qrg3, qrg4};
        return qrgs;
    }

    public void setPreference(String Key, String Value) {
        try {
            InputStream f = new FileInputStream(Main.homePath + Main.dirPrefix + "configuration.xml");
            configFile.loadFromXML(f);
            f.close();
//   System.out.println(Key + "," + Value);
            configFile.setProperty(Key, Value);
            OutputStream fo = new FileOutputStream(Main.homePath + Main.dirPrefix + "configuration.xml");
            configFile.storeToXML(fo, "Configuration file for JPSKmail client");
//  System.out.println(">>Key=" + Key + " value=" + configFile.getProperty(Key) + "\n");  // debug
        } catch (Exception e) {
            Main.log.writelog("Could not store setting: " + Key, true);
        }
    }

    /**
     *
     * @param Key
     * @return
     */
    public String getPreference(String Key) {
        String myReturn = "";
        try {
            InputStream f = new FileInputStream(Main.homePath + Main.dirPrefix + "configuration.xml");
            configFile.loadFromXML(f);
            f.close();
            myReturn = configFile.getProperty(Key);
            if (myReturn.equals("")) {
                myReturn = "";
            }
        } catch (Exception ex) {
            return "";
        }
//    System.out.println("Key=" + Key + " value=" + configFile.getProperty(Key) + "\n");  // debug
        if (!myReturn.equals("")) {
            return (myReturn);
        } else {
            return "";
        }
    }

    /**
     * Get the saved value, if its not there then use the default value
     *
     * @param Key
     * @param Default
     * @return
     */
    public String getPreference(String Key, String Default) {
        Properties configFile = new Properties();
        String myReturn = "";
        try {
            InputStream f = new FileInputStream(Main.homePath + Main.dirPrefix + "configuration.xml");
            configFile.loadFromXML(f);
            f.close();
            myReturn = configFile.getProperty(Key);
            if (myReturn.equals("")) {
                myReturn = Default;
            }
            return myReturn;
        } catch (Exception ex) {
            return Default;
        }
    }

}
