/*
 * MekWars - Copyright (C) 2025 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package mekwars.client.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import mekwars.client.MWClient;
import mekwars.client.gui.SplashWindow;

public final class SignonDialog extends JDialog implements ActionListener {
    private static final int DIALOG_WIDTH = 450;
    private static final int DIALOG_HEIGHT = 300;
	
	private static final String USERNAME_COMMAND = "user";
	private static final String PASSWORD_COMMAND = "password";
	private static final String OKAY_COMMAND = "okay";
	private static final String CANCEL_COMMAND = "cancel";
	private static final String IP_ADDRESS_COMMAND = "ip address";
	private static final String CHAT_PORT_COMMAND = "chatport";
	private static final String DATA_PORT_COMMAND = "dataport";
	
	
    private JCheckBox savePassword;
	private JCheckBox autoconnect;
	
    private MWClient mwClient;
    private ResourceBundle resourceMap;
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField;
    private JTextField ipaddressField;
    private JTextField chatPortField;
    private JTextField dataPortField;

    public SignonDialog(MWClient mwClient, Locale locale) {
        this(null, mwClient, locale);
    }

    public SignonDialog(JFrame parent, MWClient mwClient, Locale locale) {
        this.mwClient = mwClient;
        this.resourceMap = ResourceBundle.getBundle("mekwars.SignonDialog", locale);
        setupLayout(
            mwClient.getConfig().getParam("NAME"),
            mwClient.getConfig().getParam("NAMEPASSWORD"),
            mwClient.getConfig().getParam("SERVERIP"),
            mwClient.getConfig().getParam("SERVERPORT"),
            mwClient.getConfig().getParam("DATAPORT"),
            Boolean.valueOf(mwClient.getConfig().getParam("NAMEPASSWORDSAVED")),
            Boolean.valueOf(mwClient.getConfig().getParam("AUTOCONNECT"))
        );
        setLocationRelativeTo(parent);
    }

    public SignonDialog(MWClient mwClient, Locale locale, String hostname, String chatPort,
            String dataPort) {
        this(null, mwClient, locale, hostname, chatPort, dataPort);
    }

    public SignonDialog(JFrame parent, MWClient mwClient, Locale locale, String hostname,
            String chatPort, String dataPort) {
        super(parent, true);
        this.mwClient = mwClient;
        this.resourceMap = ResourceBundle.getBundle("mekwars.SignonDialog", locale);
        setupLayout(
            mwClient.getConfig().getParam("NAME"),
            mwClient.getConfig().getParam("NAMEPASSWORD"),
            hostname,
            chatPort,
            dataPort,
            Boolean.valueOf(mwClient.getConfig().getParam("NAMEPASSWORDSAVED")),
            Boolean.valueOf(mwClient.getConfig().getParam("AUTOCONNECT"))
        );
        setLocationRelativeTo(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (USERNAME_COMMAND.equals(command)) {
            ((Component) e.getSource()).requestFocus();
        } else if (PASSWORD_COMMAND.equals(command)) {
            ((Component) e.getSource()).requestFocus();
        } else if (IP_ADDRESS_COMMAND.equals(command)) {
            ((Component) e.getSource()).requestFocus();
        } else if (CHAT_PORT_COMMAND.equals(command)) {
            ((Component) e.getSource()).requestFocus();
        } else if (DATA_PORT_COMMAND.equals(command)) {
            JButton okayButton = (JButton) e.getSource();
            okayButton.doClick(200);
            dispose();
        } else if (OKAY_COMMAND.equals(command)) {
            connect();
            dispose();
            mwClient.connectDataFetcher();
            SplashWindow splashWindow = new SplashWindow(mwClient, resourceMap.getLocale());
            splashWindow.setVisible(true);
        } else if (CANCEL_COMMAND.equals(command)) {
            dispose();
        }
    }

    protected void setupLayout(String username, String password, String hostname, String chatPort,
            String dataPort, boolean shouldSavePassword, boolean shouldAutoconnect) {

        setTitle(resourceMap.getString("title.text"));
        setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        setLayout(new GridBagLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(setupPane(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(setupButtonPane(), constraints);

        pack();
        setVisible(true);
        
        ////Is a username known? if so, show it..
        usernameField.setText(username);
        if (shouldSavePassword) {
            passwordField.setText(password);
        }
        ipaddressField.setText(hostname);
        chatPortField.setText(chatPort);
        dataPortField.setText(dataPort);
        savePassword.setSelected(shouldSavePassword);
        autoconnect.setSelected(shouldAutoconnect);
    }

    protected JPanel setupPane() {
        JPanel textPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        final Set forwardKeys = textPane.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        final Set newForwardKeys = new HashSet(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        textPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

        JLabel usernameLabel = new JLabel(
            resourceMap.getString("username.text"),
            SwingConstants.LEFT
        );
        usernameLabel.setToolTipText(resourceMap.getString("username.tooltip"));
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weightx = 0.2;
        textPane.add(usernameLabel, constraints);

        usernameField = new JTextField();
        usernameField.setActionCommand(USERNAME_COMMAND);
        usernameField.addActionListener(this);
        usernameField.setToolTipText(resourceMap.getString("username.tooltip"));
        usernameField.setName("usernameField");
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.weightx = 0.60;
        textPane.add(usernameField, constraints);

        constraints.weightx = 0.0;

        JLabel passwordLabel = new JLabel(
            resourceMap.getString("password.text"),
            SwingConstants.LEFT
        );
        passwordLabel.setToolTipText(resourceMap.getString("password.tooltip"));
        constraints.gridy = 1;
        constraints.gridx = 0;
        textPane.add(passwordLabel, constraints);

        passwordField = new JPasswordField();
        passwordField.setActionCommand(PASSWORD_COMMAND);
        passwordField.addActionListener(this);
        passwordField.setToolTipText(resourceMap.getString("password.tooltip"));
        passwordField.setName("passwordField");
        constraints.gridy = 1;
        constraints.gridx = 1;
        textPane.add(passwordField, constraints);

        JLabel ipaddressLabel = new JLabel(
            resourceMap.getString("ipAddress.text"),
            SwingConstants.LEFT
        );
        ipaddressLabel.setToolTipText(resourceMap.getString("ipAddress.tooltip"));
        constraints.gridy = 2;
        constraints.gridx = 0;
        textPane.add(ipaddressLabel, constraints);

        ipaddressField = new JTextField();
        ipaddressField.addActionListener(this);
        ipaddressField.setActionCommand(IP_ADDRESS_COMMAND);
        ipaddressField.setToolTipText(resourceMap.getString("ipAddress.tooltip"));
        ipaddressField.setName("ipaddressField");
        constraints.gridy = 2;
        constraints.gridx = 1;
        textPane.add(ipaddressField, constraints);

        JLabel chatPortLabel = new JLabel(
            resourceMap.getString("chatPort.text"),
            SwingConstants.LEFT
        );
        chatPortLabel.setToolTipText(resourceMap.getString("chatPort.tooltip"));
        constraints.gridy = 3;
        constraints.gridx = 0;
        textPane.add(chatPortLabel, constraints);

        chatPortField = new JTextField();
        chatPortField.addActionListener(this);
        chatPortField.setActionCommand(CHAT_PORT_COMMAND);
        chatPortField.setToolTipText(resourceMap.getString("chatPort.tooltip"));
        chatPortField.setName("chatPortField");
        constraints.gridy = 3;
        constraints.gridx = 1;
        textPane.add(chatPortField, constraints);

        JLabel dataPortLabel = new JLabel(
            resourceMap.getString("dataPort.text"),
            SwingConstants.LEFT
        );
        dataPortLabel.setToolTipText(resourceMap.getString("dataPort.tooltip"));
        constraints.gridy = 4;
        constraints.gridx = 0;
        textPane.add(dataPortLabel, constraints);

        dataPortField = new JTextField();
        dataPortField.setActionCommand(DATA_PORT_COMMAND);
        dataPortField.setToolTipText(resourceMap.getString("dataPort.tooltip"));
        dataPortField.setName("dataPortField");
        constraints.gridy = 4;
        constraints.gridx = 1;
        textPane.add(dataPortField, constraints);

        savePassword = new JCheckBox(resourceMap.getString("savePassword.text"));
        savePassword.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                autoconnect.setSelected(false);
            }
        });
        savePassword.setName("savePassword");
        constraints.gridy = 5;
        constraints.gridx = 0;
        textPane.add(savePassword, constraints);

        autoconnect = new JCheckBox(resourceMap.getString("autoconnect.text"));
        autoconnect.setName("autoconnect");
        autoconnect.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                savePassword.setSelected(true);
            }
        });
        constraints.gridy = 5;
        constraints.gridx = 1;
        textPane.add(autoconnect, constraints);
        return textPane;
    }

    protected JPanel setupButtonPane() {
        JPanel buttonPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton okayButton = new JButton(resourceMap.getString("okayButton.text"));
        okayButton.setActionCommand(OKAY_COMMAND);
        okayButton.addActionListener(this);
        okayButton.setName("ok");
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weightx = 0.8;
        constraints.anchor = GridBagConstraints.CENTER;
        buttonPane.add(okayButton, constraints);

        JButton cancelButton = new JButton(resourceMap.getString("cancelButton.text"));
        cancelButton.setActionCommand(CANCEL_COMMAND);
        cancelButton.addActionListener(this);
        cancelButton.setName("cancel");
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.weightx = 0.2;
        buttonPane.add(cancelButton, constraints);
        return buttonPane;

    }

    protected void connect() {
       mwClient.setUsername(usernameField.getText());
       mwClient.getConfig().setParam("NAME", usernameField.getText());
       mwClient.getConfig().setParam("SERVERPORT", chatPortField.getText());
       mwClient.getConfig().setParam("DATAPORT", dataPortField.getText());
       mwClient.getConfig().setParam("SERVERIP", ipaddressField.getText());
       mwClient.getConfig().setParam(
           "NAMEPASSWORDSAVED",
           String.valueOf(savePassword.isSelected())
       );
       mwClient.getConfig().setParam("AUTOCONNECT", String.valueOf(autoconnect.isSelected()));
       if (savePassword.isSelected()) {
           mwClient.getConfig().setParam(
               "NAMEPASSWORD",
               String.valueOf(passwordField.getPassword())
           );
       } else {
           mwClient.getConfig().setParam("NAMEPASSWORD", "");
       }
       mwClient.setPassword(String.valueOf(passwordField.getPassword()));
       mwClient.getConfig().saveConfig();
    }
}
