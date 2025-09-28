package mekwars.updater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import megamek.Version;

public class VersionUpdaterPicker {
    private List<AbstractVersionUpdater> versionUpdaters = new ArrayList<>();

    public VersionUpdaterPicker() {
        addUpdaters();
        Collections.sort(versionUpdaters);
    }

    /*
     * @param version The version we want to migrate from.
     */
    public boolean migrate(Version version) {
        boolean updated = false;

        for (AbstractVersionUpdater updater : getVersionUpdaters()) {
            if (updater.getVersion().compareTo(version) > 0) {
                updater.update();
                updated = true;
            } else {
                return updated;
            }
        }
        return updated;
    }

    public List<AbstractVersionUpdater> getVersionUpdaters() {
        return versionUpdaters;
    }

    protected void addUpdaters() {
        versionUpdaters.add(new mekwars.updater._9_0_0.VersionUpdater());
    }
}
