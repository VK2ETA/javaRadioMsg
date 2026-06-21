/*
 * loggingclass.java  
 *   
 * Copyright (C) 2008 Pär Crusefalk (SM0RWO)  
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
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import java.util.logging.Logger;


/**
 * This class is used to log stuff to a log file
 * @author Per Crusefalk <per@crusefalk.se>
 */
public class LoggingClass {
    // local variables
    String logfile="jpskmail.log";      // The file to log to
    
    /**
     * 
     * @param filename
     */
    LoggingClass(String filename){
        // Check the file
        if (filename.length()>0){
            // Its good to have a log-extension on the file
            if (filename.lastIndexOf(".log")==-1){
                filename += ".log";
            }
            logfile = filename;
        }
    }

    /**
    * This method writes text to the log file, if it exists.
    * Please take care of exceptions where this is called.
    * @param text What to write in the log
    */
    private void WritetoLogg(String text) {
	// 
        String myLogstring;
        try {
            File lf = new File(logfile);
            // Does the file exist, then log.
            if (lf.exists())
            {
                BufferedWriter out = new BufferedWriter(new FileWriter(lf,true));
                myLogstring = getlogtime().toString();
                myLogstring = myLogstring +text;
                out.write(myLogstring);
                out.close();
            } 
        }
        catch (IOException e) {
        }    
    }

    /**
     * Create a nice timestring for the log file
     * @return
     */
    private String getlogtime()
    {
       String myTime="";
       
       // Get today's date
       Calendar now = Calendar.getInstance();
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'_'HH:mm:ss");
       myTime = formatter.toString();
       
       return myTime;
    }
    
    /**
     * 
     * @param logtext
     */
    public void writelog(String logtext){
        WritetoLogg(logtext);
    }
    
    /**
     * 
     * @param logtext
     * @param ex
     */
    public void writelog(String logtext, Exception ex){
        // get stuff from the exception object
        String msg = ex.getMessage();
	String thrownby = ex.getClass().toString();
	String cause = ex.getCause().toString();   
	String complete = "Error: "+msg + ". Source:"+thrownby+". Cause:"+cause+" Text:"+logtext;
	WritetoLogg(complete);
    }
    
    /**
     * 
     * @param logtext
     * @param ex
     * @param displaymessage
     */
    public void writelog(String logtext, Exception ex, Boolean displaymessage){
       String msg = "";
       String thrownby = "";
       String cause = "";
       String complete = "";

        // get stuff from the exception object
        if (!(ex == null)){
            if (ex.getMessage()!=null) complete = complete +"Message: "+ex.getMessage() +"\n";
            if (ex.getClass()!=null) complete = complete +"From class: "+ ex.getClass().toString()+"\n";
            if (ex.getCause()!=null) complete = complete +"Case: "+ ex.getCause().toString()+"\n";   
            complete = complete +logtext;
        }
        else
            complete = logtext;

        WritetoLogg(complete);
        Logger.getLogger(complete);
        if (displaymessage) { 
            JOptionPane.showMessageDialog(null, complete, "Problem", JOptionPane.ERROR_MESSAGE);
        } 
    }    

    public void writelog(String logtext,  Boolean displaymessage){
        // get stuff from the exception object
	String complete = logtext;
        WritetoLogg(complete);
        Logger.getLogger(complete);
        if (displaymessage) { 
            JOptionPane.showMessageDialog(null, complete, "Log Message", JOptionPane.ERROR_MESSAGE);
        } 
    }    
}
