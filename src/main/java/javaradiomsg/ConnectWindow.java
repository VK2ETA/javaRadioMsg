/*
 * Modem.java
 *
 * Copyright (C) 2008 Pär Crusefalk and Rein Couperus
 * Copyright (C) 2018-2021 Pskmail Server and RadioMsg sections by John Douyere (VK2ETA) 
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
 * @author jdouyere
 */
public class ConnectWindow extends javax.swing.JDialog {

    String TXmodemstr = Main.lastTxModem;
    String RXmodemstr = Main.lastRxModem;
    String[] Modemstrings = {"default","THOR8","MFSK16","THOR22",
        "MFSK32",  "PSK250R", "PSK500R", "PSK500",
        "PSK250", "PSK125R", "THOR11", "DOMINOEX5", "CTSTIA", "PSK1000",
        "PSK63RC5", "PSK63RC10", "PSK250RC3", "PSK125RC4", "DOMINOEX22", "DOMINOEX11"};
    String[] modemindicators = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "b", "d", "n", "f", "g", "h", "i", "j", "k", "l", "m"};
    ModemModesEnum TXm;
    ModemModesEnum RXm;
    private boolean addingItems = false;
    private java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("javaradiomsg/Bundle");
    private boolean updatingServerList = true;

    /**
     * Creates new form Connectwindow
     */
    public ConnectWindow(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
//     System.out.println("Modem=" + Main.LastRxModem);      
        this.cboServerConnect.removeAllItems();
//        Rigctl.Loadfreqs(myServer);

        // Add servers from main
        for (int i = 0; i < Main.serversArray.length; i++) {
            if (!Main.serversArray[i].equals("")) {
                cboServerConnect.addItem(Main.serversArray[i]);
            }
        }
        //VK2ETA issue here, we always preset, and may contain server:password 
        //this.cboServerConnect.setSelectedItem(Main.mainui.cboServer.getSelectedItem());
        String ms = Main.q.getServer();
        this.cboServerConnect.setSelectedItem(ms);
        String mp = Main.q.getPassword();
        this.txtConnectPassword.setText(mp);
        //It's ok now to process events on the combo box
        updatingServerList = false;
        
        //set modems
        this.jComboBoxTXModem.removeAllItems();
        this.jComboBoxRXModem.removeAllItems();

        int i = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        addingItems = true;
        for (i = 0; i < Main.modesListStr.length(); i++) {
            if (Main.modesListStr.substring(i, i + 1).equals("g")) {
                jComboBoxTXModem.addItem("PSK1000");
                jComboBoxRXModem.addItem("PSK1000");
                if (Main.lastRxModem.equals("PSK1000")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK1000")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("7")) {
                jComboBoxTXModem.addItem("PSK500");
                jComboBoxRXModem.addItem("PSK500");
                if (Main.lastRxModem.equals("PSK500")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK500")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("6")) {
                jComboBoxTXModem.addItem("PSK500R");
                jComboBoxRXModem.addItem("PSK500R");
                if (Main.lastRxModem.equals("PSK500R")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK500R")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("5")) {
                jComboBoxTXModem.addItem("PSK250R");
                jComboBoxRXModem.addItem("PSK250R");
                if (Main.lastRxModem.equals("PSK250R")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK250R")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("l")) {
                jComboBoxTXModem.addItem("DOMINOEX22");
                jComboBoxRXModem.addItem("DOMINOEX22");
                if (Main.lastRxModem.equals("DOMINOEX22")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("DOMINOEX22")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("m")) {
                jComboBoxTXModem.addItem("DOMINOEX11");
                jComboBoxRXModem.addItem("DOMINOEX11");
                if (Main.lastRxModem.equals("DOMINOEX11")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("DOMINOEX11")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("4")) {
                jComboBoxTXModem.addItem("MFSK32");
                jComboBoxRXModem.addItem("MFSK32");
                if (Main.lastRxModem.equals("MFSK32")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("MFSK32")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("3")) {
                jComboBoxTXModem.addItem("THOR22");
                jComboBoxRXModem.addItem("THOR22");
                if (Main.lastRxModem.equals("THOR22")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("THOR22")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("2")) {
                jComboBoxTXModem.addItem("MFSK16");
                jComboBoxRXModem.addItem("MFSK16");
                if (Main.lastRxModem.equals("MFSK16")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("MFSK16")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("1")) {
                jComboBoxTXModem.addItem("THOR8");
                jComboBoxRXModem.addItem("THOR8");
                if (Main.lastRxModem.equals("THOR8")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("THOR8")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("8")) {
                jComboBoxTXModem.addItem("PSK250");
                jComboBoxRXModem.addItem("PSK250");
                if (Main.lastRxModem.equals("PSK250")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK250")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("b")) {
                jComboBoxTXModem.addItem("PSK125R");
                jComboBoxRXModem.addItem("PSK125R");
                if (Main.lastRxModem.equals("PSK125R")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("PSK125R")) {
                    m = k;
                }
                k++;
            } else if (Main.modesListStr.substring(i, i + 1).equals("n")) {
                jComboBoxTXModem.addItem("DOMINOEX5");
                jComboBoxRXModem.addItem("DOMINOEX5");
                if (Main.lastRxModem.equals("DOMINOEX5")) {
                    l = k;
                }
                if (Main.lastTxModem.equals("DOMINOEX5")) {
                    m = k;
                }
                k++;
            }
        }
        //            System.out.println("lastRXmodem=" + Main.LastRxModem);
        addingItems = false;
        jComboBoxRXModem.setSelectedIndex(l);
        jComboBoxTXModem.setSelectedIndex(m);
    }
 
    private String getrxmodemindex(){
        String index = "8";
        for (int i = 0; i < Modemstrings.length; i++){
            if (Modemstrings[i].equals(jComboBoxRXModem.getSelectedItem().toString())) {
                index = modemindicators[i];
                break;
            }
        }
        
        return index;
    }
     private String gettxmodemindex(){
        String index = "8";
        for (int i = 0; i < Modemstrings.length; i++){
            if (Modemstrings[i].equals(jComboBoxTXModem.getSelectedItem().toString())) {
                index = modemindicators[i];
                break;
            }
        }
        
        return index;
    }   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBoxTXModem = new javax.swing.JComboBox<>();
        jComboBoxRXModem = new javax.swing.JComboBox<>();
        cboServerConnect = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ConnectServerButton = new javax.swing.JButton();
        ServerConnectCancel = new javax.swing.JButton();
        txtConnectPassword = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Server");

        jComboBoxTXModem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTXModemActionPerformed(evt);
            }
        });

        jComboBoxRXModem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRXModemActionPerformed(evt);
            }
        });

        cboServerConnect.setEditable(true);
        cboServerConnect.setFont(new java.awt.Font("DejaVu Sans Mono", 0, 12)); // NOI18N
        cboServerConnect.setMinimumSize(new java.awt.Dimension(150, 24));
        cboServerConnect.setPreferredSize(new java.awt.Dimension(150, 24));
        cboServerConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboServerConnectActionPerformed(evt);
            }
        });

        jLabel2.setText("TX");

        jLabel3.setText("RX");

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        jLabel4.setText("Connect to server");

        ConnectServerButton.setText("Connect");
        ConnectServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectServerButtonActionPerformed(evt);
            }
        });

        ServerConnectCancel.setText("Cancel");
        ServerConnectCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ServerConnectCancelActionPerformed(evt);
            }
        });

        txtConnectPassword.setText("jPasswordField1");
        txtConnectPassword.setMinimumSize(new java.awt.Dimension(109, 24));
        txtConnectPassword.setPreferredSize(new java.awt.Dimension(109, 24));

        jLabel5.setText(bundle.getString("Connectwindow.SERVER_PASSWORD")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(ServerConnectCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ConnectServerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboServerConnect, 0, 158, Short.MAX_VALUE)
                            .addComponent(txtConnectPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxTXModem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxRXModem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cboServerConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtConnectPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxTXModem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxRXModem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ConnectServerButton)
                    .addComponent(ServerConnectCancel))
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTXModemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTXModemActionPerformed
        if (!addingItems) { //Do not process when adding items, only on selection
            TXmodemstr = this.jComboBoxTXModem.getSelectedItem().toString();
            Main.lastTxModem = TXmodemstr;
            //        System.out.println("Setting TX:" + Main.LastTxModem);
        }
    }//GEN-LAST:event_jComboBoxTXModemActionPerformed

    private void jComboBoxRXModemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRXModemActionPerformed
        if (!addingItems) {
            RXmodemstr = this.jComboBoxRXModem.getSelectedItem().toString();
            //        System.out.println("rxm:" + RXmodemstr);
            Main.rxModemIndex = getrxmodemindex();
            Main.lastRxModem = RXmodemstr;
            Main.defaultmode = Main.convmodem(RXmodemstr);
            //System.out.println("Setting RX:" + Main.LastRxModem);
        }
    }//GEN-LAST:event_jComboBoxRXModemActionPerformed

    private void ConnectServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectServerButtonActionPerformed
        // TODO add your handling code here:
            //System.out.println("TXmodemstr=" + TXmodemstr);
        TXm = Main.convmodem(TXmodemstr);
        RXm = Main.convmodem(RXmodemstr);
        Main.txModem = TXm;
        Main.rxModem = RXm;
        Main.rxModemString = RXmodemstr;
        String myServer = ""; //cboServerConnect.getSelectedItem().toString();
        int index = cboServerConnect.getSelectedIndex();
        if (index == -1) {
            //We have typed a new server call, read the current combo box value
            myServer = cboServerConnect.getSelectedItem().toString();
        } else {
            //We selected an existing server
            if (index < Main.serversArray.length) {
                myServer = Main.serversArray[index];
            }
        }
        //String myServerPassword = txtConnectPassword.getPassword().toString();
        char[] passwordArray = txtConnectPassword.getPassword();
        String myServerPassword = new String(passwordArray);
        Main.q.setServer(myServer);
        //Override the default password with the one here in case we typed a new one
        Main.q.setPassword(myServerPassword);
        //Not on the fly
        //Main.configuration.setServer(myServer);
        Main.mainui.ConnectButtonAction();
        this.dispose();
    }//GEN-LAST:event_ConnectServerButtonActionPerformed

    private void ServerConnectCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ServerConnectCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_ServerConnectCancelActionPerformed

    private void cboServerConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboServerConnectActionPerformed

        int index = cboServerConnect.getSelectedIndex();
        if (!updatingServerList && index != -1) {
            //We have a valid selection event, not an Enter key event after typing a new server's call
            //Get the server from the preferences array (we may have two entries with the same server but different passwords for example)
            if (index < Main.serversArray.length) {
                Main.q.setServer(Main.serversArray[index]);
                Main.q.setPassword(Main.serversPasswordArray[index]);
                //Preset the password field too
                txtConnectPassword.setText(Main.serversPasswordArray[index]);
            }
        }
    }//GEN-LAST:event_cboServerConnectActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ConnectWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConnectWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConnectWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConnectWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConnectWindow dialog = new ConnectWindow(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ConnectServerButton;
    private javax.swing.JButton ServerConnectCancel;
    private javax.swing.JComboBox<String> cboServerConnect;
    private javax.swing.JComboBox<String> jComboBoxRXModem;
    private javax.swing.JComboBox<String> jComboBoxTXModem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPasswordField txtConnectPassword;
    // End of variables declaration//GEN-END:variables
}
