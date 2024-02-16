package io.graphys.wfdb;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class RecordPath {
    private final String dbVersion;

    private final String dbName;

    private final String[] segments;


    public String getSegment(int order) {
        return segments[order];
    }

    public String getRelativePath() {
        var relativePath = String.join("/", segments);
        return normalizePath(relativePath);
    }

    public String getFullPath() {
        var relativePath = String.join("/", segments);
        return normalizePath(dbName + "/" + dbVersion + "/" + relativePath);
    }

    private String normalizePath(String path) {
        var result = new StringBuilder();

        if (path.charAt(0) == '/') {
            result.append(path.substring(1));
        }

        result.append(path.replaceAll("[/]{2,}", "/"));
        if (result.charAt(result.length() - 1) == '/') {
            return result.substring(0, result.length() - 1);
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecordPath other) {
            return getFullPath().equals(other.getFullPath());
        }
        return false;
    }
}
