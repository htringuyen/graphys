package io.graphys.wave.internal.store;

import io.graphys.wave.metadata.DatabaseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetCachedPathStoreTest {
    private static final Logger logger = LogManager.getLogger(NetCachedPathStoreTest.class);
    private StopWatch stopWatch;
    private DatabaseInfo dbInfo;

    @BeforeEach
    void doBeforeTest() {
        try {
            this.dbInfo = new DatabaseInfo("mimic4wfdb", null,
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
        System.out.println("Executed in: " + executeTime + " ms.");
    }

    /*@Test
    void testLoadUris() {
        var pathStore = new PathStoreImpl(dbInfo);
        var uris = pathStore.loadRemoteUris(dbInfo.remoteUri());
        uris.stream().forEach(logger::error);
        assertEquals(uris.size(), 200);
    }*/
}

























