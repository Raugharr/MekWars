package mekwars.client.gui;

import java.awt.*;

/**
 * All configuration behaviour of InterStellarMap are saved here.
 *
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class InnerStellarMapConfig {
    /**
     * Whether to scale planet dots on zoom or not
     */
    private int minDotSize = 2;
    private int maxDotSize = 25;
    /**
     * The scaling maximum dimension
     */
    private int reverseScaleMax = 100;
    /**
     * The scaling minimum dimension
     */
    private int reverseScaleMin = 2;
    /**
     * Threshold to not show influence anymore. 0 means show always
     */
    private double showInfluenceThreshold = 0.0;
    /**
     * Threshold to not show unit factories anymore. 0 means show always
     */
    private double showUnitFactoriesThreshold = 0.0;
    /**
     * Threshold to not show planet names. 0 means show always
     */
    private double showPlanetNamesThreshold = 0.0;
    /**
     * brightness correction for colors. This is no gamma correction! Gamma correction brightens medium level colors more than extreme ones. 0 means no brightening.
     */
    private double colorAdjustment = 0.5;
    /**
     * The maps background color
     */
    private String backgroundColor = "#000000";

    /**
     * Various display options - Names, Control, Factories, Warehouses, Ranges, Changes
     */
    private boolean[] display = new boolean[] { true, false, true, true, true, true, true, true };

    /**
     * The actual scale factor. 1.0 for default, higher means bigger.
     */
    private double scale = 1.0;
    /**
     * The scrolling offset
     */
    private Point offset = new Point();
    /**
     * The current selected Planet-id
     */
    private int planetId;

    public int getMinDotSize() {
        return minDotSize;
    }

    public void setMinDotSize(int minDotSize) {
        this.minDotSize = minDotSize;
    }

    public int getMaxDotSize() {
        return maxDotSize;
    }

    public void setMaxDotSize(int maxDotSize) {
        this.maxDotSize = maxDotSize;
    }

    public int getReverseScaleMax() {
        return reverseScaleMax;
    }

    public void setReverseScaleMax(int reverseScaleMax) {
        this.reverseScaleMax = reverseScaleMax;
    }

    public int getReverseScaleMin() {
        return reverseScaleMin;
    }

    public void setReverseScaleMin(int reverseScaleMin) {
        this.reverseScaleMin = reverseScaleMin;
    }

    public double getShowInfluenceThreshold() {
        return showInfluenceThreshold;
    }

    public void setShowInfluenceThreshold(double showInfluenceThreshold) {
        this.showInfluenceThreshold = showInfluenceThreshold;
    }

    public double getShowUnitFactoriesThreshold() {
        return showUnitFactoriesThreshold;
    }

    public void setShowUnitFactoriesThreshold(double showUnitFactoriesThreshold) {
        this.showUnitFactoriesThreshold = showUnitFactoriesThreshold;
    }

    public double getShowPlanetNamesThreshold() {
        return showPlanetNamesThreshold;
    }

    public void setShowPlanetNamesThreshold(double showPlanetNamesThreshold) {
        this.showPlanetNamesThreshold = showPlanetNamesThreshold;
    }

    public double getColorAdjustment() {
        return colorAdjustment;
    }

    public void setColorAdjustment(double colorAdjustment) {
        this.colorAdjustment = colorAdjustment;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean[] getDisplay() {
        return display;
    }

    public void setDisplay(boolean[] display) {
        this.display = display;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Point getOffset() {
        return offset;
    }

    public void setOffset(Point offset) {
        this.offset = offset;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }
}
