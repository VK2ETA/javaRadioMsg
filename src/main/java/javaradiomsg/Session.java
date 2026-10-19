/*
 * Session.java
 * 
 * Copyright (C) 2008 Pär Crusefalk and Rein Couperus
 * Copyright (C) 2018-2022 Pskmail Server and RadioMsg sections by John Douyere (VK2ETA) 
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
import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author rein
 */
public class Session {

    public String mycall;
    public String myserver;
    private static String blocklength;
    private static int Blocklength;
    public String session_id;
    private String Lineoutstring;

    // service flags
    public boolean Headers = false;
    public boolean FileDownload = false;
    public boolean WWWDownload = false;
    public boolean CwwwDownload = false;
    public boolean FileList = false;
    public String ThisFile = "";
    public File Trfile = null;
    public String Transaction = "";
    public String ThisFileLength = "";
    public boolean MsgDownload = false;
    public boolean CMsgDownload = false;
    public boolean base64attachment = false;
    public boolean plaintextattachment = false;
    public String attachmentFilename = null;
    //Server service flags
    public boolean emailUpload = false;
    public boolean CompressedEmailUpload = false;

    //rx is my machine, tx is the other machine
    public static String tx_lastsent;    //Last block sent to me
    public static String tx_missing; //List of repeats  I need to resend.
    public static String lastTx_missing = ""; //last list of repeats (to see if we keep repeating the same blocks over)
    public static int sameRepeat = 0; //Identical repeats count
    public static String tx_ok;  //last block received conseq ok at other end.
    public static String tx_lastreceived;  // at other end

    private static String rx_lastsent;    // Last block I sent
    private static String rx_ok;   //  Text o.k  until this one
    private static String rx_lastreceived;   // Last block I received
    public static String rx_missing; //List of repeat requests I need to send to other side
    private static boolean rx_lastBlock; // Flag for end of frame
    public static boolean justSentStopCmd = false;
    private static String[] rxbuffer = new String[64];
    //private static int beginblock;
    private static int goodblock;
    private static int thisblock;
    private static int lastblock;
    public static int lastdisplayed = 0;
    private int lastgoodblock_received;
    public static boolean gooddata;
    public String serverversion;
    public String hispubkey = "";

    public static String[] txbuffer = new String[64];
    private static int lastqueued = 0;

    // progress bar values
    public static int DataReceived = 0;
    public static int DataSize = 0;

    private FileWriter headers = null;
    public FileWriter dlFile = null;
    public FileWriter pFile = null;
    private FileWriter tmpmessage = null;
    private FileWriter inbox = null;
    private BufferedWriter iacout;
    private FastFEC f;
    //Server files
    private FileWriter tempEmailFile = null;

    //  private  String lastqueued;  //Last block in my send queue
    public Session() {
        String path = Main.homePath + Main.dirPrefix;
        Config cf = new Config(path);
        f = new FastFEC();
        myserver = cf.getServer();
        blocklength = "6";
        try {
            blocklength = cf.getBlocklength();
        } catch (Exception e) {
            blocklength = "6";
        }
//             Blocklength = Integer.parseInt(blocklength);
        Blocklength = 6;
        Main.txText = "";

        initSession();
    }

    public void sendUpdate() {
        String output;
        String record;
        String rec1 = "";
        String pophost = Main.configuration.getPreference("POPHOST");
        String popuser = Main.configuration.getPreference("POPUSER");
        String poppass = Main.configuration.getPreference("POPPASS");
        String returnaddr = Main.configuration.getPreference("RETURNADDRESS");
        String recinfo = pophost + "," + popuser + "," + poppass
                + "," + returnaddr;

        // Server version 1.5+ understands encrypted data
        if (Main.sversion > 1.5) {

            String rec64 = Base64Encode.base64Encode(recinfo);
            output = Main.cr.encrypt(hispubkey, rec64);
            record = "~RECx" + output + "\n";
            while (record.length() > 30) {
                Main.txText += record.substring(0, 30) + "\n";
                record = record.substring(30);
            }
            Main.txText += record + "Q\n";
        } else {
            record = "~RECx" + Base64Encode.base64Encode(recinfo);
            int eol_loc = -1;
            String frst = null;
            String secnd = null;
            eol_loc = record.indexOf(10);
            if (eol_loc != -1) {
                frst = record.substring(0, eol_loc - 1);
                secnd = record.substring(eol_loc + 1, record.length());
                record = frst + secnd;
                Main.txText += record + "\n";
            } else // So we have an old version but could not find user settings, warn about that!
            {
                Message("Missing user record to send!", 5);
            }
        }
    }

    public void Message(String msg, int time) {
        Main.statusLine = msg;
        Main.statusLineTimer = time;
    }

    public void RXStatus(String text) {
        if (text.length() > 2) {
            tx_lastsent = text.substring(0, 1);
            tx_lastreceived = text.substring(1, 2);
            tx_ok = text.substring(2, 3);
            tx_missing = text.substring(3);
            if (tx_missing.length() > 0 & Main.connected) {
                Main.txbusy = true;
            } else {
                Main.txbusy = false;
            }
        }
    }

    @SuppressWarnings("empty-statement")
    public String getTXStatus() {
        rx_missing = "";
        int endblock;
        lastblock = (tx_lastsent.charAt(0) - 32);
        endblock = (lastgoodblock_received + 64) % 64;
        int i = 0;
        int index = 0;
        int runvar = 0;
        if (lastblock < lastdisplayed) {
            runvar = lastblock + 64;
        } else {
            runvar = lastblock;
        }
        for (i = lastdisplayed + 1; i <= runvar; i++) {
            index = i % 64;
            if (rxbuffer[index].equals("")) {;
                char m = (char) (index + 32);
                rx_missing += Character.toString(m);
            }
        }
        // generate the status block      
        if (rx_missing.length() == 0) {
            Main.rxbusy = false;
        } else {
            Main.rxbusy = true;
        }
        goodblock = lastdisplayed;
        char last = (char) (lastgoodblock_received + 32);
        rx_lastreceived = Character.toString(last);
        char ok = (char) (goodblock + 32);
        rx_ok = Character.toString(ok);
        int Missedblocks = rx_missing.length();
        Main.missedBlocks = Missedblocks;
        if (Missedblocks > 8) {
            rx_missing = rx_missing.substring(0, 8);
        }
        //We just sent a STOP command, clear receive data
        if (justSentStopCmd) {
            rx_ok = rx_lastreceived = tx_lastsent;
            rx_missing = "";
            lastdisplayed = lastblock;
            lastgoodblock_received = lastblock;
            for (int j = 0; j < 64; j++) {
                rxbuffer[j] = "";
            }
        }
        String outstr = rx_lastsent + rx_ok + rx_lastreceived + rx_missing;
        return outstr;
    }
  
    //Reset STOP command flag provided the server has received our STOP command
    public void checkStopFlag() {
        if (tx_ok.equals(rx_lastsent)) {
            //Reset flag
            justSentStopCmd = false;
            //Abort all receiving transactions
            abortAllRxTransactions();
        }
    }

    public String getRXmissing() {
        rx_missing = "";
        int i = 0;
        int end = thisblock;
        if (thisblock <= lastdisplayed & lastdisplayed - thisblock > 49) {
            end = (thisblock + 64) % 64;
        }

        for (i = lastdisplayed + 1; i < end; i++) {
            char m = (char) ((i % 64) + 32);
            rx_missing += Character.toString(m);
        }

        rx_missing = rx_missing.substring(0, 8);

        return rx_missing;
    }

    //Used in Main loop for detecting idle TTY sessions by the TTY server
    public String getBlockExchanges() {
        //rx is my machine, tx is the other machine
        return (tx_lastsent + rx_lastsent);    //Last block sent to me  + Last block I sent
    }

    //Used in Main loop for measuring the link quality (from a data perspective)
    //public double RXGoodBlocksRatio() {
    //rx is my machine, tx is the other machine
    //  return(1);
    // }
    public void initSession() {
        tx_lastsent = " ";
        tx_lastreceived = " ";
        tx_ok = " ";
        tx_missing = "";
        rx_lastsent = " ";
        rx_ok = " ";
        rx_lastreceived = " ";
        rx_missing = "";
        //Needed when receive a re-connect request from client
        Main.myRxStatus = "   ";
        rx_lastBlock = false;
        //beginblock = 0;
        goodblock = 0;
        thisblock = 0;
        lastblock = 0;
        lastqueued = 1; // to make sure the first command is transmitted...
        lastdisplayed = 0;
        gooddata = true;
        lastgoodblock_received = 0;
        serverversion = "1.0";
        hispubkey = "";

        for (int i = 0; i < 64; i++) {
            rxbuffer[i] = "";
            txbuffer[i] = "";
        }
        Lineoutstring = " ";
        Main.rxbusy = false;
        Main.txbusy = false;

        // compile the matchers
    }

    // handles the rx buffer and calculates the new TXStatus after every Block
    public String doRXBuffer(String block, String index) throws FileNotFoundException, IOException, Exception {

        if (block.length() > 0) { //valid block
            thisblock = index.charAt(0) - 32;
            if (thisblock < 64) {
                rxbuffer[thisblock] = block;
            }

            if (lastdisplayed > 63) {
                lastdisplayed -= 64;
            }

            if (thisblock > lastgoodblock_received | (lastgoodblock_received - thisblock) > (64 - 24)) {
                lastgoodblock_received = thisblock;
            }

            while (!rxbuffer[(lastdisplayed + 1) % 64].equals("")) {
                // display this block

                lastdisplayed++;
                lastdisplayed %= 64;

                // set goodblock
                goodblock = lastdisplayed;

                if ((lastdisplayed > lastgoodblock_received) | (lastgoodblock_received - lastdisplayed) > (64 - 17)
                        | (lastdisplayed == 0 & thisblock == 0)) {

                    lastgoodblock_received = thisblock;
                }
                // output to main window
                int maxwait = 0;
                while (Main.mainmutex) {
                    try {
                        Thread.sleep(5);
                        maxwait++;
                        if (maxwait > 100) {
                            break;
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (!rxbuffer[lastdisplayed].startsWith("~FA:")) {
                    Main.mainwindow += rxbuffer[lastdisplayed];
                }

                // parse commands
                Lineoutstring += rxbuffer[lastdisplayed];

                int Linebreak = -1;
                while (Lineoutstring.indexOf("\n") >= 0) {
                    Linebreak = Lineoutstring.indexOf("\n");
                    if (Linebreak >= 0) {
                        String fullLine = Lineoutstring.substring(0, Linebreak);
                        Lineoutstring = Lineoutstring.substring(Linebreak + 1);
                        parseInput(fullLine);
                    }
                }
            }

            // make room for more data
            if (lastdisplayed == lastgoodblock_received) {
                int i = 0;
                for (i = lastdisplayed + 8; i < lastdisplayed + 32; i++) {
                    rxbuffer[i % 64] = "";
                }
            }

        }

        return "";
    }

    public void parseInput(String str) throws NoClassDefFoundError, FileNotFoundException, IOException, Exception {
        boolean Firstline = false;
        boolean foundMatchingCommand = false;

        // ~STOP: <transaction> for TTY session...
        //"~STOP:" + Transaction + "\n"
        Pattern STOPm = Pattern.compile("^\\s*~STOP:([A-Za-z0-9]+)?");
        Matcher stopm = STOPm.matcher(str);
        if (Main.ttyConnected.equals("Connected") & stopm.lookingAt()) {
            foundMatchingCommand = true;
            String fileToken = stopm.group(1);
            if (fileToken != null && !fileToken.equals("")) {
                Main.log("Stopping Transaction: " + fileToken);
                //Look in the Outpending folder for partial upload to client. 
                // Files are in the format VK2ETA_-w-_12345*****
                //Delete specified transaction (the one in progress). Could be any type.
                if (!partialFilesDelete("Outpending", Main.ttyCaller, fileToken)) {
                    partialFilesDelete("Outpending", "", fileToken);
                }
            }
            //Clear everything in the current session (as if we had just concluded the connection)
            //VK2ETA, ONLY the TX side as we may also be receiving data from client + review 
            //   logic when we have missing blocks at the time of the issuance of the STOP command as it
            //   leads the client to keep asking for missing blocks over and over
            tx_lastreceived = tx_ok = rx_lastsent;
            //tx_lastsent = " ";
            //tx_lastreceived = tx_lastsent;
            //tx_ok = " ";
            //tx_missing = "";
            //rx_lastsent = " ";
            //rx_ok = " ";
            //rx_lastreceived = rx_lastsent;
            //rx_missing = "";
            Blocklength = 6;
            Main.txText = "";
            for (int i = 0; i < 64; i++) {
                //rxbuffer[i] = "";
                txbuffer[i] = "";
            }
            //Prepare client for next transaction
            Main.txText += "\n-end-\n";
        }
        // ~QUIT for TTY session...
        Pattern TTYm = Pattern.compile("^\\s*~QUIT");
        Matcher tm = TTYm.matcher(str);
        if (Main.ttyConnected.equals("Connected") & tm.lookingAt()) {
            foundMatchingCommand = true;
            Main.disconnect = true;
            Main.log("Disconnect request from " + Main.ttyCaller);
        } else if (tm.lookingAt()) {
            Main.txText = "~QUIT\n";
        }
        // ~LISTFILES for TTY session...
        Pattern LFm = Pattern.compile("^\\s*~LISTFILES");
        Matcher lf = LFm.matcher(str);
        //Open both ways                        if (Main.TTYConnected.equals("Connected") & lf.lookingAt()) {
        if (lf.lookingAt()) {
            foundMatchingCommand = true;
            String downloaddir = Main.homePath + Main.dirPrefix + "Downloads" + Main.separator;
            File dd = new File(downloaddir);
            String[] filelist = dd.list();
            Main.txText += ("Your_files: " + Integer.toString(filelist.length) + "\n");
            for (int i = 0; i < filelist.length; i++) {
                Main.txText += (filelist[i] + "\n");
            }
            Main.txText += "-end-\n";
        }

        // ~QTC? or ~QTC? NN+  for TTY session...
        Pattern MHm = Pattern.compile("^\\s*~QTC\\??\\s*(\\d*)\\+?");
        Matcher mh = MHm.matcher(str);
        //Open both ways                        if (Main.TTYConnected.equals("Connected") & lf.lookingAt()) {
        int fromNumber = 0;
        if (Main.ttyConnected.equals("Connected") & mh.lookingAt()) {
            if (mh.group(1) != null) {
                foundMatchingCommand = true;
                try {
                    fromNumber = Integer.decode(mh.group(1));
                } catch (NumberFormatException e) {
                }
            }
            if (Main.wantServer) {
                Main.txText += ServerMail.getHeaderList(fromNumber);
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }
        
        // ~DELETE NN for TTY session...delete specific message (one at a time)
        Pattern pdc = Pattern.compile("^\\s*~DELETE\\s+(\\d+)\\S*");
        Matcher mdc = pdc.matcher(str);
        int deleteNumber = 0;
        if (Main.ttyConnected.equals("Connected") & mdc.lookingAt()) {
            if (mdc.group(1) != null) {
                foundMatchingCommand = true;
                try {
                    deleteNumber = Integer.decode(mdc.group(1));
                } catch (NumberFormatException e) {
                }
            }
            if (Main.wantServer) {
                Main.txText += ServerMail.deleteMail(deleteNumber);
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        // ~READ NN or ~READZIP NN  for TTY session...Read email number NN
        Pattern RMm = Pattern.compile("^\\s*~READ(ZIP)?\\s+(\\d+)");
        Matcher rm = RMm.matcher(str);
        //Open both ways                        if (Main.TTYConnected.equals("Connected") & lf.lookingAt()) {
        int emailNumber = 0;
        boolean compressed = false;
        if (Main.ttyConnected.equals("Connected") & rm.lookingAt()) {
            if (rm.group(2) != null) {
                foundMatchingCommand = true;
                try {
                    emailNumber = Integer.decode(rm.group(2));
                    compressed = (rm.group(1) != null);
                } catch (NumberFormatException e) {
                    emailNumber = 0;
                }
            }
            if (Main.wantServer) {
                if (emailNumber > 0) {
                    Main.txText += ServerMail.readMail(emailNumber, compressed);
                }
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        // ~TGET url or ~TGETZIP url  for TTY session...
        //e.g.: "~TGET www.bom.gov.au/nsw/forecasts/centralwestslopes.shtml begin:Forecast issued at end:The next routine forecast"
        //e.g.: "~TGET www.bom.gov.au/nsw/forecasts/upperwestern.shtml end:The next routine forecast"
        //e.g.: "~TGET www.bom.gov.au/nsw/forecasts/centralwestslopes.shtml begin:Forecast issued at"       
        Pattern TGm = Pattern.compile("^(\\s*~TGET(ZIP)?)\\s+([^\\s]+)\\s*(.*)");
        Matcher tg = TGm.matcher(str);
        String startStopStr = "";
        if (Main.ttyConnected.equals("Connected") & tg.lookingAt()) {
            if (tg.group(3) != null) {
                foundMatchingCommand = true;
                if (tg.group(4) != null) {
                    startStopStr = tg.group(4).trim();
                }
                Boolean tgetZip = (tg.group(2) != null);
                if (Main.wantServer) {
                    Main.txText += ServerMail.readWebPage(tg.group(3), startStopStr, tgetZip);
                } else {
                    Main.txText += "Sorry, Not enabled\n";
                }
            }
        }

        // Record updated
        Pattern SPC = Pattern.compile("^Updated data");
        Matcher spc = SPC.matcher(str);
        if (spc.lookingAt()) {
            if (hispubkey.length() > 0) {
                String mailpass = Main.configuration.getPreference("POPPASS");
                if (mailpass.length() > 0 & Main.sessionPasswrd.length() > 0) {
                    String intext = Main.cr.encrypt(hispubkey, mailpass + "," + Main.sessionPasswrd);
                    Main.txText += ("~Msp" + intext + "\n");
                    Main.mainwindow += "\n=>>" + intext + "\n";
                } else {
                    Main.mainwindow += "\n=>>" + "No POP password or link password set?\n";
                }
            } else {
                Main.mainwindow += "\n=>>" + "No server public key... reconnect....\n";
            }

        }

        //~GETWWV space weather indexes and forecast...
        Pattern WWVp = Pattern.compile("^(\\s*~GETWWV.*)");
        Matcher WWVm = WWVp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & WWVm.lookingAt()) {
            foundMatchingCommand = true;
            if (Main.wantServer) {
                Main.txText += ServerMail.readWebPage("https://services.swpc.noaa.gov/text/wwv.txt", "", false);
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        //~GETMSG Get APRS messages
        Pattern AMp = Pattern.compile("^(\\s*~GETMSG\\s*(\\d*))");
        Matcher AMm = AMp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & AMm.lookingAt()) {
            foundMatchingCommand = true;
            int maxLines = 10; //Default 10 lines
            if (Main.wantServer) {
                if (AMm.group(2) != null) {
                    try {
                        maxLines = Integer.parseInt(AMm.group(2).trim());
                        if (maxLines < 1) {
                            maxLines = 1;
                        }
                    } catch (Exception e) {
                        maxLines = 10;
                    }
                }
                String cleanCall = Main.cleanCallForAprs(Main.ttyCaller);
                if (cleanCall.length() > 0) {
                    //We have a valid APRS callsign with possibly an APRS Id and all in upper case (e.g.VK2ETA-15)
                    String msgText = ServerMail.readWebPage("http://www.findu.com/cgi-bin/msg.cgi?call=" + cleanCall, "", false);
                    String msgCleanText = "Your messages:\nFrom       Date    Time         Message\n";
                    //"fromtotime&nbsp;message\n" +
                    //"VK2ETA-90  VK2ETA     09/21   07:21:52z Reply to message number 5"
                    Pattern psc = Pattern.compile("(^[a-zA-Z0-9\\-\\/]+)\\s+([a-zA-Z0-9\\-\\/]+)\\s+(\\S+)\\s+(\\S+)\\s+(Reply)\\s+(.*)$", Pattern.MULTILINE);
                    Matcher msc = psc.matcher(msgText);
                    boolean keepLooking = true;
                    String from;
                    String to;
                    String date;
                    String time;
                    String msg;
                    int lines = 0;
                    for (int start = 0; keepLooking;) {
                        keepLooking = msc.find(start);
                        if (keepLooking) {
                            if (lines < maxLines && Main.matchCallOneWithCallTwo(cleanCall, msc.group(2))
                                    && msc.group(6).trim().length() > 0) {
                                //For me, NOT From me, and with a non blank message, max 10 entries
                                to = RMsgUtil.padString(msc.group(2), 9);
                                from = RMsgUtil.padString(msc.group(1), 9);
                                date = msc.group(3);
                                time = RMsgUtil.padString(msc.group(4), 10);
                                msg = msc.group(6);
                                msgCleanText += from + "  " + date + "  " + time + "  " + msg + "\n";
                                lines++;
                            }
                            start = msc.end();
                        }
                    }
                    if (lines > 0) {
                        Main.txText += msgCleanText + "-end-\n";
                    } else {
                        Main.txText += "No APRS Messages\n";
                    }
                } else {
                    Main.txText += "Callsign not valid for APRS\n";
                }
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        //~GETNEAR Get (15) APRS stations near me (using last reported position to APRS)
        Pattern GNp = Pattern.compile("^(\\s*~GETNEAR.*)");
        Matcher GNm = GNp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & GNm.lookingAt()) {
            foundMatchingCommand = true;
            if (Main.wantServer) {
                String cleanCall = Main.cleanCallForAprs(Main.ttyCaller);
                if (cleanCall.length() > 0) {
                    //We have a valid APRS callsign with possibly an APRS Id and all in upper case (e.g.VK2ETA-15)
                    int maxLines = 15;
                    String msgText = ServerMail.readRawWebPage("http://www.findu.com/cgi-bin/near.cgi?last=24&n=15&call=" + cleanCall, "", false);
                    String msgCleanText = "Station     Latitude   Longitude  KMs   Bearing  Age\n";
                    /* Example of useful data lines:
                    <td> <a href="find.cgi?call=VK2XYZ B"><img src="../icon/S66.GIF" border="0"> VK2XYZ B</a> </td>
                    <td> -34.04417</td>
                    <td> 151.12567</td>
                    <td>8.0 </td>
                    <td>SE</td>
                    <td> 00:00:06:04 </td>
                     */
                    Pattern psc = Pattern.compile("^\\s+<td>.*>\\s*(\\S+)\\s*<\\/a>\\s*<\\/td>$|^\\s+<td>\\s*(\\S+)\\s*<\\/td>$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                    Matcher msc = psc.matcher(msgText);
                    boolean keepLooking = true;
                    String station = "";
                    String[] data = new String[5];
                    int lines = 0;
                    int index = -1;
                    for (int start = 0; keepLooking;) {
                        keepLooking = msc.find(start);
                        if (keepLooking) {
                            if (lines < maxLines && msc.group(1) != null) {
                                //We have a new station, prepare data gathering
                                station = RMsgUtil.padString(msc.group(1), 9);
                                index = 0;
                            } else if (lines < maxLines && msc.group(2) != null) {
                                //Store data in the array
                                if (index >= 0 && index <= 4) {
                                    String thisData = msc.group(2); //Debug
                                    data[index++] = thisData;
                                    if (index >= 5) {
                                        //We have a full datas set, add to reply
                                        msgCleanText += station + "  "
                                                + RMsgUtil.padString(data[0], 11)
                                                + RMsgUtil.padString(data[1], 11)
                                                + RMsgUtil.padString(data[2], 7)
                                                + RMsgUtil.padString(data[3], 8)
                                                + RMsgUtil.padString(data[4], 12) + "\n";
                                        index = -1; //Disable further data collection until a new station found
                                        lines++; //Entries counter
                                    }
                                }
                            }
                            start = msc.end();
                        }
                    }
                    if (lines > 0) {
                        Main.txText += msgCleanText + "-end-\n";
                    } else {
                        Main.txText += "No stations nearby\n";
                    }
                } else {
                    Main.txText += "Callsign not valid for APRS\n";
                }
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }
        
        //~GETTIDESTN Get Tide stations near me (using last reported position to APRS)
        Pattern TSp = Pattern.compile("^(\\s*~GETTIDESTN.*)");
        Matcher TSm = TSp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & TSm.lookingAt()) {
            foundMatchingCommand = true;
            if (Main.wantServer) {
                int maxLines = 50;
                String msgText = ServerMail.readRawWebPage("http://www.findu.com/cgi-bin/tidestation.cgi?call=" + Main.ttyCaller, "", false);
                String msgCleanText = "Distance  Number  Latitude Longitude  Name\n";
                /* Example of useful data lines group:
                <td>2365.7</td>
                <td>2684 </td>
                <td><a href="tide.cgi?tide=2684">Satawan Anchorage, Nomoi Islands, F.S.M. </a></td>
                <td> 5.3333 </td>
                <td> 153.733</td>
                */
                Pattern psc = Pattern.compile("^\\s+<td><.*>\\s*(.+)\\s*<\\/a>\\s*<\\/td>$|^\\s+<td>\\s*(\\S+\\s*)<\\/td>$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Matcher msc = psc.matcher(msgText);
                boolean keepLooking = true;
                String station = "";
                String[] data = new String[5];
                int lines = 0;
                int index = 0;
                int matchCount = 0;
                for (int start = 0; keepLooking;) {
                    keepLooking = msc.find(start);
                    if (keepLooking) {
                        if (lines < maxLines && msc.group(1) != null) {
                            //We have a new station
                            station = msc.group(1);
                            index = 2; //Reset index just in case 
                        } else if (lines < maxLines && msc.group(2) != null) {
                            //Store data in the array, skipping the first 4 entries (unused headers)
                            if (matchCount++ > 3 && index >= 0 && index <= 3) {
                                String thisData = msc.group(2); //Debug
                                data[index++] = thisData;
                                if (index >= 4) {
                                    //We have a full datas set, add to reply
                                    msgCleanText += RMsgUtil.padString(data[0], 10)
                                            + RMsgUtil.padString(data[1], 8)
                                            + RMsgUtil.padString(data[2], 9)
                                            + RMsgUtil.padString(data[3], 9)
                                            + station + "\n";
                                    index = 0; //Reset to first data element
                                    lines++; //Entries counter
                                }
                            }
                        }
                        start = msc.end();
                    }
                }
                if (lines > 0) {
                    Main.txText += msgCleanText + "-end-\n";
                } else {
                    Main.txText += "No Tide stations nearby\n";
                }
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        //~GETTIDE Get Tide for a given station
        Pattern TDp = Pattern.compile("\\s*~GETTIDE\\s*(\\d+)");
        Matcher TDm = TDp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & TDm.lookingAt()) {
            foundMatchingCommand = true;
            String station = TDm.group(1);
            if (Main.wantServer) {
                Main.txText += ServerMail.readWebPage("http://www.findu.com/cgi-bin/tide.cgi?tide=" + station, "begin:20 end:&nbsp", false);
            } else {
                Main.txText += "Sorry, Not enabled\n";
            }
        }

        // ~GETBIN for TTY session...
        Pattern GBp = Pattern.compile("^\\s*~GETBIN\\s(\\S+)");
        Matcher GBm = GBp.matcher(str);
        //Open both ways 
        //if (Main.TTYConnected.equals("Connected") & gb.lookingAt()) {
        if (GBm.lookingAt()) {
            foundMatchingCommand = true;
            String downloaddir = Main.homePath + Main.dirPrefix + "Downloads" + Main.separator;

            String codedFile = "";
            String token = "";
            String myfile = GBm.group(1);
            String mypath = downloaddir + GBm.group(1);

            if (mypath.length() > 0) {

                String toCall;
                String fromCall;
                if (Main.ttyConnected.equals("Connected")) {
                    //I am server
                    toCall = Main.ttyCaller;
                    fromCall = Main.callsignAsServer;
                } else {
                    //I am client
                    toCall = myserver;
                    fromCall = Main.mycall;
                }
                //Check that the resulting path is still in the ~/.pskmail/Downloads directory
                // This prevents access to files outside this directory like a "../Scripts/notApublicFile.sh" request.
                if (RMsgUtil.isScriptInSafeDirectory(myfile, "Downloads")) {

                    FileInputStream in = null;

                    File incp = new File(mypath);

                    FileInputStream fis = null;
                    boolean fileFound = true;
                    try {
                        fis = new FileInputStream(incp);
                    } catch (FileNotFoundException ex) {
                        fileFound = false;
                        //Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (fileFound) {
                        File outcp = new File(Main.homePath + Main.dirPrefix + "tmpfile");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(outcp);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        try {
                            byte[] buf = new byte[1024];
                            int i = 0;
                            while ((i = fis.read(buf)) != -1) {
                                fos.write(buf, 0, i);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, e);
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException ex) {
                                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException ex) {
                                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                        String mysourcefile = Main.homePath + Main.dirPrefix + "tmpfile";

                        try {
                            in = new FileInputStream(mysourcefile);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        GZIPOutputStream myzippedfile = null;

                        String tmpfile = Main.homePath + Main.dirPrefix + "tmpfile.gz";

                        try {
                            myzippedfile = new GZIPOutputStream(new FileOutputStream(tmpfile));
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ioe) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ioe);
                        }

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        try {
                            while ((bytesRead = in.read(buffer)) != -1) {
                                myzippedfile.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        try {
                            in.close();
                            myzippedfile.close();
                        } catch (IOException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Random r = new Random();
                        token = Long.toString(Math.abs(r.nextLong()), 12);
                        token = "tmp" + token;

                        //codedFile = Main.homePath + Main.dirPrefix + "Outpending" + Main.separator + token;
                        codedFile = Main.homePath + Main.dirPrefix + "Outpending" + Main.separator
                                + toCall.replaceAll("\\/", "+") + "_-u-_" + token + "_-" + myfile;
                        Base64.encodeFileToFile(tmpfile, codedFile);
                        File dlfile = new File(tmpfile);
                        if (dlfile.exists()) {
                            dlfile.delete();
                        }
                        File mycodedFile = new File(codedFile);
                        //if (mycodedFile.isFile()) {
                        //    TrString = ">FM:" + fromCall + ":" + toCall + ":"
                        //            + token + ":u:" + myfile
                        //            + ":" + Long.toString(mycodedFile.length()) + "\n";
                        //}

                        if (Main.connected) {
                            if (mycodedFile.isFile()) {
                                //Client or server? (File transfers work both ways regarless of who initated the connection)
                                Main.txText += ">FM:" + fromCall + ":" + toCall + ":"
                                        + token + ":u:" + myfile
                                        + ":" + Long.toString(mycodedFile.length()) + "\n";
                                String dataString = RMsgUtil.readFile(codedFile);
                                Main.txText += dataString + "\n-end-\n";
                                //Main.filetype = "u"; //To use when we receive the ~FY command from the client
                            }
                        }
                    } else {
                        Main.txText += "File " + myfile + " Not Found\n";
                    }
                } else {
                    Main.txText += "File " + myfile + " must be a simple file name, no path\n";
                }
            }
        }
        
        
        
        //~RUN scriptname
        //Executes a pre-made script in the .jPskmail/Scripts directory and ONLY in that directory (no path allowed), but arguments allowed
        Pattern RunDp = Pattern.compile("^\\s*~RUN\\s(.+)");
        Matcher RunDm = RunDp.matcher(str);
        if (RunDm.lookingAt()) {
            if (Main.ttyConnected.equals("Connected")) {
                foundMatchingCommand = true;
                String script = RunDm.group(1);
                String[] parts = RunDm.group(1).trim().split("\\s+");                                                                                                               
                  if (Main.wantServer) {                                                                                                                                              
                      executeScript(parts[0], java.util.Arrays.copyOfRange(parts, 1, parts.length));                                                                                  
                } else {
                    Main.txText += "Sorry, Not enabled\n";
                }
            }
        }



        //Not used at present
        // APRS positions 
        //P14,PE1FTV,51.35300,5.38517
        Pattern APRSm = Pattern.compile("(\\w\\d+),(\\S+),(\\-*\\d+\\.\\d+),(\\-*\\d+\\.\\d+)");
        Matcher am = APRSm.matcher(str);
        if (am.lookingAt()) {
            String wptype = am.group(1);
            String Tag = am.group(2);
            String Lat = am.group(3);
            String Lon = am.group(4);
            long epoch = System.currentTimeMillis() / 1000;

            //String outline ="SK0QO-B>SK0QO-BS:!5914.24ND01814.61E&RNG0050 70cm Voice 434.575 -2.00 MHz";
            String aprsString = "";

            aprsString = convert_to_aprsformat(Tag, Lat, Lon, "", wptype);

            Main.mapSock.sendmessage(aprsString);

            int i = 0;
            for (i = 0; i < 20; i++) {

                if (Main.positionsArray[i][0] == null) {
                    Main.positionsArray[i][0] = wptype;
                    Main.positionsArray[i][1] = Tag;
                    Main.positionsArray[i][2] = Lat;
                    Main.positionsArray[i][3] = Lon;
                    Main.positionsArray[i][4] = Long.toString(epoch);
                    break;
                } else if (Main.positionsArray[i][1].equals(Tag)) {
                    Main.positionsArray[i][2] = Lat;
                    Main.positionsArray[i][3] = Lon;
                    Main.positionsArray[i][4] = Long.toString(epoch);
                    break;
                }
                if ((epoch - Long.parseLong(Main.positionsArray[i][4])) / 1000 > 180) {
                    Main.positionsArray[i][0] = null;
                    Main.positionsArray[i][1] = null;
                    Main.positionsArray[i][2] = null;
                    Main.positionsArray[i][3] = null;
                    Main.positionsArray[i][4] = null;
                }
                if (Main.positionsArray[i][0] != null) {

                }
            }
        }

        //Receiving mail headers 
        Pattern pm = Pattern.compile("^\\s*(Your\\smail:)\\s(\\d*)");
        Matcher mm = pm.matcher(str);
        if (mm.lookingAt()) {
            if (mm.group(1).equals("Your mail:")) {
                Headers = true;
                Firstline = true;
                DataSize = Integer.parseInt(mm.group(2));
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                try {
                    this.headers = new FileWriter(Main.homePath + Main.dirPrefix + "headers", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the headers file.", e, false);
                }
            }
        }

        // IAC fleetcodes
        /*
                                    if (!Main.Connected){
                                             Pattern pc = Pattern.compile(".*<SOH>(ZFZF)");
                                            Matcher mc = pc.matcher(str);
                                             if (mc.lookingAt()) {
                                                    if (mc.group(1).equals("ZFZF")){   
                                                    Main.IACmode = true;
                                                    Firstline = true;
                                                    try {                                            
                                                        iacout = new BufferedWriter(new FileWriter(Main.HomePath + Main.Dirprefix + "iactemp", true));
                                                        Main.q.Message("Receiving IAC fleetcode file", 15);
                                                    } 
                                                    catch (Exception e){
                                                        Main.log.writelog("Error when trying to open the iac file.", e, true);
                                                        Main.q.Message("Error opening file...", 15);
                                                    }
                                                }
                                             }
                                    }                                    
         */
        
        //Receiving file list
        Pattern pl = Pattern.compile("^\\s*(Your_files:)\\s(\\d+)");
        Matcher ml = pl.matcher(str);
        if (ml.lookingAt()) {
            if (ml.group(1).equals("Your_files:")) {
                FileList = true;
                Firstline = true;
                // set progress indicator here...
                ThisFileLength = ml.group(2);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
            }
        }

        //send mail (~SEND)
        Pattern SMp = Pattern.compile("^\\s*(~SEND)$");
        Matcher smm = SMp.matcher(str);
        if (Main.ttyConnected.equals("Connected") & smm.lookingAt()) {
            foundMatchingCommand = true;
            if (smm.group(1).equals("~SEND")) {
                emailUpload = true;
                Main.comp = false;
                Firstline = true;
                try {
                    File tmp = new File(Main.homePath + Main.dirPrefix + "tempEmail");
                    tmp.delete();
                    this.tempEmailFile = new FileWriter(new File(Main.homePath + Main.dirPrefix + "tempEmail"), true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the mail file.", e, false);
                }
            }
        }

        // file download
        Pattern pf = Pattern.compile("^\\s*(Your\\sfile:)(.*)\\s(\\d+)");
        Matcher mf = pf.matcher(str);
        if (mf.lookingAt()) {
            if (mf.group(1).equals("Your file:")) {
                FileDownload = true;
                Main.comp = true;
                Firstline = true;
                ThisFile = mf.group(2);
                // set progress indicator here...
                ThisFileLength = mf.group(3);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                try {
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    tmp.delete();
                    this.dlFile = new FileWriter(new File(Main.homePath + Main.dirPrefix + "TempFile"), true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the download file.", e, false);
                }
            }
        }
        // >FM:PI4TUE:PA0R:Jynhgf:f:test.txt:496
        // >FM:fk8/vk2eta/pm:VK2ETA-1:tmp578bbbaa04091a76::changes.txt:506
        Pattern fm = Pattern.compile("\\s*>FM:(\\S+):(\\S+):(\\S+):(\\w):(.*):(\\d+)");
        Matcher fmm = fm.matcher(str);
        if (fmm.lookingAt()) {
            foundMatchingCommand = true;
            if (fmm.group(4).equals("f")) {
                FileDownload = true;
                Main.comp = true;
                Firstline = true;
                ThisFile = fmm.group(5);
                Transaction = fmm.group(3);
                // set progress indicator here...
                ThisFileLength = fmm.group(6);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                try {
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    tmp.delete();
                    // open filewriter for TempFile
                    this.dlFile = new FileWriter(new File(Main.homePath + Main.dirPrefix + "TempFile"), true);
                    // copy pending file into temp
                    File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);

                    if (pending.exists()) {
                        DataReceived = (int) pending.length();
                        FileReader in = new FileReader(pending);
                        int c;
                        // copy the pending file
                        while ((c = in.read()) != -1) {
                            dlFile.write(c);
                        }
                        // close pending file
                        in.close();
                    }
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the download file.", e, false);
                }
                try {
                    Trfile = new File(Main.pendingStr + Transaction);
                    // open pending file (now called pFile) for write
                    this.pFile = new FileWriter(Trfile, true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the pending file.", e, false);
                }
            } else if (fmm.group(4).equals("u")) {
                FileDownload = true;
                Main.comp = true;
                Firstline = true;
                ThisFile = fmm.group(5) + ".gz";
                Transaction = fmm.group(3);
                // set progress indicator here...
                ThisFileLength = fmm.group(6);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                Main.log("Receiving file " + ThisFile + " from " + Main.ttyCaller);
                try {
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    tmp.delete();
                    // open filewriter for TempFile
                    this.dlFile = new FileWriter(new File(Main.homePath + Main.dirPrefix + "TempFile"), true);
                    // copy pending file into temp
                    File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);

                    if (pending.exists()) {
                        DataReceived = (int) pending.length();
                        FileReader in = new FileReader(pending);
                        int c;
                        // copy the pending file
                        while ((c = in.read()) != -1) {
                            dlFile.write(c);
                        }
                        // close pending file
                        in.close();
                    }
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the download file.", e, false);
                }
                try {
                    Trfile = new File(Main.pendingStr + Transaction);
                    // open pending file (now called pFile) for write
                    this.pFile = new FileWriter(Trfile, true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the pending file.", e, false);
                }
            // compressed web page open...
            } else if (fmm.group(4).equals("w")) {
                //Compressed web page
                CwwwDownload = true;
                Main.comp = true;
                Firstline = true;
                ThisFile = fmm.group(5);
                Transaction = fmm.group(3);
                // set progress indicator here...
                ThisFileLength = fmm.group(6);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                try {
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    tmp.delete();
                    // open filewriter for TempFile
                    this.dlFile = new FileWriter(new File(Main.homePath + Main.dirPrefix + "TempFile"), true);
                    // open File for pending
                    File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);

                    if (pending.exists()) {
                        DataReceived = (int) pending.length();
                        FileReader in = new FileReader(pending);
                        int c;
                        // copy the pending file
                        while ((c = in.read()) != -1) {
                            dlFile.write(c);
                        }
                        // close pending file
                        in.close();
//                                                    pending.delete();
                    }
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the download file.", e, false);
                }
                try {
                    Trfile = new File(Main.pendingStr + Transaction);
                    this.pFile = new FileWriter(Trfile, true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the pending file.", e, false);
                }

                // compressed mail download
            } else if (fmm.group(4).equals("m")) {
                //Mail download
                CMsgDownload = true;
                Transaction = fmm.group(3);
                Main.comp = true;
                Firstline = true;
                try {
                    File F1 = new File(Main.homePath + Main.dirPrefix + "tmpmessage");  // make sure it is empty
                    if (F1.exists()) {
                        F1.delete();
                    }
                    File F2 = new File(Main.homePath + Main.dirPrefix + "tmpmessage.gz");  // make sure it is empty
                    if (F2.exists()) {
                        F2.delete();
                    }
                    tmpmessage = new FileWriter(Main.homePath + Main.dirPrefix + "tmpmessage", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the headers file.", e, false);
                }
                // set progress indicator here...
                ThisFileLength = fmm.group(6);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                if (pending.exists()) {
                    DataReceived = (int) pending.length();
                    FileReader in = new FileReader(pending);
                    int c;
                    // copy the pending file
                    while ((c = in.read()) != -1) {
                        tmpmessage.write(c);
                    }
                    // close pending file
                    in.close();
                }

                try {
                    Trfile = new File(Main.pendingStr + Transaction);
                    pFile = new FileWriter(Trfile, true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the pending file.", e, false);
                }
            // compressed mail upload (I am a server)
            } else if (fmm.group(4).equals("s")) {
                CompressedEmailUpload = true;
                Transaction = fmm.group(3);
                Main.comp = true;
                Firstline = true;
                try {
                    File F1 = new File(Main.homePath + Main.dirPrefix + "tmpmessage");  // make sure it is empty
                    if (F1.exists()) {
                        F1.delete();
                    }
                    File F2 = new File(Main.homePath + Main.dirPrefix + "tmpmessage.gz");  // make sure it is empty
                    if (F2.exists()) {
                        F2.delete();
                    }
                    tmpmessage = new FileWriter(Main.homePath + Main.dirPrefix + "tmpmessage", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to upload email (in TTYServer mode)", e, false);
                }
                // set progress indicator here...
                ThisFileLength = fmm.group(6);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
                File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                if (pending.exists()) {
                    DataReceived = (int) pending.length();
                    FileReader in = new FileReader(pending);
                    int c;
                    // copy the pending file
                    while ((c = in.read()) != -1) {
                        tmpmessage.write(c);
                    }
                    // close pending file
                    in.close();
                }
                try {
                    Trfile = new File(Main.pendingStr + Transaction);
                    pFile = new FileWriter(Trfile, true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the pending file.", e, false);
                }
            }
        }

        //Partial Downloads (file, web page or imap email) from server
        // >FO5:PI4TUE:PA0R:JhyJkk:f:test.txt:496
        Pattern ofr = Pattern.compile("\\s*>FO(\\d):([a-zA-Z0-9\\-\\/]+):([a-zA-Z0-9\\-\\/]+):([A-Za-z0-9_-]+):(\\w)");
        Matcher ofrm = ofr.matcher(str);
        if (ofrm.lookingAt()) {
            foundMatchingCommand = true;
            if (ofrm.group(5).equals("f") | ofrm.group(5).equals("w") | ofrm.group(5).equals("m")) {
                // get the file ?
                File pending;
                pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + ofrm.group(4));
                long x = 0;
                if (pending.exists()) {
                    x = pending.length();
                }
                if (Main.mainui.jRadioButtonAccept.isSelected()) {
                    Main.txText += "~FY:" + ofrm.group(4) + ":" + Long.toString(x) + "\n";
                } else if (Main.mainui.jRadioButtonReject.isSelected()) {
                    Main.txText += "~FN:" + ofrm.group(4) + "\n";
                } else {
                    Main.txText += "~FA:" + ofrm.group(4) + "\n";
                    if (pending.exists()) {
                        pending.delete();
                    }
                }
            }
        }

        //Partial uploads
        //~FO5:PI4TUE:PA0R:tmpasdkkdfj:u:test.txt:36
        //~FO5:VK2ETA-5:VK2ETA-1:tmp578bbbaa04091a76:u:changes.txt:506
        //~FO5:PI4TUE:PA0R-1:a30a69:s: :847 //E-mail upload to this TTYserver
        Pattern ofr2 = Pattern.compile("\\s*~FO(\\d):([a-zA-Z0-9\\-\\/]+):([a-zA-Z0-9\\-\\/]+):([A-Za-z0-9_-]+):(\\w):(.*):(\\d+)");
        Matcher ofrm2 = ofr2.matcher(str);
        if (ofrm2.lookingAt()) {
            foundMatchingCommand = true;
            if (ofrm2.group(5).equals("u")) { //Partial file upload to this server
                // get the file ?
                File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + ofrm2.group(4));
                long x = 0;
                if (pending.exists()) {
                    x = pending.length();
                }
                if (Main.mainui.jRadioButtonAccept.isSelected()) {
                    Main.txText += "~FY:" + ofrm2.group(4) + ":" + Long.toString(x) + "\n";
                } else if (Main.mainui.jRadioButtonReject.isSelected()) {
                    Main.txText += "~FN:" + ofrm2.group(4) + "\n";
                } else {
                    Main.txText += "~FA:" + ofrm2.group(4) + "\n";
                    if (pending.exists()) {
                        pending.delete();
                    }
                }
            } else if (ofrm2.group(5).equals("s")) { //E-Mail upload to this TTYserver for sending
                // get the file ?
                File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + ofrm2.group(4));
                long x = 0;
                if (pending.exists()) {
                    x = pending.length();
                }
                //Always accept client partial upload
                Main.txText += "~FY:" + ofrm2.group(4) + ":" + Long.toString(x) + "\n";
            }
        }


        // ~FY:tmp578bbbaa04091a76:234    //partial email or file upload
        Pattern yfr = Pattern.compile("\\s*~FY:([A-Za-z0-9]+):(\\d+)");
        Matcher yfrm = yfr.matcher(str);
        if (yfrm.lookingAt()) {
            String toCall = "";
            String fromCall = "";
            String result = "";
            File[] pendingFilesList;
            foundMatchingCommand = true;
            String partialfile = yfrm.group(1);
            String startingbyte = yfrm.group(2);
            //System.out.println(partialfile);
            int start = Integer.parseInt(startingbyte);
            //File penf = new File(Main.outPendingDir + partialfile);
            //File foutpending = new File(Main.outPendingDir + Main.separator + partialfile);
            //ile foutpending;
            if (Main.ttyConnected.equals("Connected")) {
                //I am server
                toCall = Main.ttyCaller;
                fromCall = Main.callsignAsServer;
                //foutpending = new File(Main.outPendingDir + Main.separator + Main.ttyCaller.replaceAll("\\/", "+") + "_-u-_" + partialfile);
            } else {
                //I am client
                toCall = myserver;
                fromCall = Main.mycall;
                //foutpending = new File(Main.outPendingDir + Main.separator + Main.sm.myserver.replaceAll("\\/", "+") + "_-u-_" + partialfile);            
            }
            //vk2eta+pm_-x-_tmp2b4662a0b27610b45a_-myfile.txt //for partial file uploads
            //tmp2b4662a0b27610b45a //for partial email uploads 
            //Pattern yfm = Pattern.compile("([a-zA-Z0-9\\+\\-]+)_-(\\w?)-_([a-zA-Z0-9]+)(_-(.+))?");
            Pattern yfm = Pattern.compile("(([a-zA-Z0-9\\+\\-]+)_-(\\w?)-_)?([a-zA-Z0-9]+)(_-(.+))?");
            Matcher yfmm;
            File dir = new File(Main.outPendingDir);
            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }
            };
            //Generates an array of strings containing the file names
            pendingFilesList = dir.listFiles(fileFilter);
            for (int i = 0; i < pendingFilesList.length; i++) {
                String pendingCaller = "";
                String pendingType = "";
                String pendingToken = "";
                String pendingFileName = "";
                String pendingFn = pendingFilesList[i].getName();
                yfmm = yfm.matcher(pendingFn);
                if (yfmm.lookingAt()) {
                    if (yfmm.group(1) != null) {
                        pendingCaller = yfmm.group(2).replaceAll("\\+", "/");
                        pendingType = yfmm.group(3);
                        pendingToken = yfmm.group(4);
                        if (yfmm.group(6) != null) {
                            pendingFileName = yfmm.group(6);
                        }
                    } else if (yfmm.group(4) != null) {
                        pendingToken = yfmm.group(4);
                        pendingType = "m";
                    }
                    if ((pendingCaller.length() == 0 || pendingCaller.equals(toCall)) 
                            && pendingToken.equals(partialfile)) {
                        //Found a match for callsign, add to list
                        long flen = pendingFilesList[i].length() - start;
                        String restOfFile = RMsgUtil.readFile(pendingFilesList[i].getAbsolutePath());
                        restOfFile = restOfFile.substring(start);
                        Main.txText += ">FM:" + fromCall + ":" + toCall + ":"
                                + pendingToken + ":" + pendingType + ":" + pendingFileName
                                + ":" + Long.toString(flen) + "\n" +
                                restOfFile + "\n-end-\n";
                    }
                }
            }
            /*
            String filename = "";
            if (foutpending.exists()) {
                int i = 0;
                try {
                    FileInputStream fis = new FileInputStream(foutpending);
                    char current;
                    String callsign = Main.configuration.getPreference("CALL");
                    callsign = callsign.trim();
                    //String servercall = Main.configuration.getPreference("SERVER");
                    String servercall = Main.q.getServer().trim();
                    File ft = new File(Main.transactions);
                    String[] ss = null;
                    if (ft.exists()) {
                        //System.out.println("Transactons exists");
                        FileReader fr = new FileReader(Main.transactions);
                        BufferedReader br = new BufferedReader(fr);
                        String s;
                        while ((s = br.readLine()) != null) {
                            //System.out.println("s=:" + s);
                            ss = s.split(":");
                            //System.out.println(ss[5]);
                            if (s.contains(partialfile)) {
                                //System.out.println(s);
                                filename = ss[5];
                            }
                        }
                        fr.close();
                    }
                    long flen = 0;
                    flen = foutpending.length() - start;
                    Main.txText += ">FM:" + callsign + ":" + servercall + ":" + partialfile + ":" + Main.filetype + ":" + filename + ":" + Long.toString(flen) + "\n";
                    while (fis.available() > 0) {
                        current = (char) fis.read();
                        i++;
                        if (i > start) {
                            Main.txText += current;
                        }
                    }
                    fis.close();
                    Main.txText += "\n-end-\n";
                    Session.DataSize = Integer.parseInt(ss[6]);
                } catch (IOException e) {
                    System.out.println("IO error on pending file");
                }
            }
            */
            Main.log("Resuming Partial dataset: " + partialfile);
        }

        
        // ~FA:tmpjGUytg  
        // ~FA:tmp16a38514418197b776 
        //Delete output file in Outbox OR in Outpending
        Pattern afr = Pattern.compile("\\s*~FA:([A-Za-z0-9]+)");
        Matcher afrm = afr.matcher(str);
        if (afrm.lookingAt()) {
            foundMatchingCommand = true;
            String deleteToken = afrm.group(1);
            str = "";
            //Are we a server or client?
            if (Main.ttyConnected.equals("Connected")) {
                //Look in the Outpending folder for partial upload to client. Files are in the format 
                //VK2ETA_-w-_12345_-_afile.txt OR tmp123456789 for email uploads
                //Since we don't know the file type from the ~FY command (s,w,f etc...),
                //  we scan the directory for a match
                String caller = Main.ttyCaller;
                //Outpending folder first
                if (partialFilesDelete("Outpending", caller, deleteToken)) {
                    Main.mainwindow += (Main.myTime() + " File Sent...\n");
                    Main.filesTextArea += " File Sent...\n";
                }
            } else { //I am a client, either uploading a file or an email
                //Check for email uploads first
                if (partialFilesDelete("Outpending", "", deleteToken)) {
                    //Found an email upload, move from outbox to sent folder
                    File df = new File(Main.homePath + Main.dirPrefix + "Outbox" + Main.separator + deleteToken);
                    movefiletodir(df, Main.homePath + Main.dirPrefix + "Sent");
                    try {
                        df.delete();
                    } catch (Exception e) {
                        //Nothing
                    }
                    Main.mainwindow += (Main.myTime() + " Email Sent...\n");
                    Main.filesTextArea += " Email Sent...\n";
                } else if (partialFilesDelete("Outpending", myserver, deleteToken)) {
                    //Found a file upload
                    Main.q.Message("Upload complete...", 10);
                    Main.filesTextArea += "Upload complete...\n";
                }
                // reset progress bar
                Session.DataSize = 0;
                Session.DataReceived = 0;
                Main.progress = 0;
                Main.q.Message("Upload complete...", 10);
                str = "";
                //Update grids on UI thread in case we send some emails
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Main.mainui.refreshEmailGrid();
                    }
                });
            }
        }

        // message receive
        Pattern pmsg = Pattern.compile("^\\s*(Your\\smsg:)\\s(\\d+)");
        Matcher mmsg = pmsg.matcher(str);
        if (mmsg.lookingAt()) {
            foundMatchingCommand = true;
            if (mmsg.group(1).equals("Your msg:")) {
                MsgDownload = true;
                Firstline = true;
                try {
                    this.tmpmessage = new FileWriter(Main.homePath + Main.dirPrefix + "tmpmessage", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the headers file.", e, false);
                }
                // set progress indicator here...
                ThisFileLength = mmsg.group(2);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
            }
        }
        
        // compresssed message receive
        Pattern cpmsg = Pattern.compile("^\\s*(~ZIPPED64)\\s(\\d+)");
        Matcher cmmsg = cpmsg.matcher(str);
        if (cmmsg.lookingAt()) {
            foundMatchingCommand = true;
            if (cmmsg.group(1).equals("~ZIPPED64")) {
                CMsgDownload = true;
                Main.comp = true;
                Firstline = true;
                try {
                    File F1 = new File(Main.homePath + Main.dirPrefix + "tmpmessage");  // make sure it is empty
                    F1.delete();
                    File F2 = new File(Main.homePath + Main.dirPrefix + "tmpmessage.gz");  // make sure it is empty
                    F2.delete();
                    this.tmpmessage = new FileWriter(Main.homePath + Main.dirPrefix + "tmpmessage", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the headers file.", e, false);
                }
                // set progress indicator here...
                ThisFileLength = cmmsg.group(2);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
            }
        }

        // web page receive
        Pattern pw = Pattern.compile("^\\s*(Your\\swwwpage:)\\s(\\d+)");
        Matcher mw = pw.matcher(str);
        if (mw.lookingAt()) {
            foundMatchingCommand = true;
            if (mw.group(1).equals("Your wwwpage:")) {
                WWWDownload = true;
                Firstline = true;
                // set progress indicator here...
                ThisFileLength = mw.group(2);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
            }
        }

        //compressed  website Send
        Pattern tgmsg = Pattern.compile("^\\s*(~TGET64)\\s(\\d+)");
        Matcher tgmmsg = tgmsg.matcher(str);
        if (tgmmsg.lookingAt()) {
            foundMatchingCommand = true;
            if (tgmmsg.group(1).equals("~TGET64")) {
                CwwwDownload = true;
                Main.comp = true;
                Firstline = true;
                try {
                    File F1 = new File(Main.homePath + Main.dirPrefix + "tmpmessage");  // make sure it is empty
                    if (F1.exists()) {
                        F1.delete();
                    }
                    File F2 = new File(Main.homePath + Main.dirPrefix + "tmpmessage.gz");  // make sure it is empty
                    if (F2.exists()) {
                        F2.delete();
                    }
                    this.tmpmessage = new FileWriter(Main.homePath + Main.dirPrefix + "tmpmessage", true);
                } catch (Exception e) {
                    Main.log.writelog("Error when trying to open the temp file.", e, false);
                }
                // set progress indicator here...
                ThisFileLength = tgmmsg.group(2);
                DataSize = Integer.parseInt(ThisFileLength);
                DataReceived = 0;
                Main.dataSize = Integer.toString(DataSize);
            }
        }

        // Message sent...
        Pattern ps = Pattern.compile("^\\s*(Message sent\\.\\.\\.)");
        Matcher ms = ps.matcher(str);
        if (ms.lookingAt()) {
            foundMatchingCommand = true;
            if (ms.group(1).equals("Message sent...")) {
                try {
                    File fd = new File(Main.mailOutFile);
                    // Move to Sent or delete
                    if (!movefiletodir(fd, Main.homePath + Main.dirPrefix + "Sent")) {
                        fd.delete();
                    }
                } catch (Exception e) {
                    Main.log.writelog("Error moving sent mail file.", e, false);
                }
            }
        }

        //Uncompressed E-mail? Look for the single . as mark of end of message
        if (emailUpload) {
            Pattern SMpe = Pattern.compile("^(\\.)$");
            Matcher SMme = SMpe.matcher(str);
            if (SMme.lookingAt()) {
                foundMatchingCommand = true;
                String to = "";
                String subject = "";
                String body = "";
                String from = "";
                if (SMme.group(1).equals(".")) {
                    emailUpload = false;
                    DataReceived = 0;
                    Main.dataSize = Integer.toString(0);
                    try {
                        this.tempEmailFile.close();
                        //Now read and analyse the file
                        FileReader fr = new FileReader(Main.homePath + Main.dirPrefix + "tempEmail");
                        BufferedReader br = new BufferedReader(fr);
                        String mLine;
                        while ((mLine = br.readLine()) != null) {
                            if (mLine.startsWith("To:")) {
                                to = mLine.substring(3);
                            } else if (mLine.startsWith("Subject:")) {
                                subject = mLine.substring(8);
                            } else if (mLine.startsWith("From:")) {
                                from = mLine.substring(5);
                            } else {
                                //Body
                                body += mLine + "\n";
                            }
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to handle the email upload file.", ex, false);
                    }
                    //Make sure we have something to send
                    if (!to.equals("") & (!subject.equals("") | !body.equals(""))) {
                        //No attachment yet
                        //Main.txText += ServerMail.sendMail(from, to, subject, body, ""); //last param is attachementFileName
                        //use call sign as originator since all emails emanate from one mail box
                        String cleanCall = Main.cleanCallForAprs(Main.ttyCaller);
                        subject = "Radio Mail from " + cleanCall + ": " + subject;
                        Main.txText += ServerMail.sendMail(cleanCall, to, subject, body, ""); //last param is attachementFileName
                    }
                }
            } else {
                //Just store the data
                if (!Firstline) {
                    this.tempEmailFile.write(str + "\n");
                } else {
                    //Reset flag
                    Firstline = false;
                }
            }
        }

        // -end- command
        Pattern pe = Pattern.compile("^\\s*(\\S+)");
        Matcher me = pe.matcher(str);
        if (me.lookingAt()) {
            if (me.group(1).equals("-end-")) {
                foundMatchingCommand = true;
                if (Headers) {
                    Headers = false;
                    DataReceived = 0;
                    Main.dataSize = Integer.toString(0);
                    try {
                        this.headers.close();
                        //Must run on UI thread otherwise we get index out of bound errors
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                Main.mainui.refreshEmailGrid();
                            }
                        });
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the headers file.", ex, false);
                    }
                }
                if (FileList) {
                    FileList = false;
                }
                if (FileDownload) {
                    FileDownload = false;
                    Main.comp = false;

                    try {
                        this.dlFile.close();
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the download file.", ex, false);
                    }
                    try {
                        Base64.decodeFileToFile(Main.homePath + Main.dirPrefix + "TempFile", Main.homePath + Main.dirPrefix + "Downloads" + Main.separator + ThisFile);

                        Unzip.Unzip(Main.homePath + Main.dirPrefix + "Downloads" + Main.separator + ThisFile);

                        File tmp = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                        if (tmp.exists()) {
                            tmp.delete();
                        }
                        Main.txText += "~FA:" + Transaction + "\n";
                        Main.progress = 0;
                    } catch (IOException e) {
                    } catch (Exception exc) {
                        Main.log.writelog("Error when trying to decode the downoad file.", exc, false);
                    } catch (NoClassDefFoundError exp) {
                        Main.q.Message("problem decoding B64 file", 10);
                    }
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    boolean success = tmp.delete();

                    try {
                        if (pFile != null) {
                            pFile.close();
                            Trfile.delete();
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the pending file.", ex, false);
                    }
                    Main.progress = 0;

                    Main.log(ThisFile.replace(".gz", "") + " received");

                }
                // messages  download      - append tmpmessage to Inbox in mbox format                          
                if (MsgDownload) {
                    MsgDownload = false;
                    this.tmpmessage.close();
                    // append to Inbox file
                    FileReader fr = new FileReader(Main.homePath + Main.dirPrefix + "tmpmessage");
                    BufferedReader br = new BufferedReader(fr);
                    // all local stuff
                    String s;
                    String From = null;
                    boolean haveFrom = false;
                    String Date = null;
                    boolean haveDate = false;
                    String Sub = null;
                    boolean haveSub = false;
                    String outstr = "";
                    String attachment = "";
                    base64attachment = false;
                    plaintextattachment = false;
                    // read tmpmessage line by line
                    while ((s = br.readLine()) != null) {
                        // compile some patterns and set up the matchers
                        Pattern pfrm = Pattern.compile("^\\s*(From:)\\s(.*)");
                        Matcher mfrm = pfrm.matcher(s);
                        Pattern pdate = Pattern.compile("^\\s*(Date:)\\s(\\w{3})\\,*\\s+(\\d+)\\s(\\w{3})\\s(\\d{4})\\s(\\d\\d:\\d\\d:\\d\\d)");
                        Matcher mdate = pdate.matcher(s);
                        Pattern pdate2 = Pattern.compile("^\\s*(Date:)\\s(\\d+)\\s+(\\w{3})\\s+(\\d{4}\\s\\d\\d:\\d\\d:\\d\\d)");
                        Matcher mdate2 = pdate2.matcher(s);
                        Pattern psub = Pattern.compile("^\\s*(Subject:)\\s(.*)");
                        Matcher msub = psub.matcher(s);
                        Pattern p64 = Pattern.compile("^\\s*(content-transfer-encoding: base64)");
                        Matcher m64 = p64.matcher(s.toLowerCase());
                        Pattern pnm = Pattern.compile(".*(filename=)(.*)");
                        Matcher mnm = pnm.matcher(s);
                        if (mfrm.lookingAt()) {
                            if (mfrm.group(1).equals("From:") && !haveFrom) {
                                From = mfrm.group(2);
                                haveFrom = true;
                            }
                        } else if (mdate.lookingAt()) {
                            if (mdate.group(1).equals("Date:") && !haveDate) {
                                Date = mdate.group(2) + " " + mdate.group(3) + " "
                                        + mdate.group(4) + " " + mdate.group(5) + " "
                                        + mdate.group(6);
                                haveDate = true;
                            }

                        } else if (mdate2.lookingAt()) {
                            if (mdate2.group(1).equals("Date:") && !haveDate) {
                                Date = mdate2.group(2) + " " + mdate2.group(3) + " "
                                        + mdate2.group(4);
                                haveDate = true;
                            }

                        } else if (msub.lookingAt()) {
                            if (msub.group(1).equals("Subject:") && !haveSub) {
                                Sub = msub.group(2);
                                haveSub = true;
                            }

                        } else if (m64.lookingAt()) {
                            if (m64.group(1).toLowerCase().contains("content-transfer-encoding: base64")) {
                                // there is an attachment...
                                base64attachment = true;
//                                                                debug ("Attachment");
                            }
                        } else if (mnm.lookingAt()) {
                            if (mnm.group(1).contains("filename=")) {
                                // get the file name
                                attachmentFilename = mnm.group(2);
                                attachmentFilename = attachmentFilename.substring(1, attachmentFilename.length() - 1);

//                                                             debug (attachmentFilename);
                            }
                        } else {
                            if (base64attachment) {
                                if (!s.equals("")) {
                                    if (!s.startsWith("--")) {
                                        attachment += s + "\n";
                                    } else {
                                        base64attachment = false;
                                        if (attachment.length() > 10 & attachmentFilename != null) {
                                            // Never mind if its a grib file
                                            attachmentFilename = "Files" + Main.separator + attachmentFilename;
                                            attachmentFilename = Main.homePath + Main.dirPrefix + attachmentFilename;
                                            SaveAttachmentToFile(attachmentFilename, attachment, true);                                                                            
                                        }
                                    }
                                }
                            }
                        }
                        // body text for Inbox
                        outstr += s + "\n";
                        //                                                 debug ("Out=" + s);
                    } // end while

                    fr.close();
                    this.inbox = new FileWriter(Main.homePath + Main.dirPrefix + "Inbox", true);
                    inbox.write("From " + From + " " + Date + "\n");
                    inbox.write("From: " + From + "\n");
                    if (Date != null) {
                        inbox.write("Date: " + Date + "\n");
                    }
                    inbox.write("Subject: " + Sub + "\n");
                    // write message body
                    if (outstr != null) {
                        inbox.write(outstr + "\n");
                    }
                    inbox.flush();
                    inbox.close();
                    //Must run on UI thread otherwise we get index out of bound errors
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            Main.mainui.refreshEmailGrid();
                        }
                    });
                    File fl = new File(Main.homePath + Main.dirPrefix + "tmpmessage");
                    if (fl.exists()) {
                        fl.delete();
                    }

                    Main.q.Message("Added to mbox queue", 10);
                }
                
                // compressed E-mail upload while I am a TTYServer
                if (CompressedEmailUpload) {
                    CompressedEmailUpload = false;
                    Main.comp = false;
                    tmpmessage.close();
                    // decode base 64 and unzip...
                    String cin = Main.homePath + Main.dirPrefix + "tmpmessage";
                    String cout = Main.homePath + Main.dirPrefix + "tmpmessage.gz";
                    String cmid = "";
                    try {
                        Base64.decodeFileToFile(cin, cout);
                        cmid = Unzip.Unzip(Main.homePath + Main.dirPrefix + "tmpmessage.gz");
                    } catch (Exception e) {
                        Main.q.Message("Decoding error!", 10);
                    }
                    //Analyse and Send email
                    String to = "";
                    String from = "";
                    String subject = "";
                    String body = "";
                    try {
                        //Now read and analyse the file
                        FileReader fr = new FileReader(Main.homePath + Main.dirPrefix + "tmpmessage");
                        BufferedReader br = new BufferedReader(fr);
                        String mLine;
                        while ((mLine = br.readLine()) != null) {
                            if (mLine.startsWith("To:")) {
                                to = RMsgMisc.ltrim(mLine.substring(3));
                            } else if (mLine.startsWith("Subject:")) {
                                subject = RMsgMisc.ltrim(mLine.substring(8));
                            } else if (mLine.startsWith("From:")) {
                                from = RMsgMisc.ltrim(mLine.substring(5));
                            } else if (!mLine.equals(".") && !mLine.startsWith("~SEND")){
                                //Body
                                body += mLine + "\n";
                            }
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to handle the email upload file.", ex, false);
                    }
                    //Make sure we have something to send
                    if (!to.equals("") && (!subject.equals("") | !body.equals(""))) {
                        //No attachment yet
                        String cleanCall = Main.cleanCallForAprs(Main.ttyCaller);
                        subject = "Radio Mail from " + cleanCall + ": " + subject;
                        String resultStr = ServerMail.sendMail(cleanCall, to, subject, body, ""); //last param is attachementFileName
                        //Format reply depending on sending method (~SEND or compressed & restartable)
                        if (resultStr.contains("Message sent..")) {
                            if (Main.protocol > 0) {
                                Main.txText += "~FA:" + Transaction + "\n";
                            } else {
                                Main.txText += resultStr;
                            }
                            Main.q.Message("Email Sent...", 10);
                        } else { //Must be an error
                            Main.txText += resultStr;
                        }
                    }
                    //Pending File Close                    
                    try {
                        if (pFile != null) {
                            pFile.close();
                            Trfile.delete();
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the pending file.", ex, false);
                    }
                    //VK2ETA: in any case delete the transaction file
                    File tmp = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                    if (tmp.exists()) {
                        tmp.delete();
                    }
                    Main.progress = 0;
                }

                // compressed email messages  download - append tmpmessage to Inbox in mbox format                          
                if (CMsgDownload) {
                    CMsgDownload = false;
                    Main.comp = false;
                    tmpmessage.close();
                    // decode base 64 and unzip...
                    String cin = Main.homePath + Main.dirPrefix + "tmpmessage";
                    String cout = Main.homePath + Main.dirPrefix + "tmpmessage.gz";
                    String cmid = "";
                    try {
                        Base64.decodeFileToFile(cin, cout);
                        cmid = Unzip.Unzip(Main.homePath + Main.dirPrefix + "tmpmessage.gz");
                    } catch (Exception e) {
                        Main.q.Message("Decoding error!", 10);
                    }
                    // append to Inbox file
                    FileReader fr = new FileReader(Main.homePath + Main.dirPrefix + "tmpmessage");
                    BufferedReader br = new BufferedReader(fr);
                    // all local stuff
                    String s;
                    String From = null;
                    boolean haveFrom = false;
                    String Date = null;
                    boolean haveDate = false;
                    String Sub = null;
                    boolean haveSub = false;
                    String outstr = "";
                    String attachment = "";
                    base64attachment = false;
                    // make some room on the screen...
                    Main.mainwindow += "\n\n";
                    // read tmpmessage line by line
                    while ((s = br.readLine()) != null) {
                        // show what we've got...
                        Main.mainwindow += s;
                        Main.mainwindow += "\n";
                        // compile some patterns and set up the matchers
                        Pattern pfrm = Pattern.compile("^\\s*(From:)\\s(.*)");
                        Matcher mfrm = pfrm.matcher(s);
                        Pattern pdate = Pattern.compile("^\\s*(Date:)\\s(\\w{3})\\,*\\s+(\\d+)\\s(\\w{3})\\s(\\d{4})\\s(\\d\\d:\\d\\d:\\d\\d)");
                        Matcher mdate = pdate.matcher(s);
                        Pattern pdate2 = Pattern.compile("^\\s*(Date:)\\s(\\d+)\\s+(\\w{3})\\s+(\\d{4}\\s\\d\\d:\\d\\d:\\d\\d)");
                        Matcher mdate2 = pdate2.matcher(s);
                        Pattern psub = Pattern.compile("^\\s*(Subject:)\\s(.*)");
                        Matcher msub = psub.matcher(s);
                        Pattern p64 = Pattern.compile("^\\s*(content-transfer-encoding: base64)");
                        Matcher m64 = p64.matcher(s.toLowerCase());
                        Pattern c64 = Pattern.compile("^\\s*(content-type)");
                        Matcher cc64 = c64.matcher(s.toLowerCase());
                        Pattern pnm = Pattern.compile(".*(filename=)(.*)");
                        Matcher mnm = pnm.matcher(s);
                        Pattern xui = Pattern.compile("X-UI-ATTACHMENT");
                        Matcher mxui = xui.matcher(s);
                        Pattern nmx = Pattern.compile("name=");
                        Matcher mnmx = nmx.matcher(s);
                        if (mfrm.lookingAt()) {
                            if (mfrm.group(1).equals("From:") && !haveFrom) {
                                From = mfrm.group(2);
                                haveFrom = true;
                            }
                        } else if (mdate.lookingAt()) {
                            if (mdate.group(1).equals("Date:") && !haveDate) {
                                Date = mdate.group(2) + " " + mdate.group(3) + " "
                                        + mdate.group(4) + " " + mdate.group(5) + " "
                                        + mdate.group(6);
                                haveDate = true;
                            }
                        } else if (mdate2.lookingAt()) {
                            if (mdate2.group(1).equals("Date:") && !haveDate) {
                                Date = mdate2.group(2) + " " + mdate2.group(3) + " "
                                        + mdate2.group(4);
                                haveDate = true;
                            }
                        } else if (msub.lookingAt()) {
                            if (msub.group(1).equals("Subject:") && !haveSub) {
                                Sub = msub.group(2);
                                haveSub = true;
                            }
                        } else if (m64.lookingAt()) {
                            if (m64.group(1).equals("content-transfer-encoding: base64")) {
                                // there is an attachment...
                                base64attachment = true;
//                                                                debug ("Attachment");
                                outstr += s + "\n";  // write to Inbox
                            }
                        } else if (cc64.lookingAt()) {
                            outstr += s + "\n";  // write to Inbox
                        } else if (mxui.lookingAt()) {
                            outstr += s + "\n";  // write to Inbox
                        } else if (mnm.lookingAt()) {
                            if (mnm.group(1).equals("filename=")) {
                                // get the file name
                                if (mnm.group(2).startsWith("\"")) {
//                                                                    debug (attachmentFilename);
                                    attachmentFilename = mnm.group(2).substring(1, mnm.group(2).length() - 1);
                                    // remove the "
                                } else {
                                    attachmentFilename = mnm.group(2);
                                }
//                                                             debug (attachmentFilename);
                                outstr += s + "\n";  // write to Inbox
                            }
                        } else if (mnmx.lookingAt()) {
                            outstr += s + "\n";  // write to Inbox
                        } else {
                            if (base64attachment) {
                                outstr += s + "\n";  // write to Inbox
                                if (!s.equals("")) {
                                    if (!s.startsWith("--")) {
                                        if (!s.contains("-")) {
                                            attachment += s + "\n";
                                        }
//     System.out.println(s);
                                    } else {
                                        base64attachment = false;
                                        if (attachment.length() > 10 && attachmentFilename != null) {
                                            // is it a grib file?
                                            if (attachmentFilename.endsWith(".grb")) {
                                                // are we on linux?
                                                if (Main.separator.equals("/")) {
                                                    try {
                                                        File f1 = new File("/opt/zyGrib/grib/");
                                                        // is zygrib installed?
                                                        if (f1.isDirectory()) {
                                                            attachmentFilename = "/opt/zyGrib/grib/" + attachmentFilename;
                                                        } else {
                                                            // no, put it in the Files directory
                                                            File myfiles = new File(Main.homePath + Main.dirPrefix + "Files");
                                                            if (!myfiles.isDirectory()) {
                                                                myfiles.mkdir();
                                                            }
                                                            attachmentFilename = "Files/" + attachmentFilename;
                                                            attachmentFilename = Main.homePath + Main.dirPrefix + attachmentFilename;
                                                        }
                                                    } catch (Exception e) {
                                                        Main.q.Message("IO problem", 10);
                                                    }
                                                } else {
                                                    try {
                                                        // put it in the Files directory
                                                        File myfiles = new File(Main.homePath + Main.dirPrefix + "Files");
                                                        if (!myfiles.isDirectory()) {
                                                            myfiles.mkdir();
                                                        }
                                                        attachmentFilename = "Files\\" + attachmentFilename;
                                                        attachmentFilename = Main.homePath + Main.dirPrefix + attachmentFilename;
                                                    } catch (Exception e) {
                                                        Main.q.Message("IO problem", 10);
                                                    }
                                                }
                                            } else {
                                                // no grib file
                                                File myfiles = new File(Main.homePath + Main.dirPrefix + "Files");
                                                if (!myfiles.isDirectory()) {
                                                    myfiles.mkdir();
                                                }
                                                attachmentFilename = "Files" + Main.separator + attachmentFilename;
                                                attachmentFilename = Main.homePath + Main.dirPrefix + attachmentFilename;
                                            }
                                            try {
                                                File myfiles = new File(Main.homePath + Main.dirPrefix + "Files");
                                                if (!myfiles.isDirectory()) {
                                                    myfiles.mkdir();
                                                }
                                                // remove first line if "X-Attachment..."
                                                String[] attlines = attachment.split("\n");
                                                if (attlines[0].startsWith("X-Attachment")) {
                                                    attachment = "";
                                                    for (int i = 1; i < attlines.length; i++) {
                                                        if (!attlines[i].contains("-")) {
                                                            attachment += attlines[i] + "\n";
                                                            //                                                                                           Main.mainwindow += ";;" +attlines[i] + "\n"; // debug
                                                        }
                                                    }
                                                } else {
                                                    attachment = "";
                                                    for (int i = 0; i < attlines.length; i++) {
                                                        if (!attlines[i].contains("-")) {
                                                            attachment += attlines[i] + "\n";
                                                            //                                                                                           Main.mainwindow += ";;" +attlines[i] + "\n"; // debug
                                                        }
                                                    }
                                                }
                                                if (Main.separator.equals("\\")) {
                                                    attachmentFilename = attachmentFilename.substring(0, attachmentFilename.length() - 1);
                                                }
                                                boolean success = Base64.decodeToFile(attachment, attachmentFilename);
                                                if (success) {
                                                    Main.q.Message("File stored in " + attachmentFilename, 10);
                                                    Main.mainwindow += "File stored in " + attachmentFilename + "\n";
                                                } else {
                                                    Main.q.Message("File not stored in " + attachmentFilename, 10);
                                                    Main.mainwindow += "File not stored in " + attachmentFilename + "?\n";
                                                }
                                            } catch (Exception e) {
                                                Main.q.Message("Problem with decoding, " + e, 10);
                                            }
                                        }
                                    }
                                }
                            } else {
                                outstr += s + "\n";  // write to Inbox
                            }
                        }

                    } // end while
                    fr.close();
                    this.inbox = new FileWriter(Main.homePath + Main.dirPrefix + "Inbox", true);
                    inbox.write("From " + From + " " + Date + "\n");
                    inbox.write("From: " + From + "\n");
                    if (Date != null) {
                        inbox.write("Date: " + Date + "\n");
                    }
                    inbox.write("Subject: " + Sub + "\n");
                    // write message body
                    if (outstr != null) {
                        inbox.write(outstr + "\n");
                    }
                    inbox.flush();
                    inbox.close();
                    //Must run on UI thread otherwise we get index out of bound errors
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            Main.mainui.refreshInbox();
                        }
                    });
                    File fl = new File(Main.homePath + Main.dirPrefix + "tmpmessage");
                    if (fl.exists()) {
                        fl.delete();
                    }
                    //Pending File Close                    
                    try {
                        if (pFile != null) {
                            pFile.close();
                            Trfile.delete();
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the pending file.", ex, false);
                    }
                    File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                    if (pending.exists()) {
                        pending.delete();
                    }
                    if (Main.protocol > 0) {
                        Main.txText += "~FA:" + Transaction + "\n";
                    }
                    Main.progress = 0;
                    Main.q.Message("Added to mbox queue", 10);
                }
                // compressed web pages download
                if (CwwwDownload) {
                    CwwwDownload = false;
                    Main.comp = false;
                    try {
                        this.dlFile.close();
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the download file.", ex, false);
                    }
                    try {
                        try {
                            Base64.decodeFileToFile(Main.homePath + Main.dirPrefix + "TempFile", Main.homePath + Main.dirPrefix + "TMP.gz");
                        } catch (Exception ex) {
                            Main.log.writelog("Error when trying to B64-decode the download file.", ex, false);
                        }
                        try {
                            Unzip.Unzip(Main.homePath + Main.dirPrefix + "TMP.gz");
                        } catch (Exception exz) {
                            Main.log.writelog("Error when trying to unzip the download file.", exz, false);
                        }
                        try {
                            BufferedReader in = new BufferedReader(new FileReader(Main.homePath + Main.dirPrefix + "TMP"));
                            String str2;
                            while ((str2 = in.readLine()) != null) {
                                Main.mainwindow += (str2 + "\n");
                            }
                            in.close();
                        } catch (IOException e) {
                            Main.q.Message("problem decoding B64 file", 10);
                        }
                        File tmp1 = new File(Main.homePath + Main.dirPrefix + "TMP");
                        if (tmp1.exists()) {
                            tmp1.delete();
                        }
                        //Pending File Close                    
                        try {
                            if (pFile != null) {
                                pFile.close();
                                Trfile.delete();
                            }
                        } catch (IOException ex) {
                            Main.log.writelog("Error when trying to close the pending file.", ex, false);
                        }
                        File tmp = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                        if (tmp.exists()) {
                            tmp.delete();
                        }
                        if (Main.protocol > 0) {
                            Main.txText += "~FA:" + Transaction + "\n";
                        }
                        Main.progress = 0;
                    } catch (Exception exc) {
                        Main.log.writelog("Error handling the download file.", exc, false);
                    } catch (NoClassDefFoundError exp) {
                        Main.q.Message("problem decoding B64 file", 10);
                    }
                    File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
                    boolean success = tmp.delete();
                    try {
                        if (pFile != null) {
                            pFile.close();
                            Trfile.delete();
                        }
                    } catch (IOException ex) {
                        Main.log.writelog("Error when trying to close the pending file.", ex, false);
                    }
                    Main.progress = 0;
                }
                // web pages   download                                      
                if (WWWDownload) {
                    WWWDownload = false;
                }
                Main.q.Message("done...", 10);
                Main.progress = 0;
                Transaction = "";

            } else if (me.group(1).equals("-abort-")) {
                foundMatchingCommand = true;
                abortAllRxTransactions();
            }
        }
        // NNNN 
// fleetcodes
        if (!Main.connected & Main.iacMode) {
            Pattern pn = Pattern.compile("<SOH>(NNNN)");
            Matcher mn = pn.matcher(str);
            if (mn.lookingAt()) {
                if (mn.group(1).equals("NNNN")) {
                    Main.iacMode = false;
                    Main.q.Message("End of code...", 10);
                    Main.status = "Listening";
                    try {
                        iacout.close();
                    } catch (Exception e) {
                        Main.log.writelog("Error closing the iac file.", e, false);
                    }
                    f.fastfec2(Main.homePath + Main.dirPrefix + "iactemp", "");
                    deleteFile("iactemp");
                }
            }
        }
        // bulletin                               
        if (!Main.connected & Main.bulletinMode) {
            Pattern pn = Pattern.compile("<SOH>(NNNN)");
            Matcher mn = pn.matcher(str);
            if (mn.lookingAt()) {
                if (mn.group(1).equals("NNNN")) {
                    Main.bulletinMode = false;
                    Main.q.Message("End of bulletin...", 2);
                }
            }
        }

        // write headers
        if (Headers & !Firstline) {
            Pattern phd = Pattern.compile("^(\\s*\\d+.*)");
            Matcher mhd = phd.matcher(str);
            if (mhd.lookingAt()) {
                String outToWindow = mhd.group(1) + "\n";
                Main.mailHeadersWindow += outToWindow;
                DataReceived += outToWindow.length();
                if (DataSize > 0) {
                    Main.progress = 100 * DataReceived / DataSize;
                }
                try {
                    this.headers.write(str + "\n");
                } catch (IOException ex) {
                    Main.log.writelog("Error when trying to write to headers file.", ex, false);
                }
            }
        }
        // files list
        if (FileList & !Firstline) {
            Main.filesTextArea += str + "\n";
            DataReceived += str.length();
            Main.progress = 100 * DataReceived / DataSize;
        }
        // write file
        if (FileDownload & !Firstline) {
            //Do not display compressed data in the file text box
            if (!Main.comp) Main.filesTextArea += str + "\n";
            DataReceived += str.length();
            if (DataSize > 0) {
                Main.progress = 100 * DataReceived / DataSize;
            }
            try {
                if (pFile != null) {
                    pFile.write(str + "\n");
                    pFile.flush();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to write to pending file.", ex, false);
            }
            try {
                this.dlFile.write(str + "\n");
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to write to download file.", ex, false);
            }
        }
// messages                                    
        if (MsgDownload & !Firstline) {
            foundMatchingCommand = true;
            DataReceived += str.length();
            Main.progress = 100 * DataReceived / DataSize;
            this.tmpmessage.write(str + "\n");
        }

        // compressed messages download or compressed Email upload                                   
        if ((CMsgDownload | CompressedEmailUpload) & !Firstline) {
            foundMatchingCommand = true;
            DataReceived += str.length();
            Main.progress = 100 * DataReceived / DataSize;

            try {
                if (pFile != null) {
                    pFile.write(str + "\n");
                    pFile.flush();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to write to pending file.", ex, false);
                Main.q.Message("Error writing pending file.", 1);
            }
            try {
                tmpmessage.write(str + "\n");
                tmpmessage.flush();
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to write to tmpmessage file.", ex, false);
            }
        }
        // compressed www pages
        if (CwwwDownload & !Firstline) {
            foundMatchingCommand = true;
            DataReceived += str.length();
            Main.progress = 100 * DataReceived / DataSize;
            try {
                if (pFile != null & str.length() > 0) {
                    pFile.write(str + "\n");
                    pFile.flush();
                }
            } catch (IOException ex) {
                //Do nothing as it happens normally when a compressed download is aborted and restarted without closing the app (init issue?)
                //Main.log.writelog("Error when trying to write to pending file.", ex, true);
            }
            try {
                if (str.length() > 0) {
                    dlFile.write(str + "\n");
                    dlFile.flush();
                }
            } catch (IOException ex) {
                //Do nothing as it happens normally when a compressed download is aborted and restarted without closing the app (init issue?)
                //Main.log.writelog("Error when trying to write to tmpmessage file.", ex, true);
            }
        }

        // www pages       WWWDownload     
        if (WWWDownload & !Firstline) {
            foundMatchingCommand = true;
            DataReceived += str.length();
            //double ProGress = 100 * DataReceived / DataSize;
            if (DataSize > 0) {
                Main.progress = 100 * DataReceived / DataSize;
            }
        }
        // iac fleetcode file     
        //debug (Integer.toString(str.length()));

        if (!Main.connected & Main.iacMode & str.length() > 0) {
            try {
                iacout.write(str + "\n");
                iacout.flush();
                Main.mainwindow += str + "\n";
            } catch (IOException exc) {
                Main.log.writelog("Error when trying to write to download file.", exc, false);
            }
        }
        //Do we have an unknown command?
        if (!foundMatchingCommand && Main.ttyConnected.equals("Connected")) {
            Pattern pmc = Pattern.compile("^(\\s*\\~[A-Z]{2,}.*)");
            Matcher mmc = pmc.matcher(str);
            if (mmc.lookingAt()) {
                Main.txText = "\nI don't understand:" + str + "\n";
            }
        }
    }

    /**
     * Move a file. Used to save sent email to the sent folder
     *
     * @param from file name and path, relative path
     * @param todir The new folder
     * @return
     */
    private boolean MoveFile(String from, String todir) {
        try {
            boolean res = false;
            // File  to be moved
            File file = new File(from);
            // Destination directory
            File dir = new File(todir);
            // Move file to new directory
            res = file.renameTo(new File(dir, file.getName()));

            return res;
        } catch (Exception e) {
            Main.log.writelog("Could not move file.", e, false);
            return false;
        }
    }

    public boolean TransactionsExists() {
        boolean result = false;
        String Tr = Main.homePath + Main.dirPrefix + "Transactions";
        File Transactions = new File(Tr);
        if (Transactions.exists()) {
            result = true;
        }
        return result;
    }

    //Reset private variable Blocklength to middle value of 5 (32 bytes block size). Used when changing modes up and down.
    public void SetBlocklength(int NewBlocklength) {
        Blocklength = NewBlocklength;
    }
    
    //UTF strings handling
    private int lengthCodepoints(String s) {
        return s.codePointCount(0, s.length());
    }

    private static String substringCodepoint(String s, int startCodepoint, int numCodepoints) {
        int startIndex = s.offsetByCodePoints(0, startCodepoint);
        int endIndex = s.offsetByCodePoints(startIndex, numCodepoints);
        return s.substring(startIndex, endIndex);
    }


    public String doTXbuffer() {

        String Outbuffer = "";
        int nr_missing = tx_missing.length();
        int b[] = new int[nr_missing];  // array of missing blocks
        int i;
        for (i = 0; i < nr_missing; i++) {
            b[i] = (int) tx_missing.substring(i, i + 1).charAt(0) - 32;
        }
        if (tx_missing.length() > 2) {
        //With adaptive modes, blocksize of 8 is not effective. Minimum of 16 (2 ** 4)
        //  if (Blocklength > 3) {
            if (Blocklength > 4) {
                Blocklength--;
            }
        } else if (tx_missing.length() < 1) {
            if (Blocklength < 6) {
                Blocklength++;
            }
        } else {
            Blocklength = 5;
        }

        // add missing blocks
        int howFarBack = 0; //number of blocks between oldest to re-transmit and next block to send
        if (nr_missing > 0) {
            for (i = 0; i < nr_missing; i++) {
                String block = TX_addblock(b[i]);
                Outbuffer += block;
            }
            howFarBack = lastqueued - b[0];
            if (howFarBack < 0) howFarBack += 64;
        }

        i = 0;
        Blocklength = Integer.parseInt(Main.txBlockLength);
        int Maxblocklength = 6;
        int Minblocklength = 4;
        int Nrblocks = 8;
        String TXmd = Main.m.getTXModemString(Main.txModem);
        // System.out.println(TXmd)  ;
        if (TXmd.equals("PSK500")) {
            Nrblocks = 16;
            Maxblocklength = 6;
            Minblocklength = 4;
        } else if (TXmd.equals("PSK500R") || TXmd.equals("PSK250")) {
            Nrblocks = 10;
            Maxblocklength = 6;
            Minblocklength = 4;
        } else if (TXmd.equals("PSK250R")) {
            Nrblocks = 8;
            Maxblocklength = 5;
            Minblocklength = 4;
        } else {
            Nrblocks = 6;
            Maxblocklength = 5;
            Minblocklength = 4;
        }
        //VK2ETA bug: We need a limit as to how much forward we can enqueue blocks, otherwise 
        //   we overwrite the to-be-retransmitted block with new ones
        while (i < (Nrblocks - nr_missing) && Main.txText.length() > 0 &&
                howFarBack < 32 ) {
            String newstring = "";
            if (Blocklength < Minblocklength) {
                Blocklength = Minblocklength;
            } else if (Blocklength > Maxblocklength) {
                Blocklength = Maxblocklength;
            }
            double bl = Math.pow(2, Blocklength);
            //UTF-8 Handling. Use codepoints instead of raw string length 
            //int queuelen = Main.txText.length();
            int queuelen = lengthCodepoints(Main.txText);

            if (queuelen > 0) {
                if (queuelen <= (int) bl) {
                    newstring = Main.txText;
                    Main.txText = "";
                } else {
                    //newstring = Main.txText.substring(0, (int) bl);
                    newstring = substringCodepoint(Main.txText, 0, (int) bl);
                    //Main.txText = Main.txText.substring((int) bl);
                    Main.txText = substringCodepoint(Main.txText, (int) bl, queuelen - (int) bl);
                }

//            lastqueued += 1;
//            if (lastqueued > 63) {
//                lastqueued = 0;
//            }
                txbuffer[lastqueued] = newstring;
                //??? Blank out past 16 ahead for 8 blocks 
                for (int j = lastqueued + 17; j < lastqueued + 25; j++) {
                    txbuffer[j % 64] = "";
                }
            }

            String block = TX_addblock(lastqueued);

            char lasttxchar = (char) (lastqueued + 32);
            rx_lastsent = Character.toString(lasttxchar);
            Main.myRxStatus = getTXStatus();
            Outbuffer += block;
            i++;
            lastqueued += 1;
            howFarBack += 1;
            if (lastqueued > 63) {
                lastqueued = 0;
            }
        }

        return Outbuffer;
    }

    public String TX_addblock(int nr) {
        if (txbuffer[nr].length() > 0) {
            char c = (char) (nr + 32);
            String accum = "0";
            accum += Main.session;
            accum += Character.toString(c);
            accum += Session.txbuffer[nr];
            String blcheck = Arq.checksum(accum);
            accum += blcheck;
            return accum + (char) 1;
        } else {
            return "";
        }
    }

    /**
     * Add a line to Main.TX_Text but make sure only one instance gets added
     *
     * @param command
     */
    void addSendCommand(String command) {
        String mymirror = Main.txText;
        if (!mymirror.contains(command)) {
            Main.txText += command;
        }
    }

    void sendQTC(String mailnr) {
        //Main.TX_Text += "~QTC " + mailnr + "+\n";   
        addSendCommand("~QTC " + mailnr + "+\n");
    }

    void sendDelete(String numbers) {
        //Main.TX_Text += "~DELETE " + numbers + "\n";
        addSendCommand("~DELETE " + numbers + "\n");
    }

    void sendRead(String mailnr) {
        if (Main.compressedmail) {
            //Main.TX_Text += "~READZIP " + mailnr + "\n";
            addSendCommand("~READZIP " + mailnr + "\n");
        } else {
            //Main.TX_Text += "~READ " + mailnr + "\n";   
            addSendCommand("~READ " + mailnr + "\n");
        }
    }

    public void makeFile(String filename) {
        String fileName = Main.homePath + Main.dirPrefix + filename;
        File fl = new File(fileName);
        try {
            boolean success = fl.createNewFile();
        } catch (IOException e) {
            Main.log.writelog("Error creating headers file:", e, false);
        }
    }

    public void deleteFile(String filename) {
        String fileName = Main.homePath + Main.dirPrefix + filename;
        // A File object to represent the filename
        File fl = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        try {
            if (!fl.exists()) {
                throw new IllegalArgumentException("Delete: Does not exist: "
                        + fileName);
            }

            if (!fl.canWrite()) {
                throw new IllegalArgumentException("Delete: write protected: "
                        + fileName);
            }

            // If it is a directory, make sure it is empty
            if (fl.isDirectory()) {
                String[] files = fl.list();
                if (files.length > 0) {
                    throw new IllegalArgumentException(
                            "Delete: directory not empty: " + fileName);
                }
            }

            // Attempt to delete it
            boolean success = fl.delete();

            if (!success) {
                throw new IllegalArgumentException("Delete: deletion failed");
            }
        } catch (IllegalArgumentException e) {
            Main.log.writelog("Error deleting headers file:", e, false);
        }

    }

    /**
     * Method used to save attachments
     *
     * @param Filename Filename including path
     * @param attachment The content
     * @param B64 True if its encoded and should be decoded
     */
    private void SaveAttachmentToFile(String Filename, String attachment, Boolean B64) {

        try {

            // Make sure there is a filename, just exit if not
            if (Filename.length() < 1) {
                return;
            }

            // First make sure we have a directory to save the attachments to
            File myfiles = new File(Main.homePath + Main.dirPrefix + "Files");
            if (!myfiles.isDirectory()) {
                myfiles.mkdir();
            }

            // remove first line if "X-Attachment..."
            String[] attlines = attachment.split("\n");
            if (attlines[0].startsWith("X-Attachment")) {
                attachment = "";
                for (int i = 1; i < attlines.length; i++) {
                    attachment += attlines[i] + "\n";
                    // Main.mainwindow += "::" +attlines[i] + "\n"; // debug
                }
            } else {
                attachment = "";
                for (int i = 0; i < attlines.length; i++) {
                    attachment += attlines[i] + "\n";
                    Main.mainwindow += ";;" + attlines[i] + "\n"; // debug
                }
            }

            if (!B64) {
                // Save to a file
                FileWriter fstream = new FileWriter(Filename);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(attachment);
                //Close the output stream
                out.close();
                Main.q.Message("File stored in " + Filename, 10);
                Main.mainwindow += "File stored in " + Filename + "\n";
            } else {
                boolean success = Base64.decodeToFile(attachment, Filename);
                if (success) {
                    Main.q.Message("File stored in " + Filename, 10);
                    Main.mainwindow += "File stored in " + Filename + "\n";
                } else {
                    Main.q.Message("File not stored in " + Filename, 10);
                    Main.mainwindow += "File not stored in " + Filename + "?\n";
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Had trouble saving the attachment. " + e.getMessage().toString(), e, false);
            Main.q.Message("Problem with decoding, " + e, 10);
        }
    }

    /**
     * read last header number from headers file
     *
     * @param filename
     * @return
     */
    public String getHeaderCount(String filename) {
        FileReader hdr = null;
        String Countstr = "0";
        File fh = new File(filename);

        if (!fh.exists()) {
            return "1";
        }
        try {
            hdr = new FileReader(fh);
            BufferedReader br = new BufferedReader(hdr);
            String s;
            while ((s = br.readLine()) != null) {
                //===================================
                Pattern ph = Pattern.compile("^\\s*(\\d+)");
                Matcher mh = ph.matcher(s);
                if (mh.lookingAt()) {
                    Countstr = mh.group(1);
                    int Count = Integer.parseInt(Countstr);
                    Count++;
                    Countstr = Integer.toString(Count);

                }
//=====================================                        
            }
            br.close();
        } catch (IOException e) {
            Main.log.writelog("Error when trying to read the headers file.", e, false);
        }
        return Countstr;
    }

    private String convert_to_aprsformat(String Tag, String latstring, String lonstring, String statustxt, String Icon) {
        String returnframe = "";
        try {
            float latnum = 0;
            float lonnum = 0;
            String latsign = "N";
            String lonsign = "E";
            String course = "0";
            String speed = "0";

            String callsign = Main.configuration.getPreference("CALL");
            statustxt = Main.configuration.getPreference("STATUS");

            latnum = Float.parseFloat(latstring);
            lonnum = Float.parseFloat(lonstring);
            if (latnum < 0) {
                latnum = Math.abs(latnum);
                latsign = "S";
            }
            if (lonnum < 0) {
                lonnum = Math.abs(lonnum);
                lonsign = "W";
            }

            DecimalFormat twoPlaces = new DecimalFormat("##0.00");
            int latint = (int) latnum;
            int lonint = (int) lonnum;

            latnum = ((latnum - latint) * 60) + latint * 100;
            latstring = twoPlaces.format(latnum);
            latstring = "0000" + latstring;
            int len = latstring.length();
            if (len > 6) {
                latstring = latstring.substring(len - 7, len);
            }

            // Make sure there is a period in there
            latstring = latstring.replace(",", ".");

            lonnum = ((lonnum - lonint) * 60) + lonint * 100;
            lonstring = twoPlaces.format(lonnum);
            lonstring = "00000" + lonstring;

            len = lonstring.length();
            if (len > 7) {
                lonstring = lonstring.substring(len - 8, len);
            }

            //make sure we have a period there
            lonstring = lonstring.replace(",", ".");
//       System.out.println("|" + Icon + "|") ;
            if (Icon.equals("P90")) {
                Icon = "y";
            } else if (Icon.equals("P14")) {
                Icon = "-";
            } else if (Icon.equals("S07")) {
                Icon = "&";
            } else if (Icon.equals("P31")) {
                Icon = ">";
            } else if (Icon.equals("P42")) {
                Icon = "I";
            } else if (Icon.equals("P87")) {
                Icon = "v";
            } else if (Icon.equals("P83")) {
                Icon = "r";
            } else if (Icon.equals("P82")) {
                Icon = "s";
            } else if (Icon.equals("P56")) {
                Icon = "Y";
            } else if (Icon.equals("P52")) {
                Icon = "U";
            } else if (Icon.equals("P26")) {
                Icon = ";";
            } else if (Icon.equals("P60")) {
                Icon = "[";
            } else if (Icon.equals("P78")) {
                Icon = "o";
            } else if (Icon.equals("P02")) {
                Icon = "#";
            } else if (Icon.equals("S04")) {
                Icon = "#";
            } else if (Icon.equals("P86")) {
                Icon = "k";
            } else if (Icon.equals("P87")) {
                Icon = "k";
            } else if (Icon.equals("P64")) {
                Icon = "_";
            } else if (Icon.equals("S87")) {
                Icon = "n";
            } else if (Icon.equals("P13")) {
                Icon = ",";
            } else {
                Icon = "D";
            }

            // Fix stream id, this is wrong
            returnframe = Tag + ">PSKAPR,qAs," + callsign + "*:!" + latstring + latsign + Main.ICONlevel + lonstring + lonsign + Icon;

            return returnframe;
            //           }
        } catch (Exception ex) {
            Main.log.writelog("Error when creating beaconblock", ex, false);
        }

        return returnframe;
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return tmp / p;
    }

    public void debug(String message) {
        System.out.println("Debug:" + message);
    }

    /**
     * Move a file to a new folder. Used for example for moving from Outbox to
     * Sent
     *
     * @param thefile The file to move
     * @param folder Where to move it, complete path
     */
    private boolean movefiletodir(File thefile, String folder) {
        try {
            // Destination directory
            File dir = new File(folder);

            // If that folder does not exist then create it
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Move file to new directory
            boolean success = thefile.renameTo(new File(dir, thefile.getName()));
            if (!success) {
                Main.log.writelog("File was not successfully moved.", false);
            }
            return success;
        } catch (Exception e) {
            Main.log.writelog("Error when trying to move a file.", e, false);
            return false;
        }
    }
 
    
    //Builds a resume command string for transmission taking into account if is a server or a client
    public String partialFilesResume(String folder, String header) {
        String result = "";
        File[] pendingFilesList;
        String toCall = "";
        String fromCall = "";
        boolean iAmServer;
        
        // Get the list of files in the designated folder
        File dir = new File(Main.homePath + Main.dirPrefix + folder);
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        //Get the callsigns
        if (Main.ttyConnected.equals("Connected")) {
            //I am server
            toCall = Main.ttyCaller;
            fromCall = Main.callsignAsServer;
            iAmServer = true;
        } else {
            //I am client
            toCall = myserver;
            fromCall = Main.mycall;
            iAmServer = true;
        }
        //vk2eta+pm_-u-_tmp2b4662a0b27610b45a_-myfile.txt
        Pattern fm = Pattern.compile("([a-zA-Z0-9\\+\\-]+)_-(\\w?)-_([a-zA-Z0-9]+)(_-(.+))?");
        Matcher fmm;
        //Generates an array of strings containing the file names
        pendingFilesList = dir.listFiles(fileFilter);
        for (int i = 0; i < pendingFilesList.length; i++) {
            String pendingCaller = "";
            String pendingType = "";
            String pendingToken = "";
            String pendingFileName = "";
            String pendingFn = pendingFilesList[i].getName();
            fmm = fm.matcher(pendingFn);
            if (fmm.lookingAt()) {
                pendingCaller = fmm.group(1).replaceAll("\\+", "/");
                pendingType = fmm.group(2);
                pendingToken = fmm.group(3);
                if (fmm.group(5) != null) {
                    pendingFileName = fmm.group(5);
                }
                if (pendingCaller.equals(toCall)) {
                    //Found a match for callsign, add to list
                    result += header + fromCall + ":" + toCall + ":"
                            + pendingToken + ":" + pendingType; 
                            //Not used + ":" + pendingFileName + ":" + Long.toString(pendingFilesList[i].length()) + "\n";
                }
            }
        }
        
        return result;
    }
    
      
    /*
    //Scan the Pending folders 
    public static String getPendingList(String server, String caller) {
        String returnList = "";

        File[] partialFiles;
        // Get the list of files in the designated folder
        File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
        partialFiles = dir.listFiles();
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        //Generates an array of strings containing the file names to resume downloading
        partialFiles = dir.listFiles(fileFilter);
        for (int i = 0; i < partialFiles.length; i++) {
            String pendingCaller = "";
            String pendingType = "";
            String pendingToken = "";
            String pendingFn = partialFiles[i].getName();
            if (pendingFn.contains("_-")) {
                int firstSep = pendingFn.indexOf("_-");
                int secondSep = pendingFn.indexOf("-_");
                if (firstSep > 0 && secondSep > 0) {
                    pendingCaller = pendingFn.substring(0, firstSep);
                    pendingType = pendingFn.substring(firstSep + 2, secondSep);
                    pendingToken = pendingFn.substring(secondSep + 2);
                }
                //Change back the "+" characters in file name into a "/" as in vk2eta/m
                //if (pendingCaller.equals(caller)) {
                if (pendingCaller.replaceAll("\\+", "/").equals(caller)) {
                    //Add this file to the list of pending downloads
                    //>FO5:PI4TUE:PA0R:JhyJkk:f:test.txt:496
                    returnList += ">FO5:" + server + ":" + caller + ":" + pendingToken + ":" + pendingType + ": :" + partialFiles[i].length() + "\n";
                } 
            }
        }
        //Now check the outpending directory
        dir = new File(Main.homePath + Main.dirPrefix + "Outpending");
        partialFiles = dir.listFiles();
        fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        //Generates an array of strings containing the file names to resume downloading
        partialFiles = dir.listFiles(fileFilter);
        for (int i = 0; i < partialFiles.length; i++) {
            String pendingCaller = "";
            String pendingType = "";
            String pendingToken = "";
            String pendingFn = partialFiles[i].getName();
            if (pendingFn.contains("_-")) {
                int firstSep = pendingFn.indexOf("_-");
                int secondSep = pendingFn.indexOf("-_");
                if (firstSep > 0 && secondSep > 0) {
                    pendingCaller = pendingFn.substring(0, firstSep);
                    pendingType = pendingFn.substring(firstSep + 2, secondSep);
                    pendingToken = pendingFn.substring(secondSep + 2);
                }
                //Change back the "+" characters in file name into a "/" as in vk2eta/m
                //if (pendingCaller.equals(caller)) {
                if (pendingCaller.replaceAll("\\+", "/").equals(caller)) {
                    //Add this file to the list of pending downloads
                    //>FO5:PI4TUE:PA0R:JhyJkk.tmp:u:496
                    returnList += ">FO5:" + server + ":" + caller + ":" + pendingToken + ":" + pendingType + ": :" + partialFiles[i].length() + "\n";
                } 
            }
        }

        return returnList;
    }
    */
    
    //Look into designated folder for a matching callsign and transaction. Returns true if found and deleted.
    private boolean partialFilesDelete(String folder, String caller, String token) {
        boolean result = false;
        File[] pendingFilesList;
        
        // Get the list of files in the designated folder
        File dir = new File(Main.homePath + Main.dirPrefix + folder);
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        Pattern fm;
        if (caller.length() == 0) { //No destination call sign, no upload type (E.g. email)
            //tmp2b4662a0b27610b45a
            fm = Pattern.compile("([a-zA-Z0-9]+)");
        } else  {
            //vk2eta+pm_-u-_tmp2b4662a0b27610b45a_-myfile.txt //file download (should it be -f- ?)
            //vk2eta+p_-w-_8b58a0 //web page
            fm = Pattern.compile("([a-zA-Z0-9\\+\\-]+)_-(\\w?)-_([a-zA-Z0-9]+)(_-(.+))?");
        }
        Matcher fmm;
        //Generates an array of strings containing the file names
        pendingFilesList = dir.listFiles(fileFilter);
        for (int i = 0; i < pendingFilesList.length; i++) {
            String pendingCaller = "";
            String pendingType = "";
            String pendingToken = "";
            String pendingFileName = "";
            String pendingFn = pendingFilesList[i].getName();
            fmm = fm.matcher(pendingFn);
            if (fmm.lookingAt()) {
                if (caller.length() == 0) { //No destination call sign
                    pendingToken = fmm.group(1);
                } else {
                    pendingCaller = fmm.group(1).replaceAll("\\+", "/");
                    pendingType = fmm.group(2);
                    pendingToken = fmm.group(3);
                    if (fmm.group(5) != null) {
                        pendingFileName = fmm.group(5);
                    }
                }
                if ((caller.length() == 0 || pendingCaller.equals(caller))
                    && pendingToken.equals(token)) {
                    File penfOut = pendingFilesList[i].getAbsoluteFile();
                    if (penfOut.exists()) {
                        penfOut.delete();
                        result = true;
                    }
                }
            }
        }
        
        return result;
    }
    
    //Resets all receiving transactions (we received and -abort- OR we issued a STOP command as a client)
    public void abortAllRxTransactions() {
        if (Headers) {
            Headers = false;
            DataReceived = 0;
            Main.dataSize = Integer.toString(0);
            try {
                this.headers.close();
                //Must run on UI thread otherwise we get index out of bound errors
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Main.mainui.refreshEmailGrid();
                    }
                });
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the headers file.", ex, false);
            }
        }
        if (FileList) {
            FileList = false;
        }
        if (FileDownload) {
            FileDownload = false;
            Main.comp = false;
            try {
                this.dlFile.close();
                File df = new File(Main.homePath + Main.dirPrefix + "TempFile");
                if (df.exists()) {
                    boolean scs = df.delete();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the download file.", ex, false);
            }
            try {
                //Pending File Close                    
                try {
                    if (pFile != null) {
                        pFile.close();
                        Trfile.delete();
                    }
                } catch (IOException ex) {
                    Main.log.writelog("Error when trying to close the pending file.", ex, false);
                }
                File tmp = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                if (tmp.exists()) {
                //No, keep    tmp.delete();
                }
                //No, don't request deletion Main.txText += "~FA:" + Transaction + "\n";
                Main.progress = 0;
            } catch (Exception exc) {
                Main.log.writelog("Error when trying to decode the downoad file.", exc, false);
            } catch (NoClassDefFoundError exp) {
                Main.q.Message("problem decoding B64 file", 10);
            }
            File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
            boolean success = tmp.delete();
            try {
                if (pFile != null) {
                    pFile.close();
                    Trfile.delete();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the pending file.", ex, false);
            }
            Main.progress = 0;
            //                                                    Main.log(ThisFile + " received");
        }
        // messages  download      - append tmpmessage to Inbox in mbox format
        if (MsgDownload) {
            MsgDownload = false;
            try {
                this.tmpmessage.close();
                // append to Inbox file
                FileReader fr = new FileReader(Main.homePath + Main.dirPrefix + "tmpmessage");
                File fl = new File(Main.homePath + Main.dirPrefix + "tmpmessage");
                if (fl.exists()) {
                    fl.delete();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the tmpmessage file.", ex, false);
            }
        }
        // compressed messages  download or compressed Email upload
        if (CMsgDownload | CompressedEmailUpload) {
            CMsgDownload = false;
            CompressedEmailUpload = false;
            Main.comp = false;
            try {
                tmpmessage.close();
                // append to Inbox file
                File fl = new File(Main.homePath + Main.dirPrefix + "tmpmessage");
                if (fl.exists()) {
                    fl.delete();
                }
                //Pending File Close                    
                try {
                    if (pFile != null) {
                        pFile.close();
                        Trfile.delete();
                    }
                } catch (IOException ex) {
                    Main.log.writelog("Error when trying to close the pending file.", ex, false);
                }
                File pending = new File(Main.homePath + Main.dirPrefix + "Pending" + Main.separator + Transaction);
                if (pending.exists()) {
                //No, keep    pending.delete();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the pending file.", ex, false);
            }
        }
        // compressed web pages download
        if (CwwwDownload) {
            CwwwDownload = false;
            Main.comp = false;
            try {
                this.dlFile.close();
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the download file.", ex, false);
            }
            File tmp = new File(Main.homePath + Main.dirPrefix + "TempFile");
            boolean success = tmp.delete();
            try {
                if (pFile != null) {
                    pFile.close();
                    //No, keep: Trfile.delete();
                }
            } catch (IOException ex) {
                Main.log.writelog("Error when trying to close the pending file.", ex, false);
            }
            Main.progress = 0;
        }
        // web pages   download
        if (WWWDownload) {
            WWWDownload = false;
        }
        Main.q.Message("done...", 10);
        Main.progress = 0;
        Transaction = "";

    }
    
    
       
    static Process ScriptProcess = null;
    static StringBuilder outBuffer = new StringBuilder(200);
    static StringBuilder errBuffer = new StringBuilder(200);
    static final java.util.concurrent.atomic.AtomicBoolean scriptRunning = new java.util.concurrent.atomic.AtomicBoolean(false);

    //Runs the script in its own thread so the main loop is not blocked                                                                                                             
    //scriptName is the bare script name (no path), args are optional                                                                                                               
    public static void executeScript(final String scriptName, final String... args) {
        if (!scriptRunning.compareAndSet(false, true)) {
            Main.txText += "A script is already running, try again later\n";
            return;
        }
        final String[] scriptArgs = (args == null) ? new String[0] : args.clone();
        Thread scriptThread = new Thread(() -> {
            try {
                runScript(scriptName, scriptArgs);
            } finally {
                scriptRunning.set(false);
            }
        }, "ScriptRunner");
        scriptThread.setDaemon(true);
        scriptThread.start();
    }

    private static void runScript(String script, String[] args) {
        outBuffer = new StringBuilder(200);
        errBuffer = new StringBuilder(200);

        if (!RMsgUtil.isScriptInSafeDirectory(script, Main.dirScripts)) {
            Main.txText += "Can't run " + script + ", it must be in the Scripts directory of .pskmail";
            return;
        }

        String fullScriptPath = Main.homePath + Main.dirPrefix + Main.dirScripts + Main.separator + script;
        //Script path and each argument are separate list entries, so no shell parsing of the arguments                                                                             
        java.util.List<String> cmd = new java.util.ArrayList<>();
        if (Main.onWindows) {
            cmd.add(fullScriptPath);
        } else { //Linux: bash runs the script, "$@" in the script receives the arguments                                                                                                                                                                                    
            cmd.add("bash");
            cmd.add(fullScriptPath);
        }
        for (String arg : args) {
            cmd.add(arg);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            ScriptProcess = processBuilder.start();
            //Launching using cmd.exe creates a subprocess for the script that is un-killable from Java                                                                             
            //fldigiProc = Runtime.getRuntime().exec(fldigiPath);                                                                                                                   

            //Prepare a thread to consume the Standard Output Stream                                                                                                                
            Thread outputThread = new Thread(()
                    -> readStream(ScriptProcess.getInputStream(), "STDOUT")
            );
            //Prepare a thread to consume the Standard Error Stream                                                                                                                 
            Thread errorThread = new Thread(()
                    -> readStream(ScriptProcess.getErrorStream(), "STDERR")
            );
            //Start both threads running in the background                                                                                                                          
            outputThread.start();
            errorThread.start();
            //Wait for the external process to finish                                                                                                                               
            int exitCode = ScriptProcess.waitFor();
            //Wait for the reading threads to finish emptying the buffers                                                                                                           
            outputThread.join();
            errorThread.join();
            //Process the result                                                                                                                                                    
            System.out.println("\nProcess exited with code: " + exitCode);
            String errString = errBuffer.toString();
            //150 characters hard limit                                                                                                                                             
            if (errString.length() > 200) {
                errString = errString.substring(0, 200) + "...";
            }
            String outString = outBuffer.toString();
            //150 characters hard limit                                                                                                                                             
            if (outString.length() > 500) {
                outString = outString.substring(0, 500) + "...";
            }
            if (!errString.isEmpty()) {
                outString += "\n With error: " + errString;
            }
            StringBuilder arguments = new StringBuilder(100);
            for (String argStr : args) {
                String thisArg = " " + argStr;
                arguments.append(thisArg);
            }
            Main.txText += "\n" + script + " " + arguments.toString() + " says:\n" + outString;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (!errBuffer.toString().isEmpty()) {
                Main.txText += "Error running " + script + ": " + errBuffer.toString();
            }
            e.printStackTrace();
        }
    }

              
              
              
              
              
              
              
              
    
    /*
    //Runs the script in its own thread so the main loop is not blocked
    public static void executeScript(final String script) {
        if (!scriptRunning.compareAndSet(false, true)) {
            Main.txText += "A script is already running, try again later\n";
            return;
        }
        Thread scriptThread = new Thread(() -> {
            try {
                runScript(script);
            } finally {
                scriptRunning.set(false);
            }
        }, "ScriptRunner");
        scriptThread.setDaemon(true);
        scriptThread.start();
    }

    private static void runScript(String script) {
        outBuffer = new StringBuilder(200);
        errBuffer = new StringBuilder(200);
        
        if (!RMsgUtil.isScriptInSafeDirectory(script, Main.dirScripts)) {
            Main.txText += "Can't run " + script + ", it must be in the Scripts directory of .pskmail";
            return;
        }
        
       
        String fullScriptPath = Main.homePath + Main.dirPrefix + Main.dirScripts + Main.separator + script;
        ProcessBuilder processBuilder = new ProcessBuilder(fullScriptPath);     
        try {
            ProcessBuilder scriptProcessBuilder = new ProcessBuilder();
            if (Main.onWindows) {
                processBuilder.command(fullScriptPath);
                ScriptProcess = scriptProcessBuilder.start();
                //Launching using cmd.exe creates a subprocess for the script that is un-killable from Java
                //fldigiProc = Runtime.getRuntime().exec(fldigiPath);
            } else { //Linux
                processBuilder.command("bash", "-c", fullScriptPath);
                ScriptProcess = processBuilder.start();
            }
            //Prepare a thread to consume the Standard Output Stream
            Thread outputThread = new Thread(()
                    -> readStream(ScriptProcess.getInputStream(), "STDOUT")
            );
            //Prepare a thread to consume the Standard Error Stream
            Thread errorThread = new Thread(()
                    -> readStream(ScriptProcess.getErrorStream(), "STDERR")
            );
            //Start both threads running in the background
            outputThread.start();
            errorThread.start();
            //Wait for the external process to finish
            int exitCode = ScriptProcess.waitFor();
            //Wait for the reading threads to finish emptying the buffers
            outputThread.join();
            errorThread.join();
            //Process the result
            System.out.println("\nProcess exited with code: " + exitCode);
            String errString = errBuffer.toString();
            //150 characters hard limit
            if (errString.length() > 150) errString = errString.substring(0, 150) + "...";
            String outString = outBuffer.toString();
            //150 characters hard limit
            if (outString.length() > 150) outString = outString.substring(0, 150) + "...";
            if (!errString.isEmpty()) outString += "\n With error: " + errString;
            Main.txText += script + " says:\n" + outString;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (!errBuffer.toString().isEmpty()) {
                Main.txText += "Error running " + script + ": " + errBuffer.toString();
            }
            e.printStackTrace();
        }
    }

  */      
    // Helper method to read a stream line-by-line
    public static void readStream(InputStream inputStream, String streamType) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Route or process the data differently depending on the source stream
                if ("STDERR".equals(streamType)) {
                    errBuffer.append(line).append("\n");
                    System.err.println("[" + streamType + "] " + line);
                } else {
                    outBuffer.append(line).append("\n");
                    System.out.println("[" + streamType + "] " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + streamType + " stream: " + e.getMessage());
        }
    }



} // end of class

