package io.graphys.wfdb;

import java.nio.file.Path;
import java.util.List;

public interface PathRetriever {
    List<RecordPath> getAll();

    List<String> getSegmentsOf(int level);

    RecordPath getByRecordName(String recordName);
}
