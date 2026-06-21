/*
 * RMsgTxList.java
 *
 * Copyright (C) 2016-2021 John Douyere (VK2ETA)
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

import java.util.LinkedList;

/**
 * Message objects list for gps position updates (when available) and TXing
 */

public class RMsgTxList {

    public static final LinkedList<RMsgObject> messageList = new LinkedList<RMsgObject>();


    //Get length of message list
    synchronized public static int getLength() {

        return messageList.size();
    }



    //Get length of message list
    synchronized public static int getAvailableLength() {
        int availableforTx = 0;
        RMsgObject messageObject = null;
        int listLength = messageList.size();

        //Iterate through the list and count only the locations with updated gps coordinates
        for (int ii=0; ii < listLength; ii++) {
            messageObject = (RMsgObject) messageList.get(ii);
            if ((!messageObject.msgHasPosition || (messageObject.msgHasPosition && messageObject.position != null))
                    && !messageObject.sent) {
                availableforTx++;
            }
        }
        return availableforTx;
    }



    //Get last message of list (the oldest in the FIFO queue)
    //Must be consistent in regards to gps location information
    // Therefore ignore objects that are awaiting location updates
    synchronized public static RMsgObject getOldest() {
        RMsgObject messageObject = null;
        int listLength = messageList.size();

        if (listLength > 0) {
            //Iterate through the list and count only the locations with updated gps coordinates
            for (int ii=listLength - 1; ii >= 0; ii--) {
                messageObject = (RMsgObject) messageList.get(ii);
                if ((!messageObject.msgHasPosition || (messageObject.msgHasPosition && messageObject.position != null))
                        && !messageObject.sent) {
                    //Mark the object as sent as this is called from the send function
                    messageObject.sent = true;
                    messageList.set(ii, messageObject);
                    return (RMsgObject) messageObject;  //.remove(ii);
                }
            }
        }
        return null;
    }

    //Get last message of list (the oldest in the FIFO queue) which has been marked as SENT
    synchronized public static RMsgObject getOldestSent() {
        synchronized (messageList) {
            RMsgObject messageObject = null;
            int listLength = messageList.size();

            if (listLength > 0) {
                //Iterate through the list and return the oldest item marked as sent
                for (int ii = listLength - 1; ii >= 0; ii--) {
                    messageObject = (RMsgObject) messageList.get(ii);
                    if ((!messageObject.msgHasPosition || (messageObject.msgHasPosition && messageObject.position != null))
                            && messageObject.sent) {
                        return (RMsgObject) messageList.get(ii);
                    }
                }
            }
            return null;
        }
    }

    //GET AND REMOVE last message of list (the oldest in the FIFO queue) which has been marked as SENT
    synchronized public static RMsgObject removeOldestSent() {
        synchronized (messageList) {
            RMsgObject messageObject = null;
            int listLength = messageList.size();

            if (listLength > 0) {
                //Iterate through the list and return the oldest item marked as sent
                for (int ii = listLength - 1; ii >= 0; ii--) {
                    messageObject = (RMsgObject) messageList.get(ii);
                    if ((!messageObject.msgHasPosition || (messageObject.msgHasPosition && messageObject.position != null))
                            && messageObject.sent) {
                        return (RMsgObject) messageList.remove(ii);
                    }
                }
            }
            return null;
        }
    }

    //Get (and remove) the earliest message in the list (the youngest in the FIFO queue)
    // Includes objects that are awaiting location updates
    synchronized public static RMsgObject getLatest() {
        int listLength = messageList.size();

        if (listLength > 0) {
            return (RMsgObject) messageList.remove(0);
        }
        return null;
    }

    //Iterate through the list and update the locations with currently
    // passed location if the boolean msgHasPosition is true AND the location is null
    synchronized public static void updateLocation(RMsgLocation msgLocation) {
        RMsgObject messageObject = null;
        int listLength = messageList.size();

        //Iterate through the list and update the locations with currently
        // passed location if the boolean msgHasPosition is true AND the location is null
        for (int ii = 0; ii < listLength; ii++) {
            messageObject = (RMsgObject) messageList.get(ii);
            if (messageObject.msgHasPosition && messageObject.position == null) {
                messageObject.position = msgLocation;
                messageObject.positionAge = (int) ((System.currentTimeMillis() - messageObject.positionRequestTime) / 1000);
                messageList.set(ii, messageObject);
            }
        }
    }

    //Without image data
    synchronized public static void addMessageToList(String msgTo, String via, String msgSms,
            Boolean msgHasPosition, RMsgLocation msgLocation, long positionRequestTime,
            Short[] msgVoiceMessage) {

        RMsgObject messageObject = new RMsgObject(msgTo, via, msgSms, null, 0, false, 0, msgHasPosition,
                msgLocation, positionRequestTime, msgVoiceMessage);
        messageObject.from = RMsgProcessor.getCall();
        messageList.addFirst(messageObject);
    }

    //With image data
    synchronized public static void addMessageToList(String msgTo, String via, String msgSms,
                                                          Bitmap msgPicture, int pictureTxSPP, boolean pictureColour, int imageTxModemIndex,
                                                          Boolean msgHasPosition, RMsgLocation msgLocation, long positionRequestTime) {
        RMsgObject messageObject = null;

        messageObject = new RMsgObject(msgTo, via, msgSms,
                msgPicture, pictureTxSPP, pictureColour, imageTxModemIndex,
                msgHasPosition, msgLocation, positionRequestTime, null);
        messageObject.from = RMsgProcessor.getCall();
        messageList.addFirst(messageObject);
    }



    //Add pre-created message object (e.g when forwarding)
    synchronized public static void addMessageToList(RMsgObject mMessage) {

        messageList.addFirst(mMessage);
    }

}
