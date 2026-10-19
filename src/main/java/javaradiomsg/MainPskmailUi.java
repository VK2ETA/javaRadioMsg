/*
 * mainpskmailui.java
 *
 * Created on den 25 november 2008, 21:55
 *
 * Copyright (C) 2008 Pär Crusefalk (SM0RWO)
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
// 13.08.14
package javaradiomsg;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.*;


/**
 *
 * @author per
 */
public class MainPskmailUi extends javax.swing.JFrame {

    static int timercnt = 0;

    //public arq myarq;
    /**
     *
     */
//    public config myconfig;
//    public Rigctl myrig;
    //VK2ETA: use Main.sm as single object for all "mysession" access
    //private Session mysession;
    private OptionsDialog optionsDialog;
    private FrequencyHelper fqhelper;
    private static int oldminute = 0;
    private static int minutebytes = 0;
    static final ResourceBundle mainpskmailui = java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui");
    private static String oldstatus = mainpskmailui.getString("Connected");
    private String Icon;
    static String clock;
    static String fr = "";
    static int Connect_time = 0;
    static int Bulletin_time = 0;
    public javax.swing.Timer timer200ms;
    public javax.swing.Timer u;
    private int Systemsecond = 0;
    private boolean sendbeacon = false;
    private JFileChooser fc;
    private int BeaconQrg = 0;
    static StyledDocument monitordoc = new DefaultStyledDocument();
    static StyledDocument terminaldoc = new DefaultStyledDocument();
    static int oldbs = 0;
    static int mincps = 0;
    String myiconsdir = Main.installPath + "images/";

    private Icon icon0 = new ImageIcon(myiconsdir + "antennahome.png");
    private Icon icon1 = new ImageIcon(myiconsdir + "home.png");
    private Icon icon2 = new ImageIcon(myiconsdir + "sailer.png");
    private Icon icon3 = new ImageIcon(myiconsdir + "powerboat.png");
    private Icon icon4 = new ImageIcon(myiconsdir + "car.png");
    private Icon icon5 = new ImageIcon(myiconsdir + "bus.png");
    private Icon icon6 = new ImageIcon(myiconsdir + "camping.png");
    private Icon icon7 = new ImageIcon(myiconsdir + "womo.png");
    private Icon icon8 = new ImageIcon(myiconsdir + "none.png");
    private Icon Icons[] = {icon8, icon0, icon1, icon2, icon3, icon4, icon5, icon6, icon7};

    private SimpleAttributeSet bold = new SimpleAttributeSet();
    private SimpleAttributeSet normal = new SimpleAttributeSet();
    private SimpleAttributeSet blue = new SimpleAttributeSet();
    private SimpleAttributeSet grey = new SimpleAttributeSet();
    private SimpleAttributeSet yellow = new SimpleAttributeSet();
    private SimpleAttributeSet green = new SimpleAttributeSet();

    private int Qhi = 100;
    private int Qlow = 0;
    private int Qavg = 40;

    private String Latitudestr = "";
    private String Longitudestr = "";

    //Email, inbox handling
    MessageViewTableModel inboxmodel;
    MessageOutViewTableModel outboxmodel;
    MessageHeaderViewTableModel headermodel;

    //private CheckBox checkbox = null;
    static final String[] opModes = new String[]{"HF-Clubs", "HF-Poor", "HF-Good", "HF-Fast", "UHF-Poor", "UHF-Good", "UHF-Fast"};
    static final String[] defaultModes = new String[]{"CCIR476", "OLIVIA_8_500", "OLIVIA_8_1000", "OLIVIA_8_2000", "MFSK32", "MFSK64", "MFSK128"};
    //Keep a reference to the spinner so that we can change its colour
    //static private Spinner jComboRMsgTo = null;
    static public boolean sendAliasAndDetails = false;
    //To field spinner selection
    public static String selectedTo = "*";
    public static String selectedToAlias = ""; //Aliases for sms or email destinations
    public static String selectedVia = "";
    public static String selectedViaPassword = ""; //For relays with password required
    public static String selectedViaIotPassword = ""; //For relays with password required for IOT commands
    public static String[] toArray; //Stores array of To stations/email addresses/Cellular numbers
    public static String[] toAliasArray; //Stores array of To stations' aliases if any in the same order as toArray
    public static String[] viaArray; //Stores array of relay (via) stations
    public static String[] viaPasswordArray; //Stores array of Via stations' access passwords, if any, in the same order as viaArray
    public static String[] viaIotPasswordArray; //Stores array of Via stations' IOT access passwords, if any, in the same order as viaArray

    //Resend request radio buttons values
    private static String howManyToResend = "1";
    private static String whatToResend = "";

    // Enums for email grid state
    private enum grid {
        IN, OUT, HEADERS, SENT
    };
    // Default is inbox, first thing to show
    grid emailgrid = grid.IN;

    int g = 0;
    private Object myconfig;

    //Date object that holds the time of the latest gps fix
    Date gpsfixlatest;

    //Radio messages display list
    DefaultTableModel mRadioMSgTblModel;
    public RMsgDisplayList msgDisplayList;
    public boolean updatingMsgListAdapter = false;

    
    
    void buildDisplayList() {
       //Collect all stored messages for display
        //Prevent updates by GPS while (re-)building the list
        updatingMsgListAdapter = true;
        // Initialize the message array adapter and load Received Messages
        //int whichFolders = config.getPreferenceI("WHICHFOLDERS", 3);
        msgDisplayList = new RMsgDisplayList(RMsgObject.loadFileListFromFolders(RMsgObject.INOUTBOXCOMBINED,
                RMsgObject.SORTBYNAME));
        updatingMsgListAdapter = false;
    }

    /**
     * Creates new form mainpskmailui
     */
    public MainPskmailUi() {

        //System.out.println("Entering mainpskmailui");

        initComponents();
        //Preset default visibilities
        resetAllMenus();
        //Preset requested visibilities
        String uiOption = Main.configuration.getPreference("UIOPTION", "Default");
        if (uiOption.equals("RadioMsg")) {
            setRadioMsgUi();
        }
        //ButtonGroup RB = new ButtonGroup();
        //myarq = new arq();
        //String path = Main.HomePath + Main.Dirprefix;
        //myconfig = new config(path);

        //mysession = new Session();
        Main.sm.mycall = Main.mycall;
        fc = new JFileChooser();

//        if (Main.configuration.getPreference("RIGCTL").equals("yes")) {
        RigCtrl.Rigctl();
//        } 
        RigCtrl.initfreqs();

        //VK2ETA: Too early. If not scanning will change the frequency to the default and if scanning we don't know which minute we are in
        //if (Rigctl.opened) {
        //    Rigctl.Setfreq(ServerfreqTxtfield.getText());
        //} else {
        //    ClientFreqTxtfield.setText(ServerfreqTxtfield.getText());
        //}
        // Fill the popup menu for email handling
        this.mnuEmailOpenGet.setText(mainpskmailui.getString("mnuEmailPopupGet"));
        // Set the text of the save to file popup menu
        this.mnuTextSave.setText(mainpskmailui.getString("mnuTermSaveAsText"));

        // Set up the outbox menu popup
        this.mnuOutboxOpenMsg.setText("Open Message");
        this.mnuOutboxDeleteMsg.setText("Delete message");

        // Set up the headers menu popup
        this.mnuHeadersFetch.setText("Fetch message");

        // attributes for output windows
        Color darkgreen = new Color(26, 127, 127);
        green.addAttribute(StyleConstants.CharacterConstants.Foreground,
                darkgreen);
        bold.addAttribute(StyleConstants.CharacterConstants.Foreground,
                Color.red);
        normal.addAttribute(StyleConstants.CharacterConstants.Foreground,
                Color.black);
        blue.addAttribute(StyleConstants.CharacterConstants.Foreground,
                Color.blue);
        grey.addAttribute(StyleConstants.CharacterConstants.Foreground,
                Color.gray);
        yellow.addAttribute(StyleConstants.CharacterConstants.Foreground,
                Color.yellow);

        // set some configuration variables
        this.txtStatus.setText(Main.configuration.getStatus());
        Latitudestr = Main.configuration.getPreference("LATITUDE");
        setLatitudeText(Latitudestr);
        Longitudestr = Main.configuration.getPreference("LONGITUDE");
        setLongitudeText(Longitudestr);

        this.txtStatus.setText(Main.configuration.getPreference("STATUS"));
        if (Main.configuration.getBeacon().equals("1")) {
            chkBeacon.setSelected(true);
        } else {
            chkBeacon.setSelected(false);
        }

        if (Main.configuration.getBeaconcomp().equals("1")) {
            this.cbComp.setSelected(true);
        } else {
            this.cbComp.setSelected(false);
        }

        // Update the timer display
        String myBtimer = Main.configuration.getPreference("BEACONTIME", "30");
        this.cboBeaconPeriod.setSelectedItem(myBtimer);

        if (Main.configuration.getAutolink().equals("1")) {
            chkAutoLink.setSelected(true);
        } else {
            chkAutoLink.setSelected(false);
        }

        this.spnMinute.setValue(Integer.parseInt(Main.configuration.getBeaconqrg()));
        // ICON
        String myIcon = Main.configuration.getPreference("ICON", "Y");
        String myLevel = Main.configuration.getPreference("ICONlevel", "/");
        this.cboAPRSIcon.setSelectedItem(myIcon);
        this.cboAPRS2nd.setSelectedItem(myLevel);
        // Also update the displayed icon. Beware of bad values
        int myindex = cboAPRSIcon.getSelectedIndex();
//        System.out.println(myindex);
//        System.out.println(myLevel);
        if (myindex > -1 & myindex < 9 & myLevel.equals("/")) {
            lblAPRSIcon.setIcon(Icons[myindex]);
        } else {
            lblAPRSIcon.setIcon(Icons[0]);
        }

//        System.out.println(Main.InstallPath + "images/");
        Main.icon = myIcon;
        lblAPRSIcon.setText("");

        // Fetch server to link to
        this.cboServer.removeAllItems();
        // Add servers from main
        for (int i = 0; i < Main.serversArray.length; i++) {
            if (!Main.serversArray[i].equals("")) {
                cboServer.addItem(Main.serversArray[i]);
            }
        }
        //String myServer = Main.configuration.getPreference("SERVER", "n0cal");
        //this.cboServer.setSelectedItem(myServer);
        this.cboServer.setSelectedItem(Main.serversArray[0]);
        RigCtrl.Loadfreqs(Main.serversArray[0]);
        
        if (Main.configuration.getPreference("SCANNER").equals("yes")) {
            ScannerCheckbox.setSelected(true);
            Main.wantScanner = true;
        } else {
            ScannerCheckbox.setSelected(false);
            Main.wantScanner = false;
        }

        //String bl = Main.configuration.getBlocklength();
        //Character c = bl.charAt(0);
        //int charval = c.charValue() - 48;

        
        //IgateCallField.setText(Main.configuration.getPreference("APRSCALL"));
        IgateCallField.setText(Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER")));
        String nr = Main.configuration.getPreference("APRSINTERNETSERVER");
        if (nr.equals("")) {
            APRSServerSelect.setSelectedIndex(10);
        } else {
            APRSServerSelect.setSelectedIndex(Integer.parseInt(nr));
        }
                /*
        //Initial connection to APRS
        if (Main.configuration.getPreference("IGATE", "no").equals("yes")) {
            if (Igate.aprsavailable) {
                IgateSwitch.setSelected(true);
                IgateSwitch.setText("ON");
                //Main.wantIgate = true;
                //iGate cIgateCallFieldallsign must be uppercase and free of prefixes and Non-Aprs IDs
                IgateCallField.setText(Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER")));
            } else {
                IgateSwitch.setSelected(false);
                IgateSwitch.setText("OFF");
                //Main.wantIgate = false;
            }
        }
        */
        Main.wantIgate = Main.configuration.getPreference("IGATE", "no").equals("yes");
        if (Main.wantIgate) {
            //Get the callsign and clean it for APRS
            String IgateCall = Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER"));
            if (IgateCall.length() > 0) {
                IgateSwitch.setSelected(true);
                IgateSwitch.setText("ON");
                IgateCallField.setText(IgateCall);
                try {
                    Igate.start();
                } catch (IOException ex) {
                    //            System.out.println("FAILED");
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //My call sign is not valid for APRS, do NOT connect ever
                IgateSwitch.setSelected(false);
                IgateSwitch.setText("OFF");
                Main.wantIgate = false;
                Main.q.Message("Call sign not valid, no APRS", 10);
            }
        }
        
        //Load Radio Messages from the list into the Gui tab
        //Build the list of inbox and/or outbox messages to display
        buildDisplayList();
        loadRadioMsg();
        loadRMsgComboBoxes();
        //Set Alias only by default
        jRadBtnAliasOnly.setSelected(true);
        
        //Pre-select and load Inbox in email tab
        emailgrid = grid.IN;
        LoadInbox();

// timer, 1 sec tick
        u = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(ActionEvent e1) {
                // get frequency
                if (!Main.connected) {
                    if (RigCtrl.opened) {
                        rigctlactivelbl.setText("active");
                        rigctlactivelbl.setForeground(new Color(0x0088dd));
                        if ((fr = RigCtrl.Getfreq()) != null) {
                            //                             System.out.println("freq: " + fr);
                            if (!fr.equals("0")) {
                                ClientFreqTxtfield.setText(fr);
                            }
                        }
                    } else {
                        rigctlactivelbl.setText("manual");
                        rigctlactivelbl.setForeground(new Color(0x0000dd));
                    }

                    //labelServerFreq.setText(cboServer.getSelectedItem().toString());
                    labelServerFreq.setText(Main.q.getServer());

                    freq0.setText(RigCtrl.freqs[0]);
                    freq1.setText(RigCtrl.freqs[1]);
                    freq2.setText(RigCtrl.freqs[2]);
                    freq3.setText(RigCtrl.freqs[3]);
                    freq4.setText(RigCtrl.freqs[4]);
                }

                // get SNR
                try {
                    String snrstr = RigCtrl.GetSNR();
                    SNRlbl.setText(snrstr);
                    snLabel.setText(snrstr);
                    if (snrstr.length() > 8) {
                        String db = snrstr.substring(4, 7);
                        try {
                            Main.snr_db = Integer.parseInt(db.trim());
                        } catch (Exception e) {
//                            System.out.println(Main.snr_db);
                        }

                    }
                } catch (NullPointerException np) {
                    ;
                }

            }
        });

        //Vk2eta debug: Done after setting the main window as visible
        //u.start();

// timer, 200 msec tick
        //vk2eta debug
        //timer50ms = new javax.swing.Timer(50, new ActionListener() {
        timer200ms = new javax.swing.Timer(200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //200 ms second timer
                // update monitor window
                if (Main.monitorText.length() > 0) {
                    Main.monmutex = true;
                    appendTextArea3(Main.monitorText);
                    Main.monitorText = "";
                    Main.monmutex = false;
                }

                // DCD indicator
                if (Main.DCD > 0) {
                    setDCDColor(Color.YELLOW);
                } else {
                    setDCDColor(Color.lightGray);
                }
                if (Main.TxActive) {
                    setDCDColor(Color.RED);
                }
                if (Main.m.BlockActive) {
                    setDCDColor(Color.cyan);
                }
                // set link status indicator
                if (Main.connected & Main.rxbusy & !Main.txbusy) {
                    setlinkstatusindicator(Color.YELLOW);
                } else if (Main.connected & !Main.rxbusy & !Main.txbusy) {
                    setlinkstatusindicator(Color.GREEN);
                } else if (Main.connected & Main.rxbusy & Main.txbusy) {
                    setlinkstatusindicator(Color.RED);
                } else if (Main.connected & !Main.rxbusy & Main.txbusy) {
                    setlinkstatusindicator(Color.cyan);
                } else {
                    if (Main.linked) {
                        setlinkstatusindicator(Color.lightGray);
                        lblAutoServer.setText(Main.linkedServer);
                    } else {
                        setlinkstatusindicator(Color.pink);
                        lblAutoServer.setText("");
                    }
                }

                // update main window
                if (Main.mainwindow.length() > 0) {
                    Main.mainmutex = true;
                    appendMainWindow(Main.mainwindow);
                    Main.mainwindow = "";
                    Main.mainmutex = false;
                }

                // 5 x  200 = 1 second timer
                if (timercnt < 5) {
                    timercnt++;
                } else { //Loop run every SECOND
                    //Check last character receipt from Fldigi
                    //More than timeout, set test mode = Squelch fully open
                    //If more than 30 seconds in test mode, then kill fldigi and let the 
                    //  modem getbyte() restart it and re-init Rigctl
                    if (!Main.TxActive 
                            && (System.currentTimeMillis() - Main.lastModemCharTime > 55000) // = 55 seconds
                            && Main.m.commToFldigiOpened) { //We had an opened connection to the Modem
                        if (!Main.modemTestMode) {
                            //Enter test mode for next iteration
                            Main.modemTestMode = true;
                            //Note: Squelch will be set to minimum below
                            System.out.println("Timeout on receipt of data from modem. Entering test mode");
                        } else {
                            if (System.currentTimeMillis() - Main.lastModemCharTime > 115000) {// = 1 minutes + 55 seconds
                                //We have a hang modem, request an Flgdigi restart
                                //NO, just kill it
                                System.out.println("Modem test period exhausted, Killing modem process");
                                Main.m.killFldigi(); //We are going to restart the modem. not final task kill
                                //Main.requestModemRestart = true;
                                //System.out.println("Modem test period exausted, requesting modem restart");
                                //Reset test flag
                                Main.modemTestMode = false;
                                //Reset time counter
                                Main.lastModemCharTime = System.currentTimeMillis();
                            }
                        }
                    } else {
                        if (Main.modemTestMode) {
                            System.out.println("Exiting Modem test mode, all OK");    
                        }
                        //Reset test flag
                        Main.modemTestMode = false;
                    }
                    timercnt = 0;
                    Calendar cal = Calendar.getInstance();
                    //Adjust for a received time reference (e.g. from this remote station)
                    cal.add(Calendar.SECOND, Main.deviceToRefTimeCorrection.intValue());
                    int Hour = cal.get(Calendar.HOUR_OF_DAY);
                    int Minute = cal.get(Calendar.MINUTE);
                    int Second = cal.get(Calendar.SECOND);
                    Systemsecond = Second;

                    // decrement DCD every second.
                    if (Main.DCD > 0) {
                        Main.DCD--;
                    }
                    // decrement RxDelayCount every second IF we are in a server session
                    //VK2ETA debug : always decrement, otherwise we can't connect/respond to first/last exchange
                    //if (Main.RxDelayCount > 0 && !Main.TTYConnected.equals("")) {
                    if (Main.rxDelayCount > 0) {
                        Main.rxDelayCount--;
                    }
                    // update message window
                    if (Main.msgWindow.length() > 0) {
                        String hourformat = "0" + Integer.toString(Hour);
                        hourformat = hourformat.substring(hourformat.length() - 2);
                        String minuteformat = "0" + Integer.toString(Minute);
                        minuteformat = minuteformat.substring(minuteformat.length() - 2);
                        String newmessage = hourformat + ":" + minuteformat + " " + Main.msgWindow;
                        appendMSGWindow(newmessage);
                        Main.msgWindow = "";
                    }

                    if (Systemsecond >= Main.second & sendbeacon) {
                        sendbeacon = false;
                        try {
                            Main.q.send_beacon();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    // mail headers
                    if (Main.mailHeadersWindow.length() > 0) {
                        appendHeadersWindow(Main.mailHeadersWindow);
                        Main.mailHeadersWindow = "";
                    }
                    // files window
                    if (Main.filesTextArea.length() > 0) {
                        FilesTxtArea.append(Main.filesTextArea);
                        Main.filesTextArea = "";
                    }

                    //Radio Messages information
                    if (Main.receivingRadioMsg) {
                        Main.statusLine = "Receiving radio Message";
                        Main.statusLineTimer = 2;
                    } else if (Main.possibleRadioMsg > 0L && Main.ttyConnected.equals("") && !Main.connected) {
                        Main.statusLine = "Receiving Frame?";
                        Main.statusLineTimer = 2;
                    } else if (!Main.connected && !Main.Connecting
                            && (System.currentTimeMillis() - Main.lastCharacterTime > 10000)) {
                        Main.receivingRadioMsg = false;
                    }
                    //Reset time since last RSID if we don't see a radio message header within 30 seconds
                    if (!Main.receivingRadioMsg && Main.possibleRadioMsg > 0 && System.currentTimeMillis() - Main.possibleRadioMsg >= 30000) {
                        Main.possibleRadioMsg = 0L;
                        Main.lastRsidTime = 0L; //Also reset the last RSID time
                    }

                    // Status Line messages
                    if (Main.statusLineTimer > 0 & Main.statusLine.length() > 0) {
                        StatusLabel.setText(Main.statusLine);
                    }
                    if (Main.statusLineTimer > 0) {
                        Main.statusLineTimer--;
                        if (Main.statusLineTimer == 0) {
                            Main.statusLine = "";
                            StatusLabel.setText(" ");
                        }
                    }
                    // get gpsd data
                    if (Main.haveGPSD) {
                        Main.getgpsddata();
                    }
                    // Get the GPS position or the preset data
                    if (Main.gpsData.getFixStatus()) {
                        if (!Main.haveGPSD) {
                            setLatitudeText(Main.gpsData.getLatitude());
                            setLongitudeText(Main.gpsData.getLongitude());
                        } else {
                            setLatitudeText(Main.gpsdLatitude);
                            setLongitudeText(Main.gpsdLongitude);
                        }
                        setSpeedText(Main.gpsData.getSpeed());
                        setCourseText(Main.gpsData.getCourse());
                        setFixAt(Main.gpsData.getFixTime());
                        checkgpsfixloss(true);
                    } else {
                        checkgpsfixloss(false);
                    }

                    // set mode indicators
                    RxmodeTextfield.setText(Main.rxModemString);
                    TxmodeTextfield.setText(Main.m.getTXModemString(Main.txModem));
                    int snrval = (int) Main.snr;
                    RxmodeQuality.setValue(snrval);

                    if (Main.connected) {
                        PositButton.setEnabled(false);
                    } else {
                        PositButton.setEnabled(true);
                    }

                    // get Quality
                    try {
                        String qual = RigCtrl.GetQual();
                        if (!qual.equals("")) {
                            float f = Float.valueOf(qual.trim()).floatValue();
                            Main.quality = (int) f;
                            if (Main.quality > Qavg) {
                                Qhi = ((Qhi * 4) + Main.quality) / 5;
                                //VK2ETA if (!Main.Connected & !Main.Connecting) {
                                if (!Main.connected & !Main.Connecting & !Main.aborting & Main.ttyConnected.equals("")) {
                                    Main.DCD = Main.MAXDCD;
                                }
                            } else if (Main.quality < Qavg) {
                                Qlow = ((Qlow * 4) + Main.quality) / 5;
                                //VK2ETA - squelch never goes below 5
                                //if (Qhi > 15) {
                                if (Qhi > 3) {
                                    Qhi -= 1;
                                }
                            }

                            Main.sql = Qlow + ((Qhi - Qlow) / 3);

                            Main.progress = Main.quality;
                            //System.out.println(Main.sql);
                            //Squelch fully open when actively listening
                            if (Main.connected || Main.m.BlockActive
                                    || Main.receivingRadioMsg || Main.possibleRadioMsg != 0L 
                                    || Main.modemTestMode 
                                    || Main.configuration.getPreference("LISTENINCWMODE").equals("yes")) {
                                Main.sql = Main.SQL_FLOOR;
                                RigCtrl.setSquelchOn(false); //OFF
                            } else {
                                RigCtrl.setSquelchOn(true); //ON
                                RigCtrl.SetSqlLevel(Main.sql);
                            }
                        }

                    } catch (NullPointerException np) {
                        ;
                    }

                    // set progress bar
                    ProgressBar.setValue(Main.progress);
                    if (Main.connected) {
                        if (Main.progress > 0) {
                            ProgressBar.setStringPainted(true);
                        } else {
                            ProgressBar.setStringPainted(false);
                        }
                    }

                    if (Main.aprsBeaconText.length() > 0) {
                        String Tm = gettime();
                        IgateTextArea.append(Tm + Main.aprsBeaconText);
                        Main.aprsBeaconText = "";
                    }

                    // max 30 seconds Bulletin without reception
                    if (Main.bulletinTime > 0) {
                        Main.bulletinTime--;
                        if (Main.bulletinTime < 1) {
                            Main.bulletinMode = false;
                        }
                    }

                    String txqual = Main.myRxStatus;
                    int strlen = txqual.length();
                    strlen -= 3;
                    int txq = (8 - strlen) * 10;
                    if (Main.connected) {
                        TxmodeQuality.setValue(txq);
                    } else {
                        TxmodeQuality.setValue(0);
                    }

                    //display mode
                    if (Main.currentModemProfile.equals("0")) {
                        Profile.setText("Follow server");
                    } else {
                        Profile.setText("Asymmetric");
                    }

                    // display block size
                    if (Main.connected & (Main.RxBlockSize == 16
                            | Main.RxBlockSize == 32
                            | Main.RxBlockSize == 64)) {
                        Size_indicator.setText(Integer.toString(Main.RxBlockSize));
                    } else {
                        Size_indicator.setText("");
                    }

                    // display total bytes
                    Totalbytes.setText(Integer.toString(Main.totalBytes));

                    // ... clock...
                    String formathour = "0" + Integer.toString(Hour);
                    formathour = formathour.substring(formathour.length() - 2);
                    String formatminute = "0" + Integer.toString(Minute);
                    formatminute = formatminute.substring(formatminute.length() - 2);
                    String formatsecond = "0" + Integer.toString(Second);
                    formatsecond = formatsecond.substring(formatsecond.length() - 2);

                    clock = formathour + ":" + formatminute + ":" + formatsecond;
                    //Adjust the text colour
                    if (Main.deviceToRefTimeCorrection.intValue() == 0) {
                        jTextField1.setForeground(Color.BLACK);
                    } else {
                        jTextField1.setForeground(Color.RED);
                    }
                    setClock(clock);

                    // display status field
                    if (Main.Connecting) {
                        Main.status = "Connecting";
                    }
                    if (Main.bulletinMode) {
                        lblStatus.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Bulletin"));
                        lblStatus.setForeground(Color.black);
                    } else if (Main.iacMode) {
                        lblStatus.setText("FEC1");
                        lblStatus.setForeground(Color.black);
                    } else if (Main.monitorMode) {
                        lblStatus.setText("Monitor");
                        lblStatus.setForeground(Color.black);
                    } else if (!oldstatus.equals(Main.status)) {
                        oldstatus = Main.status;
                        lblStatus.setText(Main.status);
                        if (Main.status.equals("Connected")) {
                            //VK2ETA logic for changing must be elsewhere
                            //Main.Connecting = false;
                            //VK2ETA logic for changing must be elsewhere
                            //Main.Connecting_time = 0;
                            lblStatus.setText(Main.linkedServer);
                            lblStatus.setForeground(Color.BLUE);
                        } else if (Main.status.equals("Connecting")) {
                            lblStatus.setForeground(Color.RED);
                        } else if (Main.status.equals("Discon")) {
                            lblStatus.setForeground(Color.RED);
                        } else {
                            lblStatus.setForeground(Color.yellow);
                        }
                        if (Main.status.equals(mainpskmailui.getString("Connected")) | Main.ttyConnected.equals("Connected")) {
                            bConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                            Conn_connect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                            FileConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                            lblStatus.setForeground(Color.BLUE);
                            lblStatus.setText(Main.linkedServer);
                        } else {
                            bConnect.setText(mainpskmailui.getString("Connect"));
                            Conn_connect.setText(mainpskmailui.getString("Connect"));
                            FileConnect.setText(mainpskmailui.getString("Connect"));
                        }
                    } else if (Main.status.equals(mainpskmailui.getString("Listening"))) {
                        lblStatus.setText(mainpskmailui.getString("Listening"));
                        lblStatus.setForeground(Color.lightGray);
                    } else if (Main.Connecting) {
                        lblStatus.setText(mainpskmailui.getString("Connecting"));
                        lblStatus.setForeground(Color.RED);
                    } else if (Main.status.equals(mainpskmailui.getString("Discon"))) {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Discon");
                    } else if (Main.ttyConnected.equals("") | Main.connected) {
                        ;
                    } else {
                        lblStatus.setText(mainpskmailui.getString("Listening"));
                        lblStatus.setForeground(Color.lightGray);
                    }

                    if (Main.TxActive) {
                        String txstring = Main.m.getTXModemString(Main.txModem);
                        txstring += "        ";
                        if (txstring.length() > 6) {
                            txstring = txstring.substring(0, 9);
                            RXlabel.setText(txstring);
                        }
                    } else {
                        String rxstring = Main.rxModemString;

                        rxstring += "    ";
                        rxstring = rxstring.substring(0, 9);
                        RXlabel.setText(rxstring);
                    }

                    // TTY connect ?
                    if (Main.ttyConnected.equals("Connected")) {
                        bConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                        Conn_connect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                        FileConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("QUIT"));
                        lblStatus.setForeground(Color.BLUE);
                        lblStatus.setText(Main.ttyCaller);
                    }
                    // did we get an abort?

                    if (Main.isDisconnected) {
                        Main.isDisconnected = false;
                        bConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                        Conn_connect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                        FileConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                    }
                    // scanning connect ?

                    if (!Main.connected & Main.scanning & (Minute % 5) == BeaconQrg) {
                        if (Second == 10 | Second == 30) {

                            try {
                                Main.q.set_txstatus(TxStatus.TXConnect);
                                Main.q.send_frame("");
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    // display cps
                    int cpm = (Main.totalBytes - oldbs);
                    oldbs = Main.totalBytes;
                    minutebytes += cpm;
                    mincps = (4 * mincps + cpm) / 5;

                    if (cpm > 0) {
                        CPSValue.setText(Integer.toString(mincps));
                    }

                    if (!Main.bulletinMode & !Main.connected & !Main.iacMode) {
                        if (Main.wantIgate) {
                            if (Igate.connected) {
                                try {
                                    if (Igate.in.ready()) {
                                        Igate.read();
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }

                    // write frequency to frame title
                    if (!Main.ttyConnected.equals("")) {
                        setTitle(Main.application + " - Server:" + Main.callsignAsServer + " - " + ClientFreqTxtfield.getText());
                    } else if (Main.connected) {
                        setTitle(Main.application + " - Client:" + Main.mycall + " - " + ClientFreqTxtfield.getText());
                    } else {
                        //setTitle(Main.application + " - Client:" + Main.mycall + ", Server:" + Main.callsignAsServer + " - " + ClientFreqTxtfield.getText());
                        setTitle(Main.application + " - Callsign: " + Main.mycall + " - " + ClientFreqTxtfield.getText());
                    }
                    //Loop run every MINUTE
                    if (Minute != oldminute) {
                        //Try to reconnect to APRS if we want to and haven't be able to,
                        //OR if the existing APRS connection has been idle for the last 10 minutes
                        if (Main.wantIgate 
                                && (!Igate.connected
                                || (Igate.connected && System.currentTimeMillis() > Igate.lastAprsServerPacketTime + 600000L))) { //debug 600000L
                            try {
                                Igate.start();
                            } catch (IOException ex) {
                                //            System.out.println("FAILED");
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        //Check if we have RadioMsg activity
                        boolean radioMsgActive = false;
                        if (RMsgTxList.getAvailableLength() > 0 | Main.receivingRadioMsg
                                | Main.radioMsgWorking | (Main.possibleRadioMsg > 0L
                                && System.currentTimeMillis() - Main.possibleRadioMsg < 50000)) { //50 seconds between RSID and end of first line of Radio Message
                            radioMsgActive = true;
                        }
                        boolean serverActive = false;
                        if (Main.ttyConnected.equals("Connecting") | Main.ttyConnected.equals("Connected")) {
                            serverActive = true;
                        }
                        boolean clientActive = false;
                        if (Main.connected | Main.Connecting | Main.bulletinMode | Main.TxActive) {
                            clientActive = true;
                        }
                        
                        //To-do: APRS-IS listening stops after 1 hour. Must renew subscription every 1/2 hour or so
                        
                        //Time to send a server beacon if required
                        //E.g: AFA1EO>PSKAPR,TCPIP*,qAC,T2UK:@092354z4353.57NP06955.94W&PSKmail 1.9.4  server
                        /* from Perl server
                        
                        			if ($Aprs_connect == 1) {
				if (time() - $systime >= 60 * $posit_time) {			## 10 mins aprs beacon...
					$systime = time();
					# now do what we want....

					my ($month, $hour, $min) = (gmtime) [3, 2, 1];
					$month = substr (("0" . $month), -2, 2);
					$hour = substr (("0" . $hour), -2, 2);
					$min = substr (("0" . $min), -2, 2);

					my $mytime = $month . $hour . $min . "z";
					if (-e "$ENV{HOME}/.pskmail/aprs_wx.txt") {

						my $WX = `cat "$ENV{HOME}/.pskmail/aprs_wx.txt"`;

						if (index ($WX, "_") == 0) { $WX = substr($WX, 1);}

						$MSG = "$ServerCall" . ">PSKAPR:" . "@" . $mytime . $Aprs_beacon . $WX ."\n";
					} else {
						$MSG = "$ServerCall" . ">PSKAPR:" . "@" . $mytime . $Aprs_beacon ."\n";
					}

					aprs_send ($MSG);

					# end
				}
			}
                        */
                        
                        //Time to restart the modem? Skip the rest of the process then
                        String everyXHoursStr = Main.configuration.getPreference("EVERYXHOURS", "1");
                        int everyXHours = 1;
                        try {
                            everyXHours = Integer.parseInt(everyXHoursStr);
                        } catch (NumberFormatException numberFormatException) {
                            //Already preset
                        }
                        if (!clientActive && !serverActive && !radioMsgActive
                                && Main.modemAutoRestartDelay > everyXHours * 60) { //* 1  Every x minutes vk2eta debug
                            Main.requestModemRestart = true;
                            System.out.println("Periodic restart of Fldigi requested");
                        } else { 
                            //Process the rest, we have an active modem connection
                            oldminute = Minute;
                            Main.modemAutoRestartDelay++;
                            if (Main.connectingTime > 0) {
                                Main.connectingTime--;
                            }
                            // display throughput value
                            if (minutebytes > 0) {
                                Throughput.setText(Integer.toString(minutebytes));
                            }
                            minutebytes = 0;
                            // reset mode  ??, 
                            //But not if we are in modem test mode to check if Fldigi is still alive. 
                            // Changing the mode will results in a couple of erratic characters being 
                            // sent, providing a false positive for the modem activity
                            if ((mnuMailScanning.isSelected() & !Main.connected)
                                    | (!Main.connected & !Main.Connecting & !Main.monitorMode
                                    & !Main.bulletinMode & !radioMsgActive & !serverActive
                                    & !Main.TxActive & !Main.modemTestMode)) {
                                if (Main.configuration.getPreference("LISTENINCWMODE").equals("yes")) {
                                    Main.m.setModemModeNow(ModemModesEnum.CW);
                                } else {
                                    Main.m.setModemModeNow(Main.defaultmode);
                                }
                                Main.txModem = Main.defaultmode;
                                Main.rxModem = Main.defaultmode;
                                String rxstring = Main.m.getTXModemString(Main.defaultmode);
                                rxstring += "        ";
                                rxstring = rxstring.substring(0, 7);
                                Main.rxModemString = rxstring;
                                RXlabel.setText(rxstring);
                            }
// check if the squelch is on, and switch on as necessary....
                            try {
                                boolean SQL = RigCtrl.GetSquelch();

                                if (!SQL) {
                                    RigCtrl.ToggleSquelch();
                                }
                            } catch (NullPointerException np) {
                                System.out.println("SQL is broken");;
                            }

                            int i = 0;
                            // scanning
                            int systemMinute = Minute % 5;

                            // show channel in Rigctl tab
                            if (!Main.monitorMode & !Main.bulletinMode) {
                                freq0.setForeground(Color.GRAY);
                                freq1.setForeground(Color.GRAY);
                                freq2.setForeground(Color.GRAY);
                                freq3.setForeground(Color.GRAY);
                                freq4.setForeground(Color.GRAY);

                                if (systemMinute == 0) {
                                    freq0.setForeground(Color.BLUE);
                                }
                                if (systemMinute == 1) {
                                    freq1.setForeground(Color.BLUE);
                                }
                                if (systemMinute == 2) {
                                    freq2.setForeground(Color.BLUE);
                                }
                                if (systemMinute == 3) {
                                    freq3.setForeground(Color.BLUE);
                                }
                                if (systemMinute == 4) {
                                    freq4.setForeground(Color.BLUE);
                                }
                            }

                            // max 5 minutes Connecting
                            if (Main.Connecting & Connect_time > 0) {
                                Connect_time--;
                                if (Connect_time == 0) {
                                    Main.Connecting = false;
                                    Main.connectingPhase = false;
                                    Main.status = "Listening";
                                }
                            }

                            // Ready to restart scanning (after a scan off command)
                            if ((mnuMailScanning.isSelected() & !Main.connected) | 
                                    (!Main.connected & !Main.Connecting & !Main.monitorMode & !sendbeacon & !Main.bulletinMode)) {
                                if (Main.configuration.getPreference("SCANNER").equals("yes")) {
                                    if (!Main.wantScanner && (System.currentTimeMillis() > Main.restartScanAtEpoch)) {
                                        Main.wantScanner = true;
                                        ScannerCheckbox.setSelected(true);
                                    }
                                }
                                Main.summoning = false;
                            }
                            
                            // set igate indicator
                            if (Igate.connected) {
                                IgateIndicator.setText("Connected to " + Main.aprsServer);
                            } else {
                                IgateIndicator.setText("Disconnected");
                            }

                            // scanning rx
                            if (!Main.monitorMode & !Main.bulletinMode) {
                                ServerfreqTxtfield.setText(RigCtrl.freqs[systemMinute]);
                                int sf = 0;
                                if (!RigCtrl.freqs[systemMinute].equals("")) {
                                    sf = Integer.parseInt(RigCtrl.freqs[systemMinute]) + RigCtrl.OFF;
                                }
                                Main.serverFreq = Integer.toString(sf);

                                if (Main.connected) {
                                    modelbl.setText("Con'ted to Server");
                                } else if (Main.Connecting) {
                                    modelbl.setText("Con'ting to Server");
                                } else if (Main.summoning) {
                                    modelbl.setText("Summoning");
                                } else if (Main.receivingRadioMsg) {
                                    modelbl.setText("Radio Msg");
                                } else if (Main.possibleRadioMsg > 0L) {
                                    modelbl.setText("Rec. Data?");
                                } else if (Main.ttyConnected.equals("Connected")) {
                                    modelbl.setText("Client Con'ted");
                                } else if (Main.ttyConnected.equals("Connecting")) {
                                    modelbl.setText("Client Con'ting");
                                } else if (ScannerCheckbox.isSelected() && !Main.wantScanner) {
                                    modelbl.setText("Scanning Paused");
                                } else if (Main.wantScanner) {
                                    modelbl.setText("Scanning");
                                } else {
                                    modelbl.setText("Idle");
                                }

                                try {
                                    String Beaconqrg = Main.configuration.getPreference("BEACONQRG");
                                    BeaconQrg = Integer.parseInt(Beaconqrg);
                                    i = BeaconQrg;
                                } catch (Exception ex) {
                                    i = 0;
                                }
                                if (RigCtrl.opened & ((mnuMailScanning.isSelected() & Main.Connecting)
                                        | (!Main.connected & !Main.Connecting & !radioMsgActive & !serverActive
                                        & !Main.ttyConnected.equals("Connected") & !Main.ttyConnected.equals("Connecting"))) & Main.wantScanner) {
                                    if (!RigCtrl.freqs[systemMinute].equals("0")) {
                                        RigCtrl.Setfreq(RigCtrl.freqs[systemMinute]);
                                        setTitle(Main.application + " - " + Main.mycall + "(c)/" + Main.callsignAsServer + "(s) - " + ClientFreqTxtfield.getText());
                                    }
                                }
                            }

                            if (!Main.bulletinMode & !Main.connected & !Main.iacMode) {
                                String Period = cboBeaconPeriod.getSelectedItem().toString();
                                int iPeriod = Integer.parseInt(Period);
                                if (chkAutoLink.isSelected() & (!Main.q.getServer().equals(Main.linkedServer) || !Main.linked) & Minute % 5 == i) {
                                    if (!Main.connected & !Main.Connecting & !Main.bulletinMode & !Main.iacMode) {
                                        if (Main.sending_link > 0 & !Main.configuration.getPreference("CALL").equals("N0CAL")) {
                                            Main.sending_link--;
                                            Main.linkmode = Main.defaultmode;
                                            if (!Main.TxActive) {
                                                try {
                                                    Main.q.Message(mainpskmailui.getString("Link_to_server"), 5);
                                                    Main.q.set_txstatus(TxStatus.TXlinkreq);
                                                    Main.q.send_link();
                                                } catch (InterruptedException ex) {
                                                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (chkBeacon.isSelected() & !Main.Connecting) {
                                    //                                   int j = Integer.valueOf(spnMinute.getValue()).intValue();
                                    int j = Integer.valueOf(spnMinute.getValue().toString()).intValue();
                                    if (iPeriod == 10) {
                                        if (Minute == (j + 10) | Minute == (j + 20) | Minute == (j + 30) | Minute == (j + 40) | Minute == (j + 50)) {

//                                                Main.q.send_beacon();
                                            sendbeacon = true;
                                            Main.configuration.setPreference("LATITUDE", Latitudestr);
                                            Main.configuration.setPreference("LONGITUDE", Longitudestr);

                                        }
                                    } else if (iPeriod == 30) {
                                        if (Minute == (j + 15) | Minute == (j + 45)) {

//                                                Main.q.send_beacon();
                                            sendbeacon = true;
                                            Main.configuration.setPreference("LATITUDE", Latitudestr);
                                            Main.configuration.setPreference("LONGITUDE", Longitudestr);

                                        }
                                    } else {
                                        // 1 hour
                                        if (Minute == (j + 55)) {
                                            //                                               Main.q.send_beacon();
                                            sendbeacon = true;
                                            Main.configuration.setPreference("LATITUDE", Latitudestr);
                                            Main.configuration.setPreference("LONGITUDE", Longitudestr);

                                        }
                                    }
                                }
                                if (File.separator.equals("/") & Main.positionsArray[0][0] != null) {
                                    String waypointfile = Main.homePath + "/way.txt";
                                    File f = new File(waypointfile);
                                    //                                    if (f.exists())
                                    //                                        f.delete();
                                    long epoch_now = System.currentTimeMillis() / 1000;
                                    long dur = 0;
                                    BufferedWriter bw = null;
                                    String s = "";
                                    try {
                                        bw = new BufferedWriter(new FileWriter(waypointfile));
                                        int j = 0;
                                        while (Main.positionsArray[j][2] != null) {
                                            if (!Main.positionsArray[j][4].equals("")) {
                                                dur = Long.valueOf(Main.positionsArray[j][4]).longValue();
                                                if (dur - epoch_now < 60) {
                                                    s = Main.positionsArray[j][1] + "\t" + Main.positionsArray[j][2] + "\t" + Main.positionsArray[j][3] + "\n";
                                                }
                                                bw.write(s);
                                            }
                                            j++;
                                        }
                                        bw.flush();
                                        bw.close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                    } finally {
                                        try {
                                            bw.close();
                                        } catch (IOException ex) {
                                            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                                String ListContent = "Server   S/Q    Bcns   Last   Mode      dB\n";
                                ListContent += "------------------------------------------\n";
                                String srvr = "";
                                String snr = "";
                                String Frames = "";
                                String dbs = "";
                                String mds = "         ";
                                for (i = 0; i < Main.serversArray.length; i++) {
                                    if (Main.serversArray[i].equals("")) {
                                        break;
                                    } else {
                                        srvr = Main.serversArray[i] + "           ";
                                        srvr = srvr.substring(0, 10);
                                        if ((int) Main.serversSnrArray[i] == 100) {
                                            snr = Integer.toString((int) Main.serversSnrArray[i]) + "    ";
                                        } else {
                                            snr = " " + Integer.toString((int) Main.serversSnrArray[i]) + "    ";
                                        }
                                        snr = snr.substring(0, 5);
                                        dbs = "  " + Integer.toString(Main.strength[i]);
                                        mds = Main.modesRcved[i] + "         ";
                                        mds = mds.substring(0, 9);
                                        Frames = Integer.toString(Main.packetsRcved[i]);
                                        if (Main.packetsRcved[i] > 9) {
                                            Frames = "   " + Frames;
                                        } else if (Main.packetsRcved[i] > 99) {
                                            Frames = "  " + Frames;
                                        } else if (Main.packetsRcved[i] > 999) {
                                            Frames = " " + Frames;
                                        } else if (Main.packetsRcved[i] < 10) {
                                            Frames = "    " + Frames;
                                        }
                                        ListContent += srvr + snr + Frames + "  " + Main.lastHeardArray[i] + "  " + mds + dbs + "\n";
                                    }
                                }
                                serverlist.setText(ListContent);
                            }//if not bulletin, not connected, not IAC mode
                        }//modem auto restart end
                    }//minute loop end
                }//second loop end
            }//End override (200ms)
        });
        //Vk2eta debug: done after main window is set visible 
        //timer200ms.start();
        //vk2eta debug
        //System.out.println("Exiting mainpskmailui");

    }

    /**
     *
     * @return
     */
    /**
     * Compare the time of latest gps fix and sound the alarm if its old
     */
    private void checkgpsfixloss(boolean latest) {
        Date timenow;
        // latest iteration had a good fix, update the last fix time
        if (latest) {
            gpsfixlatest = new Date();
        } else // Bad fix so lets see how long ago we got a fix
        {
            //Check this only if we have had a fix before
            if (gpsfixlatest != null) {
                timenow = new Date();
                long timeDiff = (Math.abs(timenow.getTime() - gpsfixlatest.getTime()) / 1000) / 60;
                // We have a five minute old time, warn about that!
                if (timeDiff > 4) {
                    Main.log.writelog("No GPS fix for five minutes or more!", true);
                    //fake a reset to have another warning wait five minutes
                    gpsfixlatest = new Date();
                }
            }
        }
    }

    /**
     * Fetch the entire contents of a text file, and return it in a String. This
     * style of implementation does not throw Exceptions to the caller.
     *
     * @param aFile is a file which already exists and can be read.
     */
    public String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuilder contents = new StringBuilder();

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null; //not declared within while loop
                /*
        * readLine is a bit quirky :
        * it returns the content of a line MINUS the newline.
        * it returns null only for the END of the stream.
        * it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }
    // delete content of file (used for emptying bulletins)

    public void deleteContent(File file) throws FileNotFoundException, IOException {
        FileOutputStream fos;
        DataOutputStream dos;
        fos = new FileOutputStream(file);
        dos = new DataOutputStream(fos);
        dos.writeChars("");
    }

    public String getAPRSIcon() {
        this.Icon = cboAPRSIcon.getSelectedItem().toString();
        return this.Icon;
    }

    /**
     *
     * @param instring
     */
    public void setTxtSpeed(String instring) {
        this.txtSpeed.setText(instring);
    }

    /**
     *
     * @param instring
     */
    public void setClock(String instring) {
        jTextField1.setText(instring);
    }

    public String getClock() {
        return jTextField1.getText();
    }

    /**
     * GPS Fix at.
     *
     * @param instring
     */
    public void setFixAt(String instring) {
        if (instring.length() > 0) {
            this.txtFixTakenAt.setText(instring);
        }
    }

    public boolean getFixTakenAt() {
        if (txtFixTakenAt.getText().length() > 2) {
            return true;
        }
        return false;
    }

    /**
     * Set the latitude text in the gps ui
     *
     * @param instring
     */
    public void setLatitudeText(String instring) {
        String newLat = convcoord(instring);
        if (newLat.charAt(0) == '-') {
            newLat = newLat.substring(1);
            newLat += " S";
        } else {
            newLat += " N";
        }
        txtLatitude.setText(newLat);
    }

    /**
     * Set the longitude text in the gps ui
     *
     * @param instring
     */
    public void setLongitudeText(String instring) {
        String newLon = convcoord(instring);
        if (newLon.charAt(0) == '-') {
            newLon = newLon.substring(1);
            newLon += " W";
        } else {
            newLon += " E";
        }
        txtLongitude.setText(newLon);
    }

    // convert to degrees/minutes
    public String convcoord(String instring) {
        Double lt = 0.0;
        try {
            lt = Double.parseDouble(instring);
        } catch (NumberFormatException e) {
            //Continue using zero
        }
        int deg = lt.intValue();
        Double rest = lt - deg;
        Double minutes = 60 * rest;
        String minstr = Double.toString(minutes);
        if (minstr.charAt(0) == '-') {
            minstr = minstr.substring(1);
        }
        int strln = minstr.length();
        if (strln > 5) {
            strln = 5;
        }
        minstr = minstr.substring(0, strln);
        char c = 176;
        String Degr = Character.toString(c);
        String out = Integer.toString(deg) + Degr + " " + minstr + "\'";
        return (out);
    }

    public void setCourseText(String instring) {
        this.txtCourse.setText(instring);
    }

    public void setSpeedText(String instring) {
        this.txtSpeed.setText(instring);
    }

    /**
     *
     * @param instring
     */
    public void appendMSGWindow(String instring) {
        txtInMsgs.append(instring);
    }

    public void setRxmodeTextfield(String text) {
        RxmodeTextfield.setText(text);
    }

    public void setRXlabel(String text) {
        RXlabel.setText(text);
    }

    public void setProfile(String text) {
        Profile.setText(text);
    }

    /**
     * Append text to the modem monitor area, Im trying to avoid extra blank
     * lines here
     *
     * @param instring
     */
    public void appendTextArea3(String instring) {
        if (instring.contains("*TX*")) {
            try {
                instring = instring.substring(6);
                monitordoc.insertString(monitordoc.getLength(), instring, bold);
            } catch (BadLocationException be) {
                System.err.println("Oops, " + be);
            }
        } else if (Main.m.BlockActive) {
            try {
                monitordoc.insertString(monitordoc.getLength(), instring, blue);
                if (Main.EotRcved) {
                    Main.EotRcved = false;
                    //VK2ETA interferes with timeouts - removed
                    //Main.m.BlockActive = false;
                }
            } catch (BadLocationException be) {
                System.err.println("Oops, " + be);
            }
        } else {
            try {
                monitordoc.insertString(monitordoc.getLength(), instring, grey);
            } catch (BadLocationException be) {
                System.err.println("Oops, " + be);
            }
        }

        // Move the cursor to the end
        txtLinkMonitor.scrollRectToVisible(new Rectangle(0, txtLinkMonitor.getHeight() - 2, 1, 1));

    }

    /**
     *
     * @param instring
     */
    public void appendMainWindow(String instring) {
        if (Main.comp) {
            /*
                try {
                    terminaldoc.insertString(terminaldoc.getLength(), instring, grey);
                 } catch (BadLocationException badLocationException) {
                    System.err.println("Oops");
                 }
             */
        } else if (instring.contains("=>>")) {
            try {
                instring = instring.substring(4);
                terminaldoc.insertString(terminaldoc.getLength(), instring, green);
            } catch (BadLocationException badLocationException) {
                System.err.println("Oops");
            }
        } else {
            try {
                if (!instring.startsWith("~FY:")
                        | instring.startsWith("~FA:")) {
                    terminaldoc.insertString(terminaldoc.getLength(), instring, normal);
                }
            } catch (BadLocationException badLocationException) {
                System.err.println("Oops");
            }
        }
        jTextPane2.scrollRectToVisible(new Rectangle(0, jTextPane2.getHeight() - 2, 1, 1));
    }

    public void appendHeadersWindow(String instring) {
//           MailHeadersWindow.append(instring);
        //TBD: Should we refresh the header window if visible?
    }

    public void setDCDColor(Color Color) {
        pnlStatusIndicator.setBackground(Color);
        pnlStatusIndicator.repaint();
    }

    public void setlinkstatusindicator(Color Color) {
        linkstatus.setBackground(Color);
        linkstatus.repaint();
    }

    public void addServer(String server) {
        cboServer.addItem(server);
//   System.out.println("mainui:adding server " + server )  ;
    }

    public void disableMboxMenu() {
        //mnuMbox2.setVisible(false);
        mnuMbox2.setEnabled(false);
    }

    public void enableMboxMenu() {
        //mnuMbox2.setVisible(true);
        mnuMbox2.setEnabled(true);
    }

    public void disableMnuPreferences2() {
        mnuPreferences2.setEnabled(false);
        PrefSaveMenu.setEnabled(false);
    }

    public void enableMnuPreferences2() {
        mnuPreferences2.setEnabled(true);
        PrefSaveMenu.setEnabled(true);
        //Automatically re-enable all menu options
        resetAllMenus();
    }

    public void setMenuForJavaServer() {
        jGetIAC.setEnabled(false);
        Getforecast.setEnabled(false);
        mnuGetServerfq2.setEnabled(false);
        mnuGetPskmailNews2.setEnabled(false);
        jMenu1.setEnabled(false);
        Resetrecord_mnu.setEnabled(false);
        Update_server.setEnabled(false);
        MnuTelnet.setEnabled(false);
        disableMboxMenu();
    }
  
    public void resetAllMenus() {
        mnuGetServerfq2.setEnabled(true);
        mnuGetPskmailNews2.setEnabled(true);
        MnuTelnet.setEnabled(true);
        //Make PERL server actions not visible (but kept in code for future possible use)
        jGetIAC.setEnabled(false);
        Getforecast.setEnabled(false);
        jGetIAC.setVisible(false);
        Getforecast.setVisible(false);
        Update_server.setEnabled(false);
        Update_server.setVisible(false);
        Resetrecord_mnu.setEnabled(false);
        Resetrecord_mnu.setVisible(false);
        mnuMbox2.setVisible(false);
        jMenu1.setEnabled(true);
        jMenu1.setVisible(false);
        PrefSaveMenu.setVisible(false);
    }
    
    //Hides the non RadioMsg relevant UI components
    private void setRadioMsgUi() {
        mnuMbox2.setVisible(false);
        jMenu1.setVisible(false);
        mnuIACcodes.setVisible(false);
        mnuLink.setVisible(false);
        mnuFqHelp.setVisible(false);
        mnuMailAPRS2.setVisible(false);
        mnuMailScanning.setVisible(false);
        mnuMonitor.setVisible(false);
        mnuModeQSY2.setVisible(false);
        mnuConnection.setVisible(false);
        mnuClear2.setVisible(false);
        mnuFileList.setVisible(false);
        jRadioButtonAccept.setVisible(false);
        jRadioButtonReject.setVisible(false);
        jRadioButtonDelete.setVisible(false);
        for (int i = 0; i < 7; i++) {
                tabMain.removeTabAt(0);
        }
        //Remove modem text
        jScrollPane3.setVisible(false);
    }
        
    public void disableMonitor() {
        if (Main.monitorMode) {
            Main.monitorMode = false;
            mnuMonitor.setSelected(false);
        }
    }

    public void debug(String message) {
        System.out.println("Debug:" + message);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mnuEmailPopup = new javax.swing.JPopupMenu();
        mnuEmailOpenGet = new javax.swing.JMenuItem();
        mnuTerminalPopup = new javax.swing.JPopupMenu();
        mnuTextSave = new javax.swing.JMenuItem();
        mnuOutbox = new javax.swing.JPopupMenu();
        mnuOutboxOpenMsg = new javax.swing.JMenuItem();
        mnuOutboxDeleteMsg = new javax.swing.JMenuItem();
        mnuHeaders = new javax.swing.JPopupMenu();
        mnuHeadersFetch = new javax.swing.JMenuItem();
        aprsscanmonitorgroup = new javax.swing.ButtonGroup();
        modemnubuttons = new javax.swing.ButtonGroup();
        buttonGroupAlias = new javax.swing.ButtonGroup();
        buttonGroupPartialDownloads = new javax.swing.ButtonGroup();
        tabMain = new javax.swing.JTabbedPane();
        tabTerminal = new javax.swing.JPanel();
        pnlTerminalButtons = new javax.swing.JPanel();
        bConnect = new javax.swing.JButton();
        AbortButton = new javax.swing.JButton();
        CQButton = new javax.swing.JButton();
        PositButton = new javax.swing.JButton();
        SendButton = new javax.swing.JButton();
        jButtonRunScript = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        tabEmail = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        scrEmailLeft = new javax.swing.JScrollPane();
        lstBoxSelector = new javax.swing.JList();
        scrEmailRight = new javax.swing.JScrollPane();
        tblInbox = new javax.swing.JTable();
        pnlEmailButtons = new javax.swing.JPanel();
        bNewMail = new javax.swing.JButton();
        bContacts = new javax.swing.JButton();
        bQTC = new javax.swing.JButton();
        EmailSendButton = new javax.swing.JButton();
        bDelete = new javax.swing.JButton();
        tabFiles = new javax.swing.JPanel();
        pnlFilesButtons = new javax.swing.JPanel();
        FileConnect = new javax.swing.JButton();
        FileAbortButton = new javax.swing.JButton();
        FileReadButton = new javax.swing.JButton();
        UpdateButton = new javax.swing.JButton();
        DownloadButton = new javax.swing.JButton();
        FileSendButton = new javax.swing.JButton();
        ListFilesButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        FilesTxtArea = new javax.swing.JTextArea();
        tabAPRS = new javax.swing.JPanel();
        pnlGPS = new javax.swing.JPanel();
        lblLatitude = new javax.swing.JLabel();
        txtLatitude = new javax.swing.JTextField();
        lblLongitude = new javax.swing.JLabel();
        txtLongitude = new javax.swing.JTextField();
        lblCourse = new javax.swing.JLabel();
        lblSpeed = new javax.swing.JLabel();
        txtCourse = new javax.swing.JTextField();
        txtSpeed = new javax.swing.JTextField();
        chkBeacon = new javax.swing.JCheckBox();
        txtFixTakenAt = new javax.swing.JTextField();
        lblFixat = new javax.swing.JLabel();
        cbComp = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        pnlBeacon = new javax.swing.JPanel();
        lblAPRSIcon = new javax.swing.JLabel();
        cboAPRSIcon = new javax.swing.JComboBox();
        scrInMsgs = new javax.swing.JScrollPane();
        txtInMsgs = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        cboBeaconPeriod = new javax.swing.JComboBox();
        lblStatusMsg = new javax.swing.JLabel();
        txtStatus = new javax.swing.JTextField();
        chkAutoLink = new javax.swing.JCheckBox();
        lblAutoServer = new javax.swing.JLabel();
        cboAPRS2nd = new javax.swing.JComboBox();
        tabModem = new javax.swing.JPanel();
        pnlModemArq = new javax.swing.JPanel();
        lblRXMode = new javax.swing.JLabel();
        RxmodeTextfield = new javax.swing.JTextField();
        lblTXMode = new javax.swing.JLabel();
        TxmodeTextfield = new javax.swing.JTextField();
        lblRxModeQ = new javax.swing.JLabel();
        TxmodeQuality = new javax.swing.JProgressBar();
        lblTXModeQ = new javax.swing.JLabel();
        Profile = new javax.swing.JTextField();
        lblProfile = new javax.swing.JLabel();
        lblBSize = new javax.swing.JLabel();
        Size_indicator = new javax.swing.JTextField();
        lblCPS = new javax.swing.JLabel();
        CPSValue = new javax.swing.JTextField();
        lblTotalBytes = new javax.swing.JLabel();
        Totalbytes = new javax.swing.JTextField();
        lblThroughput = new javax.swing.JLabel();
        Throughput = new javax.swing.JTextField();
        SNRlbl = new javax.swing.JLabel();
        RxmodeQuality = new javax.swing.JProgressBar();
        statistics = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        serverlist = new javax.swing.JTextArea();
        tabIgate = new javax.swing.JPanel();
        IgateCallField = new javax.swing.JTextField();
        IgateCall = new javax.swing.JLabel();
        IgateIndicator = new javax.swing.JLabel();
        IgateSwitch = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        IgateTextArea = new javax.swing.JTextArea();
        APRSServerSelect = new javax.swing.JComboBox();
        APRS_IS = new javax.swing.JCheckBox();
        lblIgateStatus = new javax.swing.JLabel();
        tabRigctl = new javax.swing.JPanel();
        pnlSummoning = new javax.swing.JPanel();
        labelCurrentFreq = new javax.swing.JLabel();
        ClientFreqTxtfield = new javax.swing.JTextField();
        labelServerFreq = new javax.swing.JLabel();
        ServerfreqTxtfield = new javax.swing.JTextField();
        SetToChannelButton = new javax.swing.JButton();
        Upbutton = new javax.swing.JButton();
        Downbutton = new javax.swing.JButton();
        bSummon = new javax.swing.JButton();
        rigctlactivelbl = new javax.swing.JLabel();
        modelbl = new javax.swing.JLabel();
        pnlFreqs = new javax.swing.JPanel();
        ScannerCheckbox = new javax.swing.JCheckBox();
        freq0 = new javax.swing.JTextField();
        freq1 = new javax.swing.JTextField();
        freq2 = new javax.swing.JTextField();
        freq3 = new javax.swing.JTextField();
        freq4 = new javax.swing.JTextField();
        tabRadioMsg = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        bContacts1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jComboRMsgTo = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jRadBtnAliasOnly = new javax.swing.JRadioButton();
        jRadBtnAliasAndAddress = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jComboRMsgVia = new javax.swing.JComboBox<>();
        scrRadioMessages = new javax.swing.JScrollPane();
        tblRadioMsgs = new javax.swing.JTable();
        pnlRMSgButtons = new javax.swing.JPanel();
        bRMsgSendSMS = new javax.swing.JButton();
        bRMsgSendInquire = new javax.swing.JButton();
        bRMsgSendPos = new javax.swing.JButton();
        bRMsgReqPos = new javax.swing.JButton();
        bRMsgResendLast = new javax.swing.JButton();
        bRMsgResend = new javax.swing.JButton();
        serverControl = new javax.swing.JButton();
        bRMsgManageMsg = new javax.swing.JButton();
        pnlStatus = new javax.swing.JPanel();
        snLabel = new javax.swing.JLabel();
        StatusLabel = new javax.swing.JLabel();
        cboServer = new javax.swing.JComboBox<>();
        spnMinute = new javax.swing.JSpinner();
        ProgressBar = new javax.swing.JProgressBar();
        jTextField1 = new javax.swing.JTextField();
        linkstatus = new javax.swing.JPanel();
        RXlabel = new javax.swing.JLabel();
        pnlMainEntry = new javax.swing.JPanel();
        txtMainEntry = new javax.swing.JTextField();
        lblStatus = new javax.swing.JLabel();
        pnlStatusIndicator = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtLinkMonitor = new javax.swing.JTextPane();
        jMenuBar3 = new javax.swing.JMenuBar();
        mnuFile2 = new javax.swing.JMenu();
        mnuConnection = new javax.swing.JMenu();
        Conn_connect = new javax.swing.JMenuItem();
        Conn_abort = new javax.swing.JMenuItem();
        Conn_send = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        mnuClear2 = new javax.swing.JMenu();
        mnuHeaders2 = new javax.swing.JMenuItem();
        mnuBulletins2 = new javax.swing.JMenuItem();
        mnuUploads = new javax.swing.JMenuItem();
        mnuDownloads = new javax.swing.JMenuItem();
        mnuClearInbox = new javax.swing.JMenuItem();
        mnuClearOutbox = new javax.swing.JMenuItem();
        mnuFileList = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jRadioButtonAccept = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonReject = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonDelete = new javax.swing.JRadioButtonMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mnuQuit2 = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        mnuMbox2 = new javax.swing.JMenu();
        mnuMboxList2 = new javax.swing.JMenuItem();
        mnuMboxRead2 = new javax.swing.JMenuItem();
        mnuMboxDelete2 = new javax.swing.JMenuItem();
        mnuMode2 = new javax.swing.JMenu();
        mnuMailAPRS2 = new javax.swing.JRadioButtonMenuItem();
        mnuMailScanning = new javax.swing.JRadioButtonMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuMonitor = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        mnuModeQSY2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mnuPSK63 = new javax.swing.JRadioButtonMenuItem();
        mnuPSK125 = new javax.swing.JRadioButtonMenuItem();
        mnuPSK250 = new javax.swing.JRadioButtonMenuItem();
        mnuPSK500 = new javax.swing.JRadioButtonMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        mnuPSK125R = new javax.swing.JRadioButtonMenuItem();
        mnuPSK250R = new javax.swing.JRadioButtonMenuItem();
        mnuPSK500R = new javax.swing.JRadioButtonMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        mnuTHOR8 = new javax.swing.JRadioButtonMenuItem();
        mnuTHOR22 = new javax.swing.JRadioButtonMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        mnuMFSK16 = new javax.swing.JRadioButtonMenuItem();
        mnuMFSK32 = new javax.swing.JRadioButtonMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        mnuDOMINOEX5 = new javax.swing.JRadioButtonMenuItem();
        mnuDOMINOEX11 = new javax.swing.JRadioButtonMenuItem();
        mnuDOMINOEX22 = new javax.swing.JRadioButtonMenuItem();
        defaultmnu = new javax.swing.JRadioButtonMenuItem();
        jMenu1 = new javax.swing.JMenu();
        Twitter_send = new javax.swing.JMenuItem();
        GetUpdatesmenuItem = new javax.swing.JMenuItem();
        mnuIACcodes = new javax.swing.JMenu();
        menuMessages = new javax.swing.JMenuItem();
        mnuGetTidestations2 = new javax.swing.JMenuItem();
        mnuGetTide2 = new javax.swing.JMenuItem();
        GetGrib = new javax.swing.JMenuItem();
        jGetIAC = new javax.swing.JMenuItem();
        Getforecast = new javax.swing.JMenuItem();
        WWV_menu_item = new javax.swing.JMenuItem();
        mnuGetAPRS2 = new javax.swing.JMenuItem();
        mnuGetServerfq2 = new javax.swing.JMenuItem();
        mnuGetPskmailNews2 = new javax.swing.JMenuItem();
        mnuGetWebPages2 = new javax.swing.JMenuItem();
        mnuLink = new javax.swing.JMenu();
        Ping_menu_item = new javax.swing.JMenuItem();
        menuInquire = new javax.swing.JMenuItem();
        timeSyncMenuItem = new javax.swing.JMenuItem();
        jMenuQuality = new javax.swing.JMenuItem();
        Link_menu_item = new javax.swing.JMenuItem();
        Beacon_menu_item = new javax.swing.JMenuItem();
        MnuTelnet = new javax.swing.JMenuItem();
        mnuMulticast = new javax.swing.JMenuItem();
        Update_server = new javax.swing.JMenuItem();
        Resetrecord_mnu = new javax.swing.JMenuItem();
        Stoptransaction_mnu = new javax.swing.JMenuItem();
        mnuPrefsMain = new javax.swing.JMenu();
        mnuFqHelp = new javax.swing.JMenuItem();
        mnuPreferences2 = new javax.swing.JMenuItem();
        PrefSaveMenu = new javax.swing.JMenuItem();
        mnuHelpMain2 = new javax.swing.JMenu();
        mnuAbout2 = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui"); // NOI18N
        mnuEmailOpenGet.setText(bundle.getString("MainPskmailUi.mnuEmailOpenGet.text_1")); // NOI18N
        mnuEmailOpenGet.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuEmailOpenGetMouseClicked(evt);
            }
        });
        mnuEmailOpenGet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEmailOpenGetActionPerformed(evt);
            }
        });
        mnuEmailPopup.add(mnuEmailOpenGet);

        mnuTextSave.setText(bundle.getString("MainPskmailUi.mnuTextSave.text_1")); // NOI18N
        mnuTextSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTextSaveActionPerformed(evt);
            }
        });
        mnuTerminalPopup.add(mnuTextSave);

        mnuOutboxOpenMsg.setText(bundle.getString("MainPskmailUi.mnuOutboxOpenMsg.text_1")); // NOI18N
        mnuOutboxOpenMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutboxOpenMsgActionPerformed(evt);
            }
        });
        mnuOutbox.add(mnuOutboxOpenMsg);

        mnuOutboxDeleteMsg.setText(bundle.getString("MainPskmailUi.mnuOutboxDeleteMsg.text_1")); // NOI18N
        mnuOutboxDeleteMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutboxDeleteMsgActionPerformed(evt);
            }
        });
        mnuOutbox.add(mnuOutboxDeleteMsg);

        mnuHeadersFetch.setText(bundle.getString("MainPskmailUi.mnuHeadersFetch.text_1")); // NOI18N
        mnuHeadersFetch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHeadersFetchActionPerformed(evt);
            }
        });
        mnuHeaders.add(mnuHeadersFetch);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("javaRadioMsg"); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(640, 480));
        setPreferredSize(new java.awt.Dimension(640, 469));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabMain.setBackground(new java.awt.Color(251, 219, 187));
        tabMain.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabMain.setDoubleBuffered(true);
        tabMain.setMinimumSize(new java.awt.Dimension(725, 290));
        tabMain.setPreferredSize(new java.awt.Dimension(725, 290));
        tabMain.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabMainStateChanged(evt);
            }
        });

        tabTerminal.setMinimumSize(new java.awt.Dimension(708, 306));
        tabTerminal.setName(""); // NOI18N
        tabTerminal.setLayout(new javax.swing.BoxLayout(tabTerminal, javax.swing.BoxLayout.PAGE_AXIS));

        pnlTerminalButtons.setMaximumSize(new java.awt.Dimension(32767, 40));
        pnlTerminalButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        bConnect.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        bConnect.setForeground(new java.awt.Color(0, 102, 51));
        bConnect.setText(bundle.getString("MainPskmailUi.bConnect.text_1")); // NOI18N
        bConnect.setMaximumSize(new java.awt.Dimension(100, 25));
        bConnect.setMinimumSize(new java.awt.Dimension(85, 25));
        bConnect.setPreferredSize(new java.awt.Dimension(90, 25));
        bConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bConnectActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(bConnect);

        AbortButton.setFont(new java.awt.Font("SansSerif", 1, 11)); // NOI18N
        AbortButton.setForeground(new java.awt.Color(0, 102, 0));
        AbortButton.setText(bundle.getString("MainPskmailUi.AbortButton.text_1")); // NOI18N
        AbortButton.setMaximumSize(new java.awt.Dimension(100, 25));
        AbortButton.setMinimumSize(new java.awt.Dimension(70, 25));
        AbortButton.setPreferredSize(new java.awt.Dimension(100, 25));
        AbortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbortButtonActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(AbortButton);

        CQButton.setFont(new java.awt.Font("SansSerif", 1, 11)); // NOI18N
        CQButton.setForeground(new java.awt.Color(0, 128, 19));
        CQButton.setText(bundle.getString("MainPskmailUi.CQButton.text_1")); // NOI18N
        CQButton.setMaximumSize(new java.awt.Dimension(100, 25));
        CQButton.setMinimumSize(new java.awt.Dimension(70, 25));
        CQButton.setPreferredSize(new java.awt.Dimension(100, 25));
        CQButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CQButtonActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(CQButton);

        PositButton.setFont(new java.awt.Font("SansSerif", 1, 11)); // NOI18N
        PositButton.setForeground(new java.awt.Color(0, 102, 0));
        PositButton.setText(bundle.getString("MainPskmailUi.PositButton.text_1")); // NOI18N
        PositButton.setMaximumSize(new java.awt.Dimension(100, 25));
        PositButton.setMinimumSize(new java.awt.Dimension(70, 25));
        PositButton.setPreferredSize(new java.awt.Dimension(100, 25));
        PositButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PositButtonActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(PositButton);

        SendButton.setFont(new java.awt.Font("SansSerif", 1, 11)); // NOI18N
        SendButton.setForeground(new java.awt.Color(0, 102, 0));
        SendButton.setText(bundle.getString("MainPskmailUi.SendButton.text_1")); // NOI18N
        SendButton.setFocusPainted(false);
        SendButton.setMaximumSize(new java.awt.Dimension(100, 20));
        SendButton.setMinimumSize(new java.awt.Dimension(70, 20));
        SendButton.setPreferredSize(new java.awt.Dimension(100, 25));
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(SendButton);

        jButtonRunScript.setFont(new java.awt.Font("sansserif", 1, 11)); // NOI18N
        jButtonRunScript.setForeground(new java.awt.Color(0, 102, 0));
        jButtonRunScript.setText(bundle.getString("MainPskmailUi.jButtonRunScript.text")); // NOI18N
        jButtonRunScript.setToolTipText(bundle.getString("mainpskmailui.runscriptbutton.tooltiptext")); // NOI18N
        jButtonRunScript.setMaximumSize(new java.awt.Dimension(95, 25));
        jButtonRunScript.setMinimumSize(new java.awt.Dimension(95, 25));
        jButtonRunScript.setPreferredSize(new java.awt.Dimension(95, 25));
        jButtonRunScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunScriptActionPerformed(evt);
            }
        });
        pnlTerminalButtons.add(jButtonRunScript);

        tabTerminal.add(pnlTerminalButtons);

        jTextPane2.setBackground(new java.awt.Color(255, 255, 230));
        jTextPane2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jTextPane2.setDocument(terminaldoc);
        jTextPane2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jTextPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextPane2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTextPane2MouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTextPane2);

        tabTerminal.add(jScrollPane1);

        tabMain.addTab(mainpskmailui.getString("Terminal"), tabTerminal); // NOI18N

        tabEmail.setName("tabEmail"); // NOI18N
        tabEmail.setLayout(new java.awt.BorderLayout());

        lstBoxSelector.setBackground(new java.awt.Color(255, 255, 230));
        lstBoxSelector.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        lstBoxSelector.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Headers", "Inbox", "Outbox", "Sent" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstBoxSelector.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstBoxSelector.setMaximumSize(new java.awt.Dimension(200, 125));
        lstBoxSelector.setMinimumSize(new java.awt.Dimension(90, 125));
        lstBoxSelector.setPreferredSize(new java.awt.Dimension(90, 125));
        lstBoxSelector.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstBoxSelectorMouseClicked(evt);
            }
        });
        lstBoxSelector.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstBoxSelectorValueChanged(evt);
            }
        });
        scrEmailLeft.setViewportView(lstBoxSelector);

        jSplitPane1.setLeftComponent(scrEmailLeft);

        tblInbox.setAutoCreateRowSorter(true);
        tblInbox.setBackground(new java.awt.Color(255, 255, 230));
        tblInbox.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Number", "", "Subject", "Size"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblInbox.setSelectionBackground(new java.awt.Color(150, 150, 150));
        tblInbox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblInbox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblInboxMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblInboxMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblInboxMouseClicked(evt);
            }
        });
        scrEmailRight.setViewportView(tblInbox);
        if (tblInbox.getColumnModel().getColumnCount() > 0) {
            tblInbox.getColumnModel().getColumn(0).setMinWidth(50);
            tblInbox.getColumnModel().getColumn(0).setPreferredWidth(50);
            tblInbox.getColumnModel().getColumn(0).setMaxWidth(100);
            tblInbox.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("MainPskmailUi.tblInbox.columnModel.title0_1_1")); // NOI18N
            tblInbox.getColumnModel().getColumn(1).setMinWidth(80);
            tblInbox.getColumnModel().getColumn(1).setPreferredWidth(80);
            tblInbox.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("MainPskmailUi.tblInbox.columnModel.title1_1")); // NOI18N
            tblInbox.getColumnModel().getColumn(2).setMinWidth(100);
            tblInbox.getColumnModel().getColumn(2).setPreferredWidth(200);
            tblInbox.getColumnModel().getColumn(2).setMaxWidth(400);
            tblInbox.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("MainPskmailUi.tblInbox.columnModel.title2_1")); // NOI18N
            tblInbox.getColumnModel().getColumn(3).setMinWidth(50);
            tblInbox.getColumnModel().getColumn(3).setPreferredWidth(50);
            tblInbox.getColumnModel().getColumn(3).setMaxWidth(150);
            tblInbox.getColumnModel().getColumn(3).setHeaderValue(bundle.getString("MainPskmailUi.tblInbox.columnModel.title3_1")); // NOI18N
        }

        jSplitPane1.setRightComponent(scrEmailRight);

        tabEmail.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pnlEmailButtons.setMaximumSize(new java.awt.Dimension(32767, 40));
        pnlEmailButtons.setMinimumSize(new java.awt.Dimension(0, 30));
        pnlEmailButtons.setPreferredSize(new java.awt.Dimension(717, 35));
        pnlEmailButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        bNewMail.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bNewMail.setForeground(new java.awt.Color(0, 102, 51));
        bNewMail.setText(bundle.getString("MainPskmailUi.bNewMail.text_1")); // NOI18N
        bNewMail.setToolTipText(bundle.getString("MainPskmailUi.bNewMail.toolTipText_1")); // NOI18N
        bNewMail.setMaximumSize(new java.awt.Dimension(110, 25));
        bNewMail.setMinimumSize(new java.awt.Dimension(80, 25));
        bNewMail.setPreferredSize(new java.awt.Dimension(100, 25));
        bNewMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNewMailActionPerformed(evt);
            }
        });
        pnlEmailButtons.add(bNewMail);

        bContacts.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bContacts.setForeground(new java.awt.Color(0, 102, 51));
        bContacts.setText(bundle.getString("MainPskmailUi.bContacts.text_1")); // NOI18N
        bContacts.setMaximumSize(new java.awt.Dimension(110, 27));
        bContacts.setMinimumSize(new java.awt.Dimension(80, 27));
        bContacts.setPreferredSize(new java.awt.Dimension(100, 27));
        bContacts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bContactsActionPerformed(evt);
            }
        });
        pnlEmailButtons.add(bContacts);

        bQTC.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bQTC.setForeground(new java.awt.Color(0, 102, 51));
        bQTC.setText(bundle.getString("MainPskmailUi.bQTC.text_1")); // NOI18N
        bQTC.setToolTipText(bundle.getString("MainPskmailUi.bQTC.toolTipText_1")); // NOI18N
        bQTC.setMaximumSize(new java.awt.Dimension(110, 25));
        bQTC.setMinimumSize(new java.awt.Dimension(80, 25));
        bQTC.setPreferredSize(new java.awt.Dimension(100, 25));
        bQTC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bQTCActionPerformed(evt);
            }
        });
        pnlEmailButtons.add(bQTC);

        EmailSendButton.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        EmailSendButton.setForeground(new java.awt.Color(12, 134, 40));
        EmailSendButton.setText(bundle.getString("MainPskmailUi.EmailSendButton.text_1")); // NOI18N
        EmailSendButton.setMaximumSize(new java.awt.Dimension(110, 25));
        EmailSendButton.setMinimumSize(new java.awt.Dimension(80, 25));
        EmailSendButton.setPreferredSize(new java.awt.Dimension(100, 25));
        EmailSendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EmailSendButtonActionPerformed(evt);
            }
        });
        pnlEmailButtons.add(EmailSendButton);

        bDelete.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bDelete.setForeground(new java.awt.Color(0, 102, 51));
        bDelete.setText(bundle.getString("MainPskmailUi.bDelete.text_1")); // NOI18N
        bDelete.setToolTipText(bundle.getString("MainPskmailUi.bDelete.toolTipText_1")); // NOI18N
        bDelete.setMaximumSize(new java.awt.Dimension(110, 25));
        bDelete.setMinimumSize(new java.awt.Dimension(80, 25));
        bDelete.setPreferredSize(new java.awt.Dimension(100, 25));
        bDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDeleteActionPerformed(evt);
            }
        });
        pnlEmailButtons.add(bDelete);

        tabEmail.add(pnlEmailButtons, java.awt.BorderLayout.PAGE_START);

        tabMain.addTab(bundle.getString("MainPskmailUi.tabEmail.TabConstraints.tabTitle_1_1"), tabEmail); // NOI18N

        tabFiles.setLayout(new javax.swing.BoxLayout(tabFiles, javax.swing.BoxLayout.PAGE_AXIS));

        pnlFilesButtons.setMaximumSize(new java.awt.Dimension(2000, 40));
        pnlFilesButtons.setMinimumSize(new java.awt.Dimension(525, 30));
        pnlFilesButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        FileConnect.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        FileConnect.setForeground(new java.awt.Color(0, 102, 0));
        FileConnect.setText(bundle.getString("MainPskmailUi.FileConnect.text_1")); // NOI18N
        FileConnect.setMaximumSize(new java.awt.Dimension(110, 25));
        FileConnect.setMinimumSize(new java.awt.Dimension(80, 25));
        FileConnect.setPreferredSize(new java.awt.Dimension(90, 25));
        FileConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileConnectActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(FileConnect);

        FileAbortButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        FileAbortButton.setForeground(new java.awt.Color(0, 102, 0));
        FileAbortButton.setText(bundle.getString("MainPskmailUi.FileAbortButton.text_1")); // NOI18N
        FileAbortButton.setMaximumSize(new java.awt.Dimension(110, 25));
        FileAbortButton.setMinimumSize(new java.awt.Dimension(80, 25));
        FileAbortButton.setPreferredSize(new java.awt.Dimension(90, 25));
        FileAbortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileAbortButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(FileAbortButton);

        FileReadButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        FileReadButton.setForeground(new java.awt.Color(0, 102, 0));
        FileReadButton.setText(mainpskmailui.getString("MainPskmailUi.FileReadButton.text_1")); // NOI18N
        FileReadButton.setMaximumSize(new java.awt.Dimension(110, 25));
        FileReadButton.setMinimumSize(new java.awt.Dimension(80, 25));
        FileReadButton.setPreferredSize(new java.awt.Dimension(90, 25));
        FileReadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileReadButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(FileReadButton);

        UpdateButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        UpdateButton.setForeground(new java.awt.Color(0, 102, 0));
        UpdateButton.setText(bundle.getString("MainPskmailUi.UpdateButton.text_1")); // NOI18N
        UpdateButton.setMaximumSize(new java.awt.Dimension(110, 25));
        UpdateButton.setMinimumSize(new java.awt.Dimension(80, 25));
        UpdateButton.setPreferredSize(new java.awt.Dimension(90, 25));
        UpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(UpdateButton);

        DownloadButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        DownloadButton.setForeground(new java.awt.Color(0, 102, 0));
        DownloadButton.setText(bundle.getString("MainPskmailUi.DownloadButton.text_1")); // NOI18N
        DownloadButton.setMaximumSize(new java.awt.Dimension(110, 25));
        DownloadButton.setMinimumSize(new java.awt.Dimension(80, 25));
        DownloadButton.setPreferredSize(new java.awt.Dimension(90, 25));
        DownloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DownloadButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(DownloadButton);

        FileSendButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        FileSendButton.setForeground(new java.awt.Color(8, 91, 43));
        FileSendButton.setText(bundle.getString("MainPskmailUi.FileSendButton.text_1")); // NOI18N
        FileSendButton.setMaximumSize(new java.awt.Dimension(110, 25));
        FileSendButton.setMinimumSize(new java.awt.Dimension(80, 25));
        FileSendButton.setPreferredSize(new java.awt.Dimension(90, 25));
        FileSendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileSendButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(FileSendButton);

        ListFilesButton.setFont(new java.awt.Font("Metal", 1, 11)); // NOI18N
        ListFilesButton.setForeground(new java.awt.Color(0, 102, 0));
        ListFilesButton.setText(bundle.getString("MainPskmailUi.ListFilesButton.text_1")); // NOI18N
        ListFilesButton.setMaximumSize(new java.awt.Dimension(110, 25));
        ListFilesButton.setMinimumSize(new java.awt.Dimension(80, 25));
        ListFilesButton.setPreferredSize(new java.awt.Dimension(90, 25));
        ListFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ListFilesButtonActionPerformed(evt);
            }
        });
        pnlFilesButtons.add(ListFilesButton);

        tabFiles.add(pnlFilesButtons);

        FilesTxtArea.setBackground(new java.awt.Color(255, 255, 230));
        FilesTxtArea.setColumns(20);
        FilesTxtArea.setFont(new java.awt.Font("DialogInput", 0, 12)); // NOI18N
        FilesTxtArea.setRows(5);
        FilesTxtArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                FilesTxtAreaMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(FilesTxtArea);

        tabFiles.add(jScrollPane4);

        tabMain.addTab(mainpskmailui.getString("Files"), tabFiles); // NOI18N

        tabAPRS.setMinimumSize(new java.awt.Dimension(670, 260));
        tabAPRS.setPreferredSize(new java.awt.Dimension(680, 260));
        tabAPRS.setLayout(new javax.swing.BoxLayout(tabAPRS, javax.swing.BoxLayout.LINE_AXIS));

        pnlGPS.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.pnlGPS.border.title_1"))); // NOI18N
        pnlGPS.setMaximumSize(new java.awt.Dimension(250, 800));
        pnlGPS.setMinimumSize(new java.awt.Dimension(240, 250));
        pnlGPS.setPreferredSize(new java.awt.Dimension(240, 250));
        pnlGPS.setLayout(new java.awt.GridBagLayout());

        lblLatitude.setForeground(new java.awt.Color(0, 51, 204));
        lblLatitude.setText(bundle.getString("MainPskmailUi.lblLatitude.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlGPS.add(lblLatitude, gridBagConstraints);

        txtLatitude.setEditable(false);
        txtLatitude.setMinimumSize(new java.awt.Dimension(120, 27));
        txtLatitude.setPreferredSize(new java.awt.Dimension(120, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlGPS.add(txtLatitude, gridBagConstraints);

        lblLongitude.setForeground(new java.awt.Color(0, 51, 204));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlGPS.add(lblLongitude, gridBagConstraints);

        txtLongitude.setEditable(false);
        txtLongitude.setMinimumSize(new java.awt.Dimension(120, 27));
        txtLongitude.setPreferredSize(new java.awt.Dimension(120, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlGPS.add(txtLongitude, gridBagConstraints);

        lblCourse.setForeground(new java.awt.Color(0, 51, 204));
        lblCourse.setText(bundle.getString("MainPskmailUi.lblCourse.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlGPS.add(lblCourse, gridBagConstraints);

        lblSpeed.setForeground(new java.awt.Color(0, 51, 204));
        lblSpeed.setText(bundle.getString("MainPskmailUi.lblSpeed.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlGPS.add(lblSpeed, gridBagConstraints);

        txtCourse.setEditable(false);
        txtCourse.setToolTipText(mainpskmailui.getString("Course_Made_Good,_degrees_true_")); // NOI18N
        txtCourse.setMinimumSize(new java.awt.Dimension(120, 27));
        txtCourse.setPreferredSize(new java.awt.Dimension(120, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlGPS.add(txtCourse, gridBagConstraints);

        txtSpeed.setEditable(false);
        txtSpeed.setToolTipText(mainpskmailui.getString("Speed_over_ground_in_knots")); // NOI18N
        txtSpeed.setMinimumSize(new java.awt.Dimension(120, 27));
        txtSpeed.setPreferredSize(new java.awt.Dimension(120, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlGPS.add(txtSpeed, gridBagConstraints);

        chkBeacon.setForeground(new java.awt.Color(0, 51, 204));
        chkBeacon.setText(bundle.getString("MainPskmailUi.chkBeacon.text_1")); // NOI18N
        chkBeacon.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkBeaconStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlGPS.add(chkBeacon, gridBagConstraints);

        txtFixTakenAt.setEditable(false);
        txtFixTakenAt.setToolTipText(mainpskmailui.getString("The_time_in_UTC_the_fix_was_taken")); // NOI18N
        txtFixTakenAt.setMinimumSize(new java.awt.Dimension(120, 27));
        txtFixTakenAt.setPreferredSize(new java.awt.Dimension(120, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlGPS.add(txtFixTakenAt, gridBagConstraints);

        lblFixat.setForeground(new java.awt.Color(0, 51, 204));
        lblFixat.setText(bundle.getString("MainPskmailUi.lblFixat.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlGPS.add(lblFixat, gridBagConstraints);

        cbComp.setForeground(new java.awt.Color(70, 71, 222));
        cbComp.setSelected(true);
        cbComp.setText("Comp."); // NOI18N
        cbComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbCompStateChanged(evt);
            }
        });
        cbComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        pnlGPS.add(cbComp, gridBagConstraints);

        jLabel3.setForeground(new java.awt.Color(49, 65, 178));
        jLabel3.setText(bundle.getString("MainPskmailUi.jLabel3.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 0, 1);
        pnlGPS.add(jLabel3, gridBagConstraints);

        tabAPRS.add(pnlGPS);

        pnlBeacon.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.pnlBeacon.border.title_1"))); // NOI18N
        pnlBeacon.setMinimumSize(new java.awt.Dimension(470, 250));
        pnlBeacon.setPreferredSize(new java.awt.Dimension(470, 250));
        pnlBeacon.setLayout(new java.awt.GridBagLayout());

        lblAPRSIcon.setForeground(new java.awt.Color(0, 51, 204));
        lblAPRSIcon.setText(bundle.getString("MainPskmailUi.lblAPRSIcon.text_1")); // NOI18N
        lblAPRSIcon.setMaximumSize(new java.awt.Dimension(50, 25));
        lblAPRSIcon.setMinimumSize(new java.awt.Dimension(30, 25));
        lblAPRSIcon.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlBeacon.add(lblAPRSIcon, gridBagConstraints);

        cboAPRSIcon.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "y", "-", "Y", "s", ">", "U", ";", "R", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "+", "-", "=", ":", ";", "\"", "'", ",", ".", "<", ">", "?", "/", "\\" }));
            cboAPRSIcon.setMinimumSize(new java.awt.Dimension(55, 27));
            cboAPRSIcon.setPreferredSize(new java.awt.Dimension(40, 25));
            cboAPRSIcon.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cboAPRSIconActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            pnlBeacon.add(cboAPRSIcon, gridBagConstraints);

            scrInMsgs.setMinimumSize(new java.awt.Dimension(225, 120));
            scrInMsgs.setPreferredSize(new java.awt.Dimension(225, 120));

            txtInMsgs.setBackground(new java.awt.Color(255, 255, 230));
            txtInMsgs.setColumns(20);
            txtInMsgs.setFont(new java.awt.Font("Nimbus Mono L", 0, 12)); // NOI18N
            txtInMsgs.setRows(200);
            txtInMsgs.setWrapStyleWord(true);
            txtInMsgs.setMinimumSize(new java.awt.Dimension(600, 100));
            scrInMsgs.setViewportView(txtInMsgs);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            pnlBeacon.add(scrInMsgs, gridBagConstraints);

            jLabel1.setForeground(new java.awt.Color(0, 51, 204));
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
            jLabel1.setText(bundle.getString("MainPskmailUi.jLabel1.text_1")); // NOI18N
            jLabel1.setMaximumSize(new java.awt.Dimension(110, 19));
            jLabel1.setMinimumSize(new java.awt.Dimension(110, 19));
            jLabel1.setPreferredSize(new java.awt.Dimension(120, 19));
            pnlBeacon.add(jLabel1, new java.awt.GridBagConstraints());

            cboBeaconPeriod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "30", "60", "10" }));
            cboBeaconPeriod.setMinimumSize(new java.awt.Dimension(60, 27));
            cboBeaconPeriod.setPreferredSize(new java.awt.Dimension(60, 27));
            cboBeaconPeriod.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cboBeaconPeriodActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            pnlBeacon.add(cboBeaconPeriod, gridBagConstraints);

            lblStatusMsg.setForeground(new java.awt.Color(0, 51, 204));
            lblStatusMsg.setText(bundle.getString("MainPskmailUi.lblStatusMsg.text_1")); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            pnlBeacon.add(lblStatusMsg, gridBagConstraints);

            txtStatus.setMinimumSize(new java.awt.Dimension(210, 24));
            txtStatus.setPreferredSize(new java.awt.Dimension(210, 27));
            txtStatus.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    txtStatusKeyReleased(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 5);
            pnlBeacon.add(txtStatus, gridBagConstraints);

            chkAutoLink.setForeground(new java.awt.Color(0, 51, 204));
            chkAutoLink.setSelected(true);
            chkAutoLink.setText(bundle.getString("MainPskmailUi.chkAutoLink.text_1")); // NOI18N
            chkAutoLink.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    chkAutoLinkStateChanged(evt);
                }
            });
            chkAutoLink.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    chkAutoLinkActionPerformed(evt);
                }
            });
            pnlBeacon.add(chkAutoLink, new java.awt.GridBagConstraints());

            lblAutoServer.setForeground(new java.awt.Color(0, 51, 204));
            lblAutoServer.setText(bundle.getString("MainPskmailUi.lblAutoServer.text_1")); // NOI18N
            pnlBeacon.add(lblAutoServer, new java.awt.GridBagConstraints());

            cboAPRS2nd.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "/", "\\" }));
                cboAPRS2nd.setPreferredSize(new java.awt.Dimension(35, 25));
                cboAPRS2nd.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cboAPRS2ndActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                pnlBeacon.add(cboAPRS2nd, gridBagConstraints);

                tabAPRS.add(pnlBeacon);

                tabMain.addTab(bundle.getString("MainPskmailUi.tabAPRS.TabConstraints.tabTitle_1"), tabAPRS); // NOI18N

                tabModem.setLayout(new javax.swing.BoxLayout(tabModem, javax.swing.BoxLayout.LINE_AXIS));

                pnlModemArq.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("MainPskmailUi.pnlModemArq.border.title_1"))); // NOI18N
                pnlModemArq.setMaximumSize(new java.awt.Dimension(700, 2147483647));
                pnlModemArq.setMinimumSize(new java.awt.Dimension(370, 438));
                pnlModemArq.setPreferredSize(new java.awt.Dimension(350, 438));
                pnlModemArq.setLayout(new java.awt.GridBagLayout());

                lblRXMode.setForeground(new java.awt.Color(0, 51, 204));
                lblRXMode.setText(bundle.getString("MainPskmailUi.lblRXMode.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
                pnlModemArq.add(lblRXMode, gridBagConstraints);

                RxmodeTextfield.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
                RxmodeTextfield.setText(bundle.getString("MainPskmailUi.RxmodeTextfield.text_1")); // NOI18N
                RxmodeTextfield.setMaximumSize(new java.awt.Dimension(200, 27));
                RxmodeTextfield.setMinimumSize(new java.awt.Dimension(100, 27));
                RxmodeTextfield.setPreferredSize(new java.awt.Dimension(150, 24));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
                pnlModemArq.add(RxmodeTextfield, gridBagConstraints);

                lblTXMode.setForeground(new java.awt.Color(0, 51, 204));
                lblTXMode.setText(bundle.getString("MainPskmailUi.lblTXMode.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
                pnlModemArq.add(lblTXMode, gridBagConstraints);

                TxmodeTextfield.setText(bundle.getString("MainPskmailUi.TxmodeTextfield.text_1")); // NOI18N
                TxmodeTextfield.setMaximumSize(new java.awt.Dimension(200, 27));
                TxmodeTextfield.setMinimumSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
                pnlModemArq.add(TxmodeTextfield, gridBagConstraints);

                lblRxModeQ.setForeground(new java.awt.Color(0, 51, 204));
                lblRxModeQ.setText(bundle.getString("MainPskmailUi.lblRxModeQ.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
                pnlModemArq.add(lblRxModeQ, gridBagConstraints);

                TxmodeQuality.setBackground(new java.awt.Color(255, 255, 255));
                TxmodeQuality.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                TxmodeQuality.setMinimumSize(new java.awt.Dimension(100, 20));
                TxmodeQuality.setPreferredSize(new java.awt.Dimension(100, 20));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
                pnlModemArq.add(TxmodeQuality, gridBagConstraints);

                lblTXModeQ.setForeground(new java.awt.Color(0, 51, 204));
                lblTXModeQ.setText(bundle.getString("MainPskmailUi.lblTXModeQ.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
                pnlModemArq.add(lblTXModeQ, gridBagConstraints);

                Profile.setText(bundle.getString("MainPskmailUi.Profile.text_1")); // NOI18N
                Profile.setMaximumSize(new java.awt.Dimension(200, 27));
                Profile.setMinimumSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
                pnlModemArq.add(Profile, gridBagConstraints);

                lblProfile.setForeground(new java.awt.Color(0, 51, 204));
                lblProfile.setText(bundle.getString("MainPskmailUi.lblProfile.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 5);
                pnlModemArq.add(lblProfile, gridBagConstraints);

                lblBSize.setForeground(new java.awt.Color(0, 51, 204));
                lblBSize.setText(bundle.getString("MainPskmailUi.lblBSize.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
                pnlModemArq.add(lblBSize, gridBagConstraints);

                Size_indicator.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
                Size_indicator.setText(bundle.getString("MainPskmailUi.Size_indicator.text_1")); // NOI18N
                Size_indicator.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                Size_indicator.setMaximumSize(new java.awt.Dimension(200, 27));
                Size_indicator.setMinimumSize(new java.awt.Dimension(100, 27));
                Size_indicator.setPreferredSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
                pnlModemArq.add(Size_indicator, gridBagConstraints);

                lblCPS.setForeground(new java.awt.Color(0, 51, 204));
                lblCPS.setText(bundle.getString("MainPskmailUi.lblCPS.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                pnlModemArq.add(lblCPS, gridBagConstraints);

                CPSValue.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
                CPSValue.setText(bundle.getString("MainPskmailUi.CPSValue.text_1")); // NOI18N
                CPSValue.setMaximumSize(new java.awt.Dimension(200, 27));
                CPSValue.setMinimumSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
                pnlModemArq.add(CPSValue, gridBagConstraints);

                lblTotalBytes.setForeground(new java.awt.Color(0, 51, 204));
                lblTotalBytes.setText(bundle.getString("MainPskmailUi.lblTotalBytes.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
                pnlModemArq.add(lblTotalBytes, gridBagConstraints);

                Totalbytes.setEditable(false);
                Totalbytes.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
                Totalbytes.setText(bundle.getString("MainPskmailUi.Totalbytes.text_1")); // NOI18N
                Totalbytes.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                Totalbytes.setMaximumSize(new java.awt.Dimension(200, 27));
                Totalbytes.setMinimumSize(new java.awt.Dimension(100, 27));
                Totalbytes.setPreferredSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
                pnlModemArq.add(Totalbytes, gridBagConstraints);

                lblThroughput.setForeground(new java.awt.Color(0, 51, 204));
                lblThroughput.setText(bundle.getString("MainPskmailUi.lblThroughput.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 4;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 4);
                pnlModemArq.add(lblThroughput, gridBagConstraints);

                Throughput.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
                Throughput.setText(bundle.getString("MainPskmailUi.Throughput.text_1")); // NOI18N
                Throughput.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                Throughput.setMaximumSize(new java.awt.Dimension(200, 27));
                Throughput.setMinimumSize(new java.awt.Dimension(100, 27));
                Throughput.setPreferredSize(new java.awt.Dimension(100, 27));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 4;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                pnlModemArq.add(Throughput, gridBagConstraints);

                SNRlbl.setForeground(new java.awt.Color(6, 51, 204));
                SNRlbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                SNRlbl.setText(bundle.getString("MainPskmailUi.SNRlbl.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 4;
                gridBagConstraints.ipadx = 37;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
                gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 4);
                pnlModemArq.add(SNRlbl, gridBagConstraints);

                RxmodeQuality.setBackground(new java.awt.Color(255, 255, 255));
                RxmodeQuality.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                RxmodeQuality.setMinimumSize(new java.awt.Dimension(100, 20));
                RxmodeQuality.setPreferredSize(new java.awt.Dimension(100, 20));
                RxmodeQuality.setStringPainted(true);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                pnlModemArq.add(RxmodeQuality, gridBagConstraints);

                tabModem.add(pnlModemArq);

                statistics.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.statistics.border.title_1"))); // NOI18N
                statistics.setLayout(new javax.swing.BoxLayout(statistics, javax.swing.BoxLayout.LINE_AXIS));

                jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                serverlist.setBackground(new java.awt.Color(255, 255, 230));
                serverlist.setColumns(20);
                serverlist.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
                serverlist.setRows(5);
                jScrollPane6.setViewportView(serverlist);

                statistics.add(jScrollPane6);

                tabModem.add(statistics);

                tabMain.addTab(bundle.getString("MainPskmailUi.tabModem.TabConstraints.tabTitle_1"), tabModem); // NOI18N

                tabIgate.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.tabIgate.border.title_1"))); // NOI18N
                tabIgate.setLayout(new java.awt.GridBagLayout());

                IgateCallField.setEditable(false);
                IgateCallField.setMinimumSize(new java.awt.Dimension(50, 28));
                IgateCallField.setPreferredSize(new java.awt.Dimension(60, 28));
                IgateCallField.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        IgateCallFieldActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.ipadx = 76;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
                tabIgate.add(IgateCallField, gridBagConstraints);

                IgateCall.setText(bundle.getString("MainPskmailUi.IgateCall.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
                tabIgate.add(IgateCall, gridBagConstraints);

                IgateIndicator.setText(bundle.getString("rg.netbeans.beaninfo.editors.Font")); // NOI18N
                IgateIndicator.setMaximumSize(new java.awt.Dimension(110, 24));
                IgateIndicator.setMinimumSize(new java.awt.Dimension(110, 24));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.ipadx = 168;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
                tabIgate.add(IgateIndicator, gridBagConstraints);

                IgateSwitch.setText(bundle.getString("MainPskmailUi.IgateSwitch.text_1")); // NOI18N
                IgateSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        IgateSwitchMouseClicked(evt);
                    }
                });
                IgateSwitch.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        IgateSwitchActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
                tabIgate.add(IgateSwitch, gridBagConstraints);

                IgateTextArea.setBackground(new java.awt.Color(255, 255, 230));
                IgateTextArea.setColumns(20);
                IgateTextArea.setFont(new java.awt.Font("Nimbus Mono L", 1, 12)); // NOI18N
                IgateTextArea.setRows(5);
                jScrollPane2.setViewportView(IgateTextArea);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.gridheight = 5;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.ipadx = 412;
                gridBagConstraints.ipady = 158;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 1.1;
                gridBagConstraints.weighty = 1.1;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
                tabIgate.add(jScrollPane2, gridBagConstraints);

                APRSServerSelect.setEditable(true);
                APRSServerSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "australia.aprs2.net", "austria.aprs2.net", "belgium.aprs2.net", "bern.aprs2.net", "england.aprs2.net", "euro.aprs2.net", "dl.aprs2.net", "erfurt.aprs2.net", "france.aprs2.net", "finland.aprs2.net", "italys.aprs2.net", "ontario.aprs2.net", "spain.aprs2.net", "sweden.aprs2.net", "sydney.aprs2.net", "texas.aprs2.net", "tokyo.aprs2.net", "vancouver.aprs2.net", "zurich.aprs2.net" }));
                APRSServerSelect.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        APRSServerSelectActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
                tabIgate.add(APRSServerSelect, gridBagConstraints);

                APRS_IS.setSelected(true);
                APRS_IS.setText(bundle.getString("MainPskmailUi.APRS_IS.text_1")); // NOI18N
                APRS_IS.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        APRS_ISActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
                tabIgate.add(APRS_IS, gridBagConstraints);

                lblIgateStatus.setText(bundle.getString("MainPskmailUi.lblIgateStatus.text_1")); // NOI18N
                lblIgateStatus.setMaximumSize(new java.awt.Dimension(300, 24));
                lblIgateStatus.setMinimumSize(new java.awt.Dimension(48, 24));
                lblIgateStatus.setPreferredSize(new java.awt.Dimension(160, 24));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.ipadx = 8;
                gridBagConstraints.weightx = 0.1;
                gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 23);
                tabIgate.add(lblIgateStatus, gridBagConstraints);

                tabMain.addTab(bundle.getString("MainPskmailUi.tabIgate.TabConstraints.tabTitle_1"), tabIgate); // NOI18N

                tabRigctl.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
                tabRigctl.setLayout(new java.awt.GridBagLayout());

                pnlSummoning.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.pnlSummoning.border.title_1"))); // NOI18N
                pnlSummoning.setPreferredSize(new java.awt.Dimension(720, 125));
                pnlSummoning.setLayout(new java.awt.GridBagLayout());

                labelCurrentFreq.setText(bundle.getString("MainPskmailUi.labelCurrentFreq.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.ipadx = 10;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(60, 50, 0, 0);
                pnlSummoning.add(labelCurrentFreq, gridBagConstraints);

                ClientFreqTxtfield.setText(bundle.getString("MainPskmailUi.ClientFreqTxtfield.text_1")); // NOI18N
                ClientFreqTxtfield.setMinimumSize(new java.awt.Dimension(80, 28));
                ClientFreqTxtfield.setPreferredSize(new java.awt.Dimension(82, 28));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridwidth = 4;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.ipadx = 23;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(50, 13, 0, 0);
                pnlSummoning.add(ClientFreqTxtfield, gridBagConstraints);

                labelServerFreq.setForeground(new java.awt.Color(51, 51, 52));
                labelServerFreq.setText(bundle.getString("MainPskmailUi.labelServerFreq.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.ipadx = 31;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(21, 53, 0, 0);
                pnlSummoning.add(labelServerFreq, gridBagConstraints);

                ServerfreqTxtfield.setText(bundle.getString("MainPskmailUi.ServerfreqTxtfield.text_1")); // NOI18N
                ServerfreqTxtfield.setMinimumSize(new java.awt.Dimension(80, 28));
                ServerfreqTxtfield.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        ServerfreqTxtfieldActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 3;
                gridBagConstraints.gridheight = 3;
                gridBagConstraints.ipadx = 21;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(21, 13, 0, 0);
                pnlSummoning.add(ServerfreqTxtfield, gridBagConstraints);

                SetToChannelButton.setText(bundle.getString("MainPskmailUi.SetToChannelButton.text_1")); // NOI18N
                SetToChannelButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        SetToChannelButtonActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 7;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 3;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(50, 16, 0, 0);
                pnlSummoning.add(SetToChannelButton, gridBagConstraints);

                Upbutton.setText(bundle.getString("MainPskmailUi.Upbutton.text_1")); // NOI18N
                Upbutton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        UpbuttonActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 5;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 3;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(50, 17, 0, 0);
                pnlSummoning.add(Upbutton, gridBagConstraints);

                Downbutton.setText(bundle.getString("MainPskmailUi.Downbutton.text_1")); // NOI18N
                Downbutton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        DownbuttonActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 5;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.gridheight = 4;
                gridBagConstraints.ipadx = 7;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(21, 17, 58, 0);
                pnlSummoning.add(Downbutton, gridBagConstraints);

                bSummon.setText(bundle.getString("MainPskmailUi.bSummon.text_1")); // NOI18N
                bSummon.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bSummonActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 7;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.gridheight = 4;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(21, 16, 58, 0);
                pnlSummoning.add(bSummon, gridBagConstraints);

                rigctlactivelbl.setText(bundle.getString("MainPskmailUi.rigctlactivelbl.text_1")); // NOI18N
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 9;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(60, 19, 0, 0);
                pnlSummoning.add(rigctlactivelbl, gridBagConstraints);

                modelbl.setText(bundle.getString("MainPskmailUi.modelbl.text_1")); // NOI18N
                modelbl.setFocusable(false);
                modelbl.setMaximumSize(new java.awt.Dimension(30, 18));
                modelbl.setMinimumSize(new java.awt.Dimension(30, 18));
                modelbl.setPreferredSize(new java.awt.Dimension(30, 18));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 9;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.ipadx = 85;
                gridBagConstraints.ipady = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(21, 19, 0, 245);
                pnlSummoning.add(modelbl, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 0.1;
                gridBagConstraints.weighty = 0.1;
                gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 2);
                tabRigctl.add(pnlSummoning, gridBagConstraints);

                pnlFreqs.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPskmailUi.pnlFreqs.border.title_1"))); // NOI18N
                pnlFreqs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

                ScannerCheckbox.setText(bundle.getString("MainPskmailUi.ScannerCheckbox.text_1")); // NOI18N
                ScannerCheckbox.setMaximumSize(new java.awt.Dimension(80, 24));
                ScannerCheckbox.setPreferredSize(new java.awt.Dimension(70, 24));
                ScannerCheckbox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        ScannerCheckboxActionPerformed(evt);
                    }
                });
                pnlFreqs.add(ScannerCheckbox);

                freq0.setText(bundle.getString("MainPskmailUi.freq0.text_1")); // NOI18N
                freq0.setMinimumSize(new java.awt.Dimension(74, 27));
                freq0.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        freq0MouseClicked(evt);
                    }
                });
                freq0.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        freq0ActionPerformed(evt);
                    }
                });
                pnlFreqs.add(freq0);

                freq1.setText(bundle.getString("MainPskmailUi.freq1.text_1")); // NOI18N
                freq1.setMinimumSize(new java.awt.Dimension(74, 27));
                freq1.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        freq1MouseClicked(evt);
                    }
                });
                freq1.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        freq1ActionPerformed(evt);
                    }
                });
                pnlFreqs.add(freq1);

                freq2.setText(bundle.getString("MainPskmailUi.freq2.text_1")); // NOI18N
                freq2.setMinimumSize(new java.awt.Dimension(74, 27));
                freq2.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        freq2MouseClicked(evt);
                    }
                });
                freq2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        freq2ActionPerformed(evt);
                    }
                });
                pnlFreqs.add(freq2);

                freq3.setText(bundle.getString("MainPskmailUi.freq3.text_1")); // NOI18N
                freq3.setMinimumSize(new java.awt.Dimension(74, 27));
                freq3.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        freq3MouseClicked(evt);
                    }
                });
                freq3.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        freq3ActionPerformed(evt);
                    }
                });
                pnlFreqs.add(freq3);

                freq4.setText(bundle.getString("MainPskmailUi.freq4.text_1")); // NOI18N
                freq4.setMinimumSize(new java.awt.Dimension(74, 27));
                freq4.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        freq4MouseClicked(evt);
                    }
                });
                freq4.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        freq4ActionPerformed(evt);
                    }
                });
                pnlFreqs.add(freq4);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 0.1;
                gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
                tabRigctl.add(pnlFreqs, gridBagConstraints);

                tabMain.addTab(bundle.getString("MainPskmailUi.tabRigctl.TabConstraints.tabTitle_1"), tabRigctl); // NOI18N

                tabRadioMsg.setLayout(new java.awt.BorderLayout());

                jSplitPane2.setDividerLocation(200);

                jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

                jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                jPanel4.setMaximumSize(new java.awt.Dimension(300, 50));
                jPanel4.setMinimumSize(new java.awt.Dimension(81, 50));
                jPanel4.setName("VIA Station"); // NOI18N
                jPanel4.setPreferredSize(new java.awt.Dimension(100, 50));

                bContacts1.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bContacts1.setForeground(new java.awt.Color(0, 102, 51));
                bContacts1.setText(bundle.getString("MainPskmailUi.bContacts1.text_1")); // NOI18N
                bContacts1.setMaximumSize(new java.awt.Dimension(110, 27));
                bContacts1.setMinimumSize(new java.awt.Dimension(75, 27));
                bContacts1.setPreferredSize(new java.awt.Dimension(100, 27));
                bContacts1.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bContacts1ActionPerformed(evt);
                    }
                });
                jPanel4.add(bContacts1);

                jPanel1.add(jPanel4);

                jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(mainpskmailui.getString("mainpskmailui.jPanel3Title.text"))); // NOI18N
                jPanel3.setMaximumSize(new java.awt.Dimension(300, 155));
                jPanel3.setMinimumSize(new java.awt.Dimension(81, 130));
                jPanel3.setName(""); // NOI18N
                jPanel3.setPreferredSize(new java.awt.Dimension(100, 100));

                jComboRMsgTo.setMaximumSize(new java.awt.Dimension(150, 20));
                jComboRMsgTo.setMinimumSize(new java.awt.Dimension(120, 20));
                jComboRMsgTo.setName(""); // NOI18N
                jComboRMsgTo.setPreferredSize(new java.awt.Dimension(150, 20));
                jComboRMsgTo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jComboRMsgToActionPerformed(evt);
                    }
                });
                jPanel3.add(jComboRMsgTo);

                jPanel5.setAlignmentX(0.0F);
                jPanel5.setMaximumSize(new java.awt.Dimension(32767, 70));
                jPanel5.setMinimumSize(new java.awt.Dimension(10, 50));
                jPanel5.setOpaque(false);
                jPanel5.setPreferredSize(new java.awt.Dimension(274, 50));
                jPanel5.setLayout(new java.awt.GridBagLayout());

                buttonGroupAlias.add(jRadBtnAliasOnly);
                jRadBtnAliasOnly.setText(mainpskmailui.getString("MainPskmailUi.jRadBtnAliasOnly.text_1")); // NOI18N
                jRadBtnAliasOnly.setToolTipText(mainpskmailui.getString("mainpskmailui.bRMsgjRadBtnAliasOnly.toolTipText")); // NOI18N
                jRadBtnAliasOnly.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jRadBtnAliasOnly.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadBtnAliasOnlyActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 43, 0, 0);
                jPanel5.add(jRadBtnAliasOnly, gridBagConstraints);

                buttonGroupAlias.add(jRadBtnAliasAndAddress);
                jRadBtnAliasAndAddress.setText(mainpskmailui.getString("mainpskmailui.text")); // NOI18N
                jRadBtnAliasAndAddress.setToolTipText(mainpskmailui.getString("mainpskmailui.bRMsgjRadBtnAliasAndAddress.toolTipText")); // NOI18N
                jRadBtnAliasAndAddress.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                jRadBtnAliasAndAddress.setMinimumSize(new java.awt.Dimension(100, 23));
                jRadBtnAliasAndAddress.setName(""); // NOI18N
                jRadBtnAliasAndAddress.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadBtnAliasAndAddressActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(4, 43, 0, 62);
                jPanel5.add(jRadBtnAliasAndAddress, gridBagConstraints);

                jPanel3.add(jPanel5);

                jPanel1.add(jPanel3);
                jPanel3.getAccessibleContext().setAccessibleName(mainpskmailui.getString("SENDTO")); // NOI18N

                jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(mainpskmailui.getString("mainpskmailui.iPanel2Title.text"))); // NOI18N
                jPanel2.setMaximumSize(new java.awt.Dimension(300, 90));
                jPanel2.setMinimumSize(new java.awt.Dimension(81, 80));
                jPanel2.setName("VIA Station"); // NOI18N
                jPanel2.setPreferredSize(new java.awt.Dimension(100, 30));

                jComboRMsgVia.setMaximumSize(new java.awt.Dimension(150, 20));
                jComboRMsgVia.setMinimumSize(new java.awt.Dimension(120, 20));
                jComboRMsgVia.setPreferredSize(new java.awt.Dimension(150, 20));
                jComboRMsgVia.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jComboRMsgViaActionPerformed(evt);
                    }
                });
                jPanel2.add(jComboRMsgVia);

                jPanel1.add(jPanel2);
                jPanel2.getAccessibleContext().setAccessibleName(mainpskmailui.getString("MainPskmailUi.jPanel2.AccessibleContext.accessibleName_1")); // NOI18N
                jPanel2.getAccessibleContext().setAccessibleDescription(mainpskmailui.getString("MainPskmailUi.jPanel2.AccessibleContext.accessibleDescription_1")); // NOI18N

                jSplitPane2.setLeftComponent(jPanel1);

                scrRadioMessages.setPreferredSize(new java.awt.Dimension(1000, 500));

                tblRadioMsgs.setBackground(new java.awt.Color(255, 255, 230));
                tblRadioMsgs.setModel(new javax.swing.table.DefaultTableModel(
                    new Object [][] {

                    },
                    new String [] {
                        "Message"
                    }
                ) {
                    boolean[] canEdit = new boolean [] {
                        false
                    };

                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
                    }
                });
                tblRadioMsgs.setMaximumSize(new java.awt.Dimension(3000, 0));
                tblRadioMsgs.setMinimumSize(new java.awt.Dimension(0, 0));
                tblRadioMsgs.setSelectionBackground(new java.awt.Color(150, 150, 150));
                tblRadioMsgs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                tblRadioMsgs.getTableHeader().setReorderingAllowed(false);
                tblRadioMsgs.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblRadioMsgsMouseClicked(evt);
                    }
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        tblRadioMsgsMousePressed(evt);
                    }
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        tblRadioMsgsMouseReleased(evt);
                    }
                });
                scrRadioMessages.setViewportView(tblRadioMsgs);
                if (tblRadioMsgs.getColumnModel().getColumnCount() > 0) {
                    tblRadioMsgs.getColumnModel().getColumn(0).setMinWidth(80);
                    tblRadioMsgs.getColumnModel().getColumn(0).setPreferredWidth(80);
                    tblRadioMsgs.getColumnModel().getColumn(0).setHeaderValue(mainpskmailui.getString("mainpskmailui.tblRadioMsgs.columnModel.title1")); // NOI18N
                }

                jSplitPane2.setRightComponent(scrRadioMessages);

                tabRadioMsg.add(jSplitPane2, java.awt.BorderLayout.CENTER);

                pnlRMSgButtons.setMaximumSize(new java.awt.Dimension(32767, 40));
                pnlRMSgButtons.setMinimumSize(new java.awt.Dimension(0, 30));
                pnlRMSgButtons.setPreferredSize(new java.awt.Dimension(717, 35));
                pnlRMSgButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

                bRMsgSendSMS.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgSendSMS.setForeground(new java.awt.Color(0, 102, 51));
                bRMsgSendSMS.setText(bundle.getString("MainPskmailUi.bRMsgSendSMS.text_1")); // NOI18N
                bRMsgSendSMS.setToolTipText(bundle.getString("MainPskmailUi.bRMsgSendSMS.toolTipText_1")); // NOI18N
                bRMsgSendSMS.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgSendSMS.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgSendSMS.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgSendSMS.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgSendSMS.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgSendSMSActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgSendSMS);

                bRMsgSendInquire.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgSendInquire.setForeground(new java.awt.Color(0, 102, 51));
                bRMsgSendInquire.setText(bundle.getString("MainPskmailUi.bRMsgSendInquire.text_1")); // NOI18N
                bRMsgSendInquire.setToolTipText(bundle.getString("MainPskmailUi.bRMsgSendInquire.toolTipText_1")); // NOI18N
                bRMsgSendInquire.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgSendInquire.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgSendInquire.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgSendInquire.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgSendInquire.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgSendInquireActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgSendInquire);

                bRMsgSendPos.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgSendPos.setForeground(new java.awt.Color(0, 102, 51));
                bRMsgSendPos.setText(bundle.getString("MainPskmailUi.bRMsgSendPos.text_1")); // NOI18N
                bRMsgSendPos.setToolTipText(mainpskmailui.getString("MainPskmailUi.bRMsgSendPos.toolTipText_1")); // NOI18N
                bRMsgSendPos.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgSendPos.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgSendPos.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgSendPos.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgSendPos.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgSendPosActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgSendPos);

                bRMsgReqPos.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgReqPos.setForeground(new java.awt.Color(0, 102, 51));
                bRMsgReqPos.setText(bundle.getString("MainPskmailUi.bRMsgReqPos.text_1")); // NOI18N
                bRMsgReqPos.setToolTipText(bundle.getString("MainPskmailUi.bRMsgReqPos.toolTipText_1")); // NOI18N
                bRMsgReqPos.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgReqPos.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgReqPos.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgReqPos.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgReqPos.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgReqPosActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgReqPos);

                bRMsgResendLast.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgResendLast.setForeground(new java.awt.Color(12, 134, 40));
                bRMsgResendLast.setText(bundle.getString("MainPskmailUi.bRMsgResendLast.text_1")); // NOI18N
                bRMsgResendLast.setToolTipText(mainpskmailui.getString("MainPskmailUi.bRMsgResendLast.toolTipText_1")); // NOI18N
                bRMsgResendLast.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgResendLast.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgResendLast.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgResendLast.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgResendLast.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgResendLastActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgResendLast);

                bRMsgResend.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgResend.setForeground(new java.awt.Color(12, 134, 40));
                bRMsgResend.setText(bundle.getString("MainPskmailUi.bRMsgResend.text_1")); // NOI18N
                bRMsgResend.setToolTipText(mainpskmailui.getString("mainpskmailui.bRMsgResendDialog.toolTipText")); // NOI18N
                bRMsgResend.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgResend.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgResend.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgResend.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgResend.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgResendActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgResend);

                serverControl.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                serverControl.setForeground(new java.awt.Color(12, 134, 40));
                serverControl.setText(mainpskmailui.getString("MainPskmailUi.serverControl.text_1")); // NOI18N
                serverControl.setToolTipText(mainpskmailui.getString("MainPskmailUi.serverControl.tooltip")); // NOI18N
                serverControl.setMargin(new java.awt.Insets(2, 2, 2, 2));
                serverControl.setMaximumSize(new java.awt.Dimension(110, 25));
                serverControl.setMinimumSize(new java.awt.Dimension(75, 25));
                serverControl.setPreferredSize(new java.awt.Dimension(75, 25));
                serverControl.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        serverControlActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(serverControl);

                bRMsgManageMsg.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
                bRMsgManageMsg.setForeground(new java.awt.Color(0, 102, 51));
                bRMsgManageMsg.setText(bundle.getString("MainPskmailUi.bRMsgManageMsg.text_1")); // NOI18N
                bRMsgManageMsg.setToolTipText(bundle.getString("MainPskmailUi.bRMsgManageMsg.toolTipText_1")); // NOI18N
                bRMsgManageMsg.setMargin(new java.awt.Insets(2, 2, 2, 2));
                bRMsgManageMsg.setMaximumSize(new java.awt.Dimension(110, 25));
                bRMsgManageMsg.setMinimumSize(new java.awt.Dimension(75, 25));
                bRMsgManageMsg.setPreferredSize(new java.awt.Dimension(75, 25));
                bRMsgManageMsg.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        bRMsgManageMsgActionPerformed(evt);
                    }
                });
                pnlRMSgButtons.add(bRMsgManageMsg);

                tabRadioMsg.add(pnlRMSgButtons, java.awt.BorderLayout.PAGE_START);

                tabMain.addTab(mainpskmailui.getString("MainPskmailUi.tabRadioMsg.TabConstraints.tabTitle_1"), tabRadioMsg); // NOI18N

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 0.7;
                gridBagConstraints.weighty = 0.7;
                gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
                getContentPane().add(tabMain, gridBagConstraints);
                tabMain.setEnabledAt(7, true);

                pnlStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
                pnlStatus.setMaximumSize(new java.awt.Dimension(2000, 30));
                pnlStatus.setMinimumSize(new java.awt.Dimension(400, 30));
                pnlStatus.setPreferredSize(new java.awt.Dimension(710, 30));
                pnlStatus.setLayout(new java.awt.GridBagLayout());

                snLabel.setForeground(new java.awt.Color(7, 177, 32));
                snLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
                pnlStatus.add(snLabel, gridBagConstraints);

                StatusLabel.setBackground(new java.awt.Color(254, 254, 249));
                StatusLabel.setMaximumSize(new java.awt.Dimension(300, 25));
                StatusLabel.setMinimumSize(new java.awt.Dimension(250, 20));
                StatusLabel.setPreferredSize(new java.awt.Dimension(300, 20));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.ipadx = 20;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 0.1;
                gridBagConstraints.insets = new java.awt.Insets(0, 3, 5, 1);
                pnlStatus.add(StatusLabel, gridBagConstraints);

                cboServer.setEditable(true);
                cboServer.setMinimumSize(new java.awt.Dimension(150, 27));
                cboServer.setPreferredSize(new java.awt.Dimension(150, 27));
                cboServer.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        cboServerFocusLost(evt);
                    }
                });
                cboServer.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cboServerActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.ipadx = -60;
                gridBagConstraints.ipady = -7;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(2, 9, 4, 1);
                pnlStatus.add(cboServer, gridBagConstraints);
                cboServer.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent event) {
                        cboServerProcessKeyReleased(event);
                    }
                });

                spnMinute.setModel(new SpinnerNumberModel(0,0,4,1));
                spnMinute.setToolTipText(mainpskmailui.getString("During_what_minute_(0-4)_will_the_server_listen_and_the_client_transmit?")); // NOI18N
                spnMinute.setPreferredSize(new java.awt.Dimension(28, 29));
                spnMinute.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent evt) {
                        spnMinuteStateChanged(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 4;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.ipady = -4;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(3, 10, 5, 0);
                pnlStatus.add(spnMinute, gridBagConstraints);

                ProgressBar.setBackground(new java.awt.Color(255, 255, 255));
                ProgressBar.setMaximumSize(new java.awt.Dimension(120, 20));
                ProgressBar.setMinimumSize(new java.awt.Dimension(100, 20));
                ProgressBar.setPreferredSize(new java.awt.Dimension(100, 20));
                ProgressBar.setRequestFocusEnabled(false);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 0;
                pnlStatus.add(ProgressBar, gridBagConstraints);

                jTextField1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
                jTextField1.setForeground(new java.awt.Color(47, 120, 93));
                jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
                jTextField1.setMaximumSize(new java.awt.Dimension(100, 26));
                jTextField1.setMinimumSize(new java.awt.Dimension(100, 28));
                jTextField1.setPreferredSize(new java.awt.Dimension(100, 27));
                jTextField1.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jTextField1ActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 5;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.ipadx = -20;
                gridBagConstraints.ipady = -7;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(2, 9, 0, 0);
                pnlStatus.add(jTextField1, gridBagConstraints);

                linkstatus.setBackground(new java.awt.Color(204, 204, 204));
                linkstatus.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                linkstatus.setMinimumSize(new java.awt.Dimension(15, 15));
                linkstatus.setPreferredSize(new java.awt.Dimension(15, 15));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 6;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
                gridBagConstraints.insets = new java.awt.Insets(3, 10, 7, 0);
                pnlStatus.add(linkstatus, gridBagConstraints);

                RXlabel.setBackground(new java.awt.Color(255, 255, 255));
                RXlabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
                RXlabel.setForeground(new java.awt.Color(37, 109, 87));
                RXlabel.setText(bundle.getString("MainPskmailUi.RXlabel.text_1")); // NOI18N
                RXlabel.setMaximumSize(new java.awt.Dimension(120, 14));
                RXlabel.setMinimumSize(new java.awt.Dimension(70, 14));
                RXlabel.setPreferredSize(new java.awt.Dimension(60, 15));
                RXlabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.ipadx = 5;
                gridBagConstraints.ipady = 7;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(2, 11, 0, 0);
                pnlStatus.add(RXlabel, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                getContentPane().add(pnlStatus, gridBagConstraints);

                pnlMainEntry.setMaximumSize(new java.awt.Dimension(1400, 900));
                pnlMainEntry.setMinimumSize(new java.awt.Dimension(500, 149));
                pnlMainEntry.setPreferredSize(new java.awt.Dimension(500, 149));
                pnlMainEntry.setLayout(new java.awt.GridBagLayout());

                txtMainEntry.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, new java.awt.Color(0, 102, 102)));
                txtMainEntry.setMaximumSize(new java.awt.Dimension(1400, 27));
                txtMainEntry.setMinimumSize(new java.awt.Dimension(380, 27));
                txtMainEntry.setPreferredSize(new java.awt.Dimension(380, 27));
                txtMainEntry.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        txtMainEntryMouseClicked(evt);
                    }
                });
                txtMainEntry.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        txtMainEntryActionPerformed(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.ipadx = 180;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(5, 10, 3, 0);
                pnlMainEntry.add(txtMainEntry, gridBagConstraints);

                lblStatus.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
                lblStatus.setForeground(new java.awt.Color(153, 153, 153));
                lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblStatus.setText("Listening"); // NOI18N
                lblStatus.setMaximumSize(new java.awt.Dimension(120, 17));
                lblStatus.setMinimumSize(new java.awt.Dimension(95, 17));
                lblStatus.setPreferredSize(new java.awt.Dimension(100, 17));
                lblStatus.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        lblStatusMouseClicked(evt);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.ipadx = 5;
                gridBagConstraints.ipady = 4;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.insets = new java.awt.Insets(6, 15, 0, 0);
                pnlMainEntry.add(lblStatus, gridBagConstraints);

                pnlStatusIndicator.setBackground(new java.awt.Color(255, 255, 255));
                pnlStatusIndicator.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
                pnlStatusIndicator.setMaximumSize(new java.awt.Dimension(15, 15));
                pnlStatusIndicator.setMinimumSize(new java.awt.Dimension(15, 15));
                pnlStatusIndicator.setPreferredSize(new java.awt.Dimension(15, 27));
                pnlStatusIndicator.setLayout(new java.awt.GridBagLayout());
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.insets = new java.awt.Insets(8, 7, 4, 1);
                pnlMainEntry.add(pnlStatusIndicator, gridBagConstraints);

                jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                txtLinkMonitor.setBackground(new java.awt.Color(220, 235, 207));
                txtLinkMonitor.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                txtLinkMonitor.setDocument(monitordoc);
                txtLinkMonitor.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
                txtLinkMonitor.setMaximumSize(new java.awt.Dimension(1400, 100));
                txtLinkMonitor.setMinimumSize(new java.awt.Dimension(400, 1400));
                jScrollPane3.setViewportView(txtLinkMonitor);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridwidth = 4;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.ipadx = 696;
                gridBagConstraints.ipady = 36;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 2);
                pnlMainEntry.add(jScrollPane3, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 0.5;
                gridBagConstraints.weighty = 0.5;
                getContentPane().add(pnlMainEntry, gridBagConstraints);

                jMenuBar3.setMaximumSize(new java.awt.Dimension(1024, 32769));
                jMenuBar3.setName(""); // NOI18N

                mnuFile2.setText(bundle.getString("MainPskmailUi.mnuFile2.text_1")); // NOI18N

                mnuConnection.setText(bundle.getString("MainPskmailUi.mnuConnection.text_1")); // NOI18N

                Conn_connect.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_DOWN_MASK));
                Conn_connect.setText(bundle.getString("MainPskmailUi.Conn_connect.text_1")); // NOI18N
                Conn_connect.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Conn_connectActionPerformed(evt);
                    }
                });
                mnuConnection.add(Conn_connect);

                Conn_abort.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_DOWN_MASK));
                Conn_abort.setText(bundle.getString("MainPskmailUi.Conn_abort.text_1")); // NOI18N
                Conn_abort.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Conn_abortActionPerformed(evt);
                    }
                });
                mnuConnection.add(Conn_abort);
                mnuConnection.add(Conn_send);

                mnuFile2.add(mnuConnection);
                mnuFile2.add(jSeparator9);

                mnuClear2.setText(bundle.getString("MainPskmailUi.mnuClear2.text_1_1")); // NOI18N

                mnuHeaders2.setText(bundle.getString("MainPskmailUi.mnuHeaders2.text_1")); // NOI18N
                mnuHeaders2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuHeadersActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuHeaders2);

                mnuBulletins2.setText(bundle.getString("MainPskmailUi.mnuBulletins2.text_1")); // NOI18N
                mnuBulletins2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuBulletinsActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuBulletins2);

                mnuUploads.setText(bundle.getString("MainPskmailUi.mnuUploads.text_1")); // NOI18N
                mnuUploads.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuUploadsActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuUploads);

                mnuDownloads.setText(bundle.getString("MainPskmailUi.mnuDownloads.text_1")); // NOI18N
                mnuDownloads.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuDownloadsActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuDownloads);

                mnuClearInbox.setText(bundle.getString("MainPskmailUi.mnuClearInbox.text_1")); // NOI18N
                mnuClearInbox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuClearInboxActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuClearInbox);

                mnuClearOutbox.setText(bundle.getString("MainPskmailUi.mnuClearOutbox.text_1")); // NOI18N
                mnuClearOutbox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuClearOutboxActionPerformed(evt);
                    }
                });
                mnuClear2.add(mnuClearOutbox);

                mnuFile2.add(mnuClear2);

                mnuFileList.setText(bundle.getString("MainPskmailUi.mnuFileList.text_1")); // NOI18N
                mnuFileList.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuFileListActionPerformed(evt);
                    }
                });
                mnuFile2.add(mnuFileList);

                jSeparator8.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
                mnuFile2.add(jSeparator8);

                buttonGroupPartialDownloads.add(jRadioButtonAccept);
                jRadioButtonAccept.setSelected(true);
                jRadioButtonAccept.setText(bundle.getString("MainPskmailUi.jRadioButtonAccept.text_1")); // NOI18N
                jRadioButtonAccept.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadioButtonAcceptActionPerformed(evt);
                    }
                });
                mnuFile2.add(jRadioButtonAccept);

                buttonGroupPartialDownloads.add(jRadioButtonReject);
                jRadioButtonReject.setText(bundle.getString("MainPskmailUi.jRadioButtonReject.text_1")); // NOI18N
                mnuFile2.add(jRadioButtonReject);

                buttonGroupPartialDownloads.add(jRadioButtonDelete);
                jRadioButtonDelete.setText(bundle.getString("MainPskmailUi.jRadioButtonDelete.text_1")); // NOI18N
                mnuFile2.add(jRadioButtonDelete);
                mnuFile2.add(jSeparator5);

                mnuQuit2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_DOWN_MASK));
                mnuQuit2.setText(bundle.getString("MainPskmailUi.mnuQuit2.text_1")); // NOI18N
                mnuQuit2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuQuitActionPerformed(evt);
                    }
                });
                mnuFile2.add(mnuQuit2);
                mnuFile2.add(jSeparator10);

                jMenuBar3.add(mnuFile2);

                mnuMbox2.setText(bundle.getString("MainPskmailUi.mnuMbox2.text_1")); // NOI18N

                mnuMboxList2.setText(bundle.getString("MainPskmailUi.mnuMboxList2.text_1")); // NOI18N
                mnuMboxList2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMboxListActionPerformed(evt);
                    }
                });
                mnuMbox2.add(mnuMboxList2);

                mnuMboxRead2.setText(bundle.getString("MainPskmailUi.mnuMboxRead2.text_1")); // NOI18N
                mnuMboxRead2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMboxReadActionPerformed(evt);
                    }
                });
                mnuMbox2.add(mnuMboxRead2);

                mnuMboxDelete2.setText(bundle.getString("MainPskmailUi.mnuMboxDelete2.text_1")); // NOI18N
                mnuMboxDelete2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMboxDeleteActionPerformed(evt);
                    }
                });
                mnuMbox2.add(mnuMboxDelete2);

                jMenuBar3.add(mnuMbox2);

                mnuMode2.setText(bundle.getString("MainPskmailUi.mnuMode2.text_1")); // NOI18N
                modemnubuttons.add(mnuMode2);

                aprsscanmonitorgroup.add(mnuMailAPRS2);
                mnuMailAPRS2.setSelected(true);
                mnuMailAPRS2.setText(bundle.getString("MainPskmailUi.mnuMailAPRS2.text_1")); // NOI18N
                mnuMailAPRS2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMailAPRS2ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuMailAPRS2);

                aprsscanmonitorgroup.add(mnuMailScanning);
                mnuMailScanning.setText(bundle.getString("MainPskmailUi.mnuMailScanning.text_1")); // NOI18N
                mnuMailScanning.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMailScanningActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuMailScanning);
                mnuMode2.add(jSeparator6);

                mnuMonitor.setText(bundle.getString("MainPskmailUi.mnuMonitor.text_1_1")); // NOI18N
                mnuMonitor.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMonitorActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuMonitor);
                mnuMode2.add(jSeparator4);

                mnuModeQSY2.setText(bundle.getString("MainPskmailUi.mnuModeQSY2.text_1")); // NOI18N
                mnuModeQSY2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuModeQSYActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuModeQSY2);
                mnuMode2.add(jSeparator1);

                modemnubuttons.add(mnuPSK63);
                mnuPSK63.setText("PSK63"); // NOI18N
                mnuPSK63.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK63ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK63);

                modemnubuttons.add(mnuPSK125);
                mnuPSK125.setText("PSK125"); // NOI18N
                mnuPSK125.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK125ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK125);

                modemnubuttons.add(mnuPSK250);
                mnuPSK250.setText("PSK250"); // NOI18N
                mnuPSK250.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK250ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK250);

                modemnubuttons.add(mnuPSK500);
                mnuPSK500.setText(bundle.getString("MainPskmailUi.mnuPSK500.text_1")); // NOI18N
                mnuPSK500.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK500ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK500);
                mnuMode2.add(jSeparator2);

                modemnubuttons.add(mnuPSK125R);
                mnuPSK125R.setText(bundle.getString("MainPskmailUi.mnuPSK125R.text_1")); // NOI18N
                mnuPSK125R.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK125RActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK125R);

                modemnubuttons.add(mnuPSK250R);
                mnuPSK250R.setText(bundle.getString("MainPskmailUi.mnuPSK250R.text_1")); // NOI18N
                mnuPSK250R.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK250RActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK250R);

                modemnubuttons.add(mnuPSK500R);
                mnuPSK500R.setText(bundle.getString("MainPskmailUi.mnuPSK500R.text_1_1")); // NOI18N
                mnuPSK500R.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPSK500RActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuPSK500R);
                mnuMode2.add(jSeparator3);

                modemnubuttons.add(mnuTHOR8);
                mnuTHOR8.setText(bundle.getString("MainPskmailUi.mnuTHOR8.text_1")); // NOI18N
                mnuTHOR8.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadioButtonMenuItemTHOR8ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuTHOR8);

                modemnubuttons.add(mnuTHOR22);
                mnuTHOR22.setText("THOR22"); // NOI18N
                mnuTHOR22.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuTHOR22ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuTHOR22);
                mnuMode2.add(jSeparator7);

                modemnubuttons.add(mnuMFSK16);
                mnuMFSK16.setText(bundle.getString("MainPskmailUi.mnuMFSK16.text_1")); // NOI18N
                mnuMFSK16.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadioButtonMenuItemMFSK16ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuMFSK16);

                modemnubuttons.add(mnuMFSK32);
                mnuMFSK32.setText(bundle.getString("MainPskmailUi.mnuMFSK32.text_1_1")); // NOI18N
                mnuMFSK32.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMFSK32ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuMFSK32);
                mnuMode2.add(jSeparator11);

                modemnubuttons.add(mnuDOMINOEX5);
                mnuDOMINOEX5.setText(bundle.getString("MainPskmailUi.mnuDOMINOEX5.text_1")); // NOI18N
                mnuDOMINOEX5.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadioButtonMenuItemTHOR11ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuDOMINOEX5);

                modemnubuttons.add(mnuDOMINOEX11);
                mnuDOMINOEX11.setText(bundle.getString("MainPskmailUi.mnuDOMINOEX11.text_1")); // NOI18N
                mnuDOMINOEX11.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuDOMINOEX11ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuDOMINOEX11);

                modemnubuttons.add(mnuDOMINOEX22);
                mnuDOMINOEX22.setText(bundle.getString("MainPskmailUi.mnuDOMINOEX22.text_1")); // NOI18N
                mnuDOMINOEX22.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuDOMINOEX22ActionPerformed(evt);
                    }
                });
                mnuMode2.add(mnuDOMINOEX22);

                defaultmnu.setText(bundle.getString("MainPskmailUi.defaultmnu.text_1")); // NOI18N
                defaultmnu.setEnabled(false);
                defaultmnu.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jRadioButtonMenuItemMFSK22ActionPerformed(evt);
                    }
                });
                mnuMode2.add(defaultmnu);

                jMenuBar3.add(mnuMode2);

                jMenu1.setText(bundle.getString("MainPskmailUi.jMenu1.text_1")); // NOI18N

                Twitter_send.setText(bundle.getString("MainPskmailUi.Twitter_send.text_1")); // NOI18N
                Twitter_send.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Twitter_sendActionPerformed(evt);
                    }
                });
                jMenu1.add(Twitter_send);

                GetUpdatesmenuItem.setText(bundle.getString("MainPskmailUi.GetUpdatesmenuItem.text_1")); // NOI18N
                GetUpdatesmenuItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        GetUpdatesmenuItemActionPerformed(evt);
                    }
                });
                jMenu1.add(GetUpdatesmenuItem);

                jMenuBar3.add(jMenu1);

                mnuIACcodes.setText(bundle.getString("MainPskmailUi.mnuIACcodes.text_1")); // NOI18N

                menuMessages.setText(mainpskmailui.getString("Get_Messages")); // NOI18N
                menuMessages.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        menuMessagesActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(menuMessages);

                mnuGetTidestations2.setText(bundle.getString("MainPskmailUi.mnuGetTidestations2.text_1")); // NOI18N
                mnuGetTidestations2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetTidestationsActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetTidestations2);

                mnuGetTide2.setText(bundle.getString("MainPskmailUi.mnuGetTide2.text_1")); // NOI18N
                mnuGetTide2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetTideActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetTide2);

                GetGrib.setText(mainpskmailui.getString("Get_Grib_file")); // NOI18N
                GetGrib.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        GetGribActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(GetGrib);

                jGetIAC.setText(bundle.getString("MainPskmailUi.jGetIAC.text_1")); // NOI18N
                jGetIAC.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jGetIACActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(jGetIAC);

                Getforecast.setText(bundle.getString("MainPskmailUi.Getforecast.text_1")); // NOI18N
                Getforecast.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        GetforecastActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(Getforecast);

                WWV_menu_item.setText(bundle.getString("MainPskmailUi.WWV_menu_item.text_1")); // NOI18N
                WWV_menu_item.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        WWV_menu_itemActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(WWV_menu_item);

                mnuGetAPRS2.setText(bundle.getString("MainPskmailUi.mnuGetAPRS2.text_1")); // NOI18N
                mnuGetAPRS2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetAPRSActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetAPRS2);

                mnuGetServerfq2.setText(bundle.getString("MainPskmailUi.mnuGetServerfq2.text_1")); // NOI18N
                mnuGetServerfq2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetServerfqActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetServerfq2);

                mnuGetPskmailNews2.setText(bundle.getString("MainPskmailUi.mnuGetPskmailNews2.text_1")); // NOI18N
                mnuGetPskmailNews2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetPskmailNewsActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetPskmailNews2);

                mnuGetWebPages2.setText(bundle.getString("MainPskmailUi.mnuGetWebPages2.text_1")); // NOI18N
                mnuGetWebPages2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuGetWebPagesActionPerformed(evt);
                    }
                });
                mnuIACcodes.add(mnuGetWebPages2);

                jMenuBar3.add(mnuIACcodes);

                mnuLink.setText(bundle.getString("MainPskmailUi.mnuLink.text_1")); // NOI18N
                mnuLink.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuLinkActionPerformed(evt);
                    }
                });

                Ping_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Ping_menu_item.setText(bundle.getString("MainPskmailUi.Ping_menu_item.text_1")); // NOI18N
                Ping_menu_item.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Ping_menu_itemActionPerformed(evt);
                    }
                });
                mnuLink.add(Ping_menu_item);

                menuInquire.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                menuInquire.setText(bundle.getString("MainPskmailUi.menuInquire.text_1")); // NOI18N
                menuInquire.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        menuInquireActionPerformed(evt);
                    }
                });
                mnuLink.add(menuInquire);

                timeSyncMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                timeSyncMenuItem.setText(mainpskmailui.getString("MainPskmailUi.TimeSync.MenuItem.text")); // NOI18N
                timeSyncMenuItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        timeSyncMenuItemActionPerformed(evt);
                    }
                });
                mnuLink.add(timeSyncMenuItem);

                jMenuQuality.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                jMenuQuality.setText(bundle.getString("MainPskmailUi.jMenuQuality.text_1")); // NOI18N
                jMenuQuality.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jMenuQualityActionPerformed(evt);
                    }
                });
                mnuLink.add(jMenuQuality);

                Link_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Link_menu_item.setText(bundle.getString("MainPskmailUi.Link_menu_item.text_1")); // NOI18N
                Link_menu_item.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Link_menu_itemActionPerformed(evt);
                    }
                });
                mnuLink.add(Link_menu_item);

                Beacon_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Beacon_menu_item.setText(bundle.getString("MainPskmailUi.Beacon_menu_item.text_1")); // NOI18N
                Beacon_menu_item.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Beacon_menu_itemActionPerformed(evt);
                    }
                });
                mnuLink.add(Beacon_menu_item);

                MnuTelnet.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                MnuTelnet.setText(bundle.getString("MainPskmailUi.MnuTelnet.text_1")); // NOI18N
                MnuTelnet.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        MnuTelnetActionPerformed(evt);
                    }
                });
                mnuLink.add(MnuTelnet);

                mnuMulticast.setText(bundle.getString("MainPskmailUi.mnuMulticast.text_1")); // NOI18N
                mnuMulticast.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuMulticastActionPerformed(evt);
                    }
                });
                mnuLink.add(mnuMulticast);

                Update_server.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Update_server.setText(bundle.getString("MainPskmailUi.Update_server.text_1")); // NOI18N
                Update_server.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Update_serverActionPerformed(evt);
                    }
                });
                mnuLink.add(Update_server);

                Resetrecord_mnu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Resetrecord_mnu.setText(bundle.getString("MainPskmailUi.Resetrecord_mnu.text_1")); // NOI18N
                Resetrecord_mnu.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Resetrecord_mnuActionPerformed(evt);
                    }
                });
                mnuLink.add(Resetrecord_mnu);

                Stoptransaction_mnu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                Stoptransaction_mnu.setText(bundle.getString("MainPskmailUi.Stoptransaction_mnu.text_1")); // NOI18N
                Stoptransaction_mnu.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Stoptransaction_mnuActionPerformed(evt);
                    }
                });
                mnuLink.add(Stoptransaction_mnu);

                jMenuBar3.add(mnuLink);

                mnuPrefsMain.setText(bundle.getString("MainPskmailUi.mnuPrefsMain.text_1")); // NOI18N

                mnuFqHelp.setText(bundle.getString("MainPskmailUi.mnuFqHelp.text_1")); // NOI18N
                mnuFqHelp.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuFqHelpActionPerformed(evt);
                    }
                });
                mnuPrefsMain.add(mnuFqHelp);

                mnuPreferences2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                mnuPreferences2.setText(bundle.getString("MainPskmailUi.mnuPreferences2.text_1")); // NOI18N
                mnuPreferences2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuPreferencesActionPerformed(evt);
                    }
                });
                mnuPrefsMain.add(mnuPreferences2);

                PrefSaveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                PrefSaveMenu.setText(bundle.getString("MainPskmailUi.PrefSaveMenu.text_1")); // NOI18N
                PrefSaveMenu.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        PrefSaveMenuActionPerformed(evt);
                    }
                });
                mnuPrefsMain.add(PrefSaveMenu);

                jMenuBar3.add(mnuPrefsMain);

                mnuHelpMain2.setText(bundle.getString("MainPskmailUi.mnuHelpMain2.text_1")); // NOI18N

                mnuAbout2.setText(bundle.getString("MainPskmailUi.mnuAbout2.text_1")); // NOI18N
                mnuAbout2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mnuAbout2ActionPerformed(evt);
                    }
                });
                mnuHelpMain2.add(mnuAbout2);

                jMenuBar3.add(mnuHelpMain2);

                setJMenuBar(jMenuBar3);

                getAccessibleContext().setAccessibleName("javaPskMail"); // NOI18N

                pack();
            }// </editor-fold>//GEN-END:initComponents

private void mnuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuQuitActionPerformed
    Main.m.Sendln("<cmd>normal</cmd>\n");

    Main.exitingSoon = true;
    Main.m.killFldigi();

    try {
        if (Main.m.pout != null) {
            Main.m.pout.close();
        }
        if (Main.m.in != null) {
            Main.m.in.close();
        }
    } catch (IOException ex) {
        Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NullPointerException np) {
        Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, np);
    }
    dispose();
    System.exit(0); //calling the method is a must

}//GEN-LAST:event_mnuQuitActionPerformed


private void mnuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPreferencesActionPerformed
// TODO add your handling code here:

    try {
        ModemModesEnum lastTxModem = Main.txModem;
        String lastServer = Main.q.getServer();
        optionsDialog = new OptionsDialog(this, true);

        optionsDialog.setCallsign(Main.configuration.getPreference("CALL"));
        optionsDialog.setServer(Main.configuration.getPreference("SERVER"));
        optionsDialog.setBeaconqrg(Main.configuration.getPreference("BEACONQRG"));

        // Center screen
        optionsDialog.setLocationRelativeTo(null);
        optionsDialog.setVisible(true);
        // Options have now closed
        Main.configuration.setCallsign(optionsDialog.getCallsign());
        Main.q.setCallsign(optionsDialog.getCallsign());
        IgateCallField.setText(Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER")));
        //String myServer = optionsDialog.getServer();
        //VK2ETA: Not here, only use the cboServer drop box on main UI
        //Main.q.setServer(myServer);
        //Re-load the server list in case it changed
        Main.loadServerList();
        this.cboServer.removeAllItems();
        // Add servers from main
        boolean foundLastServer = false;
        for (int i = 0; i < Main.serversArray.length; i++) {
            if (!Main.serversArray[i].equals("")) {
                cboServer.addItem(Main.serversArray[i]);
                if (lastServer.equals(Main.serversArray[i])) {
                    this.cboServer.setSelectedItem(lastServer);
                    foundLastServer = true;
                    break;
                }
            }
        }
        if (!foundLastServer) {
            this.cboServer.setSelectedItem(Main.serversArray[0]); //Try the first one if any
        }        //this.txtServer.setText(myServer);
        //Did we change the default mode in the options?
        if (lastTxModem != Main.txModem && !Main.connected) {
            try {
                Main.m.setModemModeNow(Main.txModem);
                Main.lastRxModem = Main.lastTxModem = Main.m.getModemString(Main.txModem);
            } catch (Exception ex) {
                Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
            }
        }
        // Update the gui with these settings
        if (!Main.haveGPSD) {
            if (!Main.gpsPort.curstate) {
                Latitudestr = Main.configuration.getPreference("LATITUDE");
                Longitudestr = Main.configuration.getPreference("LONGITUDE");
            }
        }

        // Change options to use a spinner
        this.spnMinute.setValue(Integer.parseInt(optionsDialog.getBeaconqrg()));

        // Update modem pre and postamble immediately
        Main.modemPreamble = optionsDialog.getModemPreamble();
        Main.modemPostamble = optionsDialog.getModemPostamble();
        optionsDialog.dispose();
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Error_when_handling_preferences!"), ex, true);
    }
}//GEN-LAST:event_mnuPreferencesActionPerformed

    /**
     * This is the main text box for user input, the one at the bottom.
     *
     * @param evt
     */
private void txtMainEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMainEntryActionPerformed
// TODO add your handling code here:

    String intext = txtMainEntry.getText();
    if (Main.connected) {
        if (Main.ttyConnected.equals("Connected")) {
            Main.mainwindow += "\n=>>" + intext + "\n";
            Main.txText += (intext + "\n");
        } else {
            if (intext.indexOf("~PASS") == 0) {
                intext = Main.cr.encrypt(Main.sm.hispubkey, intext.substring(5));
                Main.txText += (intext + "\n");
                Main.mainwindow += "\n=>>" + intext + "\n";
            } else if (intext.contains(":SETPASSWORD")) {
                String mailpass = Main.configuration.getPreference("POPPASS");
                if (mailpass.length() > 0 & Main.sessionPasswrd.length() > 0) {
                    intext = Main.cr.encrypt(Main.sm.hispubkey, mailpass + "," + Main.sessionPasswrd);
                    Main.txText += ("~Msp" + intext + "\n");
                    Main.mainwindow += "\n=>>" + intext + "\n";
                } else {
                    Main.mainwindow += "\n=>>" + "No POP password or link password set?\n";
                }
            } else {
                Main.txText += (intext + "\n");
                Main.mainwindow += "\n=>>" + intext + "\n";
            }
        }
        txtMainEntry.setText("");
    } else { //Unconnected
        //On RadioMsg tab: send radio messages (SMSs), otherwise send Pskmail unproto
        if (tabMain.getTitleAt(tabMain.getSelectedIndex()).equals("Radio Msg")) {
            if (!intext.equals("")) {
                //RMsgTxList.addMessageToList(RadioMSG.selectedTo, RadioMSG.selectedVia, intext,
                RMsgTxList.addMessageToList(selectedTo, selectedVia, intext, false, null, 0, null);
                txtMainEntry.setText("");
            }
        } else { //Not RadioMsg, so use Pskmail actions
            if (intext.startsWith(":")) {
                if (intext.startsWith(":MOTD:")) {
                    Main.Motd = intext.substring(4);
                    txtMainEntry.setText("");
                } else if (intext.startsWith(":MH")) {
                    int i = 0;
                    Main.mainwindow += "\nServer,lastrx,average\n";
                    for (i = 0; i < 10; i++) {
                        if (!Main.serversArray[i].equals("") & Main.rxData[i][0] > 0) {
                            Main.mainwindow += Main.serversArray[i] + "," + Main.rxData[i][0] + "," + Main.getrxdata_avg(i) + "\n";
                        }
                    }
                }
            } else if (intext.contains("@")) {
                //Unproto email (Pskmail version)
                Main.q.set_txstatus(TxStatus.TXUImessage);
                Main.q.send_uimessage(intext);
            } else {
                try {
                    //Unproto APRS message to another station
                    Main.q.set_txstatus(TxStatus.TXaprsmessage);
                    Main.q.send_aprsmessage(intext + Main.q.getAPRSMessageNumber());
                    Main.mainwindow += " =>>" + intext + "\n";
                    appendMSGWindow(" =>>" + intext + "\n");
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}//GEN-LAST:event_txtMainEntryActionPerformed

    /**
     * An aprs message is received from the external aprs mapping app. transmit
     * it here
     *
     * @param intext
     */
    public void SendAprsMsgDeSocket(String intext) {
        try {
            if (!Main.connected) {
                Main.q.set_txstatus(TxStatus.TXaprsmessage);
//            Main.q.send_aprsmessage(intext + Main.q.getAPRSMessageNumber());
                Main.q.send_aprsmessage(intext);
                Main.mainwindow += " =>>" + intext + "\n";
                appendMSGWindow(" =>>" + intext + "\n");
                // send an ack to the map client
                Pattern APRSm = Pattern.compile(":(\\S+)\\s*:.*\\{(.*)\\}");
                Matcher am = APRSm.matcher(intext);
                if (am.lookingAt()) {
                    String tocall = am.group(1);
                    String acknr = am.group(2);
                    String ackstring = tocall + ">APX201,TCPIP*,T2TUENL::" + IgateCallField.getText() + "     :ack" + acknr + "}\n";
                    Main.mapSock.sendmessage(ackstring);
                }
            }
        } catch (Exception e) {
            // Log this but dont make a fuss
            Main.log.writelog("Could not send aprs msg.", e, false);
        }
    }

    private void PositButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Main.q.Message(mainpskmailui.getString("Send_Beacon"), 5);
            Main.q.set_txstatus(TxStatus.TXBeacon);
            Main.q.send_beacon();
            Main.configuration.setPreference("LATITUDE", Latitudestr);
            Main.configuration.setPreference("LONGITUDE", Longitudestr);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

private void txtStatusKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtStatusKeyReleased
// TODO add your handling code here:
    String mystring;
    mystring = txtStatus.getText();
    Main.statusText = mystring;
    Main.configuration.setPreference("STATUS", mystring);
    Main.q.setTxtStatus(mystring);
}//GEN-LAST:event_txtStatusKeyReleased

private void cboAPRSIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAPRSIconActionPerformed
    // TODO add your handling code here:
    Main.q.Message(mainpskmailui.getString("Icon_set..."), 5);
    Main.configuration.setPreference("ICON", cboAPRSIcon.getSelectedItem().toString());
    Main.icon = cboAPRSIcon.getSelectedItem().toString();
    if (cboAPRSIcon.getSelectedIndex() < 9 & Main.ICONlevel.equals("/")) {
        lblAPRSIcon.setIcon(Icons[cboAPRSIcon.getSelectedIndex()]);
    } else {
        lblAPRSIcon.setIcon(Icons[0]);
    }

}//GEN-LAST:event_cboAPRSIconActionPerformed

private void chkBeaconStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkBeaconStateChanged
    // TODO add your handling code here:

    if (this.chkBeacon.isSelected()){//GEN-LAST:event_chkBeaconStateChanged
            Main.q.Message(mainpskmailui.getString("Beacon_on"), 5);
            Main.configuration.SetBeacon("1");
        } else {
            Main.q.Message(mainpskmailui.getString("Beacon_off"), 5);
            Main.configuration.SetBeacon("0");
        }
    }

private void FileReadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileReadButtonActionPerformed

    Main.q.Message(mainpskmailui.getString("Choose_File_to_read..."), 5);
    String myfile = "";
    if (evt.getSource() == FileReadButton) {
        File downloads = new File(Main.homePath + Main.dirPrefix + "Downloads" + Main.separator);

        JFileChooser chooser = new JFileChooser(downloads);
        int returnVal = chooser.showOpenDialog(chooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            myfile = chooser.getSelectedFile().getName();
        }
        try {
            File file = new File(Main.homePath + Main.dirPrefix + "Downloads" + Main.separator + myfile);
            if (file.isFile()) {
                String Content = getContents(file);
                FilesTxtArea.setText(Content);
            }
        } catch (Exception e) {
            // dbd
        }
    }
    Main.q.Message(mainpskmailui.getString("Reading_") + myfile, 5);
}//GEN-LAST:event_FileReadButtonActionPerformed

private void mnuBulletinsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuBulletinsActionPerformed

    try {
        Main.q.Message(mainpskmailui.getString("Deleting_bulletin_file..."), 5);
        String bulletinpath = null;
        if (File.separator.equals("/")) {
            bulletinpath = System.getProperty("user.home") + "/.pskmail/Downloads/bulletins";
        } else {
            bulletinpath = System.getProperty("user.home") + "\\pskmail\\Downloads\\bulletins";
        }

        File fb = new File(bulletinpath);
            deleteContent(fb);//GEN-LAST:event_mnuBulletinsActionPerformed
            FilesTxtArea.setText("");
        } catch (FileNotFoundException ex) {
            Main.log.writelog(mainpskmailui.getString("File_was_not_found!"), ex, true);
        } catch (IOException ex) {
            Main.log.writelog(mainpskmailui.getString("IO_Exception_when_accessing_file!"), ex, true);
        }
    }
private void AbortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbortButtonActionPerformed

    AbortButtonAction();

}//GEN-LAST:event_AbortButtonActionPerformed

            private void bConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bConnectActionPerformed

                Main.summoning = false;
                if (Main.connected) {
                    ConnectButtonAction();
                } else {
                    ConnectWindow cw = new ConnectWindow(this, true);
                    cw.setLocationRelativeTo(this);
                    cw.setVisible(true);
                }
            }//GEN-LAST:event_bConnectActionPerformed

            private void mnuGetTidestationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetTidestationsActionPerformed
                // TODO add your handling code here:
                if (Main.connected) {
                    Main.txText += "~GETTIDESTN\n";
                    Main.q.Message(mainpskmailui.getString("Requesting_list_of_tidal_reference_stations..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuGetTidestationsActionPerformed

            private void mnuGetTideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetTideActionPerformed

                if (Main.connected) {
                    String tidestationnumber = "";
                    tidestationnumber = txtMainEntry.getText();
                    if (tidestationnumber.equals("")) {
                        Main.q.Message(mainpskmailui.getString("Need__number_of_the_station..."), 5);
                    } else {
                        Main.txText += "~GETTIDE " + tidestationnumber + "\n";
                        Main.q.Message(mainpskmailui.getString("Requesting_tidal_information_for_atation_") + tidestationnumber, 5);
                    }
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuGetTideActionPerformed

            private void mnuGetAPRSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetAPRSActionPerformed

                if (Main.connected) {
                    Main.txText += "~GETNEAR\n";
                    Main.q.Message(mainpskmailui.getString("Getting_APRS_stations_near_you..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuGetAPRSActionPerformed

            private void mnuGetServerfqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetServerfqActionPerformed

                if (Main.connected) {
                    Main.txText += "~GETSERVERS\n";
                    Main.q.Message(mainpskmailui.getString("Getting_list_of_servers_from_the_web..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuGetServerfqActionPerformed

            private void mnuGetPskmailNewsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetPskmailNewsActionPerformed
                // TODO add your handling code here:
                if (Main.connected) {
                    Main.txText += "~GETNEWS\n";
                    Main.q.Message(mainpskmailui.getString("Trying_to_get_the_news_from_the_web..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuGetPskmailNewsActionPerformed

            private void mnuHeadersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHeadersActionPerformed

                Main.sm.deleteFile("headers");
                Main.sm.makeFile("headers");
                Main.q.Message(mainpskmailui.getString("Delete_list_of_mail_headers..."), 5);
                // Refresh the view
                refreshEmailGrids();
            }//GEN-LAST:event_mnuHeadersActionPerformed

            private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
                if (Main.connected) {
                    SendEmailWhileConnected();
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
}//GEN-LAST:event_SendButtonActionPerformed

            private void ListFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ListFilesButtonActionPerformed
                // TODO add your handling code here:
                if (Main.connected | Main.ttyConnected.equals("Connected")) {
                    Main.txText += "~LISTFILES\n";
                    Main.q.Message(mainpskmailui.getString("Requesting_files_list..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_ListFilesButtonActionPerformed

            private void DownloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadButtonActionPerformed

                if (Main.connected) {
                    String file = txtMainEntry.getText();
                    if (file.length() > 0) {
                        Main.txText += "~GETBIN " + file + "\n";
                        Main.q.Message(mainpskmailui.getString("Requesting_file_") + file, 5);
                        Main.filesTextArea += mainpskmailui.getString("Requesting_file_") + file + "\n";
                    } else {
                        Main.q.Message(mainpskmailui.getString("Which_file_shall_I_get?"), 5);
                    }
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_DownloadButtonActionPerformed

            private void FileConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileConnectActionPerformed

                Main.summoning = false;
                if (Main.connected) {
                    ConnectButtonAction();
                } else {
                    ConnectWindow cw = new ConnectWindow(this, true);
                    cw.setLocationRelativeTo(this);
                    cw.setVisible(true);
                }
            }//GEN-LAST:event_FileConnectActionPerformed

            private void FileAbortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileAbortButtonActionPerformed

                AbortButtonAction();
                /*
                try {
                    Main.Bulletinmode = false;
                    lblStatus.setText(mainpskmailui.getString("Listening"));
                    lblStatus.setForeground(Color.lightGray);
                    Main.Status = mainpskmailui.getString("Listening");
                    Main.Connected = false;
                    disableMboxMenu();
                    enableMnuPreferences2();
                    Main.TTYConnected = "";
                    Main.sm.FileDownload = false;
                    try {
                        if (Main.sm.pFile != null) {
                            Main.sm.pFile.close();
                        }
                    } catch (IOException e) {
                        Main.q.Message("Cannot close pending file", 10);
                    }

                    bConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                    Conn_connect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                    FileConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
                    Main.q.send_abort();
            	    Main.q.Message(mainpskmailui.getString("Aborting..."), 5);
        } catch (InterruptedException ex) {
            Logger.getLogger(mainpskmailui.class.getName()).log(Level.SEVERE, null, ex);
        }
                    */
    }//GEN-LAST:event_FileAbortButtonActionPerformed

            private void UpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateButtonActionPerformed

                if (!Main.connected) {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("Choose_File_to_update..."), 5);
                    if (evt.getSource() == UpdateButton) {
                        File downloads = new File(Main.homePath + Main.dirPrefix + "Downloads" + Main.separator);
                        String myfile = "";
                        JFileChooser chooser = new JFileChooser(downloads);
                        int returnVal = chooser.showOpenDialog(chooser);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            myfile = chooser.getSelectedFile().getName();
                        }
                        File dfile = new File(Main.homePath + Main.dirPrefix + "Downloads" + Main.separator + myfile);
                        if (dfile.isFile()) {
                            Main.txText += "~GETBIN " + myfile + "\n";
                            Main.q.Message(mainpskmailui.getString("Updating_") + myfile, 5);
                            Main.filesTextArea += mainpskmailui.getString("Updating_") + myfile + "\n";
                        }
                    }
                }
            }//GEN-LAST:event_UpdateButtonActionPerformed

            private void mnuMboxListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMboxListActionPerformed

                if (Main.connected) {
                    Main.txText += "~LISTLOCAL\n";
                    Main.q.Message(mainpskmailui.getString("Requesting_list_of_local_mails_on_the_server"), 5);
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuMboxListActionPerformed

            private void mnuMboxReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMboxReadActionPerformed
                // TODO add your handling code here:
                if (Main.connected) {
                    String number = txtMainEntry.getText();
                    if (number.length() > 0) {
                        Main.txText += "~READLOCAL " + number + "\n";
                        Main.q.Message(mainpskmailui.getString("Reading_local_mail_") + number + mainpskmailui.getString("_on_the_server"), 5);
                    } else {
                        Main.q.Message(mainpskmailui.getString("Which_email?_(need_number...)"), 5);
                    }
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuMboxReadActionPerformed

            private void mnuMboxDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMboxDeleteActionPerformed
                // TODO add your handling code here:
                if (Main.connected) {
                    String number = txtMainEntry.getText();
                    if (number.length() > 0) {
                        Main.txText += "~DELETELOCAL " + number + "\n";
                        Main.q.Message(mainpskmailui.getString("Deleting_mail_nr._") + number + mainpskmailui.getString("on_the_server"), 5);
                    }
                } else {
                    Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
                }
            }//GEN-LAST:event_mnuMboxDeleteActionPerformed

            private void mnuGetWebPagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGetWebPagesActionPerformed
                GetWebPageDialog WebDialog;
                // TODO add your handling code here:
                try {
                    WebDialog = new GetWebPageDialog(this, true);
                    WebDialog.dispose();
                } catch (Exception ex) {
                    Main.log.writelog(mainpskmailui.getString("Error_when_opening_web_dialog!"), ex, true);
                }

            }//GEN-LAST:event_mnuGetWebPagesActionPerformed

    /**
     * Make sure the mode buttons are set properly
     *
     * @param what 0 = Mail/APRS 1 = MAIL/Scanning 2 = TTY
     */
    private void updatemailscanset(Integer what) {
        switch (what) {
            case 0: // Mail/ APRS
                this.mnuMailAPRS2.setSelected(true);
                this.mnuMailScanning.setSelected(false);
//                this.mnuTTY2.setSelected(false);
                break;
            case 1: // Mail / Scanning
                this.mnuMailAPRS2.setSelected(false);
                this.mnuMailScanning.setSelected(true);
//                this.mnuTTY2.setSelected(false);
                break;
//            case 2: // TTY
//                this.mnuMailAPRS2.setSelected(false);
//                this.mnuMailScanning.setSelected(false);
//                this.mnuTTY2.setSelected(true);
//                break;
        }
    }

    /**
     * Update the mode menus, just make the right one selected
     *
     * @param mymode
     */
    private void updatemodeset(ModemModesEnum mymode) {
/*
//   Main.defaultmode = mymode;
        try {
            switch (mymode) {
                case PSK63:
                    mnuPSK63.setSelected(true);
                    break;
                case PSK125:
                    mnuPSK125.setSelected(true);
                    break;
                case PSK250:
                    mnuPSK250.setSelected(true);
                    break;
                case PSK500:
                    mnuPSK500.setSelected(true);
                    break;
                case PSK1000:
                    break;
                case PSK125R:
                    mnuPSK125R.setSelected(true);
                    break;
                case PSK250R:
                    mnuPSK250R.setSelected(true);
                    break;
                case PSK500R:
                    mnuPSK500R.setSelected(true);
                    break;
                case MFSK16:
                    mnuMFSK16.setSelected(true);
                    break;
                case MFSK22:
                    break;
                case MFSK32:
                    mnuMFSK32.setSelected(true);
                    break;
                case THOR8:
                    mnuTHOR8.setSelected(true);
                    break;
                case THOR11:
                    //mnuTHOR11.setSelected(false);
                    break;
                case THOR22:
                    mnuTHOR22.setSelected(true);
                    break;
                case DOMINOEX5:
                    mnuDOMINOEX5.setSelected(true);
                    break;
                case DOMINOEX11:
                    mnuDOMINOEX11.setSelected(true);
                    break;
                case DOMINOEX22:
                    mnuDOMINOEX22.setSelected(true);
                    break;
                case CTSTIA:
                    break;
            }
        } catch (NoClassDefFoundError ne) {
            Main.q.Message("Error setting mode", 5);
        }
*/
    }
    
    private void updatemodereset() {
        /*
                    mnuPSK63.setSelected(false);
                    mnuPSK125.setSelected(false);
                    mnuPSK250.setSelected(false);
                    mnuPSK500.setSelected(false);
                    mnuPSK125R.setSelected(false);
                    mnuPSK250R.setSelected(false);
                    mnuPSK500R.setSelected(false);
                    mnuMFSK16.setSelected(false);
                    defaultmnu.setSelected(false);
                    mnuMFSK32.setSelected(false);
                    mnuTHOR8.setSelected(false);
                    mnuTHOR22.setSelected(false);
                    mnuDOMINOEX5.setSelected(false);
                    mnuDOMINOEX11.setSelected(false);
                    mnuDOMINOEX22.setSelected(false);
        */
    }

    public void ConnectButtonAction() {
        if (Main.connected) {
            Main.txText += ("~QUIT" + "\n");
            lblStatus.setText(mainpskmailui.getString("Discon"));
            lblStatus.setForeground(Color.RED);
            Main.connectingTime = 0;
            Main.scanning = false;
            Main.sm.FileDownload = false;
            Main.comp = false;
            try {
                if (Main.sm.pFile != null) {
                    Main.sm.pFile.close();
                }
            } catch (IOException e) {
                Main.q.Message("Cannot close pending file", 10);
            }

        } else if (Main.ttyConnected.equals("Connected")) {
            Main.txText += ("~QUIT" + "\n");
            lblStatus.setText(mainpskmailui.getString("Discon"));
            lblStatus.setForeground(Color.RED);
            Main.ttyConnected = "";
        } else {
            try {
                if (mnuMailScanning.isSelected()) {
                    Main.scanning = true;
                    Main.Connecting = true;
                    Main.connectingPhase = true;
                    Connect_time = 5;
                    lblStatus.setText(mainpskmailui.getString("Connecting"));
                    lblStatus.setForeground(Color.RED);
                    Main.q.Message(mainpskmailui.getString("Connecting,_waiting_for_channel..."), 5);
                } else {
                    Main.Connecting = true;
                    Main.connectingPhase = true;
                    Connect_time = 5;
                    Main.m.setRxRsid("ON");
                    Main.q.set_txstatus(TxStatus.TXConnect);
                    Main.q.send_frame("");
                    Main.q.Message(mainpskmailui.getString("Sending_Connect_request..."), 5);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void AbortButtonAction() {
        Main.bulletinMode = false;
        Main.Connecting = false;
        Main.connectingPhase = false;
        Main.connectingTime = 0;
        Main.scanning = false;
        Main.sm.FileDownload = false;
        Main.comp = false;
        lblStatus.setText(mainpskmailui.getString("Listening"));
        lblStatus.setForeground(Color.lightGray);
        Main.status = mainpskmailui.getString("Listening");

        if (Main.connected) {
            try {

                Main.q.send_abort();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
            //VK2ETA: Close and reset all Rx transactions
            Main.sm.abortAllRxTransactions();
            //Old processing, maybe redundant now
            try {
                if (Main.sm.pFile != null) {
                    Main.sm.pFile.close();
                }
            } catch (IOException e) {
                Main.q.Message("Cannot close pending file", 10);
            }
            try {
                if (Main.sm.dlFile != null) {
                    Main.sm.dlFile.close();
                }
            } catch (IOException e) {
                Main.q.Message("Cannot close pending file", 10);
            }
        } else if (Main.ttyConnected.equals("Connected")) {
            try {
                Main.q.send_abort();
                //VK2ETA: Close and reset all Rx transactions
                Main.sm.abortAllRxTransactions();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                enableMnuPreferences2();
                disableMboxMenu();
                Main.q.send_abort();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        bConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
        Conn_connect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
        FileConnect.setText(java.util.ResourceBundle.getBundle("javaradiomsg/mainpskmailui").getString("Connect"));
        Main.connected = false;
        Main.ttyConnected = "";
        Main.sm.FileDownload = false;
        Main.comp = false;
        Main.m.setModemModeNow(Main.defaultmode);
    }

private void mnuPSK63ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK63ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.PSK63;
        updatemodeset(mymode);
        Main.rxModemString = "PSK63";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK63"), 5);
}//GEN-LAST:event_mnuPSK63ActionPerformed

private void mnuPSK125ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK125ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.PSK125;
        updatemodeset(mymode);
        Main.rxModemString = "PSK125";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK125"), 5);
}//GEN-LAST:event_mnuPSK125ActionPerformed

private void mnuPSK250ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK250ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.PSK250;
        updatemodeset(mymode);
        Main.rxModemString = "PSK250";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK250"), 5);
}//GEN-LAST:event_mnuPSK250ActionPerformed

private void mnuTHOR22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTHOR22ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.THOR22;
        updatemodeset(mymode);
        Main.rxModemString = "THOR22";
        Main.txModem = mymode;
        Main.rxModem = mymode;
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_THOR22"), 5);
}//GEN-LAST:event_mnuTHOR22ActionPerformed

private void mnuModeQSYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuModeQSYActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        Main.txText += "~QSY!\n";
        Main.q.Message(mainpskmailui.getString("Asking_the_server_to_QSY"), 5);
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }
}//GEN-LAST:event_mnuModeQSYActionPerformed

private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField1ActionPerformed

private void menuMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMessagesActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        //Added option to nominate the number of messages
        String maxMessages = txtMainEntry.getText();
        //VK2ETA: syntax error?
        //Main.TX_Text += "~/~GETMSG\n";
        if (maxMessages.trim().length() > 0) {
            Main.txText += "~GETMSG " + maxMessages + "\n";
        } else {
            Main.txText += "~GETMSG\n";
        }
        Main.q.Message(mainpskmailui.getString("Getting_list_of_messages_from_the_web..."), 5);
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }
}//GEN-LAST:event_menuMessagesActionPerformed

private void mnuMailAPRS2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMailAPRS2ActionPerformed
    // Set the menu selected
    updatemailscanset(0);

}//GEN-LAST:event_mnuMailAPRS2ActionPerformed

private void mnuMailScanningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMailScanningActionPerformed
    updatemailscanset(1);

}//GEN-LAST:event_mnuMailScanningActionPerformed

private void GetGribActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GetGribActionPerformed
    // TODO add your handling code here:
    String input = txtMainEntry.getText();
    if (input.length() == 0) {
        String lat = Latitudestr;
        String lon = Longitudestr;
        String hilatstr = "N";
        String lolatstr = "N";
        String hilonstr = "E";
        String lolonstr = "E";
        float fl_lat = Float.valueOf(lat).floatValue();
        float fl_lon = Float.valueOf(lon).floatValue();
        int Intlat = (int) fl_lat;
        int Intlon = (int) fl_lon;
        int hilat = 0;
        int hilon = 0;
        int lolat = 0;
        int lolon = 0;

        hilat = Intlat + 5;
        if (hilat < 0) {
            hilatstr = "S";
            hilat = Math.abs(hilat);
        }
        lolat = Intlat - 5;
        if (lolat < 0) {
            lolatstr = "S";
            lolat = Math.abs(lolat);
        }
        hilon = Intlon + 5;
        if (hilon < 0) {
            hilonstr = "W";
            hilon = Math.abs(hilon);
        }
        lolon = Intlon - 5;
        if (lolon < 0) {
            lolonstr = "W";
            lolon = Math.abs(lolon);
        }
        input = Integer.toString(hilat) + hilatstr + "," + Integer.toString(lolat) + lolatstr + ","
                + Integer.toString(hilon) + hilonstr + "," + Integer.toString(lolon) + lolonstr;
    }
    String gribsquare = "send gfs:" + input;
    Pattern pgs = Pattern.compile(".*(\\d+\\w,\\d+\\w,\\d+\\w,\\d+\\w).*");
    Matcher mgs = pgs.matcher(gribsquare);
    if (!mgs.find()) {
        Main.q.Message(mainpskmailui.getString("Format_error:") + gribsquare, 10);
    } else {
        if (Main.connected) {
            Main.txText += "~SEND\nTo: query@saildocs.com\nSubject: none\n\n" + gribsquare + "\n.\n.\n";
        } else {

            Main.q.Message(mainpskmailui.getString("Connect_first..."), 10);
        }
    }
}//GEN-LAST:event_GetGribActionPerformed
    /**
     * Store the minute setting if changed by the user
     *
     * @param evt
     */
private void spnMinuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnMinuteStateChanged
    try {
        Main.configuration.setBeaconqrg(this.spnMinute.getValue().toString());
    } catch (Exception e) {
        Main.log.writelog(mainpskmailui.getString("Problem_changing_server_minute."), true);
    }
}//GEN-LAST:event_spnMinuteStateChanged

private void cboServerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cboServerFocusLost
    try {
        //Read the text window (may contain either a selected item OR a typed in server[:password]
        //getcboServer();
        /*
        String myServer = cboServer.getSelectedItem().toString();
        String OldServer = Main.q.getServer();
        if (myServer.length() > 1 && !myServer.equals(OldServer)) {
            Main.q.setServerAndPassword(myServer);
            //VK2ETA not anymore
            //Main.configuration.setServer(myServer);
            //But save in Main too
            Main.q.setServerAndPassword(myServer);
            //serverInput(myServer);
        }
        */
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Had_trouble_setting_the_server_to_link_to."), ex, true);
    }
       
}//GEN-LAST:event_cboServerFocusLost

    /**
     * User wrote in the server combo, lets handle that shall we..
     *
     * @param evt
     */
private void cboServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboServerActionPerformed
    try {
        //Read the text window (may contain either a selected item OR a typed in server[:password]
        getcboServer();
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Had_trouble_setting_the_server_to_link_to."), ex, true);
    }

}//GEN-LAST:event_cboServerActionPerformed

private void Ping_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Ping_menu_itemActionPerformed
    
    if (!Main.connected & !Main.Connecting & !Main.bulletinMode & !Main.iacMode) {
        try {
            Main.q.Message(mainpskmailui.getString("send_ping"), 5);
            Main.q.set_txstatus(TxStatus.TXPing);
            Main.q.send_ping();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}//GEN-LAST:event_Ping_menu_itemActionPerformed

private void Link_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Link_menu_itemActionPerformed
    // TODO add your handling code here:
    if (!Main.connected & !Main.Connecting & !Main.bulletinMode & !Main.iacMode) {
        try {
            Main.q.Message(mainpskmailui.getString("Link_to_server"), 5);
            Main.q.set_txstatus(TxStatus.TXlinkreq);
            Main.q.send_link();
            Main.sending_link = 5;
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}//GEN-LAST:event_Link_menu_itemActionPerformed

private void Beacon_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Beacon_menu_itemActionPerformed
    try {
        // TODO add your handling code here:
        Main.q.Message(mainpskmailui.getString("Send_Beacon"), 5);
        Main.q.set_txstatus(TxStatus.TXBeacon);
        Main.q.send_beacon();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_Beacon_menu_itemActionPerformed

private void WWV_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WWV_menu_itemActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        Main.txText += "~GETWWV\n";
        Main.q.Message(mainpskmailui.getString("Getting_WWV_Info_from_the_web..."), 5);
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }

}//GEN-LAST:event_WWV_menu_itemActionPerformed

private void chkAutoLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAutoLinkActionPerformed
    // TODO add your handling code here:
    if (chkAutoLink.isSelected()) {
        Main.autolink = true;
    } else {
        Main.autolink = false;
    }
}//GEN-LAST:event_chkAutoLinkActionPerformed

private void jRadioButtonMenuItemTHOR11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTHOR11ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.DOMINOEX5;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModem = mymode;
        Main.rxModemString = "DOMINOEX5";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_DomEx5"), 5);
}//GEN-LAST:event_jRadioButtonMenuItemTHOR11ActionPerformed

private void jRadioButtonMenuItemMFSK16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemMFSK16ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.MFSK16;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "MFSK16";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_MFSK16"), 5);
}//GEN-LAST:event_jRadioButtonMenuItemMFSK16ActionPerformed

private void jRadioButtonMenuItemMFSK22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemMFSK22ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.MFSK16;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "MFSK16";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_MFSK16"), 5);
}//GEN-LAST:event_jRadioButtonMenuItemMFSK22ActionPerformed

private void jRadioButtonMenuItemMFSK32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemMFSK32ActionPerformed
    try {
        System.out.println("MFSK32");
        ModemModesEnum mymode = ModemModesEnum.MFSK32;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "MFSK32";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_MFSK32"), 5);
}//GEN-LAST:event_jRadioButtonMenuItemMFSK32ActionPerformed

private void jRadioButtonMenuItemTHOR8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTHOR8ActionPerformed
    try {
        ModemModesEnum mymode = ModemModesEnum.THOR8;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "THOR8";
        Main.m.setModemModeNow(mymode);
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_THOR8"), 5);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
}//GEN-LAST:event_jRadioButtonMenuItemTHOR8ActionPerformed

private void chkAutoLinkStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkAutoLinkStateChanged
    if (this.chkAutoLink.isSelected()) {
        Main.q.Message(mainpskmailui.getString("Autolink_on"), 5);
        Main.configuration.setAutolink("1");
    } else {
        Main.q.Message(mainpskmailui.getString("Autolink_off"), 5);
        Main.configuration.setAutolink("0");
    }
}//GEN-LAST:event_chkAutoLinkStateChanged

private void Update_serverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Update_serverActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        Main.sm.sendUpdate();
        Main.q.Message("Sending record to server", 5);
    } else {
        Main.q.Message("You need to connect first...", 5);
    }
}//GEN-LAST:event_Update_serverActionPerformed

private void mnuPSK500ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK500ActionPerformed
    // TODO add your handling code here:
    try {
        ModemModesEnum mymode = ModemModesEnum.PSK500;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "PSK500";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK500"), 5);
}//GEN-LAST:event_mnuPSK500ActionPerformed


private void mnuPSK125RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK125RActionPerformed
    // TODO add your handling code here:
       try{//GEN-LAST:event_mnuPSK125RActionPerformed
            ModemModesEnum mymode = ModemModesEnum.PSK125R;
            updatemodeset(mymode);
            Main.txModem = mymode;
            Main.rxModemString = "PSK125R";
            Main.m.setModemModeNow(mymode);
        } catch (Exception ex) {
            Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
        }
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK125R"), 5);
    }

private void mnuPSK250RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK250RActionPerformed
    // TODO add your handling code here:
       try{//GEN-LAST:event_mnuPSK250RActionPerformed
            ModemModesEnum mymode = ModemModesEnum.PSK250R;
            updatemodeset(mymode);
            Main.txModem = mymode;
            Main.rxModemString = "PSK250R";
            Main.m.setModemModeNow(mymode);
        } catch (Exception ex) {
            Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
        }
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK250R"), 5);
    }


private void mnuPSK500RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPSK500RActionPerformed
    // TODO add your handling code here:
    try {
        ModemModesEnum mymode = ModemModesEnum.PSK500R;
        updatemodeset(mymode);
        Main.txModem = mymode;
        Main.rxModemString = "PSK500R";
        Main.m.setModemModeNow(mymode);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
    }
    Main.q.Message(mainpskmailui.getString("Switching_modem_to_PSK500R"), 5);
}//GEN-LAST:event_mnuPSK500RActionPerformed


private void FilesTxtAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_FilesTxtAreaMouseClicked
    // TODO add your handling code here:
    if (evt.getButton() == 3) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("doubleclick to download this file");
        popup.add(menuItem);
        popup.show(evt.getComponent(),
                evt.getX(), evt.getY());
    } else if (evt.getButton() == 1) {
        if (evt.getClickCount() > 1) {
            int car = FilesTxtArea.getCaretPosition();
            int ln = 0;
            int lso = 0;
            String linestr = "";
            String filestr = "0";
            try {
                ln = FilesTxtArea.getLineOfOffset(car);
                lso = FilesTxtArea.getLineStartOffset(ln);
                int leo = FilesTxtArea.getLineEndOffset(ln);
                int dif = leo - lso;
                linestr = FilesTxtArea.getText(lso, dif);
            } catch (BadLocationException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
            //           System.out.println(linestr);
            Pattern pt = Pattern.compile("^(\\S+)\\s");
            Matcher mt = pt.matcher(linestr);
            if (mt.lookingAt()) {
                filestr = mt.group(1);
            }

            if (Main.connected & !filestr.equals("0")) {
                txtMainEntry.setText("");
                Main.txText += "~GETBIN " + filestr + "\n";
                Main.q.Message(mainpskmailui.getString("Downloading_file_") + filestr, 15);
            } else {
                Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 10);
            }
        }
    }

}//GEN-LAST:event_FilesTxtAreaMouseClicked

private void MnuTelnetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnuTelnetActionPerformed
    // TODO add your handling code here:
    TelnetDialog tdialog = new TelnetDialog(new javax.swing.JFrame(), true);
    tdialog.setVisible(true);
}//GEN-LAST:event_MnuTelnetActionPerformed

    /**
     * Called whenever The email folder (inbox, outbox etc) is changed. Use that
     * to reload the view with the proper content
     *
     * @param evt
     */
private void lstBoxSelectorValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstBoxSelectorValueChanged
    //Call the proper routine , depending on what is selected

//    Object selected = this.lstBoxSelector.getSelectedValue();
//    String strSelected = selected.toString();
////  System.out.println(strSelected);
//    if (strSelected.equalsIgnoreCase("Inbox"))
//        LoadInbox();
//    if (strSelected.equalsIgnoreCase("Outbox"))
//        LoadOutbox();
//    if (strSelected.equalsIgnoreCase("Headers"))
//        LoadHeaders();
//    if (strSelected.equalsIgnoreCase("New Mail"))
//        NewMail();
//    if (strSelected.equalsIgnoreCase("QTC"))
//        QTC();

}//GEN-LAST:event_lstBoxSelectorValueChanged

private void tblInboxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblInboxMouseClicked
    // TODO add your handling code here:
    try {
        MailViewer myEmail;
        Integer row;
        if (evt.getClickCount() == 2) {
            // Double click, open if its an email
            row = this.tblInbox.getSelectedRow();
            if (row > -1) {
                Object selected = this.lstBoxSelector.getSelectedValue();
                String strSelected = selected.toString();
                if (strSelected.equalsIgnoreCase("Inbox")) {
                    Email mymail = (Email) inboxmodel.getRowObject(row);
                    myEmail = new MailViewer(mymail);
                    // Center screen
                    myEmail.setLocationRelativeTo(null);
                    myEmail.setVisible(true);
                }
                if (strSelected.equalsIgnoreCase("Outbox") || strSelected.equalsIgnoreCase("Sent")) {
                    Email mymail = (Email) outboxmodel.getRowObject(row);
                    myEmail = new MailViewer(mymail);
                    // Center screen
                    myEmail.setLocationRelativeTo(null);
                    myEmail.setVisible(true);
                }
                if (strSelected.equalsIgnoreCase("Headers")) {
                    String mailstr = (String) headermodel.getValueAt(row, 0);
                    this.GetHeaderEmail(mailstr);
                }

            }
        }
    } catch (Exception e) {
        Main.log.writelog("Error when showing email.", e, true);
    }
}//GEN-LAST:event_tblInboxMouseClicked

    /**
     * Get the first selected row in the outbox list
     *
     * @return
     */
    private Email GetSelectedOutboxRow() {
        try {
            Integer row;
            Email mymail = null;
            row = this.tblInbox.getSelectedRow();

            if (row > -1) {
                Object selected = this.lstBoxSelector.getSelectedValue();
                String strSelected = selected.toString();
                if (strSelected.equalsIgnoreCase("Outbox")) {
                    mymail = (Email) outboxmodel.getRowObject(row);
                }
            }
            return mymail;
        } catch (Exception e) {
            Main.log.writelog("Could not fetch outbox message.", e);
            return null;
        }
    }


private void SetToChannelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetToChannelButtonActionPerformed
    // TODO add your handling code here:

    if (RigCtrl.opened) {
        RigCtrl.Setfreq(ServerfreqTxtfield.getText());

    } else {
        ClientFreqTxtfield.setText(ServerfreqTxtfield.getText());
    }

}//GEN-LAST:event_SetToChannelButtonActionPerformed

private void UpbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpbuttonActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        String fq = RigCtrl.Getfreq();
        int frq = Integer.parseInt(fq);
        frq += 500;
        fq = Integer.toString(frq);
        RigCtrl.Setfreq(fq);
    } else {
        int frq = Integer.parseInt(ClientFreqTxtfield.getText());
        frq += 500;
        String fq = Integer.toString(frq);
        ClientFreqTxtfield.setText(fq);
    }
}//GEN-LAST:event_UpbuttonActionPerformed

private void DownbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownbuttonActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        String fq = RigCtrl.Getfreq();
        int frq = Integer.parseInt(fq);
        frq -= 500;
        fq = Integer.toString(frq);
        RigCtrl.Setfreq(fq);
    } else {
        int frq = Integer.parseInt(ClientFreqTxtfield.getText());
        frq -= 500;
        String fq = Integer.toString(frq);
        ClientFreqTxtfield.setText(fq);
    }

}//GEN-LAST:event_DownbuttonActionPerformed

private void bSummonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSummonActionPerformed

    Main.summoning = true;
    Main.Connecting = true;
    Main.connectingPhase = true;

    if (Main.connected) {
        ConnectButtonAction();
    } else {
        ConnectWindow cw = new ConnectWindow(this, true);
        cw.setLocationRelativeTo(this);
        cw.setVisible(true);
    }

    /*
    if (!Main.Connected & !Main.TTYConnected.equals("Connected")){
                        ScannerCheckbox.setSelected(false);
                        Main.wantScanner = false;
                        String servermodem = Main.configuration.getPreference("BLOCKLENGTH");
                        Main.CurrentModemProfile = servermodem;
                        int smn = Integer.parseInt(servermodem);
                        if (smn > 0 & smn < 9) {
                            Main.RxModem = Main.Modemarray[smn];
                            Main.RxModemString = Main.Modes[smn];
                            Main.SendCommand = "<cmd><mode>" + Main.Modes[smn] + "</mode></cmd>";
                        }
                       try {
                               Main.summoning = true;
                               Main.Connecting = true;
                               Main.connectingPhase = true;
                               Main.Connecting_time = 5;
                               Main.q.send_rsid_command("ON");
                               Main.q.set_txstatus(txstatus.TXSummon);
                               Main.q.send_frame("");
                               Main.q.Message(mainpskmailui.getString("Sending_Summon_request..."), 5);

                        }
                       catch (InterruptedException ex) {
                            Logger.getLogger(mainpskmailui.class.getName()).log(Level.SEVERE, null, ex);
                       }
    }
     */
}//GEN-LAST:event_bSummonActionPerformed

private void ScannerCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScannerCheckboxActionPerformed
    // TODO add your handling code here:
    if (ScannerCheckbox.isSelected()) {
        Main.configuration.setPreference("SCANNER", "yes");
        Main.wantScanner = true;
    } else {
        Main.configuration.setPreference("SCANNER", "no");
        Main.wantScanner = false;
    }

}//GEN-LAST:event_ScannerCheckboxActionPerformed

private void cbCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCompActionPerformed
    // TODO add your handling code here:


}//GEN-LAST:event_cbCompActionPerformed

private void IgateSwitchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IgateSwitchActionPerformed
    // TODO add your handling code here:
    if (Main.wantIgate) {
        IgateSwitch.setSelected(false);
        IgateSwitch.setText("OFF");
        Main.configuration.setPreference("IGATE", "no");
        Main.savePreferences();

        if (Igate.in != null) {
            try {
                Igate.in.close();
                Igate.out.close();
                Igate.connected = false;
            } catch (IOException e) {
                //
            }
        }
        Main.wantIgate = false;
    } else {
        //Get the callsign and clean it for APRS
        String IgateCall = Main.cleanCallForAprs(Main.configuration.getPreference("CALLSIGNASSERVER"));
        if (IgateCall.length() > 0) {
            IgateSwitch.setText("ON");
            Main.configuration.setPreference("IGATE", "yes");
            Main.savePreferences();
            try {
                Igate.start();
            } catch (IOException ex) {
//                System.out.println("FAILED");
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (Igate.connected) {
                IgateSwitch.setSelected(true);
                IgateIndicator.setText("Connected");
                Main.wantIgate = true;
            }
        } else {
            //My call sign is not valid for APRS, do NOT connect ever
            IgateSwitch.setSelected(false);
            IgateSwitch.setText("OFF");
            Main.wantIgate = false;
            Main.q.Message("Call sign can't be used on APRS. No iGate.", 10);
        }
    }

}//GEN-LAST:event_IgateSwitchActionPerformed

private void IgateCallFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IgateCallFieldActionPerformed
    // TODO add your handling code here:
    String igateCall = Main.cleanCallForAprs(IgateCallField.getText());
    if (igateCall.length() > 0) {
        Igate.aprsCall = igateCall;
        Main.aprsCall = Igate.aprsCall;
        //Main.configuration.setPreference("APRSCALL", igate.aprscall);
    }
}//GEN-LAST:event_IgateCallFieldActionPerformed

private void IgateSwitchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IgateSwitchMouseClicked
    // TODO add your handling code here:


}//GEN-LAST:event_IgateSwitchMouseClicked

private void jGetIACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGetIACActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        Main.txText += "~GETIAC\n";
        Main.q.Message(mainpskmailui.getString("Getting_IAC_Fleetcodes..."), 5);
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }

}//GEN-LAST:event_jGetIACActionPerformed

private void FileSendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileSendButtonActionPerformed
    // TODO add your handling code here:

    String codedFile = "";
    String token = "";
    String myfile = "";
    String mypath = "";

    if (evt.getSource() == FileSendButton) {
        File uploads = new File(Main.homePath + Main.dirPrefix + "Uploads" + Main.separator);

        JFileChooser chooser = new JFileChooser(uploads);
        //Test showing hidden files as well
        //chooser.setFileHidingEnabled(false);
        int returnVal = chooser.showOpenDialog(chooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            myfile = chooser.getSelectedFile().getName();
            mypath = chooser.getSelectedFile().getPath();
//       System.out.println(myfile);
//       System.out.println(mypath);

            if (mypath.length() > 0) {
                //Get the destination callsign
                String Destination;
                if (Main.ttyConnected.equals("Connected")) {
                    //I am server
                    Destination = Main.ttyCaller;
                } else {
                    //I am client
                    Destination = Main.sm.myserver;
                }

                /*
                String Destination = Main.sm.myserver;
                Destination = (String) JOptionPane.showInputDialog(
                        new JFrame(),
                        "File destination (CALL)",
                        "Destination", JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon("java2sLogo.GIF"), null, Destination);
                //System.out.println("User's input: " + Destination);
                */
                FileInputStream in = null;

                File incp = new File(mypath);
                File outcp = new File(Main.homePath + Main.dirPrefix + "tmpfile");

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(incp);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                            + Destination.replaceAll("\\/", "+") + "_-u-_" + token + "_-" + myfile;

                Base64.encodeFileToFile(tmpfile, codedFile);

                File dlfile = new File(tmpfile);
                if (dlfile.exists()) {
                    dlfile.delete();
                }

                String TrString = "";
                File mycodedFile = new File(codedFile);
                if (mycodedFile.isFile()) {
                    TrString = ">FM:" + Main.sm.mycall + ":" + Destination + ":"
                            + token + ":u:" + myfile
                            + ":" + Long.toString(mycodedFile.length()) + "\n";
                }

                if (Main.connected) {
                    if (mycodedFile.isFile()) {
                        //Main.txText += "~FO5:" + Main.sm.mycall + ":" + Destination + ":"
                        Main.txText += ">FM:" + Main.sm.mycall + ":" + Destination + ":"
                                + token + ":u:" + myfile
                                + ":" + Long.toString(mycodedFile.length()) + "\n";
                        String dataString = RMsgUtil.readFile(codedFile);
                        Main.txText += dataString + "\n-end-\n";
                        Main.q.Message(mainpskmailui.getString("Uploading_") + myfile, 5);
                        Main.filesTextArea += mainpskmailui.getString("Uploading_") + myfile + "\n";
                        Main.filetype = "u";
                    }
                }

                /*
                File Transactions = new File(Main.transactions);
                FileWriter tr;
                try {
                    tr = new FileWriter(Transactions, true);
                    tr.write(TrString);
                    tr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                */
            }
        }
    }
}//GEN-LAST:event_FileSendButtonActionPerformed

private void mnuUploadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuUploadsActionPerformed
    FileReader tin = null;
    // TODO add your handling code here:
    File uplds = new File(Main.transactions);
    if (uplds.exists()) {
        uplds.delete();
    }
    String fileName = Main.outPendingDir;
    // A File object to represent the filename
    File dir = new File(fileName);

    // Make sure the file or directory exists and isn't write protected
    if (dir.exists()) {
        String[] info = dir.list();
        for (int i = 0; i < info.length; i++) {
            @SuppressWarnings("static-access")
            File n = new File(Main.outPendingDir + dir.separator + info[i]);
            if (!n.isFile()) {
                continue;
            } else {
                n.delete();
            }
        }
    }
    String fileNamep = Main.pendingDir;
    // A File object to represent the filename
    File dirp = new File(fileNamep);

    // Make sure the file or directory exists and isn't write protected
    if (dirp.exists()) {
        String[] info = dirp.list();
        for (int i = 0; i < info.length; i++) {
            @SuppressWarnings("static-access")
            File n = new File(Main.pendingDir + dir.separator + info[i]);
            if (!n.isFile()) {
                continue;
            } else {
                n.delete();
            }
        }
    }

    Main.q.Message("Deleting pending files", 10);
}//GEN-LAST:event_mnuUploadsActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

    if (Main.m != null) {
        Main.m.Sendln("<cmd>normal</cmd>\n");
        
        Main.exitingSoon = true;
        Main.m.killFldigi();

        try {
            if (Main.m.pout != null) {
                Main.m.pout.close();
            }
            if (Main.m.in != null) {
                Main.m.in.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException np) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, np);
        }
    }
    dispose();
    System.exit(0); //calling the method is a must
}//GEN-LAST:event_formWindowClosing

private void mnuDownloadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDownloadsActionPerformed
    // TODO add your handling code here:
    File dir = new File(Main.pendingDir);
    if (dir.exists()) {
        String[] info = dir.list();
        for (int i = 0; i < info.length; i++) {
            @SuppressWarnings("static-access")
            File n = new File(Main.pendingDir + dir.separator + info[i]);
            if (!n.isFile()) {
                continue;
            } else {
                n.delete();
            }

        }
    }
}//GEN-LAST:event_mnuDownloadsActionPerformed

//File/Pending transactions?
private void mnuFileListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFileListActionPerformed

    /*
    File tr = new File(Main.transactions);
    FileReader fr1 = null;

    if (tr.exists()) {
        Main.mainwindow += "Pending file uploads:\n";
        try {
            fr1 = new FileReader(tr);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedReader br = new BufferedReader(fr1);
        String brl = "";
        try {
            while ((brl = br.readLine()) != null) {
                String[] ss = brl.split(":");
                if (ss[4].equals("u")) {
                    Main.mainwindow += ss[2] + ":" + ss[5] + ":" + ss[6] + "\n";
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    */
    //VK2ETA: valid only as client really
    File outb1 = new File(Main.homePath + Main.dirPrefix + "Outbox");
    int i1 = outb1.list().length;
    Main.mainwindow += "\nOutbox:" + Integer.toString(i1) + "\n";

    File outb = new File(Main.pendingDir);
    int i = outb.list().length;

    Main.mainwindow += "Incomplete uploads:" + Integer.toString(i) + "\n\n";
}//GEN-LAST:event_mnuFileListActionPerformed

    /**
     * Call this to refresh the visible, selected, email window
     */
    private void refreshEmailGrids() {
        //Call the proper routine , depending on what is selected
        Object selected = this.lstBoxSelector.getSelectedValue();
        if (selected != null) {
            String strSelected = selected.toString();
            if (strSelected.equalsIgnoreCase("Inbox")) {
                emailgrid = grid.IN;
                LoadInbox();
            }
            if (strSelected.equalsIgnoreCase("Outbox")) {
                emailgrid = grid.OUT;
                LoadOutbox();
            }
            if (strSelected.equalsIgnoreCase("Headers")) {
                emailgrid = grid.HEADERS;
                LoadHeaders();
            }
            if (strSelected.equalsIgnoreCase("Sent")) {
                emailgrid = grid.SENT;
                LoadSent();
            }
        }
    }

    /**
     * The list box at email was clicked, show what email grid the user selected
     *
     * @param evt
     */
private void lstBoxSelectorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstBoxSelectorMouseClicked
    //Call the proper routine , depending on what is selected
    refreshEmailGrids();
}//GEN-LAST:event_lstBoxSelectorMouseClicked

private void bQTCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bQTCActionPerformed
    // TODO add your handling code here:
    QTC();
}//GEN-LAST:event_bQTCActionPerformed

private void bNewMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNewMailActionPerformed
    // TODO add your handling code here:
    NewMail();
}//GEN-LAST:event_bNewMailActionPerformed

private void bDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteActionPerformed
    // TODO add your handling code here:
    DeleteMail();
}//GEN-LAST:event_bDeleteActionPerformed

private void freq0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq0ActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq0.getText());

    } else {
        ClientFreqTxtfield.setText(freq0.getText());
    }
}//GEN-LAST:event_freq0ActionPerformed

private void freq1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq1ActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq1.getText());

    } else {
        ClientFreqTxtfield.setText(freq1.getText());
    }

}//GEN-LAST:event_freq1ActionPerformed

private void freq2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq2ActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq2.getText());

    } else {
        ClientFreqTxtfield.setText(freq2.getText());
    }

}//GEN-LAST:event_freq2ActionPerformed

private void freq3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq3ActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq3.getText());

    } else {
        ClientFreqTxtfield.setText(freq3.getText());
    }

}//GEN-LAST:event_freq3ActionPerformed

private void freq4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq4ActionPerformed
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq4.getText());

    } else {
        ClientFreqTxtfield.setText(freq4.getText());
    }

}//GEN-LAST:event_freq4ActionPerformed

private void freq0MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_freq0MouseClicked
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq0.getText());

    } else {
        ClientFreqTxtfield.setText(freq0.getText());
    }

}//GEN-LAST:event_freq0MouseClicked

private void freq1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_freq1MouseClicked
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq1.getText());

    } else {
        ClientFreqTxtfield.setText(freq1.getText());
    }

}//GEN-LAST:event_freq1MouseClicked

private void freq2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_freq2MouseClicked
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq2.getText());

    } else {
        ClientFreqTxtfield.setText(freq2.getText());
    }

}//GEN-LAST:event_freq2MouseClicked

private void freq3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_freq3MouseClicked
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq3.getText());

    } else {
        ClientFreqTxtfield.setText(freq3.getText());
    }

}//GEN-LAST:event_freq3MouseClicked

private void freq4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_freq4MouseClicked
    // TODO add your handling code here:
    if (RigCtrl.opened) {
        RigCtrl.Setfreq(freq4.getText());

    } else {
        ClientFreqTxtfield.setText(freq4.getText());
    }

}//GEN-LAST:event_freq4MouseClicked

private void PrefSaveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrefSaveMenuActionPerformed
    // TODO add your handling code here:
    try {
        optionsDialog = new OptionsDialog(this, true);
        //  System.out.println("SAVE:" + Main.configuration.getPreference("DEFAULTMODE"));
        optionsDialog.SaveOptions();
        optionsDialog.dispose();
    } catch (NullPointerException n) {
        Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, n);
    }
}//GEN-LAST:event_PrefSaveMenuActionPerformed

    /**
     * Archive of the old method. Only sends one email per event. I want to
     * Not used
    private void ArchivedEmailSendButtonActionPerformed() {
        // TODO add your handling code here:
        if (Main.Connected) {
//            FileReader out = null;
            Main.q.Message(mainpskmailui.getString("Trying_to_send_your_email..."), 5);
            if (Main.compressedmail) {
                try {

                    File dir = new File(Main.HomePath + Main.Dirprefix + "Outbox");

                    File[] files = dir.listFiles();

                    File dir2 = new File(Main.HomePath + Main.Dirprefix + "Outpending");

                    File[] files2 = dir2.listFiles();

                    if (files2.length > 0) {
                        if (files2[0].length() > 0) {

                            String FileOut = files2[0].toString();
                            int i = FileOut.lastIndexOf(File.separator);
                            String ffilename = (i > -1) ? FileOut.substring(i + 1) : FileOut;

                            File fpendir = new File(Main.Outpendingdir);
                            File[] fpendingfiles = fpendir.listFiles();

                            String fname = "mail";
                            File testTransactions = new File(Main.Transactions);
                            if (testTransactions.exists()) {

                                FileReader trf = new FileReader(Main.Transactions);
                                BufferedReader bf = new BufferedReader(trf);
                                String s = "";

                                while ((s = bf.readLine()) != null) {
                                    if (s.contains(ffilename)) {
                                        String[] ss = s.split(":");
                                        fname = ss[5];
                                    }
                                }
                            }

                            if (fpendingfiles.length > 0) {
                                String penfile0 = fpendingfiles[0].toString();

                                if (penfile0.length() > 0) {
                                    String outfile = penfile0;
                                    i = outfile.lastIndexOf(File.separator);
                                    ffilename = (i > -1) ? outfile.substring(i + 1) : outfile;
                                    Main.TX_Text += "~FO5:" + Main.sm.mycall + ":" + Main.sm.myserver + ":"
                                            + ffilename + ":u:" + fname + ":" + Long.toString(fpendingfiles[0].length()) + "\n";
                                }
                            }
                        }

                    } else if (files.length > 0 & files[0].length() > 0) {
                        Main.Mailoutfile = files[0].toString();
                        int j = Main.Mailoutfile.lastIndexOf(File.separator);
                        String filename = (j > -1) ? Main.Mailoutfile.substring(j + 1) : Main.Mailoutfile;

                        File pendir = new File(Main.Pendingdir);
                        File[] pendingfiles = pendir.listFiles();

                        if (pendingfiles.length > 0) {
                            String penfile0 = pendingfiles[0].toString();

                            if (penfile0.length() > 0) {
                                String outfile = penfile0;
                                j = outfile.lastIndexOf(File.separator);
                                filename = (j > -1) ? outfile.substring(j + 1) : outfile;
                                Main.TX_Text += "~FO5:" + Main.sm.mycall + ":" + Main.sm.myserver + ":"
                                        + filename + ":s: :" + Long.toString(pendingfiles[0].length()) + "\n";
                            }
                        } else {
                            // no pending message
                            String zippedfile = Main.HomePath + Main.Dirprefix + "tmp.mail";
                            String codedfile = Main.HomePath + Main.Dirprefix + "tmp2.mail";

                            FileInputStream in = new FileInputStream(Main.Mailoutfile);
                            GZIPOutputStream gzout = new GZIPOutputStream(new FileOutputStream(zippedfile));
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                gzout.write(buffer, 0, bytesRead);
                            }
                            in.close();
                            gzout.close();
                            Base64.encodeFileToFile(zippedfile, codedfile);

                            File filelength = new File(codedfile);

                            int length = (int) filelength.length();
                            Session.DataSize = length;
                            Session.DataReceived = 0;
                            String lengthstr = Integer.toString(length);

                            FileReader b64in = new FileReader(codedfile);
                            //
                            BufferedReader br = new BufferedReader(b64in);

                            FileWriter fstream = new FileWriter(Main.Pendingdir + filename);
                            BufferedWriter pout = new BufferedWriter(fstream);

                            if (Main.protocol == 0) {
                                Main.TX_Text += "~CSEND\n";
                            } else {

                                String callsign = Main.configuration.getPreference("CALL");
                                callsign = callsign.trim();
                                //String servercall = Main.configuration.getPreference("SERVER");
                                String servercall = Main.q.getServer().trim();
                                servercall = servercall.trim();
                                Main.TX_Text += ">FM:" + callsign + ":" + servercall + ":" + filename + ":s: :" + lengthstr + "\n";
                            }
                            String record = null;
                            while ((record = br.readLine()) != null) {
                                Main.TX_Text += record + "\n";
                                if (Main.protocol > 0) {
                                    pout.write(record + "\n");
                                }
                            }
                            Main.TX_Text += "-end-\n";
                            if (Main.protocol > 0) {
                                pout.write("-end-\n");
                                pout.close();
                            }

                            Session.DataSize = Main.TX_Text.length();

                            boolean success;
                            File f = new File(zippedfile);
                            if (f.exists()) {
                                success = f.delete();
                            }

                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(mainpskmailui.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // not compressed (default)
                try {
                    File dir = new File(Main.HomePath + Main.Dirprefix + "Outbox");
                    File[] files = dir.listFiles();
                    Main.Mailoutfile = files[0].getAbsolutePath();
                    FileReader in = new FileReader(files[0]);
                    BufferedReader br = new BufferedReader(in);
                    String record = null;
                    while ((record = br.readLine()) != null) {
                        Main.TX_Text += record + "\n";
                    }

                } catch (IOException ex) {
                    Logger.getLogger(mainpskmailui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
        }
    }
     */

    /**
     * Send Email button pushed, should initiate a transfer of all messages if
     * connected
     *
     * @param evt
     */
private void EmailSendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EmailSendButtonActionPerformed
    if (Main.connected) {
        SendEmailWhileConnected();
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }
}//GEN-LAST:event_EmailSendButtonActionPerformed

    /**
     * Send all emails in the outbound queue
     */
    private void SendEmailWhileConnected() {
        // Lets update the status so the user knows something is happening
        Main.q.Message(mainpskmailui.getString("Trying_to_send_your_email..."), 5);

        try {
            if (Main.compressedmail) {
                // TBD: Experimental patch
                compressedEmailSend();
            } else {
                // Prepare and send the mail
                File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
                File[] files = dir.listFiles();

                // Check for a selected row in the outbox here
                Email myemail = this.GetSelectedOutboxRow();
                if (myemail != null && myemail.getFileName().length() > 0) {
                    // Yes, there was one email selected, send that then 
                    Main.mailOutFile = Main.homePath + Main.dirPrefix + "Outbox" + File.separator + myemail.getFileName();
                    File myfi = new File(Main.mailOutFile);
                    SendUncompressedEmail(myfi);
                } else {
                    // No row selected so just go ahead and send them all                    
                    for (File myfile : files) {
                        Main.mailOutFile = myfile.toString();
                        SendUncompressedEmail(myfile);
                    }
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Had problems sending outgoing email. ", e, true);
        }
    }

    /**
     * Send an uncompressed email
     */
    private void SendUncompressedEmail(File email) {
        try {
            FileReader in = new FileReader(email);
            BufferedReader br = new BufferedReader(in);
            String record = null;
            while ((record = br.readLine()) != null) {
                Main.txText += record + "\n";
            }
        } catch (Exception ex) {
            Main.log.writelog("Could not send uncompressed email. " + ex.getMessage().toString(), ex, true);
        }
    }

    /**
     * Send outgoing emails. Compress and send
     *
     * TBD: GAMMAL

    private void SendCompressedEmail() {
        try {
            // Get all emails in the outbox folder, those are standard outgoing emails
            File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
            File[] files = dir.listFiles();

            // Get all emails in the Outpending folder, those are ??
            File dir2 = new File(Main.homePath + Main.dirPrefix + "Outpending");
            File[] files2 = dir2.listFiles();

            // Any outpending files?
            if (files2.length > 0) {
                if (files2[0].length() > 0) {

                    String FileOut = files2[0].toString();
                    int i = FileOut.lastIndexOf(File.separator);
                    String ffilename = (i > -1) ? FileOut.substring(i + 1) : FileOut;

                    File fpendir = new File(Main.outPendingDir);
                    File[] fpendingfiles = fpendir.listFiles();

                    String fname = "mail";
                    File testTransactions = new File(Main.transactions);
                    if (testTransactions.exists()) {
                        FileReader trf = new FileReader(Main.transactions);
                        BufferedReader bf = new BufferedReader(trf);
                        String s = "";
                        while ((s = bf.readLine()) != null) {
                            if (s.contains(ffilename)) {
                                String[] ss = s.split(":");
                                fname = ss[5];
                            }
                        }
                    }

                    if (fpendingfiles.length > 0) {
                        String penfile0 = fpendingfiles[0].toString();

                        if (penfile0.length() > 0) {
                            String outfile = penfile0;
                            i = outfile.lastIndexOf(File.separator);
                            ffilename = (i > -1) ? outfile.substring(i + 1) : outfile;
                            Main.txText += "~FO5:" + Main.sm.mycall + ":" + Main.sm.myserver + ":"
                                    + ffilename + ":u:" + fname + ":" + Long.toString(fpendingfiles[0].length()) + "\n";
                        }
                    }
                }
            } else if (files.length > 0 & files[0].length() > 0) {
                // Check for a selected row in the outbox here
                Email myemail = this.GetSelectedOutboxRow();
                if (myemail != null && myemail.getFileName().length() > 0) {
                    // Yes, there was one email selected, send that then 
                    Main.mailOutFile = Main.homePath + Main.dirPrefix + "Outbox" + File.separator + myemail.getFileName();
                    int j = Main.mailOutFile.lastIndexOf(File.separator);
                    String filename = (j > -1) ? Main.mailOutFile.substring(j + 1) : Main.mailOutFile;
                    Compressandsend(filename);
                } else {
                    // No row selected so just go ahead and send them all
                    for (File iterfilename : files) {
                        Main.mailOutFile = iterfilename.toString();
                        int j = Main.mailOutFile.lastIndexOf(File.separator);
                        String filename = (j > -1) ? Main.mailOutFile.substring(j + 1) : Main.mailOutFile;
                        Compressandsend(filename);
                    }
                }
            }
            // Also check for pending files
            File pendir = new File(Main.pendingDir);
            File[] pendingfiles = pendir.listFiles();

            if (pendingfiles.length > 0) {
                SendPendingfiles();
            }

        } catch (Exception ex) {
            Main.log.writelog("Failed when sending email.", true);
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     */
    
    
    /**
     * New routine to send compressed email.
     */
    private void compressedEmailSend() {
        
        try {
            // Get all emails in the outbox folder, those are standard outgoing emails
            File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
            File[] files = dir.listFiles();

            if (files.length > 0 && files[0].length() > 0) {
                // Check for a selected row in the outbox here
                Email myemail = this.GetSelectedOutboxRow();
                if (myemail != null && myemail.getFileName().length() > 0) {
                    // Yes, there was one email selected, send that then 
                    Main.mailOutFile = Main.homePath + Main.dirPrefix + "Outbox" + File.separator + myemail.getFileName();
                    int j = Main.mailOutFile.lastIndexOf(File.separator);
                    String filename = (j > -1) ? Main.mailOutFile.substring(j + 1) : Main.mailOutFile;
                    compressAndSend(filename);
                } else {
                    // No row selected so just go ahead and send them all
                    for (File iterfilename : files) {
                        Main.mailOutFile = iterfilename.toString();
                        int j = Main.mailOutFile.lastIndexOf(File.separator);
                        String filename = (j > -1) ? Main.mailOutFile.substring(j + 1) : Main.mailOutFile;
                        compressAndSend(filename);
                    }
                }
            }
        } catch (Exception ex) {
            Main.log.writelog("Failed when sending email.", true);
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Takes a filename (from outbox), compresses and sends
     *
     * @param filename
     */
    private void compressAndSend(String filename) {
        try {
            String zippedfile = Main.homePath + Main.dirPrefix + "tmp.mail";
            String codedfile = Main.homePath + Main.dirPrefix + "tmp2.mail";

            FileInputStream in = new FileInputStream(Main.mailOutFile);
            GZIPOutputStream gzout = new GZIPOutputStream(new FileOutputStream(zippedfile));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                gzout.write(buffer, 0, bytesRead);
            }
            in.close();
            gzout.close();
            Base64.encodeFileToFile(zippedfile, codedfile);

            File filelength = new File(codedfile);
            int length = (int) filelength.length();
            Session.DataSize = length;
            Session.DataReceived = 0;
            String lengthstr = Integer.toString(length);

            FileReader b64in = new FileReader(codedfile);
            BufferedReader br = new BufferedReader(b64in);

            if (Main.protocol == 0) { //Old protocol
                FileWriter fstream = new FileWriter(Main.pendingDir + filename);
                BufferedWriter pout = new BufferedWriter(fstream);
                Main.txText += "~CSEND\n";
                String record = null;
                while ((record = br.readLine()) != null) {
                    Main.txText += record + "\n";
                }
                Main.txText += "-end-\n";
            } else {
                //Current protocol with upload resumes
                String callsign = Main.configuration.getPreference("CALL");
                callsign = callsign.trim();
                //String servercall = Main.configuration.getPreference("SERVER");
                String servercall = Main.q.getServer().trim();
                servercall = servercall.trim();
                //Have we tried to send it before?
                File partialFile = new File(Main.outPendingDir + filename);
                if (partialFile.exists()) {
                    //Try an upload resume
                    Main.txText += "~FO5:" + callsign + ":" + servercall + ":" + filename + ":s: :" + lengthstr + "\n";
                } else {
                    FileWriter fstream = new FileWriter(Main.outPendingDir + filename);
                    BufferedWriter pout = new BufferedWriter(fstream);
                    //First send
                    Main.txText += ">FM:" + callsign + ":" + servercall + ":" + filename + ":s: :" + lengthstr + "\n";
                    String record = null;
                    while ((record = br.readLine()) != null) {
                        Main.txText += record + "\n";
                        pout.write(record + "\n");
                    }
                    Main.txText += "-end-\n";
                    //pout.write("-end-\n"); //compressed data without -end- statement
                    pout.close();
                }
            }

            Session.DataSize = Main.txText.length();
            boolean success;
            File f = new File(zippedfile);
            if (f.exists()) {
                success = f.delete();
            }
        } catch (Exception ex) {
            Main.log.writelog("Compressed mail send failed.", ex, true);
        }
        //VK2ETA needs a finally here for closing the files
    }

    /*
    private void SendPendingfiles() {
        File pendir = new File(Main.pendingDir);
        File[] pendingfiles = pendir.listFiles();

        if (pendingfiles.length > 0) {
            String penfile0 = pendingfiles[0].toString();

            if (penfile0.length() > 0) {
                String outfile = penfile0;
                int j = outfile.lastIndexOf(File.separator);
                String filename = (j > -1) ? outfile.substring(j + 1) : outfile;
                Main.txText += "~FO5:" + Main.sm.mycall + ":" + Main.sm.myserver + ":"
                        + filename + ":s: :" + Long.toString(pendingfiles[0].length()) + "\n";
            }
        }
    }
    */
    
private void APRSServerSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_APRSServerSelectActionPerformed
    // TODO add your handling code here:
    Main.aprsServer = (String) APRSServerSelect.getSelectedItem();
    Main.configuration.setPreference("APRSINTERNETSERVER", Integer.toString(APRSServerSelect.getSelectedIndex()));
    Main.savePreferences();
}//GEN-LAST:event_APRSServerSelectActionPerformed

private void CQButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CQButtonActionPerformed
    // TODO add your handling code here:
    if (!Main.connected) {
        try {
            Main.q.set_txstatus(TxStatus.TXCQ);
            Main.q.send_frame("");
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}//GEN-LAST:event_CQButtonActionPerformed

private void mnuAbout2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAbout2ActionPerformed
    // TODO add your handling code here:

    try {
        AboutForm about = new AboutForm();
        about.setLocationRelativeTo(null);
        about.setDefaultCloseOperation(HIDE_ON_CLOSE);
        about.setVisible(true);
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Error_when_handling_about_window."), ex, true);
    }

}//GEN-LAST:event_mnuAbout2ActionPerformed

private void tblInboxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblInboxMouseReleased
    if (mnuEmailPopup.isVisible()) {
        return;
    }
    this.tblInboxMousePressed(evt);
}//GEN-LAST:event_tblInboxMouseReleased

    /**
     * Handles showing of context menu popup from email grids
     *
     * @param evt
     */
private void tblInboxMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblInboxMousePressed
    //Show the popup if its triggered and not already visible
    if (!evt.isPopupTrigger() || mnuEmailPopup.isVisible()) {
        return;
    }
    // Where did this take place?
    JTable source = (JTable) evt.getSource();
    int nrow = source.rowAtPoint(evt.getPoint());
    int ncolumn = source.columnAtPoint(evt.getPoint());
    if (!source.isRowSelected(nrow)) {
        source.changeSelection(nrow, ncolumn, false, false);
    }

    // What is being shown? Inbox, outbox or headers
    switch (emailgrid) {
        case IN:
            this.mnuEmailPopup.show(evt.getComponent(), evt.getX(), evt.getY());
            break;
        case OUT:
            this.mnuOutbox.show(evt.getComponent(), evt.getX(), evt.getY());
            break;
        case HEADERS:
            this.mnuHeaders.show(evt.getComponent(), evt.getX(), evt.getY());
            break;
    }
}//GEN-LAST:event_tblInboxMousePressed

    /**
     * Email popup menu action, handle what is clicked.
     *
     * @param evt
     */
private void mnuEmailOpenGetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuEmailOpenGetMouseClicked
    // TODO add your handling code here:
}//GEN-LAST:event_mnuEmailOpenGetMouseClicked

    /**
     * Email popup menu action, handle what is clicked.
     *
     * @param evt
     */
private void mnuEmailOpenGetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEmailOpenGetActionPerformed
    try {
        MailViewer myEmail;
        Integer row;
        row = this.tblInbox.getSelectedRow();
        if (row > -1) {
            Object selected = this.lstBoxSelector.getSelectedValue();
            String strSelected = selected.toString();
            if (strSelected.equalsIgnoreCase("Inbox")) {
                Email mymail = (Email) inboxmodel.getRowObject(row);
                myEmail = new MailViewer(mymail);
                // Center screen
                myEmail.setLocationRelativeTo(null);
                myEmail.setVisible(true);
            }
            if (strSelected.equalsIgnoreCase("Outbox")) {
                Email mymail = (Email) outboxmodel.getRowObject(row);
                myEmail = new MailViewer(mymail);
                // Center screen
                myEmail.setLocationRelativeTo(null);
                myEmail.setVisible(true);
            }
            if (strSelected.equalsIgnoreCase("Headers")) {
                String mailstr = (String) headermodel.getValueAt(row, 0);
                this.GetHeaderEmail(mailstr);
            }
        }
    } catch (Exception e) {
        Main.log.writelog("Error when showing email.", e, true);
    }
}//GEN-LAST:event_mnuEmailOpenGetActionPerformed

private void Conn_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Conn_connectActionPerformed

    Main.summoning = false;
    if (Main.connected) {
        ConnectButtonAction();
    } else {
        ConnectWindow cw = new ConnectWindow(this, true);
        cw.setLocationRelativeTo(this);
        cw.setVisible(true);
    }
}//GEN-LAST:event_Conn_connectActionPerformed

private void Conn_abortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Conn_abortActionPerformed
    // TODO add your handling code here:
    AbortButtonAction();

}//GEN-LAST:event_Conn_abortActionPerformed

private void txtMainEntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtMainEntryMouseClicked
    // TODO add your handling code here:
    if (evt.getButton() == 3 & Main.ttyConnected.length() > 0) {
        String message = "Send text from system clipboard?";

        // Modal dialog with yes/no button
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText
                = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
                System.out.println(ex);

            } catch (IOException ex) {
                System.out.println(ex);

            }
        }

        int answer = JOptionPane.showConfirmDialog(jTextPane2, message);

        if (answer == JOptionPane.YES_OPTION) {
            //               System.out.println(result);
            Main.txText += (result);

        }

    }
}//GEN-LAST:event_txtMainEntryMouseClicked

private void ServerfreqTxtfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ServerfreqTxtfieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_ServerfreqTxtfieldActionPerformed

private void mnuMonitorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMonitorActionPerformed
    // TODO add your handling code here:
    if (Main.monitorMode) {
        Main.monitorMode = false;
        mnuMonitor.setSelected(false);
    } else {
        Main.monitorMode = true;
        mnuMonitor.setSelected(true);
        Main.m.setRxRsid("ON");
    }
}//GEN-LAST:event_mnuMonitorActionPerformed

private void mnuClearInboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuClearInboxActionPerformed
    // TODO add your handling code here:
    Main.sm.deleteFile("Inbox");
    Main.sm.makeFile("Inbox");
    Main.q.Message("Clear Inbox", 5);
    // Refresh the view
    refreshEmailGrids();

}//GEN-LAST:event_mnuClearInboxActionPerformed

private void mnuClearOutboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuClearOutboxActionPerformed
    // TODO add your handling code here:

    File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
    if (dir.exists()) {
        String[] info = dir.list();
        for (int i = 0; i < info.length; i++) {
            File n = new File(Main.homePath + Main.dirPrefix + "Outbox" + dir.separator + info[i]);
            if (!n.isFile()) // skip ., .., other directories too
            {
                continue;
            }

            if (!n.delete()) {
                Main.q.Message("Couldn't remove " + n.getPath(), 5);
            } else {
                Main.q.Message("Clear Outbox", 5);
                // Refresh the view
                refreshEmailGrids();
            }
        }
    }


}//GEN-LAST:event_mnuClearOutboxActionPerformed

private void GetforecastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GetforecastActionPerformed
    // TODO add your handling code here:
    if (Main.connected) {
        Main.txText += "~GET2IAC\n";
        Main.q.Message(mainpskmailui.getString("Getting_IAC_Forecast..."), 5);
    } else {
        Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
    }

}//GEN-LAST:event_GetforecastActionPerformed

private void lblStatusMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblStatusMouseClicked

    if (Main.monitorMode) {
        Main.monitorMode = false;
        Main.q.Message("Monitor OFF", 5);
        mnuMonitor.setSelected(false);
    } else if (Main.bulletinMode) {
        Main.bulletinMode = false;
        Main.status = "Listening";
        Main.q.Message("Bulletin OFF", 5);
    } else {
        Main.monitorMode = true;
        Main.q.Message("Monitor ON", 5);
        mnuMonitor.setSelected(true);
        Main.m.setRxRsid("ON");
    }
}//GEN-LAST:event_lblStatusMouseClicked

    /**
     * Save of text from the terminal text box. Selected text gets saved to a
     * file
     *
     * @param evt
     */
private void mnuTextSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTextSaveActionPerformed
    try {
        // First make sure there is anything to save
        String SaveText = jTextPane2.getSelectedText();
        if (SaveText != null && SaveText.isEmpty()) {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setSelectedFile(new File("fileToSave.txt"));
            // Show the save dialog and save the file if user so requires
            int ret = jFileChooser.showSaveDialog(this);
            if (ret == 0) {
                File selFile = jFileChooser.getSelectedFile();
                FileWriter fw = new FileWriter(selFile);
                fw.write(SaveText);
                fw.flush();
                fw.close();
            }
        }
    } catch (Exception ex) {
        Main.log.writelog("Save of text as file failed. ", ex, true);
    }
}//GEN-LAST:event_mnuTextSaveActionPerformed

    /**
     * Show a popupmenu and allow save if text is selected
     *
     * @param evt
     */
private void jTextPane2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPane2MousePressed
    //Show the popup if its triggered and not already visible
    if (!evt.isPopupTrigger() || mnuTerminalPopup.isVisible()) {
        return;
    }
    this.mnuTerminalPopup.show(evt.getComponent(), evt.getX(), evt.getY());
}//GEN-LAST:event_jTextPane2MousePressed

private void jTextPane2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPane2MouseReleased
    if (mnuTerminalPopup.isVisible()) {
        return;
    }
    this.jTextPane2MousePressed(evt);
}//GEN-LAST:event_jTextPane2MouseReleased

private void menuInquireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuInquireActionPerformed
    // TODO add your handling code here:
    if (!Main.connected & !Main.Connecting & !Main.bulletinMode & !Main.iacMode) {
        try {
            Main.q.Message(mainpskmailui.getString("send_inquire"), 5);
            Main.q.set_txstatus(TxStatus.TXInq);
            Main.q.send_frame("");
        } catch (InterruptedException ex) {
            Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}//GEN-LAST:event_menuInquireActionPerformed

private void jMenuQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuQualityActionPerformed
    // TODO add your handling code here:
    int i = 0;
    Main.mainwindow += "\nServer,lastrx,average\n";
    for (i = 0; i < 10; i++) {
        if (!Main.serversArray[i].equals("") & Main.rxData[i][0] > 0) {
            Main.mainwindow += Main.serversArray[i] + "," + Main.rxData[i][0] + "," + Main.getrxdata_avg(i) + "\n";
        }
    }
}//GEN-LAST:event_jMenuQualityActionPerformed

private void Stoptransaction_mnuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Stoptransaction_mnuActionPerformed
    // TODO add your handling code here:
    Main.txText += "~STOP:" + Main.sm.Transaction + "\n";
    //Must clear the receive session blocks otherwise we can have permanently missing blocks
    Main.sm.justSentStopCmd = true;
}//GEN-LAST:event_Stoptransaction_mnuActionPerformed

    /**
     * This is a context menu for the outbox
     *
     * @param evt
     */
private void mnuOutboxOpenMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutboxOpenMsgActionPerformed
    try {
        MailViewer myEmail;
        Integer row;

        row = this.tblInbox.getSelectedRow();
        if (row > -1) {
            Email mymail = (Email) outboxmodel.getRowObject(row);
            myEmail = new MailViewer(mymail);
            // Center screen
            myEmail.setLocationRelativeTo(null);
            myEmail.setVisible(true);
        }
    } catch (Exception e) {
        Main.log.writelog("Error when showing email.", e, true);
    }
}//GEN-LAST:event_mnuOutboxOpenMsgActionPerformed

    /**
     * Delete a message in the outbox
     *
     * @param evt
     */
private void mnuOutboxDeleteMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutboxDeleteMsgActionPerformed
    try {
        Integer row;
        String mym = "";

        row = tblInbox.getSelectedRow();
        if (row > -1) {
            Email mymail = (Email) outboxmodel.getRowObject(row);
            if (mymail != null) {
                mym = Main.homePath + Main.dirPrefix + "Outbox/" + mymail.getFileName();
                Integer n = ShowMessageWithYesNo("Delete", "Would you like to delete the message?");
                if (n == 0) {
                    DeleteFile(mym);
                    LoadOutbox();
                }
            }
        }
    } catch (Exception e) {
        Main.log.writelog("Error when deleting email.", e, true);
    }

}//GEN-LAST:event_mnuOutboxDeleteMsgActionPerformed

    /**
     * Show the Frequency helper guide
     *
     * @param evt
     */
private void mnuFqHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFqHelpActionPerformed
    // TODO add your handling code here:
    try {
        fqhelper = new FrequencyHelper(this, true);
        fqhelper.Calculatedial();
        // Center screen
        fqhelper.setLocationRelativeTo(null);
        fqhelper.setVisible(true);
        // Window is now closed
        fqhelper.dispose();
    } catch (Exception ex) {
        Main.log.writelog(mainpskmailui.getString("Error_when_handling_fqhelper"), ex, true);
    }
}//GEN-LAST:event_mnuFqHelpActionPerformed

    /**
     * Show the contacts window
     *
     * @param evt
     */
private void bContactsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bContactsActionPerformed
    try {
        AddressBook myBook;
        myBook = new AddressBook();
        myBook.setLocationRelativeTo(null);
        myBook.setVisible(true);
    } catch (Exception e) {
        Main.log.writelog(mainpskmailui.getString("Error_when_handling_AddressBook"), e, true);
    }
}//GEN-LAST:event_bContactsActionPerformed

    /**
     * Fetch email headers
     *
     * @param evt
     */
private void mnuHeadersFetchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHeadersFetchActionPerformed
    Integer row = this.tblInbox.getSelectedRow();
    String mailstr = (String) headermodel.getValueAt(row, 0);
    this.GetHeaderEmail(mailstr);
}//GEN-LAST:event_mnuHeadersFetchActionPerformed

    /**
     * Beacon compression checkbox had its state changed, save it
     *
     * @param evt
     */
    private void cbCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbCompStateChanged
        if (cbComp.isSelected()) {
            Main.compBeacon = true;
            Main.configuration.setBeaconcomp("1");
        } else {
            Main.compBeacon = false;
            Main.configuration.setBeaconcomp("0");
        }
    }//GEN-LAST:event_cbCompStateChanged

    /**
     * Save the state of this
     *
     * @param evt
     */
    private void cboBeaconPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboBeaconPeriodActionPerformed
        Main.q.Message(mainpskmailui.getString("Updated_beacon_time"), 5);
        Main.configuration.setPreference("BEACONTIME", cboBeaconPeriod.getSelectedItem().toString());
    }//GEN-LAST:event_cboBeaconPeriodActionPerformed

    private void Resetrecord_mnuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Resetrecord_mnuActionPerformed
        // TODO add your handling code here:
        Main.txText += "~RESETRECORD" + "\n";
    }//GEN-LAST:event_Resetrecord_mnuActionPerformed

    private void Twitter_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Twitter_sendActionPerformed
        try {
            // TODO add your handling code here:
            String message = txtMainEntry.getText();

            if (message.length() > 0) {
                if (!Main.connected) {
                    message = "TWEET " + message + "{01";
                    Main.q.set_txstatus(TxStatus.TXUImessage);
                    Main.q.send_aprsmessage(message);
                } else {
                    Main.txText += "~TWEET " + message + "\n";
                }
            } else {
                Main.q.Message(mainpskmailui.getString("What?"), 10);
            }
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Problem_sending_tweet."), e, true);
        }
    }//GEN-LAST:event_Twitter_sendActionPerformed

    private void GetUpdatesmenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GetUpdatesmenuItemActionPerformed
        if (Main.connected) {
            Main.txText += "~GETUPDATE\n";
        } else {
            Main.q.Message(mainpskmailui.getString("Connect_first..."), 10);
        }
    }//GEN-LAST:event_GetUpdatesmenuItemActionPerformed

    private void mnuLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLinkActionPerformed
        String codedFile;
        String token;
        String myfile = "";
        String path = "";
        Main.filetype = "b";

//          if (evt.getSource() == FileSendButton) {
        File uploads = new File(Main.homePath + Main.dirPrefix + "Uploads" + Main.separator);
        JFileChooser chooser = new JFileChooser(uploads);
        int returnVal = chooser.showOpenDialog(chooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            myfile = chooser.getSelectedFile().getName();
            path = chooser.getSelectedFile().getPath();
            //       System.out.println(myfile);
            //       System.out.println(path);
            //       Destination Dst = new Destination(this,true);
            if (myfile.length() > 0) {
                Main.myfile = myfile;
                String Destination = Main.sm.myserver;
                Destination = (String) JOptionPane.showInputDialog(
                        new JFrame(),
                        "File destination (CALL)",
                        "Destination", JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon("java2sLogo.GIF"), null, Destination);
                //    System.out.println("User's input: " + Destination);
                Main.fileDestination = Destination;
                FileInputStream in = null;
                File incp = new File(path);
                File outcp = new File(Main.homePath + Main.dirPrefix + Main.separator + "tmpfile");
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(incp);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outcp);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    byte[] buf = new byte[1024];
                    int i;
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
                } // end of "finally" block

                String mysourcefile = Main.homePath + Main.dirPrefix + Main.separator + "tmpfile";
                try {
                    in = new FileInputStream(mysourcefile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                GZIPOutputStream myzippedfile = null;
                String tmpfile = Main.homePath + Main.dirPrefix + Main.separator + "tmpfile.gz";
                try {
                    myzippedfile = new GZIPOutputStream(new FileOutputStream(tmpfile));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ioe) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ioe);
                }

                byte[] buff = new byte[4096];
                int bytesRead;
                try {
                    while ((bytesRead = in.read(buff)) != -1) {
                        myzippedfile.write(buff, 0, bytesRead);
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
                codedFile = Main.outPendingDir + token;
                Base64.encodeFileToFile(tmpfile, codedFile);
                File dlfile = new File(tmpfile);
                if (dlfile.exists()) {
                    dlfile.delete();
                }
                String TrString = "";
                File mycodedFile = new File(codedFile);
                if (mycodedFile.isFile()) {
                    TrString = "~FO5:" + Main.sm.mycall + ":" + Main.fileDestination + ":"
                            + token + ":b:" + myfile
                            + ":" + Long.toString(mycodedFile.length()) + "\n";
                }

                if (Main.connected) {
                    if (mycodedFile.isFile()) {
                        Main.txText += "~FO5:" + Main.sm.mycall + ":" + Main.fileDestination + ":"
                                + token + ":b:" + myfile
                                + ":" + Long.toString(mycodedFile.length()) + "\n";
                        Main.fileDestination = Destination;
                        Main.q.Message(mainpskmailui.getString("Uploading_") + myfile, 5);
                    }
                }

                File Transactions = new File(Main.transactions);
                FileWriter tr;
                try {
                    tr = new FileWriter(Transactions, true);
                    tr.write(TrString);
                    tr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//           }
    }//GEN-LAST:event_mnuLinkActionPerformed

    private void mnuMulticastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMulticastActionPerformed
        String codedFile;
        String token;
        String myfile = "";
        String path = "";
        Main.filetype = "b";

//          if (evt.getSource() == FileSendButton) {
        File uploads = new File(Main.homePath + Main.dirPrefix + "Uploads");
        JFileChooser chooser = new JFileChooser(uploads);
        int returnVal = chooser.showOpenDialog(chooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            myfile = chooser.getSelectedFile().getName();
            path = chooser.getSelectedFile().getPath();
            //       System.out.println(myfile);
            //       System.out.println(path);
            //       Destination Dst = new Destination(this,true);
            if (myfile.length() > 0) {
                Main.myfile = myfile;
                String Destination = Main.sm.myserver;
                Destination = (String) JOptionPane.showInputDialog(
                        new JFrame(),
                        "File destination (CALL)",
                        "Destination", JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon("java2sLogo.GIF"), null, Destination);
                //    System.out.println("User's input: " + Destination);
                Main.fileDestination = Destination;
                FileInputStream in = null;
                File incp = new File(path);
                File outcp = new File(Main.homePath + Main.dirPrefix + "tmpfile");
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(incp);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outcp);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    byte[] buf = new byte[1024];
                    int i;
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
                } // end of "finally" block

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

                byte[] buff = new byte[4096];
                int bytesRead;
                try {
                    while ((bytesRead = in.read(buff)) != -1) {
                        myzippedfile.write(buff, 0, bytesRead);
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
                codedFile = Main.homePath + Main.dirPrefix + "Outpending" + Main.separator + token;
                Base64.encodeFileToFile(tmpfile, codedFile);
                File dlfile = new File(tmpfile);
                if (dlfile.exists()) {
                    dlfile.delete();
                }
                String TrString = "";
                File mycodedFile = new File(codedFile);
                if (mycodedFile.isFile()) {
                    TrString = "~FO5:" + Main.sm.mycall + ":" + Main.fileDestination + ":"
                            + token + ":b:" + myfile
                            + ":" + Long.toString(mycodedFile.length()) + "\n";
                }

                if (Main.connected) {
                    if (mycodedFile.isFile()) {
                        Main.txText += "~FO5:" + Main.sm.mycall + ":" + Main.fileDestination + ":"
                                + token + ":b:" + myfile
                                + ":" + Long.toString(mycodedFile.length()) + "\n";
                        Main.fileDestination = Destination;
                        Main.filetype = "b";
                        Main.q.Message(mainpskmailui.getString("Uploading_") + myfile, 5);
                    }
                }

                File Transactions = new File(Main.transactions);
                FileWriter tr;
                try {
                    tr = new FileWriter(Transactions, true);
                    tr.write(TrString);
                    tr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//           }
    }//GEN-LAST:event_mnuMulticastActionPerformed

    private void mnuMFSK32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMFSK32ActionPerformed
        // TODO add your handling code here:
        try {
            ModemModesEnum mymode = ModemModesEnum.MFSK32;
            updatemodeset(mymode);
            Main.txModem = mymode;
            Main.rxModemString = "MFSK32";
            Main.m.setModemModeNow(mymode);
        } catch (Exception ex) {
            Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
        }
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_MFSK32"), 5);
    }//GEN-LAST:event_mnuMFSK32ActionPerformed

    private void mnuDOMINOEX22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDOMINOEX22ActionPerformed
        // TODO add your handling code here:
        try {
            ModemModesEnum mymode = ModemModesEnum.DOMINOEX22;
            updatemodeset(mymode);
            Main.txModem = mymode;
            Main.rxModemString = "DOMINOEX22";
            Main.m.setModemModeNow(mymode);
        } catch (Exception ex) {
            Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
        }
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_DomEx22"), 5);
    }//GEN-LAST:event_mnuDOMINOEX22ActionPerformed

    private void mnuDOMINOEX11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDOMINOEX11ActionPerformed
        // TODO add your handling code here:
        try {
            ModemModesEnum mymode = ModemModesEnum.DOMINOEX11;
            updatemodeset(mymode);
            Main.txModem = mymode;
            Main.rxModemString = "DOMINOEX11";
            Main.m.setModemModeNow(mymode);
        } catch (Exception ex) {
            Main.log.writelog(mainpskmailui.getString("Encountered_problem_when_setting_mode."), ex, true);
        }
        Main.q.Message(mainpskmailui.getString("Switching_modem_to_DomEx11"), 5);
    }//GEN-LAST:event_mnuDOMINOEX11ActionPerformed

    private void cboAPRS2ndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAPRS2ndActionPerformed
        Main.ICONlevel = cboAPRS2nd.getSelectedItem().toString();//GEN-HEADEREND:event_cboAPRS2ndActionPerformed
        Main.configuration.setPreference("ICONlevel", Main.ICONlevel);
//        System.out.println(Main.ICONlevel);

    }//GEN-LAST:event_cboAPRS2ndActionPerformed

    private void APRS_ISActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_APRS_ISActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_APRS_ISActionPerformed

    private void tblRadioMsgsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRadioMsgsMousePressed
        // Where did this take place?
        JTable source = (JTable) evt.getSource();
        int nrow = source.rowAtPoint(evt.getPoint());
        int ncolumn = source.columnAtPoint(evt.getPoint());
        if (!source.isRowSelected(nrow)) {
            source.changeSelection(nrow, ncolumn, false, false);
        }
    }//GEN-LAST:event_tblRadioMsgsMousePressed

    private void tblRadioMsgsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRadioMsgsMouseReleased
        // TODO add your handling code here:
        //this.tblInboxMousePressed(evt);
    }//GEN-LAST:event_tblRadioMsgsMouseReleased

    private void tblRadioMsgsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRadioMsgsMouseClicked
        JTable source = (JTable) evt.getSource();
        int nrow = source.rowAtPoint(evt.getPoint());
        int ncolumn = source.columnAtPoint(evt.getPoint());
        int clickCount = evt.getClickCount();
        if (clickCount == 1 && source.getSelectedRow() != -1) {
            if (!source.isRowSelected(nrow)) {
                source.changeSelection(nrow, ncolumn, false, false);
            }
        }
        if (clickCount == 2 && source.getSelectedRow() != -1) {
            if (!source.isRowSelected(nrow)) {
                source.changeSelection(nrow, ncolumn, false, false);
            }
            RMsgDisplayItem myDisplayItem = (RMsgDisplayItem) source.getModel().getValueAt(nrow, ncolumn);
            RMsgMessageViewer myRMsgViewer = new RMsgMessageViewer(myDisplayItem);
            // Center screen
            myRMsgViewer.setLocationRelativeTo(null);
            myRMsgViewer.setVisible(true);
        }
    }//GEN-LAST:event_tblRadioMsgsMouseClicked

    private void bRMsgSendSMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgSendSMSActionPerformed
        String intext = txtMainEntry.getText();
        if (!intext.equals("")) {
            //RMsgTxList.addMessageToList(RadioMSG.selectedTo, RadioMSG.selectedVia, intext,
            RMsgTxList.addMessageToList(selectedTo, selectedVia, intext, false, null, 0, null);
            txtMainEntry.setText("");
        } else {
            Main.q.Message(mainpskmailui.getString("mainpskmailui.type_a_mesage_first"), 5);
        }
    }//GEN-LAST:event_bRMsgSendSMSActionPerformed

    private void bRMsgSendPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgSendPosActionPerformed
        String intext = txtMainEntry.getText();
        RMsgUtil.sendPosition(intext);
        txtMainEntry.setText("");
    }//GEN-LAST:event_bRMsgSendPosActionPerformed

    private void bRMsgReqPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgReqPosActionPerformed
        if (selectedTo == "*") {
            //middleToastText("CAN'T Request Positions from \"ALL\"\n\nSelect a single TO destination above");
            Main.q.Message(mainpskmailui.getString("you_must_select_to"), 5);
        } else if (RMsgProcessor.matchMyCallWith(selectedTo, false)) {
            //middleToastText("CAN'T Request Positions from \"YOURSELF\"\n\nSelect another TO destination above");
        } else {
            RMsgTxList.addMessageToList(selectedTo, selectedVia, "*pos?",
                    false, null, 0,
                    null);
        }
    }//GEN-LAST:event_bRMsgReqPosActionPerformed

    private void bRMsgResendLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgResendLastActionPerformed
        if (selectedTo.equals("*") && selectedVia.equals("")) {
            Main.q.Message(mainpskmailui.getString("you_must_select_to_or_via"), 5);
        } else {
            String intext = txtMainEntry.getText();
            if (intext.trim().length() > 0) {
                //Only add a blank if there is a string to send
                intext = " " + intext;
            }
            String toStr = selectedVia.equals("") ? selectedTo : selectedVia;
            RMsgTxList.addMessageToList(toStr, "", "*qtc?" + intext,
                    false, null, 0,
                    null);
            txtMainEntry.setText("");
        }
    }//GEN-LAST:event_bRMsgResendLastActionPerformed

    private void bRMsgManageMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgManageMsgActionPerformed

        try {
            RMsgManageMessages mManager;
            mManager = new RMsgManageMessages();
            mManager.setLocationRelativeTo(null);
            mManager.setVisible(true);
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Error_when_handling_Radio_Messages_Manage_messages"), e, true);
        }

    }//GEN-LAST:event_bRMsgManageMsgActionPerformed

    private void jComboRMsgToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboRMsgToActionPerformed
        if (jComboRMsgTo.getItemCount() > 0) {
            String to = jComboRMsgTo.getSelectedItem().toString();
            int position = jComboRMsgTo.getSelectedIndex();
            if (to.equals("To_ALL")) {
                selectedTo = "*";
                selectedToAlias = ""; //None for ALL
            } else {
                //Save matching destination
                selectedTo = to;
                selectedToAlias = toAliasArray[position];
                if (sendAliasAndDetails) {
                    //Send full details if they exist
                    if (selectedToAlias.length() > 0) {
                        //We have an alias for this entry, send the full details "alias=destination"
                        selectedTo = selectedToAlias;
                    }
                } else {
                    if (selectedToAlias.length() > 0) {
                        //We have an alias for this entry, append an "=" to signify it is an alias
                        selectedTo = to + "=";
                    }
                }
            }
        }
    }//GEN-LAST:event_jComboRMsgToActionPerformed

    private void jComboRMsgViaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboRMsgViaActionPerformed
        if (jComboRMsgVia.getItemCount() > 0) {
            String via = jComboRMsgVia.getSelectedItem().toString();
            int position = jComboRMsgVia.getSelectedIndex();
            if (via.equals("--DIRECT--")) {
                selectedVia = "";
                selectedViaPassword = ""; //None for Direct
            } else {
                selectedVia = via.substring(4); //Remove the "via " prefix
                selectedViaPassword = viaPasswordArray[position];
                selectedViaIotPassword = viaIotPasswordArray[position];
            }
        }
    }//GEN-LAST:event_jComboRMsgViaActionPerformed

    private void jRadBtnAliasOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadBtnAliasOnlyActionPerformed
        sendAliasAndDetails = false;
        //((TextView) v2).setTextColor(getResources().getColor(android.R.color.white));
        //To-do change colour of the TO combo box 
        //Update the data to be sent
        if (selectedToAlias.length() > 0) {
            //We have an alias for this entry, remove anything after the "=" to signify it is an alias with no details
            selectedTo = selectedTo.replaceFirst("=.+$", "=");
        }
    }//GEN-LAST:event_jRadBtnAliasOnlyActionPerformed

    private void jRadBtnAliasAndAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadBtnAliasAndAddressActionPerformed
        sendAliasAndDetails = true;
        //Set the text color of the Spinner's selected view (not a drop down list view)
        //((TextView) v2).setTextColor(Color.RED);
        //To-do change colour of the TO combo box 
        //Update the data to be sent
        if (selectedToAlias.length() > 0) {
            //We have an alias for this entry, send alias=fullDetails
            selectedTo = selectedToAlias;
        }
    }//GEN-LAST:event_jRadBtnAliasAndAddressActionPerformed

    private void bRMsgResendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgResendActionPerformed

        RMsgResendDialog rw = new RMsgResendDialog(this, true);
        rw.setLocationRelativeTo(this);
        rw.setVisible(true);
    }//GEN-LAST:event_bRMsgResendActionPerformed

    private void bContacts1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bContacts1ActionPerformed
        try {
            AddressBook myBook;
            myBook = new AddressBook();
            myBook.setLocationRelativeTo(null);
            myBook.setVisible(true);
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Error_when_handling_AddressBook"), e, true);
        }
    }//GEN-LAST:event_bContacts1ActionPerformed

    private void jRadioButtonAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAcceptActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButtonAcceptActionPerformed

    private void timeSyncMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeSyncMenuItemActionPerformed
        //Request a time synchronization from the selected server
        if (!Main.connected & !Main.Connecting & !Main.bulletinMode & !Main.iacMode) {
            try {
                Main.q.Message(mainpskmailui.getString("send_timesync"), 5);
                Main.q.set_txstatus(TxStatus.TXTimeSync);
                Main.q.send_TimeSync();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_timeSyncMenuItemActionPerformed

    private void serverControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverControlActionPerformed
        if ((selectedTo.equals("*") && !selectedVia.equals("")) || (!selectedTo.equals("*") && selectedVia.equals(""))) {
            ServerControlWindow scw = new ServerControlWindow(this, true);
            scw.setLocationRelativeTo(this);
            scw.setVisible(true);
        } else {
            Main.q.Message(mainpskmailui.getString("you_must_select_to_or_via"), 5);
       }
    }//GEN-LAST:event_serverControlActionPerformed

    private void tabMainStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabMainStateChanged
        //If in RadioMsg tab, grey out the server drop down at the bottom of the screen to avoid confusion
        int index = tabMain.getSelectedIndex();
        if (index == 7) {
            cboServer.setEnabled(false); 
        } else {
            cboServer.setEnabled(true);
        }
    }//GEN-LAST:event_tabMainStateChanged

    private void bRMsgSendInquireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRMsgSendInquireActionPerformed
        if (selectedTo.equals("*") && selectedVia.equals("")) {
            Main.q.Message(mainpskmailui.getString("you_must_select_to_or_via"), 5);
            //middleToastText("CAN'T Request Positions from \"ALL\"\n\nSelect a single TO destination above");
        //} else if (RMsgProcessor.matchMyCallWith(selectedTo, false)) {
            //middleToastText("CAN'T Request Positions from \"YOURSELF\"\n\nSelect another TO destination above");
        } else {
            String toStr = selectedVia.equals("") ? selectedTo : selectedVia;
            //Send Inquire request (of My SNR) to remote station
            RMsgTxList.addMessageToList(toStr, "", "*snr?",
                        false, null, 0, null);
        }
    }//GEN-LAST:event_bRMsgSendInquireActionPerformed

    private void jButtonRunScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunScriptActionPerformed
        //Get script and arguments
        if (Main.connected) {
            String script = txtMainEntry.getText();
            if (script.length() > 0) {
                Main.txText += "~RUN " + script + "\n";
                Main.q.Message(mainpskmailui.getString("Requesting_script_launch") + script, 5);
            } else {
                Main.q.Message(mainpskmailui.getString("Which_script_to_run"), 5);
            }
        } else {
            Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
        }

        //Send to Server for execution
    }//GEN-LAST:event_jButtonRunScriptActionPerformed

    /**
     * Simple message dialog with yes and no button
     *
     * @param heading
     * @param Question
     * @return
     */
    private Integer ShowMessageWithYesNo(String heading, String Question) {
        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                Question,
                heading,
                JOptionPane.YES_NO_OPTION);
        return n;
    }

    /**
     * Delete a file (do ask first)
     *
     * @param fileName
     */
    public static void DeleteFile(String fileName) {
        try {
            // Complete path and name for file to delete
            File target = new File(fileName);

            if (!target.exists()) {
                System.err.println("File " + fileName + " does not exist!");
                return;
            }

            // Oh, yes. We come in peace (shoot to kill)
            target.delete();

        } catch (Exception e) {
            Main.log.writelog("Problem deleting email with filename:" + fileName + ". " + e.getMessage(), e, true);
        }
    }

    /**
     * Get email after double click on header row
     *
     * @param mailstr
     */
    private void GetHeaderEmail(String mailstr) {
        try {
            if (Main.connected) {
                txtMainEntry.setText("");
                Main.sm.sendRead(mailstr);
                Main.q.Message(mainpskmailui.getString("Requesting_email_nr._") + mailstr, 15);
            } else {
                Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 10);
            }
        } catch (Exception e) {
            Main.log.writelog("Issue after doubleclick on mail in header.", e, true);
        }
    }

    /**
     * Open up the mbox containing messages, the Inbox that is.
     */
    private void LoadInbox() {
        try {
            ArrayList<Email> emaillist;
            String[] inboxcolumnNames = {"From", "Subject", "Date", "Size"};
            inboxmodel = new MessageViewTableModel(inboxcolumnNames);
            MessageViewHandler inboxHandler = new MessageViewHandler(Main.homePath + Main.dirPrefix + "Inbox");
            // We will try to show the inbox so update enum
            emailgrid = grid.IN;
            //Selector in the right position for the next click/double click on the list
            lstBoxSelector.setSelectedIndex(1);

            if (inboxHandler.Fetchmbox()) {
                tblInbox.setModel(inboxmodel);
                // Set column width
                SetColumnWidth(2, 200, 400, 200); // Date column
                SetColumnWidth(3, 100, 300, 100); // Size column
                emaillist = inboxHandler.getEmaillist();
                for (int i = 0; i < emaillist.size(); i++) {
                    inboxmodel.addRow(emaillist.get(i));
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Error when showing inbox.", e, true);
        }
    }

    public void refreshInbox() {
        LoadInbox();
    }

    /**
     * Load messages in the outbox folder and display them in table
     */
    private void LoadOutbox() {
        // Local variables
        DataInputStream in;
        BufferedReader br;
        FileInputStream fstream;

        try {
            // Update enum so that we can handle context menu later
            emailgrid = grid.OUT;

            // Get the list of files in the outbox
            File dir = new File(Main.homePath + Main.dirPrefix + "Outbox");
            File[] files = dir.listFiles(); // This filter only returns files
            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }
            };

            // We should now have an array of strings containing the file names
            files = dir.listFiles(fileFilter);
            ArrayList<Email> emaillist;
            String[] outboxcolumnNames = {"To", "Subject", "Date", "Size"};
            outboxmodel = new MessageOutViewTableModel(outboxcolumnNames);
            MessageViewHandler outboxHandler = new MessageViewHandler(files);

            if (outboxHandler.FetchOutbox()) {
                outboxmodel.clear();
                tblInbox.setModel(outboxmodel);
                // Set column width
                SetColumnWidth(2, 200, 400, 200); // Date column
                SetColumnWidth(3, 100, 300, 100); // Size coumn

                emaillist = outboxHandler.getEmaillist();
                for (int i = 0; i < emaillist.size(); i++) {
                    outboxmodel.addRow(emaillist.get(i));
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Error when showing outbox.", e, true);
        } finally {
            //  if (in != null) in.close();
        }
    }

    /**
     * Load sent messages
     */
    private void LoadSent() {
        // Local variables
        DataInputStream in;
        BufferedReader br;
        FileInputStream fstream;

        try {
            // Update enum so that we can handle context menu later
            emailgrid = grid.SENT;

            // Get the list of files in the outbox
            File dir = new File(Main.homePath + Main.dirPrefix + "Sent");
            File[] files = dir.listFiles(); // This filter only returns files
            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }
            };

            // We should now have an array of strings containing the file names
            files = dir.listFiles(fileFilter);
            ArrayList<Email> emaillist;
            String[] outboxcolumnNames = {"To", "Subject", "Date", "Size"};
            outboxmodel = new MessageOutViewTableModel(outboxcolumnNames);
            MessageViewHandler outboxHandler = new MessageViewHandler(files);

            if (outboxHandler.FetchOutbox()) {
                outboxmodel.clear();
                tblInbox.setModel(outboxmodel);
                // Set column width
                SetColumnWidth(2, 200, 400, 200); // Date column
                SetColumnWidth(3, 100, 300, 100); // Size coumn

                emaillist = outboxHandler.getEmaillist();
                for (int i = 0; i < emaillist.size(); i++) {
                    outboxmodel.addRow(emaillist.get(i));
                }
            }
        } catch (Exception e) {
            Main.log.writelog("Error when showing sent emails.", e, true);
        } finally {
            //  if (in != null) in.close();
        }
    }

    /* New mail dialogue
 *
     */
    private void NewMail() {
        NewMailDialog NewDialog;
        Main.q.Message(mainpskmailui.getString("Write_new_email_message"), 5);

        try {
            NewDialog = new NewMailDialog();
            NewDialog.setLocationRelativeTo(null);
            NewDialog.setVisible(true);
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Error_when_handling_about_window."), e, true);
        }
    }

    /**
     * Write an email reply, open the creator form
     *
     * @param to The sender to reply to
     * @param subject The subject from the email.
     */
    public void ReplyMail(String to, String subject) {
        NewMailDialog NewDialog;
        Main.q.Message(mainpskmailui.getString("Write_reply_email_message"), 5);
        String re = mainpskmailui.getString("Reply_short");

        try {
            // Remove confusing characters
            to = to.replaceAll("\"", "");
            NewDialog = new NewMailDialog();
            NewDialog.setLocationRelativeTo(null);
            // Load to and subject
            NewDialog.SetSubjectField(re + subject);
            NewDialog.SetToField(to);
            // Show
            NewDialog.setVisible(true);
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Error_when_handling_about_window."), e, true);
        }
    }

    //Key input processing for custom code of cboServer (server's combo box)
    private void cboServerProcessKeyReleased(KeyEvent event) {

        String server = ((JTextComponent) ((JComboBox) ((Component) event
                .getSource()).getParent()).getEditor()
                .getEditorComponent()).getText();

        //Extract server and possibly password too
        if (server.contains(":")) {
            //We have a typed in server:password combination
            String[] serverArray = server.split(":");
            if (serverArray.length == 2) {
                //Extract both data points and save
                server = serverArray[0];
                //Apply the same format contraints as in the preferences
                server = server.replaceAll("[^a-zA-Z0-9\\/\\-]", "");
                if (server.length() > 0) {
                    Main.q.setServer(server);
                    Main.q.setPassword(serverArray[1]);
                    RigCtrl.Loadfreqs(server);
                }
            } else if (serverArray.length == 1) {
                //We have ":" but no password, set it to blank
                server = serverArray[0];
                //Apply the same format contraints as in the preferences
                server = server.replaceAll("[^a-zA-Z0-9\\/\\-]", "");
                if (server.length() > 0) {
                    Main.q.setServer(server);
                    Main.q.setPassword("");
                    RigCtrl.Loadfreqs(server);
                }
            }
        } else {
            //Server callsign only, no ":", no password
            //Apply the same format contraints as in the preferences
            server = server.replaceAll("[^a-zA-Z0-9\\/\\-]", "");
            if (server.length() > 0) {
                Main.q.setServer(server);
                Main.q.setPassword("");
                RigCtrl.Loadfreqs(server);
            }
        }
    }
 
    public void getcboServer() {
        //We selected an Item OR pressed enter on the combi box after typing data. Check which is which first?
        int index = cboServer.getSelectedIndex();
        if (index != -1) {
            //We have a valid selection event, not an Enter key event after typing a new server's call
            //Get the server from the preferences array (we may have two entries with the same server but different passwords for example)
            if (index < Main.serversArray.length) {
                Main.q.setServer(Main.serversArray[index]);
                Main.q.setPassword(Main.serversPasswordArray[index]);
            }
        }
    }

    public void ForwardMail(String subject, String content) {
        NewMailDialog NewDialog;
        Main.q.Message(mainpskmailui.getString("Write_forward_email_message"), 5);
        String fwd = mainpskmailui.getString("Forward_short");

        try {
            NewDialog = new NewMailDialog();
            NewDialog.setLocationRelativeTo(null);
            // Load to and subject
            NewDialog.SetSubjectField(fwd + subject);
            NewDialog.SetContentField(content);
            // Show
            NewDialog.setVisible(true);
        } catch (Exception e) {
            Main.log.writelog(mainpskmailui.getString("Error_when_handling_about_window."), e, true);
        }
    }

    /* Get mail headers...
 *
     */
    private void QTC() {
        if (Main.connected) {
            String mailnr = "";
            mailnr = Main.sm.getHeaderCount(Main.homePath + Main.dirPrefix + "headers");
            Main.sm.sendQTC(mailnr);
            Main.q.Message(mainpskmailui.getString("Requesting_mail_headers_from_nr._") + mailnr, 5);
        } else {
            Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
        }
    }

    private void DeleteMail() {
        if (Main.connected) {
            String numbers = txtMainEntry.getText();
            if (numbers.length() > 0) {
                Main.sm.sendDelete(numbers);
                Main.q.Message(mainpskmailui.getString("Trying_to_delete_mail_nr._") + numbers, 5);
            } else {
                Main.q.Message(mainpskmailui.getString("Which_mail_numbers?"), 5);
            }
        } else {
            Main.q.Message(mainpskmailui.getString("You_need_to_connect_first..."), 5);
        }
    }

    /**
     * Load the mail headers from the server into the table. That is the headers
     * in the file named headers.
     */
    private void LoadHeaders() {
        String s = "";
        try {
            ArrayList<Email> emaillist;
            String[] headerNames = {"No", "From", "Subject", "Size"};
            headermodel = new MessageHeaderViewTableModel(headerNames);
            // Make sure we store what we will be showing
            emailgrid = grid.HEADERS;

            // Open the file
            FileReader frs = new FileReader(Main.homePath + Main.dirPrefix + "headers");
            BufferedReader br = new BufferedReader(frs);

            // Set the right model for the table
            tblInbox.setModel(headermodel);
            // Set the first column width
            SetColumnWidth(0, 40, 60, 50);
            SetColumnWidth(3, 100, 300, 100);

            while ((s = br.readLine()) != null) {
                if (s.length() > 10) {
                    String[] mystrarr = new String[4];
                    Integer myA = s.indexOf(" ");
                    if (myA < 1) {
                        myA = 1;
                    }
                    mystrarr[0] = s.substring(0, myA).trim();    //Number
                    //From
                    Integer myB = s.indexOf("  ");
                    mystrarr[1] = ConvertToUTF(s.substring(myA + 1, myB).trim());
                    //Subject
                    Integer myC = s.lastIndexOf(" ");
                    mystrarr[2] = ConvertToUTF(s.substring(myB + 2, myC).trim());
                    //Size
                    mystrarr[3] = s.substring(myC, s.length()).trim();
                    headermodel.addRow(mystrarr);
                }
            }
            frs.close();
        } catch (Exception e) {
            Main.log.writelog("Error when showing mail headers.", e, true);
        }
    }

    /**
     * Convert ugly HTML ISO 8859-1 to something UTF like
     *
     * @param in
     * @return
     */
    private String ConvertToUTF(String in) {
        if (in.contains("=?")) {
            // Start and end
            in = in.replace("=??Q?", "");
            in = in.replace("?=", "");
            in = in.replace("=?", "");
            // Common characters
            in = in.replace("&228;", "ä");
            in = in.replace("&229;", "å");
            in = in.replace("&246;", "ö");
            in = in.replace("&197;", "Å");
            in = in.replace("&196;", "Ä");
            in = in.replace("&214;", "Ö");
        }
        return in;
    }
    
    public void addToStatusField(final String statusMessageFromBundle, final int time) {
        Main.q.Message(mainpskmailui.getString(statusMessageFromBundle), time);
    }

    public void refreshHeaders() {
        LoadHeaders();
    }

    /**
     * Refresh whatever grid is now showing
     */
    public void refreshEmailGrid() {
        
        switch (emailgrid) {
            case HEADERS:
                //Pre-select the box slector on the left to ensure consistency of next double click on the list
                lstBoxSelector.setSelectedIndex(0);
                LoadHeaders();
                break;
            case IN:
                lstBoxSelector.setSelectedIndex(1);
                LoadInbox();
                break;
            case OUT:
                lstBoxSelector.setSelectedIndex(2);
                LoadOutbox();
                break;
            case SENT:
                lstBoxSelector.setSelectedIndex(3);
                LoadSent();
                break;
        }
    }

    private String gettime() {
        Calendar cal = Calendar.getInstance();
        int Hour = cal.get(Calendar.HOUR_OF_DAY);
        int Minute = cal.get(Calendar.MINUTE);
        String formathour = "0" + Integer.toString(Hour);
        formathour = formathour.substring(formathour.length() - 2);
        String formatminute = "0" + Integer.toString(Minute);
        formatminute = formatminute.substring(formatminute.length() - 2);
        String lh = formathour + ":" + formatminute + " - ";
        return lh;
    }

    /**
     * Set the column width of the table, catch anything this throws
     *
     * @param column What column number?
     * @param minwidth Minimum width
     * @param maxwidth Maximum width
     * @param prefered Prefered width
     */
    private void SetColumnWidth(Integer column, Integer minwidth, Integer maxwidth, Integer prefered) {
        // Set the first column width
        TableColumn mycol = tblInbox.getColumnModel().getColumn(column);
        mycol.setMinWidth(minwidth);
        mycol.setPreferredWidth(prefered);
        mycol.setMaxWidth(maxwidth);
    }

    /**
     * Get the users current position, either manually entered or gps fed
     *
     * @return longitude as decimal

    private float GetUsersLongitude() {
        try {
            String lonstring = "";

            // Get the GPS position data or the preset data
            if (Main.gpsData.getFixStatus()) {
                if (!Main.haveGPSD) {
                    lonstring = Main.gpsData.getLongitude();
                } else {
                    lonstring = Main.gpsdLongitude;
                }
            } else {
                // Preset data
                lonstring = Main.configuration.getPreference("LONGITUDE");
            }
            Float userlon = Float.parseFloat(lonstring);
            return userlon;
        } catch (Exception ex) {
            Main.log.writelog("Error fetching users position.", ex, true);
            return 0;
        }
    }


    // Get the users current position, either manually entered or gps fed
    // @return latitude as float

    private float GetUsersLatitude() {
        try {
            String latstring = "";

            // Get the GPS position data or the preset data
            if (Main.gpsData.getFixStatus()) {
                if (!Main.haveGPSD) {
                    latstring = Main.gpsData.getLatitude();
                } else {
                    latstring = Main.gpsdLatitude;
                }
            } else {
                // Preset data
                latstring = Main.configuration.getPreference("LATITUDE");
            }
            Float userlat = Float.parseFloat(latstring);
            return userlat;
        } catch (Exception ex) {
            Main.log.writelog("Error fetching users position.", ex, true);
            return 0;
        }
    }

    
    // Make the map center around the users current position
     
    private void CenterMapOnUser() {
        float mylat = 0;
        float mylon = 0;
        // General test stuff for the map. Will replace..
        try {
            mylat = GetUsersLatitude();
            mylon = GetUsersLongitude();
//            mymapcls.setCenterPosition(mylat,mylon);
        } catch (Exception e) {
            Main.log.writelog("Could not center on users position.", e, true);
        }
    }

    private void SetLocalMapFromFile() {
        //       mymapcls.setlocalfileasmap("Images/freemap_world.png");

    }
    */
    
    //Load the GUI table in the Radio Msg tab with the list of messages
    public void loadRadioMsg() {
        int listSize = msgDisplayList.getLength();
        mRadioMSgTblModel = new RMsgTableModel();
        tblRadioMsgs.setModel(mRadioMSgTblModel);
        mRadioMSgTblModel.addColumn("Message");
        tblRadioMsgs.getColumnModel().getColumn(0).setCellRenderer(new RMsgTableRenderer());
        //TableColumn mycol = tblRadioMsgs.getColumnModel().getColumn(0);
        RMsgDisplayItem mDisplayItem;
        //String mMessageStr;
        for (int i = 0; i < listSize; i++) {
            mDisplayItem = msgDisplayList.getItem(i);
            mRadioMSgTblModel.addRow(new Object[]{mDisplayItem});
        }
        //Scroll to bottom of message list
        scrollRadioMsgsTableToLast();
    }
   
    
    void loadRMsgComboBoxes() {
        ArrayList<Contact> contactlist = new ArrayList<Contact>(); // Used to hold all the contacts
        
        //Read all the contacts in the file
        try {
            String contfilename = Main.homePath + Main.dirPrefix + "contacts.csv";
            String linestring;  // Used to hold lines of the files
            File contfile;          // File handle for contacts.csv
            Contact contact;        // A contact object
            contfile = new File(contfilename);

            // First check most common problems
            if (contfile == null) {
                throw new IllegalArgumentException("File should not be null.");
            }
            if (!contfile.exists()) {
                // File did not exist, create it
                contfile.createNewFile();
            }
            if (!contfile.isFile()) {
                throw new IllegalArgumentException("Should not be a directory: " + contfile);
            }

            // We should have a file now, lets fetch stuff
            FileReader fin = new FileReader(contfilename);
            BufferedReader br = new BufferedReader(fin);

            while ((linestring = br.readLine()) != null) {
                // Create another contact object and feed it the csv string
                contact = new Contact();
                contact.LoadCSV(linestring);
                contactlist.add(contact);
            }
            fin.close();
        } catch (Exception e) {
            Main.log.writelog("Could not fetch contact information.", true);
        }

        buildRMsgComboBoxes(contactlist);
    }

    //Build a list of valid Via and To stataions and To mobile and email addresses
    private void buildRMsgComboBoxes(ArrayList<Contact> contactlist) {
        
        //Fill-in spinner for To address
        //String[] toArrayOriginal = ("To_ALL," + Main.configuration.getPreference("TOLIST", "")).split(",");
        String[] toArrayOriginal = new String[contactlist.size() * 3 + 1]; //One for callsign, one for emails and one for cellular numbers
        String[] toAliasArrayOriginal = new String[toArrayOriginal.length];
        //First "to All" entry
        toArrayOriginal[0] = "To_ALL";
        int j = 1;
        for (int i = 0; i < contactlist.size(); i++) {
            Contact mycontact = contactlist.get(i);
            //Add callsign to "TO"
            if (mycontact.getShowInTO().equals("Y") &&
                    (mycontact.getHamCallsign().trim().length() > 0 || 
                    mycontact.getOtherCallsign().trim().length() > 0)) {
                toArrayOriginal[j++] = mycontact.getHamCallsign().trim().length() > 0 ? mycontact.getHamCallsign().trim() : mycontact.getOtherCallsign().trim();
            }
            //Add mobile phone (and possibly alias) to "TO"
            if (mycontact.getShowMobileInTO().equals("Y") && mycontact.getMobilePhone().trim().length() > 0) {
                if (mycontact.getMobilePhoneAlias().trim().length() > 0) {
                    toArrayOriginal[j++] = mycontact.getMobilePhoneAlias().trim() + "=" + mycontact.getMobilePhone().trim();
                } else {
                    toArrayOriginal[j++] = mycontact.getMobilePhone().trim();
                }
            }
            //Add email (and possibly alias) to "TO"
            if (mycontact.getShowEmailInTO().equals("Y") && mycontact.getEmail().trim().length() > 0) {
                if (mycontact.getEmailAlias().trim().length() > 0) {
                     toArrayOriginal[j++] = mycontact.getEmailAlias().trim() + "=" + mycontact.getEmail().trim();
                } else {
                    toArrayOriginal[j++] = mycontact.getEmail().trim();
                }
            }
        }
        //Filter for valid entries only
        int validEntries = 0;
        Pattern toPattern = Pattern.compile("^\\s*([0-9a-zA-Z/\\-_@.+]+)\\s*(=?)\\s*(\\S*)\\s*$");
        for (int i = 0; i < toArrayOriginal.length; i++) {
            if (toArrayOriginal[i] != null) {
                Matcher msc = toPattern.matcher(toArrayOriginal[i]);
                if (msc.find()) {
                    String callSign = "";
                    if (msc.group(1) != null) {
                        callSign = msc.group(1);
                    }
                    String separator = "";
                    if (msc.group(2) != null) {
                        separator = msc.group(2);
                    }
                    String toAlias = "";
                    if (msc.group(3) != null) {
                        toAlias = msc.group(3);
                    }
                    if (!callSign.equals("")) {
                        validEntries++;
                        toArrayOriginal[i] = callSign;
                        if (!separator.equals("") && !toAlias.equals("")) {
                            toAliasArrayOriginal[i] = toAlias;
                        } else {
                            toAliasArrayOriginal[i] = ""; //As it is copied later on
                        }
                    } else {
                        toArrayOriginal[i] = ""; //Blank it out
                        toAliasArrayOriginal[i] = "";
                    }
                }
            } else {
                //Malformed to destination, blank it out too
                toArrayOriginal[i] = "";
                toAliasArrayOriginal[i] = "";
            }
        }
        //Blank first alias as it corresponds to the "ALL" destination
        toAliasArrayOriginal[0] = "";
        //Copy only non null strings to final array
        toArray = new String[validEntries];
        toAliasArray = new String[validEntries];
        j = 0;
        for (int i = 0; i < toArrayOriginal.length; i++) {
            if (toArrayOriginal[i].length() > 0) {
                toArray[j] = toArrayOriginal[i];
                if (toAliasArrayOriginal[i].length() > 0) {
                    toAliasArray[j] = toArrayOriginal[i] + "=" + toAliasArrayOriginal[i];
                } else {
                    toAliasArray[j] = ""; //Must be initialized (non null)
                }
                j++;
            }
        }
        //Load "To" jComboBox
        for (int i=0; i<toArray.length; i++) {
            jComboRMsgTo.addItem(toArray[i]);
        }
        //Fill-in spinner for Via relays, with potentially Access and IOT passwords now
        //String[] viaArrayOriginal = ("--DIRECT--," + Main.configuration.getPreference("RELAYLIST", "")).split(",");
        String[] viaArrayOriginal = new String[contactlist.size() + 1];
        String[] viaPasswordArrayOriginal = new String[viaArrayOriginal.length];
        String[] viaIotPasswordArrayOriginal = new String[viaArrayOriginal.length];
        viaArrayOriginal[0] = "--DIRECT--";
        viaPasswordArrayOriginal[0] = "";
        viaIotPasswordArrayOriginal[0] = "";
        int viaValidEntries = 1;
        //Add new format to allow IOT password to be added like in: relayCallSign:relayAccessPw:relayIOTpw
        //Pattern viaPattern = Pattern.compile("^\\s*([0-9a-zA-Z/\\-_.+]+)\\s*(:?)\\s*(\\S*)\\s*$");
        Pattern viaPattern = Pattern.compile("^\\s*([0-9a-zA-Z/\\-_.+]+)\\s*(:?)\\s*([^\\s:]*)\\s*(:?)\\s*([^\\s:]*)\\s*$");
        //for (int i = 0; i < viaArrayOriginal.length; i++) {
        for (int i = 0; i < contactlist.size(); i++) {
            Contact mycontact = contactlist.get(i);
            String data = "";
            if (mycontact.getShowInVIA().equals("Y") &&
                    (mycontact.getHamCallsign().trim().length() > 0 || 
                    mycontact.getOtherCallsign().trim().length() > 0)) {
                data = mycontact.getHamCallsign().trim().length() > 0 ? mycontact.getHamCallsign().trim() : mycontact.getOtherCallsign().trim();
            }
            //Formats are "vk2eta-1" OR "vk2eta-1:relayPassword" OR "vk2eta-1:relayPassword:IOTpassword OR vk2eta-1::IOTpassword"
             if (mycontact.getIotPassword().trim().length() > 0 ) {
                data +=  ":" + mycontact.getRelayingPassword() + ":" + mycontact.getIotPassword();
            } else if (mycontact.getRelayingPassword().trim().length() > 0 ) {
                data += ":" + mycontact.getRelayingPassword();
            }
            Matcher msc = viaPattern.matcher(data);
            if (msc.find()) {
                String callSign = "";
                if (msc.group(1) != null) callSign = msc.group(1);
                String separator = "";
                if (msc.group(2) != null) separator = msc.group(2);
                String accessPassword = "";
                if (msc.group(3) != null) accessPassword = msc.group(3);
                String IOTaccessPassword = "";
                if (msc.group(5) != null) IOTaccessPassword = msc.group(5);
                if (!callSign.equals("")) {
                    viaValidEntries++;
                    viaArrayOriginal[i + 1] = callSign;
                    if (!accessPassword.equals("") || !IOTaccessPassword.equals("") ) {
                        viaPasswordArrayOriginal[i + 1] = accessPassword;
                        viaIotPasswordArrayOriginal[i + 1] = IOTaccessPassword;
                    } else {
                        viaPasswordArrayOriginal[i + 1] = ""; //As it is copied later on
                        viaIotPasswordArrayOriginal[i + 1] = ""; //As it is copied later on
                    }
                } else {
                    viaArrayOriginal[i + 1] = ""; //Blank it out
                    viaPasswordArrayOriginal[i + 1] = "";
                    viaIotPasswordArrayOriginal[i + 1] = "";
                }
            } else {
                //Malformed to destination, blank it out too
                viaArrayOriginal[i + 1] = "";
                viaPasswordArrayOriginal[i + 1] = "";
                viaIotPasswordArrayOriginal[i + 1] = "";
            }
        }
        //Blank first password as it corresponds to the "--DIRECT--" entry (no relay)
        viaPasswordArrayOriginal[0] = "";
        viaIotPasswordArrayOriginal[0] = "";
        //Copy only non null strings to final array
        viaArray = new String[viaValidEntries];
        viaPasswordArray = new String[viaValidEntries];
        viaIotPasswordArray  = new String[viaValidEntries];
        j = 0;
        for (int i = 0; i < viaArrayOriginal.length; i++) {
            if (viaArrayOriginal[i].length() > 0) {
                if (i > 0) {
                    //After the "--DIRECT--" entry
                    viaArray[j] = "via " + viaArrayOriginal[i];
                    viaPasswordArray[j] = viaPasswordArrayOriginal[i];
                    viaIotPasswordArray[j++] = viaIotPasswordArrayOriginal[i];
                } else {
                    viaArray[j] = viaArrayOriginal[i];
                    viaPasswordArray[j] = ""; //Initialise to empty string, not null element
                    viaIotPasswordArray[j++] = "";
                }
            }
        }
        //Load "Via" jComboBox
        for (int i=0; i<viaArray.length; i++) {
            jComboRMsgVia.addItem(viaArray[i]);
        }
    }

    
    public void refreshRMsgComboBoxes(ArrayList<Contact> contactlist) {

        //Save currently selected values
        String savedTo = selectedTo; //"*" or "alias/value", value = callsign or email or mobile #
        String savedVia = selectedVia;// "--DIRECT--" or callsign
        if (!savedVia.equals("--DIRECT--")) {
            savedVia = "via " + savedVia;
        }
        //Blank the combo boxes
        jComboRMsgTo.removeAllItems(); 
        jComboRMsgVia.removeAllItems();
        //Rebuild in case it changed
        buildRMsgComboBoxes(contactlist);
        //Re-position to top of list if not in list anymore
        boolean found = false;
        for (int i=0; i < toArray.length; i ++) {
            if (toArray[i].equals(savedTo)) {
                found = true;
                jComboRMsgTo.setSelectedIndex(i);
                break;
            }
        }
        if (!found) {
            jComboRMsgTo.setSelectedIndex(0);
        }
        found = false;
        for (int i=0; i < viaArray.length; i ++) {
            if (viaArray[i].equals(savedVia)) {
                found = true;
                jComboRMsgVia.setSelectedIndex(i);
                break;
            }
        }
        if (!found) {
            jComboRMsgVia.setSelectedIndex(0);
        }     
    }
        
    //Scroll the displayed list of Radio Messages to the last one (received or sent)
    public void scrollRadioMsgsTableToLast() {
        //tblRadioMsgs.addComponentListener(new ComponentAdapter() {
        //    public void componentResized(ComponentEvent e) {
        int lastIndex = tblRadioMsgs.getRowCount() - 1;
        tblRadioMsgs.changeSelection(lastIndex, 0, false, false);
        //    }
        //});

    }
    
    //To set/reset the scan checkbox in the RigCtl tab
    public void setScannerCheckbox(final boolean value) {
        
        //Make sure we execute on the UI thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScannerCheckbox.setSelected(value);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainPskmailUi().setVisible(true);
            }
        });
    }
    private javax.swing.ButtonGroup buttonGroup2;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox APRSServerSelect;
    public javax.swing.JCheckBox APRS_IS;
    private javax.swing.JButton AbortButton;
    private javax.swing.JMenuItem Beacon_menu_item;
    private javax.swing.JTextField CPSValue;
    private javax.swing.JButton CQButton;
    public static javax.swing.JTextField ClientFreqTxtfield;
    private javax.swing.JMenuItem Conn_abort;
    private javax.swing.JMenuItem Conn_connect;
    private javax.swing.JMenuItem Conn_send;
    private javax.swing.JButton Downbutton;
    private javax.swing.JButton DownloadButton;
    private javax.swing.JButton EmailSendButton;
    private javax.swing.JButton FileAbortButton;
    private javax.swing.JButton FileConnect;
    private javax.swing.JButton FileReadButton;
    private javax.swing.JButton FileSendButton;
    public javax.swing.JTextArea FilesTxtArea;
    private javax.swing.JMenuItem GetGrib;
    private javax.swing.JMenuItem GetUpdatesmenuItem;
    private javax.swing.JMenuItem Getforecast;
    private javax.swing.JLabel IgateCall;
    private javax.swing.JTextField IgateCallField;
    private javax.swing.JLabel IgateIndicator;
    public javax.swing.JCheckBox IgateSwitch;
    private javax.swing.JTextArea IgateTextArea;
    private javax.swing.JMenuItem Link_menu_item;
    private javax.swing.JButton ListFilesButton;
    private javax.swing.JMenuItem MnuTelnet;
    private javax.swing.JMenuItem Ping_menu_item;
    private javax.swing.JButton PositButton;
    private javax.swing.JMenuItem PrefSaveMenu;
    private javax.swing.JTextField Profile;
    public javax.swing.JProgressBar ProgressBar;
    public javax.swing.JLabel RXlabel;
    private javax.swing.JMenuItem Resetrecord_mnu;
    private javax.swing.JProgressBar RxmodeQuality;
    private javax.swing.JTextField RxmodeTextfield;
    private javax.swing.JLabel SNRlbl;
    private javax.swing.JCheckBox ScannerCheckbox;
    private javax.swing.JButton SendButton;
    private javax.swing.JTextField ServerfreqTxtfield;
    private javax.swing.JButton SetToChannelButton;
    private javax.swing.JTextField Size_indicator;
    public javax.swing.JLabel StatusLabel;
    private javax.swing.JMenuItem Stoptransaction_mnu;
    private javax.swing.JTextField Throughput;
    private javax.swing.JTextField Totalbytes;
    private javax.swing.JMenuItem Twitter_send;
    private javax.swing.JProgressBar TxmodeQuality;
    private javax.swing.JTextField TxmodeTextfield;
    private javax.swing.JButton Upbutton;
    private javax.swing.JButton UpdateButton;
    private javax.swing.JMenuItem Update_server;
    private javax.swing.JMenuItem WWV_menu_item;
    private javax.swing.ButtonGroup aprsscanmonitorgroup;
    private javax.swing.JButton bConnect;
    private javax.swing.JButton bContacts;
    private javax.swing.JButton bContacts1;
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bNewMail;
    private javax.swing.JButton bQTC;
    private javax.swing.JButton bRMsgManageMsg;
    private javax.swing.JButton bRMsgReqPos;
    private javax.swing.JButton bRMsgResend;
    private javax.swing.JButton bRMsgResendLast;
    private javax.swing.JButton bRMsgSendInquire;
    private javax.swing.JButton bRMsgSendPos;
    private javax.swing.JButton bRMsgSendSMS;
    private javax.swing.JButton bSummon;
    private javax.swing.ButtonGroup buttonGroupAlias;
    private javax.swing.ButtonGroup buttonGroupPartialDownloads;
    private javax.swing.JCheckBox cbComp;
    private javax.swing.JComboBox cboAPRS2nd;
    private javax.swing.JComboBox cboAPRSIcon;
    private javax.swing.JComboBox cboBeaconPeriod;
    public javax.swing.JComboBox<String> cboServer;
    private javax.swing.JCheckBox chkAutoLink;
    private javax.swing.JCheckBox chkBeacon;
    private javax.swing.JRadioButtonMenuItem defaultmnu;
    private javax.swing.JTextField freq0;
    private javax.swing.JTextField freq1;
    private javax.swing.JTextField freq2;
    private javax.swing.JTextField freq3;
    private javax.swing.JTextField freq4;
    private javax.swing.JButton jButtonRunScript;
    private javax.swing.JComboBox<String> jComboRMsgTo;
    private javax.swing.JComboBox<String> jComboRMsgVia;
    private javax.swing.JMenuItem jGetIAC;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar3;
    private javax.swing.JMenuItem jMenuQuality;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadBtnAliasAndAddress;
    private javax.swing.JRadioButton jRadBtnAliasOnly;
    public javax.swing.JRadioButtonMenuItem jRadioButtonAccept;
    public javax.swing.JRadioButtonMenuItem jRadioButtonDelete;
    public javax.swing.JRadioButtonMenuItem jRadioButtonReject;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JLabel labelCurrentFreq;
    private javax.swing.JLabel labelServerFreq;
    private javax.swing.JLabel lblAPRSIcon;
    private javax.swing.JLabel lblAutoServer;
    private javax.swing.JLabel lblBSize;
    private javax.swing.JLabel lblCPS;
    private javax.swing.JLabel lblCourse;
    private javax.swing.JLabel lblFixat;
    private javax.swing.JLabel lblIgateStatus;
    private javax.swing.JLabel lblLatitude;
    private javax.swing.JLabel lblLongitude;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblRXMode;
    private javax.swing.JLabel lblRxModeQ;
    private javax.swing.JLabel lblSpeed;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblStatusMsg;
    private javax.swing.JLabel lblTXMode;
    private javax.swing.JLabel lblTXModeQ;
    private javax.swing.JLabel lblThroughput;
    private javax.swing.JLabel lblTotalBytes;
    private javax.swing.JPanel linkstatus;
    private javax.swing.JList lstBoxSelector;
    private javax.swing.JMenuItem menuInquire;
    private javax.swing.JMenuItem menuMessages;
    private javax.swing.JMenuItem mnuAbout2;
    private javax.swing.JMenuItem mnuBulletins2;
    private javax.swing.JMenu mnuClear2;
    private javax.swing.JMenuItem mnuClearInbox;
    private javax.swing.JMenuItem mnuClearOutbox;
    private javax.swing.JMenu mnuConnection;
    private javax.swing.JRadioButtonMenuItem mnuDOMINOEX11;
    private javax.swing.JRadioButtonMenuItem mnuDOMINOEX22;
    private javax.swing.JRadioButtonMenuItem mnuDOMINOEX5;
    private javax.swing.JMenuItem mnuDownloads;
    private javax.swing.JMenuItem mnuEmailOpenGet;
    private javax.swing.JPopupMenu mnuEmailPopup;
    public javax.swing.JMenu mnuFile2;
    private javax.swing.JMenuItem mnuFileList;
    private javax.swing.JMenuItem mnuFqHelp;
    private javax.swing.JMenuItem mnuGetAPRS2;
    private javax.swing.JMenuItem mnuGetPskmailNews2;
    private javax.swing.JMenuItem mnuGetServerfq2;
    private javax.swing.JMenuItem mnuGetTide2;
    private javax.swing.JMenuItem mnuGetTidestations2;
    private javax.swing.JMenuItem mnuGetWebPages2;
    private javax.swing.JPopupMenu mnuHeaders;
    private javax.swing.JMenuItem mnuHeaders2;
    private javax.swing.JMenuItem mnuHeadersFetch;
    private javax.swing.JMenu mnuHelpMain2;
    private javax.swing.JMenu mnuIACcodes;
    private javax.swing.JMenu mnuLink;
    private javax.swing.JRadioButtonMenuItem mnuMFSK16;
    private javax.swing.JRadioButtonMenuItem mnuMFSK32;
    private javax.swing.JRadioButtonMenuItem mnuMailAPRS2;
    private javax.swing.JRadioButtonMenuItem mnuMailScanning;
    private javax.swing.JMenu mnuMbox2;
    private javax.swing.JMenuItem mnuMboxDelete2;
    private javax.swing.JMenuItem mnuMboxList2;
    private javax.swing.JMenuItem mnuMboxRead2;
    private javax.swing.JMenu mnuMode2;
    private javax.swing.JMenuItem mnuModeQSY2;
    private javax.swing.JRadioButtonMenuItem mnuMonitor;
    private javax.swing.JMenuItem mnuMulticast;
    private javax.swing.JPopupMenu mnuOutbox;
    private javax.swing.JMenuItem mnuOutboxDeleteMsg;
    private javax.swing.JMenuItem mnuOutboxOpenMsg;
    private javax.swing.JRadioButtonMenuItem mnuPSK125;
    private javax.swing.JRadioButtonMenuItem mnuPSK125R;
    private javax.swing.JRadioButtonMenuItem mnuPSK250;
    private javax.swing.JRadioButtonMenuItem mnuPSK250R;
    private javax.swing.JRadioButtonMenuItem mnuPSK500;
    private javax.swing.JRadioButtonMenuItem mnuPSK500R;
    private javax.swing.JRadioButtonMenuItem mnuPSK63;
    private javax.swing.JMenuItem mnuPreferences2;
    private javax.swing.JMenu mnuPrefsMain;
    private javax.swing.JMenuItem mnuQuit2;
    private javax.swing.JRadioButtonMenuItem mnuTHOR22;
    private javax.swing.JRadioButtonMenuItem mnuTHOR8;
    private javax.swing.JPopupMenu mnuTerminalPopup;
    private javax.swing.JMenuItem mnuTextSave;
    private javax.swing.JMenuItem mnuUploads;
    private javax.swing.JLabel modelbl;
    private javax.swing.ButtonGroup modemnubuttons;
    private javax.swing.JPanel pnlBeacon;
    private javax.swing.JPanel pnlEmailButtons;
    private javax.swing.JPanel pnlFilesButtons;
    private javax.swing.JPanel pnlFreqs;
    private javax.swing.JPanel pnlGPS;
    private javax.swing.JPanel pnlMainEntry;
    private javax.swing.JPanel pnlModemArq;
    private javax.swing.JPanel pnlRMSgButtons;
    private javax.swing.JPanel pnlStatus;
    private javax.swing.JPanel pnlStatusIndicator;
    private javax.swing.JPanel pnlSummoning;
    private javax.swing.JPanel pnlTerminalButtons;
    private javax.swing.JLabel rigctlactivelbl;
    private javax.swing.JScrollPane scrEmailLeft;
    private javax.swing.JScrollPane scrEmailRight;
    private javax.swing.JScrollPane scrInMsgs;
    private javax.swing.JScrollPane scrRadioMessages;
    private javax.swing.JButton serverControl;
    private javax.swing.JTextArea serverlist;
    private javax.swing.JLabel snLabel;
    private javax.swing.JSpinner spnMinute;
    private javax.swing.JPanel statistics;
    private javax.swing.JPanel tabAPRS;
    private javax.swing.JPanel tabEmail;
    private javax.swing.JPanel tabFiles;
    private javax.swing.JPanel tabIgate;
    private javax.swing.JTabbedPane tabMain;
    private javax.swing.JPanel tabModem;
    private javax.swing.JPanel tabRadioMsg;
    private javax.swing.JPanel tabRigctl;
    private javax.swing.JPanel tabTerminal;
    private javax.swing.JTable tblInbox;
    private javax.swing.JTable tblRadioMsgs;
    private javax.swing.JMenuItem timeSyncMenuItem;
    private javax.swing.JTextField txtCourse;
    private javax.swing.JTextField txtFixTakenAt;
    private javax.swing.JTextArea txtInMsgs;
    private javax.swing.JTextField txtLatitude;
    private javax.swing.JTextPane txtLinkMonitor;
    private javax.swing.JTextField txtLongitude;
    private javax.swing.JTextField txtMainEntry;
    private javax.swing.JTextField txtSpeed;
    private javax.swing.JTextField txtStatus;
    // End of variables declaration//GEN-END:variables

}
