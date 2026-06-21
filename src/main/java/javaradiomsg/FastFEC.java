/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javaradiomsg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rein
 */
public class FastFEC {

    private RxBlock myrxb;
    private static String[] WorkingArray;
    private int Index = 0;
    private String direct;
    private String sb = "";
   String Filename = "";
   Arq myarq;
    // extract the data from the temp file
    
    public FastFEC(){
        // constructor
        WorkingArray = new String [200];
        myrxb = new RxBlock("");
        //Slows it down: re-use public arq in Main myarq = new arq();
    }
    
    public  void fastfec2 (String infile, String outfile) throws FileNotFoundException, IOException {
        try{
            FileInputStream fstream = new FileInputStream(infile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine = "";
            String strFirst = "";
            String strCRC = "";
            String strOut = "";
            Filename = "";
            
            direct = "eotainlrscdfhmpu";
            
            
            while ((strLine = br.readLine()) != null)   {

                                    Pattern pfn = Pattern.compile("^<SOH>(FSXX21\\sEGRR\\s.*)$");
                                    Matcher mfn = pfn.matcher(strLine);
                                    if (mfn.lookingAt()) {
                                       Filename = mfn.group(1); 
                                       WorkingArray[0] = Filename;
                                    }
                
                                   Pattern pf = Pattern.compile("^<SOH>([eotainlrscdfhmpu][eotainlrscdfhmpu]\\s.*)$");
                                    Matcher mf = pf.matcher(strLine);
                                    if (mf.lookingAt()) {
                                       strFirst = mf.group(1); 
                                    }
                                    Pattern pc = Pattern.compile("^([0123456789ABCDEF]{4})<\\w\\w\\w>$");
                                    Matcher mc = pc.matcher(strLine);
                                    if (mc.lookingAt()) {
                                       strCRC = mc.group(1); 
                                       if (strFirst.length() > 3){
                                           String CRC =myrxb.checksum(strFirst + "\n");
                                           if (CRC.equals(strCRC)) {
                                               Index = stringToIndex(strFirst.substring(0,2));
                                               WorkingArray[Index] = strFirst.substring(3);
                                           } else {
 // faulty line...
                                                String aa = RepairLine(strFirst);
                                             if (aa.length() > 3) {
                                                 WorkingArray[Index] = aa.substring(3);
                                             }
                                            
                                           }
                                           strOut = "";
                                           strFirst = "";
                                           strCRC = "";
                                       }
                                    }
            } // end read loop
    
            in.close();
        } catch ( FileNotFoundException efnf){
            System.err.println("File Not Found Error:" + efnf.getMessage());
        } catch (IOException eio) {
            System.err.println("IO Error:" + eio.getMessage());
        }catch (Exception e){//Catch exception if any
            System.err.println("Unknown Error: " + e.getMessage());
        }
 // output
            String zygribfile = "";
            Date todaysDate = new java.util.Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String formattedDate = formatter.format(todaysDate);    
        
            Pattern pn = Pattern.compile("(FSXX21)\\sEGRR\\s(\\d+)");
            Matcher mn = pn.matcher(Filename);
            if (mn.lookingAt()){
                   zygribfile +=  mn.group(1);
                   zygribfile += "_";
                   zygribfile += mn.group(2);
                   zygribfile += "_";
                   zygribfile += formattedDate;
            } 
            String Outfile = "";
            if (Main.separator.equals("/")) {
                try {
                    File f1 = new File("/opt/zyGrib/grib/");
                    if (f1.isDirectory()) {
                        if (zygribfile.length() > 0) {
                            Outfile ="/opt/zyGrib/grib/" + zygribfile;
                        } else {
                            Outfile = "/opt/zyGrib/grib/error";
                        }
                    } else {          
                        Outfile = Main.homePath + Main.dirPrefix + zygribfile;
                    }   
                }
                catch (Exception e) {    
                    Outfile = Main.homePath + Main.dirPrefix + zygribfile;
                    //myarq.Message("Outfile=" + Outfile, 10);
                    Main.q.Message("Outfile=" + Outfile, 10);
                }
            }
        

     FileWriter fw = new FileWriter(Outfile);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter pw = new PrintWriter(bw, false);

        pw.print(Filename + "\n");
        
        for (int i = 1; i < Index+1; i++) {
          try{  
            if (WorkingArray[i] != null & i % 4 != 0){
                String st = WorkingArray[i];
                String sto = st.substring(0,8) + st.substring(9,17) + st.substring(18,26) +st.substring(27,35) ;
                String outstring = convertToIAC(sto);
                pw.print(outstring);
//                System.out.print(outstring);
            }
          }
          catch (Exception e){
              // do nothing
          }
        }
        pw.close();
        // delete the input file
        
    }
// converts line code to number    
    private int stringToIndex (String str) {
        if (str.length() > 0){
            int acc = 16 * charToNumber(str.substring(0,1)) + charToNumber(str.substring(1,2)) ;
            return acc;
        } else {
                return 999;
        }
    }
// decodes character to number    
    private int charToNumber (String st) {
        if (st.length() == 1) {
            char k =  st.charAt(0);
            switch ((int) k) {
                case 'e':
                    return 0;
                case 'o':
                    return 1;
                case 't':
                    return 2;
                case 'a':
                    return 3;
                case 'i':
                    return 4;
                case 'n':
                    return 5;
                case 'l':
                    return 6;
                case 'r':
                    return 7;
                case 's':
                    return 8;
                case 'c':
                    return 9;
                case 'd':
                    return 10;
                case 'f':
                    return 11;
                case 'h':
                    return 12;
                case 'm':
                    return 13;
                case 'p':
                    return 14;
                case 'u':
                    return 15;
            }    
     
        }        return 16;   
    }
        
    private String convertToIAC (String st) {
        String acc = "";
        for (int i = 0; i < 32; i++)  {  
            char k = st.substring(i, i+1).charAt(0);
                switch ((int) k) {
                    case 'e':
                        acc += " ";
                        break;
                    case 'o':
                         acc += "0";
                         break;
                     case 't':
                         acc += "1";
                         break;
                     case 'a':
                         acc += "2";
                         break;
                     case 'i':
                         acc += "3";
                         break;
                      case 'n':
                         acc += "4";
                         break;
                     case 'l':
                         acc += "5";
                         break;
                     case 'r':
                         acc += "6";
                         break;
                     case 's':
                         acc += "7";
                         break;
                     case 'c':
                         acc += "8";
                         break;
                     case 'd':
                         acc += "9";
                         break;
                     case 'f':
                         acc += "/";
                         break;
                     case 'h':
                         acc += "=";
                         break;
                     case 'm':
                         acc += "\n";
                         break;
            }
        }
          return acc;  
    }
       
    
    private String RepairLine (String instr) {
        boolean aOK = false;
       boolean bOK = false;
       boolean cOK = false;
       boolean dOK = false;
       boolean eOK = false;
       
       byte[] aB = {0,0,0,0,0,0,0,0};
       byte[] bB = {0,0,0,0,0,0,0,0};
       byte[] cB = {0,0,0,0,0,0,0,0};
       byte[] dB = {0,0,0,0,0,0,0,0};
       byte[] eB = {0,0,0,0,0,0,0,0};
       
        
        
         Pattern pl = Pattern.compile("^([eotainlrscdfhmpu]{2})\\s*(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)$");
         Matcher ml = pl.matcher(instr);
         if (ml.lookingAt()) {
            if (ml.group(1).length() == 2) {
               Index = stringToIndex(ml.group(1).substring(0,2));
            } else {  // no proper index
                return "";
            }

 // ok, looks doadable, format is o.k.
            String i = ml.group(1); // index
            String a = ml.group(2);
            String b = ml.group(3);
            String c = ml.group(4);
            String d = ml.group(5);
            String e = ml.group(6);

            Pattern pm = Pattern.compile("([eotainlrscdfhmpu]+)");
            Matcher ma = pm.matcher(a);
            if (ma.lookingAt()){
                if (ma.group(0).length() == 8){
                    aOK = true;
                }
            }
            Matcher mb = pm.matcher(b);
            if (mb.lookingAt()){
                if (mb.group(0).length() == 8){
                    bOK = true;
                }
            }
           
            Matcher mc = pm.matcher(c);
            if (mc.lookingAt()){
                if (mc.group(0).length() == 8){
                    cOK = true;
                }
            }            
            Matcher md = pm.matcher(d);
            if (md.lookingAt()){
                if (md.group(0).length() == 8){
                    dOK = true;
                }
            }
           
            Matcher me = pm.matcher(e);
            if (me.lookingAt()){
                if (me.group(0).length() == 8){
                   eOK = true;
                }
            } 
            for (int j = 0; j < 8; j++){
               if (aOK) 
                   aB[j] = (byte) charToNumber(a.substring(j, j+1));
               if (bOK) 
                     bB[j] = (byte) charToNumber(b.substring(j, j+1));
             
                if (cOK) 
                    cB[j] = (byte) charToNumber(c.substring(j, j+1));
                if (dOK) 
                    dB[j] = (byte) charToNumber(d.substring(j, j+1));
                if (eOK) 
                    eB[j] = (byte) charToNumber(e.substring(j, j+1));      
            } 
 
            
            if (aOK & bOK & cOK & dOK ) {
//                debug("e = error");
                for (int j = 0; j < 8; j++){
                    eB[j] = (byte) ( aB[j] ^  bB[j] ^  cB[j] ^ ( dB[j]));
                    sb += direct.substring((int)eB[j], (int)eB[j] + 1);
                }
                return i + " " + a + " " + b + " " + c + " " + d + " " + sb;
            }
            if (aOK & bOK & cOK & eOK) {
//                debug("d = error");
                 for (int j = 0; j < 8; j++){
                    dB[j] = (byte) ( aB[j] ^ bB[j] ^  cB[j] ^ ( eB[j]));
                    sb += direct.substring((int)dB[j], (int)dB[j] + 1);
                }
                return i + " " + a + " " + b + " " + c + " " + sb + " " + e;
           }
            if (aOK & bOK  & dOK & eOK) {
//                debug("c = error");
                 for (int j = 0; j < 8; j++){
                    cB[j] = (byte) ( aB[j] ^  bB[j] ^  dB[j] ^ ( eB[j]));
                    sb += direct.substring((int)cB[j], (int)cB[j] + 1);
                }
                return i + " " + a + " " + b + " " + sb + " " + d + " " + e;
            }
// "eotainlrscdfhmpu";
            if (aOK &  cOK & dOK & eOK) {
//                debug("b = error");
                for (int j = 0; j < 8; j++){
                    bB[j] = (byte) ( aB[j] ^  cB[j] ^  dB[j] ^ ( eB[j]));                
                    bB[j] = (byte) (bB[j] & 0x0F);
                    sb += direct.substring((int)bB[j], (int)bB[j] + 1);
                }
                return i + " " + a + " " + sb + " " + c + " " + d + " " + e;
            }
            if (bOK & cOK & dOK & eOK) {
//                debug("a = error");
                for (int j = 0; j < 8; j++){
                    aB[j] = (byte) ( bB[j] ^ cB[j] ^ dB[j] ^ ( eB[j]));
                    sb += direct.substring((int)aB[j], (int)aB[j] + 1);
                }
                return i + " " + sb + " " + b + " " + c + " " + d + " " + e;
            }
            
         } else {       // no match
            return "";
         }
         return "";
    }

    public final static void main( String[] args ) throws FileNotFoundException, IOException {
        FastFEC f = new FastFEC();
       f.fastfec2("/home/rein/.pskmail/iactemp", "");
    }
    
     public void debug(String message){
        System.out.println("Debug:" + message);
    }   
}
   
