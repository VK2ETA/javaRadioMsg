/*
 * serverMail.java
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

import javax.mail.Message;
import javax.mail.Store;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailSSLSocketFactory;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;


/**
 *
 * @author jdouyere
 */
public class ServerMail {

   
    final static int charLimit = 10000; //Maximun number of characters of body text

    
    /* send mail
    ~SEND
    From: vk2xyz@gmail.com
    To: vk2abc@gmail.com
    Subject: Test java server
    hello there
    2nd line
    3rd line
    4th line.
    .
    .
     */    
    public static String sendMail(String fromCallsign, String toStr, String subjectStr,
            String bodyStr, String attachementFileName) {

        String result = "";
        String smtpServer = Main.configuration.getPreference("SERVERSMTPHOST");
        String socketFactoryPort = Main.configuration.getPreference("SERVERSMTPPORT", "587");
        String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
        String smtpAuth = "true";
        String smtpPort = Main.configuration.getPreference("SERVERSMTPPORT", "587");
        final String fromAddress = Main.configuration.getPreference("SERVEREMAILADDRESS");
        final String userName = Main.configuration.getPreference("SERVERUSERNAME");
        final String password = Main.configuration.getPreference("SERVERPASSWORD");
        try {
            Properties props = System.getProperties();
            props.put("mail.debug", "true");
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.socketFactory.port", socketFactoryPort);
            props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.from", fromAddress);
            //Startls OR ssl but not both?
            String smtpProtocol = Main.configuration.getPreference("SERVERSMTPPROTOCOL", "STARTTLS");
            if (smtpProtocol.equals("STARTTLS")) {
                props.put("mail.smtp.starttls.enable", "true");
                props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            } else if (smtpProtocol.equals("STARTTLS")) {
                props.put("mail.smtp.ssl.enable", "true");
                props.setProperty("mail.smtps.ssl.protocols", "TLSv1.2");
            } else {
                //Must be NONE, do nothing for now (to be tested)
            }
           props.put("mail.smtp.ssl.trust", smtpServer);
            // Accept only TLS 1.1 and 1.2
            //props.setProperty("mail.smtp.ssl.protocols", "TLSv1.1 TLSv1.2");
            //props.put("mail.smtp.socketFactory.fallback", "false");
            //Get a new instance each time as default instance conflicts with the email read section
            //Session session = Session.getDefaultInstance(props, null);
            javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            });
            session.setDebug(true);
            // -- Create a new message --
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress(fromAddress));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toStr, false));
            // -- We could include CC recipients too --
            // if (cc != null)
            // msg.setRecipients(Messaging.RecipientType.CC
            // ,InternetAddress.parse(cc, false));
            // -- Set the subject and body text --
            msg.setSubject(subjectStr);
            // -- Set some other header information --
            msg.setHeader("X-Mailer", "Radio Message Relay");
            msg.setSentDate(new Date());
            String body = bodyStr;
            //msg.setText(body);
            // creates message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            //messageBodyPart.setContent(body, "text/html");
            messageBodyPart.setContent(body, "text/plain; charset=UTF-8");
            // creates multi-part
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            // adds attachments
            /* not yet
            if (mMessage.picture != null) {
                MimeBodyPart attachPart = new MimeBodyPart();
                try {
                    String fullPath = Main.HomePath +
                            Main.Dirprefix + Main.DirImages +
                            Main.Separator + mMessage.fileName.replace(".txt", ".png");
                    attachPart.attachFile(fullPath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                multipart.addBodyPart(attachPart);
            }
             */
            // sets the multi-part as e-mail's content
            msg.setContent(multipart);
            // -- Send the message --
            //Transport.send(msg);
            // Create an SMTP transport from the session
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            // Connect to the server using credentials
            t.connect(smtpServer, userName, password);
            // Send the message
            t.sendMessage(msg, msg.getAllRecipients());
            result = "\nMessage sent...\n";
            //Update filter
            RMsgProcessor.updateEmailFilter(toStr, fromCallsign);
        } catch (Exception ex) {
            //Save in log for debugging
            Main.log("Error sending Email: " + ex.getMessage() + "\n");
            System.out.println("Error sending Email: " + ex.getMessage() + "\n");
            String errorMessage = ex.getMessage();
            if (errorMessage.indexOf("http") > 0) {
                errorMessage = errorMessage.substring(0, errorMessage.indexOf("http")) + "...";
            }
            result = "Error sending Email: " + errorMessage;
        }
        return result;
    }

    //Request emails headers from mail server
    public static String getHeaderList(int fromNumber) {

        IMAPFolder folder = null;
        Store store = null;
        String mailHeaders = "";

        try {
            Properties props = System.getProperties();
            String imapProtocol = Main.configuration.getPreference("SERVERIMAPPROTOCOL", "SSL/TLS");
            if (imapProtocol.equals("SSL/TLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                //props.setProperty("mail.imap.starttls.enable", "true");
                props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else if (imapProtocol.equals("STARTTLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imap.starttls.enable", "true");
                //props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else {
                props.setProperty("mail.store.protocol", "imap");
                props.setProperty("mail.imap.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.put("mail.imap.ssl.protocols", "TLSv1.2");
            }
            MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
            socketFactory.setTrustAllHosts(true);
            props.put("mail.imaps.ssl.socketFactory", socketFactory);
            //conflict with default instance, create a new one each time
            //Session session = Session.getDefaultInstance(props, null);
            javax.mail.Session session = javax.mail.Session.getInstance(props, null); //Conflicts with local Session.java, must be explicit
            if (imapProtocol.equals("NONE")) {
                store = session.getStore("imap");
            } else {
                store = session.getStore("imaps");
            }
            String imapHost = Main.configuration.getPreference("SERVERIMAPHOST").trim();
            String userName = Main.configuration.getPreference("SERVERUSERNAME").trim();
            String emailPassword = Main.configuration.getPreference("SERVERPASSWORD").trim();
            //store.connect("imap.googlemail.com",emailAddress, emailPassword);
            store.connect(imapHost, userName, emailPassword);
            folder = (IMAPFolder) store.getFolder("inbox");
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            Message[] messages;
            //Virtualizing the email boxes? (uses email filters to re-route responses)
            if (Main.configuration.getPreference("USEVIRTUALEMAILBOXES").trim().equals("yes")) {
                //Fetch number of messages
                messages = folder.getMessages();
                //Pre-Fetch items of interest
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add(FetchProfile.Item.CONTENT_INFO);
                fp.add(FetchProfile.Item.SIZE);
                fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                fp.add("X-mailer");
                folder.fetch(messages, fp); // Load the profile of the messages in 1 fetch.
                int mailCount = 0;
                //Process in the order of oldest to most
                //for (int i = messages.length; i > 0; i--) {
                for (int i=0; i < messages.length; i++) {
                    //Filtering by date and by requester
                    Message msg = messages[i];
                    //Save message time for making filenames later on
                    //Date msgDateTime = msg.getReceivedDate();
                    //c1.setTime(msgDateTime);
                    //From email address
                    String fromString = msg.getFrom()[0].toString();
                    //Remove name and only keep email address proper
                    fromString = RMsgProcessor.extractEmailAddress(fromString);
                    //Special case for GRIB files replies: grib requests are sent to "query@saildocs.com" but 
                    //  replies are coming from "query-reply@saildocs.com", thereby never matching the filter
                    if (fromString.equals("query-reply@saildocs.com")) {
                        fromString = "query@saildocs.com";
                    }
                    String[] tos;
                    //boolean forAll = false;
                    //if (forAll) {
                    //Option of requesting all emails regardless of who they are for
                    //Just add the requester of the QTC
                    //    tos = new String[1];
                    //    tos[0] = mMessage.from;
                    //} else {
                    //Add to reply list for each matching filter
                    tos = RMsgProcessor.passEmailFilter(fromString);
                    //}
                    //Search for email addresses we communicated with before (use email filters)
                    for (int j = 0; j < tos.length; j++) {
                        //Only if the filter matches the requesting callsign
                        if (tos[j] != null && (tos[j].equals("*") || Main.ttyCaller.toLowerCase(Locale.US).equals(tos[j].toLowerCase(Locale.US)))) {
                            mailCount++;
                            /*
                            String smsString = getBodyTextFromMessage(msg);
                            if (smsString.startsWith("\n")) {
                                smsString = smsString.substring(1);
                            }
                            if (smsString.endsWith("\r\n\r\n")) {
                                smsString = smsString.substring(0, smsString.length() - 4);
                            }
                            if (smsString.length() > charLimit) {
                                smsString = smsString.subSequence(0, charLimit - 1) + " ...>";
                            }
                            String emailSubject = msg.getSubject();
                            //Not a reply to a previous radio message, add the subject line
                            if (!emailSubject.contains("Radio Message from ")
                                    && !emailSubject.contains("SMS received from ")
                                    && !emailSubject.trim().equals("")) {
                                smsString = "Subj: " + emailSubject + "\n" + smsString;
                            }
                            */
                            if (mailCount >= fromNumber) {
                                //Build header line for that email
                                //Pskmail does not handle unicode characters (it corrupts the CRC). 
                                //  Therefore in plain text mode, strip all non ASCII characters 
                                //VK2ETA UTF-8 full support now
                                //String subjectStr = msg.getSubject().replaceAll("\u2013", "-");
                                //subjectStr = subjectStr.replaceAll("[^a-zA-Z0-9\\n\\s\\<\\>\\!\\[\\]\\{\\}\\:\\;\\\\\'\"\\/\\?\\=\\+\\-\\_\\@\\#\\+\\$\\%\\^\\&\\*,\\.\\(\\)\\|]", "~");
                                String subjectStr = msg.getSubject().replaceAll("\r", ""); //Remove carriage returns
                                //JavaMail .getSize() is not accurate. Get body size and add to the header size
                                String emailBody = getEmailTextFromMessage(msg);
                                int mSize = emailBody.length() + fromString.length() + subjectStr.length();
                                //VK2ETA: check if Perl server adds a space before the email number
                                mailHeaders += "" + (mailCount) + " " + fromString + "  " + subjectStr + " " + mSize + "\n";
                                break; //No more header for this email as this would be duplicates
                            }
                        }
                    }
                }
                if (fromNumber > mailCount) {
                    //Sorry not enough
                    mailHeaders = "Sorry only " + mailCount + " mails.\n";
                    return mailHeaders;
                }
                //Provide lead and size
                mailHeaders = "Your mail: " + mailHeaders.length() + "\n" + mailHeaders + "-end-\n";
            } else {
                //Only one "mail box" for all
                //Get the number of messages in the folder and compare
                if (fromNumber > folder.getMessageCount()) {
                    //Sorry not enough
                    mailHeaders = "Sorry only " + folder.getMessageCount() + " mails.\n";
                    return mailHeaders;
                }
                //In any case, now get all messages
                messages = folder.getMessages();
                //Pre-Fetch items of interest
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add(FetchProfile.Item.CONTENT_INFO);
                fp.add(FetchProfile.Item.SIZE);
                fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                fp.add("X-mailer");
                folder.fetch(messages, fp); // Load the profile of the messages in 1 fetch.
                //add all headers from the requested number. Protocol issue: QTC 0+ should be QTC 1+ as
                //  email number 1 is first email = email index 0 in the list 
                if (fromNumber == 0) {
                    fromNumber = 1;
                }
                for (int i = fromNumber - 1; i < messages.length; i++) { //From oldest to most recent
                    //From email address
                    String fromString = messages[i].getFrom()[0].toString();
                    //Remove name and only keep email address proper
                    String[] emailAdresses = fromString.split("[ <>]+");
                    for (String fromPart : emailAdresses) {
                        if (fromPart.indexOf("@") > 0) {
                            fromString = fromPart;
                            break;
                        }
                    }
                    //Build header line for that email
                    //Pskmail does not handle unicode characters (it corrupts the CRC). 
                    //  Therefore in plain text mode, strip all non ASCII characters 
                    //VK2ETA UTF-8 full support now
                    //String subjectStr = messages[i].getSubject().replaceAll("\u2013", "-");
                    //subjectStr = subjectStr.replaceAll("[^a-zA-Z0-9\\n\\s\\<\\>\\!\\[\\]\\{\\}\\:\\;\\\\\'\"\\/\\?\\=\\+\\-\\_\\@\\#\\+\\$\\%\\^\\&\\*,\\.\\(\\)\\|]", "~");
                    String subjectStr = messages[i].getSubject().replaceAll("\r", ""); //Remove carriage returns;
                    //JavaMail .getSize() is not accurate. Get body size and add to the header size
                    String emailBody = getEmailTextFromMessage(messages[i]).replaceAll("\r", ""); //Remove carriage returns;;
                    int mSize = emailBody.length() + fromString.length() + subjectStr.length();
                    //VK2ETA: check if Perl server adds a space before the email number
                    mailHeaders += "" + (i + 1) + " " + fromString + "  " + subjectStr + " " + mSize + "\n";
                }
                //Provide lead and size
                mailHeaders = "Your mail: " + mailHeaders.length() + "\n" + mailHeaders + "-end-\n";
            }
        } catch (Error err) {
            //err.printStackTrace();
            Main.log("Error accessing Folder: " + err.getMessage() + "\n");
            mailHeaders = "Error accessing Folder: " + err.getMessage() + "\n";
            //throw (err);
        } catch (Exception e) {
            Main.log("Error accessing emails: " + e.getMessage() + "\n");
            mailHeaders = "Error accessing emails: " + e.getMessage() + "\n";
            System.out.println("Error accessing emails: " + e.getMessage() + "\n");
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (Exception e) {
                //do nothing
            }
        }
        return mailHeaders;
    }

    //Request email count from mail server
    public static int getMailCount(String reqCallSign) {

        IMAPFolder folder = null;
        Store store = null;
        int mailCount = 0;
        String imapHost = Main.configuration.getPreference("SERVERIMAPHOST").trim();
        String userName = Main.configuration.getPreference("SERVERUSERNAME").trim();
        String emailPassword = Main.configuration.getPreference("SERVERPASSWORD").trim();

        if ((Main.wantServer || Main.wantRelayEmails) && imapHost.trim().length() > 0
                && userName.length() > 0) {
            try {
                Properties props = System.getProperties();
                String imapProtocol = Main.configuration.getPreference("SERVERIMAPPROTOCOL", "SSL/TLS");
                if (imapProtocol.equals("SSL/TLS")) {
                    props.setProperty("mail.store.protocol", "imaps");
                    props.setProperty("mail.imaps.socketFactory.port",
                            Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                    props.setProperty("mail.imaps.port",
                            Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                    //props.setProperty("mail.imap.starttls.enable", "true");
                    props.setProperty("mail.imap.ssl.enable", "true");
                    props.put("mail.imaps.ssl.protocols", "TLSv1.2");
                } else if (imapProtocol.equals("STARTTLS")) {
                    props.setProperty("mail.store.protocol", "imaps");
                    props.setProperty("mail.imaps.socketFactory.port",
                            Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                    props.setProperty("mail.imaps.port",
                            Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                    props.setProperty("mail.imap.starttls.enable", "true");
                    //props.setProperty("mail.imap.ssl.enable", "true");
                    props.put("mail.imaps.ssl.protocols", "TLSv1.2");
                } else {
                    props.setProperty("mail.store.protocol", "imap");
                    props.setProperty("mail.imap.port",
                            Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                    props.put("mail.imap.ssl.protocols", "TLSv1.2");
                }
                MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
                socketFactory.setTrustAllHosts(true);
                props.put("mail.imaps.ssl.socketFactory", socketFactory);
                //conflict with default instance, create a new one each time
                //Session session = Session.getDefaultInstance(props, null);
                javax.mail.Session session = javax.mail.Session.getInstance(props, null); //Conflicts with local Session.java, must be explicit
                if (imapProtocol.equals("NONE")) {
                    store = session.getStore("imap");
                } else {
                    store = session.getStore("imaps");
                }
                store.connect(imapHost, userName, emailPassword);
                folder = (IMAPFolder) store.getFolder("inbox");
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_ONLY);
                }
                //Get the number of messages in the folder
                if (Main.configuration.getPreference("USEVIRTUALEMAILBOXES").trim().equals("yes")) {
                    //We use the email filter to count the number of matching emails
                    Message[] messages;
                    //Fetch all messages
                    messages = folder.getMessages();
                    //Process in the order of oldest to most recent
                    for (int i=0; i < messages.length;i++) {
                        //Filtering by date and by requester
                        Message msg = messages[i];
                        //From email address
                        String fromString = msg.getFrom()[0].toString();
                        //Remove name and only keep email address proper
                        fromString = RMsgProcessor.extractEmailAddress(fromString);
                                            //Special case for GRIB files replies: grib requests are sent to "query@saildocs.com" but 
                        //  replies are coming from "query-reply@saildocs.com", thereby never matching the filter
                        if (fromString.equals("query-reply@saildocs.com")) {
                            fromString = "query@saildocs.com";
                        }
                        String[] tos;
                        //boolean forAll = false;
                        //if (forAll) {
                        //Option of requesting all emails regardless of who they are for
                        //Just add the requester of the QTC
                        //    tos = new String[1];
                        //    tos[0] = mMessage.from;
                        //} else {
                        //Add to reply list for each matching filter
                        tos = RMsgProcessor.passEmailFilter(fromString);
                        //}
                        for (int j = 0; j < tos.length; j++) {
                            //Only if the filter matches the requesting callsign (Main.ttyCaller)
                            if (tos[j] != null && (tos[j].equals("*") || reqCallSign.toLowerCase(Locale.US).equals(tos[j].toLowerCase(Locale.US)))) {
                                mailCount++;
                            }
                        }
                    }
                } else {
                    //Only one "mail box" for all
                    mailCount = folder.getMessageCount();
                }
            } catch (Error err) {
                //err.printStackTrace();
                Main.log.writelog("Error accessing Folder: " + err.getMessage() + "\n", false);
                mailCount = -1;
            } catch (Exception e) {
                Exception e1 = e;
                Main.log.writelog("Error accessing emails: " + e1.getMessage() + "\n", false);
                System.out.println("Error accessing emails: " + e.getMessage() + "\n");
                mailCount = -1;
            } finally {
                try {
                    if (folder != null && folder.isOpen()) {
                        folder.close(true);
                    }
                    if (store != null) {
                        store.close();
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        }
        return mailCount; //Means error accessing inbox mail count
    }

    /*
    //Extract body text from a message (can be multi-parts, plain text/html...)
    public static String getBodyTextFromMessage_test_Only(Message message) throws Exception {
        String result = "";
        InputStream is = message.getInputStream();
        try {
            String tempAttachFN = Main.HomePath + Main.Dirprefix + "tempAttach";
            File f = new File(tempAttachFN);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
            fos.close();
            //String encodedStr = Base64.encodeFromFile(tempAttachFN);
            result = RMsgUtil.readFile(tempAttachFN);
        } catch (Exception e) {
            Main.log.writelog("Error with attachment file(s).", e, true);
        }
        return result;
    }
     */
    //Extract body text from a message (can be multi-parts, plain text/html...)
    public static String getEmailTextFromMessage(Message message) throws Exception {
        String result = "";
        boolean haveText = false;
        int attachmentCount = 0;
        Date sentDate = message.getSentDate();
        DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        String fromString = message.getFrom()[0].toString();
        //Remove name and only keep email address proper
        String[] emailAdresses = fromString.split("[ <>]+");
        for (String fromPart : emailAdresses) {
            if (fromPart.indexOf("@") > 0) {
                fromString = fromPart;
                break;
            }
        }
        //Date sentDate = message.getSentDate();
        //DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        String emailHeader = "From: " + fromString + "\n"
                + "Date: " + dateFormat.format(sentDate).replaceAll("\\.", "") + "\n"
                + "Subject: " + message.getSubject() + "\n\n"; //Needs a blank line between header and body for proper decoding at client side
        result = emailHeader;
        if (message.isMimeType("text/plain")) {
            return emailHeader + message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) { //Limit at one attachment for now
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                //String partType = bodyPart.getContentType();
                //String partContent = bodyPart.getContent().toString();
                //result += partType + " " + partContent;
                if (bodyPart.isMimeType("multipart/*")) {
                    //-----internal search - 2nd (and last) level
                    MimeMultipart mimeMultipartIn = (MimeMultipart) bodyPart.getContent();
                    int countIn = mimeMultipartIn.getCount();
                    for (int j = 0; j < countIn; j++) {
                        BodyPart bodyPartIn = mimeMultipartIn.getBodyPart(i);

                        if (bodyPartIn.isMimeType("text/plain")) {
                            if (!haveText) {
                                result += bodyPartIn.getContent() + "\n";
                                haveText = true;
                            }
                        } else if (bodyPartIn.isMimeType("text/html")) {
                            if (!haveText) {
                                String html = (String) bodyPartIn.getContent();
                                result += Jsoup.parse(html).text() + "\n";
                                haveText = true;
                            }
                        } else {
                            String attachmentFileName = bodyPartIn.getFileName();
                            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPartIn.getDisposition())
                                    && !bodyPartIn.getFileName().equals("")
                                    && ++attachmentCount < 2) {
                                result += "filename=\"" + attachmentFileName + "\"\n"
                                        + "Content-Transfer-Encoding: base64\n\n";
                                InputStream is = bodyPartIn.getInputStream();
                                try {
                                    String tempAttachFN = Main.homePath + Main.dirPrefix + "tempAttach";
                                    File f = new File(tempAttachFN);
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                    FileOutputStream fos = new FileOutputStream(f);
                                    byte[] buf = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = is.read(buf)) != -1) {
                                        fos.write(buf, 0, bytesRead);
                                    }
                                    fos.close();
                                    String encodedStr = Base64.encodeFromFile(tempAttachFN);
                                    result += encodedStr + "\n--00000000000059725805933653d8--"; //any end format will do
                                } catch (Exception e) {
                                    Main.log.writelog("Error with attachment file(s).", e, false);
                                }
                            }
                        }
                    }
                    //End internal search
                } else if (bodyPart.isMimeType("text/plain")) {
                    if (!haveText) {
                        result += bodyPart.getContent() + "\n";
                        haveText = true;
                    }
                } else if (bodyPart.isMimeType("text/html")) {
                    if (!haveText) {
                        String html = (String) bodyPart.getContent();
                        result += Jsoup.parse(html).text() + "\n";
                        haveText = true;
                    }
                } else {
                    String attachmentFileName = bodyPart.getFileName();
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                            && !bodyPart.getFileName().equals("")
                            && ++attachmentCount < 2) {
                        result += "filename=\"" + attachmentFileName + "\"\n"
                                + "Content-Transfer-Encoding: base64\n\n";
                        InputStream is = bodyPart.getInputStream();
                        try {
                            String tempAttachFN = Main.homePath + Main.dirPrefix + "tempAttach";
                            File f = new File(tempAttachFN);
                            if (f.exists()) {
                                f.delete();
                            }
                            FileOutputStream fos = new FileOutputStream(f);
                            byte[] buf = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                            fos.close();
                            String encodedStr = Base64.encodeFromFile(tempAttachFN);
                            result += encodedStr + "\n--00000000000059725805933653d8--"; //any end format will do
                        } catch (Exception e) {
                            Main.log.writelog("Error with attachment file(s).", e, false);
                        }
                    }
                }
            }
            return result;
        } else if (message.isMimeType("text/html")) {
            String html = (String) message.getContent().toString();
            result = emailHeader + Jsoup.parse(html).text() + "\n";
            return result;
        }

        return "";
    }

    //Read one email from the IMAP server
    public static String readMail(int emailNumber, boolean compressedMail) {

        IMAPFolder folder = null;
        Store store = null;
        String returnString = "";

        try {
            Properties props = System.getProperties();
            String imapProtocol = Main.configuration.getPreference("SERVERIMAPPROTOCOL", "SSL/TLS");
            if (imapProtocol.equals("SSL/TLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                //props.setProperty("mail.imap.starttls.enable", "true");
                props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else if (imapProtocol.equals("STARTTLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imap.starttls.enable", "true");
                //props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else {
                props.setProperty("mail.store.protocol", "imap");
                props.setProperty("mail.imap.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.put("mail.imap.ssl.protocols", "TLSv1.2");
            }
            MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
            socketFactory.setTrustAllHosts(true);
            props.put("mail.imaps.ssl.socketFactory", socketFactory);
            //conflict with default instance, create a new one each time
            //Session session = Session.getDefaultInstance(props, null);
            javax.mail.Session session = javax.mail.Session.getInstance(props, null); //Conflicts with local Session.java, must be explicit
            if (imapProtocol.equals("NONE")) {
                store = session.getStore("imap");
            } else {
                store = session.getStore("imaps");
            }
            String imapHost = Main.configuration.getPreference("SERVERIMAPHOST").trim();
            String userName = Main.configuration.getPreference("SERVERUSERNAME").trim();
            String emailPassword = Main.configuration.getPreference("SERVERPASSWORD").trim();
            //store.connect("imap.googlemail.com",emailAddress, emailPassword);
            store.connect(imapHost, userName, emailPassword);
            //folder = (IMAPFolder) store.getFolder("[Gmail]/Spam"); // This doesn't work for other email account
            folder = (IMAPFolder) store.getFolder("inbox");
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            Message storedMessage = null;
            Message[] messages;
            //Virtualizing the email boxes? (uses email filters to re-route responses)
            if (Main.configuration.getPreference("USEVIRTUALEMAILBOXES").trim().equals("yes")) {
                //Fetch all messages
                messages = folder.getMessages();
                //Fetch messges data in one swoop (avoid SSL connection for each item)
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add(FetchProfile.Item.CONTENT_INFO);
                fp.add(FetchProfile.Item.SIZE);
                fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                fp.add("X-mailer");
                folder.fetch(messages, fp); // Load the profile of the messages in 1 fetch.
                int mailCount = 0;
                //Process in the order of oldest to most recent
                //for (int i = messages.length; i > 0 && storedMessage == null; i--) {
                for (int i = 0; i < messages.length && storedMessage == null; i++) {
                    //Filtering by date and by requester
                    Message msg = messages[i];
                    //From email address
                    String fromString = msg.getFrom()[0].toString();
                    //Remove name and only keep email address proper
                    fromString = RMsgProcessor.extractEmailAddress(fromString);
                    //Special case for GRIB files replies: grib requests are sent to "query@saildocs.com" but 
                    //  replies are coming from "query-reply@saildocs.com", thereby never matching the filter
                    if (fromString.equals("query-reply@saildocs.com")) {
                        fromString = "query@saildocs.com";
                    }
                    String[] tos;
                    //boolean forAll = false;
                    //if (forAll) {
                    //Option of requesting all emails regardless of who they are for
                    //Just add the requester of the QTC
                    //    tos = new String[1];
                    //    tos[0] = mMessage.from;
                    //} else {
                    //Add to reply list for each matching filter
                    tos = RMsgProcessor.passEmailFilter(fromString);
                    //}
                    //Search for email addresses we communicated with before (use email filters)
                    for (int j = 0; j < tos.length; j++) {
                        //Only if the filter matches the requesting callsign
                        if (tos[j] != null && (tos[j].equals("*") || Main.ttyCaller.toLowerCase(Locale.US).equals(tos[j].toLowerCase(Locale.US)))) {
                            mailCount++;
                            if (mailCount == emailNumber) {
                                storedMessage = msg;
                            }
                            break; //No more header for this email as this would be duplicates
                        }
                    }
                }
                //Not found a match and requested number above mail count
                if (storedMessage == null && emailNumber > mailCount) {
                    //Sorry not enough
                    returnString = "Sorry only " + mailCount + " mails.\n";
                    return returnString;
                }
            } else {
                //Only one "mail box" for all
                //Get the number of messages in the folder and compare
                if (emailNumber > folder.getMessageCount()) {
                    //Sorry not enough
                    returnString = "Error, only " + folder.getMessageCount() + " mails.\n";
                    return returnString;
                }
                //Save message for processing
                storedMessage = folder.getMessage(emailNumber);
            }
            //We should now have a message stored
            if (storedMessage != null) {
                /*
                String fromString = storedMessage.getFrom()[0].toString();
                //Remove name and only keep email address proper
                String[] emailAdresses = fromString.split("[ <>]+");
                for (String fromPart : emailAdresses) {
                    if (fromPart.indexOf("@") > 0) {
                        fromString = fromPart;
                        break;
                    }
                }
                */
                if (compressedMail) {
                    returnString = readMailZip(storedMessage);
                } else {
                    //String emailHeader = "From: " + fromString + "\n"
                    //        + "Date: " + dateFormat.format(sentDate).replaceAll("\\.", "") + "\n"
                    //        + "Subject: " + message.getSubject() + "\n";
                    //Pskmail does not handle unicode characters (it corrupts the CRC). 
                    //  Therefore in plain text mode, strip all non ASCII characters 
                    //VK2ETA full UTF-8 support
                    //returnString = getEmailTextFromMessage(storedMessage).replaceAll("\u2013", "-");
                    //returnString = returnString.replaceAll("[^a-zA-Z0-9\\n\\s\\<\\>\\!\\[\\]\\{\\}\\:\\;\\\\\'\"\\/\\?\\=\\+\\-\\_\\@\\#\\+\\$\\%\\^\\&\\*,\\.\\(\\)\\|]", "~");
                    returnString = getEmailTextFromMessage(storedMessage).replaceAll("\r", ""); //Remove carriage returns;
                    //Provide lead, size and end marker
                    returnString = "Your msg: " + returnString.length() + "\n"
                            + returnString + "\n-end-\n";
                }
            }
        } catch (Error err) {
            err.printStackTrace();
            Main.log("Error accessing Folder: " + err.getMessage() + "\n");
            returnString = "Error accessing Folder: " + err.getMessage() + "\n";
            //throw (err);
        } catch (Exception e) {
            Exception e1 = e;
            Main.log("Error accessing emails: " + e1.getMessage() + "\n");
            returnString = "Error accessing Folder: " + e1.getMessage() + "\n";
            System.out.println("Error accessing emails: " + e.getMessage() + "\n");
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (Exception e) {
                //do nothing
            }
        }
        return returnString;
    }

    //Compresses an email message and provides the exchange initiation string (">FM:") to send to the client
    public static String readMailZip(Message mMessage) {
        String returnString = "";
        String codedFile = "";
        String token = "";
        FileInputStream in = null;

        if (mMessage != null) {
            String Destination = Main.ttyCaller;
            String mysourcefile = Main.homePath + Main.dirPrefix + "tmpfile";
            try {
                String emailBody = getEmailTextFromMessage(mMessage);
                RMsgUtil.saveDataStringAsFile("", mysourcefile, emailBody); //no path as it is included in filename
            } catch (Exception e) {
            }
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
            //token = Long.toString(Math.abs(r.nextLong()), 12);
            token = Long.toString(Math.abs(r.nextLong()), 12);
            token = token.substring(token.length() - 6);
            //Can't have a "/" in the filename as in vk2eta/pm
            codedFile = Main.homePath + Main.dirPrefix + "Outpending" + Main.separator + Destination.replaceAll("\\/", "+") + "_-m-_" + token;
            Base64.encodeFileToFile(tmpfile, codedFile);
            File dlfile = new File(tmpfile);
            if (dlfile.exists()) {
                dlfile.delete();
            }
            File mycodedFile = new File(codedFile);
            if (mycodedFile.isFile()) {
                returnString = ">FM:" + Main.configuration.getPreference("CALLSIGNASSERVER") + ":" + Destination + ":"
                        + token + ":m:" + " "
                        + ":" + Long.toString(mycodedFile.length()) + "\n";
                String dataString = RMsgUtil.readFile(codedFile);
                returnString += dataString + "\n-end-\n";
            }
            /*
            File Transactions = new File(Main.transactions);
            FileWriter tr;
            try {
                tr = new FileWriter(Transactions, true);
                tr.write(returnString);
                tr.close();
            } catch (IOException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
             */
        }

        return returnString;
    }

    //Delete one email from the IMAP server
    public static String deleteMail(int emailNumber) {

        IMAPFolder folder = null;
        Store store = null;
        String returnString = "";

        try {
            Properties props = System.getProperties();
            String imapProtocol = Main.configuration.getPreference("SERVERIMAPPROTOCOL", "SSL/TLS");
            if (imapProtocol.equals("SSL/TLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                //props.setProperty("mail.imap.starttls.enable", "true");
                props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else if (imapProtocol.equals("STARTTLS")) {
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.imaps.socketFactory.port",
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imaps.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.setProperty("mail.imap.starttls.enable", "true");
                //props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imaps.ssl.protocols", "TLSv1.2");
            } else {
                props.setProperty("mail.store.protocol", "imap");
                props.setProperty("mail.imap.port", 
                        Main.configuration.getPreference("SERVERIMAPPORT", "993"));
                props.put("mail.imap.ssl.protocols", "TLSv1.2");
            }
            MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
            socketFactory.setTrustAllHosts(true);
            props.put("mail.imaps.ssl.socketFactory", socketFactory);
            //conflict with default instance, create a new one each time
            //Session session = Session.getDefaultInstance(props, null);
            javax.mail.Session session = javax.mail.Session.getInstance(props, null); //Conflicts with local Session.java, must be explicit
            if (imapProtocol.equals("NONE")) {
                store = session.getStore("imap");
            } else {
                store = session.getStore("imaps");
            }
            String imapHost = Main.configuration.getPreference("SERVERIMAPHOST");
            String userName = Main.configuration.getPreference("SERVERUSERNAME");
            String emailPassword = Main.configuration.getPreference("SERVERPASSWORD");
            //store.connect("imap.googlemail.com",emailAddress, emailPassword);
            store.connect(imapHost, userName, emailPassword);
            //folder = (IMAPFolder) store.getFolder("[Gmail]/Spam"); // This doesn't work for other email account
            folder = (IMAPFolder) store.getFolder("inbox");
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            int messageToDelete = -1;
            Message[] messages;
            //Virtualizing the email boxes? (uses email filters to re-route responses)
            if (Main.configuration.getPreference("USEVIRTUALEMAILBOXES").trim().equals("yes")) {
                //Fetch all messages
                messages = folder.getMessages();
                int mailCount = 0;
                //Process in the order of oldest to most recent
                //for (int i = messages.length; i > 0 && messageToDelete == -1; i--) {
                for (int i = 0; i < messages.length && messageToDelete == -1; i++) {
                    //Filtering by date and by requester
                    Message msg = messages[i];
                    //From email address
                    String fromString = msg.getFrom()[0].toString();
                    //Remove name and only keep email address proper
                    fromString = RMsgProcessor.extractEmailAddress(fromString);
                                        //Special case for GRIB files replies: grib requests are sent to "query@saildocs.com" but 
                    //  replies are coming from "query-reply@saildocs.com", thereby never matching the filter
                    if (fromString.equals("query-reply@saildocs.com")) {
                        fromString = "query@saildocs.com";
                    }
                    String[] tos;
                    //boolean forAll = false;
                    //if (forAll) {
                    //Option of requesting all emails regardless of who they are for
                    //Just add the requester of the QTC
                    //    tos = new String[1];
                    //    tos[0] = mMessage.from;
                    //} else {
                    //Add to reply list for each matching filter
                    tos = RMsgProcessor.passEmailFilter(fromString);
                    //}
                    //Search for email addresses we communicated with before (use email filters)
                    for (int j = 0; j < tos.length; j++) {
                        //Only if the filter matches the requesting callsign
                        if (tos[j] != null && (tos[j].equals("*") || Main.ttyCaller.toLowerCase(Locale.US).equals(tos[j].toLowerCase(Locale.US)))) {
                            mailCount++;
                            if (mailCount == emailNumber) {
                                messageToDelete = i + 1; //Message numbers in the folder start at 1
                            }
                            break; //No more header for this email as this would be duplicates
                        }
                    }
                }
                //Not found any match AND we have less messages than requested
                if (messageToDelete == -1 && emailNumber > mailCount) {
                    //Sorry not enough
                    returnString = "Sorry only " + mailCount + " mails.\n";
                    return returnString;
                }
            } else {
                //Only one "mail box" for all
                //Get the number of messages in the folder and compare
                if (emailNumber > folder.getMessageCount()) {
                    //Sorry not enough
                    returnString = "Error, only " + folder.getMessageCount() + " mails.\n";
                    return returnString;
                }
                //Save message number for processing
                messageToDelete = emailNumber;
            }
            //Process if found a valid email
            if (messageToDelete != -1) {
                //Get the selected messages
                Message message = folder.getMessage(messageToDelete);
                //Mark it for deletion
                message.setFlag(Flags.Flag.DELETED, true);
                returnString = "Deleted Email # " + emailNumber + "\n";
            }
        } catch (Error err) {
            err.printStackTrace();
            Main.log("Error accessing Folder: " + err.getMessage() + "\n");
            returnString = "Error accessing Folder: " + err.getMessage() + "\n";
            //throw (err);
        } catch (Exception e) {
            Exception e1 = e;
            Main.log("Error deleting emails: " + e1.getMessage() + "\n");
            returnString = "Error deleting emails: " + e1.getMessage() + "\n";
            System.out.println("Error accessing emails: " + e.getMessage() + "\n");
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    // expunges the folder to remove messages which are marked deleted
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (Exception e) {
                //do nothing
            }
        }
        return returnString;
    }

    //Read a web page as supplied and optionally trim with the begin: and end: strings. 
    // Returns the Text content only.
    public static String readWebPage(String webURL, String startStopStr, boolean compressedPage) {
        String webPage = "";
        String begin = "";
        String end = "";
        try {
            Pattern STm;
            Matcher st;
            //Make sure the url is properly formed. Assume www.abc is http://www.abc
            if (webURL.startsWith("www")) {
                webURL = "http://" + webURL;
            }
            Document doc = Jsoup.connect(webURL).get();
            if (doc != null) {
                //Preserve or convert line breaks
                doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                doc.select("br").append("\\n");
                doc.select("p").prepend("\\n");
                String s = doc.html().replaceAll("\\\\n", "\n");
                //webPage = doc.text();
                webPage = Jsoup.clean(s, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
            }
            //Extract begin and end limits if any
            if (startStopStr.contains("end:")) {
                STm = Pattern.compile("\\s*(begin:.*)?(end:.*)");
                st = STm.matcher(startStopStr);
                if (st.lookingAt()) {
                    if (st.group(1) != null) {
                        begin = st.group(1).replaceFirst("begin:", "").trim();
                    }
                    if (st.group(2) != null) {
                        end = st.group(2).replaceFirst("end:", "").trim();
                    }
                }
            } else {
                //No end: statement, maybe just a begin:
                STm = Pattern.compile("\\s*(begin:.*)?");
                st = STm.matcher(startStopStr);
                if (st.lookingAt()) {
                    if (st.group(1) != null) {
                        begin = st.group(1).replaceFirst("begin:", "").trim();
                    }
                }
            }
            //Trim required? begin: abc
            if (!begin.equals("")) {
                if (webPage.toLowerCase(Locale.US).contains(begin.toLowerCase(Locale.US))) {
                    int beginIndex = webPage.toLowerCase(Locale.US).indexOf(begin.toLowerCase(Locale.US));
                    //Found, trim then
                    webPage = webPage.substring(beginIndex);
                }
            }
            //Trim required? end: xyz
            if (!end.equals("")) {
                if (webPage.toLowerCase(Locale.US).contains(end.toLowerCase(Locale.US))) {
                    int endIndex = webPage.toLowerCase(Locale.US).lastIndexOf(end.toLowerCase(Locale.US));
                    //Found, trim then
                    webPage = webPage.substring(0, endIndex);
                }
            }
            if (compressedPage) {
                webPage = tgetZip(webPage);
            } else {
                //Pskmail does not handle unicode (the CRC gets corrupted), replace or strip any non ASCII character
                //VK2ETA UTF-8 Full support
                //webPage = webPage.replaceAll("\u2013", "-");
                //webPage = webPage.replaceAll("[^a-zA-Z0-9\\n\\s\\<\\>\\!\\[\\]\\{\\}\\:\\;\\\\\'\"\\/\\?\\=\\+\\-\\_\\@\\#\\+\\$\\%\\^\\&\\*,\\.\\(\\)\\|]", "~");
                webPage = "Your wwwpage: " + webPage.length() + "\n"
                        + webPage + "\n-end-\n";
            }
        } catch (Exception e) {
            webPage = "Error getting web page: " + e.getMessage() + "\n";
        }
        return webPage;
    }

    //Read a web page as supplied and optionally trim with the begin: and end: strings.
    // Returns the raw HTML code.
    public static String readRawWebPage(String webURL, String startStopStr, boolean compressedPage) {
        String webPage = "";
        String begin = "";
        String end = "";
        try {
            Pattern STm;
            Matcher st;
            //Make sure the url is properly formed. Assume www.abc is http://www.abc
            if (webURL.startsWith("www")) {
                webURL = "http://" + webURL;
            }
            Document doc = Jsoup.connect(webURL).get();
            String raw = doc.html();
            return raw;
        } catch (Exception e) {
            webPage = "Error getting web page: " + e.getMessage() + "\n";
        }
        return webPage;

    }

    //Compresses a web page or other string data and provides the exchange initiation string (">FM:") to send to the client.
    // Text content only.
    public static String tgetZip(String webPage) {
        String returnString = "";

        //String downloaddir = Main.HomePath + Main.Dirprefix + "Downloads" + Main.Separator;
        String codedFile = "";
        String token = "";
        FileInputStream in = null;

        if (webPage.length() != 0) {
            String Destination = Main.ttyCaller;
            String mysourcefile = Main.homePath + Main.dirPrefix + "tmpfile";
            try {
                RMsgUtil.saveDataStringAsFile("", mysourcefile, webPage); //no path as it is included in filename
            } catch (Exception e) {
            }
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
            //token = Long.toString(Math.abs(r.nextLong()), 12);
            token = Long.toString(Math.abs(r.nextLong()), 12);
            token = token.substring(token.length() - 6);
            //token = "tmp" + token
            //Can't have the "/" in vk2eta/pm
            //codedFile = Main.homePath + Main.dirPrefix + "Outbox" + Main.separator + Destination.replaceAll("\\/", "+") + "_-w-_" + token;
            codedFile = Main.homePath + Main.dirPrefix + "Outpending" + Main.separator + Destination.replaceAll("\\/", "+") + "_-w-_" + token;
            Base64.encodeFileToFile(tmpfile, codedFile);
            File dlfile = new File(tmpfile);
            if (dlfile.exists()) {
                dlfile.delete();
            }
            File mycodedFile = new File(codedFile);
            if (mycodedFile.isFile()) {
                // >FM:PI4TUE:PA0R:Jynhgf:w: :496
                returnString = ">FM:" + Main.configuration.getPreference("CALLSIGNASSERVER") + ":" + Destination + ":"
                        + token + ":w:" + " :" + Long.toString(mycodedFile.length()) + "\n";
                String dataString = RMsgUtil.readFile(codedFile);
                returnString += dataString + "\n-end-\n";
            }
            /*
            File Transactions = new File(Main.transactions);
            FileWriter tr;
            try {
                tr = new FileWriter(Transactions, true);
                tr.write(returnString);
                tr.close();
            } catch (IOException ex) {
                Logger.getLogger(MainPskmailUi.class.getName()).log(Level.SEVERE, null, ex);
            }
             */
        }
        return returnString;
    }

}
