package io.github.perseverantdt.dprpbuilder.util;

import com.github.zafarkhaja.semver.Version;

public class PackFormatEntry {
    int format;
    String versionPattern;

    public int getFormat() {
        return format;
    }
    public boolean includes(Version version) {
        return version.satisfies(versionPattern);
    }
    public boolean includes(String version) {
        return Version.valueOf(version).satisfies(versionPattern);
    }

    public PackFormatEntry(int format, String versionPattern) {
        this.format = format;
        this.versionPattern = versionPattern;
    }
}
