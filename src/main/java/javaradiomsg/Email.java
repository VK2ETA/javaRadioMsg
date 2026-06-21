/*
 * email.java
 *
 * Copyright (C) 2010 Pär Crusefalk (SM0RWO)
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * This class is used to hold a single email object.
 * Feed it the raw data using the loaddata method and it will try to parse
 * the message and fill in the local variables. Use the getter methods to get
 * the message fields.
 * @author Pär Crusefalk <per at crusefalk.se>
 */
public class Email {
    private String from;
    private String to;
    private String cc;
    private String replyto;
    private String subject;
    private String datestr;
    private Date txdate;
    private Date rxdate;
    private String content="";
    private String rawmessage;
    private String[] attachments;
    private String size;
    private String filename=""; // Outbox stored emails as files
    Boolean HasAttachment = false;  // Look for the attachment text.
    private String attB64 = "content-transfer-encoding: base64";
    private String attfilename = "filename=";
    private String attachment="";
    private String bodytext=""; // Text cleaned of attachment and mime
    private ArrayList<String> myattachment;
    
    /**
     * This is the constructor for the email
     * @param payload This is the raw text you get after splitting an mbox file into
     * messages. Feed the whole message here.
     */
    public Email(String payload){
        this.rawmessage = payload;
        loaddata();
    }

    /**
     * Internal parser for raw message data, sets the locals
     */
    private void loaddata(){
        try{
            if (this.rawmessage.length()>10){
                // Create an array list for the attachment to make it easy to trim
                myattachment = new ArrayList<String>();
                Scanner myscan = new Scanner(rawmessage);
                String currentline="";
                content="";
                Boolean Contentstarted = false; // True if there is an empty line, add the rest to content then
                boolean haveFrom = false;
                boolean haveDate = false;
                boolean haveSub = false;
                
                while (myscan.hasNextLine()){
                    currentline = myscan.nextLine();
                    // use the currentline to set the local datafields
                    if (currentline.startsWith("From:") && !haveFrom) {
                        this.from = ConvertToUTF(currentline.substring(5));
                        haveFrom = true;
                    }
                    if (currentline.startsWith("To:")) this.to = ConvertToUTF(currentline.substring(3));
                    if (currentline.startsWith("Reply-To:")) this.replyto = ConvertToUTF(currentline.substring(9));
                    if (currentline.startsWith("Cc:")) this.cc = ConvertToUTF(currentline.substring(3));
                    if (currentline.startsWith("Date:") && !haveDate) {
                        this.datestr = currentline.substring(5);
                        haveDate = true;
                    }
                    if (currentline.startsWith("Subject:") && !haveSub) {
                        this.subject = ConvertToUTF(currentline.substring(8));
                        haveSub = true;
                    }
                    if (currentline.toLowerCase().contains(attB64)) HasAttachment=true;
                    if (Contentstarted) this.content += currentline +"\n"; // Add content if that has started
                    if (currentline.length()<1) Contentstarted = true;
                    if (HasAttachment) {
                        myattachment.add(currentline);
                        attachment += currentline + "\n";
                    }
                }
                // Get a readable text part
                GetCleanMsgBody();
                if (HasAttachment){
                   this.attfilename = FindFilename();
                   CleanB64Attachment();
                }
            }
        }
        catch(Exception ex)
        {
            // TBD: fixxa
        }
    }

    /**
     * Convert ugly HTML ISO 8859-1 to something UTF like
     * @param in
     * @return 
     */
    private String ConvertToUTF(String in){
        if (in.contains("=?")){
            // Start and end
            in = in.replace("=?ISO-8859-1", "");
            in = in.replace("?=", "");
            in = in.replace("=?", "");
            // Common characters
            in = in.replace("=E4", "ä");
            in = in.replace("=E5", "å");
            in = in.replace("=F6", "ö");
            in = in.replace("=C5", "Å");
            in = in.replace("=C4", "Ä");
            in = in.replace("=D6", "Ö");
            // Ascii coded
            // Start and end
            in = in.replace("=??Q?", "");
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
    
    /**
     * Look for a filename if attachment is detected
     * @return 
     */
    private String FindFilename(){
        String fname="";
        if (this.rawmessage.length()>10){
            Scanner myscan = new Scanner(rawmessage);
            String currentline="";
                while (myscan.hasNextLine()){
                    currentline = myscan.nextLine();
                    // use the currentline to set the local datafields
                    if (currentline.contains("filename=\"")) {
                        Integer n = currentline.lastIndexOf("filename=\"");
                        Integer m = currentline.indexOf("\"", n+10);
                        if (m==-1) m= currentline.length();
                        fname = currentline.substring(n+10, m);
                    } else
                    if (currentline.contains("filename=")) {
                        Integer n = currentline.lastIndexOf("filename=");
                        Integer m = currentline.indexOf("\"", n+10);
                        if (m==-1) m= currentline.length();
                        fname = currentline.substring(n+9, m);
                    }

                }
        }
        return fname;
    }

    /**
     * Remove empty lines from a Base64 attachment.
     * No try catch here, what could possibly go wrong??? ;-)
     */
    private void CleanB64Attachment(){
        // Empty the attachment
        attachment="";
        // For-each the sucker and remove lines the base64 decoder does not like
        for (String trosa : myattachment){
            Boolean append = true;
            if (trosa.length()<1) append = false;
            // Outlook crap mostly
            if (trosa.toLowerCase().contains("content-transfer-encoding")) append=false;
            if (trosa.toLowerCase().contains("content-disposition")) append=false;
            if (trosa.toLowerCase().contains("content-type")) append=false;
            if (trosa.toLowerCase().contains("filename=\"")) append=false;
            if (trosa.toLowerCase().contains("nextpart")) append=false;
            // Evolution
            if (trosa.toLowerCase().contains("--=")) append=false;
            // Google adds these
            if (trosa.toLowerCase().contains("content-id")) append=false;
            // Webmailer
            if (trosa.toLowerCase().contains("name")) append=false;
            // Add the line then, go ahead.
            if (append) attachment += trosa + "\n";
        }
    }
    
    /**
     * Get a message body that does not have mime headers and attachment text
     */
    private void GetCleanMsgBody(){
        Scanner myscan = new Scanner(content);
        Boolean append=true;
        Boolean attachstarted=false;
        String currentline="";
        while (myscan.hasNextLine()){
            append=true;
            currentline = myscan.nextLine();
            if (currentline.startsWith("Content-Transfer-Encoding")) append=false;
            if (currentline.contains("Content-Disposition:")) append=false;
            if (currentline.contains("Content-Type")) append=false;
            if (currentline.contains("filename=\"")) append=false;
            if (currentline.contains("NextPart")) append=false;
            if (currentline.startsWith("--")) append=false;
            if (currentline.startsWith("Content-Transfer-Encoding: base64")) attachstarted=true;
            // Add the line then, go ahead.
            if (append && !attachstarted) bodytext += currentline + "\n";
        }
        Scanner mycleaner = new Scanner(bodytext);
        String newbody="";
        Boolean started = false;
        while (mycleaner.hasNextLine()){
            currentline = mycleaner.nextLine();
            if (currentline.length()>0) started=true;
            if (started) newbody += currentline+ "\n";
        }
        bodytext = newbody;
    }
    
    public String getRawmessage() {
        return rawmessage;
    }

    public String getMessageTextPart(){
        return bodytext;
    }
    
    public void setRawmessage(String rawmessage) {
        this.rawmessage = rawmessage;
    }

    public String[] getAttachments() {
        return attachments;
    }

    public String getCc() {
        return cc;
    }

    public String getContent() {
        return content;
    }

    public String getDatestr() {
        return datestr;
    }

    public String getFrom() {
        return from;
    }

    public Date getRxdate() {
        return rxdate;
    }

    public String getSubject() {
        return subject;
    }

    public String getTo() {
        return to;
    }

    public String getReplyto(){
        return this.replyto;
    }

    public Date getTxdate() {
        return txdate;
    }

    public String getSize() {
        Integer storlek = rawmessage.length();
        return storlek.toString();
    }

    public void SetFileName(String name){
        filename = name;
    }

    // Used to get filename of email in outbox
    public String getFileName(){
        return filename;
    }

    public Boolean getHasAttachment() {
        return HasAttachment;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getAttfilename() {
        return attfilename;
    }
    
    public void debug(String message){
        System.out.println("Debug:" + message);
    }
}
