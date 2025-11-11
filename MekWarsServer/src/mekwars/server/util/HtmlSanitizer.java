package mekwars.server.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

public class HtmlSanitizer {
    private static final Logger LOGGER = LogManager.getLogger(HtmlSanitizer.class);
    private static final String configFilename = "HTMLSanitizer.cfg";
    
    private Cleaner htmlCleaner = null;
    private boolean allowLinks;
    private boolean allowPlanetLinks;


    public HtmlSanitizer(boolean allowLinks, boolean allowPlanetLinks) {
        this.allowLinks = allowLinks;
        this.allowPlanetLinks = allowPlanetLinks;
        loadSanitizer();
    }

    public void loadSanitizer() {
        Safelist whitelist = Safelist.relaxed();
        if (allowLinks) {
            whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        } else {
            whitelist.removeAttributes("a", "href");
        }

        whitelist.addTags("planet");
        whitelist.addAttributes("planet", "name");
        
        ArrayList<String> allowedTags = new ArrayList<String>();
        HashMap<String, ArrayList<String>> allowedAttributes = new HashMap<String, ArrayList<String>>();
        
        try {
            FileInputStream fstream = new FileInputStream("./data/" + configFilename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.toLowerCase().trim();
                // Process it here
                if (line.startsWith("#") || line.length() < 5) {
                    // Comment
                } else if (line.startsWith("tag")) {
                    // Add next token to allowed tags
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken();  // Eat the "Tag" token
                    allowedTags.add(st.nextToken());
                } else if (line.startsWith("att")) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken(); // Eat the "Att" token
                    String tag = st.nextToken();
                    while (st.hasMoreTokens()) {
                        if (allowedAttributes.keySet().contains(tag)) {
                            allowedAttributes.get(tag).add(st.nextToken());
                        } else {
                            ArrayList<String> newTag = new ArrayList<String>();
                            newTag.add(st.nextToken());
                            allowedAttributes.put(tag, newTag);
                        }
                    }
                }
            }
            br.close();
            in.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("No HTMLSanitizer.cfg found.");
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
        }
        
        for (String tag : allowedTags) {
            LOGGER.error("Adding to whitelist: " + tag);
            whitelist.addTags(tag);
        }
        
        for (String att : allowedAttributes.keySet()) {
            ArrayList<String> attributes = allowedAttributes.get(att);
            
            for (String attribute : attributes) {
                whitelist.addAttributes(att, attribute);
            }
        }
        LOGGER.info(whitelist.toString());
        htmlCleaner = new Cleaner(whitelist);
    }

    /**
     * Returns a string sanitized of potentially harmful HTML
     * 
     * @param unclean string
     * @return Sanitized string
     * 
     * @author Spork
     */
    public String sanitize(String unclean) {
        Document document = Jsoup.parseBodyFragment(unclean);
        document = htmlCleaner.clean(document);
        String cleanDocument = document.body().html();
        if (allowPlanetLinks) {
            cleanDocument = replacePlanetTags(cleanDocument);
        }
        return cleanDocument;
    }

    protected String replacePlanetTags(String input) {
        String regex = "<planet name=\"([^\"]+)\">([^<]+)</planet>";
        String replacement = "<a href=\"JUMPTOPLANET$1\">$2</a>";
        Pattern planetPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = planetPattern.matcher(input);
        return matcher.replaceAll(replacement);
    }
    
}
