/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaradiomsg;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author jdouyere
 */
/**
 * Formats the Address book entry according to the data contained in the record
 */
public class AddressBookRenderer extends DefaultListCellRenderer {

    public AddressBookRenderer() {
    }

    public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Contact mc = (Contact) value;
        String ds = mc.getFirstName().trim()
                + " " + mc.getLastName().trim()
                + " " + ((mc.getHamCallsign().trim().length() > 0) ? mc.getHamCallsign().trim() : mc.getOtherCallsign().trim())
                + " " + mc.getEmail()
                + " " + mc.getMobilePhone();
        setText(ds);
        return this;
    }
}
