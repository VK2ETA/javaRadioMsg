/*
 * Main.java
 * 
 * Copyright (C) 2008 Pär Crusefalk and Rein Couperus
 * Copyright (C) 2018-2025 Pskmail Server and RadioMsg sections by John Douyere (VK2ETA) 
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
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import java.util.Random;
import java.util.TimeZone;
import javax.swing.JFrame;

/**
 *
 * @author per
 */
public class Main {

    //VK2ETA: Based on "jpskmail 1.7.b";
    static String version = "3.4.0.0";
    static String application = "javaRadioMsg " + version;// Used to preset an empty status
    static String versionDate = "20260618";
    static String host = "localhost";
    static int port = 7322; //ARQ IP port
    static String xmlPort = "7362"; //XML IP port
    static boolean modemTestMode = false; //For when we check that Fldigi is effectively running as expected
    static long lastModemCharTime = 0L;
    static boolean requestModemRestart = false;
    static int modemAutoRestartDelay = 0; //In minutes, 10080 = 7 days = once per week by default
    public static boolean justReceivedRSID = false;
    static boolean onWindows = true;
    static String modemPreamble = "";  // String to send before each frame
    static String modemPostamble = ""; // String to send after each frame
    static ModemModesEnum linkmode = ModemModesEnum.PSK500R;
    static int modemnumber = 0;
    static ModemModesEnum defaultmode = ModemModesEnum.PSK500R;
    static String currentModemProfile = "0";
    static int sending_link = 5;
    static int sending_beacon = 0;
    static boolean compBeacon = true;
    static String homePath = "";
    static String installPath = "";
    static String dirPrefix = "/.pskmail/";
    static String separator = "/";
    //RadioMsg directories
    static final String dirInbox = "RadioMsgInbox";
    static final String dirArchive = "RadioMsgArchive";
    static final String dirSent = "RadioMsgSentbox";
    //static final String DirTemp = "Temp";
    //static final String DirLogs = "Logs";
    static final String dirImages = "RadioMsg-Images";
    static final String dirSendingAPI = "RadioMsgSending";
    static final String messageLogFile = "RadioMsg.log";
    //
    static String mailOutFile = "";
    static File pending = null;
    static String pendingStr = "";
    static String pendingDir = "";
    static String outPendingDir = "";
    static String filetype = "";
    static String myfile = "";
    static String fileDestination = "";
    static File consolelog = null;
    static String logFile = "client.log";
    static String transactions = "";
    static boolean compressedmail = false;
    static boolean bulletinMode = false;
    static boolean iacMode = false;
    static boolean comp = false;
    static boolean debug = false;
    static String sendLine = "";
    static String sendCommand = "";
    static String telnethost = "";
    static String telnetport = "";
    static String userid = "";
    static String pass = "";
    static int DCD = 0;
    static int MAXDCD = 3;
    static boolean bypassDCD = false;
    //static int Persistence = 4;
    //static boolean BlockActive = false;
    static boolean EotRcved = false;
    static String txBlockLength = "5";
    static int maxBlocks = 8;
    static int RxBlockSize = 0;
    static int totalBytes = 0;
    static boolean TxActive = false;
    static boolean sendingAcks = false;
    static int second = 30;  // Beacon second
    static String[] modes = {"       ", "THOR8", "MFSK16", "THOR22",
        "MFSK32", "PSK250R", "PSK500R", "PSK500",
        "PSK250", "PSK63", "PSK125R", "MFSK64", "THOR11",
        "DOMINOEX5", "CTSTIA", "DOMINOEX22", "DOMINOEX11"};
    static String[] AltModes = {"       ", "THOR8", "MFSK16", "THOR22",
        "MFSK32", "PSK125R", "PSK250R", "PSK250", "PSK250", "CTSTIA"};
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    static String[] currentModes = modes;
    static String modesListStr = "8543"; // PSK250, PSK250R, MFSK32, THOR22
    static int bulletinTime = 0;
    static ModemModesEnum txModem = ModemModesEnum.PSK500R;
    static ModemModesEnum rxModem = ModemModesEnum.PSK500R;
    static ModemModesEnum[] modemArray;
    static String rxModemString = "PSK250R";
    static String defaultTxModem = "PSK250R";
    static String lastRxModem = "PSK250R";
    static String lastTxModem = "PSK250R";
    static boolean wantbulletins = true;

    // globals to pass info to gui windows
    static String monitorText = "";
    static boolean monitorMode = false;
    static boolean monmutex = false;
    static String mainwindow = "";
    static boolean mainmutex = false;
    static String msgWindow = "";
    static String mailHeadersWindow = "";
    static String filesTextArea = "";
    static String status = "Listening";
    static String statusLine = "";
    //static String Accu = "";
    static int statusLineTimer;
    static boolean txbusy = false;
    static boolean rxbusy = false;
    static boolean autolink = true;
    static int protocol = 1;
    static String protocolstr = "1";

    // globals for communication
    static String icon;
    static String ICONlevel;
    static int aprsMessageNumber;
    static String aprsServer = "netherlands.aprs2.net";
    static String aprsCall = "";
    static String mycall;     // mycall from options
    static String myserver;    // myserver from options
    static String myserverpassword; //For mini-server requiring password for access
    static String ttyCaller;     // TTY caller
    static String ttyConnected = "";
    static String ttyModes = "6";
    static String Motd = "";
    static boolean disconnect = false;
    static long Systime;
    static int DCDthrow;
    //RxDelay is the measured delay between the return to Rx of the server and the end of the RSID tx by the client
    static final double initialRxDelay = 1.0f;//Initial 1 seconds delay of RX just in case
    static double rxDelay = initialRxDelay;
    static double rxDelayCount = initialRxDelay;
    static double RadioMsgAcksDelay = 0.0f; //Time to hear all acks in the group (Max number of Acks settings)
    static String connectsecond;
    static long oldtime = 0L;
    static int missedBlocks = 0;
    static long blockval = 0; //msec 
    static int charval = 0; //msecs
    static int chartime = 0;
    static int blocktime; // seconds
    static int idlesecs = 0;
    static String lastBlockExchange = "  ";
    static long lastSessionExchangeTime = 0;
    static boolean isDisconnected = false;
    static boolean connected = false;
    public static boolean connectingPhase = false; //True from connect request until receipt of greeting/Server info
    public static boolean Connecting = false; //True until first acknowledgment of server's connect ack
    static int connectingTime = 0;
    static boolean aborting = false;
    static boolean scanning = false;
    static boolean linked = false; // flag for link ack
    static String linkedServer = "";
    static int[][] rxData = new int[10][10];
    static String session = ""; // present session
    static String oldSession = "";
    static String[] sessions = new String[64];
    static boolean validBlock = true;
    static String myRxStatus = "   "; // last rx status
    static String txText; // output queue
    static int progress = 0;
    static String dataSize = "";
    final static int MAXNEWSERVERS = 100; //100 additional servers heard on the air
    static String[] serversArray = {""};
    static String[] serversPasswordArray = {""};
    static double avgTxMissing = 0;
    static double avgRxMissing = 0;
    static double hisS2n = 50;
    static double myS2n = 50;
    static double snr = 0.0;
    static String rxSnr = "";
    static double[] serversSnrArray = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    static String[] lastHeardArray = {"", "", "", "", "", "", "", "", "", ""};
    static int[] packetsRcved = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static String[] modesRcved = {"", "", "", "", "", "", "", "", "", ""};
    static int strength[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static int snr_db = 0;
    static int timeoutPolls = 0;
    static boolean justDowngradedRx = false;
    static boolean statusRcved = false;
    static final int maxNumberOfAcks = 5;
    static int numberOfAcks = maxNumberOfAcks;
    static int freqOffset = 1000;
    static int quality = 0;
    static int sql = 30;
    //VK2ETA static final int SQL_FLOOR = 1;
    static final int SQL_FLOOR = 0;
    static String statusText = "";
    public static boolean exitingSoon = false;
    //static boolean stxflag = false;

    // Positions
    static String[][] positionsArray = new String[100][5];
    // GPS handles
    static GpsSerialPort gpsPort;    // Serial port object
    static NmeaParser gpsData;    // Parser for nmea data
    static public boolean isCwFrame = false; //Denotes a CW beacon or email frame (special QSL)
    // gpsd data
    static boolean haveGPSD = false;
    static boolean wantGpsd = false;
    static boolean newGPSD = false;
    static boolean wantRigctl = false;
    static boolean wantScanner = false;
    static boolean scanEnabled = true;
    static String currentFreq = "0";
    static String serverFreq = "0";
    static String freqStore = "0";
    static boolean summoning = false;
    static String gpsdLatitude = "";
    static String gpsdLongitude = "";
    static Socket gpsdSocket = null;
    static PrintWriter gpsdOut = null;
    static BufferedReader gpsdIn = null;
    static String[] gpsdDataArray = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    static long t1 = System.currentTimeMillis();
    static boolean wantIgate = false;
    //Pskmail server and RadioMsg 
    static boolean wantServer = false;
    static boolean wantRelayOverRadio = false;
    static boolean wantRelayEmails = false;
    static boolean wantRelayEmailsImmediat = false;
    static boolean wantRelaySMSs = false;
    static boolean wantRelaySMSsImmediat = false;
    //Time at which to re-start scanning if we disabled it by command over the air
    static long restartScanAtEpoch = 0L;

    static String callsignAsServer = "";
    static String relayingPassword = "";
    static String IotAccessPassword = "";

    //crypto
    static String strkey = "1234";
    static String sessionPasswrd = "password";
    static String hisPubKey = "";
    static Crypt cr = null;
    static String serverVersion = "1.1";
    static double sversion = 1.1;

    static Session sm = null;
    static String aprsBeaconText = "";
    static boolean serverBeacon = false;

    static String xmlRpcURL = "http://127.0.0.1:7362/RPC2";

    // arq object
    static Arq q;
    // Config object
    public static Config configuration; // Static config object
    // Error handling and logging object
    static LoggingClass log;

    // Our main window
    static MainPskmailUi mainui;

    // Modem handle
    static public Modem m;
    static String rxModemIndex = "";

    // File handles
    static FileWriter bulletin = null;
    static FileReader hdr = null;
    // DCD
    static String DCDstr;
    // APRS server socket
    static AprsMapSocket mapSock;
    static boolean aprsServerEnabled = true;
    static Integer aprsServerPort = 8063;
    static Amp2 Bul;

    //Radio Messages variables
    static public boolean receivingRadioMsg = false;
    static public long possibleRadioMsg = 0L; //Time at which we just an RSID, or an SOH, a Radio message is possible.
    static public String lastRsidReceived = ""; //Last RSID received from modem (string)
    static public long lastRsidTime = 0L;//Time of last RSID received from Modem (for time sync)
    static public Long deviceToRefTimeCorrection = 0L; //The number of seconds this device's clock is late compared to reference time (server or GPS)
    static public String refTimeSource = ""; //Where is the time reference coming from: remote station call sign or "GPS"
    static public boolean radioMsgWorking = false; //Radiomsg processing emails or web pages - do not scan
    static public String fileNameString = "";
    static public long lastCharacterTime = 0L;//Time of last character received from Modem
    static public boolean haveSOH = false; //We have received an SOH
    static public boolean receivedRsidAck = false; //When we receive and "EOL" RSID we interpret it as a positive ACK

    /**
     * @param args the command line arguments
     */
    //@SuppressWarnings("empty-statement")
    public static void main(String[] args) throws InterruptedException {

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // handle exception
//       System.out.println("Problem setting look and feel.");
        }

        // Create error handling class
        log = new LoggingClass("jpskmail.log");
        
        try {
            String Blockline = "";

            // Call the folder handling method
            handlefolderstructure();

            // Create config object
            configuration = new Config(homePath + dirPrefix);

            host = configuration.getPreference("MODEMIP");

            // Get settings and initialize
            handleinitialization();
            /* Moved modem init to after gui to allow for auto launch of Fldigi without slowing 
            the gui down and allows for a message to be displayed while fldigi is launched
            // start the modem thread
            System.out.println("About to create new Modem.");
            m = new Modem(host, port);
            Thread myThread = new Thread(m);
            // Start the modem thread
            myThread.setDaemon(true);
            System.out.println("Launching modem thread.");            
            myThread.start();
             */
            // Make arq object
            //System.out.println("About to create new ARQ.");            
            q = new Arq();

            q.Message(version, 10);

            Bul = new Amp2();
            Bul.init();
            Thread.sleep(1000);

            if (wantGpsd & !onWindows) {
                handlegpsd();
            }

            // Handle GPS
            if (!haveGPSD) {
                handlegps();
            }
            // Make session object
            //System.out.println("About to create new Session.");            
            sm = new Session();  // Session, class

            // Show the main window (center screen)
            //System.out.println("about to new mainpskmailui");
            mainui = new MainPskmailUi();
            //System.out.println("about to pack");
            mainui.pack();
            //System.out.println("about to setLocationRelativeTo");
            mainui.setLocationRelativeTo(null); // position in the center of the screen
            //System.out.println("about to setDefaultCloseOperation");
            mainui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            //System.out.println("about to setVisible");
            mainui.setVisible(true);
            //mainui.disableMboxMenu();

            // start the modem thread
            //System.out.println("About to create new Modem.");
            m = new Modem(host, port);
            Thread myThread = new Thread(m);
            // Start the modem thread
            myThread.setDaemon(true);
            //System.out.println("Launching modem thread.");
            myThread.start();
            myThread.setName("Modem");

            //vk2eta debug
            //System.out.println("Starting UI timers");
            //VK2ETA locking up at startup when heavy CPU load. Solution: Delayed timer's
            //  start until after main gui init to avoid firing gui actions before it is initilised.
            //Ok to start UI timers here as the events run on the EDT thread and coalesces the 
            //  events if the load is too high
            mainui.u.start();
            mainui.timer200ms.start();
            //System.out.println("Timers started");

            // Start the aprs server socket
            mapSock = new AprsMapSocket();
            mapSock.setPort(aprsServerPort);
            mapSock.setPortopen(aprsServerEnabled);
            mapSock.start();
            mapSock.setName("AprsMapSock");

            // init modemarray
            modemArray = m.pmodes;

            // init rxdata array
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 0; j++) {
                    rxData[i][j] = 0;
                }
            }

            // init random generator for DCD
            Random generator = new Random();

            //Always have RXid ON so that TTY connects and igates beacons can be heard on any mode
            m.setRxRsid("ON");
            
            // Main  loop
            //m.setRxRsid("ON");
            //q.send_txrsid_command("ON");
            //Launch separate thread to monitor and relay incoming emails and messages if required
            //System.out.println("About to call startemail");
            RMsgProcessor.startEmailsAndSMSsMonitor();
            //System.out.println("Returned from startemail");
            
            //File Sending API
            if (configuration.getPreference("MONITORFILESFOLDER", "no").equals("yes")) {
                RMsgProcessor.startFilesMonitor();
            }
            
            //Start MQTT connection to broker if selected
            MeshGateway myMesh = new MeshGateway();
            
            //vk2eta debug
            //System.out.println("entering receive loop");
            while (true) {
                //Wait for return to Rx if we are transmitting
                while (TxActive) {
                    Thread.sleep(50);
                    //Check if we are stuck on transmit (jPskmail or Fldigi or interface issue)
                    if (m.expectedReturnToRxTime > 0L && System.currentTimeMillis() > m.expectedReturnToRxTime) {
                        //Reset loop by killing and re-launching fldigi (brute force for now until the root cause is identified)
                        m.expectedReturnToRxTime = 0L; //Consume event
                        m.killFldigi();
                        //Main.log.writelog("Locked in TX for too long, restarting Fldigi.", null, true);
                        System.out.println("Locked in TX for too long, restarting Fldigi.");
                    }
                }
                // Send a command to the modem ?
                //VK2ETA not through SendCommand anymore (Mutex)
                m.Sendln(sendCommand);
                Thread.sleep(50);
                sendCommand = "";
                //Handle RadioMsg messages only when fully idle
                if (sendLine.length() == 0 & !Main.TxActive
                        & !connected & !Connecting & !aborting
                        & ttyConnected.equals("") & !receivingRadioMsg
                        & !sendingAcks & possibleRadioMsg == 0L & rxDelayCount < 0.1f) {
                    if (RMsgTxList.getAvailableLength() > 0) {
                        Main.TxActive = true; //Moved up to prevent change in mode when replying
                        m.txMessage = RMsgTxList.getOldest();
                        //Set Mode
                        //SendCommand += "<cmd><mode>" + m.txMessage.rxMode + "</mode></cmd>";
                        //m.Sendln(SendCommand);
                        //SendCommand = "";
                        //Thread.sleep(250);
                        //Set TX Rsid
                        //m.requestTxRsid("ON");
                        //m.Sendln(SendCommand);
                        //SendCommand = "";
                        //Thread.sleep(100);
                        //Send message
                        //Sendline = "\n\n" + m.txMessage.formatForTx(false) + "\n"; //No CCIR modes for now
                        String toSend = "\n\n" + m.txMessage.formatForTx(false) + "\n"; //No CCIR modes for now
                        //Save date-time of start of sending in UTC timezone
                        Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        RMsgProcessor.FileNameString = String.format(Locale.US, "%04d", c1.get(Calendar.YEAR)) + "-"
                                + String.format(Locale.US, "%02d", c1.get(Calendar.MONTH) + 1) + "-"
                                + String.format(Locale.US, "%02d", c1.get(Calendar.DAY_OF_MONTH)) + "_"
                                + String.format(Locale.US, "%02d%02d%02d", c1.get(Calendar.HOUR_OF_DAY),
                                        c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND)) + ".txt";
                        //Load the RadioMsgAcksDelay 
                        RadioMsgAcksDelay = (double)Main.m.delayUntilMaxAcksHeard() / 1000.0f; //in seconds
                        m.Sendln(toSend, m.txMessage.rxMode, "ON"); //Tx Rsid ON
                        //Log in monitor screen
                        Main.monitorText += "\n*TX*  " + "<SOH>"
                                + toSend.replace(Character.toString((char) 1), "")
                                        .replace(Character.toString((char) 4), "") + "<EOT>";
                        //Sendline = "";
                    }
                }
                //Always reset the radioMsgWorking flag regardless (allows for change of frequency/mode)
                radioMsgWorking = false;     
                // See if tx active and DCD is off and we have exhausted the extra reception delay
                if (sendLine.length() > 0 & !TxActive & (DCD == 0 || bypassDCD) & rxDelayCount < 0.1f) {
                    //Reset DCD bypass (on-demand usage)
                    bypassDCD = false;
                    //System.out.println("MAIN2:" + Sendline);
                    //VK2ETA DCDthrow not used 
                    //DCDthrow = generator.nextInt(Persistence);
                    //     System.out.println("DCD:" + DCDthrow);               
                    if (connected | Connecting | aborting | !ttyConnected.equals("")) {
                        //We are in some session as client or server
                        if (aborting) {
                            aborting = false;
                        }
                    } else {
                        //Reset DCD for next round if we are not in a session
                        MAXDCD = Integer.parseInt(configuration.getPreference("DCD", "0"));
                    }
                    //VK2ETA DCDthrow not used 
                    //if (DCDthrow == 0) {
                    //      System.out.println("MAIN3:" + Sendline); 
                    String Sendline_cp = sendLine;
                    try {
                        //       System.out.println("MAIN4:" + Sendline_cp);                   
                        int TxDelay = 0;
                        String TxDelayStr = configuration.getPreference("TXDELAY", "0");
                        //System.out.println("TXDELAY:" + TxDelayStr);  
                        if (TxDelayStr.length() > 0) {
                            TxDelay = Integer.parseInt(TxDelayStr);
                        }
                        if (TxDelay > 0) {
                            Thread.sleep(TxDelay * 1000);
                        }
                        Main.TxActive = true;
                        // System.out.println("MAIN5" );                        
                        //  Add a 2 seconds delay when mode is MFSK16 (1 sec for MFSK32) to prevent overlaps as
                        //  the trail of MFSK is very long
                        //if (Main.RxModem.equals(modemmodeenum.MFSK16)) {
                        //    Thread.sleep(2000);
                        //} else if (Main.RxModem.equals(modemmodeenum.MFSK32)) {
                        //    Thread.sleep(1000);
                        //}
                        //Try to stop Fldigi locking up by having a delay between the mode change and the data
                        //  block as Fldigi needs to re-initialize the modem at each mode change
                        //String SendMode = "<cmd><mode>" + m.getTXModemString(TxModem) + "</mode></cmd>";
                        String SendMode = m.getTXModemString(txModem);
                        if (q.txserverstatus == TxStatus.TXCWACK || q.txserverstatus == TxStatus.TXCWNACK) {
                            SendMode = ModemModesEnum.CW.toString();
                        }
                        //System.out.println("TXMODEM for connect:" + m.getTXModemString(TxModem));
                        //       System.out.println("MAIN6:" + Sendline_cp);
                        //m.Sendln(SendMode);
                        //Thread.sleep(250);
                        m.Sendln(Sendline_cp, SendMode, "");
                        Sendline_cp = "";
                        sendLine = "";
                    } catch (Exception e) {
                        Main.monitorText += ("\nModem problem. Is fldigi running?");
                        log.writelog("Modem problem. Is fldigi running?", e, true);
                    }
                    //} else {
                    //    if (!Connected & !Connecting) {
                    //        DCD = MAXDCD;
                    //    }
                    //}
                    //} else {
                    //    if (!Connected & !Connecting) {
                    //        DCD = MAXDCD;
                    //    }
                    //}
                }
                // receive block
                try {
                    if (m.checkBlock()) {

                        Blockline = m.getMessage();
                        // System.out.println("BLOCK=" + Blockline);
                        RxBlock rxb = new RxBlock(Blockline);
                        if (!rxb.valid && !rxb.validWithPW) {
                            validBlock = false;
                        } else {
                            validBlock = true;
                        }
                        if (validBlock & monitorMode) {
                            char c = Blockline.charAt(6);
                            int i = (int) c - 32;
                            String calls = "";
                            if (i < 64) {
                                calls = sessions[i];
                                if (calls != null) {
                                    mainui.appendMainWindow(mainui.getClock() + " " + calls + Blockline + "\n");
                                } else {
                                    mainui.appendMainWindow(mainui.getClock() + " " + Blockline + "\n");
                                }
                            }
                        }
                        if (!bulletinMode & !iacMode) {
                            if (connected) {
                                // status block from server
                                if (rxb.type.equals("s")
                                        & rxb.valid & rxb.session.equals(session)) {
                                    oldSession = session;
                                    idlesecs = 0;      // reset idle timer
                                    Main.timeoutPolls = 0; // Reset timeout polls count
                                    // set blocktime for idle time measurement...
                                    if (Blockline.length() > 8) {
                                        charval = (int) (blockval / (Blockline.length() - 4)); // msec
                                        //Must account for Client's TX delay, RSID and silences, FEC/interleaver delay, decoding delay
                                        //Done at connect time, should be enough 
                                        //blocktime = (charval * 64 / 1000) + m.firstCharDelay;
                                    }
                                    //Move processing of block before decision on mode upgrade
                                    sm.RXStatus(rxb.payload);   // parse incoming status packet
                                    // get the tx status
                                    myRxStatus = sm.getTXStatus();
                                    if (!lastBlockExchange.equals(sm.getBlockExchanges())) {
                                        lastSessionExchangeTime = System.currentTimeMillis() / 1000;
                                        lastBlockExchange = sm.getBlockExchanges();
                                    }
                                    // set the modem type for TX if client. For TTY server, adjust TX mode based on received s2n from TTY client.
                                    //Common data needed for later
                                    String pbyte = rxb.protocol;
                                    char pchr = pbyte.charAt(0);
                                    int pint = (int) pchr;
                                    if (ttyConnected.equals("Connected")) {
                                        //Auto speed/mode adjustment
                                        //I am a TTY server (protocol byte = quality of receive by client)
                                        //Turn RXid and TXid OFF as I am a server and I received a good "s" block
                                        m.setRxRsid("OFF");
                                        //Exception is for frequency sensitive modes like MFSK16, MFSK8, DOMINOEX5
                                        //VK2ETA wrong location as it can get reset with status requests
                                        if (Main.txModem == ModemModesEnum.MFSK16
                                                || Main.txModem == ModemModesEnum.MFSK8
                                                || Main.txModem == ModemModesEnum.DOMINOEX5) {
                                            m.requestTxRsid("ON");
                                            //} else {
                                            //    q.send_txrsid_command("OFF");
                                        }
                                        //Adjust my TX mode (as a server) AND the Client's TX modes
                                        pint = (pint - 32) * 100 / 90;
                                        hisS2n = decayaverage(hisS2n, pint, 2);
                                        //Get current RX modem position in table
                                        int currentmodeindex = 0;
                                        //Should I upgrade/downgrade my TX mode?
                                        //Am I repeating the same blocks over and over again (and I am not idle)
                                        if (Session.tx_missing.length() > 0
                                                && Session.lastTx_missing.equals(Session.tx_missing)) {
                                            Session.sameRepeat++;
                                        } else {
                                            Session.sameRepeat = 0;
                                            Session.lastTx_missing = Session.tx_missing;
                                        }
                                        avgTxMissing = decayaverage(avgTxMissing, Session.tx_missing.length(), 2);
                                        if (avgTxMissing > 3 || Session.sameRepeat > 2) {
                                            //Downgrade Tx mode
                                            currentmodeindex = getClientModeIndex(Main.txModem);
                                            if (currentmodeindex < ttyModes.length() - 1) { //List in decreasing order of speed
                                                Main.txModem = getClientMode(currentmodeindex + 1);
                                                m.requestTxRsid("ON");
                                                sm.SetBlocklength(5); //restart with medium block length
                                                justDowngradedRx = false; // Make RX mode downgrade first (if necessary)
                                            }
                                            //Reset link quality indicators
                                            avgTxMissing = 0;
                                            Main.hisS2n = 50; //Reset to mid-range
                                        } else {
                                            if ((hisS2n > 85) & (avgTxMissing < 1)) {
                                                //Upgrade Rx speed
                                                currentmodeindex = getClientModeIndex(Main.txModem);
                                                if (currentmodeindex > 0) { //List in decreasing order of speed
                                                    Main.txModem = getClientMode(currentmodeindex - 1);
                                                    m.requestTxRsid("ON");
                                                    sm.SetBlocklength(4); //restart with small block length
                                                    justDowngradedRx = true; // Make TX mode downgrade first (if necessary)
                                                }
                                                Main.hisS2n = 50; //Reset to mid-range
                                            }
                                        }
                                        //Should I upgrade the client's TX mode?
                                        myS2n = decayaverage(myS2n, Main.snr, 2);
                                        currentmodeindex = 0;
                                        avgRxMissing = decayaverage(avgRxMissing, Session.rx_missing.length(), 2);
                                        if (avgRxMissing > 3) {
                                            //Downgrade Rx speed
                                            currentmodeindex = getClientModeIndex(Main.rxModem);
                                            if (currentmodeindex < ttyModes.length() - 1) { //List in decreasing order of speed
                                                Main.rxModem = getClientMode(currentmodeindex + 1);
                                                Main.rxModemString = m.getModemString(Main.rxModem);
                                                blocktime = m.getBlockTimeAndDelay(Main.rxModem);
                                                justDowngradedRx = true; // Make TX mode downgrade first (if necessary)
                                            }
                                            //Reset link quality indicators
                                            avgRxMissing = 0;
                                            Main.myS2n = 50; //Reset to mid-range
                                        } else {
                                            if ((Main.myS2n > 85) & (avgRxMissing < 1)) {
                                                //Upgrade Rx speed
                                                currentmodeindex = getClientModeIndex(Main.rxModem);
                                                if (currentmodeindex > 0) { //List in decreasing order of speed
                                                    Main.rxModem = getClientMode(currentmodeindex - 1);
                                                    Main.rxModemString = m.getModemString(Main.rxModem);
                                                    blocktime = m.getBlockTimeAndDelay(Main.rxModem);
                                                    justDowngradedRx = false; // Make RX mode downgrade first (if necessary)
                                                }
                                                Main.myS2n = 50; //Reset to mid-range
                                            }
                                        }
                                    } else { //I am a client (protocol byte = my TX mode)
                                        //Turn RXid ON as I am a client
                                        m.setRxRsid("ON");
                                        pint = (int) pchr - 48;
                                        if (pint < 9 & pint > 0) {
                                            txModem = m.getModeOffList(pint);
                                            if (currentModemProfile.equals("0")) {
                                                txModem = rxModem;
                                            }
                                        } else if (pint == 0) {
                                            txModem = rxModem;
                                        }
                                    }
                                    //Still data to send or mising block to resend?
                                    if (Session.tx_missing.length() > 0 | Main.txText.length() > 0) {
                                        String outstr = sm.doTXbuffer();
                                        q.send_data(outstr);
                                    } else {
                                        myRxStatus = sm.getTXStatus();
                                        q.send_status(myRxStatus);  // send our status
                                    }
                                    //Reset STOP command flag, provided the server has received our STOP command
                                    if (sm.justSentStopCmd) {
                                        sm.checkStopFlag();
                                    }
                                    Main.validBlock = true;
                                } else if (rxb.type.equals("p")
                                        & rxb.valid & rxb.session.equals(session)) {
                                    sm.RXStatus(rxb.payload);   // parse incoming status packet

                                    myRxStatus = sm.getTXStatus();
                                    q.send_status(myRxStatus);  // send our status
                                    Main.txbusy = true;
                                    //Disconnect request
                                } else if (rxb.type.equals("d") & (rxb.session.equals(session) | rxb.session.equals("0"))) {
                                    status = "Listening";
                                    q.Message(javaradiomsg.MainPskmailUi.mainpskmailui.getString("Disconnected_from_Server"), 10);
                                    connected = false;
                                    mainui.disableMboxMenu();
                                    mainui.enableMnuPreferences2();
                                    session = "";
                                    totalBytes = 0;
                                    sm.FileDownload = false;
                                    comp = false;
                                    try {
                                        if (sm.pFile != null) {
                                            sm.pFile.close();
                                        }
                                    } catch (IOException e) {
                                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                                    }
//                                q.send_rsid_command("OFF");
                                    // ident block
                                } else if (rxb.session.equals(session) & rxb.type.equals("i")) {
                                    // discard
                                    // info block
                                } else if (rxb.valid & rxb.session.equals(session)) {
                                    myRxStatus = sm.doRXBuffer(rxb.payload, rxb.type);
                                } else if (rxb.session.equals(session)) {
                                    myRxStatus = sm.doRXBuffer("", rxb.type);
                                }
                                // PI4TUE 0.9.33-13:28:52-IM46>
                                if (Blockline.toUpperCase(Locale.US).contains(q.getServer().toUpperCase(Locale.US))) {
                                    //Pattern ppc = Pattern.compile(".*(\\d\\.\\d).*\\-\\d+:\\d+:(\\d+)\\-(.*)M(\\d+)");
                                    Pattern ppc = Pattern.compile(".*\\S+\\s\\S+\\s(\\S{3}).*\\-\\d+:\\d+:(\\d+)\\-(.*)M(\\d+)");
                                    //System.out.println(Blockline);
                                    Matcher mpc = ppc.matcher(Blockline);
                                    connectsecond = "";
                                    String localmail = "";
                                    if (mpc.lookingAt()) {
                                        serverVersion = mpc.group(1);
                                        sm.serverversion = mpc.group(1);
                                        sversion = Double.parseDouble(serverVersion);
//     System.out.println(sversion);
                                        connectsecond = mpc.group(2);
                                        localmail = mpc.group(3);
                                        if (localmail.contains("L")) {
                                            mainui.enableMboxMenu();
                                        }
                                        if (sversion > 1.1) {
                                            //     System.out.println("success");
                                            sm.hispubkey = mpc.group(4);
                                            hisPubKey = sm.hispubkey;

                                            cr = new Crypt();

                                            String output = cr.encrypt(sm.hispubkey, sessionPasswrd);

                                            Main.txText += "~Mp" + output + "\n";
                                            //   System.out.println(Main.TX_Text);                                        
                                        }
                                    } else {
                                        //Mini-server connection, "Hi" message
                                        Pattern pps = Pattern.compile(".*" + q.getServer() + " V(\\d{1,2}\\.\\d{1,2}\\.\\d{1,2})(.\\d{1,2}){0,1}, Hi.*", Pattern.CASE_INSENSITIVE);
                                        //System.out.println(Blockline);
                                        Matcher mps = pps.matcher(Blockline);
                                        if (mps.lookingAt()) {
                                            Main.connectingPhase = false;
                                            mainui.setMenuForJavaServer();
                                        }
                                    }
                                }
                                //End if (connected)
                            } else {
                                //NOT connected
                                //if (Blockline.contains("QSL") & Blockline.toUpperCase(Locale.US).contains(q.callsign.toUpperCase(Locale.US))) {
                                if (Blockline.contains("QSL") & Blockline.contains(" de ")) {
                                    String pCheck = "";
                                    //Pattern psc = Pattern.compile(".*de ([A-Z0-9\\-]+)\\s(?:(\\d*)|((\\d+)\\s+(\\d+))\\s)([0123456789ABCDEF]{4}).*");
                                    Pattern psc = Pattern.compile(".*QSL(\\s[A-Za-z0-9\\-\\/]+)? de ([A-Za-z0-9\\-\\/]+)\\s*(((\\d+\\s)([\\-0-9]+\\s))|(\\d+\\s))?([0123456789ABCDEF]{4}).*");
                                    Matcher msc = psc.matcher(Blockline);
                                    String scall = "";
                                    rxSnr = "";
                                    String numberOfMails = "";
                                    if (msc.lookingAt()) {
                                        scall = msc.group(2);
                                        if (msc.group(7) != null) {
                                            rxSnr = msc.group(7).trim();
                                        } else {
                                            rxSnr = msc.group(5).trim();
                                            numberOfMails = msc.group(6).trim();
                                        }
                                        pCheck = msc.group(8);
                                    }
                                    // fill the servers drop down list
                                    char soh = 1;
                                    String sohstr = Character.toString(soh);
                                    String checkstring = "";
                                    if (rxSnr.equals("")) {
                                        checkstring = sohstr + "00uQSL " + q.callsign + " de " + scall + " ";
                                    } else if (!rxSnr.equals("") && !numberOfMails.equals("")) {
                                        checkstring = sohstr + "00uQSL " + q.callsign + " de " + scall + " " + rxSnr + " " + numberOfMails + " ";
                                        //System.out.println("RX_SNR:" + rx_snr);
                                        mainui.appendMainWindow("QSL from " + scall + ": " + rxSnr + "%, " + numberOfMails + " mails\n");
                                        setrxdata(scall, Integer.parseInt(rxSnr));
                                    } else if (!rxSnr.equals("") && numberOfMails.equals("")) {
                                        checkstring = sohstr + "00uQSL " + q.callsign + " de " + scall + " " + rxSnr + " ";
                                        //System.out.println("RX_SNR:" + rx_snr);
                                        mainui.appendMainWindow("QSL from " + scall + ": " + rxSnr + "%\n");
                                        setrxdata(scall, Integer.parseInt(rxSnr));
                                    }
                                    String check = q.checksum(checkstring);
                                    if (check.equals(pCheck)) {
                                        rxb.get_serverstat(scall);
                                        int i = 0;
                                        boolean knownserver = false;
                                        for (i = 0; i < serversArray.length; i++) {
                                            //                              System.out.println(Servers[i] + scall);
                                            if (scall.equals(serversArray[i])) {
                                                knownserver = true;
                                                break;
                                            }
                                        }
                                        if (!knownserver) {
                                            mainui.addServer(scall); // add to servers drop down list
                                        }
                                    }
                                } else if (Blockline.contains(":71 ")) { //Inquire (SNR) request
                                    Pattern psc = Pattern.compile(".*00u(\\S+):71\\s(\\d*)\\s([0123456789ABCDEF]{4}).*");
                                    Matcher msc = psc.matcher(Blockline);
                                    String scall = "";
                                    String pCheck = "";
                                    rxSnr = "";
                                    if (msc.lookingAt()) {
                                        scall = msc.group(1);
                                        rxSnr = msc.group(2);
                                        pCheck = msc.group(3);
                                    }
                                    // fill the servers drop down list
                                    String checkstring = "";
                                    if (!rxSnr.equals("")) {
                                        checkstring = "00u" + scall + ":71 " + rxSnr + " ";
                                        //                                       System.out.println("RX_SNR:" + rx_snr);
                                        mainui.appendMainWindow("From " + scall + ": " + rxSnr + "%\n");
                                        setrxdata(scall, Integer.parseInt(rxSnr));
                                    } else {
                                        checkstring = "00u" + scall + ":71 ";
                                    }
                                    String check = q.checksum(checkstring);
                                    if (check.equals(pCheck)) {
                                        rxb.get_serverstat(scall);
                                        // switch off txrsid
                                        //                                      q.send_txrsid_command("OFF");
                                    } else {
//                                        System.out.println("check not ok.\n");
                                    }
                                } else if (Blockline.contains(":91 ")) {
                                    //We have a time sync response from a server?
                                    Pattern psc = Pattern.compile(".*00u(\\S+)\\s(\\S+):91\\s(\\d{6})\\s([0-9A-F]{4}).*");
                                    Matcher msc = psc.matcher(Blockline);
                                    String scall = "";
                                    String toCall = "";
                                    String pCheck = "";
                                    String timeRefStr = "";
                                    if (Main.lastRsidTime > 0L && msc.lookingAt()) {
                                        scall = msc.group(1);
                                        toCall = msc.group(2);
                                        timeRefStr = msc.group(3);
                                        pCheck = msc.group(4);
                                        // fill the servers drop down list
                                        String checkstring = "";
                                        checkstring = "00u" + scall + " " + toCall + ":91 " + timeRefStr + " ";
                                        //                                       System.out.println("RX_SNR:" + rx_snr);
                                        String check = q.checksum(checkstring);
                                        String serverCall = Main.configuration.getPreference("CALLSIGNASSERVER");
                                        if (check.equals(pCheck) && scall.length() > 1 && toCall.length() > 1 
                                                && toCall.toUpperCase(Locale.US).equals(serverCall.toUpperCase(Locale.US))) {
                                            //All good, update local time
                                            Long timeRef = Long.parseLong(timeRefStr);
                                            Long timeHere = Main.lastRsidTime / 1000 - ((Long) (Main.lastRsidTime / 1000000000L) * 1000000L); //Keep the last 6 digits representing seconds
                                            Long deltaTime = timeRef - timeHere;
                                            //Wrap around if we passed a 100000 seconds threshold
                                            if (Math.abs(deltaTime) > 99998L) {
                                                deltaTime = deltaTime < 0L ? deltaTime + 1000000L : deltaTime - 1000000L;
                                            }
                                            if (deltaTime == 0) {
                                                mainui.appendMainWindow("This device clock is the same as " + scall + "'s clock\n");
                                            } else if (deltaTime < 0) {
                                                mainui.appendMainWindow("This device clock is " + (-deltaTime) + " seconds in front of " + scall + "'s clock\n");
                                            } else {
                                                mainui.appendMainWindow("This device clock is " + deltaTime + " seconds behind " + scall + "'s clock\n");
                                            }
                                            //System.out.println("server: " + timeRefStr + ", Client: " + timeHere);
                                            //System.out.println("Server - client in Secs: " + deltaTime);
                                        }
                                    }
                                } else if (Blockline.contains(":26 ")) {
                                    // System.out.println(Blockline);
                                    //Uncompressed APRS Beacon or APRS message
                                    Pattern bsc = Pattern.compile(".*00u(\\S+):26\\s(.)(.*)(.)([0123456789ABCDEF]{4}).*");
                                    Matcher bmsc = bsc.matcher(Blockline);
                                    String scall = "";
                                    String binfo = "";
                                    if (bmsc.lookingAt()) {
                                        scall = bmsc.group(1);
                                        String type = bmsc.group(2);
                                        binfo = bmsc.group(3);
                                        String nodetype = bmsc.group(4);
                                        String pCheck = bmsc.group(5);
                                        binfo += nodetype;
                                        String checkstring = "00u" + scall + ":26 " + type + binfo;
                                        String check = q.checksum(checkstring);
                                        String outstring = "";
                                        if (check.equals(pCheck)) {
                                            //System.out.println("CHECKED, type=" + type);
                                            //Make callsigns upper case to meet APRS requirements
                                            //Returns a blank call if not a standard call
                                            scall = cleanCallForAprs(scall);
                                            if (scall.length() > 0 && type.equals("!")) {
                                                //Uncompressed APRS Beacon
                                                //E.g. <SOH>00uVK2ETA:26 !2700.00S/13300.00E.Test 1FDF9<EOT>
                                                //VK2ETA>PSKAPR,TCPIP*,qAC,T2SYDNEY:!2712.85S/15303.72E.test aprs 2
                                                outstring = scall + ">PSKAPR,TCPIP*:" + type + binfo;
//                                                System.out.println(outstring);
                                                boolean igateSendOk = Igate.write(outstring);
                                                // Push this to aprs map too
                                                mapSock.sendmessage(outstring);
                                                //If I run as server, send QSL
                                                if (Main.wantServer && igateSendOk) {
                                                    q.send_QSL_reply();
                                                }
                                                //record heard server stations?????
                                                if (nodetype.equals("&")) {
                                                    // is serverbeacon
                                                    serverBeacon = true;
                                                    int i;
                                                    boolean knownserver = false;
                                                    for (i = 0; i < serversArray.length; i++) {
                                                        if (scall.equals(serversArray[i])) {
                                                            //Already in list, exit
                                                            knownserver = true;
                                                            break;
                                                        }
                                                        if (!knownserver && serversArray[i].length() == 0) {
                                                            //Not known, add it at first blank spot
                                                            serversArray[i] = scall;
                                                            mainui.addServer(scall);
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else if (scall.length() > 0 && type.equals(":")) {
                                                //APRS message
                                                //System.out.println(type + binfo);
                                                Pattern gc = Pattern.compile("(\\S+)>PSKAPR.::(\\S+)\\s*:(.*)(\\{\\d+)");
                                                Matcher gmc = gc.matcher(type + binfo);
                                                if (gmc.lookingAt()) {
                                                    String outcall = gmc.group(2);
                                                    binfo = gmc.group(3);
                                                    String mnumber = gmc.group(4);
                                                    outstring = scall + ">PSKAPR,TCPIP*::" + outcall;
                                                    //     System.out.println("MSG:" + outstring);

                                                    String padder = "        ";
                                                    outstring += padder.substring(0, 8 - outcall.length());
                                                    outstring += ":";
                                                    outstring += binfo;
                                                    outstring += mnumber;
                                                    boolean igateSendOk = Igate.write(outstring);
                                                    // Push this to aprs map too
                                                    mapSock.sendmessage(outstring);
                                                    mainui.appendMainWindow(outstring);
                                                }
                                            } else if (scall.length() > 0) {
                                                //APRS message to another callsign
                                                // message PA0R-2:26 PA0R test
                                                //System.out.println("IS :" + Blockline);
                                                Pattern gm = Pattern.compile(".*00u(\\S+):26\\s(\\S+)\\s(.*)([0123456789ABCDEF]{4}).*");
                                                Matcher gmm = gm.matcher(Blockline);
                                                if (gmm.lookingAt()) {
                                                    //System.out.println("FOUND:" +  Blockline);
                                                    String fromcall = cleanCallForAprs(gmm.group(1));// + "         ";
                                                    //fromcall = fromcall.substring(0, 9);
                                                    String outcall = cleanCallForAprs(gmm.group(2).toUpperCase(Locale.US));
                                                    if (fromcall.length() > 0 && outcall.length() > 0) {
                                                        //Pad call to 9 characters and spaces
                                                        outcall = outcall + "         ";
                                                        outcall = outcall.substring(0, 9);
                                                        binfo = gmm.group(3);
                                                        if (!mycall.toUpperCase(Locale.US).equals(fromcall)) {
                                                            //Not for my Client's callsign (can be different to myserver's callsign)
                                                            String toxastir = gmm.group(2) + ">PSKAPR,TCPIP*,qAC," + gmm.group(1) + "::" + fromcall + "  " + ":" + gmm.group(3) + "\n";
                                                            mapSock.sendmessage(toxastir);
                                                            //test: VK2ETA>PSKAPR,TCPIP*::vk2eta-1 :test aprs 1
                                                            //VK2ZZZ>APWW11,TCPIP*,qAC,T2LUBLIN::VK2XXX-8 :Hello Jack Long time no see!{21}
                                                            String aprsmessage = fromcall + ">PSKAPR,TCPIP*::" + outcall + ":" + binfo;
                                                            boolean igateSendOk = Igate.write(aprsmessage);
                                                            //System.out.println(aprsmessage);
                                                            //If I run as server, send QSL
                                                            if (Main.wantServer && igateSendOk) {
                                                                q.send_QSL_reply();
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                            outstring = "";
                                        }
                                    }
                                } else if (Blockline.contains(":25 ")) {
                                    // System.out.println(Blockline);
                                    //Unproto email message
                                    Pattern bsc = Pattern.compile(".*00u(\\S+):25(\\s+)([\\w.-]+@\\w+\\.[\\w.-]+)(\\s+)(.+)\\n([0123456789ABCDEF]{4}).*");
                                    Matcher bmsc = bsc.matcher(Blockline); //
                                    String scall = "";
                                    String spaces1 = "";
                                    String email = "";
                                    String spaces2 = "";
                                    String body = "";
                                    if (bmsc.lookingAt()) {
                                        scall = bmsc.group(1);
                                        spaces1 = bmsc.group(2);
                                        email = bmsc.group(3);
                                        spaces2 = bmsc.group(4);
                                        body = bmsc.group(5);
                                        String pCheck = bmsc.group(6);
                                        String checkstring = "00u" + scall + ":25" + spaces1 + email + spaces2 + body + "\n";
                                        String check = q.checksum(checkstring);
                                        String checkWithPass = q.checksum(checkstring + relayingPassword);
                                        //Only authorized if the server is left open (without a password)
                                        // Otherwise use the RadioMsg app to send short messages
                                        //if (Main.WantServer && (Main.accessPassword.length() == 0 && check.equals(pCheck) 
                                        //        || Main.accessPassword.length() > 0 && checkWithPass.equals(pCheck))) {
                                        //Changed back to always open
                                        if (check.equals(pCheck)) {
                                            String subject = "Short email from " + scall;
                                            String resultStr = ServerMail.sendMail("", email, subject, body, ""); //last param is attachementFileName
                                            //If I run as server, send QSL
                                            if (resultStr.contains("Message sent")) {
                                                q.send_QSL_reply();
                                            } else {
                                                //Alert Server and Client
                                                Main.txText += resultStr;
                                            }
                                        }
                                    }
                                } else if (Blockline.contains(":6 ")) {
                                    //Compressed beacon
                                    Pattern cbsc = Pattern.compile(".*00u(\\S+):6\\s(.*)([0123456789ABCDEF]{4}).*");
                                    Matcher cbmsc = cbsc.matcher(Blockline);
                                    String scall = "";
                                    String binfo = "";
                                    if (cbmsc.lookingAt()) {
                                        scall = cbmsc.group(1);
                                        binfo = cbmsc.group(2);
                                        String pCheck = cbmsc.group(3);
                                        String checkstring = "00u" + scall + ":6 " + binfo;
                                        String check = q.checksum(checkstring);
                                        if (check.equals(pCheck)) {
                                            //Callsign in uppercase for APRS
                                            scall = cleanCallForAprs(scall);
                                            if (scall.length() > 0) {
                                                //Looks like a valid call sign
                                                byte[] cmps = binfo.substring(0, 11).getBytes("UTF-8");
                                                int flg = cmps[0] - 32;
                                                int latdegrees = cmps[1] - 32;
                                                String s_latdegrees = String.format("%02d", latdegrees);
                                                int latminutes = cmps[2] - 32;
                                                String s_latminutes = String.format("%02d", latminutes);
                                                int latrest = cmps[3] - 32;
                                                String s_latrest = String.format("%02d", latrest);
                                                int londegrees = cmps[4] - 32;
                                                String s_londegrees = String.format("%03d", londegrees);
                                                int lonminutes = cmps[5] - 32;
                                                String s_lonminutes = String.format("%02d", lonminutes);
                                                int lonrest = cmps[6] - 32;
                                                String s_lonrest = String.format("%02d", lonrest);
                                                int course = cmps[7] - 32;
                                                String s_course = String.format("%03d", course);
                                                int speed = cmps[8] - 32;
                                                String s_speed = String.format("%03d", speed);
                                                char c = (char) cmps[9];
                                                String symbol = Character.toString(c);
                                                int statusinx = cmps[10] - 32;
                                                String statusmessage = binfo.substring(11);
                                                if (statusinx <= Igate.maxstatus) {
                                                    statusmessage = Igate.status[statusinx] + statusmessage;
                                                }
                                                String latstr = "S";
                                                String lonstr = "W";

                                                int x = flg & 32;
                                                if (x == 32) {
                                                    course += 180;
                                                }
                                                x = flg & 16;
                                                if (x == 16) {
                                                    speed += 90;
                                                }
                                                x = flg & 8;
                                                if (x == 8) {
                                                    latstr = "N";
                                                }
                                                x = flg & 4;
                                                if (x == 4) {
                                                    lonstr = "E";
                                                }
                                                x = flg & 2;
                                                if (x == 2) {
                                                    londegrees += 90;
                                                    s_londegrees = String.format("%03d", londegrees);
                                                }
                                                String linfo = "!";
                                                linfo += s_latdegrees;
                                                linfo += s_latminutes;
                                                linfo += ".";
                                                linfo += s_latrest;
                                                linfo += latstr;
                                                linfo += "/";
                                                linfo += s_londegrees;
                                                linfo += s_lonminutes;
                                                linfo += ".";
                                                linfo += s_lonrest;
                                                linfo += lonstr;
                                                linfo += symbol;
                                                linfo += s_course;
                                                linfo += "/";
                                                linfo += s_speed;
                                                linfo += "/";
                                                linfo += statusmessage;

                                                String outstring = scall + ">PSKAPR,TCPIP*:" + linfo;

//                                            System.out.println(outstring);
                                                boolean igateSendOk = Igate.write(outstring);
                                                // Push this to aprs map too
                                                mapSock.sendmessage(outstring);
                                                outstring = "";
                                                //If I run as server, send QSL
                                                if (Main.wantServer && igateSendOk) {
                                                    q.send_QSL_reply();
                                                }
                                            }
                                        }
                                    }
                                } else if (Blockline.contains(":7 ")) {
                                    //Ping request
                                    //<SOH>00uVK2ETA:7 1830<EOT>
                                    Pattern cbsc = Pattern.compile(".*00u(\\S+):7\\s([0123456789ABCDEF]{4}).*");
                                    Matcher cbmsc = cbsc.matcher(Blockline);
                                    String scall = "";
                                    if (cbmsc.lookingAt()) {
                                        scall = cbmsc.group(1).toUpperCase(Locale.US);
                                        if (scall.length() > 1) {
                                            //Some callsign present, reply with s/n
                                            String uiMsg = "Ping request from " + scall;
                                            q.Message(uiMsg, 10);
                                            try {
                                                Thread.sleep(10);
                                            } catch (Exception e) {
                                                //Nothing
                                            }
                                            q.set_txstatus(TxStatus.TXPingReply);
                                            q.send_ping_reply();
                                        }
                                    }
                                } else if (Blockline.contains(":8 ")) {
                                    //Inquire request
                                    //<SOH>00uVK2ETA:8 VK2ETA-5 B848<EOT>
                                    Pattern cbsc = Pattern.compile(".*00u(\\S+):8\\s(\\S+)\\s([0123456789ABCDEF]{4}).*");
                                    Matcher cbmsc = cbsc.matcher(Blockline);
                                    String scall;
                                    String reqcall;
                                    if (cbmsc.lookingAt()) {
                                        reqcall = cbmsc.group(1);
                                        scall = cbmsc.group(2);
                                        String serverCall = Main.configuration.getPreference("CALLSIGNASSERVER");
                                        if (scall.length() > 1 && reqcall.length() > 1 
                                                && scall.toUpperCase(Locale.US).equals(serverCall.toUpperCase(Locale.US))) {
                                            //Some callsigns present and match my call as sever, reply with s/n
                                            String uiMsg = "Inquire request from " + reqcall;
                                            q.Message(uiMsg, 5);
                                            q.set_txstatus(TxStatus.TXInqReply);
                                            q.setReqCallsign(reqcall);
                                            q.send_inquire_reply();
                                        }
                                    }
                                } else if (Blockline.contains(":9 ")) {
                                    //Time Sync request
                                    //<SOH>00uVK2ETA:9 VK2ETA-1 1830<EOT>
                                    Pattern cbsc = Pattern.compile(".*00u(\\S+):9\\s(\\S+)\\s([0-9A-F]{4}).*");
                                    Matcher cbmsc = cbsc.matcher(Blockline);
                                    String scall = "";
                                    String reqcall = "";
                                    if (cbmsc.lookingAt()) {
                                        reqcall = cbmsc.group(1);
                                        scall = cbmsc.group(2).toUpperCase(Locale.US);
                                        String serverCall = Main.configuration.getPreference("CALLSIGNASSERVER");
                                        if (scall.length() > 1 && reqcall.length() > 1
                                                && scall.toUpperCase(Locale.US).equals(serverCall.toUpperCase(Locale.US))) {
                                            if (scall.length() > 1) {
                                                //Some callsign present, reply with time in seconds within +/- 24
                                                String uiMsg = "Time Sync request from " + scall;
                                                q.Message(uiMsg, 10);
                                                try {
                                                    Thread.sleep(10);
                                                } catch (Exception e) {
                                                    //Nothing
                                                }
                                                q.set_txstatus(TxStatus.TXTimeSyncReply);
                                                q.send_TimeSync_reply(reqcall);
                                            }
                                        }
                                    }
                                }
                                //System.out.println(Blockline);
                                // unproto packets
                                if (rxb.type.equals("u")) {
                                    //Display received APRS message???? - Check the callsigns used
                                    if (rxb.port.equals("26") & !serverBeacon) {
                                        if (rxb.call.toUpperCase(Locale.US).equals(configuration.getPreference("CALL").toUpperCase(Locale.US)) || rxb.call.equals(configuration.getPreference("PSKAPRS"))) {
                                            if (rxb.msgtext.indexOf("ack") != 0 & rxb.msgtext.indexOf(":") != 0) {
                                                msgWindow += rxb.from + ": " + rxb.msgtext + "\n";
                                                if (!connected) {
                                                    mainwindow += rxb.from + ": " + rxb.msgtext + "\n";
                                                } else {
                                                    q.Message("You received a message", 10);
                                                }
                                            }
                                        }
                                    } else if (rxb.port.equals("71") | rxb.port.equals("72")) {
                                        //Ping reply from server
                                        int i;
                                        boolean knownserver = false;
                                        Calendar cal = Calendar.getInstance();
                                        int Hour = cal.get(Calendar.HOUR_OF_DAY);
                                        int Minute = cal.get(Calendar.MINUTE);
                                        String formathour = "0" + Integer.toString(Hour);
                                        formathour = formathour.substring(formathour.length() - 2);
                                        String formatminute = "0" + Integer.toString(Minute);
                                        formatminute = formatminute.substring(formatminute.length() - 2);
                                        String lh = formathour + ":" + formatminute;
                                        for (i = 0; i < serversArray.length; i++) {

                                            if (rxb.server.equals(serversArray[i])) {
                                                knownserver = true;
                                                serversSnrArray[i] = snr;
                                                lastHeardArray[i] = lh;
                                                packetsRcved[i]++;
                                                modesRcved[i] = rxModemString;
                                                strength[i] = snr_db;
                                                break;
                                            }
                                        }
                                        if (!knownserver) {
                                            for (i = 0; i < serversArray.length; i++) {
                                                if (serversArray[i].equals("")) {
                                                    Pattern sw = Pattern.compile("[A-Z0-9]+\\-*\\[0-9]*");
                                                    Matcher ssw = sw.matcher(rxb.server);
                                                    if (ssw.lookingAt() & rxb.server.length() > 3) {
                                                        serversArray[i] = rxb.server;
                                                        serversSnrArray[i] = snr;
                                                        lastHeardArray[i] = lh;
                                                        packetsRcved[i]++;
                                                        strength[i] = snr_db;
                                                        mainui.addServer(rxb.server);
                                                        break;
                                                    }
                                                }
                                            }

                                        }
                                        //Link request?
                                        //<SOH>00uVK2ETA><VK2ETA-5 064A<EOT>
                                    } else {
                                        Pattern pl = Pattern.compile("^(\\S+)><(\\S+)");
                                        Matcher ml = pl.matcher(rxb.payload);
                                        if (ml.lookingAt()) {
                                            String linkCall = ml.group(1);
                                            String linkServer = ml.group(2).toUpperCase(Locale.US);
                                            String serverCall = Main.configuration.getPreference("CALLSIGNASSERVER").toUpperCase(Locale.US);
                                            //Clean call sign and ensure it is conforming to standard
                                            String linkCallAprs = cleanCallForAprs(linkCall);
                                            if (linkCallAprs.length() > 0 && serverCall.equals(linkServer)) {
                                                //$MSG = "$ServerCall>PSKAPR,TCPIP*::PSKAPR   :GATING $1";
                                                String linkString = Igate.aprsCall + ">PSKAPR,TCPIP*::PSKAPR   :GATING " + linkCallAprs;
                                                try {
                                                    Igate.write(linkString);
                                                    //Add station to list
                                                    Igate.addStationToList(linkCall);
                                                    //Acknowledge
                                                    q.send_link_ack(linkCall);
                                                } catch (IOException e) {
                                                    //Noting for now: check if recovery required
                                                }
                                            }
                                        }
                                    }
                                    // reject
                                } else if (rxb.type.equals("r") & rxb.valid) {  // reject
                                    String rejectcall = "";
                                    String rejectreason = "";
                                    Pattern pr = Pattern.compile("^(\\S+):(.*)");
                                    Matcher mr = pr.matcher(rxb.payload);
                                    if (mr.lookingAt()) {
                                        rejectcall = mr.group(1);
                                        rejectreason = mr.group(2);
                                    }
                                    if (rejectcall.toUpperCase(Locale.US).equals(mycall.toUpperCase(Locale.US))) {
                                        status = "Listening";
                                        connected = false;
                                        mainui.disableMboxMenu();
                                        mainui.enableMnuPreferences2();
                                        bulletinMode = false;
                                        Connecting = false;
                                        Main.connectingPhase = false;
                                        Main.connectingTime = 0;
                                        scanning = false;
                                        session = "";
                                        totalBytes = 0;
                                        //q.send_rsid_command("OFF");
                                        //q.Message("Rejected:" + rejectreason, 10);
                                        log("Rejected:" + rejectreason);
                                    }
                                    //Connect ack
                                } else if (rxb.type.equals("k") & rxb.valid) {
                                    Pattern pk = Pattern.compile("^(\\S+):\\d+\\s(\\S+):\\d+\\s(\\d)$");
                                    Matcher mk = pk.matcher(rxb.payload);
                                    if (mk.lookingAt()) {
                                        rxb.server = mk.group(1).toUpperCase(Locale.US);
                                        rxb.call = mk.group(2).toUpperCase(Locale.US);
                                        rxb.serverBlocklength = mk.group(3);
                                        String session = rxb.session;
                                        char c = session.charAt(0);
                                        if (c != 0) {
                                            int i = (int) c - 32;
                                            if (i < 64) {
                                                sessions[i] = rxb.call + "<>" + rxb.server + ": ";
                                            } else {
//                                                System.out.println("session out of range:" + i);
                                            }
                                        }
                                    }
                                    // are we  connected?
                                    //if (rxb.call.equals(rxb.mycall) & rxb.server.equals(configuration.getPreference("SERVER"))) {
                                    if (rxb.call.toUpperCase(Locale.US).equals(rxb.mycall.toUpperCase(Locale.US)) 
                                            & rxb.server.toUpperCase(Locale.US).equals(q.getServer().toUpperCase(Locale.US))) {
                                        //txid on, rxid off. Not yet, we now wait until full connect exchange
                                        //q.send_txrsid_command("OFF");
                                        //q.send_rsid_command("ON"); 
                                        status = "Connected";
                                        connected = true;
                                        Connecting = false;
                                        Main.connectingTime = 0;
                                        scanning = false;
                                        summoning = false;
                                        Main.linked = true;
                                        Main.linkedServer = rxb.server;
                                        mainui.disableMonitor();
                                        mainui.disableMnuPreferences2();
                                        // reset tx queue 
                                        txText = "";
                                        totalBytes = 0;
                                        sm.initSession();
                                        session = rxb.session;
                                        sm.session_id = rxb.session;
                                        sm.myserver = rxb.server;
                                        protocolstr = rxb.protocol;
                                        protocol = protocolstr.charAt(0) - 48;
                                        File outb1 = new File(Main.homePath + Main.dirPrefix + "Outbox");
                                        int i1 = outb1.list().length;
                                        if (i1 > 0) {
                                            Main.mainwindow += "\nWaiting in outbox:" + Integer.toString(i1) + "\n";
                                        }
                                        File outb = new File(Main.pendingDir);
                                        int i = outb.list().length;
                                        if (i > 0) {
                                            Main.mainwindow += "Incomplete downloads:" + Integer.toString(i) + "\n\n";
                                        }
                                    }
                                    //Status block, are we a server?
                                } else if (rxb.type.equals("s")
                                        & rxb.valid & rxb.session.equals(session)) {
                                    if (Main.ttyConnected.equals("Connecting")) {
                                        Main.ttyConnected = "Connected";
                                        Main.connected = true;
                                        mainui.disableMnuPreferences2();
                                        statusRcved = true;
                                        numberOfAcks = maxNumberOfAcks;
                                        sm.initSession();
                                        String serverCall = Main.configuration.getPreference("CALLSIGNASSERVER");
                                        //Main.TX_Text = "\nHi, this is the PSKmail Server of " + serverCall + "\nVersion is " + application + "\n\n";
                                        Main.txText = serverCall + " V" + version + ", Hi\n";
                                        //Main.txText += sm.getPendingList(serverCall, ttyCaller);
                                        Main.txText += sm.partialFilesResume("Outpending", ">FO5:");
                                        Main.txText += Motd + "\n";
                                        //We are now fully connected, stop TxIDs
                                        //q.send_txrsid_command("OFF");
                                        myRxStatus = sm.getTXStatus();
                                        q.send_status(myRxStatus);  // send our status
                                        //Register on the APRS network
                                        String linkCallAprs = cleanCallForAprs(ttyCaller);
                                        String serverCallAprs = cleanCallForAprs(serverCall);
                                        //$MSG = "$ServerCall>PSKAPR,TCPIP*::PSKAPR   :GATING $1";
                                        String linkString = serverCallAprs + ">PSKAPR,TCPIP*::PSKAPR   :GATING " + linkCallAprs;
                                        try {
                                            Igate.write(linkString);
                                        } catch (IOException e) {
                                            //Noting for now: check if recovery required
                                        }
                                    }
                                } else if (rxb.radioMsgBlock) {//process RadioMsg message
                                    //if (wantRelayOverRadio | wantRelaySMSs | wantRelayEmails) {
                                    radioMsgWorking = true;//Use either last RSID modem used if any or the default mode
                                    RMsgProcessor.processBlock(Blockline, RMsgProcessor.FileNameString,
                                            Main.lastRsidReceived.length() > 0 ? Main.lastRsidReceived : Main.rxModemString);
                                    Main.lastRsidReceived = ""; //Reset for next RSID.
                                    //}
                                }
                            } //End of if NOT Connected
                            if (rxb.type.equals("c")) {
                                //Connect request
                                //Pattern cmsg = Pattern.compile("<SOH>.0c(\\S+):1024\\s(\\S+):24\\s(\\d).*");
                                Pattern cmsg = Pattern.compile("<SOH>.0c(\\S+):1024\\s(\\S+):24\\s(.*)[0-9A-F]{4}<EOT>.*");
                                Matcher getcl = cmsg.matcher(Blockline);
                                if (getcl.lookingAt()) {
                                    if (matchServerCallWith(getcl.group(2))) {
                                        String newCaller = getcl.group(1);
                                        if (ttyConnected.equals("Connected") && !newCaller.toUpperCase(Locale.US).equals(ttyCaller.toUpperCase(Locale.US))) {
                                            //I am already in a session and this request is not from the same client, ignore
                                            q.Message("Con. request from " + newCaller + ". Ignored...", 5);
                                        } else {
                                            //I am not in a session, or the current client is connecting again, try to accept connection connect
                                            ttyCaller = newCaller;
                                            //Password supplied matches or None required?
                                            if ((rxb.valid && Main.relayingPassword.length() == 0)
                                                    || (rxb.validWithPW && Main.relayingPassword.length() > 0)) {
                                                //Clean any previous session data
                                                disconnect = false;
                                                status = "Listening";
                                                connected = false;
                                                mainui.enableMnuPreferences2();
                                                //VK2ETA shorten initial exchange 
                                                //ttyConnected = "";
                                                ttyConnected = "Connecting";
                                                //Reset RxDelay too
                                                rxDelay = initialRxDelay;
                                                //
                                                session = "";
                                                txText = "";
                                                totalBytes = 0;
                                                sm.initSession();
                                                //more restting necessary here xxxx
                                                //VK2ETA: Already done in initSession()
                                                //for (int i = 0; i < 64; i++) {
                                                //    Session.txbuffer[i] = "";
                                                //}
                                                sm.FileDownload = false;
                                                comp = false;
                                                try {
                                                    if (sm.pFile != null) {
                                                        sm.pFile.close();
                                                    }
                                                } catch (IOException e) {
                                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                                                }
                                                // send TTY acknowledge
                                                String tmp = "Connect request from " + ttyCaller;
                                                //Set TX mode to mode requested by client
                                                ttyModes = getcl.group(3);
                                                //Old protocol, simulate symetric modes
                                                if (ttyModes == null) {
                                                    ttyModes = "0";
                                                }
                                                String myTxmodem = ttyModes.substring(0, 1);
                                                if (myTxmodem.equals("0")) {
                                                    Main.txModem = Main.rxModem;
                                                } else if (ttyModes.length() > 1) {
                                                    ttyModes = ttyModes.substring(1);
                                                    Main.txModem = getmodem(myTxmodem);
                                                }
                                                lastSessionExchangeTime = System.currentTimeMillis() / 1000; //Set initial value of session timeout
                                                m.requestTxRsid("ON");
                                                m.setRxRsid("ON");
                                                q.Message(tmp, 10);
                                                q.send_ack(ttyCaller);
                                                statusRcved = false;
                                                timeoutPolls = 0;
                                                if (Blockline.length() > 8) {
                                                    charval = (int) (blockval / (Blockline.length() - 4)); // msec
                                                    blocktime = m.getBlockTimeAndDelay(Main.rxModem); //Use pre-calculated value
                                                    //blocktime = (charval * 64 / 1000) + 4;
                                                }
                                                log("Connect request from " + ttyCaller);
                                            } else if (rxb.valid && Main.relayingPassword.length() > 0) {
                                                //Need password but none provided. Send a reject block with a reason
                                                m.requestTxRsid("ON");
                                                m.setRxRsid("ON");
                                                log("Connect attempted with missing password when one is required");
                                                q.send_reject(ttyCaller, "Server requires a password\n");
                                            }
                                        }
                                    }
                                }
                            }
                            //Received an Abort or a disconnect request, cleanup
                            if (ttyConnected.equals("Connected")
                                    & rxb.session.equals(session) & rxb.type.equals("a") | disconnect) {
                                q.send_disconnect();
                                disconnect = false;
                                status = "Listening";
                                connected = false;
                                mainui.enableMnuPreferences2();
                                ttyConnected = "";
                                session = "";
                                txText = "";
                                totalBytes = 0;
                                sm.initSession();
                                //Already done in initsession
                                //int i;
                                //for (i = 0; i < 64; i++) {
                                //    Session.txbuffer[i] = "";
                                //}
                                isDisconnected = true;
                                sm.FileDownload = false;
                                comp = false;
                                try {
                                    if (sm.pFile != null) {
                                        sm.pFile.close();
                                    }
                                } catch (IOException e) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                                }
                                //Set RXid ON for next connect request
                                m.requestTxRsid("ON");
                                m.setRxRsid("ON");
                                // send disconnect packet to caller...
                                //VK2ETA moved up to be first in sequence
                                //q.send_disconnect();
                                Main.rxDelay = Main.initialRxDelay;
                                //TTY connect request from other client (I become a TTY server)
                                //} else if (rxb.valid & rxb.type.equals("c")) { //now with access password
                            }
                            if (debug) {
                                System.out.println(rxb.server);
                                System.out.println(rxb.test);
                                System.out.println(rxb.protocol);
                                System.out.println(rxb.session);
                                System.out.println(rxb.type);
                                System.out.println(rxb.crc);
                                System.out.println(rxb.port);
                                if (rxb.valid == true) {
//                                System.out.println("valid");
                                }
                            }
                            //End of //if  NO bulletin mode & NO IAC mode
                        } else if (Main.bulletinMode) {
                            // Bulletin mode
                            Blockline = Blockline.substring(5);
                            if (Blockline.length() > 9) {
                                Blockline = Blockline.substring(0, Blockline.length() - 9);
                            }
                            Pattern pb = Pattern.compile("NNNN");
                            Matcher mb = pb.matcher(Blockline);
                            if (mb.find()) {
                                Blockline = "\n----------\n";
                                bulletin.write(Blockline);
                                Main.bulletinMode = false;
                                Main.status = "Listening";
                            }
                            mainwindow += Blockline;
                            bulletinTime = 30;
                            // write to bulletins file...
                            bulletin.write(Blockline);
                            bulletin.flush();
                        } else if (Main.iacMode) {
                            sm.parseInput(Blockline);
                        }
                    } else { // if NO (m.checkBlock())
                        // no block coming...and we are server (or received a connect request)
                        if (!Main.TxActive & (ttyConnected.equals("Connected")
                                | ttyConnected.equals("Connecting"))) {  //Allow timeouts when in connecting phase as well
                            //long now = System.currentTimeMillis();
                            Systime = System.currentTimeMillis();
                            idlesecs = (int) ((Systime - oldtime) / 1000);
                            //Overall session idle timeout
                            String IdleTimeStr = configuration.getPreference("IDLETIME", "120");
                            int MaxSessionIdleTime = Integer.parseInt(IdleTimeStr); //In seconds
                            if (MaxSessionIdleTime > 0 & MaxSessionIdleTime < 60) {
                                MaxSessionIdleTime = 60; //Minimum 1 minute
                            }
                            if (MaxSessionIdleTime > 0) {  //Zero mean never disconnect, use with care
                                long SessionIdleSec = (Systime / 1000) - lastSessionExchangeTime;
                                if (SessionIdleSec > MaxSessionIdleTime) {
                                    //Disconnect session
                                    disconnect = false;
                                    status = "Listening";
                                    connected = false;
                                    mainui.enableMnuPreferences2();
                                    mainui.disableMboxMenu();
                                    ttyConnected = "";
                                    session = "";
                                    txText = "";
                                    totalBytes = 0;
                                    int i;
                                    for (i = 0; i < 64; i++) {
                                        Session.txbuffer[i] = "";
                                    }
                                    isDisconnected = true;
                                    //Set RXid ON for next connect request
                                    //q.send_txrsid_command("OFF");
                                    m.setRxRsid("ON");
                                    // send disconnect packet to caller...
                                    q.send_disconnect();
                                    //Clean session
                                    sm.initSession();
                                }
                            }
                            //Check timeouts on status replies
                            //If we have seen an <SOH> wait for full block, otherwise assume we didn't hear anything
                            //if (((Main.BlockActive | TTYConnected.equals("Connecting")) & (idlesecs > (blocktime * 3) + 8))
                            //        | (!Main.BlockActive & (idlesecs > (blocktime * 0.8) + 8))) {

                            /*
                            //Split the below logical test by taking a snapshot as the modem thread changes variables on us
                            if ((m.BlockActive  && !m.receivingStatusBlock && (idlesecs > (blocktime * 2.2 + m.firstCharDelay)))) {
                                //Normal data block reception
                                System.out.println("Normal block t/o");
                            }
                            if(m.BlockActive && m.receivingStatusBlock && (idlesecs > (blocktime * 0.3 + m.firstCharDelay))) {
                                boolean ba = m.BlockActive;
                                boolean rsb = m.receivingStatusBlock;
                                int is = idlesecs;
                                int bt = blocktime;
                                //Status block reception
                                System.out.println("Status block T/O");
                            }
                            if (!m.BlockActive && (idlesecs > blocktime * 0.5 + m.firstCharDelay)) {
                                // No data block received
                                System.out.println("test 3");
                            }
                             */
                            if ((m.BlockActive && !m.receivingStatusBlock && (idlesecs > (blocktime * 2.2 + m.firstCharDelay + rxDelay))) //Normal data block reception
                                    //DEBUG 
                                    | (m.BlockActive && m.receivingStatusBlock && (idlesecs > (blocktime * (0.3) + m.firstCharDelay + rxDelay))) //Data block is status block
                                    //DEBUG                           
                                    | (!m.BlockActive && (idlesecs > (blocktime * (0.1) + m.firstCharDelay + rxDelay))) // No data block received at all
                                    ) {
                                //Debug
                                if (!m.BlockActive && (idlesecs > (blocktime * (0.1) + m.firstCharDelay + rxDelay))) {
                                    oldtime = Systime; //Redundant but need a breakpoint
                                }
                                oldtime = Systime;
                                timeoutPolls += 1;
                                // Check if we need to downgrade modes
                                if (timeoutPolls > 1) {
                                    DowngradeOneMode();
                                    //If we have already missed two polls, only try once in each mode
                                    timeoutPolls = 1;
                                }
                                if (ttyConnected.equals("Connecting") & !statusRcved & numberOfAcks > 0) {
                                    // repeat sending ack...
                                    //Turn RXid and TXid ON as I am repeating a connect ack
                                    m.setRxRsid("ON");
                                    m.requestTxRsid("ON");
                                    q.send_ack(ttyCaller);
                                    statusRcved = false;
                                    idlesecs = 0;
                                    numberOfAcks--;
                                    //Add 3 seconds to the rxDelay in case the client is slow to respond or has a long Tx delay (i.e. through a repeater)
                                    Main.rxDelay += 3;
                                    if (rxDelay > 9) {
                                        rxDelay = 9; //Max delay
                                    }
                                } else if (ttyConnected.equals("Connecting") & !statusRcved) {
                                    //Abandon connect trial
                                    status = "Listening";
                                    connected = false;
                                    mainui.enableMnuPreferences2();
                                    mainui.disableMboxMenu();
                                    ttyConnected = "";
                                    session = "";
                                    txText = "";
                                    totalBytes = 0;
                                    int i;
                                    for (i = 0; i < 64; i++) {
                                        Session.txbuffer[i] = "";
                                    }
                                    isDisconnected = true;
                                    Main.rxDelay = Main.initialRxDelay;
                                    //Set RXid ON for next connect request
                                    //q.send_txrsid_command("OFF");
                                    m.setRxRsid("ON");
                                } else if (ttyConnected.equals("Connected")) {
                                    // We are in a session, send a poll
                                    //Turn RXid and TXid ON as I am repeating a status request
                                    m.setRxRsid("ON");
                                    m.requestTxRsid("ON");
                                    myRxStatus = sm.getTXStatus();
                                    q.send_status(myRxStatus);  // send our status
                                    idlesecs = 0;
                                }
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } // end while
        } finally {
            try {
                if (!(bulletin == null)) {
                    bulletin.close();
                }
            } catch (IOException ex) {
                log.writelog("IO Exception when closing bulletins!", ex, true);
            }
        }
    } // end Main

    /**
     * Add a server to the array of known servers, for instance as written by
     * the user
     *
     * @param MyServer
     */
    public static void AddServerToArray(String myServer) {
        try {
            int i;
            boolean knownserver = false;
            for (i = 0; i < serversArray.length; i++) {
                if (myServer.equals(serversArray[i])) {
                    knownserver = true;
                    break;
                }
            }
            if (!knownserver) {
                for (i = 0; i < 10; i++) {
                    if (serversArray[i].equals("")) {
                        serversArray[i] = myServer;
                        mainui.addServer(myServer);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.writelog("Had problem adding server to array, full?", e, true);
        }
    }

    /**
     * Create or check the necessary folder structure (.pskmail)
     */
    private static void handlefolderstructure() {

        // are we on Linux?
        try {
            homePath = System.getProperty("user.home");
            if (File.separator.equals("/")) {
                dirPrefix = "/.pskmail/";
                separator = "/";
                onWindows = false;
            } else {
                dirPrefix = "\\pskmail\\";
                separator = "\\";
                onWindows = true;
            }
            // Where is this jar installed? Needs updating for windows?
            String path = MainPskmailUi.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            path = path.substring(0, path.lastIndexOf("/") + 1);
            Main.installPath = path;
            //Check if pskmail directory exists, create if not
            File dir = new File(homePath + dirPrefix);
            if (!dir.isDirectory()) {
                dir.mkdir();
            }
            //Check if Outbox directory exists, create if not
            if (File.separator.equals("/")) {
                separator = "/";
            } else {
                separator = "\\";
            }
            File outbox = new File(homePath + dirPrefix + "Outbox" + separator);
            if (!outbox.isDirectory()) {
                outbox.mkdir();
            }
            File sentbox = new File(homePath + dirPrefix + "Sent" + separator);
            if (!sentbox.isDirectory()) {
                sentbox.mkdir();
            }
            File pendingfl = new File(homePath + dirPrefix + "Pending" + separator);
            if (!pendingfl.isDirectory()) {
                pendingfl.mkdir();
            }
            File outpendingfl = new File(homePath + dirPrefix + "Outpending" + separator);
            if (!outpendingfl.isDirectory()) {
                outpendingfl.mkdir();
            }

            //Check if Downloads directory exists, create if not
            if (File.separator.equals("/")) {
                separator = "/";
            } else {
                separator = "\\";
            }
            File downloads = new File(homePath + dirPrefix + "Downloads" + separator);
            if (!downloads.isDirectory()) {
                downloads.mkdir();
            }

            //Check if Pending directory exists, create if not
            if (File.separator.equals("/")) {
                separator = "/";
            } else {
                separator = "\\";
            }
            pendingStr = homePath + dirPrefix + "Pending" + separator;
            pending = new File(pendingStr);
            if (!pending.isDirectory()) {
                pending.mkdir();
            }
            pendingDir = homePath + dirPrefix + "Pending" + separator;
            outPendingDir = homePath + dirPrefix + "Outpending" + separator;
            transactions = homePath + dirPrefix + "Transactions";

            // Check if bulletin file  exists, create if not
            File fFile = new File(Main.homePath + Main.dirPrefix + "Downloads" + separator + "bulletins");
            if (!fFile.exists()) {
                fFile.createNewFile();
            }

            bulletin = new FileWriter(fFile, true);

            // check if headers file exists, and read in contents 
            File fh = new File(homePath + dirPrefix + "headers");
            if (!fh.exists()) {
                fh.createNewFile();
            }

            hdr = new FileReader(fh);
            BufferedReader br = new BufferedReader(hdr);
            String s;
            while ((s = br.readLine()) != null) {
                String fl = s + "\n";
                mailHeadersWindow += fl;
            }
            br.close();

            //Create RadioMsgInbox
            File RadioMsgInbox = new File(homePath + dirPrefix + dirInbox + separator);
            if (!RadioMsgInbox.isDirectory()) {
                RadioMsgInbox.mkdir();
            }
            //Create RadioMsgSentbox
            File RadioMsgSentbox = new File(homePath + dirPrefix + dirSent + separator);
            if (!RadioMsgSentbox.isDirectory()) {
                RadioMsgSentbox.mkdir();
            }
            //Create RadioMsgSendingAPI
            File RadioMsgSendingAPI = new File(homePath + dirPrefix + dirSendingAPI + separator);
            if (!RadioMsgSendingAPI.isDirectory()) {
                RadioMsgSendingAPI.mkdir();
            }
            //Create RadioMsgImages
            File RadioMsgImages = new File(homePath + dirPrefix + dirImages + separator);
            if (!RadioMsgImages.isDirectory()) {
                RadioMsgImages.mkdir();
            }
            //Create RadioMsgArchive Directory
            File RadioMsgArchive = new File(homePath + dirPrefix + dirArchive + separator);
            if (!RadioMsgArchive.isDirectory()) {
                RadioMsgArchive.mkdir();
            }
        } catch (Exception ex) {
            log.writelog("Problem when handling pskmail folder structure.", ex, true);
        }
    }

    private static void handleinitialization() {

        try {
            /*
                File f1 = new File(Main.HomePath + Main.Dirprefix + "configuration.xml");
                File f2 = new File("configuration.xml");

                if (f1.isFile()) {

                    InputStream in = new FileInputStream(f1);

                    //Overwrite the file.
                    OutputStream out = new FileOutputStream(f2);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    q.Message("Config File copied.", 10);
                }
             */

            // get password
            sessionPasswrd = configuration.getPreference("PASSWORD");
            // try to initialize MAXDCD from Prefs
            DCDstr = configuration.getPreference("DCD", "3");
            MAXDCD = Integer.parseInt(DCDstr);
            // try to initialize Icon from Prefs
            icon = configuration.getPreference("ICON", "y");
            ICONlevel = configuration.getPreference("ICON2", "/");
            // Initialize APRSMessageNumber
            aprsMessageNumber = 0;
            // Initialize send queue
            txText = "";
            // init logfile
            Main.logFile = configuration.getPreference("LOGFILE", "client.log");
            // Modem settings
            host = configuration.getPreference("MODEMIP");
            port = Integer.parseInt(configuration.getPreference("MODEMIPPORT", "7322"));
            xmlPort = configuration.getPreference("MODEMXMLPORT", "7362");
            modemPreamble = configuration.getPreference("MODEMPREAMBLE", "0");
            modemPostamble = configuration.getPreference("MODEMPOSTAMBLE", "0");
            // Mail settings
            if (configuration.getPreference("COMPRESSED", "yes").equals("yes")) {
                compressedmail = true;
            } else {
                compressedmail = false;
            }
            String profile = configuration.getPreference("BLOCKLENGTH", "5");
            currentModemProfile = profile;
            Character c = profile.charAt(0);
            // Get the default modem and the selected mode list
            // If this is not set by the client then set a decent default as
            // they will end up in THOR8 hell most often otherwise!!!
            String strunt = configuration.getPreference("DEFAULTMODE");
            if (!strunt.isEmpty()) {
                defaultTxModem = configuration.getPreference("DEFAULTMODE");
                defaultmode = convmodem(defaultTxModem);
                lastTxModem = lastRxModem = defaultTxModem;
            } else {
                defaultTxModem = "PSK250R";
                Main.defaultmode = convmodem(defaultTxModem);
                configuration.setPreference("DEFAULTMODE", Main.defaultTxModem);
            }
            // Check if its empty, if so then set a decent default
            strunt = configuration.getPreference("MODES");
// System.out.println("Defaultmode=" + strunt);                  
            if (!strunt.isEmpty()) {
                modesListStr = configuration.getPreference("MODES");
            } else {
                modesListStr = "8543"; // PSK250, PSK250R, MFSK32, THOR22
                configuration.setPreference("MODES", Main.modesListStr);
            }
            if (configuration.getPreference("GPSD", "0").equals("1")) {
                wantGpsd = true;
            }
            if (configuration.getPreference("SCANNER", "no").equals("yes")) {
                wantScanner = true;
            }
            // APRSServerSettings
            //
            aprsServerPort = Integer.parseInt(configuration.getPreference("APRSSERVERPORT", "8063"));
            if (configuration.getPreference("APRSSERVER", "yes").equals("yes")) {
                aprsServerEnabled = true;
            } else {
                aprsServerEnabled = false;
            }
            //Server and RadioMsg globals
            if (configuration.getPreference("ENABLESERVER", "no").equals("yes")) {
                wantServer = true;
            } else {
                wantServer = false;
            }
            if (configuration.getPreference("RELAYOVERRADIO", "no").equals("yes")) {
                wantRelayOverRadio = true;
            } else {
                wantRelayOverRadio = false;
            }
            if (configuration.getPreference("RELAYEMAILS", "no").equals("yes")) {
                wantRelayEmails = true;
            } else {
                wantRelayEmails = false;
            }
            if (configuration.getPreference("RELAYEMAILSIMMEDIATELY", "no").equals("yes")) {
                wantRelayEmailsImmediat = true;
            } else {
                wantRelayEmailsImmediat = false;
            }
            if (configuration.getPreference("RELAYSMSS", "no").equals("yes")) {
                wantRelaySMSs = true;
            } else {
                wantRelaySMSs = false;
            }
            if (configuration.getPreference("RELAYSMSSIMMEDIATELY", "no").equals("yes")) {
                wantRelaySMSsImmediat = true;
            } else {
                wantRelaySMSsImmediat = false;
            }
            callsignAsServer = configuration.getPreference("CALLSIGNASSERVER", "N0CAL");
            relayingPassword = Main.configuration.getPreference("ACCESSPASSWORD").trim();
            IotAccessPassword = Main.configuration.getPreference("IOTACCESSPASSWORD").trim();
        } catch (Exception e) {
            MAXDCD = 3;
//                q.backoff = "5";
            icon = "y";
            ICONlevel = "/";
            log.writelog("Problems with config parameter.", e, true);
        }
        //New: now contains a list of servers with optional passwords
        loadServerList();
        Main.mycall = configuration.getPreference("CALL");
        freqOffset = Integer.parseInt(Main.configuration.getPreference("RIGOFFSET", "0"));
        String XMLIP = configuration.getPreference("MODEMIP", "127.0.0.1");
        if (XMLIP.equals("localhost")) {
            XMLIP = "127.0.0.1";
        }
        xmlRpcURL = "http://" + XMLIP + ":" + xmlPort.trim() + "/RPC2";
    }

    //Fills array of Servers and passwords from the preferences
    public static void loadServerList() {

        //Fill-in spinner for Servers, with passwords now
        //Format is Eg: vk2eta-1:pass1, vk2eta-2, vk2eta-3:pass3 (Note: vk2eta-2 does not use a password)
        String[] serverArrayOriginal = configuration.getPreference("SERVER").split(",");
        String[] serverPasswordArrayOriginal = new String[serverArrayOriginal.length];
        //int viaValidEntries = 0;
        Pattern viaPattern = Pattern.compile("^\\s*([0-9a-zA-Z/\\-_.+]+)\\s*(:?)\\s*(\\S*)\\s*$");
        for (int i = 0; i < serverArrayOriginal.length; i++) {
            Matcher msc = viaPattern.matcher(serverArrayOriginal[i]);
            if (msc.find()) {
                String callSign = "";
                if (msc.group(1) != null) {
                    callSign = msc.group(1);
                }
                String separator = "";
                if (msc.group(2) != null) {
                    separator = msc.group(2);
                }
                String accessPassword = "";
                if (msc.group(3) != null) {
                    accessPassword = msc.group(3);
                }
                if (!callSign.equals("")) {
                    //viaValidEntries++;
                    serverArrayOriginal[i] = callSign;
                    if (!separator.equals("") && !accessPassword.equals("")) {
                        serverPasswordArrayOriginal[i] = accessPassword;
                    } else {
                        serverPasswordArrayOriginal[i] = ""; //As it is copied later on
                    }
                } else {
                    serverArrayOriginal[i] = ""; //Blank it out
                    serverPasswordArrayOriginal[i] = "";
                }
            } else {
                //Malformed to destination, blank it out too
                serverArrayOriginal[i] = "";
                serverPasswordArrayOriginal[i] = "";
            }
        }
        //Copy only non null strings to final array
        serversArray = new String[MAXNEWSERVERS];
        serversPasswordArray = new String[serversArray.length];
        int j = 0;
        for (int i = 0; i < serverArrayOriginal.length && i < MAXNEWSERVERS; i++) {
            if (serverArrayOriginal[i].length() > 0) {
                serversArray[j] = serverArrayOriginal[i];
                serversPasswordArray[j++] = serverPasswordArrayOriginal[i];
            }
        }
        //Blank the rest. Index j already contains the first location to blank
        for (; j < serversArray.length; j++) {
            serversArray[j] = "";
            serversPasswordArray[j] = "";
        }
        //Set default one
        Main.myserver = serversArray[0];
        Main.myserverpassword = serversPasswordArray[0];
        //Re-size and blank the other arrays
        serversSnrArray = new double[MAXNEWSERVERS];
        for (int i = 0; i < MAXNEWSERVERS; i++) {
            serversSnrArray[i] = 0.0f;
        }
        lastHeardArray = new String[MAXNEWSERVERS];
        for (int i = 0; i < MAXNEWSERVERS; i++) {
            lastHeardArray[i] = "";
        }
        packetsRcved = new int[MAXNEWSERVERS];
        for (int i = 0; i < MAXNEWSERVERS; i++) {
            packetsRcved[i] = 0;
        }
        modesRcved = new String[MAXNEWSERVERS];
        for (int i = 0; i < MAXNEWSERVERS; i++) {
            modesRcved[i] = "";
        }
        strength = new int[MAXNEWSERVERS];
        for (int i = 0; i < MAXNEWSERVERS; i++) {
            strength[i] = 0;
        }
    }

    private static void handlegpsd() {
        try {
            // Connect to gpsd at port 2947 on localhost
            InetAddress addr = InetAddress.getByName("localhost");
            int target = 2947;
            SocketAddress sockaddr = new InetSocketAddress(addr, target);
            // Block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 2000;   // 2 seconds
            gpsdSocket = new Socket();
            gpsdSocket.connect(sockaddr, timeoutMs);
            gpsdOut = new PrintWriter(gpsdSocket.getOutputStream(), true);
            gpsdIn = new BufferedReader(new InputStreamReader(
                    gpsdSocket.getInputStream()));
            String outgps = "?WATCH={\"enable\":true, \"nmea\":true };";
            gpsdOut.println(outgps);
            long t0 = System.currentTimeMillis();
            t1 = t0;
            boolean ready = false;
            while (t1 - t0 < 2000 & !ready) {
                t1 = System.currentTimeMillis();
                String myRead = "";
                if (gpsdIn.ready()) {
                    myRead = gpsdIn.readLine();
                    if (myRead.substring(0, 6).equals("$GPRMC")) {
                        haveGPSD = true;
                        ready = true;
                    }
                }
            }
            if (!haveGPSD) {
                q.Message("Problem with GPSD", 10);
            }
        } catch (UnknownHostException e) {
            q.Message("Cannot find GPSD", 10);
            haveGPSD = false;
        } catch (IOException e) {
            q.Message("Cannot find gpsd", 10);
            haveGPSD = false;
        }
        if (haveGPSD) {
            gpsData = new NmeaParser();     // Parser for nmea data
            q.Message("Connected to GPSD", 10);
        }
    }

    static public void getgpsddata() {
        String myRead = "";
        Boolean ready = false;

        while (haveGPSD & !ready) {
            try {
                myRead = gpsdIn.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (myRead.length() > 6) {
                if (myRead.substring(0, 6).equals("$GPRMC")) {
                    gpsdDataArray = myRead.split(",");
                    if (gpsdDataArray[1].length() > 2) {
                        gpsData.validfix = true;
                        gpsData.fixat = gpsdDataArray[1];
                    }
                    gpsData.latitude = gpsdDataArray[3];
                    float latdata = Float.valueOf(gpsData.latitude) / 100;
                    int degr = (int) latdata;
                    float mindata = (latdata - degr) / 60 * 100;
                    mindata = degr + mindata;
                    if (gpsdDataArray[4].equals("S")) {
                        mindata *= -1;
                    }
                    gpsdLatitude = Float.toString(mindata);
                    gpsData.longitude = gpsdDataArray[5];
                    float longdata = Float.valueOf(gpsData.longitude) / 100;
                    degr = (int) longdata;
                    mindata = (longdata - degr) / 60 * 100;
                    mindata = degr + mindata;
                    if (gpsdDataArray[6].equals("W")) {
                        mindata *= -1;
                    }
                    gpsdLongitude = Float.toString(mindata);
                    gpsData.speed = gpsdDataArray[7];
                    gpsData.course = gpsdDataArray[8];
                    char[] buffer = new char[4000];
                    try {
                        int cnt = gpsdIn.read(buffer);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                configuration.setLatitude(gpsdLatitude);
                configuration.setLongitude(gpsdLongitude);
                ready = true;
            }
        }
    }

    static public void parsenmeadata(String nmeadata) {
        
        gpsData.newdata(nmeadata);
        if (gpsData.getFixStatus()) {
            configuration.setLatitude(gpsData.getLatitude());
            configuration.setLongitude(gpsData.getLongitude());
            configuration.setSpeed(gpsData.getSpeed());
            configuration.setCourse(gpsData.getCourse());
        }
    }

    /**
     * Open a GPS connection, if that should be used
     */
    private static void handlegps() {
        // GPS
        gpsPort = new GpsSerialPort();       // Serial port object
        gpsData = new NmeaParser();     // Parser for nmea data
        String portforgps = configuration.getPreference("GPSPORT");

        // Make sure the selected port still exists!
        if (configuration.getPreference("GPSENABLED").equals("yes")) {
            if (!gpsPort.checkComPort(portforgps)) {
                Main.log.writelog("Serial port " + portforgps + " does not exist! Was the GPS removed? Disabling GPS.", true);
                configuration.setPreference("GPSENABLED", "no");
            }
        }
        if (configuration.getPreference("GPSENABLED", "no").equals("yes")) {
            try {
                String speedforgps = configuration.getPreference("GPSSPEED", "4800");
                int speedygps = Integer.parseInt(speedforgps);
                gpsPort.connect(portforgps, speedygps);
                // Check if the port is open
                if (!gpsPort.curstate) {
                    // Disconnect and set it off
                    gpsPort.disconnect();
                    configuration.setPreference("GPSENABLED", "no");
                }
                /*if (portforgps.contains("USB"))
            // Here is the code for getting a gps out of sirf mode
            gpsdata.writehexsirfmsg("8102010100010101050101010001000100010001000112c0"); //Set 4800 bps nmea*/
            } catch (Exception ex) {
                log.writelog("Error when trying to connect to the GPS.", ex, true);
            }
        }
    }

    static void setFreq(String freq) {
        if (RigCtrl.opened) {
            int fr = Integer.parseInt(Main.currentFreq) + RigCtrl.OFF;
            freqStore = Integer.toString(fr);
            RigCtrl.Setfreq(freq);
            summoning = true;
        }
    }

    static void savePreferences() {
        /*
     try {
           // store the config file if present

             File f1 = new File( "configuration.xml");
             File f2 = new File(Main.HomePath + Main.Dirprefix + "configuration.xml");

            if (f1.isFile()) {

            InputStream in = new FileInputStream(f1);

      //Overwrite the file.
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            q.Message("Config File stored.", 10);
         }
        }
        catch (Exception e) {
                 q.Message("problem writing the config file", 10);
         }

         */
    }

    static double decayaverage(double oldaverage, double newvalue, double factor) {

        double newaverage = oldaverage;
        if (factor > 1) {
            newaverage = (oldaverage * (1 - 1 / factor)) + (newvalue / factor);
        }
        return newaverage;
    }

//Downgrade RX and TX modes alternatively (until we recover the link)
    static void DowngradeOneMode() {
        int currentmodeindex = 0;

        //Alternate Rx and Tx speed downgrades
        if (Main.justDowngradedRx) {
            justDowngradedRx = false;
            Main.hisS2n = 50; //Reset to mid-range
            //Downgrade Tx mode
            currentmodeindex = getClientModeIndex(Main.txModem);
            if (currentmodeindex < ttyModes.length() - 1) { //List in decreasing order of speed
                Main.txModem = getClientMode(currentmodeindex + 1);
                sm.SetBlocklength(5); //restart with medium block length
                m.requestTxRsid("ON");
                String TxMode = m.getTXModemString(Main.txModem);
                //String SendMode = "<cmd><mode>" + TxMode + "</mode></cmd>";
                //m.Sendln(SendMode);
                m.Sendln("", TxMode, "");
                //try {
                //    Thread.sleep(1000);
                //} catch (InterruptedException ex) {
                //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
        } else {
            justDowngradedRx = true; // Make TX mode downgrade first (if necessary)
            Main.myS2n = 50; //Reset to mid-range
            currentmodeindex = getClientModeIndex(Main.rxModem);
            if (currentmodeindex < ttyModes.length() - 1) { //List in decreasing order of speed
                Main.rxModem = getClientMode(currentmodeindex + 1);
                Main.rxModemString = m.getModemString(Main.rxModem);
                blocktime = m.getBlockTimeAndDelay(Main.rxModem);
                String TxMode = m.getTXModemString(Main.txModem);
                //String SendMode = "<cmd><mode>" + TXmd + "</mode></cmd>";
                //m.Sendln(SendMode);
                m.Sendln("", TxMode, ""); //Just change mode
                //try {
                //    Thread.sleep(1000);
                //} catch (InterruptedException ex) {
                //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
        }
    }

    static ModemModesEnum convmodem(String mymodem) {
        ModemModesEnum mode = ModemModesEnum.THOR8;

        if (mymodem.equals("THOR8")) {
            mode = ModemModesEnum.THOR8;
        } else if (mymodem.equals("THOR4")) {
            mode = ModemModesEnum.THOR4;
        } else if (mymodem.equals("MFSK16")) {
            mode = ModemModesEnum.MFSK16;
        } else if (mymodem.equals("THOR22")) {
            mode = ModemModesEnum.THOR22;
        } else if (mymodem.equals("MFSK32")) {
            mode = ModemModesEnum.MFSK32;
        } else if (mymodem.equals("PSK250R")) {
            mode = ModemModesEnum.PSK250R;
        } else if (mymodem.equals("PSK500R")) {
            mode = ModemModesEnum.PSK500R;
        } else if (mymodem.equals("PSK1000")) {
            mode = ModemModesEnum.PSK1000;
        } else if (mymodem.equals("PSK500")) {
            mode = ModemModesEnum.PSK500;
        } else if (mymodem.equals("PSK250")) {
            mode = ModemModesEnum.PSK250;
        } else if (mymodem.equals("PSK125")) {
            mode = ModemModesEnum.PSK125;
        } else if (mymodem.equals("PSK63")) {
            mode = ModemModesEnum.PSK63;
        } else if (mymodem.equals("PSK125R")) {
            mode = ModemModesEnum.PSK125R;
        } else if (mymodem.equals("DOMINOEX5")) {
            mode = ModemModesEnum.DOMINOEX5;
        } else if (mymodem.equals("CTSTIA")) {
            mode = ModemModesEnum.CTSTIA;
        } else if (mymodem.equals("DOMINOEX22")) {
            mode = ModemModesEnum.DOMINOEX22;
        } else if (mymodem.equals("DOMINOEX11")) {
            mode = ModemModesEnum.DOMINOEX11;
        } else if (mymodem.equals("PSK63RC5")) {
            mode = ModemModesEnum.PSK63RC5;
        } else if (mymodem.equals("PSK63RC10")) {
            mode = ModemModesEnum.PSK63RC10;
        } else if (mymodem.equals("PSK250RC3")) {
            mode = ModemModesEnum.PSK250RC3;
        } else if (mymodem.equals("PSK125RC4")) {
            mode = ModemModesEnum.PSK125RC4;
        }
        return mode;
    }

    static ModemModesEnum getmodem(String mymodem) {
        ModemModesEnum mode = ModemModesEnum.THOR8;

        if (mymodem.equals("1")) {
            mode = ModemModesEnum.THOR8;
        } else if (mymodem.equals("2")) {
            mode = ModemModesEnum.MFSK16;
        } else if (mymodem.equals("3")) {
            mode = ModemModesEnum.THOR22;
        } else if (mymodem.equals("4")) {
            mode = ModemModesEnum.MFSK32;
        } else if (mymodem.equals("5")) {
            mode = ModemModesEnum.PSK250R;
        } else if (mymodem.equals("6")) {
            mode = ModemModesEnum.PSK500R;
        } else if (mymodem.equals("g")) {
            mode = ModemModesEnum.PSK1000;
        } else if (mymodem.equals("7")) {
            mode = ModemModesEnum.PSK500;
        } else if (mymodem.equals("8")) {
            mode = ModemModesEnum.PSK250;
        } else if (mymodem.equals("9")) {
            mode = ModemModesEnum.PSK125;
        } else if (mymodem.equals("a")) {
            mode = ModemModesEnum.PSK63;
        } else if (mymodem.equals("b")) {
            mode = ModemModesEnum.PSK125R;
        } else if (mymodem.equals("n")) {
            mode = ModemModesEnum.DOMINOEX5;
        } else if (mymodem.equals("f")) {
            mode = ModemModesEnum.CTSTIA;
        } else if (mymodem.equals("h")) {
            mode = ModemModesEnum.PSK63RC5;
        } else if (mymodem.equals("i")) {
            mode = ModemModesEnum.PSK63RC10;
        } else if (mymodem.equals("j")) {
            mode = ModemModesEnum.PSK250RC3;
        } else if (mymodem.equals("k")) {
            mode = ModemModesEnum.PSK125RC4;
        } else if (mymodem.equals("l")) {
            mode = ModemModesEnum.DOMINOEX22;
        } else if (mymodem.equals("m")) {
            mode = ModemModesEnum.DOMINOEX11;
        }
        return mode;
    }

    //Returns the index of the parameter mode in the client supplied list of modes
    public static int getClientModeIndex(ModemModesEnum mode) {
        int index = 0;

        if (ttyModes.length() > 0) {
            for (int i = 0; i < ttyModes.length(); i++) {
                if (Main.getmodem(ttyModes.substring(i, i + 1)) == mode) {
                    index = i;
                }
            }
        }
        return index;
    }

    //Returns the modemmodeenum pointed to by the parameter in the client's array of selected modes
    public static ModemModesEnum getClientMode(int index) {

        if (index >= 0 && index < ttyModes.length()) {
            return Main.getmodem(ttyModes.substring(index, index + 1));
        }
        //No match return something (MFSK16)
        return Main.getmodem("2");
    }

    static int getserverindex(String server) {
        int o = 0;
        for (int i = 0; i < serversArray.length; i++) {
            if (server.toLowerCase(Locale.US).equals(serversArray[i].toLowerCase(Locale.US))) {
                o = i;
            }
        }
        return o;
    }

    static void setrxdata(String server, int data) {
        int i = getserverindex(server);
        if (i < MAXNEWSERVERS - 1) {
            System.arraycopy(rxData[i], 0, rxData[i], 1, 8);
            rxData[i][0] = data;
        }
    }

    static int getrxdata_avg(int index) {
        int i = 0;
        int acc = 0;
        int avg = 0;
        for (i = 0; i < 10; i++) {
            if (rxData[index][i] > 0) {
                acc += rxData[index][i];
                avg = acc / (i + 1);
            }
        }
        return avg;
    }

    static String myTime() {
        // create a java calendar instance
        Calendar calendar = Calendar.getInstance();

        // get a java.util.Date from the calendar instance.
        // this date will represent the current instant, or "now".
        java.util.Date now = calendar.getTime();

        // a java current time (now) instance
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        return currentTimestamp.toString().substring(0, 16);
    }

    static void shown(String st) {
        System.out.println(st);
    }

    static void show(String st) {
        System.out.print(st);
    }

    static void log(String logtext) {

        try {
            // Create file
            FileWriter logstream = new FileWriter(homePath + dirPrefix + logFile, true);
            BufferedWriter out = new BufferedWriter(logstream);

            out.write(myTime() + " " + logtext + "\n");
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("LogError: " + e.getMessage());
        }
        Main.mainwindow += (myTime() + " " + logtext + "\n");
    }

    //Check if we have a case insensitive match with my client's call
    public static boolean matchClientCallWith(String call) {

        if (call.trim().length() == 0) {
            return false;
        }
        boolean mMatch2 = call.trim().toUpperCase(Locale.US).equals(Main.ttyCaller.trim().toUpperCase(Locale.US));
        return mMatch2;
    }

    //Check if we have a strict match with my client's call
    public static boolean matchServerCallWith(String call) {

        if (call.trim().length() == 0) {
            return false;
        }
        boolean mMatch2 = call.trim().toUpperCase(Locale.US).equals(Main.callsignAsServer.trim().toUpperCase(Locale.US));
        return mMatch2;
    }

    
    //Check if we have a strict match between two call signs
    public static boolean matchCallOneWithCallTwo(String call1, String call2) {

        if (call1.trim().length() == 0 || call2.trim().length() == 0) {
            return false;
        }
        boolean mMatch2 = call1.trim().toUpperCase(Locale.US).equals(call2.trim().toUpperCase(Locale.US));
        return mMatch2;
    }

    
    //Remove any ABCN/ prefixes like FR/ and any suffixes like /p or /mm and reject non-standard callsigns patterns
    public static String cleanCallForAprs(String call) {
        String cleanCall = "";
        //Pattern pc = Pattern.compile(".*([a-z0-9]{1,3}[0-9]\\/)([a-z0-9]{1,3}[0-9][a-z0-9]{0,3}[a-zA-Z])(((\\/MM)|(\\/M)|(\\/PM)|(\\/P))|(\\-[a-z0-9]{1,2}))?.*", Pattern.CASE_INSENSITIVE);
        Pattern pc = Pattern.compile("([a-z0-9]{1,3}[0-9]\\/)?([a-z0-9]{1,3}[0-9][a-z0-9]{0,3}[a-z])(((\\/MM)|(\\/M)|(\\/PM)|(\\/P))|(\\-[a-z0-9]{1,2}))?", Pattern.CASE_INSENSITIVE);
        //System.out.println(Blockline);
        Matcher mc = pc.matcher(call);
        if (mc.lookingAt()) {
            cleanCall = mc.group(2).toUpperCase(Locale.US);
            if (mc.group(9) != null) {
                //We have an APRS id (E.g. -12), add it
                cleanCall += mc.group(9);
            }
        }
        return cleanCall;
    }


} // end Main class

