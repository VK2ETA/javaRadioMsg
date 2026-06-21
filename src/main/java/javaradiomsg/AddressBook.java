/*
 * AddressBook.java
 *
 * Copyright (C) 2011 Pär Crusefalk (SM0RWO)
 * Copyright (C) 2018-2021 Pskmail Server, RadioMsg sections and other improvements John Douyere (VK2ETA) 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This is a contact manager that handles...well, contacts.
 * Its pretty simple but simplicity is also powerful :-)
 * 
 * Created on 2011-okt-02, 10:35:51
 */

package javaradiomsg;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EventListener;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author per
 */
public class AddressBook extends javax.swing.JFrame  {

    private String contfilename;    // probably contacts.csv
    private File contfile;          // File handle for contacts.csv  
    private Contact contact;        // A contact object
    private ArrayList<Contact> contactlist = new ArrayList<Contact>(); // Used to hold all the contacts
    private Boolean SaveNeed = false; // Set to true after a change and before save

    private java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("javaradiomsg/Bundle");  
      
    /** Creates new form AddressBook */
    public AddressBook() {
        initComponents();
        contfilename = Main.homePath+Main.dirPrefix+"contacts.csv";
        FetchContacts();
        DisplayContacts();
    }

    //Custom paint method to add lines between the RadioMsg "To" checkboxes
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //Draw lines between the "CheckTo..." checkboxes and associated label to show identical action
        int xOff = RMsgPanel.getX();
        int yOff = RMsgPanel.getLocationOnScreen().y;
        int horizontalPos = xOff + checkToEmail.getX() + checkToEmail.getWidth() / 2;
        int startY = checkToCallsign.getLocationOnScreen().y + checkToCallsign.getHeight() - this.getLocationOnScreen().y;
        int endY = checkToEmail.getLocationOnScreen().y - this.getLocationOnScreen().y;
        g.setColor(Color.BLUE);
        g.drawLine(horizontalPos, startY, horizontalPos, endY);
        g.drawLine(horizontalPos + 1, startY, horizontalPos + 1, endY);
        startY = yOff + checkToEmail.getY() + checkToEmail.getHeight() - this.getLocationOnScreen().y;
        endY = yOff + checkToMobile.getY() - this.getLocationOnScreen().y;
        g.drawLine(horizontalPos, startY, horizontalPos, endY );
        g.drawLine(horizontalPos + 1, startY, horizontalPos + 1, endY);
        int endX = xOff + showInToLabel.getX();
        int yPos = yOff + showInToLabel.getY() - this.getLocationOnScreen().y + showInToLabel.getHeight()/2;
        g.drawLine(horizontalPos, yPos, endX, yPos );
        g.drawLine(horizontalPos, yPos + 1, endX, yPos + 1);
        //Draw lines between the "CheckVia" checkbox and associated label
        g.setColor(Color.RED);
        //Vertical part
        startY = showInViaLabel.getLocationOnScreen().y + showInViaLabel.getHeight()/2 - this.getLocationOnScreen().y;
        endY = checkVia.getLocationOnScreen().y - this.getLocationOnScreen().y;
        g.drawLine(horizontalPos, startY, horizontalPos, endY);
        g.drawLine(horizontalPos + 1, startY, horizontalPos + 1, endY);
        //Horizontal part
        endX = xOff + showInViaLabel.getX();
        yPos = yOff + showInViaLabel.getY() - this.getLocationOnScreen().y + showInViaLabel.getHeight()/2;
        g.drawLine(horizontalPos, yPos, endX, yPos );
        g.drawLine(horizontalPos, yPos + 1, endX, yPos + 1);
    }
   
    /**
     * Update the need to save, check before exiting
     * @param set 
     */
    private void SetSaveNeed(Boolean set){
        SaveNeed = set;
    }
    
    /**
     * Used to check for a need to save
     * @return 
     */
    private Boolean GetSaveNeed(){
        return SaveNeed;
    }
    
    /**
     * Show the contacts
     */
    private void DisplayContacts(){
        try {
            Contact mycontact;
            DefaultListModel myListModel;
            myListModel = (DefaultListModel) this.lstContacts.getModel();
            myListModel.clear();
            AddressBookRenderer myRenderer = new AddressBookRenderer();
            lstContacts.setCellRenderer(myRenderer);

            for (int i = 0; i < contactlist.size(); i++) {
                mycontact = contactlist.get(i);
                // Initialize the list with items
                myListModel.add(i, mycontact);
            }
        } catch (Exception e) {
            Main.log.writelog("Could not display contact information.", true);
        }
    }
 
    /**
     * Try to read all the contacts in the file
     */
    private void FetchContacts(){
        try{
            String linestring;  // Used to hold lines of the files
            contfile = new File(contfilename);
            
            // First check most common problems
            if (contfile == null) {
                throw new IllegalArgumentException("File should not be null.");
            }
            if (!contfile.exists()) {
                // File did not exist, create it
                contfile.createNewFile();
            }
            if (!contfile.isFile()) {
                throw new IllegalArgumentException("Should not be a directory: " + contfile);
            }
            
            // We should have a file now, lets fetch stuff                
            FileReader fin = new FileReader(contfilename);
            BufferedReader br = new BufferedReader(fin); 
            //Reset list in case it is not blank
            contactlist.clear();
            while((linestring = br.readLine()) != null) { 
                // Create another contact object and feed it the csv string
                contact = new Contact();
                contact.LoadCSV(linestring);
                contactlist.add(contact);
            }   
            fin.close(); 
        }
        catch(Exception e) {
            Main.log.writelog("Could not fetch contact information.", true);
        }
    }
    
    /**
     * Refreshes the contact list and then request refresh of the drop down boxes
     */
    private void refreshToAndViaLists() {

        //First refresh list
        FetchContacts();
        Main.mainui.refreshRMsgComboBoxes(contactlist);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel9 = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        bNew = new javax.swing.JButton();
        bProperties = new javax.swing.JButton();
        bDelete = new javax.swing.JButton();
        pnlTop = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstContacts = new javax.swing.JList<>();
        jSeparator1 = new javax.swing.JSeparator();
        RMsgPanel = new javax.swing.JPanel();
        checkToCallsign = new javax.swing.JCheckBox();
        checkVia = new javax.swing.JCheckBox();
        textRelayingPassword = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        checkToEmail = new javax.swing.JCheckBox();
        textMobileAlias = new javax.swing.JTextField();
        checkToMobile = new javax.swing.JCheckBox();
        showInToLabel = new javax.swing.JLabel();
        showInViaLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        textEmailAlias = new javax.swing.JTextField();
        passwordLabel1 = new javax.swing.JLabel();
        textIotPassword = new javax.swing.JTextField();
        pnlBottom = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        txtLastName = new javax.swing.JTextField();
        txtHamCall = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();
        txtNotes = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtOtherCall = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtMobilePhone = new javax.swing.JTextField();
        lblMMSI = new javax.swing.JLabel();
        txtMMSI = new javax.swing.JTextField();
        lblNickname = new javax.swing.JLabel();
        txtNickname = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuOpenFile = new javax.swing.JMenuItem();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        mnuClose = new javax.swing.JMenuItem();
        mnuEdit = new javax.swing.JMenu();
        mnuCut = new javax.swing.JMenuItem();
        mnuCopy = new javax.swing.JMenuItem();
        mnuPaste = new javax.swing.JMenuItem();

        jLabel9.setText(bundle.getString("AddressBook.jLabel9.text")); // NOI18N

        setMinimumSize(new java.awt.Dimension(550, 469));
        setName("AddressBook"); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        pnlButtons.setMinimumSize(new java.awt.Dimension(500, 35));
        pnlButtons.setPreferredSize(new java.awt.Dimension(100, 35));
        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        bNew.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("javapskmail/Bundle"); // NOI18N
        bNew.setText(bundle.getString("AddressBook.bNew.text")); // NOI18N
        bNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNewActionPerformed(evt);
            }
        });
        pnlButtons.add(bNew);

        bProperties.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bProperties.setText(bundle.getString("AddressBook.bProperties.text")); // NOI18N
        bProperties.setMinimumSize(new java.awt.Dimension(85, 27));
        bProperties.setPreferredSize(new java.awt.Dimension(85, 27));
        bProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bPropertiesActionPerformed(evt);
            }
        });
        pnlButtons.add(bProperties);

        bDelete.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        bDelete.setText(bundle.getString("AddressBook.bDelete.text")); // NOI18N
        bDelete.setMinimumSize(new java.awt.Dimension(85, 27));
        bDelete.setPreferredSize(new java.awt.Dimension(85, 27));
        bDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDeleteActionPerformed(evt);
            }
        });
        pnlButtons.add(bDelete);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(pnlButtons, gridBagConstraints);

        pnlTop.setMinimumSize(new java.awt.Dimension(200, 120));
        pnlTop.setPreferredSize(new java.awt.Dimension(200, 120));
        pnlTop.setLayout(new java.awt.GridBagLayout());

        lstContacts.setModel(new DefaultListModel());
        lstContacts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstContacts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstContactsMouseClicked(evt);
            }
        });
        lstContacts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstContactsKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(lstContacts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 478;
        gridBagConstraints.ipady = 168;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlTop.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlTop, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        getContentPane().add(jSeparator1, gridBagConstraints);

        RMsgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("AddressBook.rmsgTitle.text"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 3, 12))); // NOI18N
        RMsgPanel.setMaximumSize(new java.awt.Dimension(190, 230));
        RMsgPanel.setMinimumSize(new java.awt.Dimension(190, 230));
        RMsgPanel.setName(""); // NOI18N
        RMsgPanel.setPreferredSize(new java.awt.Dimension(190, 259));

        checkToCallsign.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        checkToCallsign.setText(bundle.getString("AddressBook.checkToCallsign.text")); // NOI18N
        checkToCallsign.setMargin(new java.awt.Insets(1, 1, 1, 1));
        checkToCallsign.setMaximumSize(new java.awt.Dimension(111, 19));
        checkToCallsign.setMinimumSize(new java.awt.Dimension(111, 19));
        checkToCallsign.setPreferredSize(new java.awt.Dimension(111, 19));
        checkToCallsign.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        checkToCallsign.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        checkToCallsign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkToCallsignActionPerformed(evt);
            }
        });

        checkVia.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        checkVia.setMargin(new java.awt.Insets(1, 1, 1, 1));
        checkVia.setMaximumSize(new java.awt.Dimension(115, 19));
        checkVia.setMinimumSize(new java.awt.Dimension(115, 19));
        checkVia.setPreferredSize(new java.awt.Dimension(115, 19));
        checkVia.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkVia.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        textRelayingPassword.setEditable(false);
        textRelayingPassword.setText(bundle.getString("AddressBook.textRelayingPassword.text")); // NOI18N
        textRelayingPassword.setMinimumSize(new java.awt.Dimension(4, 28));
        textRelayingPassword.setPreferredSize(new java.awt.Dimension(4, 28));

        jLabel10.setText(bundle.getString("AddressBook.jLabel10.text")); // NOI18N

        checkToEmail.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        checkToEmail.setMargin(new java.awt.Insets(1, 1, 1, 1));
        checkToEmail.setMaximumSize(new java.awt.Dimension(111, 19));
        checkToEmail.setMinimumSize(new java.awt.Dimension(111, 19));
        checkToEmail.setPreferredSize(new java.awt.Dimension(111, 19));
        checkToEmail.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        checkToEmail.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        checkToEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkToEmailActionPerformed(evt);
            }
        });

        textMobileAlias.setEditable(false);
        textMobileAlias.setText(bundle.getString("AddressBook.text")); // NOI18N
        textMobileAlias.setMaximumSize(new java.awt.Dimension(110, 28));
        textMobileAlias.setMinimumSize(new java.awt.Dimension(110, 28));
        textMobileAlias.setName(""); // NOI18N
        textMobileAlias.setPreferredSize(new java.awt.Dimension(110, 28));
        textMobileAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textMobileAliasActionPerformed(evt);
            }
        });

        checkToMobile.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        checkToMobile.setMargin(new java.awt.Insets(1, 1, 1, 1));
        checkToMobile.setMaximumSize(new java.awt.Dimension(111, 19));
        checkToMobile.setMinimumSize(new java.awt.Dimension(111, 19));
        checkToMobile.setPreferredSize(new java.awt.Dimension(111, 19));
        checkToMobile.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        checkToMobile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        checkToMobile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkToMobileActionPerformed(evt);
            }
        });

        showInToLabel.setText(bundle.getString("AddressBook.showInToLabel.text")); // NOI18N

        showInViaLabel.setText(bundle.getString("AddressBook.showInViaLabel.text")); // NOI18N

        passwordLabel.setText(bundle.getString("AddressBook.IotpasswordLabel.text")); // NOI18N

        textEmailAlias.setEditable(false);
        textEmailAlias.setText(bundle.getString("AddressBook.textEmailAlias.text")); // NOI18N
        textEmailAlias.setMinimumSize(new java.awt.Dimension(4, 28));
        textEmailAlias.setPreferredSize(new java.awt.Dimension(4, 28));

        passwordLabel1.setText(bundle.getString("AddressBook.passwordLabel1.text")); // NOI18N

        textIotPassword.setEditable(false);
        textIotPassword.setText(bundle.getString("AddressBook.textIotPassword.text")); // NOI18N
        textIotPassword.setMinimumSize(new java.awt.Dimension(4, 28));
        textIotPassword.setPreferredSize(new java.awt.Dimension(4, 28));
        textIotPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textIotPasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RMsgPanelLayout = new javax.swing.GroupLayout(RMsgPanel);
        RMsgPanel.setLayout(RMsgPanelLayout);
        RMsgPanelLayout.setHorizontalGroup(
            RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RMsgPanelLayout.createSequentialGroup()
                .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkToCallsign, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkVia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textIotPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textRelayingPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(RMsgPanelLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(showInToLabel))
                            .addGroup(RMsgPanelLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(passwordLabel1)
                                    .addComponent(showInViaLabel))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(RMsgPanelLayout.createSequentialGroup()
                .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(passwordLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, RMsgPanelLayout.createSequentialGroup()
                        .addComponent(checkToMobile, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textMobileAlias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, RMsgPanelLayout.createSequentialGroup()
                        .addComponent(checkToEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textEmailAlias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(1, 1, 1))
        );
        RMsgPanelLayout.setVerticalGroup(
            RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RMsgPanelLayout.createSequentialGroup()
                .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(textIotPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addComponent(showInViaLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordLabel1)
                        .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(RMsgPanelLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(textRelayingPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(passwordLabel))
                            .addGroup(RMsgPanelLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(checkVia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkToCallsign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(showInToLabel)
                .addGap(5, 5, 5)
                .addComponent(jLabel10)
                .addGap(3, 3, 3)
                .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(checkToMobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(RMsgPanelLayout.createSequentialGroup()
                        .addGroup(RMsgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(checkToEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textEmailAlias, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addComponent(textMobileAlias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );

        checkToCallsign.getAccessibleContext().setAccessibleName(bundle.getString("AddressBook.checkToCallsign.AccessibleContext.accessibleName")); // NOI18N
        checkVia.getAccessibleContext().setAccessibleName(bundle.getString("AddressBook.checkVia.AccessibleContext.accessibleName")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weighty = 0.5;
        getContentPane().add(RMsgPanel, gridBagConstraints);

        pnlBottom.setMaximumSize(new java.awt.Dimension(400, 210));
        pnlBottom.setMinimumSize(new java.awt.Dimension(400, 210));
        pnlBottom.setName(""); // NOI18N
        pnlBottom.setPreferredSize(new java.awt.Dimension(400, 210));
        pnlBottom.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(bundle.getString("AddressBook.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel1, gridBagConstraints);

        jLabel2.setText(bundle.getString("AddressBook.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel2, gridBagConstraints);

        jLabel4.setText(bundle.getString("AddressBook.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel4, gridBagConstraints);

        jLabel3.setText(bundle.getString("AddressBook.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel3, gridBagConstraints);

        jLabel5.setText(bundle.getString("AddressBook.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        jLabel6.setText(bundle.getString("AddressBook.jLabel6.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel6, gridBagConstraints);

        txtFirstName.setEditable(false);
        txtFirstName.setMinimumSize(new java.awt.Dimension(130, 28));
        txtFirstName.setPreferredSize(new java.awt.Dimension(150, 28));
        txtFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFirstNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(txtFirstName, gridBagConstraints);

        txtLastName.setEditable(false);
        txtLastName.setMinimumSize(new java.awt.Dimension(130, 28));
        txtLastName.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtLastName, gridBagConstraints);

        txtHamCall.setEditable(false);
        txtHamCall.setMinimumSize(new java.awt.Dimension(130, 28));
        txtHamCall.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(txtHamCall, gridBagConstraints);

        txtPhone.setEditable(false);
        txtPhone.setMinimumSize(new java.awt.Dimension(130, 28));
        txtPhone.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(txtPhone, gridBagConstraints);

        txtNotes.setEditable(false);
        txtNotes.setMinimumSize(new java.awt.Dimension(4, 28));
        txtNotes.setPreferredSize(new java.awt.Dimension(4, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtNotes, gridBagConstraints);

        txtEmail.setEditable(false);
        txtEmail.setMinimumSize(new java.awt.Dimension(130, 28));
        txtEmail.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtEmail, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        jLabel7.setText(bundle.getString("AddressBook.jLabel7.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel7, gridBagConstraints);

        txtOtherCall.setEditable(false);
        txtOtherCall.setMinimumSize(new java.awt.Dimension(130, 28));
        txtOtherCall.setPreferredSize(new java.awt.Dimension(150, 28));
        txtOtherCall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOtherCallActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtOtherCall, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        jLabel8.setText(bundle.getString("AddressBook.jLabel8.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(jLabel8, gridBagConstraints);

        txtMobilePhone.setEditable(false);
        txtMobilePhone.setMinimumSize(new java.awt.Dimension(130, 28));
        txtMobilePhone.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtMobilePhone, gridBagConstraints);

        lblMMSI.setText(bundle.getString("AddressBook.lblMMSI.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        pnlBottom.add(lblMMSI, gridBagConstraints);

        txtMMSI.setEditable(false);
        txtMMSI.setMinimumSize(new java.awt.Dimension(130, 28));
        txtMMSI.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtMMSI, gridBagConstraints);

        lblNickname.setText(bundle.getString("AddressBook.lblNickname.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        pnlBottom.add(lblNickname, gridBagConstraints);

        txtNickname.setEditable(false);
        txtNickname.setText(bundle.getString("AddressBook.txtNickname.text")); // NOI18N
        txtNickname.setMinimumSize(new java.awt.Dimension(130, 28));
        txtNickname.setPreferredSize(new java.awt.Dimension(130, 28));
        txtNickname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNicknameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        pnlBottom.add(txtNickname, gridBagConstraints);
        txtNickname.getAccessibleContext().setAccessibleName(bundle.getString("AddressBook.txtNickname.AccessibleContext.accessibleName")); // NOI18N

        jLabel11.setText(bundle.getString("AddressBook.jLabel11.text")); // NOI18N
        jLabel11.setMaximumSize(new java.awt.Dimension(59, 29));
        jLabel11.setMinimumSize(new java.awt.Dimension(59, 29));
        jLabel11.setPreferredSize(new java.awt.Dimension(59, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlBottom.add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlBottom, gridBagConstraints);

        mnuFile.setText(bundle.getString("AddressBook.mnuFile.text")); // NOI18N

        mnuOpenFile.setText(bundle.getString("AddressBook.mnuOpenFile.text")); // NOI18N
        mnuOpenFile.setEnabled(false);
        mnuFile.add(mnuOpenFile);

        mnuSave.setText(bundle.getString("AddressBook.mnuSave.text")); // NOI18N
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSave);

        mnuSaveAs.setText(bundle.getString("AddressBook.mnuSaveAs.text")); // NOI18N
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        mnuFile.add(mnuSaveAs);

        mnuClose.setText(bundle.getString("AddressBook.mnuClose.text")); // NOI18N
        mnuClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCloseActionPerformed(evt);
            }
        });
        mnuFile.add(mnuClose);

        jMenuBar1.add(mnuFile);

        mnuEdit.setText(bundle.getString("AddressBook.mnuEdit.text")); // NOI18N

        mnuCut.setText(bundle.getString("AddressBook.mnuCut.text")); // NOI18N
        mnuEdit.add(mnuCut);

        mnuCopy.setText(bundle.getString("AddressBook.mnuCopy.text")); // NOI18N
        mnuEdit.add(mnuCopy);

        mnuPaste.setText(bundle.getString("AddressBook.mnuPaste.text")); // NOI18N
        mnuPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPasteActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuPaste);

        jMenuBar1.add(mnuEdit);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFirstNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFirstNameActionPerformed

private void mnuPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPasteActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_mnuPasteActionPerformed

/**
 * Hide the window, close selected
 * @param evt 
 */
private void mnuCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCloseActionPerformed
        this.setVisible(false);
}//GEN-LAST:event_mnuCloseActionPerformed

    /**
 * Create a new contact if the edit window returns true.
 * @param evt 
 */
    private void bNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNewActionPerformed
        try {
            AddressEdit myEditwindow;
            // Create the contact object
            Contact myContact;
            myContact = new Contact();
            // Show the editor, set and get our local contact object
            myEditwindow = new AddressEdit(this, true);
            myEditwindow.setMyContact(myContact);
            myEditwindow.setLocationRelativeTo(null);
            myEditwindow.setVisible(true);
            if (myEditwindow.isExitstatus()){
                myContact = myEditwindow.getMyContact();            
                // Do something with the new object
                contactlist.add(myContact);
                this.DisplayContacts();
                SaveToFile();
            }
            myEditwindow.dispose();
            repaint(); //Repaints window and lines between "To" and "Via" checkboxes
            refreshToAndViaLists(); //To reflect changes in the address list
        }
        catch (Exception e) {
            Main.log.writelog("Error showing contacts edit window.", e, true);
        }
    }//GEN-LAST:event_bNewActionPerformed

    /**
     * Check for selected rows in the jlist
     * @param evt
     */
    private void lstContactsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstContactsMouseClicked
        // Check if there is a selected row
        Integer row;
        Contact mycontact;
        // Not a necessary check now but I am keeping it for a future addition
        if(evt.getClickCount() == 1) {
            row = this.lstContacts.getSelectedIndex();
            if (row > -1) {
                mycontact = (Contact) this.lstContacts.getSelectedValue();
                previewcontact(mycontact);
            }
        } else if(evt.getClickCount() == 2) {
            row = this.lstContacts.getSelectedIndex();
            if (row > -1) {
                EditContact();
            }
        }
    }//GEN-LAST:event_lstContactsMouseClicked

    /**
     * Look for a selected row and try to edit that object
     */
    private void EditContact(){
        // Check if there is a selected row
        Integer row;
        Contact mycontact;

        try {
            // Is there a selected row in the list?
            row = this.lstContacts.getSelectedIndex();
            if (row > -1) {
                mycontact = (Contact) this.lstContacts.getSelectedValue();
                AddressEdit myEditwindow;
                // Show the editor, set and get our local contact object
                myEditwindow = new AddressEdit(this, true);
                myEditwindow.setMyContact(mycontact);
                myEditwindow.setLocationRelativeTo(null);
                myEditwindow.setVisible(true);
                if (myEditwindow.isExitstatus()) {
                    mycontact = myEditwindow.getMyContact();
                    // Put it back
                    this.lstContacts.setSelectedValue(contact, true);
                    previewcontact(contact);
                    SaveToFile();
                }
                myEditwindow.dispose();
                repaint(); //Repaints window and lines between "To" and "Via" checkboxes
                refreshToAndViaLists(); //To reflect changes in the address list
            }
        } catch (Exception e) {
            Main.log.writelog("Error showing contacts edit window: "+e.getMessage(), e, true);
        }
    }

    /**
     * Time to delete a contact?
     */
    private void deletecontact(){
        // Check if there is a selected row
        Integer row;
        Contact mycontact;
        Object[] options = {"Yes, delete!", "No, keep!"};
        DefaultListModel myListModel;
        
        
        myListModel = (DefaultListModel) lstContacts.getModel();
        // Is there a selected row in the list?
        row = lstContacts.getSelectedIndex();
        if (row > -1) {
            mycontact = (Contact) lstContacts.getSelectedValue();
            if (mycontact != null) {
                int n = JOptionPane.showOptionDialog(this, "Would you like to delete this contact?\n"
                + mycontact.getFirstName()+ " " +mycontact.getLastName(),
                "Delete contact?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
                if (n==0){
                    int x = lstContacts.getSelectedIndex();
                    //myListModel.remove(n); //Bug
                    myListModel.remove(x);
                    // Update internal list
                    contactlist.clear();
                    for (int i = 0; i < myListModel.getSize(); i++) {
                        mycontact = (Contact) myListModel.get(i);
                        // Initialize the list with items
                        contactlist.add(i, mycontact);
                    }
                    this.DisplayContacts();
                    this.SaveToFile();
                    refreshToAndViaLists(); //To reflect changes in the address list
                }
            }
        }
    }

    /**
     * Save all changes to a csv file
     * @param evt
     */
    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveActionPerformed
        SaveToFile();
    }//GEN-LAST:event_mnuSaveActionPerformed

    /**
     * Save the contacts to the default file
     */
    private void SaveToFile(){
         try{
            Contact mycontact;
            //Save all the contacts to a file
            FileWriter fstream = new FileWriter(contfilename);
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i = 0; i < contactlist.size(); i++) {
                mycontact = contactlist.get(i);
                out.write(mycontact.GetDataAsCSV());
                out.newLine();
            }
            //Close the output stream
            out.close();
            }
        catch (Exception e){//Catch exception if any
            Main.log.writelog("Could not save to file: "+e.getMessage(), e, true);
        }
    }
    
    /**
     * Check if a contact was selected, if so then do something...
     * @param evt 
     */
    private void lstContactsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lstContactsKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_lstContactsKeyPressed

    /**
     * Edit a selected row
     * @param evt
     */
    private void bPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPropertiesActionPerformed
        EditContact();
    }//GEN-LAST:event_bPropertiesActionPerformed

    private void bDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteActionPerformed
        deletecontact();
    }//GEN-LAST:event_bDeleteActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        Saveas();
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void txtOtherCallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOtherCallActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOtherCallActionPerformed

    private void txtNicknameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNicknameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNicknameActionPerformed

    private void checkToMobileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkToMobileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkToMobileActionPerformed

    private void checkToEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkToEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkToEmailActionPerformed

    private void textMobileAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textMobileAliasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textMobileAliasActionPerformed

    private void checkToCallsignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkToCallsignActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkToCallsignActionPerformed

    private void textIotPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textIotPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textIotPasswordActionPerformed

    /**
     * Save the contacts to a file selected by the user
     */
    private void Saveas(){
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        Contact mycontact;
        try {
            //In response to a button click:
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //Save all the contacts to a file
                FileWriter fstream = new FileWriter(file);
                //FileWriter fstream = new FileWriter(contfilename);
                BufferedWriter out = new BufferedWriter(fstream);
                for (int i = 0; i < contactlist.size(); i++) {
                    mycontact = contactlist.get(i);
                    out.write(mycontact.GetDataAsCSV());
                    out.newLine();
                }
                //Close the output stream
                out.close();                
            }
        } catch (Exception ex) {
            Main.log.writelog("Could not save to file: "+ex.getMessage(), ex, true);
        } 
    }


    /**
     * Show the contact info at the bottom of the address book
     * @param mycontact 
     */
    private void previewcontact(Contact mycontact){
        if (mycontact != null){
            txtFirstName.setText(mycontact.getFirstName());
            txtLastName.setText(mycontact.getLastName());
            txtHamCall.setText(mycontact.getHamCallsign());
            txtOtherCall.setText(mycontact.getOtherCallsign());
            txtEmail.setText(mycontact.getEmail());
            txtMMSI.setText(mycontact.getMMSI());
            txtMobilePhone.setText(mycontact.getMobilePhone());
            txtNotes.setText(mycontact.getNotes());
            txtNickname.setText(mycontact.getNickname());
            txtPhone.setText(mycontact.getPhone());
            textEmailAlias.setText(mycontact.getEmailAlias());
            textMobileAlias.setText(mycontact.getMobilePhoneAlias());
            checkToCallsign.setSelected(mycontact.getShowInTO().equals("Y"));
            checkVia.setSelected(mycontact.getShowInVIA().equals("Y"));
            textRelayingPassword.setText(mycontact.getRelayingPassword());
            textIotPassword.setText(mycontact.getIotPassword());
            checkToMobile.setSelected(mycontact.getShowMobileInTO().equals("Y"));
            checkToEmail.setSelected(mycontact.getShowEmailInTO().equals("Y"));
            //Make checkboxes "read only" (prevent from being changed)
            EventListener[] listeners = checkToCallsign.getListeners(MouseListener.class);
            for (EventListener eventListener : listeners) {
                checkToCallsign.removeMouseListener((MouseListener) eventListener);
            }
            checkToCallsign.setFocusable(false);
            listeners = checkVia.getListeners(MouseListener.class);
            for (EventListener eventListener : listeners) {
                checkVia.removeMouseListener((MouseListener) eventListener);
            }
            checkVia.setFocusable(false);
            listeners = checkToEmail.getListeners(MouseListener.class);
            for (EventListener eventListener : listeners) {
                checkToEmail.removeMouseListener((MouseListener) eventListener);
            }
            checkToEmail.setFocusable(false);
            listeners = checkToMobile.getListeners(MouseListener.class);
            for (EventListener eventListener : listeners) {
                checkToMobile.removeMouseListener((MouseListener) eventListener);
            }
            checkToMobile.setFocusable(false);
        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AddressBook();
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel RMsgPanel;
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bNew;
    private javax.swing.JButton bProperties;
    private javax.swing.JCheckBox checkToCallsign;
    private javax.swing.JCheckBox checkToEmail;
    private javax.swing.JCheckBox checkToMobile;
    private javax.swing.JCheckBox checkVia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblMMSI;
    private javax.swing.JLabel lblNickname;
    private javax.swing.JList<javaradiomsg.Contact> lstContacts;
    private javax.swing.JMenuItem mnuClose;
    private javax.swing.JMenuItem mnuCopy;
    private javax.swing.JMenuItem mnuCut;
    private javax.swing.JMenu mnuEdit;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenuItem mnuOpenFile;
    private javax.swing.JMenuItem mnuPaste;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel passwordLabel1;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JLabel showInToLabel;
    private javax.swing.JLabel showInViaLabel;
    private javax.swing.JTextField textEmailAlias;
    private javax.swing.JTextField textIotPassword;
    private javax.swing.JTextField textMobileAlias;
    private javax.swing.JTextField textRelayingPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtHamCall;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtMMSI;
    private javax.swing.JTextField txtMobilePhone;
    private javax.swing.JTextField txtNickname;
    private javax.swing.JTextField txtNotes;
    private javax.swing.JTextField txtOtherCall;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables

}
