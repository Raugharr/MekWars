/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original File: GraphicGimicks.java
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import megamek.common.MechSummaryCache;
import mekwars.client.MWClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplashWindow extends JDialog {
    private static final Logger LOGGER = LogManager.getLogger(SplashWindow.class);
    private static final int STATUS_OPERATIONS = 0;
    private static final int STATUS_FETCHINGDATA = 1;
    private static final int STATUS_LOADINGMECHS = 2;
    private static final int STATUS_MIN = STATUS_OPERATIONS;
    private static final int STATUS_MAX = STATUS_LOADINGMECHS + 1;
    private static final int UPDATE_FREQUENCY = 50;
    private static final int PROGRESS_BAR_FREQUENCY = 150;

	private boolean continueAnimating;
	private JLabel imageLabel;
	private JLabel versionLabel;
	private JProgressBar progressBar;
    private Task task;
    private ResourceBundle resourceMap;
	
	public SplashWindow(MWClient mwClient, Locale locale) {
        super((JFrame)null, true);
		
        this.resourceMap = ResourceBundle.getBundle("mekwars.SplashWindow", locale);
		continueAnimating = true;
		
        setUndecorated(true);
	
        progressBar = new JProgressBar(STATUS_MIN, STATUS_MAX);
		progressBar.setMaximumSize(new Dimension(350, 10));
		progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		progressBar.setAlignmentY(Component.LEFT_ALIGNMENT);
		
        
		//load and scale the splash image
		ImageIcon splashImage = null;
		boolean useJPGImage = new File("data/images/mekwarssplash.jpg").exists();
		if (useJPGImage)
			splashImage = new ImageIcon("data/images/mekwarssplash.jpg");
		else
			splashImage = new ImageIcon("data/images/mekwarssplash.gif");
		Image tempImage = splashImage.getImage().getScaledInstance(350,350,Image.SCALE_SMOOTH);
		splashImage.setImage(tempImage);
		
		//format the label
        String text = "<HTML><CENTER>" + resourceMap.getString("title.text") + " " + MWClient.CLIENT_VERSION + "</CENTER></HTML>";
		imageLabel = new JLabel(text, splashImage, SwingConstants.CENTER);
		setTitle("MekWars Client " + MWClient.CLIENT_VERSION);
		imageLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		imageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		imageLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		imageLabel.setIconTextGap(6);
		
		//create a version label
		versionLabel = new JLabel("<HTML><CENTER><b>Initializing</b></CENTER></HTML>",SwingConstants.CENTER);
		
		//place the labels in a panel
		JPanel windowPanel = new JPanel();
		
		//give the labels a fixed amount of buffer space
		imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		versionLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));	
		
		//use a box layout to align panel components vertically
		windowPanel.setLayout(new BoxLayout(windowPanel, BoxLayout.Y_AXIS));
		
		//format the panel - JLabels and a divider
		windowPanel.add(imageLabel);
		windowPanel.add(new JSeparator());
		windowPanel.add(versionLabel);
		
		windowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		windowPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		//give the panel an attractive border
		windowPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		
		getContentPane().add(windowPanel);
		getContentPane().add(progressBar, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);
        task = new Task(mwClient, this);
        task.execute();    
	}
	
	public boolean shouldAnimate() {
		return continueAnimating;
	}
	
	public JLabel getImageLabel() {
		return versionLabel;
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}

    private void setLabelText(String s) {
        getImageLabel().setText("<HTML><CENTER><b>" + s + "</b></CENTER></HTML>");
    }

    class Task extends SwingWorker<Void, Void> {
        private MWClient mwClient;
        private SplashWindow splashWindow;
        private boolean mechSummaryLoaded = false;
        private MechSummaryCache.Listener mechSummaryCacheListener = () -> {
            mechSummaryLoaded = true;
        };

        public Task(MWClient mwClient, SplashWindow splashWindow) {
            this.mwClient = mwClient;
            this.splashWindow = splashWindow;
        }

        @Override
        public Void doInBackground() {
            try {
                MechSummaryCache.getInstance().addListener(mechSummaryCacheListener);
                setLabelText(resourceMap.getString("operations.text"));
                progressBar.setValue(progressBar.getValue() + 1);
                mwClient.setupAllOps();
                Thread.sleep(PROGRESS_BAR_FREQUENCY);

                setLabelText(resourceMap.getString("data.text"));
                progressBar.setValue(progressBar.getValue() + 1);
                mwClient.getData();
                Thread.sleep(PROGRESS_BAR_FREQUENCY);

                setLabelText(resourceMap.getString("mechs.text"));
                while (mechSummaryLoaded == false) {
                    Thread.sleep(UPDATE_FREQUENCY);
                }
            MechSummaryCache.getInstance().removeListener(mechSummaryCacheListener);
            } catch (Exception exception) {
                LOGGER.error("Exception: ", exception);
            }
            return null;
        }

        @Override
        public void done() {
            mwClient.setupMainFrame();
            splashWindow.dispose();
            mwClient.connectToServer();
        }
    }
}
