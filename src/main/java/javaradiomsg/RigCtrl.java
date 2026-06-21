/*
 * Rigctl.java
 * 
 * Copyright (C) 2008 Pär Crusefalk and Rein Couperus
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
import java.net.Socket;
import java.net.MalformedURLException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

/**
 *
 * @author rein
 */
public class RigCtrl {

    static Socket sock;
    public PrintWriter pout;
    public BufferedReader in;
    static boolean opened = false;
    static String host = "localhost";
    static int port = 4532;
    static String Offset = "1000";
    static int OFF = 1000;
    static String[] freqs = {"0", "0", "0", "0", "0",};
    static String freq = "0";
    static XmlRpcClient client = null;
    static boolean function_ok = true;

    public static void Rigctl() {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(Main.xmlRpcURL));
            opened = true;
            function_ok = true; //Set flag in case of Fldigi restart
        } catch (MalformedURLException ex) {
            //Main.log.writelog("Problem with xmlrpc: " + ex, true);
            function_ok = false;
            opened = false;
        }
        try {
            client = new XmlRpcClient();
        } catch (Exception ex) {
            System.out.println("XmlRpc problem");
            opened = false;
        }
        client.setConfig(config);
    }

    public static void initfreqs() {
        OFF = Integer.parseInt(Offset);
        freqs[0] = Main.configuration.getPreference("QRG0");
        freqs[1] = Main.configuration.getPreference("QRG1");
        freqs[2] = Main.configuration.getPreference("QRG2");
        freqs[3] = Main.configuration.getPreference("QRG3");
        freqs[4] = Main.configuration.getPreference("QRG4");
        Main.freqOffset = Integer.parseInt(Main.configuration.getPreference("RIGOFFSET", "0"));
        OFF = Main.freqOffset;
    }

    public static String Setfreq(String Frequency) {

        if (function_ok & Frequency != null) {
            Double Outfreq = Double.parseDouble(Frequency);
            Outfreq -= OFF;

            Object[] params = new Object[]{Outfreq};

            try {
                Object result = client.execute("main.set_frequency", params);
                String myobject = result.toString();
                Double myfrequency = Double.parseDouble(myobject);
                Integer oldfrequency = myfrequency.intValue();
                return oldfrequency.toString();

            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc set frequency: " + ex, true);
                function_ok = false;
            }
        } else if (Frequency != null) Rigctl(); //Try to re-connect
        return "0";
    }

    public static String SetSqlLevel(int Sql) {

        double d;

        //VK2ETA try squelch of zero
        //if (function_ok & Sql != 0) {
        if (function_ok & Sql >= 0) {

            d = Double.parseDouble(Integer.toString(Sql));
            Object[] params = new Object[]{d};

            try {
                Object result = client.execute("main.set_squelch_level", params);
                String myobject = result.toString();
                Double mySqlLevel = Double.parseDouble(myobject);
                Integer oldlevel = mySqlLevel.intValue();
                return oldlevel.toString();

            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc set squelch_level: " + ex, true);
                function_ok = false;
            }
        } else if (Sql != 0) Rigctl(); //Try to re-connect
        return "0";
    }

    public static String Getfreq() {

        Object[] params = null;

        if (function_ok) {
            try {
                Object result = client.execute("main.get_frequency", params);
                String myobject = result.toString();
                Double myfrequency = Double.parseDouble(myobject);
                Integer frequency = myfrequency.intValue() + OFF;
                Integer curfreq = myfrequency.intValue();
                Main.currentFreq = Integer.toString(curfreq);
                String newfreq = Integer.toString(frequency);
                //         System.out.println(newfreq);
                return newfreq;

            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc get frequency: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
        return "0";
    }

    public static String GetSNR() {

        Object[] params = null;

        if (function_ok) {

            try {
                Object result = client.execute("main.get_status1", params);
                return result.toString();
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc get snr: " + ex, true);
                function_ok = false;
            } catch (NullPointerException np) {
                //          Main.log.writelog("Problem with xmlrpc get snr: " + np, true)
                return "";
            }
        } else Rigctl(); //Try to re-connect

        return "";

    }

    public static String GetQual() {

        Object[] params = null;

        if (function_ok) {

            try {
                Object result = client.execute("modem.get_quality", params);
                String str = result.toString();

                return str;
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc get quality: " + ex, true);
                //Add Fldigi restart flag
                function_ok = false;
            } catch (NullPointerException np) {
                //          Main.log.writelog("Problem with xmlrpc get quality: " + np, true)
                return "";
            }
        } else Rigctl(); //Try to re-connect

        return "";

    }

    public static boolean GetSquelch() {

        Object[] params = null;

        if (function_ok) {

            try {
                Object result = client.execute("main.get_squelch", params);

                String str = result.toString();
//          System.out.println("STR:" + str);          
                if (str.equals("true") | str.equals("1")) {
                    return true;
                } else {
                    return false;
                }
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc get SQL: " + ex, true);
                function_ok = false;
            } catch (NullPointerException np) {
//            Main.log.writelog("Problem with xmlrpc get quality: " + np, true);
//            System.out.println("SQL null"); 
                return false;
            }
        } else Rigctl(); //Try to re-connect

        return false;

    }

    public static String ToggleSquelch() {

        if (function_ok) {

            Object[] params = null;

            try {
                Object result = client.execute("main.toggle_squelch", params);

            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc set SQL: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
        return "0";
    }

    public static String setSquelchOn(Boolean on) { //false = Off
        if (function_ok) {

            Object[] params = new Object[]{on};
            Object result = null;

            try {
                result = client.execute("main.set_squelch", params);

            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc set SQL: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
        return "0";
    }
    
    public static boolean setEotRsid(Boolean onOff) { //false = Off
        if (function_ok) {

            Object[] params = new Object[]{onOff};
            Object result = null;

            try {
                result = client.execute("main.set_eot_rsid", params);
                String str = result.toString();
//          System.out.println("STR:" + str);          
                if (str.equals("true") | str.equals("1")) {
                    return true;
                } else {
                    return false;
                }
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc set SQL: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
        return false;
    }

    public void Message(String msg, int time) {
        Main.statusLine = msg;
        Main.statusLineTimer = time;
    }

    public static void Loadfreqs(String server) {
        File f = new File(Main.homePath + Main.dirPrefix + server + ".chn");
        if (f.exists()) {
            try {
                // Open the file 
                FileInputStream fstream = new FileInputStream(Main.homePath + Main.dirPrefix + server + ".chn");
                // Get the object of DataInputStream
                DataInputStream lfin = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(lfin));
                String strLine;
                //Read File Line By Line
                strLine = br.readLine();
                freqs = strLine.split(",");

                //Close the input stream
                lfin.close();
            } catch (Exception e) {//Catch exception if any
                //System.err.println("Error: " + e.getMessage());
            }

        }

    }
    
    
    public static void Tune() {

        if (function_ok) {
            Object[] params = null;
            try {
                Object result = client.execute("main.tune", params);
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc Tune: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
    }

    public static void Abort() {

        if (function_ok) {
            Object[] params = null;
            try {
                Object result = client.execute("main.abort", params);
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc Abort: " + ex, true);
                function_ok = false;
            }
        } else Rigctl(); //Try to re-connect
    }
    
    
    
    public static int SetAudiofreq(int aFrequency) {

        if (function_ok & aFrequency >= 500 & aFrequency <= 2500) {

            Object[] params = new Object[]{aFrequency};

            try {
                Object result = client.execute("modem.set_carrier", params);
                String myobject = result.toString();
                int oldfrequency = Integer.parseInt(myobject);
                return oldfrequency;
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc SetAudiofreq: " + ex, true);
                function_ok = false;
            }
        } else if (!function_ok) Rigctl(); //Try to re-connect
        return -1;
    }

    /* Parked code
    public static int SetModeByName(String newMode) {

        if (function_ok) {

            Object[] params = new Object[]{new String(newMode)};

            try {
                Object result = client.execute("modem.set_by_name", params);
                String myobject = result.toString();
                return 0;
            } catch (XmlRpcException ex) {
                //Main.log.writelog("Problem with xmlrpc modem.set_by_name: " + ex, true);
                function_ok = false;
            }
        } else if (!function_ok) Rigctl(); //Try to re-connect
        return -1;
    }
    */


}
