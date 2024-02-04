package io.graphys.wave.metadata;

import java.net.URI;

public class DatabaseInfo {
    private final String name;
    private final URI remoteUri;
    private final URI localUri;

    public DatabaseInfo(String name, URI localUri, URI remoteUri) {
        this.name = name;
        this.localUri = localUri;
        this.remoteUri = remoteUri;
    }

    public String name() {
        return name;
    }

    public URI localUri() {
        return localUri;
    }

    public URI remoteUri() {
        return remoteUri;
    }
}
