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

/*
 * MechInfo.java
 *
 * Created on June 14, 2002, 9:02 PM
 */

package mekwars.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import megamek.client.ui.swing.tileset.MechTileset;
import megamek.client.ui.swing.util.RotateFilter;
import megamek.common.Entity;
import megamek.common.Mech;
import megamek.common.Tank;
import mekwars.client.GUIClientConfig;
import mekwars.client.MWClient;
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.client.gui.icons.StatusIconsTable;
import mekwars.client.gui.icons.unitstatus.AmmoStatus;
import mekwars.client.gui.icons.unitstatus.ArmorStatus;
import mekwars.client.gui.icons.unitstatus.CommanderStatus;
import mekwars.client.gui.icons.unitstatus.EngineStatus;
import mekwars.client.gui.icons.unitstatus.EquipmentStatus;
import mekwars.client.gui.icons.unitstatus.PilotStatus;
import mekwars.client.gui.icons.unitstatus.RepairStatus;
import mekwars.client.gui.icons.unitstatus.UnitStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Steve Hawkins
 */

public class MechInfo extends JPanel {
    private static final Logger LOGGER = LogManager.getLogger(MechInfo.class);

    public static final int WIDTH = 84;
    public static final int HEIGHT = 72;

    private static final long serialVersionUID = 4308503800966118202L;
    protected static MechTileset mt;
    private JLabel lblName;
    private JLabel lblImage;
    private JLabel lblStatus;
    private StatusIconsTable statusIconsTable;

    MWClient mwclient = null;
    GUIClientConfig Config = null;
    ImageIcon previewIcon = null;
    CUnit cm = null;
    CArmy army = null;

    /**
     * Creates new general-purpose MechInfo.
     * 
     * Used to generate images in HQ, BM, etx.
     */
    public MechInfo(MWClient client) {
        mwclient = client;
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        if (mwclient != null) {
            Config = mwclient.getConfig();
        }

        lblImage = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                // First draw the background image - tiled
                if (Config.isParam("UNITHEX")) {
                    ImageIcon image = new ImageIcon((new ImageIcon("data/images/hexes/boring/beige_plains_0.gif")).getImage().getScaledInstance(WIDTH, getHeight(), Image.SCALE_DEFAULT));
                    g.drawImage(image.getImage(), (getWidth() - image.getIconWidth()) / 2, (getHeight() - image.getIconHeight()) / 2, null, null);
                }

                // Now let the regular paint code do it's work
                Icon icon = getIcon();
                icon.paintIcon(this, g, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
            }
        }; // end new JLabel(LBL Image)

        statusIconsTable = new StatusIconsTable(
            ResourceBundle.getBundle("mekwars.UnitStatus", client.getGUIClient().getLocale()),
            4,
            15,
            15, 
            new CommanderStatus(),
            new PilotStatus(),
            new RepairStatus(mwclient),
            new EngineStatus(),
            new EquipmentStatus(),
            new ArmorStatus(),
            new AmmoStatus()
        );

        lblName = new JLabel();
        setLayout(new GridBagLayout());

        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);

        lblStatus = new JLabel(statusIconsTable);
        add(lblStatus, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(lblImage, gridBagConstraints);

        lblName.setHorizontalAlignment(SwingConstants.CENTER);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        add(lblName, gridBagConstraints);
    }

    /**
     * Creates new MechInfo for use in previews. Is passed a ficticious config
     * which contains preview camo.
     * 
     * Used to generate images in HQ, BM, etc.
     */
    public MechInfo(ImageIcon preview) {
        // set the preview icon
        this.previewIcon = preview;
        Config = null;

        GridBagConstraints gridBagConstraints;
        if (mwclient != null) {
            Config = mwclient.getConfig();
        }

        lblImage = new JLabel() {

            @Override
            public void paint(Graphics g) {
                // first draw the background image - tiled
                ImageIcon image = new ImageIcon((new ImageIcon("data/images/hexes/boring/beige_plains_0.gif")).getImage().getScaledInstance(80, 68, Image.SCALE_DEFAULT));
                g.drawImage(image.getImage(), (getWidth() - image.getIconWidth()) / 2, (getHeight() - image.getIconHeight()) / 2, null, null);

                // Now let the regular paint code do it's work
                Icon icon = getIcon();
                icon.paintIcon(this, g, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
                // super.paint(g);
            }
        };// end new JLabel(LBL Image)

        lblName = new JLabel();
        setLayout(new GridBagLayout());
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(lblImage, gridBagConstraints);

        lblName.setHorizontalAlignment(SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(lblName, gridBagConstraints);
    }

    public void setText(String s) {
        lblName.setText(s);
    }

    public void setImage(Image img) {
        lblImage.setIcon(new ImageIcon(img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)));
    }

    public Image getEmbeddedImage() {
        return ((ImageIcon) lblImage.getIcon()).getImage();
    }

    public static Image getImageFor(Entity m, Component c) {
        if (mt == null) {
            mt = new MechTileset(new File("data/images/units/"));
            try {
                mt.loadFromFile("mechset.txt");
            } catch (IOException ex) {
                LOGGER.error("Unable to read data/images/units/mechset.txt");
            }
        }// end if(null tileset)
        //@Salient - from what i can tell from the megamek code, passing in the component does nothing.
        return mt.imageFor(m, -1); 
    }

    public void setPreviewIcon(ImageIcon preview) {
        previewIcon = preview;
    }

    public void setUnit(Entity m) {
        Image unit = null;
        Image camo = null;
        ImageIcon camoicon = null;
        this.cm = null;

        unit = getImageFor(m, lblImage).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);

        // look for a config image to load. if no config exists,
        // try to load the preview icon.
        if (Config != null) {
            camoicon = Config.getImage("CAMO");
        } else {
            camoicon = previewIcon;
        }

        if (camoicon != null) {
            camo = camoicon.getImage();
        }

        EntityImage ei = new EntityImage(unit, 0xFFFFFF, camo, this);
        setImage(ei.loadPreviewImage());
    }

    public void setUnit(CUnit cm, CArmy army) {
        if (cm == null) {
            statusIconsTable.clear();
            return;
        }

        Entity entity = cm.getEntity();

        this.cm = cm;
        this.army = army;
        Image unit = null;
        Image camo = null;
        ImageIcon camoicon = null;
        Entity m = cm.getEntity();

        try { // @ salient, this should fix the gui problem.
        	unit = getImageFor(m, lblImage).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);     	
        } catch (Exception ex) {
        	LOGGER.error("Exception: ", ex);   	
        	try {
        	    File pathToFile = new File("./data/images/ImageMissing.png");
        	    unit = ImageIO.read(pathToFile);
        	    unit = unit.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
                LOGGER.error("incorrect image filename in mechset.txt for {} {}",
                        cm.getModelName(), CUnit.getTypeClassDesc(cm.getType()), ex);
            } catch (IOException ex2) {
                LOGGER.error("incorrect image filename in mechset.txt for {} {}",
                        cm.getModelName(), CUnit.getTypeClassDesc(cm.getType()), ex2);
        	}
        }

        // look for a config image to load. if no config exists,
        // try to load the preview icon.
        if (Config != null) {
            camoicon = Config.getImage("CAMO");
        } else {
            camoicon = previewIcon;
        }

        if (camoicon != null) {
            camo = camoicon.getImage();
        }

        EntityImage ei = new EntityImage(unit, 0xFFFFFF, camo, this);
        setImage(ei.loadPreviewImage());
        if (lblStatus.isVisible() && (entity instanceof Mech || entity instanceof Tank)) {
            statusIconsTable.setUnit(cm, army);
        }
    }

    public void setImageVisible(boolean flag) {
        lblImage.setVisible(flag);
    }

    /**
     * A class to handle the image permutations for an entity (Code from
     * megamek.common.TilesetManager class)
     */
    private class EntityImage {
        private Image base;
        private Image wreck;
        private Image icon;
        private int tint;
        private Image camo;
        private Image[] facings = new Image[6];
        private Image[] wreckFacings = new Image[6];
        private Component comp;

        private final int IMG_WIDTH = 84;
        private final int IMG_HEIGHT = 84;
        private final int IMG_SIZE = IMG_WIDTH * IMG_HEIGHT;

        public EntityImage(Image base, int tint, Image camo, Component comp) {
            this(base, null, tint, camo, comp);
        }

        public EntityImage(Image base, Image wreck, int tint, Image camo, Component comp) {
            this.base = base;
            this.tint = tint;
            this.camo = camo;
            this.comp = comp;
            this.wreck = wreck;
        }

        public void loadFacings() {
            base = applyColor(base);

            icon = base.getScaledInstance(56, 48, Image.SCALE_SMOOTH);
            for (int i = 0; i < 6; i++) {
                ImageProducer rotSource = new FilteredImageSource(base.getSource(), new RotateFilter((Math.PI / 3) * (6 - i)));
                facings[i] = comp.createImage(rotSource);
            }

            if (wreck != null) {
                wreck = applyColor(wreck);
                for (int i = 0; i < 6; i++) {
                    ImageProducer rotSource = new FilteredImageSource(wreck.getSource(), new RotateFilter((Math.PI / 3) * (6 - i)));
                    wreckFacings[i] = comp.createImage(rotSource);
                }
            }
        }

        public Image loadPreviewImage() {
            base = applyColor(base);
            return base;
        }

        public Image getFacing(int facing) {
            return facings[facing];
        }

        public Image getWreckFacing(int facing) {
            return wreckFacings[facing];
        }

        public Image getBase() {
            return base;
        }

        public Image getIcon() {
            return icon;
        }


        private Image applyColor(Image image) {
            Image iMech;
            boolean useCamo = (camo != null);

            iMech = image;

            int[] pMech = new int[IMG_SIZE];
            int[] pCamo = new int[IMG_SIZE];
            PixelGrabber pgMech = new PixelGrabber(iMech, 0, 0, IMG_WIDTH, IMG_HEIGHT, pMech, 0, IMG_WIDTH);

            try {
                pgMech.grabPixels();
            } catch (InterruptedException e) {
                LOGGER.error("EntityImage.applyColor(): Failed to grab pixels for mech image." + e.getMessage());
                return image;
            }
            if ((pgMech.getStatus() & ImageObserver.ABORT) != 0) {
                LOGGER.error("EntityImage.applyColor(): Failed to grab pixels for mech image. ImageObserver aborted.");
                return image;
            }

            if (useCamo) {
                PixelGrabber pgCamo = new PixelGrabber(camo, 0, 0, IMG_WIDTH, IMG_HEIGHT, pCamo, 0, IMG_WIDTH);
                try {
                    pgCamo.grabPixels();
                } catch (InterruptedException e) {
                    LOGGER.error("EntityImage.applyColor(): Failed to grab pixels for camo image." + e.getMessage());
                    return image;
                }
                if ((pgCamo.getStatus() & ImageObserver.ABORT) != 0) {
                    LOGGER.error("EntityImage.applyColor(): Failed to grab pixels for mech image. ImageObserver aborted.");
                    return image;
                }
            }

            for (int i = 0; i < IMG_SIZE; i++) {
                int pixel = pMech[i];
                int alpha = (pixel >> 24) & 0xff;

                if (alpha != 0) {
                    int pixel1 = useCamo ? pCamo[i] : tint;
                    float red1 = ((float) ((pixel1 >> 16) & 0xff)) / 255;
                    float green1 = ((float) ((pixel1 >> 8) & 0xff)) / 255;
                    float blue1 = ((float) ((pixel1) & 0xff)) / 255;

                    float black = ((pMech[i]) & 0xff);

                    int red2 = Math.round(red1 * black);
                    int green2 = Math.round(green1 * black);
                    int blue2 = Math.round(blue1 * black);

                    pMech[i] = (alpha << 24) | (red2 << 16) | (green2 << 8) | blue2;
                }
            }

            image = comp.createImage(new MemoryImageSource(IMG_WIDTH, IMG_HEIGHT, pMech, 0, IMG_WIDTH));
            return image;
        }
    }

    protected JLabel getStatusLabel() {
        return lblStatus;
    }
}
