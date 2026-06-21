/*
 * Modem.java
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
import java.io.InputStream;
import static java.lang.Math.abs;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/**
 *
 * @author rein PA0R
 */

/*  public methods:
 * SendLine(String) : send a block
 * (String) GetMessage : get a block from the queue
 *      returns "none" if queue is empty 
 * (boolean) checkBlock : returns true is message in queue
 */
public class Modem implements Runnable {

    //static config c; // Static config object
    public String outLine = "";
    public PrintWriter pout;
    public InputStream in;
    static Config configuration; // Static config object
    static final int MAXQUEUE = 8;
    private Vector<String> messages = new Vector<String>();
    private String BlockString;
    private String newLine;
    private String rxIDstart = "<cmd><rsid>";
    private String rxIDend = "</rsid></cmd>";
    private String txIDstart = "<cmd><txrsid>";
    private String txIDend = "</txrsid></cmd>";
    private String modeIDstart = "<cmd><mode>";
    private String modeIDend = "</mode></cmd>";
    private String nextTxRsidCommand = "";
    private long blockstart;
    private long blocktime;
    public int firstCharDelay = 4;
    private double validPskmailRxDelay = 0.0f;
    public boolean receivingStatusBlock = false;
    public  boolean BlockActive = false;
    public boolean possibleCwFrame = false;
    private StringBuilder cwStringBuilder = new StringBuilder(100);
    static String[] fldigimodes = {"unknown", "THOR 8>", "MFSK-16>", "THOR 22>", "MFSK-32>",
        "PSK-250R>", "PSK-500R>", "BPSK-500>", "BPSK-250>", "BPSK-125>",
        "BPSK-63>", "PSK-125R>", "MFSK-64>", "THOR 11>", "THOR 4>", "Contestia>",
        "BPSK1000>", "PSK63RC5>", "PSK63RC10>", "PSK250RC3>",
        "PSK125RC4>", "DominoEX 22>", "DominoEX 11>"};

    public final ModemModesEnum[] pmodes = {ModemModesEnum.PSK500R,
        ModemModesEnum.THOR8,
        ModemModesEnum.MFSK16,
        ModemModesEnum.THOR22,
        ModemModesEnum.MFSK32,
        ModemModesEnum.PSK250R,
        ModemModesEnum.PSK500R,
        ModemModesEnum.PSK500,
        ModemModesEnum.PSK250,
        ModemModesEnum.PSK125,
        ModemModesEnum.PSK63,
        ModemModesEnum.PSK125R,
        ModemModesEnum.MFSK64,
        ModemModesEnum.THOR11,
        ModemModesEnum.DOMINOEX5,
        ModemModesEnum.CTSTIA,
        ModemModesEnum.PSK1000,
        ModemModesEnum.PSK63RC5,
        ModemModesEnum.PSK63RC10,
        ModemModesEnum.PSK250RC3,
        ModemModesEnum.PSK125RC4,
        ModemModesEnum.DOMINOEX22,
        ModemModesEnum.DOMINOEX11,};
    public final String[] smodes = {"     ", "THOR8", "MFSK16", "THOR22", "MFSK32", "PSK250R",
        "PSK500R", "PSK500", "PSK250", "PSK125",
        "PSK63", "PSK125R", "MFSK64", "THOR11", "DOMINOEX5", "CTSTIA", "PSK1000",
        "PSK63RC5", "PSK63RC10", "PSK250RC3", "PSK125RC4", "DOMINOEX22", "DOMINOEX11"};
    public final String[] qmodes = {"0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9",
        "a", "b", "c", "d", "n", "f", "g",
        "h", "i", "j", "k", "l", "m"};

    private String notifier = "";
    private int rxchars = 0;
    private double snr = 0.0;
    private double snrsd = 0.0;
    static boolean c_escape = false;
    static String accu = "";
    static Pattern MODEM;
    public Amp2 amp;
    private static Process fldigiProc = null;
    private String fldigiHost;
    private int fldigiPort;
    private BufferedReader procErrReader = null;
    //private Socket sock = null;
    private boolean fldigiRunning = false;
    private boolean cantLaunchFldigi = false;
    private boolean warnedOnce = false;
    private Thread myOutputStreamThread = null;
    public boolean commToFldigiOpened = false; // Use this to suppress errors when the port is closed
    public RMsgObject txMessage = null;
    public long expectedReturnToRxTime = 0L;  
    
    public static final char SOH = 1;
    public static final char EOT = 4;
    public static final char DC1 = 17; //Olivia SOH replacement (used in jPskmail due to Fldigi limitations of Olivia's character set)
    public static final char DC2 = 18;
    public static final char DC3 = 19; //Olivia EOT replacement of

    //RadioMsg stuff
    //Picture transfer conversion of speed to SPP (Samples Per Pixel) and vice-versa
    //map from speed to SPP with defaults to 2 SPP in case of errors
    public static final int[] speedtoSPP = {2, 8, 4, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 16, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 32};
    //int[] SPPtoSpeed = {0,8,4,0,2,0,0,0,1}; //map from SPP to Xy speed display
    public static final int[] SPPtoSpeed = {0, 8, 4, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32}; //map from SPP to Xy speed display
    //The following value MUST Match the MAXMODES in modem.h
    public static final int MAXMODES = 300;
    //List of modems and modes returned from the C++ modems
    public static int[] modemCapListInt = new int[MAXMODES];
    public static String[] modemCapListString = new String[MAXMODES];
    public static int numModes = 0;
    //Custom list of modes as selected in the preferences (can include all modes above if
    //  "Custom List" is not selected, or alternatively all modes manually selected in preferences)
    public static int[] customModeListInt = new int[MAXMODES];
    public static String[] customModeListString = new String[MAXMODES];
    public static int customNumModes = 0;
    public static int minImageModeIndex;
    public static int maxImageModeIndex;
    
    //UTF-8 handling
    private int utfExpectedChars = 0;
    private int utfFoundChars = 0;
    private byte[] utf8PartialBuffer = new byte[20];
    private int utfLen = 0;
    
    //To abandon reception of malformed messages (prevents scanning and sending)
    private static Pattern validDataLinesPattern = Pattern.compile("^([a-p]{4})|(via\\:)|(rly\\:)|(sms\\:)|(id\\:.)|(rd\\:.)|(ro\\:.)|(pos\\:)|(pic\\:)|(tim\\:)");

//  public int MAXDCD = 3;
    Modem(String host, int port) {

        fldigiHost = host;
        fldigiPort = port;

        this.amp = new Amp2();
        amp.init();
        
        //   System.out.println(host);
        //   System.out.println(port);
        //MODEM = Pattern.compile("Mode:(.*)>");
        MODEM = Pattern.compile(".*\\<Mode:(.+)\\>.*");
        int launchResult = 0;
        int launchCount = 0;

        while (!commToFldigiOpened && launchCount++ < 1) {
            try {
                launchResult = connectToFldigi();
                //Prevent premature killing of Fldigi. Start timeout at modem launch
                Main.lastCharacterTime = System.currentTimeMillis();
                //if (launchResult == -1) {
                //    opened = true;
                //}
                Sendln("<cmd>server</cmd>");
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    //Nothing
                }
                // initialize modem
                char SOC = (char) 26; // Start of command
                char EOC = (char) 27; // End of command
                String pskmailon = SOC + "MULTIPSK-OFF" + EOC;
                // make instance of config
                //c = new config(Main.HomePath + Main.Dirprefix);
                //System.out.println("Modem initialized.");       
            } catch (IOException e) { //exception thrown by connectToFldigi() above
                commToFldigiOpened = false;
                System.out.println("Error connecting to host.");
                //We launch Fldigi automatically if path is provided
                if (launchResult == -1) {
                    Main.log.writelog("Could not connect to modem, only offline mode available.\n Please start fldigi and then restart this application for online work.", true);
                } else if (launchResult == -2) {
                    Main.log.writelog("Could not connect to modem, only offline mode available.\n Please start fldigi and then restart this application for online work.", true);
                } else {
                    Main.log.writelog("Manual connection only.\n Please start fldigi and then restart this application for online work.", true);
                }
            }
        }
    }

    
    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    private int connectToFldigi() throws IOException {
        int result = 0; //0 = Failed to start when it should have

        //if (Main.WantRelayOverRadio | Main.WantRelaySMSs | Main.WantRelayEmails | Main.WantServer) {
        if (getJavaVersion() >= 7 && Main.configuration.getPreference("FLDIGIAPPLICATIONPATH", "").trim().length() > 0) {
            //Fldigi path not blank, we should launch Fldigi automatically
            int launchResult = killAndLaunchFldigi();
            if (launchResult == -1) {
                //Preset time counter
                Main.lastModemCharTime = System.currentTimeMillis();
                //int exitCode = fldigiProc.waitFor();
                //(re-)init Rigctl
                RigCtrl.Rigctl();
            } else if (launchResult == +1) {
                Main.log.writelog("Can't Launch Fldigi, check program path and name in options", true);
            }
        } else {
            //Manual start required or done before launching this app, check if we can connect to a running Fldigi
            result = -2;
            try {
                //We may have a listening socket, make the socket objects
                Socket sock = new Socket(fldigiHost, fldigiPort);
                OutputStream out = sock.getOutputStream();
                in = sock.getInputStream();
                pout = new PrintWriter(out, true);
                commToFldigiOpened = true;
                //fldigiRunning = true;
                //Reset mode as server
                Sendln("<cmd>server</cmd>");
            } catch (IOException e) {
                commToFldigiOpened = false;
            }
        }
        if (Main.configuration.getPreference("FLDIGIAPPLICATIONPATH", "").trim().length() > 0
                && getJavaVersion() < 7 && !warnedOnce) {
            Main.log.writelog("Installed Java version is below version 1.8, Can't launch Fldigi Automatically.", true);
            warnedOnce = true;
        }

        return result;
    }

    private int killAndLaunchFldigi() {
        int result = -1;
      
        /*
        //Must first close the previous monitoring thread
        //Close stream now if it was open
        try {
            if (procErrReader != null) {
                procErrReader.close();
                procErrReader = null;
                Thread.sleep(500);
                System.out.println("Closed error stream, interrupted console thread in K&L Fldigi");
                if (myOutputStreamThread != null) {
                    myOutputStreamThread.interrupt();
                }
                Thread.sleep(500);
            }
            
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        */
        //New listening thread looking for error stream data (e.g. we can't launch Fldigi)
        myOutputStreamThread = new Thread() {
            @Override
            public void run() {
                cantLaunchFldigi = false;
                String outProcLine2 = "";
                //Boolean readerOpen = false;
                String fldigiPath = Main.configuration.getPreference("FLDIGIAPPLICATIONPATH", "fldigi");
                //Remove parameters
                Pattern ppc = Pattern.compile("\\s*(\\S+)\\s*");
                Matcher mpc = ppc.matcher(fldigiPath);
                if (mpc.lookingAt()) {
                    fldigiPath = mpc.group(1);
                }
                //Wait until we start reading data from error (input?) buffer
                //VK2ETA debug while (!readerOpen && !commToFldigiOpened) {
                while (!commToFldigiOpened) {
                    try {
                        Thread.sleep(100);
                        if (procErrReader != null) {
                            outProcLine2 = procErrReader.readLine();
                            if (outProcLine2 != null) { 
                                //System.out.println("0AA - Read line: " + outProcLine2);
                                //VK2ETA debug readerOpen = true;
                                if (outProcLine2.contains(fldigiPath) 
                                        && outProcLine2.toLowerCase(Locale.US).contains(" no such file")) {
                                    cantLaunchFldigi = true;
                                    System.out.println("No such file - 0B: " + fldigiPath);
                                }
                            }
                        }
                    } catch (IOException e) {
                        //Expected until the process if really launched (can be several seconds)
                        //e.printStackTrace();
                    } catch (InterruptedException e) {
                    } catch (Exception e) {
                        cantLaunchFldigi = true;
                        //e.printStackTrace();
                    }
                }
            }
        };

        Thread fldigiLaunchThread = new Thread() {
            @Override
            public void run() {
                //Kill
                try {
                    if (fldigiProc != null) {
                        try {
                            fldigiProc.destroyForcibly();
                        } catch (NoSuchMethodError er) {
                            //For Java version < 1.8
                            //System.out.println("Using .destroy method");
                            fldigiProc.destroy();
                            //Main.log.writelog("Problem with task kill: " + er, true);
                        }
                        //Wait until the socket is closed
                        System.out.println("Sent Kill Signal - waiting end of err stream - 1E.2");
                        while (procErrReader != null && (procErrReader.readLine()) != null) {
                            Thread.sleep(100);
                        };
                    }
                } catch (IOException e) {
                    //We have a closed stream due to the process terminating
                    //Expect to get one hit at the end of the launch process
                    try {
                        if (procErrReader != null) {
                            System.out.println("IO Exception in Err stream - Closing ");
                            procErrReader.close();
                            procErrReader = null;
                        }
                    } catch (IOException ee) {
                    }
                    //e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*
                //Close stream now if it was open
                try {
                    if (procErrReader != null) {
                        procErrReader.close();
                        procErrReader = null;
                    }
                } catch (IOException e) {
                }
                 //Kill any listening thread               
                if (myOutputStreamThread != null && myOutputStreamThread.isAlive()) {
                    try {
                        //Close output stream first
                        if (procErrReader != null) {
                            procErrReader.close();
                            procErrReader = null;
                        }
                        Thread.sleep(1000);
                        System.out.println("Closed error stream, interrupting Console thread - 1E.3");
                        myOutputStreamThread.interrupt();
                        Thread.sleep(1000);
                    } catch (IOException e1) {
                        System.out.println("IOException while closing errorstream - 1EA");
                    } catch (InterruptedException e) {
                        //
                    }
                }
                */
                //Now (re-)launch Fldigi
                fldigiProc = null;
                fldigiRunning = false;
                try {
                    String fldigiPath = Main.configuration.getPreference("FLDIGIAPPLICATIONPATH", "fldigi");
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    if (Main.onWindows) {
                        processBuilder.command(fldigiPath);
                        fldigiProc = processBuilder.start();
                        //Launching using cmd.exe creates a subprocess for fldigi that is un-killable from Java
                        //fldigiProc = Runtime.getRuntime().exec(fldigiPath);
                    } else { //Linux
                        processBuilder.command("bash", "-c", fldigiPath);
                        fldigiProc = processBuilder.start();
                    }
                    //fldigiProc = Runtime.getRuntime().exec("fldigi");
                    procErrReader = new BufferedReader(new InputStreamReader(fldigiProc.getErrorStream()));
                } catch (IOException e) {
                    cantLaunchFldigi = true;
                    e.printStackTrace();
                }
            }
        };
        //Clear running flag as we expect to kill the Fldigi task first
        fldigiRunning = false;
        myOutputStreamThread.start();
        myOutputStreamThread.setName("FldigiConsole");
        fldigiLaunchThread.start();
        fldigiLaunchThread.setName("FldigiLaunch");
        //Wait up to 60 seconds for launch
        int waitCount = 0;
        commToFldigiOpened = false;
        cantLaunchFldigi = false; //debug added to ensure proper processing of loop below
        System.out.println("Launched Fldigi Threads - 2A");
        while (!cantLaunchFldigi && !commToFldigiOpened && ++waitCount < 90) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //We should have a listening socket, make the socket objects
                Socket sock = new Socket(fldigiHost, fldigiPort);
                OutputStream out = sock.getOutputStream();
                in = sock.getInputStream();
                pout = new PrintWriter(out, true);
                commToFldigiOpened = true;
                fldigiRunning = true;
            } catch (IOException e) {
                //System.out.println("Failed to open sockets to Fldigi. WaitCount: " + waitCount);
                commToFldigiOpened = false;
            }
        }
        //System.out.println("After loop WaitCount: " + waitCount);
        if (waitCount >= 90) {
            //Timeout. Fldigi didn't launch within 60 seconds
            System.out.println("Fldigi didn't launch within 90 seconds.");
            result = 0;
        }
        if (cantLaunchFldigi) {
            result = +1;
        }
        //Kill stream listening thread if not already exited (no use now)
        //System.out.println("Stopping myOutputStreamThread");
        myOutputStreamThread.interrupt();
        //debug
        //System.out.println("Stopping fldigiLaunchThread");
        fldigiLaunchThread.interrupt();
        //Reset transmit flag so that we return to Rx strait away
        Main.TxActive = false;
        System.out.println("Returning Launch code: " + result);
        return result;
    }
 
    public int killFldigi() {
        int result = -1;

        String outProcLine;
        //Kill
        try {
            if (fldigiProc != null) {
                //Re-open the process error stream it it was closed before
                if (procErrReader == null) {
                    procErrReader = new BufferedReader(new InputStreamReader(fldigiProc.getErrorStream()));
                }
                System.out.println("Re-opened process error stream, killing process");
                try {
                    fldigiProc.destroyForcibly();
                } catch (NoSuchMethodError er) {
                    //For Java version < 1.8
                    //System.out.println("Using .destroy method");
                    fldigiProc.destroy();
                    //System.out.println("Can't use destroyForcibly");
                }
                //System.out.println("Wait until the socket is closed");
                //Wait until the socket is closed
                while (procErrReader != null && (outProcLine = procErrReader.readLine()) != null) {
                    Thread.sleep(100);
                };
                System.out.println("Process has shut down");
            }
        } catch (IOException e) {
            //We have a closed stream due to the process terminating
            //Expected, do not print stack trace
            //e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        //Close stream now if it was open
        try {
            if (procErrReader != null) {
                System.out.println("Closing streams");
                procErrReader.close();
                //fldigiInputStreamReader.close();
                //fldigiErrorStream.close();
                procErrReader = null;
            }
        } catch (IOException e) {
            //System.out.println("IO exception closing streams");
        }
        */
        //Mark Fldigi as not running
        fldigiProc = null;
        fldigiRunning = false;
        return result;
    }

    public String getTXModemString(ModemModesEnum mode) {
        try {
            String Txmodemstring = "";
            Txmodemstring = getModemString(mode);
            return Txmodemstring;
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Send an RX Rsid command to the modem
     */
    public void setRxRsid(String s) {
        String rsidstart = "<cmd><rsid>";
        String rsidend = "</rsid></cmd>";
        if (!s.equals("")) {
            Main.sendCommand += (rsidstart + s + rsidend);
        }
    }

   /**
    * Set the Tx Rsid now
    */
   public void setTxRsid(String s) {
        //System.out.println("arq.send_txrsid_command: " + s + "\n"); 
        String txrsidstart = "<cmd><txrsid>";
        String txrsidend = "</txrsid></cmd>";
        if (!s.equals("")) {
            Main.sendCommand += (txrsidstart + s + txrsidend);
        }
    }
   
   /**
    * Request that the next transmission use TX Rsid or not
    */
   public void requestTxRsid(String s) {
        nextTxRsidCommand = s;
    }

    /**
     * Send a mode command to the modem
     */
    public void setModemModeNow(ModemModesEnum mode) throws NullPointerException {
        String modeset = "";

        try {
            switch (mode) {
                case PSK63:
                    modeset = "PSK63";
                    break;
                case PSK125:
                    modeset = "PSK125";
                    break;
                case PSK125R:
                    modeset = "PSK125R";
                    break;
                case PSK250:
                    modeset = "PSK250";
                    break;
                case PSK250R:
                    modeset = "PSK250R";
                    break;
                case PSK500:
                    modeset = "PSK500";
                    break;
                case PSK1000:
                    modeset = "PSK1000";
                    break;
                case PSK500R:
                    modeset = "PSK500R";
                    break;
                case MFSK16:
                    modeset = "MFSK16";
                    break;
                case MFSK32:
                    modeset = "MFSK32";
                    break;
                case THOR8:
                    modeset = "THOR8";
                    break;
                case THOR4:
                    modeset = "THOR4";
                    break;
                case DOMINOEX5:
                    modeset = "DOMINOEX5";
                    break;
                case THOR22:
                    modeset = "THOR22";
                    break;
                case CTSTIA:
                    modeset = "CTSTIA";
                    break;
                case PSK63RC5:
                    modeset = "PSK63RC5";
                    break;
                case PSK63RC10:
                    modeset = "PSK63RC10";
                    break;
                case PSK250RC3:
                    modeset = "PSK250RC3";
                    break;
                case PSK125RC4:
                    modeset = "PSK125RC4";
                    break;
                case DOMINOEX22:
                    modeset = "DOMINOEX22";
                    break;
                case DOMINOEX11:
                    modeset = "DOMINOEX11";
                    break;
                case CW:
                    modeset = "CW";
                    break;
            }
        } catch (NullPointerException npe) {
            Main.log.writelog("Error in arq.send_mode_command", npe, true);
        }

        if (!modeset.equals("")) {
            if (Main.status.equals("Connected") && !Main.q.equals(modeset)) {
                // During connected state
                //             Main.TX_Text += connstr + "\n";                
            } else {
                // Order a mode switch (could be second press too)
                //Main.SendCommand += modestart + modeset + modeend;
                //Main.m.Sendln(Main.SendCommand);
                Main.m.Sendln("", modeset, "");
            }

            // Save the new mode
            Main.q.Modem = modeset;
        }
    }

    private Object sendMutex = new Object();

    // Send routine with pre-set RSIDs and Mode
    public void Sendln(String outLine) {

        synchronized (sendMutex) {
            try {
                if (commToFldigiOpened & outLine.length() > 0) {
                    //            System.out.println(outLine);
                    pout.println(outLine);
                    if (outLine.contains("<cmd><mode>")) {
                        try {
                            //Thread.sleep(1000);
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (!outLine.contains("<cmd>")) {
                        //Must be a data block, reset receive marker of RSID for next RX
                        //VK2ETA: TO-DO: check if logic (and place of decision) is consistent with RadioMsg
                        Main.justReceivedRSID = false;
                        //Calculate the expected return time to Rx (to prevent TX lockups). Add 50% margin.
                        //expectedReturnToRxTime = System.currentTimeMillis() + (3000 * (long) (getTxTimeEstimate(Main.txModem, outLine.length()))) / 2;
                        long txTime = (3000 * (long)(getTxTimeEstimate(Main.txModem, outLine.getBytes("UTF-8").length)));
                        System.out.println("txTime: " + txTime);
                        expectedReturnToRxTime = System.currentTimeMillis() + txTime;
                    }
                }
            } catch (Exception ex) {
                Main.log.writelog("Could not send frame, fldigi not running or busy?", ex, true);
            }
        }
    }
    
    // Send routine with Mode and TxRsid as parameters
    public void Sendln(String outLine, String modemString, String TxRsidString) {

        synchronized (sendMutex) {
            try {
                //if (opened & outLine.length() > 0) {
                if (commToFldigiOpened) {
                    //System.out.println(outLine);
                    if (!modemString.equals("")) {
                        pout.println(modeIDstart + modemString + modeIDend);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //Set Tx RSID base on previous request OR current send command (priority)
                    if (!TxRsidString.equals("") || !nextTxRsidCommand.equals("")) {
                        String txRdis = TxRsidString.equals("") ? nextTxRsidCommand : TxRsidString;
                        pout.println(txIDstart + txRdis + txIDend);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!outLine.equals("")) {
                        if (!outLine.contains("<cmd>")) {
                            //Must be a data block, reset receive marker of RSID for next RX
                            //VK2ETA: TO-DO: check if logic (and place of decision) is consistent with RadioMsg
                            Main.justReceivedRSID = false;
                            ModemModesEnum txMode = Main.convmodem(modemString);
                            //Calculate the expected return time to Rx (to prevent TX lockups). Add 50% margin.
                            long txTime = (3000 * (long)(getTxTimeEstimate(txMode, outLine.getBytes("UTF-8").length)));
                            System.out.println("txTime: " + txTime);
                            expectedReturnToRxTime = System.currentTimeMillis() + txTime;
                            //Olivia and MT-63 modes, replace SOH by DC1 and EOT by DC3 due to limitation in Olivia's and MT-63's character set in Fldigi (ch > 7 only)
                            if (modemString.startsWith("OLIV") || modemString.startsWith("MT63")) {
                                outLine = outLine.replace(SOH, DC1).replace(EOT, DC3);
                            }
                        }
                        pout.println(outLine);
                    }
                }
            } catch (Exception ex) {
                Main.log.writelog("Could not send frame, fldigi not running or busy?", ex, true);
            }
        }
    }

    public String getModemString(ModemModesEnum mode) {
        int i;
        String modemString = "unknown";
        for (i = 1; i < pmodes.length; i++) {
            if (mode == pmodes[i]) {
                modemString = smodes[i];

                return modemString;
            }
        }
        return "";
    }

    public String getAltModemString(ModemModesEnum mode) {
        int i;
        String modemString = "unknown";
        for (i = 1; i < 9; i++) {
            if (mode == pmodes[i]) {
                modemString = Main.AltModes[i];
                return modemString;
            }
        }
        return "";
    }

    public void setcurrentmodetable(String modes) {
        for (int i = 0; i < Main.currentModes.length; i++) {
            Main.currentModes[i] = "      ";
        }

        for (int i = 0; i < modes.length(); i++) {
            if (modes.substring(i, i + 1).equals("0")) {
                Main.modes[modes.length() - i] = "default";
            } else if (modes.substring(i, i + 1).equals("1")) {
                Main.modes[modes.length() - i] = "THOR8";
            } else if (modes.substring(i, i + 1).equals("2")) {
                Main.modes[modes.length() - i] = "MFSK16";
            } else if (modes.substring(i, i + 1).equals("3")) {
                Main.modes[modes.length() - i] = "THOR22";
            } else if (modes.substring(i, i + 1).equals("4")) {
                Main.modes[modes.length() - i] = "MFSK32";
            } else if (modes.substring(i, i + 1).equals("5")) {
                Main.modes[modes.length() - i] = "PSK250R";
            } else if (modes.substring(i, i + 1).equals("6")) {
                Main.modes[modes.length() - i] = "PSK500R";
            } else if (modes.substring(i, i + 1).equals("7")) {
                Main.modes[modes.length() - i] = "PSK500";
            } else if (modes.substring(i, i + 1).equals("8")) {
                Main.modes[modes.length() - i] = "PSK250";
            } else if (modes.substring(i, i + 1).equals("9")) {
                Main.modes[modes.length() - i] = "PSK125";
            } else if (modes.substring(i, i + 1).equals("a")) {
                Main.modes[modes.length() - i] = "PSK63";
            } else if (modes.substring(i, i + 1).equals("b")) {
                Main.modes[modes.length() - i] = "PSK125R";
            } else if (modes.substring(i, i + 1).equals("c")) {
                Main.modes[modes.length() - i] = "MFSK64";
            } else if (modes.substring(i, i + 1).equals("d")) {
                Main.modes[modes.length() - i] = "THOR11";
            } else if (modes.substring(i, i + 1).equals("n")) {
                Main.modes[modes.length() - i] = "DOMINOEX5";
            } else if (modes.substring(i, i + 1).equals("f")) {
                Main.modes[modes.length() - i] = "CTSTIA";
            } else if (modes.substring(i, i + 1).equals("g")) {
                Main.modes[modes.length() - i] = "PSK1000";
            } else if (modes.substring(i, i + 1).equals("h")) {
                Main.modes[modes.length() - i] = "PSK63RC5";
            } else if (modes.substring(i, i + 1).equals("i")) {
                Main.modes[modes.length() - i] = "PSK63RC10";
            } else if (modes.substring(i, i + 1).equals("j")) {
                Main.modes[modes.length() - i] = "PSK250RC3";
            } else if (modes.substring(i, i + 1).equals("k")) {
                Main.modes[modes.length() - i] = "PSK125RC4";
            } else if (modes.substring(i, i + 1).equals("l")) {
                Main.modes[modes.length() - i] = "DOMINOEX22";
            } else if (modes.substring(i, i + 1).equals("m")) {
                Main.modes[modes.length() - i] = "DOMINOEX11";
            }
//          System.out.println(Main.Modes[modes.length() - i]);
            Main.currentModes = Main.modes;
        }
    }

    public ModemModesEnum getModeOffList(int index) {
        ModemModesEnum mode = Main.defaultmode;
        String mymodem = Main.currentModes[index];
        //for (int i = 0; i < Main.Currentmodes.length; i++) {
            //System.out.println(Main.Currentmodes[i]);
        //}
        //  System.out.println(index);
        //  System.out.println("modem:" + mymodem);

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
        } else if (mymodem.equals("MFSK64")) {
            mode = ModemModesEnum.MFSK64;
        } else if (mymodem.equals("THOR11")) {
            mode = ModemModesEnum.THOR11;
        } else if (mymodem.equals("DOMINOEX5")) {
            mode = ModemModesEnum.DOMINOEX5;
        } else if (mymodem.equals("CTSTIA")) {
            mode = ModemModesEnum.CTSTIA;
        } else if (mymodem.equals("PSK63RC5")) {
            mode = ModemModesEnum.PSK63RC5;
        } else if (mymodem.equals("PSK63RC10")) {
            mode = ModemModesEnum.PSK63RC10;
        } else if (mymodem.equals("PSK250RC3")) {
            mode = ModemModesEnum.PSK250RC3;
        } else if (mymodem.equals("PSK125RC4")) {
            mode = ModemModesEnum.PSK125RC4;
        } else if (mymodem.equals("DOMINOEX22")) {
            mode = ModemModesEnum.DOMINOEX22;
        } else if (mymodem.equals("DOMINOEX11")) {
            mode = ModemModesEnum.DOMINOEX11;
        }
        return mode;
    }

    public int getModemPos(ModemModesEnum mode) {
        int i;
        int modemPos = 0;
        for (i = 1; i < 9; i++) {
            if (mode == pmodes[i]) {
                modemPos = i;
                return modemPos;
            }
        }
        return 0;
    }

    //VK2ETA fix this for alt modes
    public int getAltModemPos(ModemModesEnum mode) {
        int i;
        int modemPos = 0;
        for (i = 1; i < 9; i++) {
            if (mode == pmodes[i]) {
                modemPos = i;
                return modemPos;
            }
        }
        return 0;
    }

    public int getTxTimeEstimate(ModemModesEnum mode, int dataLength) {
        int savedFirstCharDelay = firstCharDelay;
        
        //Get block time for a block size of 64 characters
        //int txTime = getBlockTimeAndDelay(Main.txModem);
        int txTime = getBlockTimeAndDelay(mode);
        //Adjust for the length of the data to be transmitted
        txTime = firstCharDelay + (txTime * dataLength) / 64; //Include FEC delay
        //Restore global value (was calculated based on Rx mode not Tx mode, and is needed for Rx timeouts)
        firstCharDelay = savedFirstCharDelay;
        return txTime + 3; //3 Seconds safety margin
    }
    
    //Returns the theoretical blocktime so that timings are correct when we change RX modes
    public int getBlockTimeAndDelay(ModemModesEnum mode) {
        double cps = 2;
        double myblocktime = 0;

        //VK2ETA: some delays now calculated at connect time firstCharDelay = 4; 
        //Must account for Client's TX delay, RSID and silences, FEC/interleaver delay, decoding delay
        firstCharDelay = 2;
        try {
            switch (mode) {
                case PSK63:
                    cps = 6.5;
                    break;
                case PSK125:
                    cps = 13;
                    break;
                case PSK125R:
                    firstCharDelay = 7;
                    cps = 7;
                    break;
                case PSK250:
                    cps = 26;
                    break;
                case PSK250R:
                    firstCharDelay = 6;
                    cps = 15;
                    break;
                case PSK500:
                    cps = 52;
                    break;
                case PSK500R:
                    firstCharDelay = 5;
                    cps = 31;
                    break;
                case MFSK16:
                    firstCharDelay = 7;
                    cps = 3.4;
                    break;
                case MFSK32:
                    firstCharDelay = 5;
                    cps = 7;
                    break;
                case MFSK64:
                    firstCharDelay = 6;
                    cps = 22;
                    break;
                case THOR4:
                    firstCharDelay = 22;
                    cps = 1;
                    break;
                case THOR8:
                    firstCharDelay = 11;
                    cps = 2;
                    break;
                case THOR16:
                    firstCharDelay = 7;
                    cps = 4;
                    break;
                case THOR22:
                    firstCharDelay = 6;
                    cps = 5.6;
                    break;
                case DOMINOEX5:
                    cps = 4;
                    break;
                case CTSTIA:
                    cps = 1;
                    break;
                case PSK63RC5:
                    firstCharDelay = 10;
                    cps = 16;
                    break;
                case PSK63RC10:
                    firstCharDelay = 10;
                    cps = 31;
                    break;
                case PSK250RC3:
                    firstCharDelay = 10;
                    cps = 30;
                    break;
                case PSK125RC4:
                    firstCharDelay = 10;
                    cps = 25;
                    break;
                case DOMINOEX22:
                    cps = 16;
                    break;
                case DOMINOEX11:
                    cps = 8;
                    break;
            }
        } catch (NullPointerException npe) {
            Main.log.writelog("Error in modem.getBlockTime", npe, true);
        }
        //VK2ETA assumes a maximum 64 character block size!!
        //and delay from start of TX to RXed first character is 9 seconds in MFSK16
        //This is too short for slow modes
        //myblocktime = (64 + 9) / cps;
        myblocktime = (64) / cps;
        return (int) (myblocktime + 0.5);
    }


    private char GetByte() {
        char myChar;

        //Exiting, return 0
        if (Main.exitingSoon) {
            try {
                Thread.sleep(200);
            } catch (Exception e1) {
                //Nothing
            }
            return '\0';
        }
        //Normal run, read a byte
        int back = '\0';
        try {
            //Changed to int to prevent 255(char) to be read as -1(byte)
            //byte back = (byte) in.read();
            back = in.read();
            if (!Main.exitingSoon) {

                //Broken socket?
                if (back == -1 || Main.requestModemRestart) {
                    if (back == -1) {
                        if (fldigiRunning) {
                            //We launched Fldigi automatically
                            System.out.println("Error reading from modem (socket closed -1), restarting Fldigi");
                        } else {
                            //Prevent fast processing of null character
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e1) {
                                //Nothing
                            }
                        }
                    } else {
                        System.out.println("Received request to restart Fldigi");
                    }
                    Main.modemAutoRestartDelay = 0; //Clear auto-restart timer, no point in doubling up
                    Main.requestModemRestart = false; //Clear restart flag
                    try {
                        connectToFldigi();
                        //Reset mode as server
                        Sendln("<cmd>server</cmd>");
                        //And reset RxRsid to ON
                        setRxRsid("ON");
                        //Clear flags again in case they were reset in-between
                        Main.modemAutoRestartDelay = 0; //Clear auto-restart timer, no point in doubling up
                        Main.requestModemRestart = false; //Clear restart flag
                        back = in.read();
//                    System.out.println("Test, 2nd read returns: " + (char) back);
                    } catch (IOException e1) {
                        //Tried but fail, stop trying
                        //e1.printStackTrace();
                        Main.modemAutoRestartDelay = 0; //Clear auto-restart timer, no point in doubling up
                        Main.requestModemRestart = false; //Clear restart flag
                    }
                } else {
                    if (back != 0) {
                        Main.lastModemCharTime = System.currentTimeMillis();
                    }
                }
                    myChar = (char) back;
                //if (myChar == 2) {
                //    Main.stxflag = false;
                //}
                //         System.out.println( back);
                //         System.out.println( myChar);
                return myChar;
            }
        } catch (IOException e) {
            if (!Main.exitingSoon) {
                try {
                    System.out.println("Error reading from modem (IOException), restarting Fldigi: " + e);
                    connectToFldigi();
                    //Reset mode as server
                    Sendln("<cmd>server</cmd>");
                    //And reset RxRsid to ON
                    setRxRsid("ON");
                    //Clear flags again in case they were reset in-between
                    Main.modemAutoRestartDelay = 0; //Clear auto-restart timer, no point in doubling up
                    Main.requestModemRestart = false; //Clear restart flag
                    back = in.read();
                } catch (Exception e3) {
                    //Nothing we try once only
                }
            }
            //Modem was forcibly killed either by operator or the program monitoring
            return '\0';  // should not happen.
        } catch (Exception e) {
            if (cantLaunchFldigi) {
                //Prevent fast processing of null character
                try {
                    Thread.sleep(100);
                } catch (Exception e1) {
                    //Nothing
                }
            } else {
                System.out.println("Error reading from modem (Exception): " + e);
            }
            return '\0';  // should not happen.
        }
        return '\0';  // should not happen.
    }
   
    //Takes the raw RSID <Mode:abcd> string from Fldigi and returns the modem string used to set the mode in Fldigi
    private String rawRsidToModeString(String rawRsidStr) {
        String modeStr = "";
        Pattern mpf = Pattern.compile("^<Mode:([\\w-_\\s]+)>$");
        Matcher mmf = mpf.matcher(rawRsidStr);
        if (mmf.lookingAt() && mmf.group(1).length() > 0) {
            modeStr = mmf.group(1);
            if (modeStr.startsWith("MFSK")) {
                modeStr = modeStr.replace("-", "");
            } else if (modeStr.startsWith("OL ")) {
                modeStr = modeStr.replace("OL ", "OLIV ");
            } else if (modeStr.startsWith("THOR")) {
                modeStr = modeStr.replace(" ", "");
            } else if (modeStr.startsWith("DominoEX ")) {
                modeStr = modeStr.replace("DominoEX ", "DOMINOEX");
            } else if (modeStr.startsWith("BPSK") || modeStr.startsWith("8PSK") || modeStr.startsWith("PSKR")) {
                modeStr = modeStr.replace("B", "").replace("-", "");
            } else if (modeStr.contains("PSK")) {
                //Must be a multicarrier PSK or PSKR mode
                Pattern pskP = Pattern.compile("^(\\d{1,2})x(PSK\\d{2,4}R?)$");
                Matcher pskM = pskP.matcher(modeStr);
                if (pskM.lookingAt() && pskM.group(1) != null && pskM.group(2) != null) {
                    //E.g. 3xPSK250R --> PSK250RC3
                    modeStr = pskM.group(2) + "C" + pskM.group(1);
                }
            } else if (modeStr.startsWith("MT63-1") || modeStr.startsWith("MT63-2")) {
                modeStr = modeStr.replace("000", "XX");
            } else if (modeStr.startsWith("EOT")) {
                Main.receivedRsidAck = true;
            }
        }
      
        return modeStr;
    }

    private void GetBlock() {
        try {

            int inChar = '\0';
            boolean DC2_rcvd = false;
            int first = -1;
            BlockActive = false;
            //int lst = 0;
            int C;
            int B;

            BlockString = "";

            outLine = "<cmd>server</cmd>";
            Sendln(outLine);
            Thread.sleep(10);

            while (true) {

                if (Main.rxModem == ModemModesEnum.CTSTIA) {
                    inChar = GetByte();
                    ModemModesEnum myrxmode = checkMode((char)inChar);
                    if (inChar > 64 & inChar < 73) {
                        first = inChar - 65;
                        inChar = GetByte();
                    }
                    if (inChar > 73 & inChar < 91 & first > -1) {
                        B = (int) inChar;
                        B -= 74;
                        C = B + (first * 16);
                        inChar = (char) C;
                        Main.shown(Character.toString((char)inChar));
                        first = 0;
                        //lst = 0;
                    } else if (inChar == 6) {
                        if (Main.TxActive) {
                            Main.TxActive = false;
                        }
                    } else {// no contestia                      
                        inChar = (char) 178;
                    }
                } //else if (utfExpectedChars == 0 && utfFoundChars == 0) {
                //VK2ETA utf-8 handling
                //Only send complete UTF-8 sequences to Java
                //Behaviour on invalid combinations: discard and re-sync only on valid characters to
                //  avoid exceptions in upstream methods.
                // not contestia and we have used all utf characters or are accumulating more
                inChar = GetByte();
                //}
                if (utfExpectedChars < 1) { //Normally zero
                    //We are not in a multi-byte sequence yet
                    if ((inChar & 0xE0) == 0xC0) {
                        utfExpectedChars = 1; //One extra characters
                        utfLen = 2;
                        utfFoundChars = 0;
                        utf8PartialBuffer[utfFoundChars++] = (byte)inChar;
                        inChar = 0; //No further processing
                    } else if ((inChar & 0xF0) == 0xE0) {
                        utfExpectedChars = 2; //Two extra characters
                        utfLen = 3;
                        utfFoundChars = 0;
                        utf8PartialBuffer[utfFoundChars++] = (byte)inChar;
                        inChar = 0; //No further processing
                    } else if ((inChar & 0xF8) == 0xF0) {
                        utfExpectedChars = 3; //Three extra characters
                        utfLen = 4;
                        utfFoundChars = 0;
                        utf8PartialBuffer[utfFoundChars++] = (byte)inChar;
                        inChar = 0; //No further processing
                    } else if ((inChar & 0xC0) == 0x80) { //Is it a follow-on character?
                        //Should not be there (missing first character in sequence), discard and reset just in case
                        utfExpectedChars = utfFoundChars = utfLen = 0;
                        inChar = 0; //No further processing
                    //} else if ((inChar & 0x80) == 0x00) { //Could be a single Chareacter, check it is legal
                        //decodedBuffer[lastCharPos++] = inChar;
                    //    utf8PartialBuffer[utfFoundChars++] = inChar;
                        //test no. utfExpectedChars = utfFoundChars = 0; //Reset to zero in case counter is negative (just in case)
                    //} else { //Not a legal case, ignore and reset counter
                        //utfExpectedChars = utfFoundChars = 0;
                        //inChar = 0; //Blank that character
                    }
                } else { //we are still expecting follow-up UTF-8 characters
                    if ((inChar & 0xC0) == 0x80) { //Valid follow-on character, store it
                        utfExpectedChars--;
                        utf8PartialBuffer[utfFoundChars++] = (byte)inChar;
                        //Check if we have the whole sequence
                        if (utfExpectedChars == 0 && utfFoundChars >= utfLen) {
                            //Add to data string
                            utf8PartialBuffer[utfFoundChars] = 0;
                            String utfDataPoint = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8PartialBuffer, 0, utfLen)).toString();
                            if (BlockActive) {
                                //Add to string buffer
                                BlockString += utfDataPoint;
                                WriteToMonitor(utfDataPoint);
                                Main.lastCharacterTime = System.currentTimeMillis();
                                //No need to update the display, the next non-utf character will trigger an update
                            }
                        }
                        inChar = 0; //No further processing
                    } else { //Invalid sequence, discard it and start from scratch
                        utfExpectedChars = utfFoundChars = utfLen = 0;
                        inChar = 0; //No further processing
                    }
                    // NO, done above
                    //If we have a complete sequence, add to receive buffer 
                    //if (utfExpectedChars < 1 && utfFoundChars > 0) {
                    //    for (int i = 0; i < utfFoundChars; i++) {
                    //        decodedBuffer[lastCharPos++] = utf8PartialBuffer[i];
                    //    }
                    //    utfExpectedChars = utfFoundChars = 0; //Reset
                    //}
                }
                switch (inChar) {
                    case 0:
                        break; // ignore
                    case DC1:
                        inChar = SOH; //Valid Olivia SOH replacement. Exchange and fall through to processing of normal SOH
                    case SOH:
                        Main.lastCharacterTime = blockstart = System.currentTimeMillis();
                        Main.haveSOH = true;
                        //Just received RSID, restart counting Radio Msg header timeout from now
                        if (Main.possibleRadioMsg > 0L) {
                            Main.possibleRadioMsg = blockstart;
                        }
                        //Create filename (with UTC time zone) in case we have a radio message
                        Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        RMsgProcessor.FileNameString = String.format(Locale.US, "%04d", c1.get(Calendar.YEAR)) + "-"
                                + String.format(Locale.US, "%02d", c1.get(Calendar.MONTH) + 1) + "-"
                                + String.format(Locale.US, "%02d", c1.get(Calendar.DAY_OF_MONTH)) + "_"
                                + String.format(Locale.US, "%02d%02d%02d", c1.get(Calendar.HOUR_OF_DAY),
                                        c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND)) + ".txt";
                        WriteToMonitor("<SOH>");
                        if (!BlockActive) {
                            BlockActive = true;
                            BlockString = "<SOH>";
                        } else {
                            BlockString += "<SOH>";
                            //We continue on a new block, reset timeout counter
                            Main.oldtime = System.currentTimeMillis();
                            Main.RxBlockSize = BlockString.length() - 17;
                            Main.totalBytes += Main.RxBlockSize;
                            try {
                                putMessage(BlockString);
                                BlockString = "<SOH>";
                            } catch (InterruptedException e) {
                            }
                        }
                        //VK2ETA not here
                        //Main.DCD = 0;
                        break;
                    case DC3:
                        inChar = EOT; //Valid Olivia EOT replacement. Exchange and fall through to processing of normal EOT
                    case EOT:
                        //        System.out.println("EOT:" + BlockString);
                        blocktime = (System.currentTimeMillis() - blockstart);
                        //VK2ETA debug extra status send when TXing long data in slow mode from server
                        Main.oldtime = System.currentTimeMillis();
                        Main.haveSOH = false;                        //Just received RSID, restart counting Radio Msg header timeout from now
                        Main.possibleRadioMsg = 0L;
                        //VK2ETA testing symnc between rx and tx
                        //Main.receivingRadioMsg = false;
                        receivingStatusBlock = false;
                        Main.blockval = blocktime;
                        WriteToMonitor("<EOT>\n");
                        if (BlockActive) {
                            BlockString += "<EOT>";
                            //RM reset block reception if active
                            BlockActive = false;
                            Main.EotRcved = true;
//VK2ETA simplify DCD         if (!Main.Connected) {
//                                Main.DCD = 2;
//                            } else {
//                                Main.DCD = 0;
//                            }
                            try {
                                putMessage(BlockString);
                                //                               System.out.println("\n" + BlockString);
                            } catch (InterruptedException e) {
//                                System.out.println("Problem writing to queue");
                            }

                            BlockString = "";
                        }
                        break;
                    case 6:
                        //Returning from TX
                        expectedReturnToRxTime = 0L; //Consume event
                        System.out.println("Returned from Tx (Char 6 received)");
                        //Start timeout count for TTY server mode (but update only once)
                        if (Main.TxActive) {
                            Main.oldtime = System.currentTimeMillis();
                        }
                        Main.m.receivingStatusBlock = false; //Reset now as there may not be an EOT to reset it
                        //VK2ETA not here
                        //Main.DCD = 0;
                        //Moved below modem command so that main loop does not interfere with it
                        //Main.TXActive = false;
                        if (Main.summoning) {
                            Main.summoning = false;
                            System.out.println("Summoning, change freq");
                            Main.setFreq(Main.freqStore);
                        }
                        //String myrxmodem = Main.RxModemString;
                        //VK2ETA review need for config value usage here (legacy?)
                        //  if (!myrxmodem.equals("") & !Main.configuration.getBlocklength().equals("0")) {
                        //Sendln("<cmd><mode>" + myrxmodem + "</mode></cmd>\n");
                        System.out.println("Sending mode: " + Main.rxModemString.trim());
                        Sendln("", Main.rxModemString.trim(), "");
                        //Reset Rx timeout counter
                        Main.lastCharacterTime = System.currentTimeMillis();
                        //And restart rxdelay counter if we are in a server session
                        if (!Main.ttyConnected.equals("")) {
                            Main.rxDelayCount = Main.rxDelay;
                        } else {
                            //Consume RadioMsg inter-TX delay if any
                            Main.rxDelayCount = Main.RadioMsgAcksDelay;
                            Main.RadioMsgAcksDelay = 0;
                        }
                        //Ensure we don't have EOT RSID enabled for next transmission
                         RigCtrl.setEotRsid(false);
                         //Rset sending Ack flag
                         Main.sendingAcks = false;
                        //Reset TxRSID as it is OFF by default and needs to be enabled when required
                        Main.m.requestTxRsid("OFF");
                        //Does not solve intermittent TXID issue
                        Main.TxActive = false;
                        //Save Radio  Message in Sent Box
                        saveSentRadioMSg();
                        break;
                    case 31:
                        WriteToMonitor("<US>");
                        break;
                    case 10:
                    case 13:
                        WriteToMonitor("\n");
                        if (BlockActive) {
                            BlockString += "\n";
                            Main.lastCharacterTime = System.currentTimeMillis();
                            //RM check if we have the start of a RadioMessage block (and not while in a connected session)
                            if (!Main.receivingRadioMsg && Main.ttyConnected.equals("") && !Main.connected) {
                                Matcher msc = RxBlock.validRMsgHeader.matcher(BlockString);
                                if (msc.find()) {
                                    //Processor.CrcString = msc.group(1);
                                    Main.receivingRadioMsg = true;
                                    //Reset time of RSID since we now know it is a Radio Message.
                                    Main.possibleRadioMsg = 0L;
                                }
                            }
                        } else if (Main.wantbulletins) {
                            //Main.Bul.get("" + inChar); 
                            //String dummy = amp.get("" + inChar);
                        }
                        //Reset Accumulation of new line characters for checking radio message format
                        newLine = "";
                        //VK2ETA not here
                        //Main.DCD = 0;
                        break;
                    case DC2:
                        DC2_rcvd = true;
                        break;
                    default:
                        if (DC2_rcvd) {
                            notifier += (char) inChar;
                            //Not found char(62) ">" yet, check length
                            if (notifier.length() > 35) {
                                //Too long, false positive
                                DC2_rcvd = false;
                                notifier = "";
                            }
                            if ((char) inChar == 62) { // ">" character
                                DC2_rcvd = false;
//                               System.out.println(notifier);
                                //    <s2n: 58, 100.0, 0.0>
                                if (notifier.contains("s2n: ")) {
                                    notifier = notifier.substring(6);
                                    Pattern mpf = Pattern.compile("(\\d*), (\\d+\\.\\d), (\\d+\\.\\d).*");
                                    Matcher mmf = mpf.matcher(notifier);
                                    if (mmf.lookingAt()) {
                                        rxchars = Integer.parseInt(mmf.group(1));
                                        if (rxchars > 8) {
                                            snr = Float.valueOf(mmf.group(2).trim()).floatValue();
                                            snrsd = Float.valueOf(mmf.group(3).trim()).floatValue();
                                            Main.snr = snr - snrsd;
                                        }
                                    }
                                } else { //We must have received an RSID                         
                                    //Interpret the text after "<Mode:"
                                    Main.lastRsidReceived = rawRsidToModeString(notifier);
                                    //          System.out.println(notifier);
                                    if (Main.receivedRsidAck) {
                                        //Display an a message in the modem section of the screen and in the bottom status line
                                        Main.monitorText += "\n*** Message Received *** ";
                                        //Run on UI thread
                                        java.awt.EventQueue.invokeLater(new Runnable() {
                                            public void run() {
                                                Main.mainui.addToStatusField("Message_Received_rsid_ack", 5);
                                            }
                                        });
                                        Main.receivedRsidAck = false; //Reset
                                    } else {
                                        Main.lastRsidTime = Main.possibleRadioMsg = System.currentTimeMillis();
                                        //Open squelch...a frame may be coming
                                        RigCtrl.SetSqlLevel(Main.SQL_FLOOR);
                                        //Reset receiving radio message as we are getting a new message in all logic and the RSID would have resetted the modem anyway
                                        Main.receivingRadioMsg = false;
                                        Main.haveSOH = false;
                                        Main.receivedRsidAck = false;
                                        int mi = getmodeindex(notifier);
                                        //          System.out.println(mi);
                                        if (mi < 16 & mi > 0) {
                                            Main.justReceivedRSID = true;
                                            //As the client always sends an RSID in connect phase, calculate
                                            // the Client's delay to send an RSID so that we adapt to its timing.
                                            //This may be of value when using repeaters with long hang times. 
                                            // The client can then safely increase the Tx delay. Useful for slow CPUs too.
                                            if (Main.Connecting && Main.oldtime > 0) {
                                                validPskmailRxDelay = (System.currentTimeMillis() - Main.oldtime) / 1000;
                                                if (validPskmailRxDelay < 10.0f && validPskmailRxDelay >= 0.0f) { //Max 10 seconds delay
                                                    //With average
                                                    //Main.RxDelay = decayAverage(Main.RxDelay, validPskmailRxDelay, 3);
                                                    //Without average
                                                    Main.rxDelay = validPskmailRxDelay + 0; //Allow for RSID silence and processing
                                                }
                                            }
                                            Main.rxModem = pmodes[mi];
                                            Main.rxModemString = smodes[mi];
                                            //Mark time when we received an RSID to block mode and 
                                            // frequency change until we are sure we are not receiving a
                                            // Radio Message.
                                        } else if (mi == 0) { //False alarm, not a valid RSID or not a unsupported mode
                                            Main.txModem = Main.rxModem;
                                        }
                                    }
                                }
                                notifier = "";
                            }
                        }
                        if ((char) inChar > 31 & !DC2_rcvd) {
                            if (Main.wantbulletins) {
                                try {
//                                    Main.Bul.get("" + inChar);
//                                    String dummy = amp.get("" + inChar);
                                } catch (NullPointerException ex) {
                                    Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            //Build sequence for CW APRS messages
                            if (possibleCwFrame) {
                                //Continue building the sequence and reject if does not conform to expected format
                                cwStringBuilder.append((char) inChar);
                                if (cwStringBuilder.length() > 68) { //Around 50 characters max for status or message
                                    //Too long, erase and reset flag
                                    cwStringBuilder.setLength(0);
                                    possibleCwFrame = false;
                                } else if (cwStringBuilder.length() > 5
                                        && cwStringBuilder.toString().contains("VVV--")) {
                                    //Align start at last discovery of header start (in the case of 
                                    //  a send error, the opertor can ignore the rest of the message and 
                                    //  restart a new hearder "VVV--" strait away)
                                    int lastStart = cwStringBuilder.lastIndexOf("VVV--");
                                    if (lastStart > 0) {
                                        cwStringBuilder.delete(0, lastStart);
                                    }
                                    //Check if we have a complete APRS beacon sequence
                                    Pattern cwBeaconPattern = Pattern.compile("VVV--([a-zA-Z0-9]{1,3}[0123456789][a-zA-Z0-9]{0,3}[a-zA-Z])\\/([a-rA-R]{2}[0-9]{2}[a-xA-X]{2})\\/([^\\/]{1,40})\\/([0-9]{1,2})([A-Z]{1})");
                                    Matcher cwBeaconMatcher = cwBeaconPattern.matcher(cwStringBuilder.toString());
                                    if (cwBeaconMatcher.lookingAt()) {
                                        //Check the data length against the sent value
                                        int sentDataLen;
                                        try {
                                            sentDataLen = Integer.parseInt(cwBeaconMatcher.group(4));
                                        } catch (NumberFormatException e) {
                                            sentDataLen = 0;
                                        }
                                        int dataLen = (cwBeaconMatcher.group(1) + "/"
                                                + cwBeaconMatcher.group(2) + "/"
                                                + cwBeaconMatcher.group(3) + "/").length();
                                        //Check the conversion to GPS coordinates
                                        String gpsLatLon = NmeaParser.grid6UpperToGps(cwBeaconMatcher.group(2));
                                        String scall = Main.cleanCallForAprs(cwBeaconMatcher.group(1));
                                        if (sentDataLen == dataLen && gpsLatLon.length() > 0
                                                && scall.length() > 0) {
                                            //Build the new block with the received information
                                            //<SOH>00uVK2ETA:26 !2700.00S/13300.00E.Test 1FDF9<EOT>
                                            BlockString = "00u" + scall
                                                    + ":26 !" + gpsLatLon + cwBeaconMatcher.group(5)
                                                    + cwBeaconMatcher.group(3);
                                            String check = Main.q.checksum(BlockString);
                                            BlockString = "<SOH>" + BlockString + check + "<EOT>";
                                            possibleCwFrame = false; //Reset flag
                                            Main.isCwFrame = true;
                                            putMessage(BlockString);
                                            BlockString = "";
                                        } else {
                                            //Send NACK
                                            Main.isCwFrame = true;
                                            Main.q.send_NACK_reply();
                                        }
                                        //Consume data
                                        cwStringBuilder.setLength(0);
                                    }
                                    //Same for short email message
                                    Pattern cwAprsMsgPattern = Pattern.compile("VVV--([a-zA-Z0-9]{1,3}[0123456789][a-zA-Z0-9]{0,3}[a-zA-Z])\\/25\\/([a-zA-Z0-9\\-\\_\\.]{1,30})\\/([a-zA-Z0-9\\.]{1,30})\\/([a-zA-Z0-9\\.]{1,30})\\/([^\\/]{1,40})\\/([0-9]{1,2})NNNN");
                                    Matcher cwAprsMsgMatcher = cwAprsMsgPattern.matcher(cwStringBuilder.toString());
                                    if (cwAprsMsgMatcher.lookingAt()) {
                                        //Check the data length against the sent value
                                        int sentDataLen;
                                        try {
                                            sentDataLen = Integer.parseInt(cwAprsMsgMatcher.group(6));
                                        } catch (NumberFormatException e) {
                                            sentDataLen = 0;
                                        }
                                        int dataLen = (cwAprsMsgMatcher.group(1) + "/25/"
                                                + cwAprsMsgMatcher.group(2) + "/"
                                                + cwAprsMsgMatcher.group(3) + "/"
                                                + cwAprsMsgMatcher.group(4) + "/"
                                                + cwAprsMsgMatcher.group(5) + "/").length();
                                        String scall = Main.cleanCallForAprs(cwAprsMsgMatcher.group(1));
                                        if (sentDataLen == dataLen && scall.length() > 0) {
                                            //Build the new block with the received information
                                            //"00u" + scall + ":25" + spaces1 + email + spaces2 + body + "\n";
                                            BlockString = "00u" + scall
                                                    + ":25 " + cwAprsMsgMatcher.group(2) + "@"
                                                    + cwAprsMsgMatcher.group(3) + "."
                                                    + cwAprsMsgMatcher.group(4) + " "
                                                    + cwAprsMsgMatcher.group(5) + "\n";
                                            String check = Main.q.checksum(BlockString);
                                            BlockString = "<SOH>" + BlockString + check + "<EOT>";
                                            possibleCwFrame = false; //Reset flag
                                            Main.isCwFrame = true;
                                            putMessage(BlockString);
                                            BlockString = "";
                                        } else {
                                            //Send NACK
                                            Main.isCwFrame = true;
                                            Main.q.send_NACK_reply();
                                        }
                                        //Consume data
                                        cwStringBuilder.setLength(0);
                                    }
                                }
                            } else if ((char) inChar == 'V') {
                                possibleCwFrame = true;
                                cwStringBuilder.append((char) inChar);
                            }
                            WriteToMonitor((char) inChar);
                        }
                        if (BlockActive) {
                            BlockString += (char) inChar;
                            newLine += (char) inChar;
                            Main.lastCharacterTime = System.currentTimeMillis();
                            //            System.out.println("BS:" + BlockString);
                            //Determine if this is a status frame coming
                            if (BlockString.length() == 8) {
                                //String blockType = BlockString.substring(7, 8);
                                receivingStatusBlock = BlockString.substring(7, 8).equals("s");
                            }
                            //Put limit to block size (256 chars plus expansion of multiple <SOH>/<EOT> from
                            //  single character to "<SOH>" or "<EOT>" string (Fldigi specific)
                            if (BlockString.length() > 280) {
                                BlockActive = false;
                                BlockString = "";
                            }
                        }
                        break;
                } // end switch
                //Check that we are not waiting for nothing while expecting a Radio message header
                //Resets if invalid characters are found in the address line, or it is longer than 52 characters
                if (BlockActive && !Main.receivingRadioMsg
                        && BlockString.length() > 3) {
                    Matcher msc = RxBlock.invalidCharsInHeaderPattern.matcher(BlockString);
                    if (msc.find() || BlockString.length() > 52) {
                        //Reset time of RSID since we now know it CANNOT be a Radio Message.
                        Main.possibleRadioMsg = 0L;
                    }
                }
                //Reset if end marker not found within
                // 500 characters of start (A Radio Message is an SMS system not an email one)
                if (Main.receivingRadioMsg && BlockString.length() > 500) {
                    Main.receivingRadioMsg = false;
                }
                //Reset if receiving message and lines are not in valid format ("sms:" or "via:" or "ro:x" etc...)
                if (Main.receivingRadioMsg && newLine.length() == 4) {
                    Matcher msc = validDataLinesPattern.matcher(newLine);
                    if (!msc.find()) {
                        ///malformed Radio Message, reset reception flag to allow scanning or sending.
                        Main.receivingRadioMsg = false;
                    }
                }
            } // end while
        } catch (InterruptedException ex) {
            Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // end GetBlock

    public int getmodeindex(String mode) {

        int modemindex = 0;
        int i;
//     System.out.println(mode.substring(7));
        for (i = 1; i < 21; i++) {
            if (mode.contains(fldigimodes[i])) {
                modemindex = i;
                break;
            }
        }
//     System.out.println("modem=" + modemindex);
        return modemindex;
    }

    public ModemModesEnum getModeFromIndex(int modemindex) {
        ModemModesEnum outmode = ModemModesEnum.PSK500R;
        int maxmodems = Main.currentModes.length;

        if (modemindex >= 0 & modemindex <= maxmodems) {
            outmode = getModeOffList(modemindex);
        } else {
            outmode = Main.txModem;
        }

        return outmode;
    }

    private void WriteToMonitor(char inchar) {
        while (Main.monmutex) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Main.monitorText += Character.toString(inchar);
        //Main.Accu += Character.toString(inchar);
    }

    public void WriteToMonitor(String instr) {
        while (Main.monmutex) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(Modem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Main.monitorText += instr;
    }

    /* Not Used
    public void WriteToMain(String instr) {
        Pattern pcnv = Pattern.compile("&(\\d\\d\\d);");
        Matcher mcvt = pcnv.matcher(instr);
        if (mcvt.lookingAt()) {
            int chrst = Integer.valueOf(mcvt.group(1));
            String str = Integer.toString(chrst);
            String fnd = "&" + mcvt.group(1) + ";";
            int found = instr.indexOf(fnd);
            String strt = instr.substring(0, found);
            String chrstring = instr.substring(found, found + 5);
            String endst = instr.substring(found + 5);
            instr = strt + chrstring + endst;
        }
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
//                                    System.out.print(instr);
        Main.mainwindow += instr;
    }
    */

    private synchronized void putMessage(String BlockString)
            throws InterruptedException {

        while (messages.size() == MAXQUEUE) {
            wait();
        }
        messages.addElement(BlockString);
//     System.out.println("PUT:" + BlockString);
        notify();
    }

    public synchronized String getMessage()
            throws InterruptedException {
//        notify( );  
        if (messages.isEmpty()) {
            return "none";
        }
        String message = messages.firstElement();
        messages.removeElement(message);
        //System.out.println("GET:" + message);
        return message;
    }

    public synchronized boolean checkBlock() {
        if (messages.size() > 0) {
//            System.out.println(messages.size());
            return true;
        } else {
            //           System.out.println(messages.size());
            return false;
        }
    }

    private void saveSentRadioMSg() {
        RMsgObject txedMessage = RMsgTxList.getOldestSent(); //Has to be the one just sent
        //Save message into Sent folder if tx is not aborted and is not selcall
        if (txedMessage != null  && !txedMessage.rxMode.equals("CCIR493")) {
            //Calendar c1 = Calendar.getInstance(TimeZone.getDefault());
            //Adjust time so that is is accurate if using GPS time
            //Not on laptops
            //c1.add(Calendar.SECOND, RadioMSG.DeviceToGPSTimeCorrection);
            //String sentFileNameString = String.format(Locale.US, "%04d", c1.get(Calendar.YEAR)) + "-"
            //        + String.format(Locale.US, "%02d", c1.get(Calendar.MONTH) + 1) + "-"
            //        + String.format(Locale.US, "%02d", c1.get(Calendar.DAY_OF_MONTH)) + "_"
            //        + String.format(Locale.US, "%02d%02d%02d", c1.get(Calendar.HOUR_OF_DAY),
            //                c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND)) + ".txt";
            String sentFileNameString = RMsgProcessor.FileNameString;
            //Save the text part of the message
            String sentFolderPath = Main.homePath
                    + Main.dirPrefix + Main.dirSent + Main.separator;
            txedMessage.fileName = sentFileNameString;
            File msgSentFile = new File(sentFolderPath + sentFileNameString);
            if (msgSentFile.exists()) {
                msgSentFile.delete();
            }
            try {
                FileWriter sentFile = null;
                sentFile = new FileWriter(msgSentFile, true);
                String stringToSave = txedMessage.formatForStorage(false);
                sentFile.write(stringToSave);
                sentFile.close();
            } catch (IOException e) {
                //RadioMSG.middleToastText("Error Saving Message in Sent Folder " + e.toString());
            }
            /* Not yet on Laptops
            //Save the binary part of the message if any
            if (myMessage.picture != null) {
                try {
                    //In any case, save the image file for further usage if required
                    //fileName = RMsgUtil.dateTimeStamp() + ".png";
                    String sentPicFileNameString = sentFileNameString.replace(".txt", ".png");
                    String ImageFilePath = RMsgProcessor.HomePath + RMsgProcessor.Dirprefix + RMsgProcessor.DirImages
                            + RMsgProcessor.Separator;
                    File dest = new File(ImageFilePath + sentPicFileNameString);
                    FileOutputStream out = new FileOutputStream(dest);
                    myMessage.picture.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    //Make it available in the System Picture Gallery
                    ContentValues values = new ContentValues();
                    values.put(Images.Media.TITLE, sentPicFileNameString);
                    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(Images.ImageColumns.BUCKET_ID, dest.toString().toLowerCase(Locale.US).hashCode());
                    values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, dest.getName().toLowerCase(Locale.US));
                    values.put("_data", dest.getAbsolutePath());
                    values.put(Images.Media.DESCRIPTION, "RadioMsg Image");
                    ContentResolver cr = RadioMSG.myContext.getContentResolver();
                    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } catch (Exception e) {
                    loggingclass.writelog("Exception Error in 'txData' " + e.getMessage(), null, true);
                }
            }
            */
            //Set the earliest time to transmit again, after we have heard all the acks (maxAcks)
            //Not yet 
            //nextTimeToTx = waitUntilAcksHeard();
            //Are we displaying the list of Sent Messages?
            //int whichFolders = config.getPreferenceI("WHICHFOLDERS", 3);
            int whichFolders = 3;
            if (whichFolders == 2 || whichFolders == 3) {
                //Add to Display list
                //final RMsgObject txFinalMessage = RMsgObject.extractMsgObjectFromFile(Main.DirSent, sentFileNameString, false); //Text part only
                Main.mainui.msgDisplayList.addNewItem(txedMessage, true); //Is my own message
                //VK2ETA test debug changing filename of received SMS
                //RadioMSG.msgDisplayList.notifyDataSetChanged();
                //final RMsgDisplayItem sentItem = new RMsgDisplayItem(txFinalMessage, 0, 0, false, true); //My own 
                //Add to displayed table of messages on GUI thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        RMsgObject txedMessage = RMsgTxList.removeOldestSent(); //Must be the one just sent
                        txedMessage.crcValid = true;
                        final RMsgDisplayItem sentItem = new RMsgDisplayItem(txedMessage, 0, 0, false, true); //My own 
                        Main.mainui.mRadioMSgTblModel.addRow(new Object[]{sentItem});
                        //Scroll to bottom
                        Main.mainui.scrollRadioMsgsTableToLast();
                    }
                });
            }
        }
    }
  
    private ModemModesEnum checkMode(char c) {
        ModemModesEnum mymode = ModemModesEnum.CTSTIA;

        //  "Mode:(.*)>"     
        c_escape = false;
        accu += Character.toString(c);
        while (accu.length() > 32) {
            accu = accu.substring(1);
        }
        //Main.shown(accu);
        if (accu.contains(">")) {
            //Re-use pre-defined pattern
            MODEM = Pattern.compile(".*\\<Mode:(.+).*");
            Matcher modem = MODEM.matcher(accu);
            if (modem.lookingAt()) {
                String myaccu = modem.group(1);
                Main.shown(myaccu);
                accu = "";
                //VK2ETA: why?
                if (myaccu.equals("THOR 2")) {
                    myaccu = "THOR 22";
                } else if (myaccu.equals("Cntestia") | myaccu.equals("Cntestia")) {
                    myaccu = "Contestia>";
                }

                int m = getmodeindex(myaccu);

                mymode = getModeFromIndex(m);
                Main.rxModem = pmodes[m];
                Main.rxModemString = smodes[m];
            }
        }
        return mymode;
    }

    public void run() {
        GetBlock();
    }
  
    
    //Acknowledgement code for 1 to 8 stations. 
    //Only used here for calculating the waiting time before ack
    private static final int DIT = 100; //in milli-seconds
    private static final int DAH = 400;
    private static final int SPACE = -100; //Negative means silence of that duration
    private static final int[][] ackArray = {
            {SPACE, SPACE, SPACE}, //Position zero is not used
            {DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DIT, SPACE, DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DIT, SPACE, DIT, SPACE, DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DAH, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DAH, SPACE, DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DAH, SPACE, DIT, SPACE, DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DAH, SPACE, DAH, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
            {DAH, SPACE, DAH, SPACE, DIT, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}
    };

    //For non-Morse code readers, make it simpler with a maximum of 3 symbols per acknowledgement position
    //Also minimizes the total duration of all acknowledgments (already is 10.3 seconds for 8 stations)
    private static final String[]ackArrayMorse = {" ", "E", "I", "S", "T", "N", "D", "M", "G" }; 
    
    
    //Generates a sequence of Morse-like DITs and DAHs.
    public void generateDitDahSequence(int ackPosition, boolean positiveAck) {
        String toSend;

        if (!positiveAck) {
            toSend = "5";
        } else if (ackPosition < ackArrayMorse.length) {
            toSend = ackArrayMorse[ackPosition];
        } else {
            return;
        }
        //Now send the ack
        Main.m.Sendln(toSend, "CW", "");
    }
    
    //Calculates the total accumulated time to wait tio get to my ack position
    public long delayUntilMyAckPosition(int ackPosition, boolean inclusiveOfMyAck) {
        long beepsDelay = 0L;
        int symbol;

        if (inclusiveOfMyAck) {
            for (int i = 0; i <= ackPosition; i++) {
                for (int seq = 0; seq < ackArray[i].length; seq++) {
                    symbol = ackArray[i][seq];
                    beepsDelay += abs(symbol);
                }
            }
        } else {
            for (int i = 0; i < ackPosition; i++) {
                for (int seq = 0; seq < ackArray[i].length; seq++) {
                    symbol = ackArray[i][seq];
                    beepsDelay += abs(symbol);
                }
            }
        }
   
        return beepsDelay;
    }
    
    //Set the delay before the next TX the duration of Preference MAXACKS to allow for all the acks to take place
    public long delayUntilMaxAcksHeard() {

        //Integer.parseInt(config.getPreferenceS("MAXACKS", "0"));
        String maxAckStr = Main.configuration.getPreference("MAXACKS", "0");
        int maxAcks = 0;
        try {
            maxAcks = Integer.parseInt(maxAckStr);
        } catch (NumberFormatException e) {
            //Nothing
        }
        //Check we are within the bounds of the array
        if (maxAcks >= ackArray.length) {
            maxAcks = ackArray.length - 1;
        }
        //Now calculate total accumulated time to maxAcks (inclusive of)
        long delayToTx = 0L;
        int symbol;
        for (int i = 0; i <= maxAcks; i++) {
            for (int seq = 0; seq < ackArray[i].length; seq++) {
                symbol = ackArray[i][seq];
                delayToTx += abs(symbol);
            }
        }
        return delayToTx;
    }

    
    
}  // end Modem

