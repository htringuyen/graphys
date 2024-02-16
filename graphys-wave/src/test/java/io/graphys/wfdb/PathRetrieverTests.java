package io.graphys.wfdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathRetrieverTests extends BaseTests{
    private static final Logger logger = LogManager.getLogger();

    @Value("${path.local.home_dir}")
    private String LOCAL_HOME;

    @Value("${path.remote.home_dir}")
    private String REMOTE_HOME;

    private StopWatch stopWatch;

    private RecordStore recordStore;
    private PathRetriever pathRetriever;

    @BeforeEach
    void initObjects() {
        var dbInfo = DatabaseInfo.builder()
                .name("mimic4wdb")
                .version("0.1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        recordStore = new RecordStoreImpl(dbInfo, PATH_CACHING_LIMIT, RECORD_CACHING_LIMIT);
        pathRetriever = recordStore.pathRetriever();
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    @AfterEach
    void summaryPerformance() {
        stopWatch.stop();
        var numDuration = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
        logger.info("Main program executed in " + numDuration + " ms.");
    }

    @Test
    void testGetAll() {
        assertEquals(200, pathRetriever.getAll().size());
    }

    @Test
    void testGetSegmentsOfLevel() {
        assertEquals(198, pathRetriever.getSegmentsOf(1).size());
        assertEquals(200, pathRetriever.getSegmentsOf(2).size());
        assertEquals(0, pathRetriever.getSegmentsOf(3).size());
    }

    @Test
    void testRetrievePathById() {
        var vectors = new HashMap<String, String>();
        vectors.put("84050536", "waves/p100/p10082591/84050536/84050536");
        vectors.put("89922194", "waves/p199/p19918916/89922194/89922194");
        vectors.put("88841356", "waves/p134/p13457656/88841356/88841356");

        vectors.forEach((name, path) ->
                assertEquals(path, pathRetriever.getByRecordName(name).getRelativePath()));
    }

}







































