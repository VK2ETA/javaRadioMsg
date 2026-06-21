/*
 * MeshGateway.java
 *
 * Copyright (C) 2018-2022 John Douyere (VK2ETA)
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

/**
 *
 * @author John Douyere
 *
 * Interface class for receiving and forwarding messages TO/FROM the Meshtastic
 * mesh network via a gateway node, using an MQTT broker as the transport
 * mechanism
 *
 * Topology: Meshtastic Node (User) on Channel X ==> Meshtastic Location 1
 * gateway node ==> MQTT Broker ==> RadioMsg Station Location 1 ==> RadioMsg
 * station 2 ==> MQTT Broker ==> Meshtastic Location 2 gateway node ==>
 * Meshtastic Location 2 Node (User) on Channel X
 *
 * Note: Meshtastic gateway node role can be: CLIENT or CLIENT_HIDDEN or ROUTER
 * or ROUTER_LATE
 *
 * Nodes: 
 * 0ee8 =      49024744 (!02ec0ee8) 
 * d4ac =       67425452 (!0404d4ac) 
 * ALL    =  4294967295 (!FFFFFFFF)
 *
 * 0ee8 to channel S (channel zero): topic: msh/V/2/json/S/!0404d4ac payload:
 * {"channel":0,"from":49024744,"hop_start":3,"hops_away":0,"id":1239474094,"payload":{"text":"Msg to S channel #1"},"rssi":-40,"sender":"!0404d4ac","snr":6,"timestamp":1781572504,"to":4294967295,"type":"text"}
 * As received by jPskmail (Note that a protobuf msg is also received at topic "msh/V/2/e/S/!0404d4ac")
 * topic: msh/V/2/json/S/!0404d4ac 
 * qos: 0
 * payload.toString: {"channel":0,"from":49024744,"hop_start":3,"hops_away":0,"id":1239474107,"payload":{"text":"Msg #2 to S ch"},"rssi":-39,"sender":"!0404d4ac","snr":6.25,"timestamp":1781574144,"to":4294967295,"type":"text"}
 *
 *
 * 0ee8 direct to d4ac topic: msh/V/2/json/PKI/!0404d4ac payload:
 * {"channel":0,"from":49024744,"hop_start":3,"hops_away":0,"id":1239474095,"payload":{"text":"Direct to d4ac #1"},"rssi":-40,"sender":"!0404d4ac","snr":6.25,"timestamp":1781573372,"to":67425452,"type":"text"}
 *
 *json format for injecting a message into the mesh
 * 1. Gemini supplied: did not work
 * Topic: 
Payload {
  "type": "sendtext",
  "from": 1239474107,
  "to": 4294967295,
  "channel": 0,
  "hop_limit": 3,
  "id": 55443322,
  "want_ack": true,
  "want_response": false,
  "payload": {
    "text": "This is a heavily configured MQTT message."
  }
}
 * 
 * Tested working #1:
 * Topic: msh/V/2/json/mqtt/!0404d4ac OR msh/V/2/json/mqtt/
 * Payload: 
 * {
  "type": "sendtext",
  "from": 67425452,
  "to": 4294967295,
  "channel": 0,
  "payload": "msg #4"
}
* 
* Note the subscription from the gateway node (firmware 2.8.0)
INFO  | 19:59:39 11 [mqtt] Subscribe to msh/V/2/e/LongFast/+
INFO  | 19:59:39 11 [mqtt] Subscribe to msh/V/2/e/mqtt/+
INFO  | 19:59:39 11 [mqtt] Subscribe to msh/V/2/e/PKI/+
* Requires Meshtastic firmware <= 2.7.5 that handles json mqtt message. 
* Restriction: the "from" value HAS to be the node id of the gateway
* This has been removed in 2.8.0. and only protobufs are available after that version.
=================================================================
* SEE https://github.com/meshtastic/mqtt-converter for conversion of json to protobuf for gateway retransmission
=================================================================
* 
 */
//import com.github.cliftonlabs.json_simple.JsonException;
//import com.github.cliftonlabs.json_simple.JsonObject;
//import com.github.cliftonlabs.json_simple.Jsoner;
import java.math.BigDecimal;
import java.util.Arrays;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

//Protobuf stuff
import com.google.protobuf.InvalidProtocolBufferException;
import org.meshtastic.proto.MQTTProtos.ServiceEnvelope; //   meshtastic.Mqtt.ServiceEnvelope;
import org.meshtastic.proto.MeshProtos.MeshPacket;
import org.meshtastic.proto.MeshProtos.Data;
import org.meshtastic.proto.Portnums.PortNum;
//Encrypt / Decrypt Meshtastic packets
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

//For publishing
import java.nio.charset.StandardCharsets;
import java.util.Random;
import com.google.protobuf.ByteString;


public class MeshGateway {

    MqttClient client;
    String broker; //= "tcp://192.168.192.50:1883";
    String clientId; // = "vk2eta-1";
    String subTopic; // = "msh/V/#";
    String pubTopic; // = "gateway";
    int subQos; // = 1;
    int pubQos; // = 1;
    // Default encryption key is often AQ== (base64 for default channel)
    // Replace this with your own AES-128 key in Base64 if you have a private channel.
    //private static final String PSK_BASE64 = "AQ=="; 
    //V channel key
    //private static final String PSK_BASE64 = "EYd6yUykVbhYgPiYHDXsB7iCZKf29WDlVejvSxSmeAg=";
    //Longfast key
    private static final String PSK_BASE64 = "NSOdQ7U8gAxny6/Sa3fCyAcScX3q3DKeBu4pisw0Jhc=";
    //mqtt key
    //private static final String PSK_BASE64 = "We+jq7vHYPgsO/7J8MirE/3S2fBhwkhJF4UR8LN1t1I=";

    MeshGateway() {
        broker = "tcp://192.168.8.50:1883";
        clientId = "vk2eta-1";
        subTopic = "msh/V/2/e/#";
        pubTopic = "gateway";
        subQos = 1;
        pubQos = 1;
        String msg = "Hello MQTT";

        try {
            client = new MqttClient(broker, clientId);
            setupMqttCallbacks();
           MqttConnectionOptions options = new MqttConnectionOptions();
            // Username and password used to connect to MQTT Broker
            options.setUserName("valda");
            options.setPassword("Elorac12".getBytes());
            // Set whether to clear the session
            options.setCleanStart(true);
            // Set the heartbeat interval in seconds
            options.setKeepAliveInterval(300);
            // Set the connection timeout in seconds
            options.setConnectionTimeout(30);
            // Set whether to automatically reconnect
            options.setAutomaticReconnect(true);
            client.connect(options);
            
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(pubQos);
            client.publish(pubTopic, message);

            //For now
            //client.disconnect();
            //client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //return true;
    }

    public void publishTextMessage(MqttClient client, String text, String channelId, String pskBase64, String gatewayNodeId) {
        try {
            int packetId = new Random().nextInt();

            // 1. Build the inner Data payload
            Data dataPayload = Data.newBuilder()
                    .setPortnum(PortNum.TEXT_MESSAGE_APP)
                    .setPayload(ByteString.copyFrom(text, StandardCharsets.UTF_8))
                    .build();
/*  For "Encrytion enabled" on gateway
            byte[] serializedData = dataPayload.toByteArray();

            // 2. Encrypt using AES-CTR 
            byte[] keyBytes = Base64.getDecoder().decode(pskBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[16];
            for (int i = 0; i < 8; i++) {
                iv[i] = (byte) ((packetId >> (8 * i)) & 0xFF);
            }

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv)); // Standard execution
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encryptedDataBytes = cipher.doFinal(serializedData);


            // 3. Wrap inside MeshPacket with valid source tracking & hop parameters
            MeshPacket meshPacket = MeshPacket.newBuilder()
                    .setId(packetId)
                    .setFrom(11223344) // MUST NOT match the gateway hardware Node ID
                    .setTo(-1)  //.setTo(4294967295L) // Broadcast (0xFFFFFFFF)
                    .setHopLimit(3) // Pass gateway's hop validation rules
                    .setHopStart(3)
                    .setEncrypted(ByteString.copyFrom(encryptedDataBytes))
                    .build();
*/

            //For "encryption NOT enabaled on gateway
            MeshPacket meshPacket = MeshPacket.newBuilder()
                .setId(packetId)
                .setFrom(11223344)       // MUST NOT match the gateway hardware Node ID
                .setTo(-1)      // Broadcast (0xFFFFFFFF)
                .setHopLimit(3)          // Pass gateway's hop validation rules
                .setHopStart(3)
                .setDecoded(dataPayload) // Pass the plaintext Data object directly here
                .build();
            
            
            
            // 4. Construct ServiceEnvelope targeting the gateway rules
            ServiceEnvelope envelope = ServiceEnvelope.newBuilder()
                    .setPacket(meshPacket)
                    .setChannelId(channelId)
                    .setGatewayId("!02ec0ee8")   //.setGatewayId("") // Leaving this blank/empty strings bypasses loopback drop blocks
                    .build();

            byte[] finalMqttPayload = envelope.toByteArray();

            // 5. Publish to the exact topic path expected by your broker topology
            //String topic = "msh/V/2/e/" + channelId + "/" + gatewayNodeId;
            //String topic = "msh/V/2/e/" + channelId + "/" + gatewayNodeId;
            String topic = "msh/V/2/e/" + channelId + "/";
            //String topic = "msh/V/2/e/mqtt" + "/" + gatewayNodeId;

            MqttMessage mqttMessage = new MqttMessage(finalMqttPayload);
            mqttMessage.setQos(1);
            mqttMessage.setRetained(false);

            System.out.println("Publishing downlink to topic: " + topic);
            client.publish(topic, mqttMessage);
            System.out.println("Downlink payload broadcast processed cleanly.");

        } catch (Exception e) {
            System.err.println("Downlink assembly rejected: " + e.getMessage());
            e.printStackTrace();
        }
    }
     
    private void processMqttMsg(String topic, MqttMessage message) {
        
         //Ignore messages we sent to the gateway (no gateway node id in the topic)
                if (!topic.contains("!")) {
            return;
        }

        try {
            // 1. Your gateway code strictly wraps everything in a ServiceEnvelope
            ServiceEnvelope envelope = ServiceEnvelope.parseFrom(message.getPayload());
            System.out.println("--- ServiceEnvelope Received ---");
            System.out.println("Gateway ID: " + envelope.getGatewayId());
            System.out.println("Channel ID: " + envelope.getChannelId());

            // 2. Extract the MeshPacket from the envelope
            MeshPacket packet = envelope.getPacket();
            System.out.println("Packet from Node ID: " + packet.getFrom());
            System.out.println("Packet ID: " + packet.getId());

            // 3. Process according to encryption settings
            if (packet.hasEncrypted()) {
                System.out.println("Payload is encrypted. Decrypting...");

                // Pass the raw byte array and the packet ID (used as the CTR counter/IV)
                byte[] decryptedBytes = decryptPayload(
                        packet.getEncrypted().toByteArray(),
                        packet.getId()
                );

                // 4. Parse the decrypted byte array as a Data object
                Data dataPayload = Data.parseFrom(decryptedBytes);

                System.out.println("Application PortNum: " + dataPayload.getPortnum());

                // PortNum 1 is TEXT_MESSAGE_APP
                if (dataPayload.getPortnum() == PortNum.TEXT_MESSAGE_APP || !dataPayload.getPayload().isEmpty()) {
                    System.out.println("Decrypted Text: " + dataPayload.getPayload().toStringUtf8());
                }

            } else if (packet.hasDecoded()) {                                                                                                                                               
                // Fallback if encryption was toggled off on the fly
                Data dataPayload = packet.getDecoded();
                //Ignore non text messages data
                if (dataPayload.getPortnum() != PortNum.TEXT_MESSAGE_APP) {
                    return;
                }
                String text = dataPayload.getPayload().toStringUtf8();
                System.out.println("Plaintext Text: " + text);
                if (!text.contains("Return")) {
                    publishTextMessage(client, "Return message of " + text, envelope.getChannelId(), PSK_BASE64, envelope.getGatewayId());
                    System.out.println("Sent mqtt message for sending over mesh");
                } else {
                    System.out.println("Should not be there");
                    return;
                }
            }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Protobuf formatting error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Decryption or processing failed: " + e.getMessage());
        }

         
      /*   
    try {
        byte[] payloadBytes = message.getPayload();

        // On 2.8.0 private uplinks, the root object is almost always a MeshPacket
        MeshPacket packet = MeshPacket.parseFrom(payloadBytes);
        System.out.println("--- New Packet Received ---");
        System.out.println("From Node ID: " + packet.getFrom());
        System.out.println("Packet ID: " + packet.getId());

        // Handle Encrypted Payload (Default Behavior)
        if (packet.hasEncrypted()) {
            System.out.println("Payload is encrypted. Decrypting...");
            
            byte[] decryptedBytes = decryptPayload(
                    packet.getEncrypted().toByteArray(),
                    packet.getId()
            );

            // Parse the decrypted inner block as Data
            Data dataPayload = Data.parseFrom(decryptedBytes);
            
            // Portnum 1 = TEXT_MESSAGE_APP
            System.out.println("Application PortNum: " + dataPayload.getPortnum());
            System.out.println("Decrypted Text String: " + dataPayload.getPayload().toStringUtf8());
            
        } 
        // Handle Plaintext/Decoded (If "Encryption Enabled" is toggled OFF on your gateway)
        else if (packet.hasDecoded()) {
            Data dataPayload = packet.getDecoded();
            System.out.println("Application PortNum: " + dataPayload.getPortnum());
            System.out.println("Plaintext Text String: " + dataPayload.getPayload().toStringUtf8());
        }

    } catch (InvalidProtocolBufferException e) {
        System.err.println("Protobuf formatting error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Decryption or processing failed: " + e.getMessage());
    }
    //Old method with envelope for comparison
           try {
            // 1. Read and Parse the Service Envelope
            ServiceEnvelope envelope = ServiceEnvelope.parseFrom(message.getPayload());
            System.out.println("Received ServiceEnvelope from Node: " + envelope.getPacket().getFrom());

            // 2. Check if the packet contains an encrypted payload
            if (envelope.getPacket().hasEncrypted()) {
                byte[] decryptedPayload = decryptPayload(
                        envelope.getPacket().getEncrypted().toByteArray(),
                        envelope.getPacket().getId()
                );

                // Parse the inner Data payload
                Data dataPayload = Data.parseFrom(decryptedPayload);
                   System.out.println("Decrypted PortNum: " + dataPayload.getPortnum());
                   System.out.println("Topic: " + topic + ",  Decrypted Text: " + dataPayload.getPayload().toStringUtf8());
               } else if (envelope.getPacket().hasDecoded()) {
                   // Handle Plaintext/Decoded (If "Encryption Enabled" is toggled OFF on your gateway)
                   Data dataPayload = envelope.getPacket().getDecoded();
                   System.out.println("Application PortNum: " + dataPayload.getPortnum());
                   System.out.println("Plaintext Text String: " + dataPayload.getPayload().toStringUtf8());
               }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Failed to parse ServiceEnvelope: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
        }
 */
}
     
       
    public void setupMqttCallbacks() {
        this.client.setCallback(new MqttCallback() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("connected to: " + serverURI);
                try {
                    // Implicitly accesses class fields 'client', 'subTopic', 'subQos'
                    client.subscribe(subTopic, subQos);
                    System.out.println("Subscribed to: " + subTopic);
                } catch (MqttException e) {
                    System.out.println("Can't subscribe on connect: " + e.getMessage());
                }
            }

            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                System.out.println("disconnected: " + disconnectResponse.getReasonString());
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                System.out.println("deliveryComplete: " + token.isComplete());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("topic: " + topic);
                System.out.println("qos: " + message.getQos());
                System.out.println("message content: " + new String(message.getPayload()));
                // Calls your processing method (must also be non-static or accessible)
                processMqttMsg(topic, message);
            }
            
            @Override
            public void mqttErrorOccurred(MqttException me) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void authPacketArrived(int i, MqttProperties mp) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    String decTo8digitHex(int decimalNumber) {

        // Convert and pad to 8 digits
        String hexString = String.format("%08X", decimalNumber);

        // Output will be: 000000FF
        //System.out.println("Hexadecimal: " + hexString);
        return hexString;
    }

     private byte[] decryptPayload(byte[] encryptedPayload, long packetId) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(PSK_BASE64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // AES-CTR typically uses the packet ID as the IV
        byte[] iv = new byte[16];
        for (int i = 0; i < 8; i++) {
            iv[i] = (byte) ((packetId >> (8 * i)) & 0xFF);
        }

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        
        return cipher.doFinal(encryptedPayload);
    }

    
    /*OLD / New Protobuf method
    private void processMqttMsg(String topic, MqttMessage message) {
        try {
            // 1. Read and Parse the Service Envelope
            ServiceEnvelope envelope = ServiceEnvelope.parseFrom(message.getPayload());
            System.out.println("Received ServiceEnvelope from Node: " + envelope.getPacket().getFrom());

            // 2. Check if the packet contains an encrypted payload
            if (envelope.getPacket().hasEncrypted()) {
                byte[] decryptedPayload = decryptPayload(
                        envelope.getPacket().getEncrypted().toByteArray(),
                        envelope.getPacket().getId()
                );

                // Parse the inner Data payload
                Data dataPayload = Data.parseFrom(decryptedPayload);
                System.out.println("Decrypted PortNum: " + dataPayload.getPortnum());
                System.out.println("Topic: " + topic + ",  Decrypted Text: " + dataPayload.getPayload().toStringUtf8());
            }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Failed to parse ServiceEnvelope: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
        }
    }

        
   
    //AI reference snippets
    Notes: 
    
    Key Considerations:
    1. Protobuf Generation: The code above relies on Google's protobuf-java compiler. You must run protoc on the .proto files provided in 
    the official Meshtastic-protobufs repository to generate the ServiceEnvelope and MeshPacket Java classes.
    
    2. Encryption Mechanism: Meshtastic uses AES-128 in CTR mode. The Initialization Vector (IV) is usually derived from the packet ID. 
    If you change channels or use your own Pre-Shared Key (PSK), it must be a 16-byte array, converted to Base64 in your code.
    
    3. Topics: The TOPIC_OUT and TOPIC_IN vary by configuration. Messages meant to be forwarded to the mesh often require the 
    topic to end with !<Node-Hex-ID>. Make sure the node's MQTT Uplink/Downlink is configured properly via the Meshtastic CLI Config.
    
    
    
    
import com.google.protobuf.InvalidProtocolBufferException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

// Note: Ensure you have compiled the Meshtastic .proto files to Java classes
// using `protoc` and imported them here.
import meshtastic.MeshProtos.ServiceEnvelope;
import meshtastic.MeshProtos.MeshPacket;
import meshtastic.MeshProtos.Data;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class MeshtasticMqttHandler {

    private static final String BROKER_URL = "tcp://mqtt.meshtastic.org:1883";
    private static final String CLIENT_ID = "JavaMeshClient";
    private static final String TOPIC_IN = "msh/2/c/mqtt/#"; // Topic to listen to
    private static final String TOPIC_OUT = "msh/2/c/mqtt/"; // Topic to publish to
    
    // Default encryption key is often AQ== (base64 for default channel)
    // Replace this with your own AES-128 key in Base64 if you have a private channel.
    private static final String PSK_BASE64 = "AQ=="; 

    private MqttClient mqttClient;

    public void start() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(BROKER_URL, CLIENT_ID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            mqttClient.connect(connOpts);
            System.out.println("Connected to MQTT broker.");

            // Subscribe to mesh traffic
            mqttClient.subscribe(TOPIC_IN, this::processMessage);
            
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String topic, MqttMessage message) {
        try {
            // 1. Read and Parse the Service Envelope
            ServiceEnvelope envelope = ServiceEnvelope.parseFrom(message.getPayload());
            System.out.println("Received ServiceEnvelope from Node: " + envelope.getPacket().getFrom());

            // 2. Check if the packet contains an encrypted payload
            if (envelope.getPacket().hasEncrypted()) {
                byte[] decryptedPayload = decryptPayload(
                        envelope.getPacket().getEncrypted().toByteArray(), 
                        envelope.getPacket().getId()
                );
                
                // Parse the inner Data payload
                Data dataPayload = Data.parseFrom(decryptedPayload);
                System.out.println("Decrypted PortNum: " + dataPayload.getPortnum());
                System.out.println("Decrypted Text: " + dataPayload.getPayload().toStringUtf8());
            }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Failed to parse ServiceEnvelope: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
        }
    }

    private byte[] decryptPayload(byte[] encryptedPayload, long packetId) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(PSK_BASE64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // AES-CTR typically uses the packet ID as the IV
        byte[] iv = new byte[16];
        for (int i = 0; i < 8; i++) {
            iv[i] = (byte) ((packetId >> (8 * i)) & 0xFF);
        }

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        
        return cipher.doFinal(encryptedPayload);
    }

    public void sendMessageToMesh(String textMessage, int destinationNodeId) {
        try {
            // Build the Data packet
            Data data = Data.newBuilder()
                    .setPortnum(3) // 3 is TEXT_MESSAGE_APP
                    .setPayload(com.google.protobuf.ByteString.copyFromUtf8(textMessage))
                    .build();

            // Build the MeshPacket wrapper
            MeshPacket packet = MeshPacket.newBuilder()
                    .setDestination(destinationNodeId)
                    .setWantAck(true)
                    .setHopLimit(3)
                    .setEncrypted(com.google.protobuf.ByteString.copyFromUtf8("...")) // Requires encryption if not using default
                    .build();

            // Build the ServiceEnvelope
            ServiceEnvelope envelope = ServiceEnvelope.newBuilder()
                    .setPacket(packet)
                    .build();

            MqttMessage mqttMessage = new MqttMessage(envelope.toByteArray());
            mqttMessage.setQos(1);
            
            // Publish to node's hex id
            mqttClient.publish(TOPIC_OUT + "!hex_node_id", mqttMessage);
            System.out.println("Sent message to mesh via MQTT.");
            
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MeshtasticMqttHandler handler = new MeshtasticMqttHandler();
        handler.start();
    }
}
*/
    
    
       
    /* Old JSON method
    
    void processMqttMsg(String topic, MqttMessage message) {
        final String topicFinal = topic;
        final MqttMessage messageFinal = message;

        Thread myMqttMsgReceivedThread = new Thread() {

            @Override
            public void run() {
                boolean allOK = true;
                BigDecimal from;
                int fromInt = 0;
                String channel;
                String type;


                String[] topicArray = topicFinal.split("\\/");
                if ((topicArray[0] + "/" + topicArray[1]).equals(subTopic) && topicArray[2].equals("2") && topicArray[2].equals("json")) {
                    //We have a valid json message received over Lora, process it
                    //Ex: {"channel":0,"from":49024744,"hop_start":3,"hops_away":0,"id":1239474107,"payload":{"text":"Msg #2 to S ch"},"rssi":-39,"sender":"!0404d4ac","snr":6.25,"timestamp":1781574144,"to":4294967295,"type":"text"}
                    //OR {"channel":0,"from":49024744,"hop_start":3,"hops_away":0,"id":1239474095,"payload":{"text":"Direct to d4ac #1"},"rssi":-40,"sender":"!0404d4ac","snr":6.25,"timestamp":1781573372,"to":67425452,"type":"text"}

                    try {
                        String payload = new String(messageFinal.getPayload());
                        JsonObject rootObject = (JsonObject) Jsoner.deserialize(payload);
                        if (rootObject != null) {
                            channel = (String) rootObject.get("channel");
                            type = (String) rootObject.get("type");
                            from = ((BigDecimal) rootObject.get("from"));
                            JsonObject payloadObject = (JsonObject) rootObject.get("payload");
                            if (from != null && payloadObject != null && channel != null && type != null && type.equals("text")) {
                                String textValue = (String) payloadObject.get("text");
                                fromInt = from.intValue();
                                if (textValue != null) {
                                    System.out.println("Extracted msg Topic: " + topicFinal);
                                    System.out.println("Extracted msg Text: " + textValue);
                                    System.out.println("Extracted channel: " + channel);
                                    System.out.println("Extracted from: " + decTo8digitHex(fromInt));

                                    if (topicArray[4].equals("PKI")) {
                                        //We have a direct message
                                    } else {
                                        //We have a message to a channel, forward it to the selected relays
                                        
                                    }
                                }
                            }
                        }
                    } catch (JsonException e) {
                        System.err.println("Cliftonlabs JSON parsing failed: " + e.getMessage());
                    }
                }
            }
        };
        myMqttMsgReceivedThread.run();
    }
*/
    
    
    
}
