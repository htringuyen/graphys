package io.graphys.wave;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Mimic4WfdbTest {
    private static final Logger logger = LogManager.getLogger(Mimic4WfdbTest.class);
    private Instant startInstant;

    @BeforeEach
    void initializeStartInstant() {
        startInstant = Instant.now();
    }

    @AfterEach
    void logExecutionTime() {
        logger.info("Execution time: " + Duration.between(startInstant, Instant.now()).toMillis() + " ms.");
    }

    @Test
    void testGetAllRecordNames() {
        var mimic4Wfdb = new Mimic4WaveformDatabase();

        startInstant = Instant.now();
        var records = mimic4Wfdb.getAllRecordNames();
        logger.info("Execution time: " + Duration.between(startInstant, Instant.now()).toMillis() + " ms.");

        logger.info("Number of records count: " + records.size());
        records.forEach(logger::info);
    }

    @Test
    void testListFileInDirectory() {
        var mimic4Wfdb = new Mimic4WaveformDatabase();

        var fileNames = mimic4Wfdb.listFileInDirectory();
        fileNames.forEach(logger::info);
    }

    @Test
    void testIsFile() {
        var mimic4Wfdb = new Mimic4WaveformDatabase();

        var urls = new ArrayList<String>();

        urls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/p100/p10014354/RECORDS");
        urls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves");
        //urls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/RECORDSx");

        urls.forEach(url -> System.out.println(url.toString() + " is file: " + mimic4Wfdb.getContentType(url)));
    }
}
