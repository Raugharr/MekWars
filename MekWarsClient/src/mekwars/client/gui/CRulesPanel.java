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

package mekwars.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import mekwars.client.MWClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to display simple rules tab
 *
 * @author Salient
 */

public class CRulesPanel extends JPanel {
    private static final long serialVersionUID = 5547551469995402891L;
    private static final Logger logger = LogManager.getLogger(CRulesPanel.class);

    MWClient mwclient;

    public CRulesPanel(MWClient client) {
        mwclient = client;

        setLayout(new BorderLayout());
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        String rulesLocation = mwclient.getServerConfigs("Rules_Location");
        File rulesFile = new File(rulesLocation);

        if (rulesFile != null) {
            try {
                editorPane.setContentType("text/html");
                editorPane.setText(Files.readString(Path.of(rulesLocation)));
            } catch (IOException e)  {
                logger.error("Bad URL: " + rulesFile);
                logger.catching(e);
            }
        } else {
            logger.error("Couldn't find: ServerRules.html");
        }

        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));

        add(editorScrollPane);
    }
}
