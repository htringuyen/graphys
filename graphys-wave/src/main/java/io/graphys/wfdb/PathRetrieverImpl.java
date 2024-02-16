package io.graphys.wfdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static io.graphys.wfdb.RecordStoreImpl.LOCAL_RECORD_PATHS_FILE_NAME;


public class PathRetrieverImpl implements PathRetriever {
    private static final Logger logger = LogManager.getLogger(PathRetrieverImpl.class);

    private DatabaseInfo dbInfo;

    private CachePool<String, RecordPath> cachePool;

    protected PathRetrieverImpl(DatabaseInfo dbInfo, int cachingLimit) {
        this.dbInfo = dbInfo;
        this.cachePool = new CachePoolImpl<>(cachingLimit);
    }

    @Override
    public List<RecordPath> getAll() {
        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME)))) {
            return in
                    .tokens()
                    .parallel()
                    .map(this::parseRecordPath)
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when open file: " + dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME));
        }
    }

    @Override
    public RecordPath getByRecordName(String recordName) {
        if (recordName == null) {
            return null;
        }

        var result = cachePool.get(recordName);
        if (result != null) {
            return result;
        }

        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME)))) {
            var recordPath = in.tokens()
                    .parallel()
                    .filter(s -> s.startsWith(recordName + ","))
                    .map(this::parseRecordPath)
                    .findFirst()
                    .orElse(null);

            if (recordPath != null) {
                cachePool.put(recordName, recordPath);
            }

            return recordPath;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> getSegmentsOf(int level) {
        if (level < 1) {
            return new LinkedList<>();
        }

        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME)))) {
            return in.tokens()
                    .map(s -> {
                        var columns = s.split(",");
                        if (level < columns.length) {
                            return columns[level];
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when open file " + dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME));
        }
    }

    private RecordPath parseRecordPath(String str) {
        var columns = str.split(",");
        if (columns[0].isBlank()
                || columns.length < 2) {
            throw new RuntimeException("Error when reading file " + LOCAL_RECORD_PATHS_FILE_NAME);
        }
        return RecordPath
                .builder()
                .dbName(dbInfo.name())
                .dbVersion(dbInfo.version())
                .segments(Arrays.copyOfRange(columns, 1, columns.length))
                .build();
    }


    // Previous version
    /*@Override
    public List<String> retrievePathsOf(int level) {
        return retrievePathsOfHelper("", 1, level);
    }

    private List<String> retrievePathsOfHelper(String currentPath, int level, int limit) {
        var file = new File(dbInfo.localUri().resolve(currentPath).resolve(RECORD_LIST_FILE_NAME));

        if (!file.exists() && limit > 0 && level - 1 < limit) {
            return new ArrayList<>();
        }

        if (!file.exists() || (limit > 0 && level > limit)) {
            var result = new ArrayList<String>();
            result.add(currentPath);
            return result;
        }

        try (var in = new Scanner(file.toPath())) {
            return in.tokens()
                    .parallel()
                    .map(str -> URI.create(currentPath).resolve(str).toString())
                    .flatMap(str -> retrievePathsOfHelper(str, level + 1, limit).stream())
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when reading file.");
        }
    }*/


}



























