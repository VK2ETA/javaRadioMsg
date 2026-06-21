/*
 * optionsdialog.java  
 *   
 * Copyright (C) 2008 Pär Crusefalk (SM0RWO)  
 * Copyright (C) 2018-2021 Pskmail Server and RadioMsg sections by John Douyere (VK2ETA) 
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

import java.util.ResourceBundle;
import javax.swing.SpinnerNumberModel;
import java.util.ArrayList;
import java.util.ListIterator;
import java.text.NumberFormat;      // For masked edit field for position
import java.util.Locale;            // For format in masked edit
import javax.swing.DefaultComboBoxModel;

    /**
     *
 * @author per
 */
public class OptionsDialog extends javax.swing.JDialog {

    private static final ResourceBundle optionsdialog = ResourceBundle.getBundle("javaradiomsg/optionsdialog");

    private String callsign = "N0CAL";
    private String serverList = "N0CAL";
    private String beaconqrg = "0";
    private String latitude = "0.0";
    private String longitude = "0.0";
    private String blocklength = "5";
    private String password = "";

    private String gpsport = "/dev/ttyS0";
    private String gpsspeed = "4800";
    private boolean gpsenabled = false;
    String enablegps = "";
    String enableaprsserver;

    // Modem connection details
    private String modemip = "localhost";
    private String modemport = "3122";
    private String modempreamble = "";
    private String modempostamble = "";
    public String defmode = Main.defaultTxModem;
    private String storemodem = Main.defaultTxModem;

    private String[] modemarray = {"default", "THOR8", "MFSK16", "DOMINOEX22", "THOR22",
        "MFSK32", "DOMINOEX22", "PSK250R", "PSK500R", "PSK500", "PSK250", "PSK63", "PSK125R",
        "MFSK64", "THOR11", "DOMINOEX5", "CTSTIA"};

    //private Session mysession;
    //private arq myarq;

    //Java APRS server Port
    private String APRSServerPort = "8063";
    private boolean APRSServerEnabled = true;
    private Object cf;

    private int defaultmodemindex = 0;

    private String txtSmsEmailGatewayDomain;
    private String txtSendCellNumAs;
    private String txtGatewayISOCountryCode;
    private String txtDeleteUpTo;
    private String txtDeleteWholeLine;
    private String txtDeleteFrom;
        
    /**
     *
     * @return
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     *
     * @param call
     */
    public void setCallsign(String call) {
        callsign = call;
        this.txtCallsign.setText(call);
    }

    public void setPassword(String pass) {
        password = pass;
        jPasswordField1.setText(pass);
        Main.sessionPasswrd = pass;
    }

    /**
     *
     * @param beacon
     */
    public void setBeaconqrg(String beacon) {
        beaconqrg = beacon;
        this.spinBeaconMinute.setValue(Integer.parseInt(beacon));
    }

    /**
     *
     * @return
     */
    public String getBeaconqrg() {
        return beaconqrg;
    }

    /**
     *
     * @return
     */
    public String getServer() {
        return serverList;
    }

    /**
     *
     * @param call
     */
    public void setServer(String call) {
        serverList = call;
        this.txtLinkto.setText(call);
    }

    /**
     *
     * @param string
     */
    public void setLatitude(String string) {
        latitude = string;
        txtLatitude.setText(string);
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
     * @param string
     */
    public void setLongitude(String string) {
        longitude = string;
        this.txtLongitude.setText(string);
    }

    /**
     *
     * @return
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     *
     * @param string
     */
    /**
     *
     * @return
     */
    public String getBlocklength() {
        return blocklength;
    }

    public void setSeconds() {
        try {
            Object BeaconSecond = spinOffsetSeconds.getValue();
            Main.second = Integer.parseInt(BeaconSecond.toString());
        } catch (NumberFormatException numberFormatException) {
            Main.log.writelog(optionsdialog.getString("PROBLEM WHEN SETTING OFFSET SECONDS VALUE!"), numberFormatException, true);
        }
    }

    public void setDCD() {
        try {
            Object DCDValue = DCDSpinner.getValue();
            Main.DCD = Integer.parseInt(DCDValue.toString());
            Main.MAXDCD = Integer.parseInt(DCDValue.toString());
        } catch (Exception ex) {
            Main.log.writelog(optionsdialog.getString("PROBLEM WHEN SETTING DCD VALUE!"), ex, true);
        }
    }

    public String getModemPreamble() {
        modempreamble = txtModemPreamble.getText();
        return modempreamble;
    }

    public String getModemPostamble() {
        modempostamble = txtModemPostamble.getText();
        return modempostamble;
    }

    public void sendUpdate() {

        String pophost = Main.configuration.getPreference("POPHOST");
        String popuser = Main.configuration.getPreference("POPUSER");
        String poppass = Main.configuration.getPreference("POPPASS");
        String returnaddr = Main.configuration.getPreference("RETURNADDRESS");
        String recinfo = pophost + "," + popuser + "," + poppass
                + "," + returnaddr + ",NONE";

        String record = "~RECx" + Base64Encode.base64Encode(recinfo);
        int eol_loc = -1;
        String frst = null;
        String secnd = null;
        eol_loc = record.indexOf(10);
        if (eol_loc != -1) {
            frst = record.substring(0, eol_loc - 1);
            secnd = record.substring(eol_loc + 1, record.length());
            record = frst + secnd;
        }

        Main.txText += record + "\n";
    }

    public boolean isAPRSServerEnabled() {
        return APRSServerEnabled;
    }

    public void setAPRSServerEnabled(boolean APRSServerEnabled) {
        this.APRSServerEnabled = APRSServerEnabled;
    }

    public String getAPRSServerPort() {
        return APRSServerPort;
    }

    public void setAPRSServerPort(String APRSServerPort) {
        this.APRSServerPort = APRSServerPort;
    }

    /**
     * Ask RXTX to provide serial ports for this platform
     */
    private void getComPorts() {
        GpsSerialPort mySerial;
        ArrayList portList;

        try {
            // Only do this if the port is closed        
            if (!Main.gpsPort.getconnectstate()) {
                mySerial = Main.gpsPort;
                portList = mySerial.getCommports();
                boolean portFound = false;

                // Do not do this if the port is open
                // remove the items first
                this.cboGPSSerialPort.removeAllItems();

                ListIterator<String> li = portList.listIterator();
                while (li.hasNext()) {
                    cboGPSSerialPort.addItem(li.next());
                    portFound = true;
                }
                if (!portFound) {
                    Main.log.writelog(optionsdialog.getString("DID NOT FIND ANY SERIAL PORT, IS RXTX INSTALLED?"), true);
                    // Disable the gps handling
                    chkGPSConnection.setSelected(false);
                    chkGPSConnection.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Main.log.writelog(optionsdialog.getString("PROBLEM FETCHING SERIAL PORTS FROM THE SYSTEM!"), e, true);
        }
    }

    /**
     * Just here to allow save from external button
     */
    public void SaveOptions() {
        this.SaveConfiguration();
    }
    
    public void saveSmsConfig(String txtSmsEmailGatewayDomain,
            String jComboBoxSendCellNumAs,
            String txtGatewayISOCountryCode,
            String jTextDeleteUpTo,
            String jCheckBoxDeleteWholeLine,
            String jTextDeleteFrom) {

        this.txtSmsEmailGatewayDomain = txtSmsEmailGatewayDomain;
        this.txtSendCellNumAs = jComboBoxSendCellNumAs;
        this.txtGatewayISOCountryCode = txtGatewayISOCountryCode;
        this.txtDeleteUpTo = jTextDeleteUpTo;
        this.txtDeleteWholeLine = jCheckBoxDeleteWholeLine;
        this.txtDeleteFrom = jTextDeleteFrom;

    }

    /**
     * Creates new form optionsdialog
     *
     * @param parent
     * @param modal
     */
    public OptionsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        //Hide PERL server options for now. Keep code for possible future use
        tabOptions.remove(1);
        //
        try {
           //Hide Callsign as Server section for now. May delete later on
            jPanel4.setVisible(false);
            
            String storemodem = Main.defaultTxModem;
            //Slows start of dialog
            // = new Session();
            //myarq = new arq();

            // Setup user data tab
            setCallsign(Main.configuration.getPreference("CALL"));
            setPassword(Main.configuration.getPreference("PASSWORD"));
            setServer(Main.configuration.getPreference("SERVER"));
            setBeaconqrg(Main.configuration.getPreference("BEACONQRG"));

            setLatitude(Main.configuration.getPreference("LATITUDE"));
            setLongitude(Main.configuration.getPreference("LONGITUDE"));

            if (Main.configuration.getPreference("GPSD").equals("1")) {
                chkGpsd.setSelected(true);
                Main.wantGpsd = true;
            } else {
                chkGpsd.setSelected(false);
                Main.wantGpsd = false;
            }

            // setup serial port list if gpsd is not running or not used.
            if (!Main.haveGPSD) //    getComPorts();
            // Enable the controls if the gps is not running and gpsd is not running
            {
                if (!Main.haveGPSD) {
                    this.enablegpscontrols(!Main.gpsPort.curstate);
                }
            }

            // IF the gps is already running then just display the settings
            if (!Main.haveGPSD) {
                if (Main.gpsPort.curstate) {
                    this.cboGPSSerialPort.removeAllItems();
                    this.cboGPSSerialPort.addItem(Main.configuration.getPreference("GPSPORT"));
                    this.cboGPSSerialPort.setSelectedItem(Main.configuration.getPreference("GPSPORT"));
                }
            }

            if (!Main.haveGPSD) {
                this.cboGPSSpeed.setSelectedItem(Main.configuration.getPreference("GPSSPEED"));
                enablegps = Main.configuration.getPreference("GPSENABLED");
            }
            if (enablegps.equals(Config.state.yes.toString())) {
                this.chkGPSConnection.setSelected(true);
            } else {
                this.chkGPSConnection.setSelected(false);
            }

            // APRS Server frame
            this.txtAPRSServerPort.setText(Main.configuration.getPreference("APRSSERVERPORT"));
            enableaprsserver = Main.configuration.getPreference("APRSSERVER");
            if (enableaprsserver.equals(Config.state.yes.toString())) {
                this.chkAPRSServer.setSelected(true);
            } else {
                this.chkAPRSServer.setSelected(false);
            }
            // Setup email tab
            this.txtPophost.setText(Main.configuration.getPreference("POPHOST"));
            this.txtPopUser.setText(Main.configuration.getPreference("POPUSER"));
            this.txtPopPassword.setText(Main.configuration.getPreference("POPPASS"));
            this.txtReplyto.setText(Main.configuration.getPreference("RETURNADDRESS"));
            if (Main.configuration.getPreference("COMPRESSED").equals("yes")) {
                this.Compressed.setSelected(true);
            } else {
                this.Compressed.setSelected(false);
            }
                    
            // Setup configuration tab
            String mystr = Main.configuration.getPreference("UIOPTION");
            if (mystr.equals("")) {
                mystr = "Default";
            }
            spinUiOption.setValue(mystr);
            DCDSpinner.setValue(GetSpinValue("DCD", 1));
            spinRetries.setValue(GetSpinValue("RETRIES", 16));
            spinIdleTime.setValue(GetSpinValue("IDLETIME", 15));
            spinTXdelay.setValue(GetSpinValue("TXDELAY", 0));
            spinOffsetMinute.setValue(GetSpinValue("OFFSETMINUTE", 0));
            spinOffsetSeconds.setValue(GetSpinValue("OFFSETSECONDS", 20));

            // Modem connection settings
            this.txtModemIPAddress.setText(Main.configuration.getPreference("MODEMIP", "LOCALHOST"));
            this.txtModemIPPort.setText(Main.configuration.getPreference("MODEMIPPORT", "7322"));
            this.txtModemXMLPort.setText(Main.configuration.getPreference("MODEMXMLPORT", "7362"));
            this.txtModemPostamble.setText(Main.configuration.getPreference("MODEMPOSTAMBLE"));
            this.txtModemPreamble.setText(Main.configuration.getPreference("MODEMPREAMBLE"));
            this.txtFldigipath.setText(Main.configuration.getPreference("FLDIGIAPPLICATIONPATH"));
            spinEveryXHours.setValue(GetSpinValue("EVERYXHOURS", 1));


            if (Main.configuration.getPreference("RIGCTL").equals("yes")) {
                jCheckBoxRigctl.setSelected(true);
                Main.wantRigctl = true;
                if (RigCtrl.opened) {
                    jLabelRigStatus.setText("STATUS: ON");
                }

                RigCtrl.Offset = Main.configuration.getPreference("RIGOFFSET");
                OffsetField.setText(RigCtrl.Offset);
                Main.freqOffset = Integer.parseInt(RigCtrl.Offset);
                RigCtrl.OFF = Main.freqOffset;
                QRG0.setText(Main.configuration.getPreference("QRG0"));
                QRG1.setText(Main.configuration.getPreference("QRG1"));
                QRG2.setText(Main.configuration.getPreference("QRG2"));
                QRG3.setText(Main.configuration.getPreference("QRG3"));
                QRG4.setText(Main.configuration.getPreference("QRG4"));

                if (Main.configuration.getPreference("SCANNER").equals("yes")) {
                    //Main.wantScanner = true;
                    ScannerCheckbox.setSelected(true);
                } else {
                    //Main.wantScanner = false;
                    ScannerCheckbox.setSelected(false);
                }
            }
            Main.defaultTxModem = Main.configuration.getPreference("DEFAULTMODE");
            Main.modesListStr = Main.configuration.getPreference("MODES");

            storemodem = Main.defaultTxModem;
            int i = 0;
//  System.out.println(Main.modes);
//  System.out.println("default=" + storemodem);

            for (i = 0; i < Main.modesListStr.length(); i++) {
                if (Main.modesListStr.substring(i, i + 1).equals("7")) {
                    CB_PSK500.setSelected(true);
                    modemarray[i] = "PSK500";
                } else if (Main.modesListStr.substring(i, i + 1).equals("6")) {
                    CB_PSK500R.setSelected(true);
                    modemarray[i] = "PSK500R";
                } else if (Main.modesListStr.substring(i, i + 1).equals("5")) {
                    CB_PSK250R.setSelected(true);
                    modemarray[i] = "PSK250R";
                } else if (Main.modesListStr.substring(i, i + 1).equals("l")) {
                    CB_DOMEX22.setSelected(true);
                    modemarray[i] = "DOMINOEX22";
                } else if (Main.modesListStr.substring(i, i + 1).equals("4")) {
                    CB_MFSK32.setSelected(true);
                    modemarray[i] = "MFSK32";
                } else if (Main.modesListStr.substring(i, i + 1).equals("3")) {
                    CB_THOR22.setSelected(true);
                    modemarray[i] = "THOR22";
                } else if (Main.modesListStr.substring(i, i + 1).equals("m")) {
                    CB_DOMEX11.setSelected(true);
                    modemarray[i] = "DOMINOEX11";
                } else if (Main.modesListStr.substring(i, i + 1).equals("2")) {
                    CB_MFSK16.setSelected(true);
                    modemarray[i] = "MFSK16";
                } else if (Main.modesListStr.substring(i, i + 1).equals("1")) {
                    CB_THOR8.setSelected(true);
                    modemarray[i] = "THOR8";
                } else if (Main.modesListStr.substring(i, i + 1).equals("8")) {
                    CB_PSK250.setSelected(true);
                    modemarray[i] = "PSK250";
                } else if (Main.modesListStr.substring(i, i + 1).equals("9")) {
//                        CB_PSK125.setSelected(true);
                    modemarray[i] = "PSK125";
                } else if (Main.modesListStr.substring(i, i + 1).equals("b")) {
                    CB_PSK125R.setSelected(true);
                    modemarray[i] = "PSK125R";
                } else if (Main.modesListStr.substring(i, i + 1).equals("d")) {
//                        CB_THOR11.setSelected(true);
                    modemarray[i] = "THOR11";
                } else if (Main.modesListStr.substring(i, i + 1).equals("n")) {
                    CB_DOMEX5.setSelected(true);
                    modemarray[i] = "DOMINOEX5";
                }
                //VK2ETA: Bug? when opening options when connected to server, messes up the modelist or 
                //   the Main.mode string of character coded modes. 
                //   Solution: prevent access to preferences when connected.

                if (Main.configuration.getPreference("LISTENINCWMODE").equals("yes")) {
                    CB_ListenInCwMode.setSelected(true);
                } else {
                    CB_ListenInCwMode.setSelected(false);
                }

                Main.currentModes = modemarray;

                cboModes.removeAllItems();
                String mds = Main.modesListStr;
                char[] mdc;
                mdc = mds.toCharArray();
                String Md = "";
                String MdStringList = "";

                for (int index = 0; index < mdc.length; index++) {
                    switch (mdc[index]) {
                        case 49:
                            Md = "THOR8";
                            break;
                        case 50:
                            Md = "MFSK16";
                            break;
                        case 51:
                            Md = "THOR22";
                            break;
                        case 52:
                            Md = "MFSK32";
                            break;
                        case 53:
                            Md = "PSK250R";
                            break;
                        case 54:
                            Md = "PSK500R";
                            break;
                        case 55:
                            Md = "PSK500";
                            break;
                        case 56:
                            Md = "PSK250";
                            break;
                        case 57:
                            Md = "PSK125";
                            break;
                        case 97:
                            Md = "PSK63";
                            break;
                        case 98:
                            Md = "PSK125R";
                            break;
                        case 99:
                            Md = "MFSK64";
                            break;
                        case 100:
                            Md = "THOR11";
                            break;
                        case 110:
                            Md = "DOMINOEX5";
                            break;
                        case 102:
                            Md = "CTSTIA";
                            break;
                        case 103:
                            Md = "PSK1000";
                            break;
                        case 104:
                            Md = "PSK63RC5";
                            break;
                        case 105:
                            Md = "PSK63RC10";
                            break;
                        case 106:
                            Md = "PSK250RC3";
                            break;
                        case 107:
                            Md = "PSK125RC4";
                            break;
                        case 108:
                            Md = "DOMINOEX22";
                            break;
                        case 109:
                            Md = "DOMINOEX11";
                            break;

                        default:
                            Md = "default";
                    }
                    //Built a comma separated list
                    if (MdStringList.length() > 0) {
                        MdStringList += "," + Md;
                    } else {
                        MdStringList = Md;
                    }
                    //cboModes.addItem(Md);
                }
                //VK2ETA: speeds up display of options
                String [] values = MdStringList.split(",");
                //cboModes = new javax.swing.JComboBox(values);
                //DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<String>(values);
                //cboModes.setModel(model);
                cboModes.setModel(new DefaultComboBoxModel(values));
                
                defmode = storemodem;
                //Set default mode
                for (int j = 0; j < Main.m.smodes.length; j++) {
                    if (Main.m.smodes[j].equals(defmode)) {
                        Main.defaultmode = Main.m.pmodes[j];
                        break;
                    }
                }
                //Set combo box
                //cboModes.setSelectedItem(defmode);
                for (int j = 0; j < values.length; j++) {
                    if (values[j].equals(defmode)) {
                        cboModes.setSelectedIndex(j);
                        break;
                    }
                }               
            }
            
            //Pskmail Server and RadioMsg
            this.txtCallsignAsServer.setText(Main.configuration.getPreference("CALLSIGNASSERVER"));
            this.txtAccessPassword.setText(Main.configuration.getPreference("ACCESSPASSWORD").trim());
            this.txtServerImapHost.setText(Main.configuration.getPreference("SERVERIMAPHOST"));
            this.spinnerImapProtocol.setValue(Main.configuration.getPreference("SERVERIMAPPROTOCOL", "SSL/TLS"));
            this.txtServerImapPort.setText(Main.configuration.getPreference("SERVERIMAPPORT", "993"));
            this.txtServerSmtpHost.setText(Main.configuration.getPreference("SERVERSMTPHOST"));
            this.spinnerSmtpProtocol.setValue(Main.configuration.getPreference("SERVERSMTPPROTOCOL", "STARTTLS"));
            this.txtServerSmtpPort.setText(Main.configuration.getPreference("SERVERSMTPPORT", "587"));
            this.txtServerEmailAddress.setText(Main.configuration.getPreference("SERVEREMAILADDRESS"));
            this.txtServerUserName.setText(Main.configuration.getPreference("SERVERUSERNAME"));
            this.txtServerEmailPassword.setText(Main.configuration.getPreference("SERVERPASSWORD"));
            txtSmsEmailGatewayDomain = Main.configuration.getPreference("SMSEMAILGATEWAY");
            txtSendCellNumAs = Main.configuration.getPreference("SENDCELLULARNUMBERAS", "Local Number");
            txtGatewayISOCountryCode = Main.configuration.getPreference("GATEWAYISOCOUNTRYCODE");
            txtDeleteUpTo = Main.configuration.getPreference("DELETESMSREPLYUPTO");
            txtDeleteWholeLine = Main.configuration.getPreference("DELETESMSREPLYSWHOLELINE", "no");
            txtDeleteFrom = Main.configuration.getPreference("DELETESMSREPLYFROM");
            if (Main.configuration.getPreference("ENABLESERVER").equals("yes")) {
                this.checkboxEnablePskmailServer.setSelected(true);
            } else {
                this.checkboxEnablePskmailServer.setSelected(false);
            }
            if (Main.configuration.getPreference("USEVIRTUALEMAILBOXES").equals("yes")) {
                this.checkboxUseVirtualMailBoxes.setSelected(true);
            } else {
                this.checkboxUseVirtualMailBoxes.setSelected(false);
            }
            if (Main.configuration.getPreference("RELAYOVERRADIO").equals("yes")) {
                this.checkboxRelayOverRadio.setSelected(true);
            } else {
                this.checkboxRelayOverRadio.setSelected(false);
            }
            if (Main.configuration.getPreference("RELAYEMAILS").equals("yes")) {
                this.checkboxRelayEmail.setSelected(true);
            } else {
                this.checkboxRelayEmail.setSelected(false);
            }
            if (Main.configuration.getPreference("RELAYEMAILSIMMEDIATELY").equals("yes")) {
                this.checkboxRelayEmailsImmediately.setSelected(true);
            } else {
                this.checkboxRelayEmailsImmediately.setSelected(false);
            }
            if (Main.configuration.getPreference("RELAYSMSS").equals("yes")) {
                this.checkboxRelaySMSs.setSelected(true);
            } else {
                this.checkboxRelaySMSs.setSelected(false);
            }
            if (Main.configuration.getPreference("RELAYSMSSIMMEDIATELY").equals("yes")) {
                this.checkboxRelaySMSsImmediately.setSelected(true);
            } else {
                this.checkboxRelaySMSsImmediately.setSelected(false);
            }
            spinnerAckPosition.setValue(GetSpinValue("ACKPOSITION", 0));
            spinnerMaxAckPosition.setValue(GetSpinValue("MAXACKS", 0));
            if (Main.configuration.getPreference("ACKWITHRSID").equals("yes")) {
                this.checkboxAckWithRSID.setSelected(true);
            } else {
                this.checkboxAckWithRSID.setSelected(false);
            }
            spinnerDaysToKeepLink.setValue(GetSpinValue("DAYSTOKEEPLINK", 90));
            //Home Assistant IOT settings
            this.txtHAIpAddress.setText(Main.configuration.getPreference("HOMEASSISTANTIPADDRESS"));
            this.txtHALongLivedToken.setText(Main.configuration.getPreference("HOMEASSISTANTLONGLIVEDTOKEN"));
            this.txtHAAccessPassword.setText(Main.configuration.getPreference("IOTACCESSPASSWORD"));
            this.txtEntitiesAliases.setText(Main.configuration.getPreference("IOTENTITIESSHORTCUTS"));
            //Monitoring of files folder .pskmail/RadioMsgSending 
            if (Main.configuration.getPreference("MONITORFILESFOLDER").equals("yes")) {
                this.checkboxMonitorFilesFolder.setSelected(true);
            } else {
                this.checkboxMonitorFilesFolder.setSelected(false);
            }
            
         } catch (Exception ex) {
            Main.log.writelog(optionsdialog.getString("ERROR WHEN FETCHING SETTINGS!"), ex, true);
        }
    }

    /**
     * Get an integer from the config file, setup using default value if missing
     *
     * @param key
     * @param defvalue
     * @return
     */
    Integer GetSpinValue(String key, Integer defvalue) {
        Integer retval = defvalue;
        // Get the possibly saved value
        String mystr = Main.configuration.getPreference(key);
        if (!mystr.equals("")) {
            try {
               retval = Integer.parseInt(mystr);
            } catch (NumberFormatException e) {
                //nothing, already set to defvalue
            }
        }
        return retval;
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

        jLabel14 = new javax.swing.JLabel();
        txtPophost1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        test2 = new javax.swing.JPanel();
        tabOptions = new javax.swing.JTabbedPane();
        pnlUserData = new javax.swing.JPanel();
        test3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtCallsignAsServer = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        lblCallsign = new javax.swing.JLabel();
        txtCallsign = new javax.swing.JTextField();
        lblLinkto = new javax.swing.JLabel();
        txtLinkto = new javax.swing.JTextField();
        lblBeaconQRG = new javax.swing.JLabel();
        spinBeaconMinute = new javax.swing.JSpinner();
        lblLatitude = new javax.swing.JLabel();
        // Setup masked boxes
        NumberFormat nfLat;
        nfLat = NumberFormat.getInstance(Locale.US);
        nfLat.setMinimumFractionDigits(4);
        nfLat.setMaximumFractionDigits(4);
        nfLat.setMaximumIntegerDigits(2);
        nfLat.setMinimumIntegerDigits(2);
        txtLatitude = new javax.swing.JFormattedTextField(nfLat);
        lblLongitude = new javax.swing.JLabel();
        // Setup masked boxes
        NumberFormat nfLonLat;
        nfLonLat = NumberFormat.getInstance(Locale.US);
        nfLonLat.setMinimumFractionDigits(4);
        nfLonLat.setMaximumFractionDigits(4);
        nfLonLat.setMaximumIntegerDigits(3);
        nfLonLat.setMinimumIntegerDigits(3);
        txtLongitude = new javax.swing.JFormattedTextField(nfLonLat);
        bPosConverter = new javax.swing.JButton();
        pnlEmail = new javax.swing.JPanel();
        jPasswordField1 = new javax.swing.JPasswordField();
        lblPassword = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtPophost = new javax.swing.JTextField();
        txtPopUser = new javax.swing.JTextField();
        txtReplyto = new javax.swing.JTextField();
        lblEmailHeader = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtPopPassword = new javax.swing.JPasswordField();
        jLabel20 = new javax.swing.JLabel();
        ServerPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        checkboxEnablePskmailServer = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        txtServerImapHost = new javax.swing.JTextField();
        txtServerSmtpHost = new javax.swing.JTextField();
        txtServerEmailAddress = new javax.swing.JTextField();
        txtServerUserName = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtAccessPassword = new javax.swing.JPasswordField();
        txtServerEmailPassword = new javax.swing.JPasswordField();
        txtServerImapPort = new javax.swing.JTextField();
        txtServerSmtpPort = new javax.swing.JTextField();
        spinnerSmtpProtocol = new javax.swing.JSpinner();
        spinnerImapProtocol = new javax.swing.JSpinner();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        checkboxUseVirtualMailBoxes = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        spinnerDaysToKeepLink = new javax.swing.JSpinner();
        RadioMsgPanel = new javax.swing.JPanel();
        test1 = new javax.swing.JPanel();
        checkboxRelayOverRadio = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        checkboxRelayEmail = new javax.swing.JCheckBox();
        checkboxRelayEmailsImmediately = new javax.swing.JCheckBox();
        jLabel30 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        checkboxRelaySMSs = new javax.swing.JCheckBox();
        checkboxRelaySMSsImmediately = new javax.swing.JCheckBox();
        jButtonConfigSmsGateway = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        spinnerAckPosition = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        spinnerMaxAckPosition = new javax.swing.JSpinner();
        jLabel26 = new javax.swing.JLabel();
        checkboxAckWithRSID = new javax.swing.JCheckBox();
        jLabel38 = new javax.swing.JLabel();
        checkboxMonitorFilesFolder = new javax.swing.JCheckBox();
        pnlConfiguration = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        txtLogFile = new javax.swing.JTextField();
        lblRetries = new javax.swing.JLabel();
        lblIdle = new javax.swing.JLabel();
        lblTXdelay = new javax.swing.JLabel();
        lblOffsetmin = new javax.swing.JLabel();
        spinRetries = new javax.swing.JSpinner();
        spinIdleTime = new javax.swing.JSpinner();
        spinTXdelay = new javax.swing.JSpinner();
        lblSecond = new javax.swing.JLabel();
        spinOffsetMinute = new javax.swing.JSpinner();
        spinOffsetSeconds = new javax.swing.JSpinner();
        DCDSpinner = new javax.swing.JSpinner();
        lblDCD = new javax.swing.JLabel();
        Compressed = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        spinUiOption = new javax.swing.JSpinner();
        pnlGPS = new javax.swing.JPanel();
        frGPS = new javax.swing.JPanel();
        chkGPSConnection = new javax.swing.JCheckBox();
        lblPortscbo = new javax.swing.JLabel();
        lblSpeed = new javax.swing.JLabel();
        cboGPSSpeed = new javax.swing.JComboBox();
        cboGPSSerialPort = new javax.swing.JComboBox<>();
        chkGpsd = new javax.swing.JCheckBox();
        bGetSerPorts = new javax.swing.JButton();
        frAPRSServer = new javax.swing.JPanel();
        chkAPRSServer = new javax.swing.JCheckBox();
        lblPortNumber = new javax.swing.JLabel();
        txtAPRSServerPort = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxRigctl = new javax.swing.JCheckBox();
        jLabelRigStatus = new javax.swing.JLabel();
        QRG0 = new javax.swing.JTextField();
        QRG1 = new javax.swing.JTextField();
        QRG2 = new javax.swing.JTextField();
        QRG3 = new javax.swing.JTextField();
        QRG4 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        OffsetField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        ScannerCheckbox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        CB_PSK500 = new javax.swing.JCheckBox();
        CB_PSK500R = new javax.swing.JCheckBox();
        CB_PSK250 = new javax.swing.JCheckBox();
        CB_PSK250R = new javax.swing.JCheckBox();
        CB_PSK125R = new javax.swing.JCheckBox();
        CB_MFSK32 = new javax.swing.JCheckBox();
        CB_THOR22 = new javax.swing.JCheckBox();
        CB_MFSK16 = new javax.swing.JCheckBox();
        CB_THOR8 = new javax.swing.JCheckBox();
        cboModes = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        CB_DOMEX5 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        CB_DOMEX22 = new javax.swing.JCheckBox();
        CB_DOMEX11 = new javax.swing.JCheckBox();
        CB_ListenInCwMode = new javax.swing.JCheckBox();
        pnlModem = new javax.swing.JPanel();
        lblModemIPAddress = new javax.swing.JLabel();
        txtModemIPAddress = new javax.swing.JTextField();
        lblModemIPPort = new javax.swing.JLabel();
        txtModemIPPort = new javax.swing.JTextField();
        lblModemPreamble = new javax.swing.JLabel();
        txtModemPreamble = new javax.swing.JTextField();
        lblModemPostamble = new javax.swing.JLabel();
        txtModemPostamble = new javax.swing.JTextField();
        txtFldigipath = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        spinEveryXHours = new javax.swing.JSpinner();
        lblHours = new javax.swing.JLabel();
        lblFldigiAutorestart = new javax.swing.JLabel();
        txtModemXMLPort = new javax.swing.JTextField();
        lblModemXmlPort = new javax.swing.JLabel();
        pnlHaIOT = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtHAIpAddress = new javax.swing.JTextField();
        txtHALongLivedToken = new javax.swing.JTextField();
        txtHAAccessPassword = new javax.swing.JPasswordField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtEntitiesAliases = new javax.swing.JTextArea();
        jLabel37 = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        bOK = new javax.swing.JButton();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        bCancel = new javax.swing.JButton();

        jLabel14.setText(optionsdialog.getString("HOSTNAME")); // NOI18N

        txtPophost1.setToolTipText(optionsdialog.getString("HOST NAME THAT EMAIL SHOULD BE FETCHED FROM, LIKE POP.HOME.SE")); // NOI18N
        txtPophost1.setMinimumSize(new java.awt.Dimension(100, 27));
        txtPophost1.setPreferredSize(new java.awt.Dimension(150, 27));

        jButton1.setText("jButton1");

        jButton4.setText("jButton4");

        jButton2.setText("jButton2");

        jButton3.setText("jButton3");

        test2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        test2.setName(""); // NOI18N

        javax.swing.GroupLayout test2Layout = new javax.swing.GroupLayout(test2);
        test2.setLayout(test2Layout);
        test2Layout.setHorizontalGroup(
            test2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 301, Short.MAX_VALUE)
        );
        test2Layout.setVerticalGroup(
            test2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 24, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(470, 420));
        setSize(new java.awt.Dimension(455, 410));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabOptions.setMinimumSize(new java.awt.Dimension(460, 350));
        tabOptions.setPreferredSize(new java.awt.Dimension(460, 340));

        pnlUserData.setLayout(new java.awt.GridBagLayout());

        test3.setName(""); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Server/iGate/Relay information"));
        jPanel4.setEnabled(false);

        txtCallsignAsServer.setToolTipText(optionsdialog.getString("SERVER CALLSIGN")); // NOI18N
        txtCallsignAsServer.setAlignmentX(22.0F);
        txtCallsignAsServer.setEnabled(false);
        txtCallsignAsServer.setMinimumSize(new java.awt.Dimension(100, 27));
        txtCallsignAsServer.setPreferredSize(new java.awt.Dimension(150, 27));
        txtCallsignAsServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCallsignAsServerActionPerformed(evt);
            }
        });

        jLabel22.setText("Callsign as Server");
        jLabel22.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCallsignAsServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCallsignAsServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addGap(0, 0, 0))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(optionsdialog.getString("STATIONCONFIGURATIONPANEL"))); // NOI18N

        lblCallsign.setText(optionsdialog.getString("CALLSIGN")); // NOI18N

        txtCallsign.setToolTipText(optionsdialog.getString("STATION CALLSIGN")); // NOI18N
        txtCallsign.setMinimumSize(new java.awt.Dimension(150, 27));
        txtCallsign.setPreferredSize(new java.awt.Dimension(150, 27));
        txtCallsign.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCallsignFocusLost(evt);
            }
        });
        txtCallsign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCallsignActionPerformed(evt);
            }
        });

        lblLinkto.setText(optionsdialog.getString("LINK TO")); // NOI18N

        txtLinkto.setToolTipText(optionsdialog.getString("ENTER THE CALLSIGN OF A SERVER TO LINK UP WITH")); // NOI18N
        txtLinkto.setMinimumSize(new java.awt.Dimension(150, 27));
        txtLinkto.setPreferredSize(new java.awt.Dimension(150, 27));
        txtLinkto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLinktoActionPerformed(evt);
            }
        });

        lblBeaconQRG.setText(optionsdialog.getString("BEACON QRG NR")); // NOI18N

        spinBeaconMinute.setModel(new SpinnerNumberModel(0,0,4,1));
        spinBeaconMinute.setToolTipText(optionsdialog.getString("THE MINUTE TO TRANSMIT TO A (SCANNING) SERVER, WHEN IS IT LISTENING AT THE CURRENT FQ?")); // NOI18N
        spinBeaconMinute.setMinimumSize(new java.awt.Dimension(45, 28));
        spinBeaconMinute.setPreferredSize(new java.awt.Dimension(50, 28));

        lblLatitude.setText(optionsdialog.getString("LATITUDE")); // NOI18N

        txtLatitude.setToolTipText(optionsdialog.getString("ENTER LATITUDE AS DECIMAL DEGREES WITHOUT N&S. FOR INSTANCE LIKE -59.0010 (SOUTH IS NEGATIVE)")); // NOI18N
        txtLatitude.setMinimumSize(new java.awt.Dimension(150, 27));
        txtLatitude.setPreferredSize(new java.awt.Dimension(150, 27));
        txtLatitude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLatitudeActionPerformed(evt);
            }
        });

        lblLongitude.setText(optionsdialog.getString("LONGITUDE")); // NOI18N

        txtLongitude.setToolTipText(optionsdialog.getString("ENTER LONGITUDE AS DECIMAL DEGREES WITHOUT W&E. FOR INSTANCE LIKE -017.0010 (WEST IS NEGATIVE)")); // NOI18N
        txtLongitude.setMinimumSize(new java.awt.Dimension(150, 27));
        txtLongitude.setPreferredSize(new java.awt.Dimension(150, 27));
        txtLongitude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLongitudeActionPerformed(evt);
            }
        });

        bPosConverter.setText("dd.mm");
        bPosConverter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bPosConverterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCallsign, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLinkto, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblBeaconQRG, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLatitude, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLongitude, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spinBeaconMinute, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCallsign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLongitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtLatitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(bPosConverter))
                    .addComponent(txtLinkto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCallsign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCallsign))
                .addGap(3, 3, 3)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(lblLinkto))
                    .addComponent(txtLinkto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lblBeaconQRG))
                    .addComponent(spinBeaconMinute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(lblLatitude))
                            .addComponent(txtLatitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(lblLongitude))
                            .addComponent(txtLongitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bPosConverter)
                        .addGap(18, 18, 18)))
                .addGap(27, 27, 27))
        );

        javax.swing.GroupLayout test3Layout = new javax.swing.GroupLayout(test3);
        test3.setLayout(test3Layout);
        test3Layout.setHorizontalGroup(
            test3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(test3Layout.createSequentialGroup()
                .addGroup(test3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        test3Layout.setVerticalGroup(
            test3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(test3Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pnlUserData.add(test3, new java.awt.GridBagConstraints());

        tabOptions.addTab("User data", pnlUserData);

        pnlEmail.setEnabled(false);

        jPasswordField1.setToolTipText("Keep empty if not set at the server or email will not work!!");
        jPasswordField1.setPreferredSize(new java.awt.Dimension(120, 27));
        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });

        lblPassword.setText("Session password");

        jLabel7.setText(optionsdialog.getString("HOSTNAME")); // NOI18N

        jLabel9.setText(optionsdialog.getString("PASSWORD")); // NOI18N

        jLabel10.setText(optionsdialog.getString("REPLY TO")); // NOI18N

        txtPophost.setToolTipText(optionsdialog.getString("HOST NAME THAT EMAIL SHOULD BE FETCHED FROM, LIKE POP.HOME.SE")); // NOI18N
        txtPophost.setMinimumSize(new java.awt.Dimension(100, 27));
        txtPophost.setPreferredSize(new java.awt.Dimension(150, 27));
        txtPophost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPophostActionPerformed(evt);
            }
        });

        txtPopUser.setToolTipText(optionsdialog.getString("USER NAME AT THE POP HOST")); // NOI18N
        txtPopUser.setMinimumSize(new java.awt.Dimension(100, 27));
        txtPopUser.setPreferredSize(new java.awt.Dimension(150, 27));
        txtPopUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPopUserActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("javaradiomsg/Bundle"); // NOI18N
        txtReplyto.setToolTipText(bundle.getString("REPLY TO ADDRESS")); // NOI18N
        txtReplyto.setMinimumSize(new java.awt.Dimension(100, 27));
        txtReplyto.setPreferredSize(new java.awt.Dimension(150, 27));
        txtReplyto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReplytoActionPerformed(evt);
            }
        });

        lblEmailHeader.setText(optionsdialog.getString("POP SERVER TO FETCH EMAIL FROM")); // NOI18N

        jLabel21.setText("pop Host");

        jLabel17.setText("Username");

        txtPopPassword.setText("jPasswordField2");
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("javaradiomsg/optionsdialog"); // NOI18N
        txtPopPassword.setToolTipText(bundle1.getString("PASSWORD AT THE POP HOST")); // NOI18N

        jLabel20.setText(optionsdialog.getString("ONLY FOR PERL SERVER")); // NOI18N

        javax.swing.GroupLayout pnlEmailLayout = new javax.swing.GroupLayout(pnlEmail);
        pnlEmail.setLayout(pnlEmailLayout);
        pnlEmailLayout.setHorizontalGroup(
            pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEmailLayout.createSequentialGroup()
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(lblEmailHeader))
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addGap(362, 362, 362)
                        .addComponent(jLabel7))
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel20))
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlEmailLayout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel17)
                                        .addComponent(jLabel9))
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(pnlEmailLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel10)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtPopUser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                            .addComponent(txtPophost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtReplyto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtPopPassword, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblPassword)
                        .addGap(18, 18, 18)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlEmailLayout.setVerticalGroup(
            pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEmailLayout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(31, 31, 31)
                .addComponent(jLabel20)
                .addGap(18, 18, 18)
                .addComponent(lblEmailHeader)
                .addGap(2, 2, 2)
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPophost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(5, 5, 5)
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPopUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(7, 7, 7)
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtPopPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtReplyto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEmailLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(lblPassword))
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        tabOptions.addTab("Email settings", pnlEmail);

        ServerPanel.setPreferredSize(new java.awt.Dimension(460, 250));

        jPanel11.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel11.setPreferredSize(new java.awt.Dimension(430, 400));

        checkboxEnablePskmailServer.setText("Enable Pskmail Server");
        checkboxEnablePskmailServer.setToolTipText("Listen for Client's connections and provide Pskmail server's functions");
        checkboxEnablePskmailServer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkboxEnablePskmailServer.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkboxEnablePskmailServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxEnablePskmailServerActionPerformed(evt);
            }
        });

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("SERVER RADIOMSG RELAY SECURITY AND EMAIL DATA"))); // NOI18N

        jLabel27.setText("smtp Host");

        txtServerImapHost.setToolTipText(optionsdialog.getString("SERVER IMAP HOST")); // NOI18N
        txtServerImapHost.setAlignmentX(22.0F);
        txtServerImapHost.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerImapHost.setPreferredSize(new java.awt.Dimension(150, 27));
        txtServerImapHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServerImapHostActionPerformed(evt);
            }
        });

        txtServerSmtpHost.setToolTipText(optionsdialog.getString("SERVER SMTP HOST")); // NOI18N
        txtServerSmtpHost.setAlignmentX(22.0F);
        txtServerSmtpHost.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerSmtpHost.setPreferredSize(new java.awt.Dimension(150, 27));
        txtServerSmtpHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServerSmtpHostActionPerformed(evt);
            }
        });

        txtServerEmailAddress.setToolTipText(optionsdialog.getString("SERVER EMAIL ADDRESS")); // NOI18N
        txtServerEmailAddress.setAlignmentX(22.0F);
        txtServerEmailAddress.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerEmailAddress.setPreferredSize(new java.awt.Dimension(150, 27));

        txtServerUserName.setToolTipText(optionsdialog.getString("SERVER USERNAME")); // NOI18N
        txtServerUserName.setAlignmentX(22.0F);
        txtServerUserName.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerUserName.setPreferredSize(new java.awt.Dimension(150, 27));
        txtServerUserName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServerUserNameActionPerformed(evt);
            }
        });

        jLabel31.setText("imap Host");

        jLabel32.setText("E-mail Address");

        jLabel33.setText("Username");

        jLabel34.setText("Password");

        jLabel18.setText(bundle1.getString("SECURITY PASSWORD")); // NOI18N

        txtAccessPassword.setToolTipText(bundle1.getString("ACCESS PASSWORD TIP")); // NOI18N
        txtAccessPassword.setMinimumSize(new java.awt.Dimension(4, 27));
        txtAccessPassword.setName(""); // NOI18N
        txtAccessPassword.setPreferredSize(new java.awt.Dimension(4, 27));

        txtServerEmailPassword.setToolTipText(bundle1.getString("SERVER EMAIL PASSWORD")); // NOI18N
        txtServerEmailPassword.setMinimumSize(new java.awt.Dimension(4, 27));
        txtServerEmailPassword.setPreferredSize(new java.awt.Dimension(4, 27));

        txtServerImapPort.setToolTipText(optionsdialog.getString("SERVER IMAP PORT")); // NOI18N
        txtServerImapPort.setAlignmentX(22.0F);
        txtServerImapPort.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerImapPort.setPreferredSize(new java.awt.Dimension(150, 27));
        txtServerImapPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServerImapPortActionPerformed(evt);
            }
        });

        txtServerSmtpPort.setToolTipText(optionsdialog.getString("SERVER SMTP PORT")); // NOI18N
        txtServerSmtpPort.setAlignmentX(22.0F);
        txtServerSmtpPort.setMinimumSize(new java.awt.Dimension(100, 27));
        txtServerSmtpPort.setPreferredSize(new java.awt.Dimension(150, 27));
        txtServerSmtpPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServerSmtpPortActionPerformed(evt);
            }
        });

        spinnerSmtpProtocol.setModel(new javax.swing.SpinnerListModel(new String[] {"NONE", "STARTTLS", "SSL/TLS"}));
        spinnerSmtpProtocol.setToolTipText("The security protocol used to establish a link with the SMTP server");
        spinnerSmtpProtocol.setMinimumSize(new java.awt.Dimension(32, 27));
        spinnerSmtpProtocol.setName(""); // NOI18N
        spinnerSmtpProtocol.setPreferredSize(new java.awt.Dimension(32, 27));

        spinnerImapProtocol.setModel(new javax.swing.SpinnerListModel(new String[] {"NONE", "STARTTLS", "SSL/TLS"}));
        spinnerImapProtocol.setToolTipText("The security protocol used to establish a link with the IMAP server");
        spinnerImapProtocol.setMinimumSize(new java.awt.Dimension(32, 27));
        spinnerImapProtocol.setName(""); // NOI18N
        spinnerImapProtocol.setPreferredSize(new java.awt.Dimension(32, 27));

        jLabel35.setText("Protocol");

        jLabel36.setText("Port");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtServerUserName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtServerEmailAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtServerEmailPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(48, 48, 48))
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(txtAccessPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addGap(3, 3, 3)
                                .addComponent(txtServerImapHost, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addGap(3, 3, 3)
                                .addComponent(txtServerSmtpHost, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spinnerSmtpProtocol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spinnerImapProtocol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtServerImapPort, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtServerSmtpPort, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel18))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(txtAccessPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(jLabel36))
                .addGap(3, 3, 3)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtServerImapHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31)
                    .addComponent(txtServerImapPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerImapProtocol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtServerSmtpHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(txtServerSmtpPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerSmtpProtocol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtServerEmailAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtServerUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(txtServerEmailPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkboxUseVirtualMailBoxes.setText("Virtual email accounts");
        checkboxUseVirtualMailBoxes.setToolTipText("Share the email account by linking email addresses with the clients' callsigns. De-select for single callsign use.");
        checkboxUseVirtualMailBoxes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkboxUseVirtualMailBoxes.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkboxUseVirtualMailBoxes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxUseVirtualMailBoxesActionPerformed(evt);
            }
        });

        jLabel23.setText(optionsdialog.getString("DAYSTOKEEPLINKSFORLABEL")); // NOI18N

        spinnerDaysToKeepLink.setModel(new javax.swing.SpinnerNumberModel(90, 0, null, 1));
        spinnerDaysToKeepLink.setToolTipText(optionsdialog.getString("DAYSTOKEEPLINKFOR")); // NOI18N
        spinnerDaysToKeepLink.setMinimumSize(new java.awt.Dimension(32, 25));
        spinnerDaysToKeepLink.setName(""); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(checkboxUseVirtualMailBoxes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel23))
                    .addComponent(checkboxEnablePskmailServer, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerDaysToKeepLink, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addComponent(checkboxEnablePskmailServer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerDaysToKeepLink, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(jLabel23)
                    .addComponent(checkboxUseVirtualMailBoxes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(107, Short.MAX_VALUE))
        );

        ServerPanel.add(jPanel11);

        tabOptions.addTab("Server", ServerPanel);

        RadioMsgPanel.setMinimumSize(new java.awt.Dimension(600, 282));
        RadioMsgPanel.setPreferredSize(new java.awt.Dimension(600, 282));

        test1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        test1.setName(""); // NOI18N

        checkboxRelayOverRadio.setText(optionsdialog.getString("RELAYRADIOMESSAGES")); // NOI18N
        checkboxRelayOverRadio.setToolTipText(optionsdialog.getString("RELAYRADIOMESSAGESTIP")); // NOI18N
        checkboxRelayOverRadio.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        checkboxRelayOverRadio.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkboxRelayOverRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxRelayOverRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout test1Layout = new javax.swing.GroupLayout(test1);
        test1.setLayout(test1Layout);
        test1Layout.setHorizontalGroup(
            test1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(test1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkboxRelayOverRadio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        test1Layout.setVerticalGroup(
            test1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, test1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(checkboxRelayOverRadio))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        checkboxRelayEmail.setText(optionsdialog.getString("RELAYEMAILS")); // NOI18N
        checkboxRelayEmail.setToolTipText(optionsdialog.getString("RELAYEMAILSTIP")); // NOI18N
        checkboxRelayEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxRelayEmailActionPerformed(evt);
            }
        });

        checkboxRelayEmailsImmediately.setText(optionsdialog.getString("RELAYEMAILSIMMEDIATELY")); // NOI18N
        checkboxRelayEmailsImmediately.setToolTipText(optionsdialog.getString("RELAYEMAILSIMMEDIATELYTIP")); // NOI18N
        checkboxRelayEmailsImmediately.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxRelayEmailsImmediatelyActionPerformed(evt);
            }
        });

        jLabel30.setText(optionsdialog.getString("REQUIRESEMAILDATAINSERVERTAB")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(checkboxRelayEmail)
                        .addGap(34, 34, 34)
                        .addComponent(checkboxRelayEmailsImmediately))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel30)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkboxRelayEmail)
                    .addComponent(checkboxRelayEmailsImmediately))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel30)
                .addGap(5, 5, 5))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        checkboxRelaySMSs.setText(optionsdialog.getString("RELAYSMSS")); // NOI18N
        checkboxRelaySMSs.setToolTipText("Relay Radio Messages as Cellular SMS and relay SMSs as Radio Messages");

        checkboxRelaySMSsImmediately.setText(optionsdialog.getString("RELAYSMSIMMEDIATELYWHENRECEIVED")); // NOI18N
        checkboxRelaySMSsImmediately.setToolTipText(optionsdialog.getString("RELAYSMSSIMMEDIATELYTIP")); // NOI18N
        checkboxRelaySMSsImmediately.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxRelaySMSsImmediatelyActionPerformed(evt);
            }
        });

        jButtonConfigSmsGateway.setText(optionsdialog.getString("CONFIGURESMSGATEWAY")); // NOI18N
        jButtonConfigSmsGateway.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigSmsGatewayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(checkboxRelaySMSs)
                        .addGap(48, 48, 48)
                        .addComponent(checkboxRelaySMSsImmediately))
                    .addComponent(jButtonConfigSmsGateway))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkboxRelaySMSs)
                    .addComponent(checkboxRelaySMSsImmediately))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonConfigSmsGateway)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        spinnerAckPosition.setModel(new javax.swing.SpinnerNumberModel(0, 0, 8, 1));
        spinnerAckPosition.setToolTipText(optionsdialog.getString("ACKPOSITIONTIP")); // NOI18N
        spinnerAckPosition.setMinimumSize(new java.awt.Dimension(32, 25));
        spinnerAckPosition.setName(""); // NOI18N

        jLabel24.setText(optionsdialog.getString("ACKPOSITION")); // NOI18N

        jLabel25.setText(optionsdialog.getString("MAXACKPOSITION")); // NOI18N

        spinnerMaxAckPosition.setModel(new javax.swing.SpinnerNumberModel(0, 0, 8, 1));
        spinnerMaxAckPosition.setToolTipText(optionsdialog.getString("MAXACKPOSITIONTIP")); // NOI18N
        spinnerMaxAckPosition.setMinimumSize(new java.awt.Dimension(32, 25));
        spinnerMaxAckPosition.setName(""); // NOI18N

        jLabel26.setText(optionsdialog.getString("ACKNOWLEDGEWITHRSID")); // NOI18N
        jLabel26.setToolTipText("");

        checkboxAckWithRSID.setToolTipText(optionsdialog.getString("ACKWITHRSIDTOOLTIP")); // NOI18N

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spinnerAckPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkboxAckWithRSID))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spinnerMaxAckPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(spinnerAckPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(spinnerMaxAckPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel26)
                    .addComponent(checkboxAckWithRSID))
                .addGap(12, 12, 12))
        );

        jLabel38.setText(optionsdialog.getString("lblMonitorFolder")); // NOI18N

        checkboxMonitorFilesFolder.setToolTipText(optionsdialog.getString("TOOLTIPMONITORFILESFOLDER")); // NOI18N
        checkboxMonitorFilesFolder.setMargin(new java.awt.Insets(0, 2, 2, 2));

        javax.swing.GroupLayout RadioMsgPanelLayout = new javax.swing.GroupLayout(RadioMsgPanel);
        RadioMsgPanel.setLayout(RadioMsgPanelLayout);
        RadioMsgPanelLayout.setHorizontalGroup(
            RadioMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RadioMsgPanelLayout.createSequentialGroup()
                .addGroup(RadioMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(test1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(RadioMsgPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel38)
                        .addGap(78, 78, 78)
                        .addComponent(checkboxMonitorFilesFolder)))
                .addContainerGap(215, Short.MAX_VALUE))
            .addGroup(RadioMsgPanelLayout.createSequentialGroup()
                .addGroup(RadioMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        RadioMsgPanelLayout.setVerticalGroup(
            RadioMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RadioMsgPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(test1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(RadioMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkboxMonitorFilesFolder)
                    .addComponent(jLabel38))
                .addGap(3, 3, 3)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabOptions.addTab("RadioMsg", RadioMsgPanel);

        pnlConfiguration.setLayout(new java.awt.GridBagLayout());

        jLabel12.setText(optionsdialog.getString("LOG FILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(jLabel12, gridBagConstraints);

        txtLogFile.setText("Client.log");
        txtLogFile.setMinimumSize(new java.awt.Dimension(150, 27));
        txtLogFile.setPreferredSize(new java.awt.Dimension(200, 27));
        txtLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLogFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlConfiguration.add(txtLogFile, gridBagConstraints);

        lblRetries.setText(optionsdialog.getString("MAX RETRIES")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(lblRetries, gridBagConstraints);

        lblIdle.setText(optionsdialog.getString("IDLE TIME")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(lblIdle, gridBagConstraints);

        lblTXdelay.setText(optionsdialog.getString("TX DELAY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(lblTXdelay, gridBagConstraints);

        lblOffsetmin.setText(optionsdialog.getString("OFFSET MINUTE")); // NOI18N
        lblOffsetmin.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(lblOffsetmin, gridBagConstraints);

        spinRetries.setModel(new SpinnerNumberModel(16, 5, 50, 1));
        spinRetries.setToolTipText(bundle1.getString("MAX RETRIES TIP")); // NOI18N
        spinRetries.setMinimumSize(new java.awt.Dimension(45, 24));
        spinRetries.setPreferredSize(new java.awt.Dimension(50, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlConfiguration.add(spinRetries, gridBagConstraints);

        spinIdleTime.setModel(new SpinnerNumberModel(120, 0, 1800, 30));
        spinIdleTime.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1200, 1));
        spinIdleTime.setToolTipText(bundle1.getString("IDLE TIME TIP")); // NOI18N
        spinIdleTime.setMinimumSize(new java.awt.Dimension(45, 24));
        spinIdleTime.setPreferredSize(new java.awt.Dimension(50, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlConfiguration.add(spinIdleTime, gridBagConstraints);

        spinTXdelay.setModel(new SpinnerNumberModel(0, 0, 3, 1));
        spinTXdelay.setModel(new javax.swing.SpinnerNumberModel(1, 0, 5, 1));
        spinTXdelay.setToolTipText(bundle1.getString("TX DELAY TIP")); // NOI18N
        spinTXdelay.setMinimumSize(new java.awt.Dimension(45, 24));
        spinTXdelay.setPreferredSize(new java.awt.Dimension(50, 24));
        spinTXdelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinTXdelayStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlConfiguration.add(spinTXdelay, gridBagConstraints);

        lblSecond.setText(optionsdialog.getString("SECOND")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        pnlConfiguration.add(lblSecond, gridBagConstraints);

        spinOffsetMinute.setModel(new SpinnerNumberModel(0, 0, 4, 1));
        spinOffsetMinute.setEnabled(false);
        spinOffsetMinute.setMinimumSize(new java.awt.Dimension(45, 24));
        spinOffsetMinute.setPreferredSize(new java.awt.Dimension(50, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlConfiguration.add(spinOffsetMinute, gridBagConstraints);

        spinOffsetSeconds.setModel(new SpinnerNumberModel(0, 0, 50, 1));
        spinOffsetSeconds.setToolTipText(optionsdialog.getString("YOU CAN FINE TUNE THE TIME YOUR BEACON IS SENT BY CHANGING THIS VALUE")); // NOI18N
        spinOffsetSeconds.setMinimumSize(new java.awt.Dimension(45, 24));
        spinOffsetSeconds.setPreferredSize(new java.awt.Dimension(50, 24));
        spinOffsetSeconds.setValue(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlConfiguration.add(spinOffsetSeconds, gridBagConstraints);
        spinOffsetSeconds.setModel(new SpinnerNumberModel(30, 0, 50, 10));

        DCDSpinner.setModel(new SpinnerNumberModel(2, 0, 10, 1));
        DCDSpinner.setToolTipText(optionsdialog.getString("THE DCD HANG TIME SETS THE SENSITIVITY OF THE RX TO NOISE ON THE CHANNEL")); // NOI18N
        DCDSpinner.setMinimumSize(new java.awt.Dimension(45, 24));
        DCDSpinner.setPreferredSize(new java.awt.Dimension(50, 24));
        DCDSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                DCDSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlConfiguration.add(DCDSpinner, gridBagConstraints);
        DCDSpinner.setModel(new SpinnerNumberModel(3,0,9,1));

        lblDCD.setText(optionsdialog.getString("DCD")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        pnlConfiguration.add(lblDCD, gridBagConstraints);

        Compressed.setText(optionsdialog.getString("COMPRESSED OTA")); // NOI18N
        Compressed.setToolTipText(optionsdialog.getString("COMPRESS EMAIL DURING TRANSFER OVER THE AIR")); // NOI18N
        Compressed.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        Compressed.setBorderPainted(true);
        Compressed.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Compressed.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        Compressed.setMargin(new java.awt.Insets(10, 10, 2, 2));
        Compressed.setMaximumSize(new java.awt.Dimension(170, 25));
        Compressed.setMinimumSize(new java.awt.Dimension(170, 25));
        Compressed.setPreferredSize(new java.awt.Dimension(170, 25));
        Compressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CompressedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(30, 0, 0, 0);
        pnlConfiguration.add(Compressed, gridBagConstraints);

        jLabel8.setText(optionsdialog.getString("USER INTERFACE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        pnlConfiguration.add(jLabel8, gridBagConstraints);

        spinUiOption.setModel(new javax.swing.SpinnerListModel(new String[] {"Default", "RadioMsg"}));
        spinUiOption.setPreferredSize(new java.awt.Dimension(90, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        pnlConfiguration.add(spinUiOption, gridBagConstraints);

        tabOptions.addTab("Configuration", pnlConfiguration);

        pnlGPS.setLayout(new java.awt.GridBagLayout());

        frGPS.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS settings"));
        frGPS.setLayout(new java.awt.GridBagLayout());

        chkGPSConnection.setText(optionsdialog.getString("GPS IS CONNECTED")); // NOI18N
        chkGPSConnection.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkGPSConnectionStateChanged(evt);
            }
        });
        chkGPSConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGPSConnectionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        frGPS.add(chkGPSConnection, gridBagConstraints);

        lblPortscbo.setText(optionsdialog.getString("SERIAL PORT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        frGPS.add(lblPortscbo, gridBagConstraints);

        lblSpeed.setText(optionsdialog.getString("SPEED")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        frGPS.add(lblSpeed, gridBagConstraints);

        cboGPSSpeed.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));
        cboGPSSpeed.setMinimumSize(new java.awt.Dimension(150, 27));
        cboGPSSpeed.setPreferredSize(new java.awt.Dimension(150, 27));
        cboGPSSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboGPSSpeedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        frGPS.add(cboGPSSpeed, gridBagConstraints);

        cboGPSSerialPort.setEditable(true);
        cboGPSSerialPort.setMinimumSize(new java.awt.Dimension(150, 27));
        cboGPSSerialPort.setPreferredSize(new java.awt.Dimension(150, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        frGPS.add(cboGPSSerialPort, gridBagConstraints);

        chkGpsd.setText(optionsdialog.getString("USE GPSD")); // NOI18N
        chkGpsd.setToolTipText(optionsdialog.getString("GPSD IS MAI")); // NOI18N
        frGPS.add(chkGpsd, new java.awt.GridBagConstraints());

        bGetSerPorts.setText(optionsdialog.getString("GET PORTS")); // NOI18N
        bGetSerPorts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bGetSerPortsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        frGPS.add(bGetSerPorts, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        pnlGPS.add(frGPS, gridBagConstraints);
        frGPS.getAccessibleContext().setAccessibleName(bundle1.getString("optionsdialog.frGPS.AccessibleContext.accessibleName")); // NOI18N

        frAPRSServer.setBorder(javax.swing.BorderFactory.createTitledBorder("APRS Server port"));
        frAPRSServer.setLayout(new java.awt.GridBagLayout());

        chkAPRSServer.setText(optionsdialog.getString("ENABLED")); // NOI18N
        chkAPRSServer.setToolTipText(optionsdialog.getString("RESTART MAY BE NEEDED IF ALTERED")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        frAPRSServer.add(chkAPRSServer, gridBagConstraints);

        lblPortNumber.setText(optionsdialog.getString("PORT NUMBER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        frAPRSServer.add(lblPortNumber, gridBagConstraints);

        txtAPRSServerPort.setText("8063");
        txtAPRSServerPort.setMinimumSize(new java.awt.Dimension(50, 28));
        txtAPRSServerPort.setPreferredSize(new java.awt.Dimension(50, 28));
        frAPRSServer.add(txtAPRSServerPort, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        pnlGPS.add(frAPRSServer, gridBagConstraints);

        tabOptions.addTab("Devices", pnlGPS);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jCheckBoxRigctl.setText(optionsdialog.getString("USE RIGCTL")); // NOI18N
        jCheckBoxRigctl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRigctlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 13, 0);
        jPanel1.add(jCheckBoxRigctl, gridBagConstraints);

        jLabelRigStatus.setText(optionsdialog.getString("STATUS: OFF")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 22, 11);
        jPanel1.add(jLabelRigStatus, gridBagConstraints);

        QRG0.setText("10148000");
        QRG0.setToolTipText("Frequency minute 0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 2);
        jPanel1.add(QRG0, gridBagConstraints);

        QRG1.setText("10148000");
        QRG1.setToolTipText("Frequency minute 1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        jPanel1.add(QRG1, gridBagConstraints);

        QRG2.setText("10148000");
        QRG2.setToolTipText("Frequency minute 2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 1);
        jPanel1.add(QRG2, gridBagConstraints);

        QRG3.setText("10148000");
        QRG3.setToolTipText("Frequency minute 3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 1);
        jPanel1.add(QRG3, gridBagConstraints);

        QRG4.setText("10148000");
        QRG4.setToolTipText("Frequency minute 4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 0, 0);
        jPanel1.add(QRG4, gridBagConstraints);

        jLabel1.setText(optionsdialog.getString("CHANNELS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 28;
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.insets = new java.awt.Insets(19, 35, 0, 29);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(20, 29, 0, 33);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setText("4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(20, 28, 0, 27);
        jPanel1.add(jLabel6, gridBagConstraints);

        OffsetField.setText("1000");
        OffsetField.setToolTipText("Rig Offset (Filter Cernter): frequency = dial + offset\");");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(11, 31, 11, 0);
        jPanel1.add(OffsetField, gridBagConstraints);

        jLabel11.setText(optionsdialog.getString("OFFSET (HZ)")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel1.add(jLabel11, gridBagConstraints);

        ScannerCheckbox.setText(optionsdialog.getString("SCAN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 8, 0);
        jPanel1.add(ScannerCheckbox, gridBagConstraints);

        tabOptions.addTab("Rig", jPanel1);

        CB_PSK500.setForeground(new java.awt.Color(51, 0, 204));
        CB_PSK500.setText("PSK500");
        CB_PSK500.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_PSK500ActionPerformed(evt);
            }
        });

        CB_PSK500R.setForeground(new java.awt.Color(51, 0, 204));
        CB_PSK500R.setText("PSK500R");
        CB_PSK500R.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_PSK500RActionPerformed(evt);
            }
        });

        CB_PSK250.setForeground(new java.awt.Color(0, 153, 0));
        CB_PSK250.setText("PSK250");
        CB_PSK250.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_PSK250ActionPerformed(evt);
            }
        });

        CB_PSK250R.setForeground(new java.awt.Color(0, 153, 0));
        CB_PSK250R.setText("PSK250R");
        CB_PSK250R.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_PSK250RActionPerformed(evt);
            }
        });

        CB_PSK125R.setForeground(new java.awt.Color(0, 153, 0));
        CB_PSK125R.setText("PSK125R");
        CB_PSK125R.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_PSK125RActionPerformed(evt);
            }
        });

        CB_MFSK32.setForeground(new java.awt.Color(0, 153, 0));
        CB_MFSK32.setText("MFSK32");
        CB_MFSK32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_MFSK32ActionPerformed(evt);
            }
        });

        CB_THOR22.setForeground(new java.awt.Color(0, 153, 0));
        CB_THOR22.setText("THOR22");
        CB_THOR22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_THOR22ActionPerformed(evt);
            }
        });

        CB_MFSK16.setForeground(new java.awt.Color(0, 153, 0));
        CB_MFSK16.setText("MFSK16");
        CB_MFSK16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_MFSK16ActionPerformed(evt);
            }
        });

        CB_THOR8.setForeground(new java.awt.Color(0, 153, 0));
        CB_THOR8.setText("THOR8");
        CB_THOR8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_THOR8ActionPerformed(evt);
            }
        });

        cboModes.setMinimumSize(new java.awt.Dimension(100, 28));
        cboModes.setPreferredSize(new java.awt.Dimension(108, 27));
        cboModes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboModesActionPerformed(evt);
            }
        });

        jLabel13.setText("Default mode:");

        CB_DOMEX5.setForeground(new java.awt.Color(0, 153, 0));
        CB_DOMEX5.setText("DOMEX5");
        CB_DOMEX5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_DOMEX5ActionPerformed(evt);
            }
        });

        jLabel15.setForeground(new java.awt.Color(0, 0, 204));
        jLabel15.setText("> 300 Bd");

        CB_DOMEX22.setForeground(new java.awt.Color(0, 153, 0));
        CB_DOMEX22.setText("DOMEX22");
        CB_DOMEX22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_DOMEX22ActionPerformed(evt);
            }
        });

        CB_DOMEX11.setForeground(new java.awt.Color(0, 153, 51));
        CB_DOMEX11.setText("DOMEX11");
        CB_DOMEX11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_DOMEX11ActionPerformed(evt);
            }
        });

        CB_ListenInCwMode.setText(optionsdialog.getString("LISTEN IN CW MODE")); // NOI18N
        CB_ListenInCwMode.setToolTipText(optionsdialog.getString("TIP FOR LISTEN IN CW MODE")); // NOI18N
        CB_ListenInCwMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CB_ListenInCwModeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_PSK500)
                            .addComponent(CB_PSK125R)
                            .addComponent(CB_DOMEX11))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_PSK500R)
                            .addComponent(CB_DOMEX22)
                            .addComponent(CB_MFSK16))
                        .addGap(13, 13, 13)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_PSK250)
                            .addComponent(CB_MFSK32)
                            .addComponent(CB_THOR8))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CB_PSK250R)
                            .addComponent(CB_THOR22)
                            .addComponent(CB_DOMEX5)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel15)
                        .addGap(82, 82, 82)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboModes, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(CB_ListenInCwMode)))
                .addGap(76, 76, 76))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(CB_PSK500))
                    .addComponent(CB_PSK500R)
                    .addComponent(CB_PSK250)
                    .addComponent(CB_PSK250R))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CB_PSK125R)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(CB_DOMEX22))
                    .addComponent(CB_MFSK32)
                    .addComponent(CB_THOR22))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CB_DOMEX11)
                    .addComponent(CB_MFSK16)
                    .addComponent(CB_THOR8)
                    .addComponent(CB_DOMEX5))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cboModes, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(CB_ListenInCwMode)
                .addGap(40, 40, 40))
        );

        tabOptions.addTab("Modes", jPanel2);

        pnlModem.setBorder(javax.swing.BorderFactory.createTitledBorder("Modem settings"));

        lblModemIPAddress.setText(optionsdialog.getString("IP ADDRESS")); // NOI18N

        txtModemIPAddress.setText(optionsdialog.getString("LOCALHOST")); // NOI18N
        txtModemIPAddress.setToolTipText(optionsdialog.getString("LOCALHOST IS DEFAULT, RESTART IF CHANGING")); // NOI18N
        txtModemIPAddress.setPreferredSize(new java.awt.Dimension(120, 25));

        lblModemIPPort.setText(optionsdialog.getString("IP PORT")); // NOI18N

        txtModemIPPort.setText("7322");
        txtModemIPPort.setToolTipText(optionsdialog.getString("7322 IS DEFAULT, RESTART IF CHANGING")); // NOI18N
        txtModemIPPort.setPreferredSize(new java.awt.Dimension(120, 25));
        txtModemIPPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtModemIPPortActionPerformed(evt);
            }
        });

        lblModemPreamble.setText(optionsdialog.getString("FRAME PREAMBLE")); // NOI18N

        txtModemPreamble.setToolTipText(optionsdialog.getString("A STRING TO SEND BEFORE EACH FRAME")); // NOI18N
        txtModemPreamble.setPreferredSize(new java.awt.Dimension(120, 25));

        lblModemPostamble.setText(optionsdialog.getString("FRAME POSTAMBLE")); // NOI18N

        txtModemPostamble.setToolTipText(optionsdialog.getString("A STRING TO SEND AFTER EACH FRAME")); // NOI18N
        txtModemPostamble.setPreferredSize(new java.awt.Dimension(120, 25));

        txtFldigipath.setText("fldigi");
        txtFldigipath.setToolTipText(bundle1.getString("FLDIGI PATH")); // NOI18N
        txtFldigipath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFldigipathActionPerformed(evt);
            }
        });

        jLabel19.setText(optionsdialog.getString("FLDIGIAPPLICATIONPATH")); // NOI18N

        spinEveryXHours.setModel(new javax.swing.SpinnerNumberModel(1, 0, 24, 1));
        spinEveryXHours.setToolTipText(optionsdialog.getString("TO RESTART FLDIGI IN A FRESH STATE. 0 = DISABLED.")); // NOI18N

        lblHours.setText(optionsdialog.getString("HOURS")); // NOI18N

        lblFldigiAutorestart.setText(optionsdialog.getString("FLDIGIAUTORESTART")); // NOI18N

        txtModemXMLPort.setText("7362");
        txtModemXMLPort.setToolTipText(optionsdialog.getString("7362 IS DEFAULT, RESTART IF CHANGING")); // NOI18N
        txtModemXMLPort.setPreferredSize(new java.awt.Dimension(120, 25));

        lblModemXmlPort.setText(optionsdialog.getString("XML PORT")); // NOI18N

        javax.swing.GroupLayout pnlModemLayout = new javax.swing.GroupLayout(pnlModem);
        pnlModem.setLayout(pnlModemLayout);
        pnlModemLayout.setHorizontalGroup(
            pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtFldigipath)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlModemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFldigiAutorestart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addComponent(spinEveryXHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(lblHours, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63))
            .addGroup(pnlModemLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblModemIPPort)
                            .addComponent(lblModemPreamble)
                            .addComponent(lblModemPostamble)
                            .addComponent(lblModemIPAddress)
                            .addComponent(lblModemXmlPort))
                        .addGap(3, 3, 3)
                        .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtModemIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtModemIPPort, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtModemPreamble, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtModemPostamble, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtModemXMLPort, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlModemLayout.setVerticalGroup(
            pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModemLayout.createSequentialGroup()
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(lblModemIPAddress))
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(txtModemIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(3, 3, 3)
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblModemIPPort))
                    .addComponent(txtModemIPPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(txtModemXMLPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblModemXmlPort)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblModemPreamble))
                    .addComponent(txtModemPreamble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlModemLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblModemPostamble))
                    .addComponent(txtModemPostamble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFldigipath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlModemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinEveryXHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHours, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFldigiAutorestart))
                .addGap(25, 25, 25))
        );

        tabOptions.addTab("Modem", pnlModem);

        pnlHaIOT.setLayout(new java.awt.GridBagLayout());

        jLabel16.setText(optionsdialog.getString("HOMEASSISTANTIPADDRESS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlHaIOT.add(jLabel16, gridBagConstraints);

        jLabel28.setText(optionsdialog.getString("HOMEASSISTANTLONGLIVEDTOKEN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlHaIOT.add(jLabel28, gridBagConstraints);

        jLabel29.setText(optionsdialog.getString("IOTPASSWORD")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlHaIOT.add(jLabel29, gridBagConstraints);

        txtHAIpAddress.setPreferredSize(new java.awt.Dimension(150, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        pnlHaIOT.add(txtHAIpAddress, gridBagConstraints);

        txtHALongLivedToken.setMinimumSize(new java.awt.Dimension(150, 19));
        txtHALongLivedToken.setPreferredSize(new java.awt.Dimension(150, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlHaIOT.add(txtHALongLivedToken, gridBagConstraints);

        txtHAAccessPassword.setMinimumSize(new java.awt.Dimension(120, 19));
        txtHAAccessPassword.setPreferredSize(new java.awt.Dimension(120, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlHaIOT.add(txtHAAccessPassword, gridBagConstraints);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 160));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 160));

        txtEntitiesAliases.setColumns(20);
        txtEntitiesAliases.setRows(5);
        jScrollPane1.setViewportView(txtEntitiesAliases);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlHaIOT.add(jScrollPane1, gridBagConstraints);

        jLabel37.setText(optionsdialog.getString("LBLENTITIESSHORTCUTS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlHaIOT.add(jLabel37, gridBagConstraints);

        tabOptions.addTab("HA IOT", pnlHaIOT);

        getContentPane().add(tabOptions, new java.awt.GridBagConstraints());
        tabOptions.setEnabledAt(3, true);

        pnlButtons.setMinimumSize(new java.awt.Dimension(400, 35));
        pnlButtons.setPreferredSize(new java.awt.Dimension(435, 35));
        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        bOK.setText(optionsdialog.getString("OK")); // NOI18N
        bOK.setMaximumSize(new java.awt.Dimension(120, 35));
        bOK.setMinimumSize(new java.awt.Dimension(100, 25));
        bOK.setPreferredSize(new java.awt.Dimension(80, 25));
        bOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bOKActionPerformed(evt);
            }
        });
        pnlButtons.add(bOK);
        pnlButtons.add(jDesktopPane1);

        bCancel.setText(optionsdialog.getString("CANCEL")); // NOI18N
        bCancel.setMaximumSize(new java.awt.Dimension(100, 35));
        bCancel.setMinimumSize(new java.awt.Dimension(85, 25));
        bCancel.setPreferredSize(new java.awt.Dimension(85, 25));
        bCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(bCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(pnlButtons, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * OK button leads here, saves configuration
     */
    private void SaveConfiguration() {
        try {
            String path = Main.homePath + Main.dirPrefix;
            Config cf = new Config(path);

            // Save users callsign
            if (txtCallsign.getText().length() > 0) {
                String cleanCall = txtCallsign.getText();
                //remove un-allowed characters
                cleanCall = cleanCall.replaceAll("[^a-zA-Z0-9\\/\\-]", "");
                if (cleanCall.length() > 0) {
                    callsign = cleanCall;
                    Main.mycall = callsign;
                    cf.setPreference("CALL", cleanCall);
                    //Merge with callsign as server
                    cf.setPreference("CALLSIGNASSERVER", cleanCall);
                    Main.callsignAsServer = cleanCall;
                    Igate.aprsCall = Main.cleanCallForAprs(cleanCall);
                    Main.aprsCall = Igate.aprsCall;
                }
            }

            // Save default server
            if (txtLinkto.getText().length() > 0) {
                serverList = txtLinkto.getText();
                //VK2ETA, allow lower cases here for stations and passwords
                //cf.setPreference("SERVER", serverList.toUpperCase());
                cf.setPreference("SERVER", serverList);
            }

            // Session password
            char[] s = jPasswordField1.getPassword();
            String st = new String(s);
            if (st.length() > 0) {
                password = st;
                cf.setPreference("PASSWORD", password);
                Main.sessionPasswrd = st;
            }

            if (txtLogFile.getText().length() > 0) {
                cf.setPreference("LOGFILE", txtLogFile.getText());
            }

            Object myobject = this.spinBeaconMinute.getValue();
            beaconqrg = myobject.toString();
            cf.setBeaconqrg(beaconqrg);

            if (txtLatitude.getText().length() > 0) {
                if (Main.mainui.getFixTakenAt()) {
                    if (!Main.haveGPSD) {
                        latitude = Main.gpsData.getLatitude();
                    } else {
                        latitude = Main.gpsdLatitude;
                    }
                } else {
                    latitude = txtLatitude.getText();
                }

                cf.setPreference("LATITUDE", latitude);
                Main.mainui.setLatitudeText(latitude);
                Main.gpsdLatitude = latitude;
            }

            if (txtLongitude.getText().length() > 0) {
                if (Main.mainui.getFixTakenAt()) {
                    if (!Main.haveGPSD) {
                        longitude = Main.gpsData.getLongitude();
                    } else {
                        longitude = Main.gpsdLongitude;
                    }
                } else {
                    longitude = txtLongitude.getText();
                }
                cf.setPreference("LONGITUDE", longitude);
                Main.mainui.setLongitudeText(longitude);
                Main.gpsdLongitude = (longitude);
            }

            //GPS Section, only save when set in a disconnected state. This is due to rxtx that only
            // gives the available ports, not all the ports.
            if (chkGpsd.isSelected()) {
                cf.setPreference("GPSD", "1");
                Main.wantGpsd = true;
            } else {
                cf.setPreference("GPSD", "0");
                Main.wantGpsd = false;
            }

            if (!Main.haveGPSD) {
                Boolean mygpstest = Main.gpsPort.curstate;
                if (!mygpstest) {
                    gpsport = (String) cboGPSSerialPort.getSelectedItem();
                    if (gpsport != null) {
                        cf.setPreference("GPSPORT", gpsport);
                    }
                    gpsspeed = (String) cboGPSSpeed.getSelectedItem();
                    if (gpsspeed != null) {
                        cf.setPreference("GPSSPEED", gpsspeed);
                    }
                }

                gpsenabled = chkGPSConnection.isSelected();
                if (gpsenabled) {
                    cf.setPreference("GPSENABLED", "yes");
                } else {
                    cf.setPreference("GPSENABLED", "no");
                }
            }

            APRSServerEnabled = this.chkAPRSServer.isSelected();
            if (APRSServerEnabled) {
                cf.setPreference("APRSSERVER", "yes");
            } else {
                cf.setPreference("APRSSERVER", "no");
            }

            if (this.txtAPRSServerPort.getText().length() > 0) {
                cf.setPreference("APRSSERVERPORT", txtAPRSServerPort.getText());
            }

            //Rigctl section
            if (jCheckBoxRigctl.isSelected()) {
                cf.setPreference("RIGCTL", "yes");
                Main.wantRigctl = true;
            } else {
                cf.setPreference("RIGCTL", "no");
                Main.wantRigctl = false;
            }

            if (ScannerCheckbox.isSelected()) {
                cf.setPreference("SCANNER", "yes");
                Main.wantScanner = true;
            } else {
                cf.setPreference("SCANNER", "no");
                Main.wantScanner = false;
            }
            cf.setPreference("RIGOFFSET", OffsetField.getText());
            cf.setPreference("QRG0", QRG0.getText());
            cf.setPreference("QRG1", QRG1.getText());
            cf.setPreference("QRG2", QRG2.getText());
            cf.setPreference("QRG3", QRG3.getText());
            cf.setPreference("QRG4", QRG4.getText());

            // Configuration tab
            myobject = spinUiOption.getValue();
            cf.setPreference("UIOPTION", myobject.toString());
            myobject = DCDSpinner.getValue();
            cf.setPreference("DCD", myobject.toString());
            myobject = spinRetries.getValue();
            cf.setPreference("RETRIES", myobject.toString());
            myobject = spinIdleTime.getValue();
            cf.setPreference("IDLETIME", myobject.toString());
            myobject = spinTXdelay.getValue();
            cf.setPreference("TXDELAY", myobject.toString());
            myobject = spinOffsetMinute.getValue();
            cf.setPreference("OFFSETMINUTE", myobject.toString());
            myobject = spinOffsetSeconds.getValue();
            cf.setPreference("OFFSETSECONDS", myobject.toString());

            // mail options
            if (txtPophost.getText().length() > 0) {
                cf.setPreference("POPHOST", txtPophost.getText());
            }
            if (txtPopUser.getText().length() > 0) {
                cf.setPreference("POPUSER", txtPopUser.getText());
            }
            //Pop Password in jPasswordField now
            char[] s2 = txtPopPassword.getPassword();
            String st2 = new String(s2);
            if (st2.length() > 0) {
                cf.setPreference("POPPASS", st2);
            }
            if (txtReplyto.getText().length() > 0) {
                cf.setPreference("RETURNADDRESS", txtReplyto.getText());
            }
            if (Main.compressedmail) {
                cf.setPreference(Config.email.COMPRESSED.toString(), Config.state.yes.toString());
            } else {
                cf.setPreference(Config.email.COMPRESSED.toString(), Config.state.no.toString());
            }

            // Modem connection settings
            cf.setPreference("MODEMIP", txtModemIPAddress.getText());
            cf.setPreference("MODEMIPPORT", txtModemIPPort.getText());
            cf.setPreference("MODEMXMLPORT", txtModemXMLPort.getText());
            cf.setPreference("MODEMPOSTAMBLE", txtModemPostamble.getText());
            cf.setPreference("MODEMPREAMBLE", txtModemPreamble.getText());
            cf.setPreference("FLDIGIAPPLICATIONPATH", txtFldigipath.getText());
            myobject = this.spinEveryXHours.getValue();
            cf.setPreference("EVERYXHOURS", myobject.toString());

//System.out.println(cboModes.getSelectedItem());
            if (CB_ListenInCwMode.isSelected()) {
                cf.setPreference("LISTENINCWMODE", "yes");
            } else {
                cf.setPreference("LISTENINCWMODE", "no");
            }
            Main.defaultTxModem = cboModes.getSelectedItem().toString();
            cf.setPreference("DEFAULTMODE", Main.defaultTxModem);

            for (int j = 0; j < Main.m.smodes.length; j++) {
                if (Main.m.smodes[j].equals(Main.defaultTxModem)) {
                    Main.defaultmode = Main.m.pmodes[j];
                    break;
                }
            }

//System.out.println("SAVING:" + Main.DefaultTXmodem);
            Main.modemPostamble = txtModemPostamble.getText();
            Main.modemPreamble = txtModemPreamble.getText();
            //Main.defaultmode = Main.convmodem(Main.DefaultTXmodem);

            // Modes section, save the mode ladder and default mode
            Main.configuration.setPreference("MODES", Main.modesListStr);
            Main.configuration.setPreference("DEFAULTMODE", Main.defaultTxModem);

            // Handle gps settings
            if (!Main.haveGPSD) {
                handlegpsupdown();
            }

            //Pskmail Server preferences
            //String callAsServer = txtCallsignAsServer.getText();
            //if (callAsServer.length() > 0) {
            //    callAsServer = callAsServer.replaceAll("[^a-zA-Z0-9\\/\\-\\s]", "");
            //    if (callAsServer.length() > 0) {
            //        cf.setPreference("CALLSIGNASSERVER", callAsServer);
            //        Main.callsignAsServer = callAsServer;
            //        Igate.aprsCall = Main.cleanCallForAprs(callAsServer);
            //        Main.aprsCall = Igate.aprsCall;
            //    }
            //}
            
            //cf.setPreference("ACCESSPASSWORD", txtAccessPassword.getPassword().toString().trim());
            //Main.accessPassword = txtAccessPassword.getPassword().toString().trim();
            //Always save in case it gets blanked out
            char[] s3 = txtAccessPassword.getPassword();
            String st3 = new String(s3);
            cf.setPreference("ACCESSPASSWORD", st3);
            Main.relayingPassword = st3;
            if (txtServerImapHost.getText().length() > 0) {
                cf.setPreference("SERVERIMAPHOST", txtServerImapHost.getText());
            }
            myobject = spinnerImapProtocol.getValue();
            cf.setPreference("SERVERIMAPPROTOCOL", myobject.toString());
            if (txtServerImapPort.getText().length() > 0) {
                cf.setPreference("SERVERIMAPPORT", txtServerImapPort.getText());
            }
            if (txtServerSmtpHost.getText().length() > 0) {
                cf.setPreference("SERVERSMTPHOST", txtServerSmtpHost.getText());
            }
            myobject = spinnerSmtpProtocol.getValue();
            cf.setPreference("SERVERSMTPPROTOCOL", myobject.toString());
            if (txtServerSmtpPort.getText().length() > 0) {
                cf.setPreference("SERVERSMTPPORT", txtServerSmtpPort.getText());
            }
            if (txtServerEmailAddress.getText().length() > 0) {
                cf.setPreference("SERVEREMAILADDRESS", txtServerEmailAddress.getText());
            }
            if (txtServerUserName.getText().length() > 0) {
                cf.setPreference("SERVERUSERNAME", txtServerUserName.getText());
            }
            //Server email Password in jPasswordField now
            char[] s4 = txtServerEmailPassword.getPassword();
            String st4 = new String(s4);
            if (st4.length() > 0) {
                cf.setPreference("SERVERPASSWORD", st4);
            }
            if (checkboxEnablePskmailServer.isSelected()) {
                cf.setPreference("ENABLESERVER", "yes");
                Main.wantServer = true;
            } else {
                cf.setPreference("ENABLESERVER", "no");
                Main.wantServer = false;
            }
            if (this.checkboxUseVirtualMailBoxes.isSelected()) {
                cf.setPreference("USEVIRTUALEMAILBOXES", "yes");
            } else {
                cf.setPreference("USEVIRTUALEMAILBOXES", "no");
            }
            //RadioMsg preferences
            if (checkboxRelayOverRadio.isSelected()) {
                cf.setPreference("RELAYOVERRADIO", "yes");
                Main.wantRelayOverRadio = true;
            } else {
                cf.setPreference("RELAYOVERRADIO", "no");
                Main.wantRelayOverRadio = false;
            }
            if (checkboxRelayEmail.isSelected()) {
                cf.setPreference("RELAYEMAILS", "yes");
                Main.wantRelayEmails = true;
            } else {
                cf.setPreference("RELAYEMAILS", "no");
                Main.wantRelayEmails = false;
            }
            if (checkboxRelayEmailsImmediately.isSelected()) {
                cf.setPreference("RELAYEMAILSIMMEDIATELY", "yes");
                Main.wantRelayEmailsImmediat = true;
            } else {
                cf.setPreference("RELAYEMAILSIMMEDIATELY", "no");
                Main.wantRelayEmailsImmediat = false;
            }
            if (checkboxRelaySMSs.isSelected()) {
                cf.setPreference("RELAYSMSS", "yes");
                Main.wantRelaySMSs = true;
            } else {
                cf.setPreference("RELAYSMSS", "no");
                Main.wantRelaySMSs = false;
            }
            if (checkboxRelaySMSsImmediately.isSelected()) {
                cf.setPreference("RELAYSMSSIMMEDIATELY", "yes");
                Main.wantRelaySMSsImmediat = true;
            } else {
                cf.setPreference("RELAYSMSSIMMEDIATELY", "no");
                Main.wantRelaySMSsImmediat = false;
            }
            myobject = spinnerAckPosition.getValue();
            cf.setPreference("ACKPOSITION", myobject.toString());
            myobject = spinnerMaxAckPosition.getValue();
            cf.setPreference("MAXACKS", myobject.toString());
            if (checkboxAckWithRSID.isSelected()) {
                cf.setPreference("ACKWITHRSID", "yes");
            } else {
                cf.setPreference("ACKWITHRSID", "no");
            }
            //Always save in case we blank it out
            cf.setPreference("SMSEMAILGATEWAY", txtSmsEmailGatewayDomain);
            cf.setPreference("SENDCELLULARNUMBERAS", txtSendCellNumAs);
            cf.setPreference("GATEWAYISOCOUNTRYCODE", txtGatewayISOCountryCode);
            cf.setPreference("DELETESMSREPLYUPTO", txtDeleteUpTo);
            cf.setPreference("DELETESMSREPLYSWHOLELINE", txtDeleteWholeLine);
            cf.setPreference("DELETESMSREPLYFROM", txtDeleteFrom);
            myobject = spinnerDaysToKeepLink.getValue();
            cf.setPreference("DAYSTOKEEPLINK", myobject.toString());
             //Home Assistant IOT settings
            cf.setPreference("HOMEASSISTANTIPADDRESS", txtHAIpAddress.getText());
            cf.setPreference("HOMEASSISTANTLONGLIVEDTOKEN", txtHALongLivedToken.getText());
            char[] shapw = txtHAAccessPassword.getPassword();
            cf.setPreference("IOTACCESSPASSWORD", new String(shapw));
            Main.IotAccessPassword = new String(shapw);
            cf.setPreference("IOTENTITIESSHORTCUTS", txtEntitiesAliases.getText());
            //Monitoring of sending folder
            //checkboxMonitorFilesFolder
            if (checkboxMonitorFilesFolder.isSelected()) {
                cf.setPreference("MONITORFILESFOLDER", "yes");
            } else {
                cf.setPreference("MONITORFILESFOLDER", "no");
            }
        
        } catch (Exception ex) {
            Main.log.writelog(optionsdialog.getString("ERROR ENCOUNTERED WHEN STORING PREFERENCES!"), ex, true);
        }
    }

    /**
     * Ok was selected, that means everything should be saved!
     *
     * @param evt
     */
private void bOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOKActionPerformed
// TODO add your handling code here:
    // Update the config object and have it save itself
    SaveConfiguration();
    this.setVisible(false);
}//GEN-LAST:event_bOKActionPerformed

    /**
     * Enable or disable the gps controls
     *
     * @param set
     */
    private void enablegpscontrols(Boolean set) {
        this.cboGPSSerialPort.setEnabled(set);
        this.cboGPSSpeed.setEnabled(set);
    }

    /**
     * Open the port if its selected and not open before
     */
    private void handlegpsupdown() {
        // Take care of GPS settings and enable/disable it
        String portforgps = Main.configuration.getPreference("GPSPORT");
        String speedforgps = Main.configuration.getPreference("GPSSPEED");
        int speedygps = Integer.parseInt(speedforgps);

        try {
            if (!Main.gpsPort.getconnectstate()) {
                // Not connected
                if (chkGPSConnection.isSelected()) { // But would like to be
                    Main.gpsPort.connect(portforgps, speedygps);
                    // Check if the port is open
                    if (!Main.gpsPort.curstate) {
                        // Disconnect and set it off
                        Main.gpsPort.disconnect();
                        Main.configuration.setPreference("GPSENABLED", "no");
                    }
                }
            } else {   // Connected
                if (!chkGPSConnection.isSelected()) { // But would like it not to be
                    // Check if the port is open
                    if (Main.gpsPort.curstate) {
                        // Disconnect and set it off
                        Main.gpsPort.disconnect();
                        Main.configuration.setPreference("GPSENABLED", "no");
                    }
                }
            }
        } catch (Exception ex) {
            Main.log.writelog(optionsdialog.getString("COULD NOT OPEN/CLOSE GPS PORT!"), ex, true);
        }
    }

private void bCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCancelActionPerformed
// TODO add your handling code here:
    this.setVisible(false);
}//GEN-LAST:event_bCancelActionPerformed

private void spinOffsetSecondsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinOffsetSecondsStateChanged
    // TODO add your handling code here:
    setSeconds();
}//GEN-LAST:event_spinOffsetSecondsStateChanged

    private void CB_DOMEX11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_DOMEX11ActionPerformed
        // TODO add your handling code here:
        UpdateModeList();
    }//GEN-LAST:event_CB_DOMEX11ActionPerformed

    private void CB_DOMEX22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_DOMEX22ActionPerformed
        // TODO add your handling code here:
        UpdateModeList();
    }//GEN-LAST:event_CB_DOMEX22ActionPerformed

    private void CB_DOMEX5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_DOMEX5ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_DOMEX5ActionPerformed

    private void cboModesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboModesActionPerformed

        int mode = cboModes.getSelectedIndex();

        String md = "";
        if (mode >= 0 & mode < modemarray.length) {

            md = cboModes.getSelectedItem().toString();
            //        md = modemarray[mode];
            //System.out.println("md=" + md);
            Main.defaultTxModem = md;
            //       Main.DefaultTXmodem = md;
            defaultmodemindex = mode;
            ModemModesEnum mmode = Main.convmodem(md);

            Main.defaultmode = mmode;
            Main.txModem = mmode;
            //Do not change the Fldigi mode at each selection (introduces delays). 
            //Now done when we exit the option dialog 
            //Main.m.ChangeMode(Main.TxModem);
            Main.mainui.setRxmodeTextfield(md);
            Main.mainui.setRXlabel(md);
            Main.rxModem = mmode;
            Main.rxModemString = md;
            Main.mainui.setProfile("Asymmetrical");
            //        myarq.Message("Server Mode:" + md, 5);
        }
        Main.configuration.setPreference("DEFAULTMODE", md);
    }//GEN-LAST:event_cboModesActionPerformed

    private void CB_THOR8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_THOR8ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_THOR8ActionPerformed

    private void CB_MFSK16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_MFSK16ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_MFSK16ActionPerformed

    private void CB_THOR22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_THOR22ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_THOR22ActionPerformed

    private void CB_MFSK32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_MFSK32ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_MFSK32ActionPerformed

    private void CB_PSK125RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_PSK125RActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_PSK125RActionPerformed

    private void CB_PSK250RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_PSK250RActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_PSK250RActionPerformed

    private void CB_PSK250ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_PSK250ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_PSK250ActionPerformed

    private void CB_PSK500RActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_PSK500RActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_PSK500RActionPerformed

    private void CB_PSK500ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_PSK500ActionPerformed
        // Refill the default mode list drop down
        UpdateModeList();
    }//GEN-LAST:event_CB_PSK500ActionPerformed

    private void jCheckBoxRigctlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRigctlActionPerformed
        // TODO add your handling code here:
        if (jCheckBoxRigctl.isSelected()) {
            Main.configuration.setPreference("RIGCTL", "yes");
            Main.wantRigctl = true;
        } else {
            Main.configuration.setPreference("RIGCTL", "no");
            Main.wantRigctl = false;
        }
    }//GEN-LAST:event_jCheckBoxRigctlActionPerformed

    private void bGetSerPortsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bGetSerPortsActionPerformed
        getComPorts();
    }//GEN-LAST:event_bGetSerPortsActionPerformed

    private void cboGPSSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboGPSSpeedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboGPSSpeedActionPerformed

    private void chkGPSConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGPSConnectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkGPSConnectionActionPerformed

    private void chkGPSConnectionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkGPSConnectionStateChanged
        // Get the checkbox state, too tired to remember how now (need sleep)
    }//GEN-LAST:event_chkGPSConnectionStateChanged

    private void DCDSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_DCDSpinnerStateChanged
        // TODO add your handling code here:
        setDCD();
    }//GEN-LAST:event_DCDSpinnerStateChanged

    private void spinTXdelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinTXdelayStateChanged
        // TODO add your handling code here:
        System.out.println(spinTXdelay.getValue());
    }//GEN-LAST:event_spinTXdelayStateChanged

    private void txtLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLogFileActionPerformed
        // TODO add your handling code here:
        String path = Main.homePath + Main.dirPrefix;
        Config cf = new Config(path);
        if (txtLogFile.getText().length() > 0) {
            cf.setPreference("LOGFILE", txtLogFile.getText());
        }
        Main.logFile = txtLogFile.getText();
    }//GEN-LAST:event_txtLogFileActionPerformed

    private void CompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CompressedActionPerformed
        // TODO add your handling code here:
        if (Main.compressedmail) {
            Main.compressedmail = false;
            Compressed.setSelected(false);
        } else {
            Main.compressedmail = true;
            Compressed.setSelected(true);
        }
    }//GEN-LAST:event_CompressedActionPerformed

    private void checkboxRelayOverRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxRelayOverRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxRelayOverRadioActionPerformed

    private void checkboxRelayEmailsImmediatelyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxRelayEmailsImmediatelyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxRelayEmailsImmediatelyActionPerformed

    private void checkboxRelayEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxRelayEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxRelayEmailActionPerformed

    private void checkboxEnablePskmailServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxEnablePskmailServerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxEnablePskmailServerActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
        String path = Main.homePath + Main.dirPrefix;
        Config cp = new Config(path);

        char[] s = jPasswordField1.getPassword();
        String st = new String(s);
        if (st.length() > 0) {
            password = st;
            cp.setPreference("PASSWORD", password);
        }
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void txtLongitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLongitudeActionPerformed
        // TODO add your handling code here:
        //   SaveConfiguration();
        //   System.out.println(txtLongitude.getText());
    }//GEN-LAST:event_txtLongitudeActionPerformed

    private void txtLatitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLatitudeActionPerformed
        // TODO add your handling code here:
        //    SaveConfiguration()
    }//GEN-LAST:event_txtLatitudeActionPerformed

    private void txtLinktoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLinktoActionPerformed
        // TODO add your handling code here:
        serverList = txtLinkto.getText();
    }//GEN-LAST:event_txtLinktoActionPerformed

    private void txtCallsignFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCallsignFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCallsignFocusLost

    private void txtCallsignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCallsignActionPerformed
        // TODO add your handling code here:
        callsign = txtCallsign.getText();
    }//GEN-LAST:event_txtCallsignActionPerformed

    private void txtPopUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPopUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPopUserActionPerformed

    private void txtReplytoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReplytoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtReplytoActionPerformed

    private void txtCallsignAsServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCallsignAsServerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCallsignAsServerActionPerformed

    private void txtServerImapHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServerImapHostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServerImapHostActionPerformed

    private void txtPophostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPophostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPophostActionPerformed

    private void txtServerSmtpHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServerSmtpHostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServerSmtpHostActionPerformed

    private void txtServerUserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServerUserNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServerUserNameActionPerformed

    private void bPosConverterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPosConverterActionPerformed
        // TODO add your handling code here:
    // Show the position converter window
                PosFormatConv frmConverter;

                try{
                    frmConverter = new PosFormatConv(Main.mainui,true);
                    frmConverter.setLocationRelativeTo(null);
                    frmConverter.setVisible(true);
                    // Get the data if ok was used
                    if(frmConverter.isUsedata()){
                        this.txtLatitude.setText(frmConverter.getTxtLatDecimal().toString());
                        this.txtLongitude.setText(frmConverter.getTxtLonDecimal().toString());
                    }
                    SaveConfiguration();
                    frmConverter.dispose();
                }
                catch(Exception e){
                    Main.log.writelog("Sri, could not open that window", e, true);
                }
    }//GEN-LAST:event_bPosConverterActionPerformed

    private void checkboxRelaySMSsImmediatelyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxRelaySMSsImmediatelyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxRelaySMSsImmediatelyActionPerformed

    private void txtFldigipathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFldigipathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFldigipathActionPerformed

    private void txtModemIPPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtModemIPPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtModemIPPortActionPerformed

    private void CB_ListenInCwModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CB_ListenInCwModeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CB_ListenInCwModeActionPerformed

    private void checkboxUseVirtualMailBoxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxUseVirtualMailBoxesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkboxUseVirtualMailBoxesActionPerformed

    private void txtServerImapPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServerImapPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServerImapPortActionPerformed

    private void txtServerSmtpPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServerSmtpPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServerSmtpPortActionPerformed

    private void jButtonConfigSmsGatewayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigSmsGatewayActionPerformed

        OptionsDialogSms dialog = new OptionsDialogSms(new javax.swing.JFrame(), true);
        //Pass "this" reference to the dialog for getters and setters
        dialog.SetBackReference(this);
        dialog.setValues(txtSmsEmailGatewayDomain,
            txtSendCellNumAs,
            txtGatewayISOCountryCode,
            txtDeleteUpTo,
            txtDeleteWholeLine,
            txtDeleteFrom);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        //Finished, clean
        dialog.dispose();
    }//GEN-LAST:event_jButtonConfigSmsGatewayActionPerformed

    /**
     * A mode is checked/unchecked so the mode list should be refilled
     */
    private void UpdateModeList() {
        String MdStringList = "";
        try {
            String[] modes = {"PSK1000", "PSK500", "PSK500R", "PSK250R", "DOMINOEX22", "MFSK32", "THOR22", "DOMINOEX11", "MFSK16", "THOR8", "DOMINOEX5", "CTSTIA 8/250",
                "PSK63RC5", "PSK63RC10", "PSK250RC3", "PSK125RC4", ""};

            int i = 0;
            String modestr = "";
            String Defmodem = Main.defaultTxModem;

            if (CB_PSK500.isSelected()) {
                modes[i] = "PSK500";
                modestr += "7";
                i++;
            }

            if (CB_PSK500R.isSelected()) {
                modes[i] = "PSK500R";
                modestr += "6";
                i++;
            }
            if (CB_PSK250.isSelected()) {
                modes[i] = "PSK250";
                modestr += "8";
                i++;
            }

            if (CB_PSK250R.isSelected()) {
                modes[i] = "PSK250R";
                modestr += "5";
                i++;
            }
//        if (CB_PSK125.isSelected()) {
//            modes[i] = "PSK125";
//            modestr += "9";
//            i++;
//        }
            if (CB_DOMEX22.isSelected()) {
                modes[i] = "DOMINOEX22";
                modestr += "l";
                i++;
            }
            if (CB_PSK125R.isSelected()) {
                modes[i] = "PSK125R";
                modestr += "b";
                i++;
            }
            if (CB_MFSK32.isSelected()) {
                modes[i] = "MFSK32";
                modestr += "4";
                i++;
            }
            if (CB_THOR22.isSelected()) {
                modes[i] = "THOR22";
                modestr += "3";
                i++;
            }
            if (CB_DOMEX11.isSelected()) {
                modes[i] = "DOMINOEX11";
                modestr += "m";
                i++;
            }
            if (CB_MFSK16.isSelected()) {
                modes[i] = "MFSK16";
                modestr += "2";
                i++;
            }
            if (CB_THOR8.isSelected()) {
                modes[i] = "THOR8";
                modestr += "1";
                i++;
            }

            if (CB_DOMEX5.isSelected()) {
                modes[i] = "DOMINOEX5";
                modestr += "n";
                i++;
            }

//System.out.println("MODE=" + modestr);
            Main.modesListStr = modestr;
            String mds = Main.modesListStr;
            char[] mdc;
            mdc = mds.toCharArray();
            String Md = "";

            Main.defaultTxModem = Defmodem;
            //Remember which was selected before updating
            // The following is done twice, once here and once in the combobox event. Commenting it all out.
            // I think we should set a default modem here instead of 
            // reusing the old that may not exist any more...
            if (this.cboModes.getItemCount() > 0) {
                Defmodem = cboModes.getSelectedItem().toString();
            }
            //Clear all
            cboModes.removeAllItems();

            for (int index = 0; index < mdc.length; index++) {
                switch (mdc[index]) {
                    case 49:
                        Md = "THOR8";
                        break;
                    case 50:
                        Md = "MFSK16";
                        break;
                    case 51:
                        Md = "THOR22";
                        break;
                    case 52:
                        Md = "MFSK32";
                        break;
                    case 53:
                        Md = "PSK250R";
                        break;
                    case 54:
                        Md = "PSK500R";
                        break;
                    case 55:
                        Md = "PSK500";
                        break;
                    case 56:
                        Md = "PSK250";
                        break;
                    case 57:
                        Md = "PSK125";
                        break;
                    case 97:
                        Md = "PSK63";
                        break;
                    case 98:
                        Md = "PSK125R";
                        break;
                    case 99:
                        Md = "MFSK64";
                        break;
                    case 110:
                        Md = "DOMINOEX5";
                        break;
                    case 102:
                        Md = "CTSTIA";
                        break;
                    case 103:
                        Md = "PSK1000";
                        break;
                    case 104:
                        Md = "PSK63RC5";
                        break;
                    case 105:
                        Md = "PSK63RC10";
                        break;
                    case 106:
                        Md = "PSK250RC3";
                        break;
                    case 107:
                        Md = "PSK125RC4";
                        break;
                    case 108:
                        Md = "DOMINOEX22";
                        break;
                    case 109:
                        Md = "DOMINOEX11";
                        break;
                    default:
                        Md = "default";
                }
                //cboModes.addItem(Md); //Way too slooooooowwwww (Swing issue)
                //Built a comma separated list
                if (MdStringList.length() > 0) {
                    MdStringList += "," + Md;
                } else {
                    MdStringList = Md;
                }
            }
            //Then add the full list once to speed up the update
            String [] values = MdStringList.split(",");
            cboModes.setModel(new DefaultComboBoxModel(values));

            //  System.out.println("DEFMODE=" + Defmodem);       
            Main.configuration.setPreference("DEFAULTMODE", Defmodem);
            for (int j = 0; j < modes.length; j++) {
                if (modes[j].equals(Defmodem)) {
                    defaultmodemindex = j;
                    break;
                }
            }
            //cboModes.setSelectedItem(modemarray[defaultmodemindex]);
            for (int j = 0; j < values.length; j++) {
                if (values[j].equals(Defmodem)) {
                    cboModes.setSelectedIndex(j);
                    break;
                }
            }
            Main.mainui.setRXlabel(Defmodem);
        } catch (Exception e) {
            Main.log.writelog("Encountered trouble when respooling the mode list drop down.", e, true);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                OptionsDialog dialog = new OptionsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CB_DOMEX11;
    private javax.swing.JCheckBox CB_DOMEX22;
    private javax.swing.JCheckBox CB_DOMEX5;
    private javax.swing.JCheckBox CB_ListenInCwMode;
    private javax.swing.JCheckBox CB_MFSK16;
    private javax.swing.JCheckBox CB_MFSK32;
    private javax.swing.JCheckBox CB_PSK125R;
    private javax.swing.JCheckBox CB_PSK250;
    private javax.swing.JCheckBox CB_PSK250R;
    private javax.swing.JCheckBox CB_PSK500;
    private javax.swing.JCheckBox CB_PSK500R;
    private javax.swing.JCheckBox CB_THOR22;
    private javax.swing.JCheckBox CB_THOR8;
    private javax.swing.JCheckBox Compressed;
    private javax.swing.JSpinner DCDSpinner;
    private javax.swing.JTextField OffsetField;
    private javax.swing.JTextField QRG0;
    private javax.swing.JTextField QRG1;
    private javax.swing.JTextField QRG2;
    private javax.swing.JTextField QRG3;
    private javax.swing.JTextField QRG4;
    private javax.swing.JPanel RadioMsgPanel;
    private javax.swing.JCheckBox ScannerCheckbox;
    private javax.swing.JPanel ServerPanel;
    private javax.swing.JButton bCancel;
    private javax.swing.JButton bGetSerPorts;
    private javax.swing.JButton bOK;
    private javax.swing.JButton bPosConverter;
    private javax.swing.JComboBox<String> cboGPSSerialPort;
    private javax.swing.JComboBox cboGPSSpeed;
    private javax.swing.JComboBox<String> cboModes;
    private javax.swing.JCheckBox checkboxAckWithRSID;
    private javax.swing.JCheckBox checkboxEnablePskmailServer;
    private javax.swing.JCheckBox checkboxMonitorFilesFolder;
    private javax.swing.JCheckBox checkboxRelayEmail;
    private javax.swing.JCheckBox checkboxRelayEmailsImmediately;
    private javax.swing.JCheckBox checkboxRelayOverRadio;
    private javax.swing.JCheckBox checkboxRelaySMSs;
    private javax.swing.JCheckBox checkboxRelaySMSsImmediately;
    private javax.swing.JCheckBox checkboxUseVirtualMailBoxes;
    private javax.swing.JCheckBox chkAPRSServer;
    private javax.swing.JCheckBox chkGPSConnection;
    private javax.swing.JCheckBox chkGpsd;
    private javax.swing.JPanel frAPRSServer;
    private javax.swing.JPanel frGPS;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButtonConfigSmsGateway;
    private javax.swing.JCheckBox jCheckBoxRigctl;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelRigStatus;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBeaconQRG;
    private javax.swing.JLabel lblCallsign;
    private javax.swing.JLabel lblDCD;
    private javax.swing.JLabel lblEmailHeader;
    private javax.swing.JLabel lblFldigiAutorestart;
    private javax.swing.JLabel lblHours;
    private javax.swing.JLabel lblIdle;
    private javax.swing.JLabel lblLatitude;
    private javax.swing.JLabel lblLinkto;
    private javax.swing.JLabel lblLongitude;
    private javax.swing.JLabel lblModemIPAddress;
    private javax.swing.JLabel lblModemIPPort;
    private javax.swing.JLabel lblModemPostamble;
    private javax.swing.JLabel lblModemPreamble;
    private javax.swing.JLabel lblModemXmlPort;
    private javax.swing.JLabel lblOffsetmin;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPortNumber;
    private javax.swing.JLabel lblPortscbo;
    private javax.swing.JLabel lblRetries;
    private javax.swing.JLabel lblSecond;
    private javax.swing.JLabel lblSpeed;
    private javax.swing.JLabel lblTXdelay;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlConfiguration;
    private javax.swing.JPanel pnlEmail;
    private javax.swing.JPanel pnlGPS;
    private javax.swing.JPanel pnlHaIOT;
    private javax.swing.JPanel pnlModem;
    private javax.swing.JPanel pnlUserData;
    private javax.swing.JSpinner spinBeaconMinute;
    private javax.swing.JSpinner spinEveryXHours;
    private javax.swing.JSpinner spinIdleTime;
    private javax.swing.JSpinner spinOffsetMinute;
    public javax.swing.JSpinner spinOffsetSeconds;
    private javax.swing.JSpinner spinRetries;
    private javax.swing.JSpinner spinTXdelay;
    private javax.swing.JSpinner spinUiOption;
    private javax.swing.JSpinner spinnerAckPosition;
    private javax.swing.JSpinner spinnerDaysToKeepLink;
    private javax.swing.JSpinner spinnerImapProtocol;
    private javax.swing.JSpinner spinnerMaxAckPosition;
    private javax.swing.JSpinner spinnerSmtpProtocol;
    private javax.swing.JTabbedPane tabOptions;
    private javax.swing.JPanel test1;
    private javax.swing.JPanel test2;
    private javax.swing.JPanel test3;
    private javax.swing.JTextField txtAPRSServerPort;
    private javax.swing.JPasswordField txtAccessPassword;
    private javax.swing.JTextField txtCallsign;
    private javax.swing.JTextField txtCallsignAsServer;
    private javax.swing.JTextArea txtEntitiesAliases;
    private javax.swing.JTextField txtFldigipath;
    private javax.swing.JPasswordField txtHAAccessPassword;
    private javax.swing.JTextField txtHAIpAddress;
    private javax.swing.JTextField txtHALongLivedToken;
    private javax.swing.JFormattedTextField txtLatitude;
    private javax.swing.JTextField txtLinkto;
    private javax.swing.JTextField txtLogFile;
    private javax.swing.JFormattedTextField txtLongitude;
    private javax.swing.JTextField txtModemIPAddress;
    private javax.swing.JTextField txtModemIPPort;
    private javax.swing.JTextField txtModemPostamble;
    private javax.swing.JTextField txtModemPreamble;
    private javax.swing.JTextField txtModemXMLPort;
    private javax.swing.JPasswordField txtPopPassword;
    private javax.swing.JTextField txtPopUser;
    private javax.swing.JTextField txtPophost;
    private javax.swing.JTextField txtPophost1;
    private javax.swing.JTextField txtReplyto;
    private javax.swing.JTextField txtServerEmailAddress;
    private javax.swing.JPasswordField txtServerEmailPassword;
    private javax.swing.JTextField txtServerImapHost;
    private javax.swing.JTextField txtServerImapPort;
    private javax.swing.JTextField txtServerSmtpHost;
    private javax.swing.JTextField txtServerSmtpPort;
    private javax.swing.JTextField txtServerUserName;
    // End of variables declaration//GEN-END:variables

}
