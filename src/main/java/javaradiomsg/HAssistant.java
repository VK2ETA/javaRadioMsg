/*
 * HAssistant.java  
 *   
 * Copyright (C) 2025-2025 John Douyere (VK2ETA)  
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

//REST API to Home Assistant and JSON library
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jdouyere
 */
public class HAssistant {

    //Replies with the state of the supplied entiry
    public static void getState(RMsgObject message, String entity) {
        final RMsgObject mMessage = message;

        //Extract the entity id from the message sms field. Example: "*get? sensor.main_battery_ah"
        Pattern pident = Pattern.compile("\\s*(\\w+\\.\\w+)");
        Matcher mident = pident.matcher(entity.trim());
        String entityId = "";
        if (mident.lookingAt()) {
            entityId = mident.group(1);
        }
        final String entityIdFinal = entityId;
        //Make the call to the Home Assistant server
        Thread myHaIotRequestThread = new Thread() {
            @Override
            public void run() {
                String response = "";
                boolean allOK = true;
                String result = "";

                try {
                    String homeAssistantServerAddress = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTIPADDRESS", "").trim());
                    String longLivedToken = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTLONGLIVEDTOKEN", "").trim());
                    String httpRequest = "http://" + homeAssistantServerAddress + ":8123/api/states/" + entityIdFinal;
                    response = HttpRequest.get(httpRequest)
                            .authorization("Bearer " + longLivedToken)
                            .contentType("application/json").body();
                    System.out.println("Response was: " + response);
                    if (response.matches("4\\d\\d\\:.*")) {
                        result = response.length() > 31 ? response.substring(0, 31) : response;
                        allOK = false;
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    result = e.getMessage();
                    System.out.println("Error:" + e.getMessage());
                    result = result.length() > 31 ? result.substring(0, 31) : result;
                    allOK = false;
                }
                JsonObject obj = null;
                if (allOK) {
                    obj = Jsoner.deserialize(response, new JsonObject());

                    if (obj.get("message") != null && ((String) obj.get("message")).length() > 0) {
                        allOK = false;
                        result = (String) obj.get("message");
                    }
                    if (allOK) {
                        String id = (String) obj.get("entity_id");
                        JsonObject attrib = (JsonObject) obj.get("attributes");
                        String fname = "";
                        String UOM = "";
                        if (attrib != null) {
                            fname = (String) attrib.get("friendly_name");
                            if (fname == null) fname = "";
                            UOM = (String) attrib.get("unit_of_measurement");
                            if (UOM == null) UOM = "";
                        }
                        String val = (String) obj.get("state");

                        result = (fname.length() == 0 ? id : fname) + ": " + val + UOM;
                    }
                }
                if (allOK) {
                    System.out.println("Send back: " + result);
                    RMsgUtil.replyWithText(mMessage, result);
                } else {
                    System.out.println("Error: " + result);
                    RMsgUtil.replyWithText(mMessage, "Error: " + result);
                }
            }
        };
        myHaIotRequestThread.run();
    }


    //Runs the specified service domain and action on the supplied entiry. Returns a confirmation that the service was run or not.
    public static void runAction(RMsgObject message, String entityId, String action) {
        final RMsgObject mMessage = message;

        final String entityIdFinal = entityId;
        final String actionFinal = action;

        Thread myHaIotRequestThread = new Thread() {
            @Override
            public void run() {
                String response = "";
                boolean allOK = true;
                String jsonData = "{\"entity_id\":\"" + entityIdFinal + "\"}";
                String result = "";
                try {
                    String homeAssistantServerAddress = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTIPADDRESS", "").trim());
                    String longLivedToken = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTLONGLIVEDTOKEN", "").trim());
                    response = HttpRequest.post("http://" + homeAssistantServerAddress + ":8123/api/services/" + actionFinal)
                            .authorization("Bearer " + longLivedToken)
                            .contentType("application/json")
                            .send(jsonData).body();
                    System.out.println("Response was: " + response);
                    if (response.matches("4\\d\\d\\:.*")) {
                        result = response.length() > 31 ? response.substring(0, 31) : response;
                        allOK = false;
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    result = e.getMessage();
                    result = result.length() > 31 ? result.substring(0, 31) : result;
                    allOK = false;
                }
                JsonObject obj = null;
                if (allOK) {
                    //Remove enclosing "[" and "]"
                    if (response.startsWith("[")) {
                        response = response.substring(1, response.length() - 1);
                    }
                    obj = Jsoner.deserialize(response, new JsonObject());
                    if (obj.get("message") != null && ((String) obj.get("message")).length() > 0) {
                        allOK = false;
                        result = (String) obj.get("message");
                    }
                    if (allOK) {
                        String id = (String) obj.get("entity_id");
                        JsonObject attrib = (JsonObject) obj.get("attributes");
                        String fname = "";
                        if (attrib != null) {
                            fname = (String) attrib.get("friendly_name");
                            if (fname == null) fname = "";
                        }
                        String val = (String) obj.get("state");
                        //String UOM = (String) attrib.get("unit_of_measurement");
                        result = (fname.length() == 0 ? id : fname) + ": " + val;
                    }
                }
                if (allOK) {
                    System.out.println("Send back: " + result);
                    RMsgUtil.replyWithText(mMessage, result);
                } else {
                    System.out.println("Error: " + result);
                    RMsgUtil.replyWithText(mMessage, "Error: " + result);
                }
            }
        };
        myHaIotRequestThread.run();
    }

    
/* Parked code - not checked
    ////Sets the state of the supplied entiry (but does not communicate with the underlying device if one is present)
    public static void setState(RMsgObject message) {
        final RMsgObject mMessage = message;
        //Extract the entity id and service domain and action from the message sms field. example: "*set? nput_number.living_room_aircon_current_set_point_temp 22"
        Pattern pident = Pattern.compile("\\*set\\?\\s+(\\w+\\.\\w+)\\s+(\\w+)");
        Matcher mident = pident.matcher(message.sms);
        String entityId = "";
        String state = "";
        if (mident.lookingAt()) {
            entityId = mident.group(1);
            state = mident.group(2);
        }
        final String entityIdFinal = entityId;
        final String stateFinal = state;

        Thread myHaIotRequestThread = new Thread() {
            @Override
            public void run() {
                String response = "";
                boolean allOK = true;
                String jsonData = "{\"state\":\"" + stateFinal + "\"}";
                String result = "";
                try {
                    String homeAssistantServerAddress = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTIPADDRESS", "").trim());
                    String longLivedToken = RMsgMisc.ltrim(Main.configuration.getPreference("HOMEASSISTANTLONGLIVEDTOKEN", "").trim());
                    response = HttpRequest.post("http://" + homeAssistantServerAddress + ":8123/states/" + entityIdFinal)
                            .authorization("Bearer " + longLivedToken)
                            .contentType("application/json")
                            .send(jsonData).body();
                    System.out.println("Response was: " + response);
                    if (response.matches("4\\d\\d\\:.*")) {
                        result = response.length() > 31 ? response.substring(0, 31) : response;
                        allOK = false;
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    result = e.getMessage();
                    result = result.length() > 31 ? result.substring(0, 31) : result;
                    allOK = false;
                }
                JsonObject obj = null;
                if (allOK) {
                    //Remove enclosing "[" and "]"
                    if (response.startsWith("[")) {
                        response = response.substring(1, response.length() - 1);
                    }
                    obj = Jsoner.deserialize(response, new JsonObject());
                    if (obj.get("message") != null && ((String) obj.get("message")).length() > 0) {
                        allOK = false;
                        result = (String) obj.get("message");
                    }
                    if (allOK) {
                        String id = (String) obj.get("entity_id");
                        JsonObject attrib = (JsonObject) obj.get("attributes");
                        String fname = "";
                        if (attrib != null) {
                            fname = (String) attrib.get("friendly_name");
                        }
                        String val = (String) obj.get("state");
                        //String UOM = (String) attrib.get("unit_of_measurement");
                        result = (fname.length() == 0 ? id : fname) + ": " + val;
                    }
                }
                if (allOK) {
                    System.out.println("Send back: " + result);
                    RMsgUtil.replyWithText(mMessage, result);
                } else {
                    System.out.println("Error: " + result);
                    RMsgUtil.replyWithText(mMessage, "Error: " + result);
                }
            }
        };
        myHaIotRequestThread.run();
    }
*/
    
    
}
