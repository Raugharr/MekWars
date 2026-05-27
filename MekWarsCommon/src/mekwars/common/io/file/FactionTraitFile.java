/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.io.file;

import mekwars.common.entities.FactionTrait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class FactionTraitFile {
    private static final Logger LOGGER = LogManager.getLogger(FactionTraitFile.class);

    private Path path;
    private String houseName;
    private List<FactionTrait> factionTraits = new ArrayList<>();

    public FactionTraitFile(Path path, String houseName) {
        this.path = path;
        this.houseName = houseName;
    }

    public Path getPath() {
        return path;
    }

    public String getHouseName() {
        return houseName;
    }

    public List<FactionTrait> getFactionTraits() {
        return factionTraits;
    }

    public FactionTrait getFactionTraitByName(String name) {
        for (FactionTrait factionTrait : factionTraits) {
            if (factionTrait.getName().equals(name)) {
                return factionTrait;
            }
        }
        return null;
    }

    public boolean removeFactionTrait(String name) {
        ListIterator<FactionTrait> listIterator = factionTraits.listIterator();

        while (listIterator.hasNext()) {
            FactionTrait factionTrait = listIterator.next();

            if (factionTrait.getName().equals(name)) {
                listIterator.remove();
                return true;
            }
        }
        return false;
    }

    public void addFactionTrait(FactionTrait factionTrait) {
        removeFactionTrait(factionTrait.getName());
        factionTraits.add(factionTrait);
    }

    public void load() throws IOException {
        if (!Files.exists(path)) {
            LOGGER.warn("Unable to load faction trait file {}, file does not exist", path);
            return;
        }

        factionTraits =
                Files.readAllLines(path).stream()
                        .map(line -> new FactionTrait(line))
                        .collect(Collectors.toList());
    }

    public void save() {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }

            StringBuilder sb = new StringBuilder();
            List<String> outputLines =
                    factionTraits.stream()
                            .map(
                                    trait -> {
                                        sb.setLength(0);
                                        trait.serialize(sb);
                                        return sb.toString();
                                    })
                            .collect(Collectors.toList());

            Files.write(path, outputLines);
        } catch (Exception ex) {
            LOGGER.error("Error while saving trait file for faction: {}", houseName, ex);
        }
    }
}
