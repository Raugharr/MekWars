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

import com.thoughtworks.xstream.io.xml.DomDriver;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import mekwars.admin.AdminMapPopupMenu;
import mekwars.client.MWClient;
import mekwars.client.campaign.CArmy;
import mekwars.client.gui.dialog.PlanetSearchDialog;
import mekwars.common.House;
import mekwars.common.Influences;
import mekwars.common.Planet;
import mekwars.common.util.MMNetXStream;
import mekwars.common.util.Position;
import mekwars.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Draws the main map component.
 *
 * @author Imi
 */

public class InnerStellarMap extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(InnerStellarMap.class);

    private static final long serialVersionUID = 8655078955521790260L;

    private InnerStellarMapConfig conf = new InnerStellarMapConfig();
    private CMapPanel mp;

    /**
     * The main client to access
     */
    private MWClient mwclient;

    /**
     * A cache for image icons. key=filename(String), value=ImageIcon
     */
    private static class IconProvider extends TreeMap<String, ImageIcon> {
        /**
         *
         */
        private static final long serialVersionUID = 4594828039895948331L;

        public ImageIcon get(String key) {
            if (!containsKey(key)) {
                put(key, new ImageIcon(key));
            }
            return super.get(key);
        }
    }

    private IconProvider iconCache = new IconProvider();

    private Planet selectedPlanet = null;

    ArrayList<ArrayList<Position>> overlayLines = new ArrayList<ArrayList<Position>>();

    private static final String[] displayStr = { "Planet Names", "Planet Control", "Factories", "Warehouses", "Attack Ranges", "Recent Changes", "Overlay", "Tooltips" };

    private static final int DISPLAY_NAMES = 0;
    private static final int DISPLAY_INFLUENCE = 1;
    private static final int DISPLAY_UNITS = 2;
    private static final int DISPLAY_WAREHOUSES = 3;
    private static final int DISPLAY_RANGES = 4;
    private static final int DISPLAY_LAST_CHANGED = 5;
    private static final int DISPLAY_OVERLAY = 6;
    private static final int DISPLAY_TOOLTIPS = 7;

    /**
     * Various display options
     *
     * @see InnerStellarMapConfig
     */
    private JCheckBoxMenuItem[] display = new JCheckBoxMenuItem[displayStr.length];

    private static final String[] filterStr = { "All", "",// filler. replaced w/ seperator
            "Factories", "Facilities", "Faction", "Disputed", "Contested" };

    /**
     * Map filtering options ; ALL, _FILLER_, Factories, Facilities, Disputed, Contested
     */
    boolean[] filterSettings = new boolean[] { true, false, true, true, true, true, true };

    private static final int FILTER_ALL = 0;
    private static final int FILTER_SEP = 1;
    private static final int FILTER_FACTORIES = 2;
    private static final int FILTER_FACILITIES = 3;
    private static final int FILTER_FACTION = 4;
    private static final int FILTER_DISPUTED = 5;
    private static final int FILTER_CONTESTED = 6;

    private JCheckBoxMenuItem[] filter = new JCheckBoxMenuItem[filterStr.length];

    /**
     * A data stucture to hold all planets marked as "changed" since last update. - see common.CampaignData.decodeMutablePlanets()
     */
    private Map<Integer, Influences> changesSinceLastRefresh;

    /**
     * Used to indicate the blinking of a planet. If true, the planets are drawn white.
     */
    private boolean blinkPhase = false;

    /**
     * Method which returns the panel underlying the map. This is used by the AdminMapPopupMenu class in admin package.
     */
    public CMapPanel getMapPanel() {
        return mp;
    }

    // private int xcoord;
    // private int ycoord;

    /**
     * If right button clicked, open the popup menu
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {

            // popup menu
            final Planet p = nearestNeighbour(scr2mapX(e.getX()), scr2mapY(e.getY()));

            if (p != null) {
                activate(p);
            }

            JPopupMenu popup = new JPopupMenu();

            // Information
            JMenuItem info = new JMenuItem("Information");
            info.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JEditorPane label;
                    if (Boolean.parseBoolean(mwclient.getServerConfigs("UseStaticMaps"))) {
                        House h = mwclient.getData().getHouseByName(mp.getPPanel().getPlanet().getOriginalOwner());
                        String color = mwclient.getServerConfigs("DisputedPlanetColor");
                        String name = "None";

                        if (h != null) {
                            color = h.getHouseColor();
                            name = h.getName();
                        }

                        label = new JEditorPane("text/html", "<html>" + mp.getPPanel().getPlanet().getAdvanceDescription(mwclient.getUser(mwclient.getUsername()).getUserlevel()) + "<b>Original Owner:</b><br><font color=" + color + ">" + name + "</font>" + "</html>");
                        label.setEditable(false);
                        label.setCaretPosition(0);
                        label.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(InnerStellarMap.this, new JScrollPane(label), "Information for " + mp.getPPanel().getPlanet().getName(), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        House h = mwclient.getData().getHouseByName(mp.getPPanel().getPlanet().getOriginalOwner());
                        String color = mwclient.getServerConfigs("DisputedPlanetColor");
                        String name = "None";

                        if (h != null) {
                            color = h.getHouseColor();
                            name = h.getName();
                        }

                        label = new JEditorPane("text/html", "<html>" + mp.getPPanel().getPlanet().getLongDescription(true) + "<b>Original Owner:</b><br><font color=" + color + ">" + name + "</font>" + "</html>");
                        // mwclient.getData().getHouseByName("hi").getName();
                        label.setEditable(false);
                        label.setCaretPosition(0);
                        label.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(InnerStellarMap.this, new JScrollPane(label), "Information for " + mp.getPPanel().getPlanet().getName(), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            popup.add(info);

            /*
             * If a planet is selected and the player is active, build an attack menu and include it in the popup.
             */
            if (p != null) {
                AttackMenu aMenu = new AttackMenu(mwclient, -1, p.getName());
                aMenu.updateMenuItems(false);
                popup.add(aMenu);
            }

            // Separator!
            popup.addSeparator();

            // Search, using planet dialog.
            JMenuItem search = new JMenuItem("Find Planet");
            search.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    createPlanetSearchDialog();
                }
            });
            popup.add(search);

            // CENTER Menu.
            JMenu centerM = new JMenu("Center Map");
            JMenuItem item = new JMenuItem("On Selected Planet");
            if (p != null) {// only add if there is a planet to center on
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        conf.getOffset().setLocation(-p.getPosition().x * conf.getScale(), p.getPosition().y * conf.getScale());
                        mp.repaint();
                    }
                });
                centerM.add(item);
            }

            item = new JMenuItem("On Natural Center");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    conf.setOffset(new Point());
                    conf.setScale(1);
                    mp.getSlider().setValue((int) Math.round(50 / conf.getScale()));
                    mp.repaint();
                }
            });
            centerM.add(item);
            popup.add(centerM);

            // DISPLAY options
            JMenu displayMenu = new JMenu("Display");
            popup.add(displayMenu);
            for (int i = 0; i < displayStr.length; ++i) {
                displayMenu.add(display[i]);
            }

            // FILTER options
            JMenu filterMenu = new JMenu("Filter");
            popup.add(filterMenu);
            for (int i = 0; i < filterStr.length; i++) {

                if (i == FILTER_SEP) {
                    filterMenu.addSeparator();
                    continue;
                }

                filterMenu.add(filter[i]);
            }

            if (mwclient.isLeader() && mwclient.getUserLevel() >= mwclient.getData().getAccessLevel("PurchaseFactory")) {
                item = new JMenuItem("Purchase Factory");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        mwclient.getGUIClient().getMainFrame().jMenuLeaderPurchaseFactory_actionPerformed(p == null ? null : p.getName());
                    }
                });
                popup.add(item);
            }

            popup.addSeparator();

            // REFRESH - one button
            item = new JMenuItem("Refresh");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    changesSinceLastRefresh.clear();
                    mwclient.refreshData();
                    mp.repaint();
                }
            });
            popup.add(item);

            if (mwclient.isMod()) {
                AdminMapPopupMenu adminMapPopupMenu = new AdminMapPopupMenu();
                adminMapPopupMenu.createMenu(mwclient, this, (int) scr2mapX(e.getX()), (int) scr2mapY(e.getY()), mp.getPPanel().getPlanet());
                popup.add((JMenu) adminMapPopupMenu);
            }

            popup.show(this, e.getX() + 10, e.getY() + 10);
        // normal left click
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() >= 2) {
                JEditorPane label;
                if (Boolean.parseBoolean(mwclient.getServerConfigs("UseStaticMaps"))) {
                    House h = mwclient.getData().getHouseByName(mp.getPPanel().getPlanet().getOriginalOwner());
                    String color = mwclient.getServerConfigs("DisputedPlanetColor");
                    String name = "None";

                    if (h != null) {
                        color = h.getHouseColor();
                        name = h.getName();
                    }

                    label = new JEditorPane("text/html", "<html>" + mp.getPPanel().getPlanet().getAdvanceDescription(mwclient.getUser(mwclient.getUsername()).getUserlevel()) + "<b>Original Owner:</b><br><font color=" + color + ">" + name + "</font>" + "</html>");
                    label.setEditable(false);
                    label.setCaretPosition(0);
                    label.setPreferredSize(new Dimension(500, 400));
                    JOptionPane.showMessageDialog(InnerStellarMap.this, new JScrollPane(label), "Information for " + mp.getPPanel().getPlanet().getName(), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    House h = mwclient.getData().getHouseByName(mp.getPPanel().getPlanet().getOriginalOwner());
                    String color = mwclient.getServerConfigs("DisputedPlanetColor");
                    String name = "None";

                    if (h != null) {
                        color = h.getHouseColor();
                        name = h.getName();
                    }

                    label = new JEditorPane("text/html", "<html>" + mp.getPPanel().getPlanet().getLongDescription(true) + "<b>Original Owner:</b><br><font color=" + color + ">" + name + "</font>" + "</html>");
                    // mwclient.getData().getHouseByName("hi").getName();
                    label.setEditable(false);
                    label.setCaretPosition(0);
                    label.setPreferredSize(new Dimension(500, 400));
                    JOptionPane.showMessageDialog(InnerStellarMap.this, new JScrollPane(label), "Information for " + mp.getPPanel().getPlanet().getName(), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /**
     * Constructs the ISMap.
     *
     * @param panel -
     *            The panel it belongs to.
     */
    InnerStellarMap(CMapPanel panel, MWClient client, CMainFrame mainFrame) {
        mwclient = client;
        setBackground(Color.BLACK);
        mp = panel;
        setOpaque(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        conf = loadMapConf(client.getCacheDir());

        try {
            parseOverlayFile();
        } catch (Throwable e) {
            if (!(e instanceof FileNotFoundException)) {
                LOGGER.error("Exception: ", e);
            }
            LOGGER.info("could not read map overlay file.");
        }

        for (int i = 0; i < displayStr.length; ++i) {
            display[i] = new JCheckBoxMenuItem(displayStr[i], conf.getDisplay()[i]);
            display[i].addActionListener(this);
        }

        // read in map filter settings
        StringTokenizer tokenizer = new StringTokenizer(mwclient.getConfigParam("MAPFILTER1"), "$");
        int currFilter = FILTER_ALL;
        while (tokenizer.hasMoreElements() || currFilter < filterStr.length) {

            String nextToken = tokenizer.nextToken();

            if (currFilter == FILTER_SEP) {
                currFilter++;
                continue;
            }

            if (nextToken != null) {
                boolean filterState = Boolean.parseBoolean(nextToken);
                filterSettings[currFilter] = filterState;
                filter[currFilter] = new JCheckBoxMenuItem(filterStr[currFilter], filterState);
                filter[currFilter].addActionListener(this);
            }

            // null. default to true and add.
            else {
                filterSettings[currFilter] = true;
                filter[currFilter] = new JCheckBoxMenuItem(filterStr[currFilter], true);
                filter[currFilter].addActionListener(this);
            }

            currFilter++;
        }

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                processTick();
            }
        });

        changesSinceLastRefresh = client.getChangesSinceLastRefresh();
        // new Thread() {
        //     @Override
        //     public void run() {
        //         while (true) {
        //             try {
        //                 Thread.sleep(1000);
        //             } catch (InterruptedException e) {
        //             }
        //             blinkPhase = !blinkPhase;
        //             if (changesSinceLastRefresh.size() > 0) {
        //                 mp.repaint();
        //             }
        //         }
        //     }
        // }.start();

        // restore previous zoom level
        Double storedZoom = Double.parseDouble(client.getConfigParam("MAPZOOMLEVEL"));
        if (storedZoom != null) {
            double storedValue = storedZoom.doubleValue();
            if (storedValue != 0) {
                conf.setScale(storedValue);
            }
        }

        // restore previous offset
        int storedXOffset = Integer.parseInt(client.getConfigParam("MAPXOFFSET"));
        int storedYOffset = Integer.parseInt(client.getConfigParam("MAPYOFFSET"));
        conf.setOffset(new Point(storedXOffset, storedYOffset));

        // restore previously selected planet
        String storedPlanetName = client.getConfigParam("SELECTEDPLANET");
        if (storedPlanetName != null && !storedPlanetName.trim().isEmpty()) {
            // planet setting exists. lets see if the planet does ...
            Planet currPlan = client.getData().getPlanetByName(storedPlanetName);
            if (currPlan != null) {
                this.activate(currPlan, false);
            }
        }
    }

    public InnerStellarMapConfig loadMapConf(String directory) {
        MMNetXStream xml = new MMNetXStream(new DomDriver());
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            InnerStellarMapConfig innerStellarMapConfig = (InnerStellarMapConfig) xml.fromXML(new FileReader(directory + "/mapconf.xml"));
            if (innerStellarMapConfig.getDisplay().length != displayStr.length) {
                throw new RuntimeException("not my file");
            }
            return innerStellarMapConfig;
        } catch (Throwable e) {
            if (!(e instanceof FileNotFoundException)) {
                LOGGER.error("Exception: ", e);
            }
            LOGGER.info("could not read map config file. Will use defaults");
            return new InnerStellarMapConfig();
        }
    }

    private void parseOverlayFile() throws Exception {
        File file = new File("data/mapoverlay.txt");
        Reader r = new BufferedReader(new FileReader(file));
        StreamTokenizer st = new StreamTokenizer(r);
        st.eolIsSignificant(true);
        st.commentChar('#');
        ArrayList<Position> line = new ArrayList<Position>();
        Position position = null;
        String color = mwclient.getConfigParam("MAPOVERLAYCOLOR");
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals("LINE") && line.size() > 0) {
                overlayLines.add(line);
                line = new ArrayList<Position>();
            } else if (st.ttype == StreamTokenizer.TT_WORD && st.sval.startsWith("COLOR")) {
                color = st.sval.substring("COLOR".length());
            } else if (st.ttype == StreamTokenizer.TT_NUMBER) {
                double x = st.nval;
                if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
                    position = new Position(x, st.nval);
                    position.setColor(color);
                    line.add(position);
                }
                while (st.ttype != StreamTokenizer.TT_EOF && st.ttype != StreamTokenizer.TT_EOL) {
                    st.nextToken();
                }
            }
        }
        if (line.size() > 0) {
            overlayLines.add(line);
        }
    }

    /**
     * Calculate the nearest neighbour for the given point If anyone has a better algorithm than this stupid kind of shit, please, feel free to exchange my brute force thing... An good idea would be an voronoi diagram and the sweep algorithm from Steven Fortune.
     */
    private Planet nearestNeighbour(double x, double y) {
        Iterator<Planet> it = mp.getData().getAllPlanets().iterator();
        double minDiff = Double.MAX_VALUE;
        double diff = 0.0;
        Planet minPlanet = null;
        while (it.hasNext()) {
            Planet p = it.next();
            diff = p.getPosition().distanceSq(x, y);
            if (diff < minDiff && planetIsVisible(p)) {
                minDiff = diff;
                minPlanet = p;
            }
        }
        return minPlanet;
    }

    /**
     * Computes the map-coordinate from the screen koordinate system
     */
    private double scr2mapX(int x) {
        return Math.round((x - getWidth() / 2 - conf.getOffset().x) / conf.getScale());
    }

    private int map2scrX(double x) {
        return (int) Math.round(getWidth() / 2 + x * conf.getScale()) + conf.getOffset().x;
    }

    private double scr2mapY(int y) {
        return Math.round((getHeight() / 2 - (y - conf.getOffset().y)) / conf.getScale());
    }

    private int map2scrY(double y) {
        return (int) Math.round(getHeight() / 2 - y * conf.getScale()) + conf.getOffset().y;
    }

    /**
     * Actually does the drawing of the map.
     */
    @Override
    public void paint(Graphics g) {
        Collection<Planet> planets = mp.getData().getAllPlanets();
        // background
        g.setColor(StringUtils.html2Color(conf.getBackgroundColor()));
        g.fillRect(0, 0, getWidth(), getHeight());
        int size = (int) Math.round(Math.max(5, Math.log(conf.getScale()) * 15 + 5));
        size = Math.max(Math.min(size, conf.getMaxDotSize()), conf.getMinDotSize());

        if (conf.getDisplay()[DISPLAY_OVERLAY] && overlayLines != null) {
            for (ArrayList<Position> points : overlayLines) {
                Position last = null;
                for (Position p : points) {
                    if (last != null) {
                        g.setColor(StringUtils.html2Color(p.getColor()));
                        g.drawLine(map2scrX(last.x), map2scrY(last.y), map2scrX(p.x), map2scrY(p.y));
                    }
                    last = p;
                }
            }
        } else {
            try {
                int x = mwclient.getConfig().getIntParam("MAPIMAGEX");
                int y = mwclient.getConfig().getIntParam("MAPIMAGEY");
                int height = (int) (mwclient.getConfig().getIntParam("MAPIMAGEHEIGHT") * conf.getScale());
                int width = (int) (mwclient.getConfig().getIntParam("MAPIMAGEWIDTH") * conf.getScale());
                ImageIcon ic = null;

                boolean useJPGImage = new File("data/images/mekwarsmap.jpg").exists();
                if (useJPGImage) {
                    ic = new ImageIcon("data/images/mekwarsmap.jpg");
                } else {
                    ic = new ImageIcon("data/images/mekwarsmap.gif");
                }

                g.drawImage(ic.getImage(), map2scrX(x), map2scrY(y), width, height, ic.getImageObserver());

            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }

        }

        // For each through the collection of Planets
        for (Planet p : planets) {
            /*
             * check the visibility of the planet. TODO: Checking on every paint is slow. We should cache the visibility data in a map of some kind and clear it on refresh or viewing option change.
             */
            if (!planetIsVisible(p)) {
                continue;
            }

            // calculate the color of the faction owner
            Integer houseID = p.getInfluence().getOwner();
            String houseColor = "";

            if (houseID == null || houseID == -1 || p.getInfluence().getInfluence(houseID) < mwclient.getMinPlanetOwnerShip(p)) {
                houseColor = mwclient.getServerConfigs("DisputedPlanetColor");
            } else {
                houseColor = mwclient.getData().getHouse(houseID).getHouseColor();
            }

            Color c = Color.WHITE;

            if (Boolean.parseBoolean(mwclient.getConfigParam("DARKERMAP"))) {
                try {
                    c = StringUtils.html2Color(houseColor);
                } catch (Exception ex) {
                    LOGGER.error("Bad House for planet: {}", p.getName(), ex);
                }
            } else {
                try {
                    c = adjustColor(StringUtils.html2Color(houseColor));
                } catch (Exception ex) {
                    LOGGER.error("Bad House for planet: {}", p.getName(), ex);
                }
            }

            if ( (c.getRed() == 0 && c.getBlue() == 0) ){
                c = Color.white;
            }

            // calculate the current screen position
            int x = map2scrX(p.getPosition().x) - size / 2;
            int y = map2scrY(p.getPosition().y) - size / 2;
            if (mp.getPPanel() != null && mp.getPPanel().getPlanet() != null && mp.getPPanel().getPlanet().equals(p)) {
                g.setColor(Color.WHITE);
                g.fillArc(x - 2, y - 2, size + 4, size + 4, 0, 360);
            }

            // planet dot
            int dotSize = size;
            boolean blink = false;
            if (conf.getDisplay()[DISPLAY_LAST_CHANGED] && blinkPhase && changesSinceLastRefresh.containsKey(p.getId())) {
                g.setColor(Color.WHITE);
                dotSize++;
                blink = true;
            } else {
                g.setColor(c);
            }
            if (size < 3) {
                g.fillRect(x, y, dotSize, dotSize);
            } else {
                g.fillArc(x, y, dotSize, dotSize, 0, 360);
            }
            // names
            if (!blink) {
                g.setColor(c);
            }
            if (conf.getDisplay()[DISPLAY_NAMES] && (conf.getShowPlanetNamesThreshold() == 0 || conf.getScale() > conf.getShowPlanetNamesThreshold())) {
                g.drawString(p.getName(), x + size, y);
            }

            // influence icon
            if (conf.getDisplay()[DISPLAY_INFLUENCE] && (conf.getShowInfluenceThreshold() == 0 || conf.getScale() > conf.getShowInfluenceThreshold())) {
                int pos = 0;
                Iterator<House> infIt = p.getInfluence().getHouses().iterator();
                while (infIt.hasNext()) {
                    House h = infIt.next();
                    String color = mwclient.getServerConfigs("DisputedPlanetColor");
                    int id = -1;

                    if (h != null) {
                        color = h.getHouseColor();
                        id = h.getId();
                    }

                    int flu = p.getInfluence().getInfluence(id) / 10;
                    if (flu != 10) {
                        Color factionColor = StringUtils.html2Color(color);
                        // g.setColor(mplanet.getMap().adjustColor(factionColor));
                        g.setColor(factionColor);
                        g.fillRect(x - 10, y + size + pos, 10, flu);
                        pos += flu;
                    }
                }
            }

            // unit factories
            if (conf.getDisplay()[DISPLAY_UNITS] && (conf.getShowUnitFactoriesThreshold() == 0 || conf.getScale() > conf.getShowUnitFactoriesThreshold())) {
                int pos = 0;

                if (p.getFactoryCount() > 0) {
                    ImageIcon staricon = iconCache.get("data/images/star.gif");
                    staricon.paintIcon(this, g, x + size + pos, y + size / 2);
                }
            }

            // warehouses
            if (conf.getDisplay()[DISPLAY_WAREHOUSES] && p.getBaysProvided() > 0) {
                g.setColor(Color.WHITE);
                g.drawString(Integer.toString(p.getBaysProvided()), x - 8, y);
            }

            if (p.isHomeWorld()) {
                ImageIcon homeicon = iconCache.get("data/images/homeworld.gif");
                homeicon.paintIcon(this, g, x, y);
            }
        }

        try {

            /*
             * Draw Range Circles. This is not so simple as it was with Tasks.
             */

            if (conf.getDisplay()[DISPLAY_RANGES]) {

                // determine which ops the player is eligible for
                TreeSet<String> legalOps = new TreeSet<String>();
                for (CArmy currA : mwclient.getPlayer().getArmies()) {
                    legalOps.addAll(currA.getLegalOperations());
                }

                // loop and draw
                for (String typeName : legalOps) {

                    String[] vals = mwclient.getAllOps().get(typeName);
                    double range = Double.parseDouble(vals[0]);

                    // don't draw stupidly large ranges.
                    if (range < 2000) {
                        Planet p = mp.getPPanel().getPlanet();

                        /*
                         * TODO: Replace the constant visibility checks with a map. See earlier comment.
                         */
                        if (!planetIsVisible(p)) {
                            continue;
                        }

                        int x = map2scrX(p.getPosition().x);
                        int y = map2scrY(p.getPosition().y);

                        Color c = StringUtils.html2Color(vals[1]);

                        g.setColor(c);
                        int rSize = (int) Math.round(2 * range * conf.getScale());
                        g.drawArc(x - rSize / 2, y - rSize / 2, rSize, rSize, 0, 360);
                    }

                }
            }// end if(should display)

        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }

    }

    /**
     * Finds the best factory on a planet
     *
     * @author Torren
     * @param p -
     *            planet to get fac on
     * @return 2 character string of factory icon. i.e. am for assault mek factory.
     */
    /*
     * private String getBestFactory(Planet p){ int type = Integer.MAX_VALUE; int weight = Integer.MIN_VALUE; int tempType = 0; int tempWeight = 0; String id =""; Iterator i = p.getUnitFactories().iterator(); //No factories on this planet. if ( !i.hasNext() ) return ""; while (i.hasNext()) { UnitFactory uf = (UnitFactory)i.next(); tempType = uf.getBestTypeProducable(); tempWeight = uf.getWeightclass(); if ( tempType <= type && tempWeight > weight ) { weight = tempWeight; type = tempType; } //Found an Assault Mek Factory not going to get any better then that! if ( type == Unit.MEK && weight == Unit.ASSAULT) return ("am"); } //no factories redundent check. if ( type == Integer.MAX_VALUE && weight == Integer.MIN_VALUE) return ""; switch ( weight ){ case Unit.LIGHT: id="l";break; case Unit.MEDIUM: id="m";break; case Unit.HEAVY: id="h";break; case Unit.ASSAULT: id="a";break; } switch ( type ){
     * case Unit.MEK: id += "m";break; case Unit.VEHICLE: id += "v";break; case Unit.INFANTRY: id += "i";break; } return id; }
     */

    /**
     * What we NOT want, is to wash out the color tone by adding simple gray to the color. I preferre the code from Color.brighter() which simple looks good. (But it had to be adjusted a bit) Imi
     */
    private int adj(int r) {
        if (conf.getColorAdjustment() == 0) {
            return r;
        }
        if (conf.getColorAdjustment() == 1) {
            return 255;
        }
        int i = (int) (1.0 / conf.getColorAdjustment());
        if (r > 0 && r < i) {
            r = i;
        }
        return Math.min((int) (r / (1 - conf.getColorAdjustment())), 255);
    }

    /**
     * Adjust the color according to the current colorAdjustment...
     */
    public Color adjustColor(Color c) {
        return new Color(adj(c.getRed()), adj(c.getGreen()), adj(c.getBlue()));
    }

    /**
     * Activate a specfic planet
     *
     * @param p
     *            This planet becomes the selected one.
     */
    public void activate(Planet p) {

        if (p == null) {
            return;
        }

        if (mp.getPPanel() != null && mp.getPPanel().getPlanet() != p) {

            mp.getPPanel().update(p);
            conf.setPlanetId(p.getId());
            mp.repaint();

            saveMapSelection(p);
        }
    }

    /**
     * Activate and Center
     */
    public void activate(Planet p, boolean center) {

        if (p == null) {
            return;
        }

        // activate normally
        this.activate(p);

        // then center on the world
        if (center) {
            conf.getOffset().setLocation(-p.getPosition().x * conf.getScale(), p.getPosition().y * conf.getScale());
        }

    }// end activate(p,center)

    public void mouseEntered(MouseEvent e) {
        // mp.requestFocus();
        lastMousePos = new Point(e.getX(), e.getY());
    }

    public void mouseExited(MouseEvent e) {
        lastMousePos = null;
    }

    public void mousePressed(MouseEvent e) {
        mouseMod = e.getButton();
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        selectedPlanet = nearestNeighbour(scr2mapX(e.getX()), scr2mapY(e.getY()));
        activate(selectedPlanet);
    }

    public void mouseReleased(MouseEvent e) {
        mouseMod = 0;
    }

    Point lastMousePos = null;

    int mouseMod = 0;

    private URLClassLoader loader;

    public void mouseDragged(MouseEvent e) {
        if (mouseMod != MouseEvent.BUTTON3) {
            return;
        }
        if (lastMousePos != null) {
            conf.getOffset().x -= lastMousePos.x - e.getX();
            conf.getOffset().y -= lastMousePos.y - e.getY();
        }
        mouseMoved(e);
        mp.repaint();
    }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == 37)// left arrow
        {
            conf.getOffset().y -= conf.getScale();
        } else if (keyCode == 38) // uparrow
        {
            conf.getOffset().x -= conf.getScale();
        } else if (keyCode == 39)// right arrow
        {
            conf.getOffset().y += conf.getScale();
        } else if (keyCode == 40)// down arrow
        {
            conf.getOffset().x += conf.getScale();
        } else {
            return;
        }
        mp.repaint();
    }

    public void mouseMoved(MouseEvent e) {

        if (lastMousePos == null) {
            lastMousePos = new Point(e.getX(), e.getY());
        } else {
            lastMousePos.x = e.getX();
            lastMousePos.y = e.getY();
        }

        if (conf.getDisplay()[DISPLAY_TOOLTIPS]) {

            Planet planet = nearestNeighbour(scr2mapX(e.getX()), scr2mapY(e.getY()));
            StringBuilder result = new StringBuilder("<html><center><b><u>" + planet.getName() + "</b></u></center>");
            result.append("<TABLE CELLPADDING=1 CELLSPACING=1>");

            Iterator<House> it = planet.getInfluence().getHouses().iterator();
            while (it.hasNext()) {
                House h = it.next();
                String color = mwclient.getServerConfigs("DisputedPlanetColor");
                String name = "None";
                int id = -1;

                if (h != null) {
                    color = h.getHouseColor();
                    name = h.getName();
                    id = h.getId();
                }

                result.append("<TR><TD><font color=" + color + ">" + name + "</font></TD><TD>" + Math.floor(100*planet.getInfluence().getInfluence(id)/planet.getConquestPoints()) + "%</TD></TR>");
            }
            result.append("</TABLE></html>");

            setToolTipText(result.toString());
        } else {
            setToolTipText(null);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        mp.getSlider().setValue(mp.getSlider().getValue() + e.getWheelRotation() * 3);
        if (selectedPlanet != null) {
            conf.getOffset().setLocation(-selectedPlanet.getPosition().x * conf.getScale(), selectedPlanet.getPosition().y * conf.getScale());
            mp.repaint();
        }

    }

    /**
     * @param scale
     *            The scale to set.
     */
    public void setScale(double scale) {
        conf.setScale(scale);
        if (selectedPlanet != null) {
            conf.getOffset().setLocation(-selectedPlanet.getPosition().x * conf.getScale(), selectedPlanet.getPosition().y * conf.getScale());
        }
    }

    /**
     * Method to set the selected world. - Called by the HyperLinkListener
     */
    public void setSelectedPlanet(Planet p) {
        selectedPlanet = p;
    }

    /**
     * The event listener for all the display options...
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {

        mp.repaint();

        // save display settings
        for (int i = 0; i < displayStr.length; ++i) {
            conf.getDisplay()[i] = display[i].isSelected();
        }

        // save filter settings
        String filterString = "";
        for (int i = 0; i < filterSettings.length; i++) {

            if (i == FILTER_SEP) {
                filterString += "false$";
                continue;
            }
            filterSettings[i] = filter[i].isSelected();
            filterString += filterSettings[i] + "$";
        }

        mwclient.getConfig().setParam("MAPFILTER", filterString);
        mwclient.getConfig().saveConfig();
    }

    /**
     * At each tick, save the config file... (I just needed a time to do this)
     */
    public void processTick() {
        try {
            new MMNetXStream().toXML(conf, new FileWriter(mwclient.getCacheDir() + "/mapconf.xml"));
        } catch (IOException ioe) {
            LOGGER.error("Exception: ", ioe);
        }
    }

    /**
     * Solves events of data fetches by adding the changes to the current change set.
     */
    public void dataFetched(Map<Integer, Influences> changes) {
        for (int id : changes.keySet()) {
            try {
                Influences addinf = changes.get(id);
                Influences oldinf = changesSinceLastRefresh.get(id);
                if (oldinf != null) {
                    addinf.add(oldinf);
                }
                changesSinceLastRefresh.put(id, addinf);
                if (mp != null) {
                    mp.repaint();
                }
            } catch (Exception ex) {
                LOGGER.error("Error with Planet: " + mwclient.getData().getPlanet(id).getName());
            }
        }
    }

    /**
     * @return Returns the changesSinceLastRefresh.
     */
    public Map<Integer, Influences> getChangesSinceLastRefresh() {
        return changesSinceLastRefresh;
    }

    /*
     * Called from action listeners, or stand alone from a button on a non-map panel.
     */
    public void createPlanetSearchDialog() {
        PlanetSearchDialog searchDialog = new PlanetSearchDialog(this, mwclient);
        searchDialog.setVisible(true);
    }

    /**
     * Method which saves current map properties. Called when a new planet is selected. The settings are restored when a client is loaded, preserving map selections between user sessions.
     */
    public void saveMapSelection(Planet p) {
        // save the config
        mwclient.getConfig().setParam("SELECTEDPLANET", p.getName());
        mwclient.getConfig().setParam("MAPZOOMLEVEL", "" + conf.getScale());
        mwclient.getConfig().setParam("MAPYOFFSET", "" + (int) conf.getOffset().getY());
        mwclient.getConfig().setParam("MAPXOFFSET", "" + (int) conf.getOffset().getX());

        mwclient.getConfig().saveConfig();
        mwclient.setConfig();
    }

    /**
     * Utility method which checks the visibility of a given planet.
     *
     * @param p
     * @return
     */
    private boolean planetIsVisible(Planet planet) {
        if (null == planet) {
            return false;
        }
        
        // first, make sure its not "All"
        if (filterSettings[FILTER_ALL]) {
            return true;
        }

        /*
         * Not showing all, so do check all relevant server options and determine whether this particular world should be visible @ this time.
         */
        if (filterSettings[FILTER_FACTORIES] && planet.getFactoryCount() > 0) {
            return true;
        }

        if (filterSettings[FILTER_FACILITIES] && planet.getBaysProvided() > 0) {
            return true;
        }

        if (filterSettings[FILTER_DISPUTED]) {

            Integer houseID = planet.getInfluence().getOwner();

            // no owner means disputed
            if (houseID == null) {
                return true;
            }

            // there's a high ID, but it doesn't own enough of the world to be undisputed
            if (planet.getInfluence().getInfluence(houseID) < mwclient.getMinPlanetOwnerShip(planet)) {
                return true;
            }
        }

        if (filterSettings[FILTER_CONTESTED] && planet.getInfluence().getHouses().size() > 1) {
            return true;
        }

        if (filterSettings[FILTER_FACTION] && planet.getInfluence().getInfluence(mwclient.getPlayer().getMyHouse().getId()) > 0) {
            return true;
        }
        // no qualifiers. we shouldn't see the world.
        return false;
    }

    public InnerStellarMapConfig getConf() {
        return conf;
    }

    public void setConf(InnerStellarMapConfig conf) {
        this.conf = conf;
    }
}
