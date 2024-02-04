package io.graphys.wave.internal.store;

import java.io.Closeable;
import java.net.URI;
import java.util.List;

public interface PathStore {
    void rebuild();
    void setCacheSize(int size);
    PathRetriever retriever();
    String getDbName();
    List<String> getPathsOf(int level);
    List<String> getAllPaths();
    String getPathByRecordId(String recordId);

}
