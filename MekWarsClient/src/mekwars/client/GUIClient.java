/*
 * MekWars - Copyright (C) 2025
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import mekwars.client.gui.CCommPanel;
import mekwars.client.gui.CMainFrame;
import mekwars.client.gui.SplashWindow;
import mekwars.client.gui.dialog.ServerBrowserDialog;
import mekwars.client.gui.dialog.SignonDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GUIClient {
    private static final Logger LOGGER = LogManager.getLogger(GUIClient.class);

    public static final int REFRESH_STATUS = 0;
    public static final int REFRESH_USERLIST = 1;
    public static final int REFRESH_PLAYERPANEL = 2;
    public static final int REFRESH_BATTLETABLE = 4;
    public static final int REFRESH_HQPANEL = 5;
    public static final int REFRESH_BMPANEL = 6;

    private MWClient mwClient;
    private GUIClientConfig config;
    private CMainFrame mainFrame;
    private Locale locale = Locale.US;

    public GUIClient(MWClient mwClient, GUIClientConfig config) {
        this.mwClient = mwClient;
        this.config = config;
    }

    public void init() {
        boolean shouldAutoconnect = Boolean.parseBoolean(config.getParam("AUTOCONNECT"))
            && !config.getParam("SERVERIP").trim().isEmpty()
            && !config.getParam("NAME").trim().isEmpty()
            && !config.getParam("NAMEPASSWORD").trim().isEmpty();

        setupFlatLaf();
        if (shouldAutoconnect) {
            mwClient.connectDataFetcher();
            SplashWindow splashWindow = new SplashWindow(mwClient, getLocale());
            splashWindow.setVisible(true);
            mwClient.setUsername(config.getParam("NAME"));
        } else if (mwClient.getHpgClient().isConnected()) {
            ServerBrowserDialog serverBrowserDialog = new ServerBrowserDialog(null,
                    locale, mwClient);

            serverBrowserDialog.setVisible(true);

        } else {
            new SignonDialog(mwClient, getLocale());
        }
    }

    public MWClient getMWClient() {
        return mwClient;
    }

    public CMainFrame getMainFrame() {
        return mainFrame;
    }

    public Locale getLocale() {
        return locale;
    }

    /*
     * Rewritten in order to allow ConfigPage to reset the skin on the fly.
     *
     * @urgru 2.21.05
     */
    public void setLookAndFeel(boolean isRedraw) {
        String lookAndFeel = config.getParam("LOOKANDFEEL");
        LOGGER.info("SetLookAndFeel: {}", lookAndFeel);

        try {
            if (isRedraw) {
                getMainFrame().setVisible(false);
            }
            LookAndFeelInfo lookAndFeelInfo = getLookAndFeel(lookAndFeel);
            if (lookAndFeelInfo != null) {
                UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
            } else {
                LOGGER.error("Invalid LookAndFeel '{}'", lookAndFeel);
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            if (isRedraw) {
                SwingUtilities.updateComponentTreeUI(getMainFrame());
                getMainFrame().setVisible(true);
                getMainFrame().getMainPanel().getUserListPanel()
                        .resetActivityButton();
            }
        } catch (Exception ex) {
            LOGGER.catching(ex);
            try {
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.catching(e);
            }
        }
    }

    public void addToChat(String s) {
        addToChat(s, CCommPanel.CHANNEL_MAIN, null);
    }

    public void addToChat(String s, int channel) {
        addToChat(s, channel, null);
    }

    public void addToChat(String s, int channel, String tabName) {
        s = "<BODY  <font size=\"" + config.getParam("CHATFONTSIZE") + "\">"
                + s + "</font></BODY>";
        try {
            SwingUtilities.invokeLater(new CAddToChat(s, channel, tabName));
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }


    @Deprecated(since = "9.0.0", forRemoval = false)
    public void refreshGUI(int mode) {
        try {
            SwingUtilities.invokeLater(new CRefreshGUI(mode));
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    public void setupMainFrame() {
        // make the main frame
        this.mainFrame = new CMainFrame(getMWClient());

        try {
            getMainFrame().setIconImage(config.getImage("LOGOUT").getImage());
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }

        getMainFrame().validate();
        getMainFrame().setExtendedState(Integer
                .parseInt(config.getParam("WINDOWSTATE")));
        getMainFrame().setSize(Integer.parseInt(config.getParam("WINDOWWIDTH")),
                Integer.parseInt(config.getParam("WINDOWHEIGHT")));
        getMainFrame().setLocation(
                Integer.parseInt(config.getParam("WINDOWLEFT")),
                Integer.parseInt(config.getParam("WINDOWTOP")));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getMainFrame().getSize();
        // check for unacceptable dimensions
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        // set the initial mute value
        getMWClient().setSoundMuted(config.isParam("DISABLEALLSOUND"));

        // build the attack menu. at this point we know we have the
        // necessary data.
        getMainFrame().updateAttackMenu();
        getMainFrame().setVisible(true);
    }


    public void showInfoWindow(String text) {
        // Show a popup with a message
        if (!getMWClient().isDedicated()) {

            // JOptionPane.showInternalMessageDialog(getMainFrame().getContentPane(),
            // Error);
            final JDialog dialog = new JDialog(getMainFrame(), "Message");

            // Add contents to it.
            JLabel label = new JLabel("<html>" + text + "</html>");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            Container contentPane = dialog.getContentPane();
            contentPane.setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.ipadx = 30;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 0, 10);
            contentPane.add(label, gridBagConstraints);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0;
            gridBagConstraints.weighty = 0;
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton okButton = new JButton("OK");
            panel.add(okButton);
            contentPane.add(panel, gridBagConstraints);

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            // Show it.
            Dimension d = dialog.getPreferredSize();
            d.setSize(d.getWidth() + 20, d.getHeight() + 40);
            dialog.setSize(d);
            dialog.setLocationRelativeTo(getMainFrame());
            dialog.setVisible(true);

        } else {
            LOGGER.info(text);
        }
    }

    public LookAndFeelInfo getLookAndFeel(String name) {
        LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();

        for (LookAndFeelInfo lookAndFeel : lookAndFeels) {
            if (lookAndFeel.getName().equals(name)) {
                return lookAndFeel;
            }
        }
        return null;
    }

    protected void setupFlatLaf() {
        FlatLaf.registerCustomDefaultsSource("mekwars.themes");
        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        FlatLightLaf.setup();
        FlatIntelliJLaf.setup();
        FlatDarkLaf.setup();
        FlatDarculaLaf.setup();

        UIManager.installLookAndFeel("Flat Light", "com.formdev.flatlaf.FlatLightLaf");
        UIManager.installLookAndFeel("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        UIManager.installLookAndFeel("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf");
        UIManager.installLookAndFeel("Flat Darcula", "com.formdev.flatlaf.FlatDarculaLaf");
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    protected class CRefreshGUI implements Runnable {
        protected int mode = -1;

        public CRefreshGUI(int tmode) {
            mode = tmode;
        }

        @Override
        public void run() {
            if (getMainFrame() == null) {
                return;
            } // return if main frame not yet drawn (still fetching data)
            try {
                switch (mode) {
                    case REFRESH_USERLIST:
                        getMainFrame().getMainPanel().getUserListPanel().refresh();
                        break;
                    case REFRESH_PLAYERPANEL:
                        getMainFrame().getMainPanel().getPlayerPanel().refresh();
                        break;
                    case REFRESH_BATTLETABLE:
                        getMainFrame().refreshBattleTable();
                        break;
                    case REFRESH_HQPANEL:
                        getMainFrame().getMainPanel().getHQPanel().refresh();
                        break;
                    case REFRESH_BMPANEL:
                        getMainFrame().getMainPanel().getBMPanel().refresh();
                        break;
                }
            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
                // do nothing
            }
        }
    }

    protected class CAddToChat implements Runnable {
        String input = "";
        int channel = -1;
        String tabName = "";

        public CAddToChat(String tinput, int tchannel, String ttabName) {
            input = tinput;
            channel = tchannel;
            tabName = ttabName;
        }

        @Override
        public void run() {
            (getMainFrame().getMainPanel().getCommPanel()).setChat(input, channel,
                    tabName);
        }
        //@salient discord bot chat capture
        //if i wanted to capture multiple channels
        //i'd have to do it here?
    }
}
