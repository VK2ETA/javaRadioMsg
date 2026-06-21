/*
 * RMsgObject.java  
 *   
 * Copyright (C) 2017-2022 John Douyere (VK2ETA)  
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
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RMsgObject {

    String rawRxString;
    String fileName;
    String from;
    String to;
    String via;
    String relay;
    String sms;
    String pictureString;
    Bitmap picture;
    int pictureTxSPP;
    boolean pictureColour;
    int pictureTxModemIndex;
    Boolean msgHasPosition;
    RMsgLocation position;
    long positionRequestTime;
    int positionAge;
    Short[] voiceMessage;
    boolean crcValid; //Valid with no access password
    boolean crcValidWithRelayPW; //Valid only if access password is used
    boolean crcValidWithIotPW; //Valid only if IOT access password is used
    String rxMode;
    String accessPasswordUsed;
    String timeId;
    Boolean sent;
    Calendar receiveDate;
    boolean sendMyTime;
    

    //Blank object constructor
    public RMsgObject() {

        this.rawRxString = "";
        this.fileName = "";
        this.from = "";
        this.to = "";
        this.via = "";
        this.relay = "";
        this.sms = "";
        this.pictureString = "";
        this.picture = null;
        this.pictureTxSPP = 0;
        this.pictureColour = false;
        this.pictureTxModemIndex = 0;
        this.msgHasPosition = false;
        this.position = null;
        this.positionRequestTime = 0L;
        this.positionAge = 0;
        this.voiceMessage = null;
        this.crcValid = false;
        this.crcValidWithRelayPW =false;
        this.rxMode = "";
        this.accessPasswordUsed = "";
        this.timeId = "";
        this.sent = false;
        this.receiveDate = null;
        this.sendMyTime = false;
    }


    //Create directly with data
    public RMsgObject(String to, String via, String sms,
                     Bitmap picture, int pictureTxSPP, boolean pictureColour, int pictureTxModemIndex,
                     Boolean msgHasPosition, RMsgLocation msgLocation, long positionRequestTime,
                     Short[] voiceMessage) {

        this.to = to;
        this.sms = sms;
        this.via = via;
        this.relay = "";
        this.picture = picture;
        this.pictureTxSPP = pictureTxSPP;
        this.pictureColour = pictureColour;
        this.pictureTxModemIndex = pictureTxModemIndex;
        //Added blank pictureString to avoid crashes (Hazmat device)
        this.pictureString = "";
        this.msgHasPosition = msgHasPosition;
        this.position = msgLocation;
        this.positionRequestTime = positionRequestTime;
        this.voiceMessage = voiceMessage;
        this.rxMode = ""; //No mode provided
        this.accessPasswordUsed = "";
        this.timeId = "";
        this.sent = false;
        this.receiveDate = null;
        this.sendMyTime = false;
    }


    //Returns the latitude and longitude in a formatted string
    //Changed so that it is always with a decimal point as the decimal separator
    public String getLatLongString() {
        double latnum = 0.0;
        double lonnum = 0.0;
        if (this.position != null) {
            latnum = this.position.getLatitude();
            lonnum = this.position.getLongitude();
        }
        DecimalFormat dFlat = new DecimalFormat("##0.00000;-##0.00000");
        DecimalFormat dFlon = new DecimalFormat("###0.00000;-###0.00000");
        String decDegreesString = dFlat.format(latnum).replace(',','.') + "," + dFlon.format(lonnum).replace(',','.');

        return decDegreesString;
    }


//https://www.google.com/maps/search/?api=1&query=47.5951518,-122.3316393


    //Extracts data from the message object passed as parameter
    public String formatForSmsOrEmail() {

        String formattedString = "";

        if (!this.sms.equals("")) {
            formattedString += "Message: " + RMsgMisc.unescape(this.sms) + "\n";
        }
        if (this.msgHasPosition && this.position != null) {
            formattedString += "Location: " + "https://www.google.com/maps/search/?api=1&query=" + this.getLatLongString() +
                    "\n" + "Speed: " + ((int) (this.position.getSpeed() * 3.6)) + "Km/h\n";
        }
        //Added non-null test for crashes in Hazmat device + initialized to "" in object creation.
        if (this.pictureString != null && !this.pictureString.equals("")) {
            formattedString += "Picture: " + this.pictureString + "\n";
        }
        formattedString += "From: " + this.from + "\n" + "To: " + (this.to.equals("*") ? "All" : this.to);
        if (!this.relay.equals("")) {
            formattedString += ", Relayed by: " + this.relay;
        }
        if (!this.via.equals("")) {
            formattedString += ", Relayed by: " + this.via;
        }
        if (this.fileName != null && this.fileName.indexOf(".txt") != -1) { //Error in file name (crashes in Hazmat device)
            formattedString += "\nReceived at: " + this.fileName.substring(0, this.fileName.indexOf(".txt"));
        } else {
            formattedString += "\nReceived at: " + this.fileName;
        }

        return formattedString;
    }




    //Extracts data from the message object passed as parameter
    public String formatForList(boolean trackedFormat) {

        String formattedString = "";

        if (trackedFormat) {
/* not yet            if (!this.sms.equals("")) {
                formattedString += misc.unescape(this.sms) + "\n";
            }
            if (this.msgHasPosition && this.position != null) {
                int mDistance = -1;
                int timeToDest = -1;
                if (RadioMSG.currentLocation != null) {
                    mDistance = (int) this.position.distanceTo(RadioMSG.currentLocation);
                    //Speed > 1/2 Km/H, calculate time to destination
                    if (RadioMSG.currentLocation.getSpeed() > 2 / 3.6) { //2 Km/H in M/S
                        //Time expressed in seconds
                        timeToDest = (int) (((float) mDistance) / RadioMSG.currentLocation.getSpeed());
                        timeToDestString = misc.secToTime(timeToDest);
                    }
                }
                formattedString += "Dist: " + (mDistance > 0 ? misc.metrestoDistance(mDistance) : "???") +
                        ",  " + ((int) (this.position.getSpeed() * 3.6)) + " Km/h\n" +
                        "TTD: " + (timeToDest > 0 ? timeToDestString : "???");
                if (this.positionAge > 1) {
                    formattedString += ",  " +
                            this.positionAge + " Secs late\n";
                } else {
                    formattedString += "\n";
                }
            }
            */
        } else {
            if (!this.sms.equals("")) {
                formattedString += "Msg: " + RMsgMisc.unescape(this.sms) + "\n";
            }
            if (this.msgHasPosition && this.position != null) {
                formattedString += "Position: " + this.getLatLongString() + "\n" +
                        ((int) (this.position.getSpeed() * 3.6));
                if (this.positionAge > 0) {
                    formattedString += " Km/h,  " +
                            this.positionAge + " second(s) late" + "\n";
                } else {
                    formattedString += " Km/h\n";
                }
            }
        }
        //Added non-null test for crashes in Hazmat device + initialized to "" in object creation.
        if (this.pictureString != null && !this.pictureString.equals("")) {
            formattedString += "Picture: " + this.pictureString + "\n";
        }
        formattedString += "From: " + this.from + ", To: " + (this.to.equals("*") ? "All" : this.to);
        if (!this.relay.equals("")) {
            formattedString += ", Relay by: " + this.relay;
        }
        if (!this.via.equals("")) {
            formattedString += ", Via: " + this.via;
        }
        if (this.receiveDate != null 
                && (this.from.contains("=") || RMsgProcessor.isEmail(this.from) || RMsgProcessor.isCellular(this.from))) {
            //We have an email/SMS message with a received date/time in UTC, convert to local time zone
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getDefault());
            String strDate = dateFormat.format(this.receiveDate.getTime());
            formattedString += ", On " + strDate;
        } else {
            //All other cases, display the sent time instead of the received time
            if (this.fileName != null && this.fileName.indexOf(".txt") != -1) { //Error in file name (crashes in Hazmat device)
                //Normally formed file name, convert UTC to local time zone
                String dateStamp = this.fileName.substring(0, this.fileName.indexOf(".txt"));
                //We have a full date/time in UTC timezone for the receipt of this message (Msg stored on disk)
                //Example = "2017-10-25_113958";
                try {                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTime(sdf.parse(dateStamp));
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getDefault());
                    String strDate = dateFormat.format(cal.getTime());
                    formattedString += ", On " + strDate;
                } catch (ParseException e) {
                    //Debug
                    e.printStackTrace();
                }
            } else {
                formattedString += " On " + this.fileName;
            }
        }
        return formattedString;
    }



    //Take a gps position and returns a string in compressed or un-compressed format
    //format is either
    // pos:XXXXYYYYSSS,NNN\n compressed position like in APRS
    //OR
    // pos:+LL.LLLLL,+lll.lllll:SSS,NNN\n
    //with SSS = speed in Km/h and NNN the delay in GPS fix (as in the age of gps fix since request time)

    private static String formatPositionFix(RMsgLocation gpsFix, int positionAge, boolean compressedBeacon) {
        String returnPos = "";

        double latnum = gpsFix.getLatitude();
        double lonnum = gpsFix.getLongitude();
        int speednum = (int) (gpsFix.getSpeed() + 0.5f); //From m/s to km/h

        //debug test
        //latnum = -27.541944;
        //lonnum = 152.940833;
        //lonnum = -72.75;

        if (compressedBeacon) {

            double c91P3 = 91.0 * 91.0 * 91.0;
            double c91P2 = 91.0 * 91.0;
            //encode Latitude in base 91 over 4 "digits"
            latnum = 380926.0 * (90.0 - latnum);
            int lat1 = (int) (latnum / c91P3);
            latnum = latnum % c91P3;
            int lat2 = (int) (latnum / c91P2);
            latnum = latnum % c91P2;
            int lat3 = (int) (latnum / 91.0);
            latnum = latnum % 91.0;
            int lat4 = (int) latnum;
            //encode Longitude in base 91 over 4 "digits"
            lonnum = 190463 * (180.0 + lonnum);
            int lon1 = (int) (lonnum / c91P3);
            lonnum = lonnum % c91P3;
            int lon2 = (int) (lonnum / c91P2);
            lonnum = lonnum % c91P2;
            int lon3 = (int) (lonnum / 91.0);
            lonnum = lonnum % 91.0;
            int lon4 = (int) lonnum;

            int flg = 0;
            int courseint = 0; //Integer.parseInt(course);
            int speedint = 0;  //Integer.parseInt(speed);

/*
            if (latsign.equals("N")) {
                flg += 8;
            }
            if (lonsign.equals("E")) {
                flg += 4;
            }
*/
            if (courseint > 179) {
                courseint -= 180;
                flg += 33;
            }
            courseint /= 2;
            if (speedint > 89) {
                speedint -= 90;
                flg += 16;
            }
            flg += 33;

            lat1 += 33;
            lat2 += 33;
            lat3 += 33;
            lat4 += 33;
            lon1 += 33;
            lon2 += 33;
            lon3 += 33;
            lon4 += 33;
            courseint += 33;
            speedint += 33;
            int stdmsg = 0;

/*            Pattern pw = Pattern.compile("^\\s*(\\d+)(.*)");
            Matcher mw = pw.matcher(statustxt);
            if (mw.lookingAt()) {
                stdmsg = Integer.parseInt(mw.group(1));
                statustxt = mw.group(2);
            }
*/
            stdmsg += 33;
            //fixAge += 33; //translate to printable character

            returnPos += Character.toString((char)lat1);
            returnPos += Character.toString((char)lat2);
            returnPos += Character.toString((char)lat3);
            returnPos += Character.toString((char)lat4);
            returnPos += Character.toString((char)lon1);
            returnPos += Character.toString((char)lon2);
            returnPos += Character.toString((char)lon3);
            returnPos += Character.toString((char)lon4);
            //returnPos += Integer.toString(fixAge);
            returnPos += speednum + "," + positionAge;

            //+ (char) lat2 + (char) lat3 + (char) lat4
            //        + (char) lon1 + (char) lon2 + (char) lon3 + (char) lon4
            //        + fixAge;
            //      + (char)courseint + (char)speedint
            //      + Icon + (char)stdmsg + statustxt;


        } else { //Uncompressed format

/*            DecimalFormat twoPlaces = new DecimalFormat("##0.00");
            int latint = (int) latnum;
            int lonint = (int) lonnum;

            latnum = ((latnum - latint) * 60) + latint * 100;
            String latstring = twoPlaces.format(latnum);
            latstring = "0000" + latstring;
            int len = latstring.length();
            if (len > 6) {
                latstring = latstring.substring(len - 7, len);
            }

            // Make sure there is a period in there
            latstring = latstring.replace(",", ".");

            lonnum = ((lonnum - lonint) * 60) + lonint * 100;
            String lonstring = twoPlaces.format(lonnum);
            lonstring = "00000" + lonstring;

            len = lonstring.length();
            if (len > 7) {
                lonstring = lonstring.substring(len - 8, len);
            }
*/
            //Send position to 1 metre accuracy
            DecimalFormat fivePlaces = new DecimalFormat("###0.00000");
            String latstring = fivePlaces.format(latnum);
            // Make sure there is a period in there
            latstring = latstring.replace(",", ".");

            String lonstring = fivePlaces.format(lonnum);
            //make sure we have a period there
            lonstring = lonstring.replace(",", ".");

            returnPos = latstring + "," + lonstring + ":" + speednum + "," + positionAge;
            //+ Icon + statustxt;

        }
        return returnPos;
    }


/*
    //Takes a message object and returns a formatted string ready for Txing via Radio Modems
    public String formatForTx(boolean allCharsToLowerCase) {

        String chksumString = new String();
        String txBuffer;

        txBuffer = Character.toString((char)1) + this.from.toLowerCase(Locale.US) + ":" +
                this.to.toLowerCase(Locale.US) + "\n";
        if (!this.sms.equals("")) {
            txBuffer += "sms:" + RMsgMisc.escape(RMsgMisc.nocr(this.sms)) + "\n";
        }
        if (!this.via.equals("")) {
            txBuffer += "via:" + this.via.toLowerCase(Locale.US) + "\n";
        }
        if (!this.relay.equals("")) {
            txBuffer += "rly:" + this.relay.toLowerCase(Locale.US) + "\n";
        }
        if (this.msgHasPosition && this.position != null) {
            //If we reply in a mode that does not support all characters, like CCIR476, do NOT compress position
            //String lastOpMode = config.getPreferenceS("LASTMODEUSED","HF-Clubs");
            //boolean compressedBeacon = !(lastOpMode.equals("HF-Clubs"));
            boolean compressedBeacon = !allCharsToLowerCase;
            txBuffer += "pos:" + formatPositionFix(this.position,
                    this.positionAge, compressedBeacon) + "\n";
        }
        if (this.picture != null) {
            txBuffer += "pic:" + this.picture.getWidth() + "x" + this.picture.getHeight() + "," +
                    (this.pictureColour ? "Col" : "B&W") + "," +
                    Modem.speedtoSPP[this.pictureTxSPP] + "X," +
                    Modem.modemCapListString[this.pictureTxModemIndex] + "\n";
        }
        if (allCharsToLowerCase) {
            txBuffer = txBuffer.toLowerCase(Locale.US);
        }
        //Add enclosing new lines and crc
        chksumString = RMsgCheckSum.Crc16(txBuffer);
        txBuffer = txBuffer + chksumString + Character.toString((char)4);

        return txBuffer;
    }

*/
    
    

    //Takes a message object and returns a formatted string rebuilt with matching received CRC
    public String formatForRx(boolean withAccessPassword) {

        String rxBuffer = createBufferNoCRC(false, true); //No conversion to lowercase, for storage

        //Add enclosing new lines and crc
        String accessPassword = Main.relayingPassword;
        String chksumString = RMsgCheckSum.Crc16(rxBuffer + (withAccessPassword ? accessPassword : ""));
        rxBuffer = rxBuffer + chksumString + Character.toString((char)4);

        return rxBuffer;
    }


    //Takes a message object and returns a formatted string ready for Txing via Radio Modems
    public String formatForTx(boolean isCCIR476) {

        String txBuffer = createBufferNoCRC(isCCIR476, false); //Not for storage
        //Find the password for this particular message's Via station 
        //Note: it may not be the via station selected on the screen right now (if we reply to a message for example)
        String password = RMsgProcessor.getRequiredAccessPassword(this);
        System.out.println("Using Password: /" + password + "/");
        //Build the checksum using password if required
        //String chksumString = RMsgCheckSum.Crc16(txBuffer + password);
        String chksumString = "";
        if (!password.startsWith("_")) {
            //No time based password or blank password
            chksumString = RMsgCheckSum.Crc16(txBuffer + password);
        } else {
            //We have a time based password
            Long nowTimeInSecs = (System.currentTimeMillis() / 1000L + Main.deviceToRefTimeCorrection) / 10L; //epoch in 10s of seconds steps, corrected for time if available
            System.out.println("Password Time Used: " + nowTimeInSecs);
            //test nowTimeInSecs = 171234567L;
            String md5String = org.apache.commons.codec.digest.DigestUtils.md5Hex(txBuffer + password + nowTimeInSecs.toString());
            chksumString = RMsgCheckSum.Crc16(md5String);
        }
        //Add enclosing new lines and crc
        txBuffer = txBuffer + chksumString + Character.toString((char)4);
        return txBuffer;
    }


    //Takes a message object and returns a formatted string ready for Saving to file, including additional information
    public String formatForStorage(boolean allCharsToLowerCase) {

        String txBuffer = createBufferNoCRC(allCharsToLowerCase, true); //For storage (include password and mode)
        //Build the checksum WITHOUT password
        String chksumString = RMsgCheckSum.Crc16(txBuffer);
        txBuffer = txBuffer + chksumString + Character.toString((char)4);
        return txBuffer;
    }


    //Takes a message object and returns a formatted string
    public String createBufferNoCRC(boolean allCharsToLowerCase, boolean forStorage) {

        String txBuffer;

        txBuffer = Character.toString((char)1) + this.from.toLowerCase(Locale.US) + ":" +
                this.to.toLowerCase(Locale.US) + "\n";
        if (!this.sms.equals("")) {
            txBuffer += "sms:" + RMsgMisc.escape(RMsgMisc.nocr(this.sms)) + "\n";
        }
        if (!this.via.equals("")) {
            txBuffer += "via:" + this.via.toLowerCase(Locale.US) + "\n";
        }
        if (!this.relay.equals("")) {
            txBuffer += "rly:" + this.relay.toLowerCase(Locale.US) + "\n";
        }
        if (this.msgHasPosition && this.position != null) {
            //If we reply in a mode that does not support all characters, like CCIR476, do NOT compress position
            //String lastOpMode = config.getPreferenceS("LASTMODEUSED","HF-Clubs");
            //boolean compressedBeacon = !(lastOpMode.equals("HF-Clubs"));
            boolean compressedBeacon = !allCharsToLowerCase;
            txBuffer += "pos:" + formatPositionFix(this.position,
                    this.positionAge, compressedBeacon) + "\n";
        }
        if (this.picture != null) {
            txBuffer += "pic:" + this.picture.getWidth() + "x" + this.picture.getHeight() + "," +
                    (this.pictureColour ? "Col" : "B&W") + "," +
                    Modem.speedtoSPP[this.pictureTxSPP] + "X," +
                    Modem.modemCapListString[this.pictureTxModemIndex] + "\n";
        }
        if (!this.timeId.equals("")) {
            txBuffer += "id:" + this.timeId + "\n";
        }
        if (this.receiveDate != null) {
            if (forStorage) {
                //Keep the received date as a full date (not an offset to Now)
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'_'HHmmss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String strDate = dateFormat.format(this.receiveDate.getTime());
                txBuffer += "rd:" + strDate + "\n";
            } else {
                //We Tx an offset to the current UTC time in seconds (most of the time way shorter than sending a full date)
                Long nowInMillis = System.currentTimeMillis();
                Long recDateInMillis = this.receiveDate.getTimeInMillis();
                Long secs = nowInMillis - recDateInMillis;
                secs = secs / 1000;
                String strRo = secs.toString();
                txBuffer += "ro:" + strRo + "\n";
            }
        }
        if (this.sendMyTime) {
            //Time data to be sent must be generated just before sending the message to minimise Tx timing errors as messages
            // can stay in the Tx queue for a while (multiple messages, busy with a message reception, etc...)
            Long myTime = System.currentTimeMillis() / 1000; //UTC time in seconds by definition
            //Changed from last 6 digits of epoch to full epoch value to allow clocks to be widely off (anywhere between 1970 to 2038 at minimum)
            //myTime = myTime - ((Long) (myTime / 1000000L) * 1000000L); //Keep the last 6 digits representing seconds
            //int TxDelay = 0;
            //String TxDelayStr = Main.configuration.getPreference("TXDELAY", "0");
            //int dcdSecs = Integer.parseInt(Main.configuration.getPreference("DCD", "0"));
            //if (TxDelayStr.length() > 0) {
            //    TxDelay = Integer.parseInt(TxDelayStr);
            //}
            myTime += (3); //DCD and TxDelay not used for RadioMsg
            //String myTimeStr = ("0000000" + myTime.toString());
            //myTimeStr = myTimeStr.substring(myTimeStr.length() - 6);
            txBuffer += "tim:" + myTime + "\n";
        }
        if (allCharsToLowerCase) {
            txBuffer = txBuffer.toLowerCase(Locale.US);
        }
        return txBuffer;
    }



    //Takes a string and returns a message object
    //if withBinaryData is true, extracts, if available, the image or voice data
    public static RMsgObject extractMsgObjectFromString(String dataString, boolean withBinaryData, String mFilename, String rxMode) {
        RMsgObject mMessage = new RMsgObject();
        RMsgLocation mLocation = new RMsgLocation("");
        mMessage.fileName = mFilename;
        mMessage.rawRxString = dataString;
        mMessage.rxMode = rxMode;
        Pattern psc = Pattern.compile("(^\\001?[^:]+):(.+)|(\\w{4}\\004$)", Pattern.MULTILINE);
        Matcher msc = psc.matcher(dataString);
        boolean keepLooking = true;
        String group1;
        String group2;
        String group3;
        boolean foundFrom = false;
        for (int start = 0; keepLooking;) {
            keepLooking = msc.find(start);
            if (keepLooking) {
                group1 = msc.group(1);
                group2 = msc.group(2);
                group3 = msc.group(3);
                if (group1 != null &&  group2 != null) {
                    if (!foundFrom && group1.startsWith(Character.toString((char)1))) {
                        mMessage.from = group1.substring(1);
                        mMessage.to = group2;
                        foundFrom = true;
                    } else if (group1.equals("via")) {
                        mMessage.via = group2;
                    } else if (group1.equals("rly")) {
                        mMessage.relay = group2;
                    } else if (group1.equals("sms")) {
                        mMessage.sms = group2;
                    } else if (group1.equals("id")) {
                        mMessage.timeId = group2;
                    } else if (group1.equals("rd")) {
                        //We have a full date/time in UTC timezone for the receipt of this message (Msg stored on disk)
                        String rtStr = group2;
                        try {
                            //Example = "2017-10-25_113958";
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            mMessage.receiveDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            mMessage.receiveDate.setTime(sdf.parse(rtStr));
                        } catch (ParseException e) {
                            //Debug
                            e.printStackTrace();
                        }
                    } else if (group1.equals("ro")) {
                        //We have an offset to current UTC time in seconds (Rxed message)
                        Long deltaSecs = 0L;
                        try {
                            deltaSecs = Long.parseLong(group2);
                            //Example = "3662" as a receipt time, typically for an email, that was received 1 hour, 1 minute and 2 seconds ago
                            Date recDate = new Date(System.currentTimeMillis() - (deltaSecs * 1000));
                            mMessage.receiveDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            mMessage.receiveDate.setTime(recDate);
                        } catch (NumberFormatException e) {
                            //Debug
                            //e.printStackTrace();
                        }
                    } else if (group1.equals("pos")) {
                        //format is either
                        // pos:XXXXYYYYSSS,NNN\n compressed position like in APRS
                        //OR
                        // pos:+LL.LLLLL,+lll.lllll:SSS,NNN\n
                        //with SSS = speed in Km/h and NNN the delay in GPS fix in seconds
                        mMessage.positionAge = 0;
                        mLocation.setSpeed(0f);
                        //Unescape position information
                        //group2 = Messaging.unescape(group2);
                        //Uncompressed position?
                        if (group2.length()>18) {
                            //un-compressed position
                            if (group2.lastIndexOf(":") > 14 && group2.indexOf(",") > 6) {
                                int lastColumn = group2.lastIndexOf(":");
                                int lastComma = group2.lastIndexOf(",");
                                mMessage.msgHasPosition = true;
                                double latitude = 0;
                                double longitude = 0;
                                if (lastColumn != -1 && lastComma != -1 && lastComma > lastColumn) {
                                    try {
                                        String positionSpeedString = group2.substring(lastColumn + 1, lastComma);
                                        mLocation.setSpeed((float) Integer.parseInt(positionSpeedString));
                                        String positionAgeString = group2.substring(lastComma + 1);
                                        mMessage.positionAge = Integer.parseInt(positionAgeString);
                                        latitude = Double.parseDouble(group2.substring(0, group2.indexOf(",")));
                                        longitude = Double.parseDouble(group2.substring(group2.indexOf(",") + 1, lastColumn));
                                        mLocation.setLatitude(latitude);
                                        mLocation.setLongitude(longitude);
                                    } catch (Exception e) {
                                        mMessage.msgHasPosition = false;
                                    }
                                }
                            }
                        } else if (group2.length()>8) {
                            //compressed position
                            mMessage.msgHasPosition = true;
                            String posString = group2.substring(0, 8);
                            mLocation = decodeLocation(posString);
                            int lastComma = group2.lastIndexOf(",");
                            if (lastComma != -1 && lastComma > 8) {
                                try {
                                    String positionSpeedString = group2.substring(8, lastComma);
                                    mLocation.setSpeed((float) Integer.parseInt(positionSpeedString));
                                } catch (Exception e) {
                                    mLocation.setSpeed(0f);
                                }
                                if (group2.length() > lastComma + 1) {
                                    try {
                                        String positionAgeString = group2.substring(lastComma + 1);
                                        mMessage.positionAge = Integer.parseInt(positionAgeString);
                                    } catch (Exception e) {
                                        mMessage.positionAge = 0;
                                    }
                                }
                            }
                        }
                        mMessage.position = mLocation;
                    } else if (group1.equals("pic")) {
                        mMessage.pictureString = group2;
                        if (withBinaryData) {
                            //Extract data for presenting or re-sending
                            // Check this below mMessage.pictureTxModemIndex = Modem.getModeIndexFullList(Modem.getMode("MFSK32"));//Default just in case
                            int mi = Main.m.getmodeindex("MFSK-32>");
                            if (mi < Main.m.pmodes.length & mi > 0) {
                                        mMessage.pictureTxModemIndex = mi;// Main.m.pmodes[mi];
                                        //Main.RxModemString = Main.m.smodes[mi];
                            }
                            mMessage.pictureTxSPP = 2; //Default just in case
                            mMessage.pictureColour = true;

                            String[] pictureParams = group2.split(",");
                            if (pictureParams.length == 4) {
                                if (pictureParams[1].equals("B&W")) {
                                    mMessage.pictureColour = false;
                                }
                                if (!pictureParams[2].equals("")) {
                                    int speedXPos = pictureParams[2].indexOf("X");
                                    if (speedXPos != -1) {
                                        String speedS = pictureParams[2].substring(0,speedXPos);
                                        try {
                                            int speed = Integer.parseInt(speedS);
                                            if (speed > 0 && speed < 9) {
                                                mMessage.pictureTxSPP = Modem.speedtoSPP[speed];
                                            }
                                        } catch (NumberFormatException e) {
                                            //nothing, already preset
                                        }
                                    }
                                }
                                //int modeIndex = Modem.getModeIndexFullList(Modem.getMode(pictureParams[3]));
                                int modeIndex = Main.m.getmodeindex(pictureParams[3]+">");
                                if (modeIndex < Main.m.pmodes.length & modeIndex > 0) {
                                    mMessage.pictureTxModemIndex = modeIndex;// Main.m.pmodes[mi];
                                    //Main.RxModemString = Main.m.smodes[mi];
                                }

                                if (modeIndex >= Modem.minImageModeIndex && modeIndex <= Modem.maxImageModeIndex) {
                                    mMessage.pictureTxModemIndex = modeIndex;
                                }
                            }
                            //Build picture filename and extract bitmap to send
                            String pictureFn = mFilename.replace(".txt", ".png");
                            String filePath = Main.homePath + Main.dirPrefix
                                    + "RadioMsgImages" + Main.separator;
                            mMessage.picture = Bitmap.decodeFile(filePath + pictureFn);
                        }
                    } else if (group1.equals("tim")) {
                        //We have a time synchronisation data, store it.
                        //Now allows for full epoch value to be sent
                        if (group2.length() >= 1) { //at least one digit
                            //All digits and nothing else?
                            boolean formatOk = true;
                            for (int i = 0; i < group2.length(); i++) {
                                if (group2.charAt(i) < '0' || group2.charAt(i) > '9') {
                                    formatOk = false;
                                    break; //end of exercise
                                }
                            }
                            if (formatOk) {
                                //Process information
                                if (Main.lastRsidTime > 0L) {
                                    //We have a recent RSID received time, store the delta time and
                                    // the provisional time server call sign for later processing
                                    try {
                                        Long timeRef = Long.parseLong(group2);
                                        //Long timeHere = Main.lastRsidTime / 1000 - ((Long) (Main.lastRsidTime / 1000000000L) * 1000000L); //Keep the last 6 digits representing seconds
                                        //Long deltaTime = timeRef - timeHere;
                                        Long deltaTime = timeRef - Main.lastRsidTime / 1000;
                                        //Wrap around if we passed a 100000 seconds threshold
                                        //if (Math.abs(deltaTime) > 99998L) {
                                        //    deltaTime = deltaTime < 0L ? deltaTime + 1000000L : deltaTime - 1000000L;
                                        //}
                                        Main.deviceToRefTimeCorrection = deltaTime;
                                        Main.refTimeSource = mMessage.from;
                                        mMessage.sms = "*Time Reference Received*";
                                    } catch (NumberFormatException e) {
                                        //Discard. Should not happen, but to make sure
                                    }
                                }
                            }
                        }
                    }
                } else if (group3 != null) {
                    //CRC and EOT
                    String messageLessCrc = dataString.replaceFirst(group3 + "\n", ""); //For data read from file
                    messageLessCrc = messageLessCrc.replaceFirst(group3 , "");              //For data received direct from modem
                    String crcRxValue = group3.substring(0,4);
                    //No Access Password used. Note: "ssss" = CRC Fixed value for Selcall/Telcall/Pagecall
                    mMessage.crcValid = (RMsgCheckSum.checkCrcWithPasswordAndTime(messageLessCrc, "", "", crcRxValue) || crcRxValue.equals("ssss"));
                    //With Relaying Password used
                    mMessage.crcValidWithRelayPW = RMsgCheckSum.checkCrcWithPasswordAndTime(messageLessCrc, Main.relayingPassword, mFilename, crcRxValue);
                    //With IOT Access Password used
                    mMessage.crcValidWithIotPW = RMsgCheckSum.checkCrcWithPasswordAndTime(messageLessCrc, Main.IotAccessPassword, mFilename, crcRxValue);
                }
                start = msc.end();
            }
        }
        return mMessage;
    }


    //Receives a string of exactly 8 characters for a compressed location OR
// for uncompressed locations, a string representing latitude, a comma then the longitude, both in decimal degrees
    private static RMsgLocation decodeLocation(String posString) {
        RMsgLocation mLocation = new RMsgLocation("");
        double latnum = 0.0;
        double lonnum = 0.0;
        String decDegreesString = "0,0";

        //Bug: if compressed location contained a comma, would return zero
        // if (posString.indexOf(",") != -1) {  //uncompressed format
        if (posString.length() > 8) {  //uncompressed format
            String[] latlon = posString.split(",");
            try {
                latnum = Double.parseDouble(latlon[0]);
                lonnum = Double.parseDouble(latlon[1]);
            } catch (Exception e) {
                //Do nothing, returns zero
            }
        } else if (posString.length() == 8) {
            //compressed format, same as compressed APRS beacon
            String cLatString = posString.substring(0, 4);
            String cLonString = posString.substring(4, 8);
            for (int j=0; j<4; j++){
                latnum += (cLatString.charAt(j) - 33) * Math.pow(91, 3 - j);
                lonnum += (cLonString.charAt(j) - 33) * Math.pow(91, 3 - j);
            }
            latnum = 90.0 - latnum / 380926.0;
            lonnum = -180.0 + lonnum / 190463.0;

        }
        mLocation.setLatitude(latnum);
        mLocation.setLongitude(lonnum);
        return mLocation;
    }



    //Extracts data from the stored message file, and if required from the stored pictures and voice message and returns a message object
    public static RMsgObject extractMsgObjectFromFile(String folder, String mFileName, boolean withBinaryData) {

        String dataString = RMsgUtil.readFile(folder, mFileName);
        return extractMsgObjectFromString(dataString, withBinaryData, mFileName, "");//No rxMode available
    }



    //Generates a geo string for passing to goople maps, etc...
    public static String getGeoFormat(RMsgObject mMessage) {

        //On desktop must be the full Google map URI
        //E.g: https://www.google.com/maps/search/?api=1&query=47.5951518%2C-122.3316393
        String format = "";

        if (mMessage.msgHasPosition && mMessage.position != null) {
            String locationString = mMessage.getLatLongString();
            format = "https://www.google.com/maps/search/?api=1&query=" + locationString;
            /* Check URI parsing error
            String posAgeString = "";
            String posSpeedString = "";
            if (mMessage.position.getSpeed() > 0) {
                posSpeedString = " / " + (mMessage.position.getSpeed() * 3.6) + " Km/h";
            }
            if (mMessage.positionAge > 0) {
                posAgeString = " / " + mMessage.positionAge + " Secs late";
            }
            if (!mMessage.sms.equals("") || !posAgeString.equals("") || !posSpeedString.equals("")) {
                format += " " + mMessage.sms + posSpeedString + posAgeString + "";
            }
            */
        }

        return format;
    }


/* Not yet
    //Forward or re-send message currently displayed in the message popup window
    public static void forwardMessage(MsgObject mMessage) {

        mMessage.to = RadioMSG.selectedTo;
        mMessage.via = RadioMSG.selectedVia;
        //If NOT from me, list me as relay (as I am not the originator)
        if (Processor.matchMyCallWith(mMessage.from, false)) {
            mMessage.relay = "";
        } else {
            mMessage.relay = Processor.getCall();
        }

        //Queue
        txMessageList.addMessageToList(mMessage);
    }
*/


    public  final static int SORTBYNAME = 1;
    public  final static int SORTBYNAMEREVERSED = 2;

    public  final static int INBOXONLY = 1;
    public  final static int SENTONLY = 2;
    public  final static int INOUTBOXCOMBINED = 3;

 
    // Loads the list of Received messages smsview
    public static ArrayList<RMsgDisplayItem> loadFileListFromFolders(int whichFolders, int sortMethod) {
        File[] filesInbox = null;
        File[] filesSent = null;
        ArrayList<RMsgDisplayItem> displayList = new ArrayList<RMsgDisplayItem>();

        try {
            //Inbox first
            if (whichFolders == 1 || whichFolders == 3) {
                // Get the list of files in the designated folder
                File dir = new File(Main.homePath + Main.dirPrefix
                        + Main.dirInbox);
                filesInbox = dir.listFiles();
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        return file.isFile();
                    }
                };
                //Generates an array of strings containing the file names
                filesInbox = dir.listFiles(fileFilter);
            }
            //Sent items next
            if (whichFolders == 2 || whichFolders == 3) {
                // Get the list of files in the designated folder
                File dir = new File(Main.homePath + Main.dirPrefix
                        + Main.dirSent);
                filesSent = dir.listFiles();
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        return file.isFile();
                    }
                };
                //Generates an array of strings containing the file names
                filesSent = dir.listFiles(fileFilter);
            }
            //Combine both in any case
            int pos = 0;
            File[] files = new File[(filesInbox != null ? filesInbox.length : 0) + (filesSent != null ? filesSent.length : 0)];
            if (filesInbox != null) {
                for (int i = 0; i < filesInbox.length; i++) {
                    files[pos] = filesInbox[i];
                    pos++;
                }
            }
            if (filesSent != null) {
                for (int i = 0; i < filesSent.length; i++) {
                    files[pos] = filesSent[i];
                    pos++;
                }
            }

            if (sortMethod == 1 || sortMethod == 3) {//Sort by name, ignoring case
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File fileA, File fileB) {
                        if (fileA != null && fileB != null) {
                            //Just in case we have directories
                            if (fileB.isDirectory() && (!fileA.isDirectory())) return 1;
                            if (fileA.isDirectory() && (!fileB.isDirectory())) return -1;
                            return fileA.getName().toLowerCase(Locale.US).
                                    compareTo(fileB.getName().toLowerCase(Locale.US));
                        }
                        return 0;
                    }
                });
            } else { //sort by date, reversed
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File fileA, File fileB) {
                        if (fileA != null && fileB != null) {
                            //Just in case we have directories
                            if (fileB.isDirectory() && (!fileA.isDirectory())) return +1;
                            if (fileA.isDirectory() && (!fileB.isDirectory())) return -1;
                            if (fileA.lastModified() == fileB.lastModified()) return 0;
                            return (fileA.lastModified() > fileB.lastModified() ? -1 : +1);
                        }
                        return 0;
                    }
                });
            }

            String sentDirPath = Main.homePath + Main.dirPrefix + Main.dirSent;
            RMsgObject mMessage;
            for (int i = 0; i < files.length; i++) {
                //Iterate through all the files to extract the data for display
                boolean fromSentFolder = files[i].getAbsolutePath().contains(sentDirPath);
                //Choose between Sent and Inbox folders
                String msgFolder = fromSentFolder ? Main.dirSent : Main.dirInbox;
                mMessage = extractMsgObjectFromFile(msgFolder, files[i].getName(), false);//Text part only
                RMsgDisplayItem mDisplayItem = new RMsgDisplayItem(mMessage, 0f, 0f, false, fromSentFolder);
                //mDisplayItem.mMessage = mMessage;
                //Check if from Sent folder
                //if (fromSentFolder) {
                //    mDisplayItem.myOwn = true;
                //}
                displayList.add(mDisplayItem);
            }
        } catch (Exception e) {
            //loggingclass.writelog("Error when extracting Inbox or Sent Items to list." + "\nDetails: ", e, true);
        }

        return displayList;
    }


    
    // Loads the list of Received messages smsview
    public static ArrayList<String> loadStringListFromFolders(int whichFolders, int sortMethod)
    {
        File[] filesInbox = null;
        File[] filesSent = null;
        ArrayList<String> displayList = new ArrayList<String>();

        try
        {
            //Inbox first
            if (whichFolders == 1 || whichFolders == 3) {
                // Get the list of files in the designated folder
                File dir = new File(Main.homePath + Main.dirPrefix
                        + Main.dirInbox);
                filesInbox = dir.listFiles();
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file)
                    {
                        return file.isFile();
                    }
                };
                //Generates an array of strings containing the file names
                filesInbox = dir.listFiles(fileFilter);
            }
            //Sent items next
            if (whichFolders == 2 || whichFolders == 3) {
                // Get the list of files in the designated folder
                File dir = new File(Main.homePath + Main.dirPrefix
                        + Main.dirSent);
                filesSent = dir.listFiles();
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file)
                    {
                        return file.isFile();
                    }
                };
                //Generates an array of strings containing the file names
                filesSent = dir.listFiles(fileFilter);
            }
            //Combine both in any case
            int pos = 0;
            File[] files = new File[(filesInbox != null? filesInbox.length: 0) + (filesSent != null? filesSent.length: 0)];
            if (filesInbox != null) {
                for (int i=0; i<filesInbox.length; i++) {
                    files[pos] = filesInbox[i];
                    pos++;
                }
            }
            if (filesSent != null) {
                for (int i=0; i<filesSent.length; i++) {
                    files[pos] = filesSent[i];
                    pos++;
                }
            }

            if (sortMethod == 1 || sortMethod == 3) {//Sort by name, ignoring case
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File fileA, File fileB) {
                        if(fileA != null && fileB != null) {
                            //Just in case we have directories
                            if(fileB.isDirectory() && (!fileA.isDirectory())) return 1;
                            if(fileA.isDirectory() && (!fileB.isDirectory())) return -1;
                            return fileA.getName().toLowerCase(Locale.US).
                                    compareTo(fileB.getName().toLowerCase(Locale.US));
                        }
                        return 0;
                    }
                });
            } else { //sort by date, reversed
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File fileA, File fileB) {
                        if(fileA != null && fileB != null) {
                            //Just in case we have directories
                            if(fileB.isDirectory() && (!fileA.isDirectory())) return +1;
                            if(fileA.isDirectory() && (!fileB.isDirectory())) return -1;
                            if (fileA.lastModified() == fileB.lastModified()) return 0;
                            return (fileA.lastModified() > fileB.lastModified()? -1: +1);
                        }
                        return 0;
                    }
                });
            }

            String sentDirPath = Main.homePath + Main.dirPrefix + Main.dirSent;
            RMsgObject mMessage;
            for (int i = 0; i < files.length; i++)
            {
                //Iterate through all the files to extract the data for display
                boolean fromSentFolder = files[i].getAbsolutePath().contains(sentDirPath);
                //Choose between Sent and Inbox folders
                String msgFolder = fromSentFolder ? Main.dirSent : Main.dirInbox;
                mMessage = extractMsgObjectFromFile(msgFolder, files[i].getName(), false);//Text part only
                //Check this below displayItem mDisplayItem = new displayItem(mMessage, 0f, 0f, false, fromSentFolder);
                String mDisplayItem = mMessage.formatForList(fromSentFolder);
                //mDisplayItem.mMessage = mMessage;
                //Check if from Sent folder
                //if (fromSentFolder) {
                //    mDisplayItem.myOwn = true;
                //}
                displayList.add(mDisplayItem);
            }
        }
        catch (Exception e)
        {
            //loggingclass.writelog("Error when extracting Inbox or Sent Items to list." + "\nDetails: ", e, true);
        }

        return displayList;
    }



    //Build a list of strings for the Sms manage screen
    public static ArrayList<String> loadManageListFromFolder(int whichFolders, int sortMethod) {
        ArrayList<String> mList = new ArrayList<String>();
        ArrayList<String> displayList = new ArrayList<String>();

        displayList = loadStringListFromFolders(whichFolders, sortMethod);
        int length = displayList.size();
        String mDisplayItem;
        //Extract string representations for the manage list
        for(int i=0; i < length; i++) {
            mDisplayItem = displayList.get(i);
            //T be fixed
            //mList.add(mDisplayItem.mMessage.formatForList(false));
        }

        return mList;
    }

}
