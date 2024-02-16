package io.graphys.wfdb;

import lombok.Builder;

import java.net.URI;

@Builder
public class DatabaseInfo {
    private final String name;
    private final String version;
    private final URI localHome;
    private final URI remoteHome;

    public DatabaseInfo(String name, String version, URI localHome, URI remoteHome) {
        this.name = name;
        this.version = version;
        this.localHome = localHome;
        this.remoteHome = remoteHome;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public URI localUri() {
        return localHome.resolve(name + "/").resolve( version + "/");
    }

    public URI remoteUri() {
        return remoteHome.resolve("/" + name).resolve( "/" + version);
    }

    public URI localHome() {
        return localHome;
    }

    public URI remoteHome() {
        return remoteHome;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatabaseInfo other) {
            return this.name.equals(other.name)
                    && this.version.equals(other.version);
        }

        return false;
    }
}
