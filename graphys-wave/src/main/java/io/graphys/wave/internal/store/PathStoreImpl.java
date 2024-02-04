package io.graphys.wave.internal.store;

import io.graphys.util.FileUtils;
import io.graphys.wave.metadata.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PathStoreImpl implements PathStore {
    private static final Logger logger = LogManager.getLogger(PathStoreImpl.class);
    private static final String RECORD_LIST_FILE_NAME = "RECORDS";
    private static final String LOCAL_RECORD_PATHS_FILE_NAME = "RECORD_PATHS.csv";
    private static final int DEFAULT_CACHE_SIZE = 200;
    private final DatabaseInfo dbInfo;
    private int cacheSize = DEFAULT_CACHE_SIZE;
    private final Map<String, String> cacheMap = new ConcurrentHashMap<>(DEFAULT_CACHE_SIZE);

    public PathStoreImpl(DatabaseInfo dbInfo) {
        this.dbInfo = dbInfo;
    }

    @Override
    public void setCacheSize(int size) {
        this.cacheSize = size;
    }

    @Override
    public String getDbName() {
        return dbInfo.name();
    }

    @Override
    public void rebuild() {
        var recordPaths = Collections.synchronizedList(new LinkedList<String>());
        var pathAppenders = new ConcurrentHashMap<URI, List<String>>();
        pathAppenders.put(dbInfo.localUri(), new LinkedList<>());

        var dir = new File(dbInfo.localUri());
        if (! dir.isDirectory() && ! dir.mkdirs()) {
            throw new RuntimeException("Error when creating directory.");
        }
        buildLocalPathStore(dbInfo.remoteUri(), dbInfo.localUri(),
                pathAppenders, 1, new HashMap<Integer, String>(), recordPaths);


        try (var out = new PrintWriter(
                new File(dbInfo.localUri().resolve(LOCAL_RECORD_PATHS_FILE_NAME)))) {
            recordPaths.stream()
                    .map(path -> extractRecordIdFrom(path) + "," + path)
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

    @Override
    public List<String> getPathsOf(int level) {
        return getPathsOfHelper("", 1, level);
    }

    @Override
    public List<String> getAllPaths() {
        return getPathsOfHelper("", 1, -1);
    }

    @Override
    public String getPathByRecordId(String recordId) {
        return null;
    }

    private List<String> getPathsOfHelper(String currentPath, int level, int limit) {
        var file = new File(dbInfo.localUri().resolve(currentPath).resolve(RECORD_LIST_FILE_NAME));

        if (!file.exists() || (limit > 0 && level > limit)) {
            var result = new ArrayList<String>();
            result.add(currentPath);
            return result;
        }

        try (var in = new Scanner(file.toPath())) {
            return in.tokens()
                    .parallel()
                    .map(str -> URI.create(currentPath).resolve(str).toString())
                    .flatMap(str -> getPathsOfHelper(str, level + 1, limit).stream())
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when reading file.");
        }
    }


    private void buildLocalPathStore(URI remoteUri, URI localUri, Map<URI,
            List<String>> pathAppenders, int level, Map<Integer, String> levelMap, List<String> recordPaths) {
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

                            buildLocalPathStore(remoteUri.resolve(str), localUri.resolve(str),
                                    pathAppenders, level + 1, levelMap, recordPaths);
                        }
                        else {
                            recordPaths.add(extractRecordPathFrom(remoteUri.resolve(str).toString()));
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

    private String extractRecordIdFrom(String uriStr) {
        var startInd = 1 + uriStr.lastIndexOf("/");
        var recordId = uriStr.substring(startInd);
        if (recordId.isBlank()) {
            throw new RuntimeException("Unexpected result when extract record id.");
        }
        return recordId;
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

    @Override
    public PathRetriever retriever() {
        return null;
    }

}





















