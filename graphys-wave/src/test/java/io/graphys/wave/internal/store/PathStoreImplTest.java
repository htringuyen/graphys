package io.graphys.wave.internal.store;

import io.graphys.wave.metadata.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathStoreImplTest {
    private static final Logger logger = LogManager.getLogger(NetCachedPathStoreTest.class);
    private StopWatch stopWatch;
    private DatabaseInfo dbInfo;

    @BeforeEach
    void doBeforeTest() {
        try {
            this.dbInfo = new DatabaseInfo("mimic4wfdb",
                    new URI("file:///home/nhtri/physionet.org/files/local/mimic4wdb/0.1.0/"),
                    new URI("https://physionet.org/files/mimic4wdb/0.1.0/"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.stopWatch = new StopWatch();
        stopWatch.start();
    }

    @AfterEach
    void doAfterTest() {
        stopWatch.stop();
        var executeTime = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
        logger.info("Executed in: " + executeTime + " ms.");
    }

    @Test
    void testBuildLocalPathStore() {
        var pathStore = new PathStoreImpl(dbInfo);
        pathStore.rebuild();
    }

    @Test
    void testBuildLocalPathStoreForMimic4Ecg() {
        DatabaseInfo dbInfo;
        try {
            dbInfo = new DatabaseInfo("mimic-iv-ecg",
                    new URI("file:///home/nhtri/physionet.org/files/local/mimic-iv-ecg/1.0/"),
                    new URI("https://physionet.org/files/mimic-iv-ecg/1.0/"));
            var pathStore = new PathStoreImpl(dbInfo);
            pathStore.rebuild();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetPathsOfLevel() {
        DatabaseInfo dbInfo = null;
        try {
            dbInfo = new DatabaseInfo("mimic-iv-ecg",
                    new URI("file:///home/nhtri/physionet.org/files/local/mimic-iv-ecg/1.0/"),
                    new URI("https://physionet.org/files/mimic-iv-ecg/1.0/"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        var pathStore = new PathStoreImpl(dbInfo);

        var l1Paths = pathStore.getPathsOf(1);
        //logger.info("Paths of level 1");
        //l1Paths.forEach(logger::info);
        assertEquals(1000, l1Paths.size());

        var l2Paths = pathStore.getPathsOf(2);
        //logger.info("Paths of level 2");
        //l2Paths.forEach(logger::info);
        assertEquals(724087, l2Paths.size());

    }
}





















