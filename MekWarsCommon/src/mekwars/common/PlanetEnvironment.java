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

package common;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import common.util.BinReader;
import common.util.BinWriter;


/**
 * A Planet's Environment.
 * 
 * Final simple because you should be aware to overwrite binIn and binOut properly 
 * if you subclass PlanetEnvironment.
 */

final public class PlanetEnvironment{
    // id
    private int id = -1;
    private String name = "";
    
    //Crater
    private int craterProbability = 0;
    private int craterMinimum = 0;
    private int craterMaximum = 0;
    private int craterMinRadius = 0;
    private int craterMaxRadius = 0;
    
    //Hills
    private int hillyness = 100;
    private int hillElevationRange = 3;
    private int hillInvertProbability = 0;
    
    //Water
    private int waterMinSpots = 3;
    private int waterMaxSpots = 8;
    private int waterMinHexes = 2;
    private int waterMaxHexes = 10;
    private int waterDeepProbability = 20;
    
    //Forest
    private int forestMinSpots = 4;
    private int forestMaxSpots = 8;
    private int forestMinHexes = 2;
    private int forestMaxHexes = 6;
    private int forestHeavyProbability = 20;
    
    //Rough
    private int roughMinSpots = 0;
    private int roughMaxSpots = 5;
    private int roughMinHexes = 1;
    private int roughMaxHexes = 2;
    
    //Swamp
    private int swampMinSpots = 0;
    private int swampMaxSpots = 0;
    private int swampMinHexes = 0;
    private int swampMaxHexes = 0;
    
    //Pavement
    private int pavementMinSpots = 0;
    private int pavementMaxSpots = 0;
    private int pavementMinHexes = 0;
    private int pavementMaxHexes = 0;
    
    //Ice
    private int iceMinSpots = 0;
    private int iceMaxSpots = 0;
    private int iceMinHexes = 0;
    private int iceMaxHexes = 0;

    //Rubble
    private int rubbleMinSpots = 0;
    private int rubbleMaxSpots = 0;
    private int rubbleMinHexes = 0;
    private int rubbleMaxHexes = 0;

    //Fortified
    private int fortifiedMinSpots = 0;
    private int fortifiedMaxSpots = 0;
    private int fortifiedMinHexes = 0;
    private int fortifiedMaxHexes = 0;

    //Sand 
    private int sandMinSpots = 0;
    private int sandMaxSpots = 0;
    private int sandMinHexes = 0;
    private int sandMaxHexes = 0;
    
    //Planted Field 
    private int plantedFieldMinSpots = 0;
    private int plantedFieldMaxSpots = 0;
    private int plantedFieldMinHexes = 0;
    private int plantedFieldMaxHexes = 0;
    
    //Buildings
    private int minBuildings = 0;
    private int maxBuildings = 0;
    private int minCF = 0;
    private int maxCF = 0;
    private int minFloors = 0;
    private int maxFloors = 0;
    private int cityDensity = 50;
    private String cityType = "NONE";
    private int roads = 4;
    private int townSize = 0;
    
    //Special Effects
    private int fxMod = 0;
    private int forestFireProbability = 0;
    private int freezeProbability = 0;
    private int floodProbability = 0;
    private int droughtProbability = 0;
    private String theme = "";
    
    //Mountains
    private int mountPeaks = 0;
    private int mountWidthMin = 0;
    private int mountWidthMax = 0;
    private int mountHeightMin = 0;
    private int mountHeightMax = 0; 
    private int mountStyle = 0; 
    
    //Misc
    private int roadProbability = 25;
    private int riverProbability = 25;
    private int algorithm = 0;
    private int cliffProbability = 0;
    private int invertNegativeTerrain = 0;
    private int environmentProbability = 1;
 
    //static maps support
    private String staticMapName = "surprise";
    private int xSize = -1;
    private int ySize = -1;

    private boolean staticMap = false;
    private int xBoardSize = -1;
    private int yBoardSize = -1;

    /**
     * For Serialisation.
     */
    public PlanetEnvironment() {
    }

    public PlanetEnvironment(String s) {
        StringTokenizer ST = new StringTokenizer(s,"$");
        //Read the PE$;
        ST.nextToken();
        //Read the Data
        
        name = ST.nextToken();
        craterProbability = Integer.parseInt(ST.nextToken());
        craterMinimum = Integer.parseInt(ST.nextToken());
        craterMaximum = Integer.parseInt(ST.nextToken());
        craterMinRadius = Integer.parseInt(ST.nextToken());
        craterMaxRadius = Integer.parseInt(ST.nextToken());
        hillyness = Integer.parseInt(ST.nextToken());
        hillElevationRange = Integer.parseInt(ST.nextToken());
        hillInvertProbability = Integer.parseInt(ST.nextToken());
        waterMinSpots = Integer.parseInt(ST.nextToken());
        waterMaxSpots = Integer.parseInt(ST.nextToken());
        waterMinHexes = Integer.parseInt(ST.nextToken());
        waterMaxHexes = Integer.parseInt(ST.nextToken());
        waterDeepProbability = Integer.parseInt(ST.nextToken());
        forestMinSpots = Integer.parseInt(ST.nextToken());
        forestMaxSpots = Integer.parseInt(ST.nextToken());
        forestMinHexes = Integer.parseInt(ST.nextToken());
        forestMaxHexes = Integer.parseInt(ST.nextToken());
        forestHeavyProbability = Integer.parseInt(ST.nextToken());
        roughMinSpots = Integer.parseInt(ST.nextToken());
        roughMaxSpots = Integer.parseInt(ST.nextToken());
        roughMinHexes = Integer.parseInt(ST.nextToken());
        roughMaxHexes = Integer.parseInt(ST.nextToken());
        roadProbability = Integer.parseInt(ST.nextToken());
        riverProbability = Integer.parseInt(ST.nextToken());
        algorithm = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            id = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fxMod = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            forestFireProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            freezeProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            floodProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            droughtProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            theme = ST.nextToken();
        if (ST.hasMoreTokens())
            iceMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            rubbleMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            fortifiedMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            minBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            minCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            minFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            cityDensity = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            cityType = ST.nextToken();
        if (ST.hasMoreTokens())
            roads = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements())
            cliffProbability = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements())
            invertNegativeTerrain = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            townSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountPeaks = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountWidthMin = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountWidthMax = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountHeightMin = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountHeightMax = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountStyle = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            environmentProbability = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            setStaticMap(Boolean.parseBoolean(ST.nextToken()));
        if ( ST.hasMoreElements() )
            staticMapName = ST.nextToken();        
        if ( ST.hasMoreElements() )
            xSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            ySize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            xBoardSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            yBoardSize = Integer.parseInt(ST.nextToken());

        if ( ST.hasMoreElements() )
            sandMinSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMaxSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMinHexes = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMaxHexes = Integer.parseInt(ST.nextToken());
        
        if ( ST.hasMoreElements() )
            plantedFieldMinSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMaxSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMinHexes = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMaxHexes = Integer.parseInt(ST.nextToken());
    }

    public PlanetEnvironment(StringTokenizer ST) {
        //Read the PE$;
        ST.nextToken();
        //Read the Data
        
        name = ST.nextToken();
        craterProbability = Integer.parseInt(ST.nextToken());
        craterMinimum = Integer.parseInt(ST.nextToken());
        craterMaximum = Integer.parseInt(ST.nextToken());
        craterMinRadius = Integer.parseInt(ST.nextToken());
        craterMaxRadius = Integer.parseInt(ST.nextToken());
        hillyness = Integer.parseInt(ST.nextToken());
        hillElevationRange = Integer.parseInt(ST.nextToken());
        hillInvertProbability = Integer.parseInt(ST.nextToken());
        waterMinSpots = Integer.parseInt(ST.nextToken());
        waterMaxSpots = Integer.parseInt(ST.nextToken());
        waterMinHexes = Integer.parseInt(ST.nextToken());
        waterMaxHexes = Integer.parseInt(ST.nextToken());
        waterDeepProbability = Integer.parseInt(ST.nextToken());
        forestMinSpots = Integer.parseInt(ST.nextToken());
        forestMaxSpots = Integer.parseInt(ST.nextToken());
        forestMinHexes = Integer.parseInt(ST.nextToken());
        forestMaxHexes = Integer.parseInt(ST.nextToken());
        forestHeavyProbability = Integer.parseInt(ST.nextToken());
        roughMinSpots = Integer.parseInt(ST.nextToken());
        roughMaxSpots = Integer.parseInt(ST.nextToken());
        roughMinHexes = Integer.parseInt(ST.nextToken());
        roughMaxHexes = Integer.parseInt(ST.nextToken());
        roadProbability = Integer.parseInt(ST.nextToken());
        riverProbability = Integer.parseInt(ST.nextToken());
        algorithm = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            id = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            swampMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            pavementMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fxMod = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            forestFireProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            freezeProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            floodProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            droughtProbability = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            theme = ST.nextToken();
        if (ST.hasMoreTokens())
            iceMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            iceMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            rubbleMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            rubbleMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            fortifiedMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            fortifiedMaxHexes = Integer.parseInt(ST.nextToken());

        if (ST.hasMoreTokens())
            minBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            minCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            minFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            maxFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            cityDensity = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens())
            cityType = ST.nextToken();
        if (ST.hasMoreTokens())
            roads = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements())
            cliffProbability = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements())
            invertNegativeTerrain = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            townSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountPeaks = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountWidthMin = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountWidthMax = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountHeightMin = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountHeightMax = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            mountStyle = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            environmentProbability = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            setStaticMap(Boolean.parseBoolean(ST.nextToken()));
        if ( ST.hasMoreElements() )
            staticMapName = ST.nextToken();        
        if ( ST.hasMoreElements() )
            xSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            ySize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            xBoardSize = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            yBoardSize = Integer.parseInt(ST.nextToken());

        if ( ST.hasMoreElements() )
            sandMinSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMaxSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMinHexes = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            sandMaxHexes = Integer.parseInt(ST.nextToken());
        
        if ( ST.hasMoreElements() )
            plantedFieldMinSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMaxSpots = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMinHexes = Integer.parseInt(ST.nextToken());
        if ( ST.hasMoreElements() )
            plantedFieldMaxHexes = Integer.parseInt(ST.nextToken());
    }
    

    public String toDescription() {
        String result="";
        
        int water=(((waterMaxSpots+waterMinSpots)/2)*
                (waterMinHexes+waterMaxSpots)/2)+riverProbability/10;
        int rough=(((roughMaxSpots+roughMinSpots)/2)*
                (roughMinHexes+roughMaxSpots)/2);
        int forest=(((forestMaxSpots+forestMinSpots)/2)*
                (forestMinHexes+forestMaxSpots)/2);
        /*int swamp=(((swampMaxSpots+swampMinSpots)/2)*
                (swampMinHexes+swampMaxSpots)/2);
        int pavement=(((pavementMaxSpots+pavementMinSpots)/2)*
                (pavementMinHexes+pavementMaxSpots)/2);
        */
        /* generate the hilliness/crater description */
        result+="The landscape is ";
        if (hillyness < 200)
            result+="plain";
        if ((hillyness < 500) && (hillyness >=200))
            result+="uneven";
        if ((hillyness >= 500) && (hillyness <= 800))
            result+="hilly";
        if (hillyness > 800)
            result+="mountainous";
        if (craterProbability==0) {
            result+=". <br> ";
            if (rough>0) {
                result+="Through tectonic activity of this continent, rough terrain is appearing";
                if (rough > 8)
                    result+=" everywhere";
                else
                    result+=" sometimes";
            }
        }
        else {
            if (craterProbability<30)
                result+=", which is seldom coverd with";
            if ((craterProbability>=30) && (craterProbability<60))
                result+=", which is covered with";
            if (craterProbability>=60)
                result+=", often coverd with";
            int avgCraterSize=(craterMinRadius+craterMaxRadius)/2;
            if (avgCraterSize < 4)
                result+=" small craters";
            if ((avgCraterSize >=4) && (avgCraterSize<7))
                result+=" craters ";
            if (avgCraterSize>=7)
                result+=" large craters";
            if (rough>0) {
                result+=". Another remaing of the ancient meteorid impacts is the rough terrain appearing";
                if (rough > 8)
                    result+=" everywhere";
                else
                    result+= " sometimes";
            }
        } // craters
        result+=". <br>";
        
        /* woods */
        result+="Most facitlities on this continent are lying";
        if (forest>50) {
            result+=" deep in the ";
            result+=(forestHeavyProbability < 30)? "woods": "jungle";
            if (water > 20)
                result+=" mixed up with much water, because of heavy rain due too monsoon period";
            result+=".";
        } /* jungle */
        else {
            if (water > 20)
                result+=" close to the coast.";
            else
                if (water < 3) {
                    if (forest < 15)
                        result+=" in the desert. So dont expect vegetation for cover or water for cooling.";
                    else
                        result+=" in an area moderatly forested.";
                }
                else
                    result+=" in an area famous for its agriculture.";
        } /* else */
        return result;
    } /* to Description */
    

    public String toImageDescription()
    {
        String result ="";
        int water=(((waterMaxSpots+waterMinSpots)/2)*
                (waterMinHexes+waterMaxSpots)/2)+riverProbability/10;
        int rough=(((roughMaxSpots+roughMinSpots)/2)*
                (roughMinHexes+roughMaxSpots)/2);
        int forest=(((forestMaxSpots+forestMinSpots)/2)*
                (forestMinHexes+forestMaxSpots)/2);
        /*int swamp=(((swampMaxSpots+swampMinSpots)/2)*
                (swampMinHexes+swampMaxSpots)/2);
        int pavement=(((pavementMaxSpots+pavementMinSpots)/2)*
                (pavementMinHexes+pavementMaxSpots)/2);
        */
        /* generate the hilliness/crater description */
        if (hillyness < 200)
            result+="<img src=\"data/images/hill0.gif\">";
        if ((hillyness < 500) && (hillyness >=200))
            result+="<img src=\"data/images/hill1.gif\">";
        if ((hillyness >= 500) && (hillyness <= 800))
            result+="<img src=\"data/images/hill2.gif\">";
        if (hillyness > 800)
            result+="<img src=\"data/images/hill3.gif\">";
        if (rough > 8)
            result+="<img src=\"data/images/roug1.gif\">";
        if (craterProbability>30)
            result+="<img src=\"data/images/crtr1.gif\">";
        
        /* woods */
        if (forest>15 && forest < 30) 
            result+="<img src=\"data/images/wood1.gif\">";
        else if (forest>=30 && forest <50)
            result+="<img src=\"data/images/wood2.gif\">";
        else if (forest >=50)
            result+="<img src=\"data/images/wood3.gif\">";
        
        /*water */
        if (water >5 && water < 20)
            result+="<img src=\"data/images/watr1.gif\">";
        else if (water >= 20)
            result+="<img src=\"data/images/watr2.gif\">";
        if (getRiverProbability() > 50)
            result+="<img src=\"data/images/rivr1.gif\">";
        if (getRoadProbability() > 50)
            result+="<img src=\"data/images/road1.gif\">";
        
        return result;
        
    }

    /**
     * TODO: remove this code bloat - make a better way to get the images.
     */
    public String toImageAbsolutePathDescription()
    {
        String result ="";
        int water=(((waterMaxSpots+waterMinSpots)/2)*
                (waterMinHexes+waterMaxSpots)/2)+riverProbability/10;
        int rough=(((roughMaxSpots+roughMinSpots)/2)*
                (roughMinHexes+roughMaxSpots)/2);
        int forest=(((forestMaxSpots+forestMinSpots)/2)*
                (forestMinHexes+forestMaxSpots)/2);
        /*int swamp=(((swampMaxSpots+swampMinSpots)/2)*
                (swampMinHexes+swampMaxSpots)/2);
        int pavement=(((pavementMaxSpots+pavementMinSpots)/2)*
                (pavementMinHexes+pavementMaxSpots)/2);
          */      
        /* generate the hilliness/crater description */
        String path = "file:///"+new File(".").getAbsolutePath();
        
        if (hillElevationRange  < 2)
            result+="<img src=\""+path+"/data/images/hill0.gif\">";
            //result+="<img src=\"file:///"+path+"/data/images/hill0.gif\">";
        if ((hillElevationRange  < 5) && (hillElevationRange   >=2))
            result+="<img src=\""+path+"/data/images/hill1.gif\">";
        if ((hillElevationRange   >= 5) && (hillElevationRange  <= 8))
            result+="<img src=\""+path+"/data/images/hill2.gif\">";
        if (hillElevationRange  > 8)
            result+="<img src=\""+path+"/data/images/hill3.gif\">";
        if (rough > 8)
            result+="<img src=\""+path+"/data/images/roug1.gif\">";
        if (craterProbability>30)
            result+="<img src=\""+path+"/data/images/crtr1.gif\">";

        /* woods */
        if (forest>15 && forest < 30) 
            result+="<img src=\""+path+"/data/images/wood1.gif\">";
        else if (forest>=30 && forest <50)
            result+="<img src=\""+path+"/data/images/wood2.gif\">";
        else if (forest >=50)
            result+="<img src=\""+path+"/data/images/wood3.gif\">";

        /*water */
        if (water > 5 && water < 20)
            result += "<img src=\"" + path + "/data/images/watr1.gif\">";
        else if (water >= 20)
            result += "<img src=\"" + path + "/data/images/watr2.gif\">";
        if (getRiverProbability() > 50)
            result += "<img src=\"" + path + "/data/images/rivr1.gif\">";
        if (getRoadProbability() > 50)
            result +="<img src=\"" + path + "/data/images/road1.gif\">";
        
        return result;
    }

    public String toString() {
        String result = "PE$";
        result += name + "$";
        result += craterProbability + "$";
        result += craterMinimum + "$";
        result += craterMaximum + "$";
        result += craterMinRadius + "$";
        result += craterMaxRadius + "$";
        result += hillyness + "$";
        result += hillElevationRange + "$";
        result += hillInvertProbability + "$";
        result += waterMinSpots + "$";
        result += waterMaxSpots + "$";
        result += waterMinHexes + "$";
        result += waterMaxHexes + "$";
        result += waterDeepProbability + "$";
        result += forestMinSpots + "$";
        result += forestMaxSpots + "$";
        result += forestMinHexes + "$";
        result += forestMaxHexes + "$";
        result += forestHeavyProbability + "$";
        result += roughMinSpots + "$";
        result += roughMaxSpots + "$";
        result += roughMinHexes + "$";
        result += roughMaxHexes + "$";
        result += roadProbability + "$";
        result += riverProbability + "$";
        result += algorithm + "$";
        result += id + "$";
        result += swampMinSpots + "$";
        result += swampMaxSpots + "$";
        result += swampMinHexes + "$";
        result += swampMaxHexes + "$";
        result += pavementMinSpots + "$";
        result += pavementMaxSpots + "$";
        result += pavementMinHexes + "$";
        result += pavementMaxHexes + "$";
        result += fxMod + "$";
        result += forestFireProbability + "$";
        result += freezeProbability + "$";
        result += floodProbability + "$";
        result += droughtProbability + "$";
        result += theme + "$";
        result += iceMinSpots + "$";
        result += iceMaxSpots + "$";
        result += iceMinHexes + "$";
        result += iceMaxHexes + "$";

        result += rubbleMinSpots + "$";
        result += rubbleMaxSpots + "$";
        result += rubbleMinHexes + "$";
        result += rubbleMaxHexes + "$";

        result += fortifiedMinSpots + "$";
        result += fortifiedMaxSpots + "$";
        result += fortifiedMinHexes + "$";
        result += fortifiedMaxHexes + "$";
        
        result += minBuildings + "$";
        result += maxBuildings + "$";
        result += minCF + "$";
        result += maxCF + "$";
        result += minFloors + "$";
        result += maxFloors + "$";
        result += cityDensity + "$";
        result += cityType + "$";
        result += roads + "$";
        result += cliffProbability + "$";
        result += invertNegativeTerrain + "$";
        result += townSize + "$";
        result += mountPeaks + "$";
        result += mountWidthMin + "$";
        result += mountWidthMax + "$";
        result += mountHeightMin + "$";
        result += mountHeightMax + "$";
        result += mountStyle + "$";
        result += environmentProbability + "$";
        result += staticMap;
        result += "$";
        result += staticMapName;
        result += "$";
        result += xSize;
        result += "$";
        result += ySize;
        result += "$";
        result += xBoardSize;
        result += "$";
        result += yBoardSize;
        result += "$";

        result += sandMinSpots + "$";
        result += sandMaxSpots + "$";
        result += sandMinHexes + "$";
        result += sandMaxHexes + "$";


        result += plantedFieldMinSpots + "$";
        result += plantedFieldMaxSpots + "$";
        result += plantedFieldMinHexes + "$";
        result += plantedFieldMaxHexes + "$";


        return result;
    }

    public String toString(String city)
    {
        
        //no city info then use the normal one.
        if ( city.trim().length() <= 1)
            return this.toString();
        
        //else 
        String result = "PE$";
        result += name + "$";
        result += craterProbability + "$";
        result += craterMinimum + "$";
        result += craterMaximum + "$";
        result += craterMinRadius + "$";
        result += craterMaxRadius + "$";
        result += hillyness + "$";
        result += hillElevationRange + "$";
        result += hillInvertProbability + "$";
        result += waterMinSpots + "$";
        result += waterMaxSpots + "$";
        result += waterMinHexes + "$";
        result += waterMaxHexes + "$";
        result += waterDeepProbability + "$";
        result += forestMinSpots + "$";
        result += forestMaxSpots + "$";
        result += forestMinHexes + "$";
        result += forestMaxHexes + "$";
        result += forestHeavyProbability + "$";
        result += roughMinSpots + "$";
        result += roughMaxSpots + "$";
        result += roughMinHexes + "$";
        result += roughMaxHexes + "$";
        result += roadProbability + "$";
        result += riverProbability + "$";
        result += algorithm + "$";
        result += id + "$";
        result += swampMinSpots + "$";
        result += swampMaxSpots + "$";
        result += swampMinHexes + "$";
        result += swampMaxHexes + "$";
        result += pavementMinSpots + "$";
        result += pavementMaxSpots + "$";
        result += pavementMinHexes + "$";
        result += pavementMaxHexes + "$";
        result += fxMod + "$";
        result += forestFireProbability + "$";
        result += freezeProbability + "$";
        result += floodProbability + "$";
        result += droughtProbability + "$";
        result += theme + "$";
        result += iceMinSpots + "$";
        result += iceMaxSpots + "$";
        result += iceMinHexes + "$";
        result += iceMaxHexes + "$";

        result += rubbleMinSpots + "$";
        result += rubbleMaxSpots + "$";
        result += rubbleMinHexes + "$";
        result += rubbleMaxHexes + "$";

        result += fortifiedMinSpots + "$";
        result += fortifiedMaxSpots + "$";
        result += fortifiedMinHexes + "$";
        result += fortifiedMaxHexes + "$";
        
        result += city + "$";
        
        result += cliffProbability + "$";
        result += invertNegativeTerrain + "$";
        result += townSize + "$";
        result += mountPeaks + "$";
        result += mountWidthMin + "$";
        result += mountWidthMax + "$";
        result += mountHeightMin + "$";
        result += mountHeightMax + "$";
        result += mountStyle + "$";
        result += environmentProbability + "$";
        result += staticMap;
        result += "$";
        result += staticMapName;
        result += "$";
        result += xSize;
        result += "$";
        result += ySize;
        result += "$";
        result += xBoardSize;
        result += "$";
        result += yBoardSize;
        result += "$";

        result += sandMinSpots + "$";
        result += sandMaxSpots + "$";
        result += sandMinHexes + "$";
        result += sandMaxHexes + "$";


        result += plantedFieldMinSpots + "$";
        result += plantedFieldMaxSpots + "$";
        result += plantedFieldMinHexes + "$";
        result += plantedFieldMaxHexes + "$";
        
        return result;
    }
    //Getter and Setter
    public int getWaterMinSpots() {
        return waterMinSpots;
    }

    public int getWaterMinHexes() {
        return waterMinHexes;
    }

    public int getWaterMaxHexes() {
        return waterMaxHexes;
    }

    public int getWaterMaxSpots() {
        return waterMaxSpots;
    }

    public int getWaterDeepProbability() {
        return waterDeepProbability;
    }

    public int getRoughMinSpots() {
        return roughMinSpots;
    }

    public int getRoughMinHexes() {
        return roughMinHexes;
    }

    public int getRoughMaxSpots() {
        return roughMaxSpots;
    }

    public int getRoughMaxHexes() {
        return roughMaxHexes;
    }

    public int getSwampMinSpots() {
        return swampMinSpots;
    }

    public int getSwampMinHexes() {
        return swampMinHexes;
    }

    public int getSwampMaxSpots() {
        return swampMaxSpots;
    }

    public int getSwampMaxHexes() {
        return swampMaxHexes;
    }

    public int getPavementMinSpots() {
        return pavementMinSpots;
    }

    public int getPavementMinHexes() {
        return pavementMinHexes;
    }

    public int getPavementMaxSpots() {
        return pavementMaxSpots;
    }

    public int getPavementMaxHexes() {
        return pavementMaxHexes;
    }

    public int getIceMinSpots() {
        return iceMinSpots;
    }

    public int getIceMinHexes() {
        return iceMinHexes;
    }

    public int getIceMaxSpots() {
        return iceMaxSpots;
    }

    public int getIceMaxHexes() {
        return iceMaxHexes;
    }

    public int getRubbleMinSpots() {
        return rubbleMinSpots;
    }

    public int getRubbleMinHexes() {
        return rubbleMinHexes;
    }

    public int getRubbleMaxSpots() {
        return rubbleMaxSpots;
    }

    public int getRubbleMaxHexes() {
        return rubbleMaxHexes;
    }

    public int getFortifiedMinSpots() {
        return fortifiedMinSpots;
    }

    public int getFortifiedMinHexes() {
        return fortifiedMinHexes;
    }

    public int getFortifiedMaxSpots() {
        return fortifiedMaxSpots;
    }

    public int getFortifiedMaxHexes() {
        return fortifiedMaxHexes;
    }

    public int getMaxBuildings() {
        return maxBuildings;
    }

    public int getMinBuildings() {
        return minBuildings;
    }

    public int getMaxCF() {
        return maxCF;
    }

    public int getMinCF() {
        return minCF;
    }

    public int getMaxFloors() {
        return maxFloors;
    }

    public int getMinFloors() {
        return minFloors;
    }

    public int getCityDensity() {
        return cityDensity;
    }

    public int getRoads() {
        return roads;
    }

    public String getCityType() {
        return cityType;
    }


    public int getRoadProbability() {
        return roadProbability;
    }

    public int getRiverProbability() {
        return riverProbability;
    }

    public int getHillyness() {
        return hillyness;
    }

    public int getForestMinSpots() {
        return forestMinSpots;
    }

    public int getHillElevationRange() {
        return hillElevationRange;
    }

    public int getForestMinHexes() {
        return forestMinHexes;
    }

    public int getForestMaxSpots() {
        return forestMaxSpots;
    }

    public int getForestMaxHexes() {
        return forestMaxHexes;
    }

    public int getForestHeavyProbability() {
        return forestHeavyProbability;
    }

    public int getCraterProbability() {
        return craterProbability;
    }

    public int getCraterMinRadius() {
        return craterMinRadius;
    }

    public int getCraterMaxRadius() {
        return craterMaxRadius;
    }

    public int getCraterMinNum() {
        return craterMinimum;
    }

    public int getCraterMaxNum() {
        return craterMaximum;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public int getCliffProbability() {
        return cliffProbability;
    }

    public int getInvertNegativeTerrain() {
        return invertNegativeTerrain;
    }

    public int getTownSize() {
        return townSize;
    }

    public int getMountPeaks() {
        return mountPeaks;
    }

    public int getMountWidthMin() {
        return mountWidthMin;
    }

    public int getMountWidthMax() {
        return mountWidthMax;
    }

    public int getMountHeightMin() {
        return mountHeightMin;
    }

    public int getMountHeightMax() {
        return mountHeightMax;
    }

    public int getMountStyle() {
        return mountStyle;
    }

    public int getEnvironmentalProbability() {
        return environmentProbability;
    }

    public void setEnvironmentalProbability(int prob) {
        environmentProbability = prob;
    }

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    public void setCraterMaxNum(int craterMaximum) {
        this.craterMaximum = craterMaximum;
    }

    public void setCraterMaxRadius(int craterMaxRadius) {
        this.craterMaxRadius = craterMaxRadius;
    }

    public void setCraterMinNum(int craterMinimum) {
        this.craterMinimum = craterMinimum;
    }

    public void setCraterMinRadius(int craterMinRadius) {
        this.craterMinRadius = craterMinRadius;
    }

    public void setCraterProbability(int craterProbability) {
        this.craterProbability = craterProbability;
    }

    public void setForestHeavyProbability(int forestHeavyProbability) {
        this.forestHeavyProbability = forestHeavyProbability;
    }

    public void setForestMaxHexes(int forestMaxHexes) {
        this.forestMaxHexes = forestMaxHexes;
    }

    public void setForestMaxSpots(int forestMaxSpots) {
        this.forestMaxSpots = forestMaxSpots;
    }

    public void setForestMinHexes(int forestMinHexes) {
        this.forestMinHexes = forestMinHexes;
    }

    public void setForestMinSpots(int forestMinSpots) {
        this.forestMinSpots = forestMinSpots;
    }

    public void setHillElevationRange(int hillElevationRange) {
        this.hillElevationRange = hillElevationRange;
    }

    public void setHillyness(int hillyness) {
        this.hillyness = hillyness;
    }

    public void setRoadProbability(int roadProbability) {
        this.roadProbability = roadProbability;
    }

    public void setRiverProbability(int riverProbability) {
        this.riverProbability = riverProbability;
    }

    public void setRoughMaxHexes(int roughMaxHexes) {
        this.roughMaxHexes = roughMaxHexes;
    }

    public void setRoughMaxSpots(int roughMaxSpots) {
        this.roughMaxSpots = roughMaxSpots;
    }

    public void setRoughMinSpots(int roughMinSpots) {
        this.roughMinSpots = roughMinSpots;
    }

    public void setRoughMinHexes(int roughMinHexes) {
        this.roughMinHexes = roughMinHexes;
    }

    public void setWaterDeepProbability(int waterDeepProbability) {
        this.waterDeepProbability = waterDeepProbability;
    }

    public void setWaterMaxHexes(int waterMaxHexes) {
        this.waterMaxHexes = waterMaxHexes;
    }

    public void setWaterMaxSpots(int waterMaxSpots) {
        this.waterMaxSpots = waterMaxSpots;
    }

    public void setWaterMinHexes(int waterMinHexes) {
        this.waterMinHexes = waterMinHexes;
    }

    public void setWaterMinSpots(int waterMinSpots) {
        this.waterMinSpots = waterMinSpots;
    }

    public int getHillInvertProbability() {
        return hillInvertProbability;
    }

    public void setHillInvertProbability(int hillInvertProbability) {
        this.hillInvertProbability = hillInvertProbability;
    }

    public void setSwampMaxHexes(int swampMaxHexes) {
        this.swampMaxHexes = swampMaxHexes;
    }

    public void setSwampMaxSpots(int swampMaxSpots) {
        this.swampMaxSpots = swampMaxSpots;
    }

    public void setSwampMinSpots(int swampMinSpots) {
        this.swampMinSpots = swampMinSpots;
    }

    public void setSwampMinHexes(int swampMinHexes) {
        this.swampMinHexes = swampMinHexes;
    }

    public void setPavementMaxHexes(int pavementMaxHexes) {
        this.pavementMaxHexes = pavementMaxHexes;
    } 

    public void setPavementMaxSpots(int pavementMaxSpots) {
        this.pavementMaxSpots = pavementMaxSpots;
    }

    public void setPavementMinSpots(int pavementMinSpots) {
        this.pavementMinSpots = pavementMinSpots;
    }

    public void setPavementMinHexes(int pavementMinHexes) {
        this.pavementMinHexes = pavementMinHexes;
    }
    
    public void setIceMaxHexes(int iceMaxHexes) {
        this.iceMaxHexes = iceMaxHexes;
    }

    public void setIceMaxSpots(int iceMaxSpots) {
        this.iceMaxSpots = iceMaxSpots;
    }

    public void setIceMinSpots(int iceMinSpots) {
        this.iceMinSpots = iceMinSpots;
    }

    public void setIceMinHexes(int iceMinHexes) {
        this.iceMinHexes = iceMinHexes;
    }
    
    public void setRubbleMaxHexes(int rubbleMaxHexes) {
        this.rubbleMaxHexes = rubbleMaxHexes;
    } 

    public void setRubbleMaxSpots(int rubbleMaxSpots) {
        this.rubbleMaxSpots = rubbleMaxSpots;
    }

    public void setRubbleMinSpots(int rubbleMinSpots) {
        this.rubbleMinSpots = rubbleMinSpots;
    }

    public void setRubbleMinHexes(int rubbleMinHexes) {
        this.rubbleMinHexes = rubbleMinHexes;
    }
  
    public void setFortifiedMaxHexes(int fortifiedMaxHexes) {
        this.fortifiedMaxHexes = fortifiedMaxHexes;
    }

    public void setFortifiedMaxSpots(int fortifiedMaxSpots) {
        this.fortifiedMaxSpots = fortifiedMaxSpots;
    }

    public void setFortifiedMinSpots(int fortifiedMinSpots) {
        this.fortifiedMinSpots = fortifiedMinSpots;
    }

    public void setFortifiedMinHexes(int fortifiedMinHexes) {
        this.fortifiedMinHexes = fortifiedMinHexes;
    }
  
    public void setMaxBuildings(int Buildings) {
        this.maxBuildings = Buildings;
    }

    public void setMinBuildings(int Buildings) {
        this.minBuildings = Buildings;
    }

    public void setMaxCF(int CF) {
        this.maxCF = CF;
    }

    public void setMinCF(int CF) {
        this.minCF = CF;
    }

    public void setMinFloors(int Floors) {
        this.minFloors = Floors;
    }

    public void setMaxFloors(int Floors) {
        this.maxFloors = Floors;
    }

    public void setCityDensity(int types) {
        this.cityDensity = types;
    }

    public void setCityType(String types) {
        this.cityType = types;
    }

    public void setRoads(int roads) {
        this.roads = roads;
    }

    public void setFxMod(int mod) {
        this.fxMod = mod;
    }

    public void setProbabilityForestFire(int prob) {
        this.forestFireProbability = prob;
    }

    public void setProbabilityFreeze(int prob) {
        this.freezeProbability = prob;
    }

    public void setProbabilityFlood(int prob) {
        this.floodProbability = prob;
    }

    public void setProbabilityDrought(int prob) {
        this.droughtProbability = prob;
    }

    public void setCliffProbability(int prob) {
        this.cliffProbability = prob;
    }

    public void setInvertNegativeTerrain(int invert) {
        this.invertNegativeTerrain = invert;
    }

    public void setTownSize(int amount) {
        this.townSize = amount;
    }

    public void setMountPeaks(int amount) {
        this.mountPeaks = amount;
    }

    public void setMountWidthMin(int amount) {
        this.mountWidthMin = amount;
    }

    public void setMountWidthMax(int amount) {
        this.mountWidthMax = amount;
    }

    public void setMountHeightMin(int amount) {
        this.mountHeightMin = amount;
    }

    public void setMountHeightMax(int amount) {
        this.mountHeightMax = amount;
    }

    public void setMountStyle(int amount) {
        this.mountStyle = amount;
    }

    public int getFxMod() {
        return fxMod;
    }
    public int getProbabilityForestFire() {
        return forestFireProbability;
    }
    public int getProbabilityFreeze() {
        return freezeProbability;
    }
    public int getProbabilityFlood() {
        return floodProbability;
    }
    public int getProbabilityDrought() {
        return droughtProbability;
    }
    /**
     * Writes as binary stream
     */
    public void binOut(BinWriter out) throws IOException {
        out.println(id, "id");
        out.println(name, "name");
        out.println(craterProbability, "craterProbability");
        out.println(craterMinimum, "craterMinimum");
        out.println(craterMaximum, "craterMaximum");
        out.println(craterMinRadius, "craterMinRadius");
        out.println(craterMaxRadius, "craterMaxRadius");
        out.println(hillyness, "hillyness");
        out.println(hillElevationRange, "hillElevationRange");
        out.println(hillInvertProbability, "hillInvertProbability");
        out.println(waterMinSpots, "waterMinSpots");
        out.println(waterMaxSpots, "waterMaxSpots");
        out.println(waterMinHexes, "waterMinHexes");
        out.println(waterMaxHexes, "waterMaxHexes");
        out.println(waterDeepProbability, "waterDeepProbability");
        out.println(forestMinSpots, "forestMinSpots");
        out.println(forestMaxSpots, "forestMaxSpots");
        out.println(forestMinHexes, "forestMinHexes");
        out.println(forestMaxHexes, "forestMaxHexes");
        out.println(forestHeavyProbability, "forestHeavyProbability");
        out.println(roughMinSpots, "roughMinSpots");
        out.println(roughMaxSpots, "roughMaxSpots");
        out.println(roughMinHexes, "roughMinHexes");
        out.println(roughMaxHexes, "roughMaxHexes");
        out.println(swampMinSpots, "swampMinSpots");
        out.println(swampMaxSpots, "swampMaxSpots");
        out.println(swampMinHexes, "swampMinHexes");
        out.println(swampMaxHexes, "swampMaxHexes");
        out.println(pavementMinSpots, "pavementMinSpots");
        out.println(pavementMaxSpots, "pavementMaxSpots");
        out.println(pavementMinHexes, "pavementMinHexes");
        out.println(pavementMaxHexes, "pavementMaxHexes");
        out.println(fxMod, "fxMod");
        out.println(forestFireProbability, "forestFireProbability");
        out.println(freezeProbability, "freezeProbability");
        out.println(floodProbability, "floodProbability");
        out.println(droughtProbability, "droughtProbability");
        out.println(cliffProbability, "cliffProbability");
        out.println(invertNegativeTerrain, "invertNegativeTerrain");
        out.println(roadProbability, "roadProbability");
        out.println(riverProbability, "riverProbability");
        out.println(algorithm, "algorithm");
        out.println(theme, "theme");
        out.println(iceMinSpots, "iceMinSpots");
        out.println(iceMaxSpots, "iceMaxSpots");
        out.println(iceMinHexes, "iceMinHexes");
        out.println(iceMaxHexes, "iceMaxHexes");

        out.println(rubbleMinSpots, "rubbleMinSpots");
        out.println(rubbleMaxSpots, "rubbleMaxSpots");
        out.println(rubbleMinHexes, "rubbleMinHexes");
        out.println(rubbleMaxHexes, "rubbleMaxHexes");

        out.println(sandMinSpots, "sandMinSpots");
        out.println(sandMaxSpots, "sandMaxSpots");
        out.println(sandMinHexes, "sandMinHexes");
        out.println(sandMaxHexes, "sandMaxHexes");

        out.println(plantedFieldMinSpots, "plantedFieldMinSpots");
        out.println(plantedFieldMaxSpots, "plantedFieldMaxSpots");
        out.println(plantedFieldMinHexes, "plantedFieldMinHexes");
        out.println(plantedFieldMaxHexes, "plantedFieldMaxHexes");
                
        out.println(fortifiedMinSpots, "fortifiedMinSpots");
        out.println(fortifiedMaxSpots, "fortifiedMaxSpots");
        out.println(fortifiedMinHexes, "fortifiedMinHexes");
        out.println(fortifiedMaxHexes, "fortifiedMaxHexes");

        out.println(minBuildings, "minBuildings");
        out.println(maxBuildings, "maxBuildings");
        out.println(minCF, "minCF");
        out.println(maxCF, "maxCF");
        out.println(minFloors, "minFloors");
        out.println(maxFloors, "maxFloors");
        out.println(cityDensity, "cityDensity");
        out.println(cityType, "cityType");
        out.println(roads, "roads");
        out.println(townSize, "townSize");
        out.println(mountPeaks, "mountPeaks");
        out.println(mountWidthMin, "mountWidthMin");
        out.println(mountWidthMax, "mountWidthMax");
        out.println(mountHeightMin, "mountHeightMin");
        out.println(mountHeightMax, "mountHeightMax");
        out.println(mountStyle, "mountStyle");
        out.println(staticMap, "staticMap");
        out.println(staticMapName, "staticMapName");
        out.println(xSize, "xSize");
        out.println(ySize, "ySize");
        out.println(xBoardSize, "xBoardSize");
        out.println(yBoardSize, "yBoardSize");
        
    }
    
     /**
     * Read from a binary stream
     */
    public void binIn(BinReader in, CampaignData data) throws IOException {
        id = in.readInt("id");
        name = in.readLine("name");
        craterProbability = in.readInt("craterProbability");
        craterMinimum = in.readInt("craterMinimum");
        craterMaximum = in.readInt("craterMaximum");
        craterMinRadius = in.readInt("craterMinRadius");
        craterMaxRadius = in.readInt("craterMaxRadius");
        hillyness = in.readInt("hillyness");
        hillElevationRange = in.readInt("hillElevationRange");
        hillInvertProbability = in.readInt("hillInvertProbability");
        waterMinSpots = in.readInt("waterMinSpots");
        waterMaxSpots = in.readInt("waterMaxSpots");
        waterMinHexes = in.readInt("waterMinHexes");
        waterMaxHexes = in.readInt("waterMaxHexes");
        waterDeepProbability = in.readInt("waterDeepProbability");
        forestMinSpots = in.readInt("forestMinSpots");
        forestMaxSpots = in.readInt("forestMaxSpots");
        forestMinHexes = in.readInt("forestMinHexes");
        forestMaxHexes = in.readInt("forestMaxHexes");
        forestHeavyProbability = in.readInt("forestHeavyProbability");
        roughMinSpots = in.readInt("roughMinSpots");
        roughMaxSpots = in.readInt("roughMaxSpots");
        roughMinHexes = in.readInt("roughMinHexes");
        roughMaxHexes = in.readInt("roughMaxHexes");
        swampMinSpots = in.readInt("swampMinSpots");
        swampMaxSpots = in.readInt("swampMaxSpots");
        swampMinHexes = in.readInt("swampMinHexes");
        swampMaxHexes = in.readInt("swampMaxHexes");
        pavementMinSpots = in.readInt("pavementMinSpots");
        pavementMaxSpots = in.readInt("pavementMaxSpots");
        pavementMinHexes = in.readInt("pavementMinHexes");
        pavementMaxHexes = in.readInt("pavementMaxHexes");
        fxMod = in.readInt("fxMod");
        forestFireProbability = in.readInt("forestFireProbability");
        freezeProbability = in.readInt("freezeProbability");
        floodProbability = in.readInt("floodProbability");
        droughtProbability = in.readInt("droughtProbability");
        cliffProbability = in.readInt("cliffProbability");
        invertNegativeTerrain = in.readInt("invertNegativeTerrain");
        roadProbability = in.readInt("roadProbability");
        riverProbability = in.readInt("riverProbability");
        algorithm = in.readInt("algorithm");
        theme = in.readLine("theme"); 
        iceMinSpots = in.readInt("iceMinSpots");
        iceMaxSpots = in.readInt("iceMaxSpots");
        iceMinHexes = in.readInt("iceMinHexes");
        iceMaxHexes = in.readInt("iceMaxHexes");

        rubbleMinSpots = in.readInt("rubbleMinSpots");
        rubbleMaxSpots = in.readInt("rubbleMaxSpots");
        rubbleMinHexes = in.readInt("rubbleMinHexes");
        rubbleMaxHexes = in.readInt("rubbleMaxHexes");

        sandMinSpots = in.readInt("sandMinSpots");
        sandMaxSpots = in.readInt("sandMaxSpots");
        sandMinHexes = in.readInt("sandMinHexes");
        sandMaxHexes = in.readInt("sandMaxHexes");

        plantedFieldMinSpots = in.readInt("plantedFieldMinSpots");
        plantedFieldMaxSpots = in.readInt("plantedFieldMaxSpots");
        plantedFieldMinHexes = in.readInt("plantedFieldMinHexes");
        plantedFieldMaxHexes = in.readInt("plantedFieldMaxHexes");

        fortifiedMinSpots = in.readInt("fortifiedMinSpots");
        fortifiedMaxSpots = in.readInt("fortifiedMaxSpots");
        fortifiedMinHexes = in.readInt("fortifiedMinHexes");
        fortifiedMaxHexes = in.readInt("fortifiedMaxHexes");

        minBuildings = in.readInt("minBuildings");
        maxBuildings = in.readInt("maxBuildings");
        minCF = in.readInt("minCF");
        maxCF = in.readInt("maxCF");
        minFloors = in.readInt("minFloors");
        maxFloors = in.readInt("maxFloors");
        cityDensity = in.readInt("cityDensity");
        cityType = in.readLine("cityType");
        roads = in.readInt("roads");
        townSize = in.readInt("townSize");
        mountPeaks = in.readInt("mountPeaks");
        mountWidthMin = in.readInt("mountWidthMin");
        mountWidthMax = in.readInt("mountWidthMax");
        mountHeightMin = in.readInt("mountHeightMin");
        mountHeightMax = in.readInt("mountHeightMax");
        mountStyle = in.readInt("mountStyle");
        staticMap = in.readBoolean("staticMap");
        staticMapName = in.readLine("staticMapName");
        xSize = in.readInt("xSize");
        ySize =in.readInt("ySize");
        xBoardSize = in.readInt("xBoardSize");
        yBoardSize = in.readInt("yBoardSize");
        
    }
    
     /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Do not use this to set the id. This is only used in 
     * PlanetEnvironments.add until .dat - saving vanishes.
     * @param id The id to set.
     * @TODO DON'T USE THIS! You were warned! (imi) 
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getTheme() {
        return theme;
    }
    public void settheme(String theme) {
        if (theme.length() <= 1) {
            theme = " ";
        }
        this.theme = theme;
    }
    public boolean isStaticMap() {
        return staticMap;
    }

    public void setStaticMap(boolean map) {
        staticMap = map;
    }

    public String getStaticMapName() {
        return staticMapName;
    }

    public void setStaticMapName(String name) {
        staticMapName = name;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public void setXSize(int xSize) {
        this.xSize = xSize;
    }

    public void setYSize(int ySize) {
        this.ySize = ySize;
    }
    public int getXBoardSize() {
        return xBoardSize;
    }

    public void setXBoardSize(int size) {
        xBoardSize = size;
    }

    public int getYBoardSize() {
        return yBoardSize;
    }

    public void setYBoardSize(int size) {
        yBoardSize = size;
    }

    public int getSandMinSpots() {
        return sandMinSpots;
    }

    public void setsandMinSpots(int sandMinSpots) {
        sandMinSpots = sandMinSpots;
    }

    public int getSandMaxSpots() {
        return sandMaxSpots;
    }

    public void setsandMaxSpots(int sandMaxSpots) {
        sandMaxSpots = sandMaxSpots;
    }

    public int getSandMinHexes() {
        return sandMinHexes;
    }

    public void setsandMinHexes(int sandMinHexes) {
        sandMinHexes = sandMinHexes;
    }

    public int getSandMaxHexes() {
        return sandMaxHexes;
    }

    public void setsandMaxHexes(int sandMaxHexes) {
        sandMaxHexes = sandMaxHexes;
    }

    public int getPlantedFieldMinSpots() {
        return plantedFieldMinSpots;
    }

    public void setplantedFieldMinSpots(int plantedFieldMinSpots) {
        plantedFieldMinSpots = plantedFieldMinSpots;
    }

    public int getPlantedFieldMinHexes() {
        return plantedFieldMinHexes;
    }

    public void setplantedFieldMinHexes(int plantedFieldMinHexes) {
        plantedFieldMinHexes = plantedFieldMinHexes;
    }

    public int getPlantedFieldMaxSpots() {
        return plantedFieldMaxSpots;
    }

    public void setplantedFieldMaxSpots(int plantedFieldMaxSpots) {
        plantedFieldMaxSpots = plantedFieldMaxSpots;
    }

    public int getPlantedFieldMaxHexes() {
        return plantedFieldMaxHexes;
    }

    public void setplantedFieldMaxHexes(int plantedFieldMaxHexes) {
        plantedFieldMaxHexes = plantedFieldMaxHexes;
    }

}
