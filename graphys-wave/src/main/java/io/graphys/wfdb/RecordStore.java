package io.graphys.wfdb;

public interface RecordStore {

    void buildRecordStore(boolean forceRebuild);
    void buildRecordStore();

    PathRetriever pathRetriever();

    RecordRetriever recordRetriever();

    DatabaseInfo dbInfo();
}
