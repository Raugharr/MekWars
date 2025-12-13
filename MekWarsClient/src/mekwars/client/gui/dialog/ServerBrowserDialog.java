/*
 * MekWars - Copyright (C) 2025
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import megamek.Version;
import mekwars.client.MWClient;
import mekwars.client.common.ServerInfo;
import mekwars.client.gui.MWTable;
import mekwars.client.gui.ServerModel;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.CallbackResolverListener;
import mekwars.common.net.Connection;
import mekwars.common.net.hpgnet.packets.ServerQueryAll;
import mekwars.common.net.hpgnet.packets.ServerQueryAllResponse;
import mekwars.common.net.hpgnet.packets.ServerQueryResponse;

public class ServerBrowserDialog extends JDialog implements CallbackResolverListener {
    private static final int DIALOG_WIDTH = 600;
    private static final int DIALOG_HEIGHT = 400;
    private static final String SELECT_SERVER = "SelectServer";

    private JButton joinButton;
    private ServerModel serverModel;
    private MWClient mwClient;
    private ServerInfo selectedServer;
    private ResourceBundle resourceMap;

    public ServerBrowserDialog(JFrame frame, Locale locale, MWClient mwClient) {
        super(frame, true);
        this.mwClient = mwClient;
        this.resourceMap = ResourceBundle.getBundle("mekwars.ServerBrowserDialog", locale);
        this.serverModel = new ServerModel(locale);

        setTitle(resourceMap.getString("title.text"));
        List<ServerInfo> newList = new ArrayList<ServerInfo>();
        serverModel.setItems(newList);

        mwClient.getHpgClient().registerServerQueryAllResponse(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                close();
            }
        });
        setLayout(new GridBagLayout());

        setupLayout();
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        updateServerList(false);
        requestFocus();
        setLocationRelativeTo(frame);
    }

    public ServerInfo getSelectedServer() {
        return selectedServer;
    }

    /*
     * Called whenever the HPGClient receives a ServerQueryAllResponse.
     * @param abstractMessage The received message.
     * @param connection The connection the message came from.
     */
    public void resolverUpdate(AbstractPacket abstractMessage, Connection connection) {
        ServerQueryAllResponse message = (ServerQueryAllResponse) abstractMessage;
        List<ServerInfo> newList = new ArrayList<ServerInfo>();

        joinButton.setEnabled(false);
        for (ServerQueryResponse response : message.getQueryResponses()) {
            newList.add(new ServerInfo(response));
        }
        SwingUtilities.invokeLater(() -> serverModel.setItems(newList));
    }

    public MWClient getMwClient() {
        return mwClient;
    }

    public void close() {
        mwClient.getHpgClient().unregisterServerQueryAllResponse(this);
    }

    /*
     * Returns if the currently selected server can be joined.
     */
    protected boolean canJoinServer() {
        Version serverVersion = new Version(selectedServer.getVersion());

        return MWClient.CLIENT_VERSION.is(serverVersion);
    }

    protected void updateServerList(boolean showDialog) {
        try {
            mwClient.getHpgClient().getConnection().write(new ServerQueryAll());
        } catch (IOException exception) {
            if (showDialog) {
                JOptionPane.showMessageDialog(
                    this,
                    resourceMap.getString("refreshError.text"),
                    "Error", JOptionPane.ERROR_MESSAGE
                ); 
            }
        }
    }

    /*
     * Method caalled when we have selected a server to join.
     */
    protected void selectServer() {
        if (canJoinServer()) {
            String serverPort = String.valueOf(selectedServer.getServerPort());
            String dataPort = String.valueOf(selectedServer.getDataPort());

            dispose();
            new SignonDialog(
                    mwClient,
                    resourceMap.getLocale(),
                    selectedServer.getHostname(),
                    serverPort,
                    dataPort
            );
        } else {
            String errorString = MessageFormat.format(
                resourceMap.getString("joinError.text"),
                selectedServer.getVersion()
            ); 

            JOptionPane.showMessageDialog(this, errorString, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void setupLayout() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 4.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        add(new JScrollPane(setupServerTable()), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        add(buttonPanel, constraints);

        setupButtonPanel(buttonPanel);
    }

    protected void setupButtonPanel(JPanel buttonPanel) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        joinButton = new JButton(resourceMap.getString("join.text"));
        joinButton.setEnabled(false);
        joinButton.setName("join");
        joinButton.addActionListener(e -> selectServer());
        buttonPanel.add(joinButton, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        JButton refreshButton = new JButton(resourceMap.getString("refresh.text"));
        refreshButton.setName("refresh");
        buttonPanel.add(refreshButton, constraints);
        refreshButton.addActionListener(e -> updateServerList(true));

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        JButton quitButton = new JButton(resourceMap.getString("quit.text"));
        quitButton.setName("quit");
        quitButton.addActionListener(e -> dispose());
        buttonPanel.add(quitButton, constraints);
    }

    protected JTable setupServerTable() {
        JTable serverTable = new MWTable();

        serverTable.setName("serverTable");
        serverTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverTable.setAutoCreateRowSorter(true);

        // Double clicking calls selectServer
        serverTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);

                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1 && row != -1) {
                    selectServer();
                }
            }
        });
        
        // Map pressing enter to call selectServer.
        serverTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), SELECT_SERVER);

        serverTable.getActionMap().put(SELECT_SERVER, new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
               if (joinButton.isEnabled()) {
                   selectServer();
               } 
           }
        });

        serverTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();

                if (e.getValueIsAdjusting()) {
                    return;
                }

                if (selectionModel.isSelectionEmpty()) {
                    joinButton.setEnabled(false);
                } else {
                    int index = selectionModel.getMinSelectionIndex();
                    int row = serverTable.convertRowIndexToModel(index);

                    selectedServer = serverModel.getRow(row);
                    joinButton.setEnabled(true);
                }
            }
        });
        serverTable.setModel(serverModel);
        return serverTable;
    }
}
