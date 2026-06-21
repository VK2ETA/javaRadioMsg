/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaradiomsg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rein
 */
public class Amp2 {

    static String Progname = "";
    static String Filename = "";
    static String Filetime = "";
    static int Blocks = 0;
    static double Blockrnr = 0.0;
    static int Blocklen = 0;
    static int Filelength = 0;
    static int blknr = 0;
    static String data = "";
    private static int headerlen = 0;
    private static int len = 0;
    private static int datalen = 0;
    private static int Aclen = 0;
    private static int arraynr = 0;
    private static int bytes = 0;
    private static String crc = "";
    private static String hash = "";
    private static String meat = "";
    static String Accu = "";
    static boolean noindex = true;
    private static int iw = -1;
    private static int iv = -1;
    private static int ix = -1;
    private static int iy = -1;
    private static int iz = -1;
    private static int alength = 10;
    private static int hashnumber = 0;
    private String[][] Contentarray = new String[10][1128];
    private static Pattern prg1 = Pattern.compile("(<PROG\\s(\\d+)\\s(\\S+)>)");
    private static Pattern prg = Pattern.compile("<PROG\\s\\d+\\s(\\S+)>(\\{(\\S{4})\\}(\\S+))");
    private static Pattern id1 = Pattern.compile("<ID (\\d+) (\\S+)>(\\S+)");
    private static Pattern id = Pattern.compile("<ID (\\d+)\\s(\\S+)>(\\S+)");
    private static Pattern fil1 = Pattern.compile("(<FILE\\s(\\d+)\\s(\\S+)>)");
    private static Pattern fil = Pattern.compile("<FILE\\s\\d+\\s(\\S+)>(\\{(\\S{4})\\}(\\d{14})(\\S+))");
    private static Pattern siz1 = Pattern.compile("(<SIZE\\s(\\d+)\\s(\\S+)>)");
    private static Pattern siz = Pattern.compile("<SIZE\\s\\d+\\s(\\S+)>(\\{(\\S{4})\\}(\\d+)\\s(\\d+)\\s(\\d+))");
    private static Pattern dat1 = Pattern.compile("(<DATA\\s(\\d+)\\s(\\S+)>)");
    private static Pattern dat = Pattern.compile("<DATA\\s\\d+\\s(\\S+)>(\\{(\\S{4}):(\\d+)\\}([A-Za-z0-9+/=]*\\s*))", Pattern.DOTALL);
//    private static Pattern dat = Pattern.compile("<DATA\\s\\d+\\s(\\S+)>(\\{(\\S{4}):(\\d+)\\}(.*))", Pattern.DOTALL);
    private static Pattern ctr = Pattern.compile("(<CNTL\\s(\\d+)\\s(\\S+)>)\\{\\S{4}:EOT\\}");

    Amp2(){
        init();
//        System.out.println("init amp2");
    };
    
    public final void init() {
        Progname = "";
        Filename = "";
        Filetime = "";
        Blocks = 0;
        Blocklen = 0;
        Filelength = 0;
        blknr = 0;
        data = "";
        headerlen = 0;
        len = 0;
        datalen = 0;
        Aclen = 0;
        crc = "";
        hash = "";
        meat = "";
        Accu = "";
        noindex = true;
        iw = -1;
        ix = -1;
        iy = -1;
        iz = -1;
        alength = 10;
//        Contentarray = new String[alength][1128];
        for (int i = 0; i < alength; i++) {
            for (int j = 0; j < 1128; j++) {
                Contentarray[i][j] = "";
            }
        } 
   }

    public String get(String inString) {

        Accu += inString;
//System.out.print("ACCU:" + Accu + ":"); 
//System.out.println(Accu.length());

        if (noindex) {
            iw = Accu.indexOf("<DATA");
            iv = Accu.indexOf("<CNTL");
            ix = Accu.indexOf("<PROG");
            iy = Accu.indexOf("<FILE");
            iz = Accu.indexOf("<SIZE");
        }
//if (iw > -1 | iv > -1 |ix > -1 |iy > -1 |iz > -1 )   {
//    System.out.println("found"); 
//}         
            

        if (ix > 0) {
            noindex = false;
            Accu = Accu.substring(ix);
            ix = 0;
        } else if (ix == 0 & Accu.contains(">")) { // PROG line
            noindex = false;
            Aclen = Accu.length();
            bytes = 0;

            if (len > 0 & Aclen == headerlen + len) { // complete
                Matcher mprg = prg.matcher(Accu);
                if (mprg.lookingAt()) {
                    crc = mprg.group(1);
                    hash = mprg.group(3);
                    Progname = mprg.group(4);
                    if (crc.equals(Main.q.checksum(mprg.group(2)))) {
//                        System.out.println(Progname);
                        Main.filesTextArea += Progname + "\n";
                        Message("Receiving bulletin ", 10);
                        Main.bulletinMode = true;
                    }
                    Accu = "";
                    len = 0;
                    ix = -1;
                    iy = -1;
                    iw = -1;
                    iv = -1;
                    iz = -1;
                    crc = "";
                    noindex = true;
                }
            } else {                             //still reading
                Matcher mprg = prg1.matcher(Accu);
                if (mprg.lookingAt()) {
                    headerlen = mprg.group(1).length();
                    len = Integer.parseInt(mprg.group(2));
                    crc = mprg.group(3);
                }
            }
        }
        
        if (iv > 0) {
            noindex = false;
            Accu = Accu.substring(iv);
            iv = 0;
        } else if (iv == 0 & Accu.contains(">")) { // CTRL line
//System.out.println(Accu);  
            try{
//                Main.mainui.appendMainWindow("\n");
                Main.mainwindow += "\n";
            }
            catch (Exception ex){
                System.out.println("PROBLEM with append");
            }
                Message("End of TX " + Filename, 10);
                Main.bulletinMode = false;
                    Accu = "";
                    Blocks = 0;
                    Blockrnr = 0.0;
                    Blocklen = 0;
                    Filelength = 0;
                    blknr = 0;
                    data = "";
                    headerlen = 0;
                    len = 0;
                    datalen = 0;
                    Aclen = 0;
                    arraynr = 0;
                    bytes = 0;
                    crc = "";
                    hash = "";                 
                    len = 0;
                    iv = -1;
                    crc = "";
                    noindex = true;               
        }
       
        
        if (iy > 0) {
            noindex = false;
            Accu = Accu.substring(iy);
            iy = 0;
        } else if (iy == 0 & Accu.contains(">")) { // FILE line
  //System.out.println(Accu);         
            bytes = 0;
            noindex = false;
            Aclen = Accu.length();
            if (len > 0 & Aclen == headerlen + len) { // complete
                Matcher mfil = fil.matcher(Accu);
                if (mfil.lookingAt()) {
                    crc = mfil.group(1);
                    hash = mfil.group(3);
                    Filetime = mfil.group(4);
                    Filename = mfil.group(5);
                    meat = mfil.group(2);
 //System.out.println(Accu); 
                    if (crc.equals(Main.q.checksum(meat))) {
//                        System.out.println(Filename);
                        Main.filesTextArea += "Receiving bulletin: " + Filename + "\n";
//                        System.out.println("Hash is " + hash);
                        Blockrnr = 0;
                        Main.bulletinMode = true;
                    }
                    Accu = "";
                    len = 0;
                    ix = -1;
                    iy = -1;
                    iw = -1;
                    iv = -1;
                    iz = -1;
                    crc = "";
                    noindex = true;
                }
            } else {                             //still reading
                Matcher mfil = fil1.matcher(Accu);
                if (mfil.lookingAt()) {
                    headerlen = mfil.group(1).length();
                    len = Integer.parseInt(mfil.group(2));
                    crc = mfil.group(3);
                }
            }
        }

        if (iz > 0) {
            noindex = false;
            Accu = Accu.substring(iz);
            iz = 0;
        } else if (iz == 0 & Accu.contains(">")) { // SIZE line
// System.out.println(Accu);            
            bytes = 0;
            noindex = false;
            Aclen = Accu.length();
            if (len > 0 & Aclen == headerlen + len) { // complete 
              
                Matcher mfil = siz.matcher(Accu);
                if (mfil.lookingAt()) {
                    crc = mfil.group(1);
                    hash = mfil.group(3);
                    Filelength = Integer.parseInt(mfil.group(4));
                    Blocks = Integer.parseInt(mfil.group(5));
                    Blocklen = Integer.parseInt(mfil.group(6));
                    meat = mfil.group(2);

//                    if (crc.equals(Main.arq.checksum(meat))) {
//                        System.out.println(Filelength);
//                        System.out.println(Blocks);
//                        System.out.println(Blocklen);
//                    }
                    Accu = "";
                    len = 0;
                    ix = -1;
                    iy = -1;
                    iw = -1;
                    iv = -1;
                    iz = -1;
                    crc = "";
                    noindex = true;
                    Main.bulletinMode = true;
                }
            } else {                             //still reading
                Matcher mfil = siz1.matcher(Accu);
                if (mfil.lookingAt()) {
                    headerlen = mfil.group(1).length();
                    len = Integer.parseInt(mfil.group(2));
                    crc = mfil.group(3);
                }
            }
        }
        
        if (iw > 0) {
            noindex = false;
            Accu = Accu.substring(iw);
            iw = 0;
        }
        if (iw == 0 & Accu.contains(">")) { // DATA line
            noindex = false;
            Aclen = Accu.length();

            if (datalen == 0 && Aclen > 10 && Aclen < 16) {
                Matcher mfil = dat1.matcher(Accu);
                if (mfil.lookingAt()) {
                    headerlen = mfil.group(1).length();
                    datalen = Integer.parseInt(mfil.group(2));
                    crc = mfil.group(3);
//                       System.out.print("DATALEN:");
//                       System.out.println(datalen);                       
                }
            }

            if (Aclen == headerlen + datalen -1) { // complete 
//                   System.out.println(Accu);
                int leng = headerlen + datalen - 1;
                String str = Accu.substring (0, leng );
                Accu = Accu.substring(leng );
//Main.FilesTextArea += leng + "\n";
//Main.FilesTextArea += str;
                Matcher mfil = dat.matcher(str);
                if (mfil.lookingAt()) {
                    crc = mfil.group(1);
                    hash = mfil.group(3);
                    blknr = Integer.parseInt(mfil.group(4));
                    data = mfil.group(5);
// System.out.println(data.length());                   
                    meat = mfil.group(2);
//System.out.print(crc + ":" + Main.q.checksum(meat) + ":" + data + "\n"); 
//Main.FilesTextArea += crc + ":" + Main.q.checksum(meat) + ":" + data + "\n";
                    if (crc.equals(Main.q.checksum(meat + "\n"))) {
                        try {
                            if (blknr == 1){
                                Main.mainwindow += "\n*";
                            } else {
                                Main.mainwindow += "*";
                            }

                            Main.filesTextArea += data + "\n";
                        }
                        catch (Exception ex){
                            System.out.println("Exception appending text to main");
                        }
                        
                       
                        addfile(hash);

                        try {
                            addcontent(hash, data, blknr);
                        } catch (IOException e) {
                            System.out.println("Problem storing file: " + e);
                        }
                        Accu = "";
                        ix = -1;
                        iy = -1;
                        iw = -1;
                        iv = -1;
                        iz = -1;
                        Aclen = 0;
                        noindex = true;
                    } else { 
                        if (blknr == 1){
                            Main.mainwindow += "\n.";
                        } else {
                            Main.mainwindow += ".";
                        }                        

                        Accu = "";
                        Aclen = 0;
                        ix = -1;
                        iy = -1;
                        iw = -1;
                        iv = -1;
                        iz = -1;
                        crc = "";
                        noindex = true;
                        
                    }
                }
            } else {                             //still reading
                Matcher mfil = dat1.matcher(Accu);
                if (mfil.lookingAt()) {
                    headerlen = mfil.group(1).length();
                    datalen = Integer.parseInt(mfil.group(2));
                    crc = mfil.group(3);
//                       System.out.print("DATALEN:");
//                       System.out.println(datalen);
                }
            }
        }
        if (Accu.length() > 100) {
            Accu = Accu.substring(1);
        }

        return "";
    }

    private void addfile(String hash) {
        
        for (int i = 0; i < alength; i++) {
            if (Contentarray[i][0].contains(hash)) {
                Contentarray[i][0] = hash;
                break;
            }
        }
        for (int i = 0; i < alength; i++) {
            if (Contentarray[i][0].contains(hash)){
                break;
            }
            if (Contentarray[i][0].length() == 0) {
                Contentarray[i][0] = hash;
                break;
            }
        }
    }

    private boolean checkbulletin(String Filename) {
        boolean result = true;

        File Bulldir = new File(Main.homePath + Main.dirPrefix + "bulletins" + Main.separator);
        if (!Bulldir.isDirectory()) {
            Bulldir.mkdir();
        }
        File fnam = new File(Bulldir + Main.separator + Filename);

        if (!fnam.exists()) {
            result = false;
        }

        return result;
    }

    private boolean checkfile(String hash1) {
        boolean result = false;
        if (Contentarray == null) {
            System.out.println("No Contentarray...");
        }
        for (int i = 0; i < alength; i++) {
            if (Contentarray[i][0].contains(hash1)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean addcontent(String hash1, String Line, int blocknr) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        String content;
//        content = "";
        boolean result = false;
        bytes += Line.length();
        Blockrnr += 1;
        
        if (Filelength > 0) {
            Main.progress = (int) (Blockrnr/Blocks * 100);
            Main.mainui.ProgressBar.setValue(Main.progress);
            Main.mainui.ProgressBar.setStringPainted(true);
        }

        for (int i = 0; i < alength; i++) {
//            System.out.println(Contentarray[i][0]);

            if (Contentarray[i][0].contains(hash1)) {
//                System.out.print("hash " + hash1 + " found."
//                        + " in ");
//                System.out.println(i);
                Contentarray[i][blocknr] = Line;
                arraynr = i;
//                System.out.print(arraynr);
//                System.out.print(":");
//                System.out.print(blocknr);
//                System.out.println(":" + Line);
                break;
            }
        }
        if (Blocks > 0) {
            if (checklines(arraynr, Blocks)) {
                content = getcontent(hash1, Blocks);
                if (!Filename.equals("")) {
                    result = storecontent(hash1, Filename);
                    if (result) {
//                        System.out.println("File stored in " + Filename);
//                        Main.mainui.appendMainWindow("\nFile stored in " + Filename + "\n");
                        Main.mainwindow += "\nFile stored in " + Filename + "\n";
                        Message("File stored in " + Filename, 10);
                        for (int j = 0; j < Blocks + 1; j++) {
                            Contentarray[arraynr][j] = "";
                        }
                        bytes = 0;
                        hash = "";
                        Filename = "";
                        return result;
                    } else {
                        Message("File incomplete", 10);
                        return result;
                    }
                }

            }
        }
        return result;
    }

    private boolean checklines(int Array, int blocks) {
        boolean result = false;

        int nrblocks = 0;

        for (int j = 1; j <= blocks + 1; j++) {
            if (Contentarray[Array][j].equals("")) {
                result = false;
                break;
            } else {
                nrblocks += 1;
            }
        }

//        System.out.print("Blocks:");
//        System.out.println(nrblocks);

        if (nrblocks == blocks) {
            return true;
        } else {
            return false;
        }
    }

    private String getcontent(String hash, int blocks) {
        String Line = "";

        for (int i = 0; i < alength; i++) {
            if (Contentarray[i][0].contains(hash)) {
                for (int j = 1; j <= blocks; j++) {
                    Line += Contentarray[i][j];
                }
            }
        }

        return Line;
    }

    private boolean storecontent(String hash, String filename) {

        boolean result = true;
        String content = "";
        String checker;
        boolean b64 = true;
        String zipfile = "";
                
        for (int i = 0; i < alength; i++) {
//System.out.print(i);            
//System.out.println(":" + Contentarray[i][0]);            
            if (Contentarray[i][0].contains(hash)) {
                hashnumber = i;
// System.out.println("Hash found:" + hash);               
                for (int j = 1; j < Blocks + 1; j++) {
                    String line = Contentarray[i][j];
//  System.out.print(j);
//  System.out.println(":" + line);
                    if (line.length() > 0) {
                        line = line.replace("[b64:start]", "");
                        line = line.replace("[b64:end]", "");
                        content += line;
//System.out.print(j);                        
//System.out.println(":" + content);                        
                    } else {
                        result = false;
                        break;
                    }
                }
            } else {
                continue;
            }
  
            if (result) {                
                if (content.length() > 100){
                    checker = content.substring(1, 100);
                } else {
                    checker = content;
                }
//System.out.println(content.length());


                for (int k = 0; k < checker.length(); k++){
                    String c = checker.substring(k,k+1);

                    if (c.equals(" ")) {
                        b64 = false;
                        break;
                    }
                }
               
                BufferedWriter writer = null;

                File Bulldir = new File(Main.homePath + Main.dirPrefix + "bulletins" + Main.separator);
                if (!Bulldir.isDirectory()) {
                    Bulldir.mkdir();
                }
                
                if (b64) {
// System.out.println("B64 file");                   
                   zipfile = Bulldir + Main.separator + filename + ".gz";
                   try {
                        Base64.decodeToFile(content, zipfile);
                   }
                   catch (Exception e){
                       System.out.println("Problem decoding b64:" + e);
                   }
                   
//System.out.println("zipfile =" + zipfile);  
//                    try {
//                        Thread.sleep(20000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(amp2.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                    try {
                        Unzip.Unzip(zipfile);
                        String myfile = zipfile.replace(".gz", ""); 
                        content = readFile(myfile);
//System.out.println("CONTENT:\n" + content) ;                       
                    } catch (Exception ex) {
//                        Logger.getLogger(amp2.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Problem with b64z:" + ex);
                    }
                    for (int j = 1; j < Blocks + 1; j++) {
                        Contentarray[hashnumber][j] = "";
                    }
                } else {
                    try {
                        writer = new BufferedWriter(new FileWriter(Bulldir + Main.separator + filename));
                        writer.write(content);
//                        Main.mainui.appendMainWindow("\n" + content);
                        Main.mainwindow += "\n" + content;
                    } catch (IOException e) {
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException e) {
                            
                        }
                        
                        for (int j = 0; j < Blocks + 1; j++) {
                            Contentarray[hashnumber][j] = "";
                        }
                        
                    }
                }
            }
            break;
        }
//System.out.println("\nCONTENT:\n" + content);
        b64 = false;
        for (int l = 0; l < content.length(); l++){
            String check = content.substring(l, l+1);
         
            if (check.charAt(0) > 127 | check.charAt(0) < 9){
                b64 = true;
                break;
            } 
            
            if (l > 1000) {
                    break;
            }
        }
        
        if (b64) {
        } else {
//            Main.mainui.appendMainWindow("\n" + content);
            Main.mainwindow += "\n" + content;
        }
        
        
        Main.progress = 0;
        Blockrnr = 0;
        Main.bulletinMode = false;
        Main.bulletinTime = 0;
        Main.mainui.ProgressBar.setValue(Main.progress);
        Main.mainui.ProgressBar.setStringPainted(false); 
//        Main.mainui.appendMainWindow("\n");
        Main.mainwindow += "\n";
        
        return result;
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
        
    private String NrToCar (String st) {
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
       
public static String readFile(String filename)
{
   String content = null;
   File file = new File(filename); //for ex foo.txt
   try {
       FileReader reader;
       reader = new FileReader(file);
       char[] chars = new char[(int) file.length()];
       reader.read(chars);
       content = new String(chars);
       reader.close();
   } catch (IOException e) {
   }
   return content;
}    

    public void Message(String msg, int time) {
        Main.statusLine = msg;
        Main.statusLineTimer = time;
    }


}

