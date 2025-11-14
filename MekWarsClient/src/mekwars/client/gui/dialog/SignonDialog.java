/*
 * MekWars - Copyright (C) 2004 
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import mekwars.client.MWClient;
import mekwars.client.common.ServerInfo;
import mekwars.client.gui.SplashWindow;

public final class SignonDialog implements ActionListener {
	
	private static final String usernameCommand = "user";
	private static final String passwordCommand = "password";
	private static final String okayCommand = "okay";
	private static final String cancelCommand = "cancel";
	private static final String windowName = "MekWars Login";
	private static final String ipaddressCommand = "ip address";
	private static final String chatPortCommand = "chatport";
	private static final String dataPortCommand = "dataport";
	
	private final JTextField usernameField = new JTextField();
	private final JPasswordField passwordField = new JPasswordField();
	private final JTextField ipaddressField = new JTextField();
	private final JTextField chatPortField = new JTextField();
	private final JTextField dataPortField = new JTextField();
	
	private final JButton okayButton = new JButton("OK");
	private final JButton cancelButton = new JButton("Cancel");
	
	private JDialog dialog;
	private JOptionPane pane;
    private MWClient mwClient;
	
	public SignonDialog(MWClient mwClient) {
        this.mwClient = mwClient;
		
		// Create the labels and buttons
		JLabel usernameLabel  = new JLabel("Username: ", SwingConstants.LEFT);
		JLabel passwordLabel  = new JLabel("Password (none if unregistered): ", SwingConstants.LEFT);
		JLabel ipaddressLabel = new JLabel("IP Address: ",SwingConstants.LEFT);
		JLabel chatPortLabel  = new JLabel("Chat Port: ",SwingConstants.LEFT);
		JLabel dataPortLabel  = new JLabel("Data Port: ",SwingConstants.LEFT);
		
		// Set the actions to generate
		usernameField.setActionCommand(usernameCommand);
		passwordField.setActionCommand(passwordCommand);
		chatPortField.setActionCommand(chatPortCommand);
		dataPortField.setActionCommand(dataPortCommand);
		ipaddressField.setActionCommand(ipaddressCommand);
		okayButton.setActionCommand(okayCommand);
		cancelButton.setActionCommand(cancelCommand);
		
		// Set the listeners to this object
		usernameField.addActionListener(this);
		passwordField.addActionListener(this);
		ipaddressField.addActionListener(this);
		chatPortField.addActionListener(this);
		okayButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		// Set tool tips (balloon help)
		usernameLabel.setToolTipText("Username for remote systems");
		passwordLabel.setToolTipText("Password for remote systems");
		ipaddressLabel.setToolTipText("IP address for remote systems");
		chatPortLabel.setToolTipText("Port which server uses to host chat");
		dataPortLabel.setToolTipText("Port which server uses to host data");
		okayButton.setToolTipText("Use this username and password");
		cancelButton.setToolTipText("Quit MekWars");
		ipaddressField.setToolTipText("IP address for remote systems");
		
		// Create the panel holding the labels and text fields
		JPanel textPanel = new JPanel(new GridLayout(5,4), false);
		textPanel.add(usernameLabel);
		textPanel.add(usernameField);
		textPanel.add(passwordLabel);
		textPanel.add(passwordField);
		textPanel.add(ipaddressLabel);
		textPanel.add(ipaddressField);
		textPanel.add(chatPortLabel);
		textPanel.add(chatPortField);
		textPanel.add(dataPortLabel);
		textPanel.add(dataPortField);
		
		// Create the panel that will hold the entire UI
		JPanel mainPanel = new JPanel(false);
		
		// Set the user's options
		Object[] options = {okayButton, cancelButton};
		
		// Create the pane containing the buttons
		pane = new JOptionPane(textPanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, options,
				usernameField);
		
		// Create the main dialog and set the default button
		dialog = pane.createDialog(mainPanel, windowName);
		dialog.getRootPane().setDefaultButton(okayButton);
		
		//Is a username known? if so, show it..
		usernameField.setText(mwClient.getConfig().getParam("NAME"));
		passwordField.setText(mwClient.getConfig().getParam("NAMEPASSWORD"));
		ipaddressField.setText(mwClient.getConfig().getParam("SERVERIP"));
		chatPortField.setText(mwClient.getConfig().getParam("SERVERPORT"));
		dataPortField.setText(mwClient.getConfig().getParam("DATAPORT"));
		
		// Show the dialog and get the user's input
		dialog.setVisible(true);
		dialog.requestFocus();
		usernameField.requestFocus();
		dialog.setLocationRelativeTo(mwClient.getMainFrame());
		if (pane.getValue() == okayButton) {
			mwClient.getConfig().setParam("NAME",usernameField.getText());
			mwClient.setUsername(usernameField.getText());
			mwClient.setPassword(new String(passwordField.getPassword()));
			mwClient.getConfig().setParam("SERVERPORT",chatPortField.getText());
			mwClient.getConfig().setParam("DATAPORT", dataPortField.getText());
			mwClient.getConfig().setParam("SERVERIP",ipaddressField.getText());
		} else {
            //not ok with signing on? ok. quit!
            System.exit(0);
        }
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(usernameCommand)) {
			passwordField.requestFocus();
		} else if (command.equals(passwordCommand)) {
			ipaddressField.requestFocus();
		} 
		else if ( command.equals(ipaddressCommand)){
			chatPortField.requestFocus();
		} else if ( command.equals(chatPortCommand)){
			dataPortField.requestFocus();
		} else if (command.equals(dataPortCommand)){
			okayButton.doClick(200);
			pane.setValue(okayButton);
			dialog.dispose();
		} else if (command.equals(okayCommand)) {
			pane.setValue(okayButton);
			dialog.dispose();
            mwClient.connectDataFetcher();
            SplashWindow splashWindow = new SplashWindow(mwClient, Locale.US);
            splashWindow.setVisible(true);
		} else if (command.equals(cancelCommand)) {
			pane.setValue(cancelButton);
			dialog.dispose();
		}
	}
}
