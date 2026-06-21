/*
 * Igate.java
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
import java.net.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rein
 */
public class Igate {

    static Socket aprsSocket = null;
    static PrintWriter out = null;
    static BufferedReader in = null;
    static String[] aprshosts = {"netherlands.aprs2.net", "germany.aprs2.net", "italys.aprs2.net"};
    static int aprsindex = 0;
    static String aprsCall = "";
    static String aprscheck = "";
    static String aprspass = "";
    static String aprsversion = "";
    public static boolean connected = false;
    static String aprs_output = "";
    static String[] status = {"", "Testing"};
    static int maxstatus = 2;
    static boolean aprsavailable = true;
    //VK2ETA add Linked station service
    static ArrayList<String> linkedStationsList = new ArrayList<String>();
    public static long lastAprsServerPacketTime = 0L;
    
    public static void start() throws IOException {

        aprsversion = Main.application;

        while (!connected) {

            aprsCall = Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER"));
            if (aprsCall.length() == 0) {
                //Not a valid callsign
                return;
            }
            
            aprspass = getHash(aprsCall);

            connected = true;
            try {
                aprsSocket = new Socket(Main.aprsServer, 14580);
                out = new PrintWriter(aprsSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        aprsSocket.getInputStream()));
            } catch (UnknownHostException e) {
                System.out.println("Don't know about host:" + Main.aprsServer + " " + e);
                connected = false;
                aprsindex++;

            } catch (IOException e) {
                System.out.println("Couldn't get I/O for "
                        + "the connection to:" + Main.aprsServer + " " + e);
                connected = false;
                aprsindex++;

            }

            //            String initaprs = "user " + aprscall + " pass " + aprspass + " vers " + aprsversion + " filter u/" + aprscall + "\n";
            String initaprs = "user " + aprsCall + " pass " + aprspass + " vers " + aprsversion + " filter u/PSKAPR" + "\n";

            // send init string to the aprs server
            if (out != null) {
                out.println(initaprs);
            }

            String line = "";

            try {
                while (connected & (line = in.readLine()) != null) {
                    Main.q.Message("APRS connected to " + Main.aprsServer, 10);
                    System.out.println("APRS Server says:" + line);
                    break;
                }
            } catch (NullPointerException np) {
                Main.q.Message("APRS server " + Main.aprsServer + " not available", 10);

            }

            if (connected) {
                break;
            } else if (!connected & aprsindex > 0) {
                System.out.println("No APRS connection, to be retried...");
//                    Main.mainui.IgateSwitch.setSelected(false);
//                    Main.mainui.IgateSwitch.setText("OFF");
                aprsavailable = false;
                break;
            } else {
                System.out.println("reconnecting aprs");
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Igate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void read() throws IOException {
        String line = "";

        if (in.ready()) {     
            while ((line = in.readLine()) != null) {
                lastAprsServerPacketTime = System.currentTimeMillis();
                //VK2ETA Add listening for and forwarding of APRS messages
                if (line.length() > 0 & !line.startsWith("#")) {
                    //System.out.println(line);
                    if (line.contains("PSKAPR") && line.contains("GATING")) {
                        //Find the station call and remove if previously gated here
                        Pattern pl = Pattern.compile("^(\\w*\\-*\\d*)>(\\w*),*(.*?):(.)(\\w*\\-*\\d*)\\s*:GATING (.*)(\\{*.*)");
                        Matcher ml = pl.matcher(line);
                        if (ml.lookingAt()) {
                            String tocall = ml.group(6).toUpperCase(Locale.US);
                            removeStationFromList(tocall);
                        }
                    } else { 
                        //Must be a message to be forwarded
                        //VK2ETA check what other frames may make it here and filter out if required
                        Main.mapSock.sendmessage(line + "\n");
                        //Do we have an APRS message to forward?
                        //elsif ($line =~ /^(\w*\-*\d*)>(\w*),*(.*?):(.)(\w*\-*\d*)\s*:(.*)(\{*.*)/) {	# message
                        doAprsRfForward(line);
                    }
                    //if (line.length() > 0 & line.contains("PSKAPR") & !line.startsWith("#") & Main.mainui.APRS_IS.isSelected()) {
                    if (Main.mainui.APRS_IS.isSelected()) {
                        Main.aprsBeaconText += line + "\n";
                    }
                    break;
                } else {
                    break;
                }
            }
        }
    }

    public static boolean write(String aprs_out) throws IOException {

        if (connected) {
            if (aprs_out.length() > 0) {
                out.println(aprs_out);
                Main.aprsBeaconText = aprs_out + "\n";
                aprs_out = "";
            }
            return true;
        }
        return false;
    }

    public void close() throws IOException {
        out.close();
        in.close();
        aprsSocket.close();
    }

    public static void Loadservers() {
        File f = new File(Main.homePath + Main.dirPrefix + "aprsservers");
        if (f.exists()) {
            try {
                // Open the file
                FileInputStream fstream = new FileInputStream(Main.homePath + Main.dirPrefix + "aprsservers");
                // Get the object of DataInputStream
                DataInputStream lfin = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(lfin));
                String strLine;
                //Read File Line By Line
                int i;
                for (i = 0; i < 3; i++) {
                    strLine = br.readLine();
                    Main.aprsServer = strLine;
                    System.out.println(Main.aprsServer + "|");
                }

                //Close the input stream
                lfin.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }

    }

    private static String getHash(String call) {

        int Out = 0;
        int i;

        Pattern fm = Pattern.compile("([A-Z0-9]+).*");
        Matcher fmm = fm.matcher(call);
        if (fmm.lookingAt()) {
            call = fmm.group(1);
        }

        byte k[] = new byte[2];

        k[0] = 115;
        k[1] = (byte) 226;

        int len = call.length();

        byte c[] = new byte[10];
        char d[] = new char[10];

        for (i = 0; i < len; i++) {
            d[i] = 0;
        }

        for (i = 0; i < len; i++) {
            d[i] = call.charAt(i);
        }

        for (i = 0; i < len; i++) {
            c[i] = (byte) d[i];
        }

        for (i = 0; i < len; i += 2) {
            k[0] ^= c[i];
            k[1] ^= c[i + 1];
        }

        k[0] &= 127;
        int highbyte = (int) k[0];

        if (highbyte < 0) {
            highbyte += 256;
        }

        k[1] &= 255;

        int lowerbyte = (int) k[1];

        if (lowerbyte < 0) {
            lowerbyte += 256;
        }

        Out = highbyte * 256 + lowerbyte;

        return Integer.toString(Out);
    }
    
    //Forward APRS message over the air
    private static void doAprsRfForward(String line) {
        //elsif ($line =~ /^(\w*\-*\d*)>(\w*),*(.*?):(.)(\w*\-*\d*)\s*:(.*)(\{*.*)/) {	# message
        Pattern pl = Pattern.compile("^(\\w*\\-*\\d*)>(\\w*),*(.*?):(.)(\\w*\\-*\\d*)\\s*:(.*)(\\{*.*)");
        Matcher ml = pl.matcher(line);
        if (ml.lookingAt()) {
            String fromcall = ml.group(1).toUpperCase(Locale.US);
            String groupcall = ml.group(2);
            String path = ml.group(3);
            String type = ml.group(4);
            String tocall = ml.group(5).toUpperCase(Locale.US);
            String message = ml.group(6);
            if (getLinkedCall(tocall).length() > 0) {
                //We have a valid call, send over RF. We do not check for duplicates here (YET?)
                try {
                    String sendMessage = fromcall + ">PSKAPR::" + tocall + " :" + message;//For now + mesgnumber;
                    Main.q.setCallsign(tocall);
                    Main.q.send_aprsmessage(sendMessage);
                } catch (InterruptedException e) {
                    //Nothing
                }     
           }
        }
    }

    //Check that station is in the list and was last linked less than 45 minutes ago
    public static String getLinkedCall(String stationRaw) {
        String linkedCall = "";
        //Aprs call sign (E.g. VK2ETA-5). Upper case, no prefix (E.g "fk8"), no suffix (E.g. "/pm"), but can have id (E.g. -10)
        String station = stationRaw.toUpperCase(Locale.US);

        //Look in list if already registered
        if (station.length() > 0) {
            for (int i = 0; i < linkedStationsList.size(); i++) {
                if (linkedStationsList.get(i).toUpperCase(Locale.US).contains(station)) {
                    //Was it less than 45 minutes ago?
                    long lastLinkTime = 0L;
                    String entry = linkedStationsList.get(i);
                    String[] linkRecord = entry.split("\\|");
                    if (linkRecord.length == 2) { //Valid entries only
                        try {
                            lastLinkTime = Long.parseLong(linkRecord[1]);
                        } catch (NumberFormatException e) {
                            //Nothing
                        }
                        //Max 45 minutes since last interaction as APRS-IS server seem to 
                        //   hold the link for one hour approx.    
                        if (System.currentTimeMillis() - lastLinkTime > 45 * 60000) {
                            //Too old, remove
                            linkedStationsList.remove(i);
                        } else {
                            //All good, valid link from that station, get the original call (E.g. fk8/vk2eta/pm)
                            linkedCall = linkRecord[0];
                        }
                    }
                    break;
                }
            }
        }
        return linkedCall;
    }
   
    //Remove a Station to the linked stations list
    public static void removeStationFromList(String stationRaw) {
        String station = Main.cleanCallForAprs(stationRaw);

        if (station.length() > 0) {
            for (int i = 0; i < linkedStationsList.size(); i++) {
                if (linkedStationsList.get(i).toUpperCase(Locale.US).contains(station)) {
                    linkedStationsList.remove(i);
                    break;
                }
            }
        }
    }    

    //Add a Station to the linked stations list
    public static void addStationToList(String newStation) {
        boolean inList = false;

        //Look in list if already registered
        if (newStation.length() > 0) {
            for (int i = 0; i < linkedStationsList.size(); i++) {
                if (linkedStationsList.get(i).contains(newStation)) {
                    inList = true;
                    //Update the time
                    linkedStationsList.set(i, newStation + "|" + System.currentTimeMillis());
                    break;
                }
            }
            //Not in list yet, add
            if (!inList) {
                linkedStationsList.add(newStation + "|" + System.currentTimeMillis());
            }
        }
        //Now purge the list of obsolete stations
        for (int i = 0; i < linkedStationsList.size(); i++) {
            long lastLinkTime = 0L;
            String entry = linkedStationsList.get(i);
            String[] linkRecord = entry.split("\\|");
            if (linkRecord.length == 2) { //Valid entries only
                try {
                    lastLinkTime = Long.parseLong(linkRecord[1]);
                } catch (NumberFormatException e) {
                    //Nothing
                }
                //Max 45 minutes since last interaction as APRS-IS server seem to 
                //   hold the link for one hour approx.    
                if (System.currentTimeMillis() - lastLinkTime > 45 * 60000) {
                    //Too old, remove
                    linkedStationsList.remove(i);
                }
            }
        }
    }

}
