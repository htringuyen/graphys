package io.graphys.wfdb;

import io.graphys.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecordStoreImpl implements RecordStore {
    static final String RECORD_LIST_FILE_NAME = "RECORDS";

    static final String LOCAL_RECORD_PATHS_FILE_NAME = "RECORD_PATHS.csv";

    private static final String SEGMENT_BOUNDARY_MARK = "<>";

    private final DatabaseInfo dbInfo;

    private final PathRetriever pathRetriever;

    private final RecordRetriever recordRetriever;

    public RecordStoreImpl(DatabaseInfo dbInfo, int pathCachingLimit, int recordCachingLimit) {
        this.dbInfo = dbInfo;

        this.pathRetriever = new PathRetrieverImpl(dbInfo, pathCachingLimit);

        this.recordRetriever = new RecordRetrieverImpl(dbInfo, pathRetriever, recordCachingLimit);
    }

    @Override
    public DatabaseInfo dbInfo() {
        return dbInfo;
    }

    @Override
    public PathRetriever pathRetriever() {
        return pathRetriever;
    }

    @Override
    public RecordRetriever recordRetriever() {
        return recordRetriever;
    }

    @Override
    public void buildRecordStore() {
        buildRecordStore(false);
    }

    @Override
    public void buildRecordStore(boolean forceRebuild) {
        if (forceRebuild) {
            buildRecordStoreHelper();
        }
        else {
            if (!storeExists()) {
                buildRecordStoreHelper();
            }
        }
    }

    private boolean storeExists() {
        var dbUri = dbInfo.localUri();
        return Files.isDirectory(Path.of(dbUri))
                && Files.exists(Path.of(dbUri.resolve(RECORD_LIST_FILE_NAME)))
                && Files.exists(Path.of(dbUri.resolve(LOCAL_RECORD_PATHS_FILE_NAME)));
    }

    private void buildRecordStoreHelper() {
        var recordPaths = Collections.synchronizedList(new LinkedList<String>());
        var pathAppenders = new ConcurrentHashMap<URI, List<String>>();
        pathAppenders.put(dbInfo.localUri(), Collections.synchronizedList(new LinkedList<>()));

        var dir = new File(dbInfo.localUri());
        if (! dir.isDirectory() && ! dir.mkdirs()) {
            throw new RuntimeException("Error when creating directory: " + dir);
        }
        buildRecordStoreHelper(dbInfo.remoteUri(), dbInfo.localUri(), "",
                pathAppenders, 1, new HashMap<Integer, String>(), recordPaths);


        try (var out = new PrintWriter(
                new File(dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME)))) {
            recordPaths.stream()
                    .map(segmentablePath -> {
                        var result = new StringBuilder();
                        var purePath = segmentablePath.replace(SEGMENT_BOUNDARY_MARK, "");
                        result.append(extractRecordNameFrom(purePath)).append(",");
                        result.append(
                                Arrays.stream(segmentablePath.split(SEGMENT_BOUNDARY_MARK))
                                        .filter(s -> !s.isBlank())
                                        .collect(Collectors.joining(","))
                        );
                        return result.toString();
                    })
                    .sorted()
                    .forEach(out::println);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "error when open file: " + dbInfo.remoteUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME));
        }



        pathAppenders.forEach((uri, list) -> {
            try (var out = new PrintWriter(new File(uri.resolve(RECORD_LIST_FILE_NAME)))) {
                list.stream().sorted().forEach(out::println);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("error when open file: " + uri);
            }
        });
    }

    private void buildRecordStoreHelper(URI remoteUri, URI localUri, String segmentablePath, Map<URI, List<String>> pathAppenders,
                                        int level, Map<Integer, String> levelMap, List<String> recordPaths) {
        try (var in = new Scanner(remoteUri.resolve(RECORD_LIST_FILE_NAME).toURL().openStream())) {
            in.tokens().parallel()
                    .filter(s -> !s.isBlank())
                    .forEach(str -> {
                        pathAppenders.get(localUri).add(str);

                        if (! Files.isDirectory(Path.of(localUri))) {
                            throw new RuntimeException("Directory should exist.");
                        }

                        if (synchronizedCheckFileExists(
                                remoteUri.resolve(str).resolve(RECORD_LIST_FILE_NAME), level, levelMap)) {
                            var appendedDir = new File(localUri.resolve(str));
                            if (! appendedDir.isDirectory() && ! appendedDir.mkdirs()) {
                                throw new RuntimeException("Error when creating directory.");
                            }

                            pathAppenders.put(localUri.resolve(str), new LinkedList<>());

                            buildRecordStoreHelper(remoteUri.resolve(str), localUri.resolve(str),
                                    segmentablePath + SEGMENT_BOUNDARY_MARK + str,
                                    pathAppenders, level + 1, levelMap, recordPaths);
                        }
                        else {
                            recordPaths.add(segmentablePath + SEGMENT_BOUNDARY_MARK + str);
                            str = str.substring(0, str.lastIndexOf("/"));
                            var appendedDir = new File(localUri.resolve(str));
                            if (! appendedDir.isDirectory() && ! appendedDir.mkdirs()) {
                                throw new RuntimeException("Error when creating directory.");
                            }
                        }
                    });
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when reading record path.");
        }
    }

    private synchronized boolean synchronizedCheckFileExists(URI uri, int level, Map<Integer, String> levelMap) {
        if (! levelMap.containsKey(level)) {
            levelMap.put(level, "UNKNOWN");
        }

        var status = levelMap.get(level);

        if (status.equals("UNKNOWN")) {
            var fileExists = FileUtils.fileExists(uri);
            levelMap.put(level, fileExists ? "FILE_EXISTS" : "FILE_NOT_EXISTS");
            return fileExists;
        }
        else if (status.equals("FILE_EXISTS")) {
            return true;
        }
        else if (status.equals("FILE_NOT_EXISTS")) {
            return false;
        }

        return false;
    }

    private String extractRecordNameFrom(String uriStr) {
        var startInd = 1 + uriStr.lastIndexOf("/");
        var recordName = uriStr.substring(startInd);
        if (recordName.isBlank()) {
            throw new RuntimeException("Unexpected result when extract record id.");
        }
        return recordName;
    }

    private String extractRecordPathFrom(String uriStr) {
        var index = uriStr.lastIndexOf(dbInfo.localUri().toString());

        if (index < 0) {
            index = dbInfo.remoteUri().toString().length()
                    + uriStr.lastIndexOf(dbInfo.remoteUri().toString());
        }
        else {
            index += dbInfo.localUri().toString().length();
        }

        return uriStr.charAt(index) != File.separator.charAt(0)
                ? uriStr.substring(index) : uriStr.substring(index + 1);
    }
}


