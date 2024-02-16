package io.graphys.wfdb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;
import wfdb.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralRecordTests extends BaseTests {
    private static final Logger logger = LogManager.getLogger(GeneralRecordTests.class);

    private StopWatch stopWatch = new StopWatch();
    private RecordStore recordStore;

    @BeforeEach
    public void setupNativeLibrary() {
        var dbInfo = DatabaseInfo.builder()
                .name("mimic4wdb")
                .version("0.1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        recordStore = new RecordStoreImpl(dbInfo, PATH_CACHING_LIMIT, RECORD_CACHING_LIMIT);
        recordStore.buildRecordStore(false);

        wfdb.setwfdb(dbInfo.localHome() + ";" + dbInfo.remoteHome());

        stopWatch.start();
    }

    @AfterEach
    public void summaryTest() {
        stopWatch.stop();
        var timeCount = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);

        logger.info("Executed in " + timeCount + " ms.");
    }

    @Test
    public void testInitiateRecord() {
        var recordName = "81739927_0001";
        var record = new GeneralRecord(
                recordStore.dbInfo(),
                recordName,
                recordStore.pathRetriever().getByRecordName(recordName)
        );

        record.getSignalInfos().forEach(logger::info);
        assertEquals(7, record.getSignalInfos().size());
    }




}
