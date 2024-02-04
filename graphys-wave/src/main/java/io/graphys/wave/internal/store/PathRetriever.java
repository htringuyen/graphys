package io.graphys.wave.internal.store;

import java.nio.file.Path;
import java.util.List;

public interface PathRetriever {
    List<String> retrieveAllPaths();

    String retrievePathById(String id);
}
