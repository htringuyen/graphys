package io.graphys.wfdb;

import java.util.Map;

public class RecordRetrieverImpl implements RecordRetriever {
    private PathRetriever pathRetriever;
    private DatabaseInfo dbInfo;
    private CachePool<String, Record> cachePool;


    RecordRetrieverImpl(DatabaseInfo dbInfo, PathRetriever pathRetriever, int cachingLimit) {
        this.dbInfo = dbInfo;
        this.pathRetriever = pathRetriever;
        this.cachePool = new CachePoolImpl<>(cachingLimit);
    }

    @Override
    public Record getByName(String recordName) {
        var record = cachePool.get(recordName);

        if (record != null) {
            return record;
        }
        record = new GeneralRecord(
                dbInfo, recordName, pathRetriever.getByRecordName(recordName));

        cachePool.put(recordName, record);

        return record;
    }
}























