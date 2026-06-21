/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javaradiomsg;

/**
 *
 * @author rein
 */

public class WordConv {

    static String outstring;
    static char[] stringArray;

    static String getps (String instring) {
        outstring = "";
        if (!Main.connectsecond.equals("")){
               int cvpc = (int) (Integer.valueOf(Main.connectsecond)) % 6 + 1;
                stringArray = instring.toCharArray();
              int i;
                for (i=0; i < instring.length(); i++) {
                   stringArray[i] -= cvpc;
                    outstring += Character.toString(stringArray[i]);
               }
               return outstring;
        } else {
            return "";
        }
     }
}

