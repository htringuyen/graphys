package io.graphys.wfdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import wfdb.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static io.graphys.wfdb.RecordStoreImpl.LOCAL_RECORD_PATHS_FILE_NAME;
import static io.graphys.wfdb.RecordStoreImpl.RECORD_LIST_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecordStoreTests extends BaseTests {
    private static final Logger logger = LogManager.getLogger();

    private StopWatch stopWatch;

    private RecordStore recordStore;

    @BeforeEach
    void initObjects() {
        var dbInfo = DatabaseInfo.builder()
                .name("mimic4wdb")
                .version("0.1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();


        recordStore = new RecordStoreImpl(dbInfo, PATH_CACHING_LIMIT, RECORD_CACHING_LIMIT);

        wfdb.setwfdb(dbInfo.remoteHome().toString());

        stopWatch = new StopWatch();
        stopWatch.start();
    }

    @AfterEach
    void summaryPerformance() {
        stopWatch.stop();
        var numDuration = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
        logger.info("Executed in " + numDuration + " ms.");
    }

    @Test
    void testBuildRecordStore() {
        recordStore.buildRecordStore(true);

        var dbRoot = recordStore.dbInfo().localUri();
        assertTrue(Files.exists(
                Path.of(dbRoot.resolve("waves/p100/p10014354/81739927"))));

        try (
                var l1PathsInput = new Scanner(Path.of(dbRoot.resolve(RECORD_LIST_FILE_NAME)));
                var allPathsInput = new Scanner(Path.of(dbRoot.resolve(LOCAL_RECORD_PATHS_FILE_NAME)));
        ) {
            assertEquals(198, l1PathsInput.tokens().filter(s -> !s.isBlank()).count());
            assertEquals(200, allPathsInput.tokens().filter(s -> !s.isBlank()).count());
        }
        catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testRetrieveRecordByName() {
        var vectors = new HashMap<String, Integer>();
        vectors.put("81739927", 7);
        //vectors.put("83268087", 7);
        //vectors.put("84248019", 12);

        vectors.forEach((recName, nSig) -> {
            var record = recordStore.recordRetriever().getByName(recName);
            assertEquals(
                    nSig, record.getSignalInfos().size());
            record.getSignalInfos().forEach(logger::info);
            wfdb.wfdbquit();
        });
    }
}































