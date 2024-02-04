package io.graphys.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsTest {
    private static final Logger logger = LogManager.getLogger(FileUtilsTest.class);
    private StopWatch stopWatch;

    @BeforeEach
    void doBeforeTest() {
        this.stopWatch = new StopWatch();
        stopWatch.start();
    }

    @AfterEach
    void doAfterTest() {
        stopWatch.stop();
        var executeTime = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
        System.out.println("Executed in: " + executeTime + " ms.");
    }

    @Test
    void testHeadForHeaders() {
        var strUrls = new ArrayList<String>();
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/p100/p10014354/RECORDS");
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/RECORDS");
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/RECORDS");

        strUrls.forEach(strUrl -> {
            try {
                var uri = new URI(strUrl);
                var headers = FileUtils.headForHeaders(uri.toURL());
                logger.error(headers);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testHeadForHeadersWithRestTemplate() {
        var strUrls = new ArrayList<String>();
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/p100/p10014354/RECORDS");
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/RECORDS");
        strUrls.add("https://physionet.org/files/mimic4wdb/0.1.0/waves/RECORDS");

        strUrls.forEach(strUrl -> {
            try {
                var uri = new URI(strUrl);
                var headers = FileUtils.headForHeadersWithRestTemplate(uri.toURL());
                logger.error(headers);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testFileExists() {
        var pathVectors = new HashMap<String, Boolean>();
        var httpBasePath = "https://physionet.org/files/mimic4wdb/0.1.0/";
        var localBasePath = "file:///home/nhtri/physionet.org/files/mimic4wdb/0.1.0/";

        pathVectors.put(httpBasePath + "waves/p100/p10014354", true);
        pathVectors.put(httpBasePath + "RECORDS", true);
        pathVectors.put(httpBasePath + "waves/RECORDS", false);

        pathVectors.put(localBasePath + "waves/p100/p10014354", true);
        pathVectors.put(localBasePath + "RECORDS", true);
        pathVectors.put(localBasePath + "waves/RECORDS", false);

        pathVectors.forEach((path, result) -> {
            try {
                var uri = new URI(path);
                var exists = FileUtils.fileExists(uri);
                assertEquals(result, exists);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testUriAppend() {
        var httpBasePath = "https://physionet.org/files/mimic4wdb/0.1.0/";
        var fragment = "waves/p100/p10014354";

        try {
            var baseUri = new URI(httpBasePath);
            var fullUri = FileUtils.uriAppend(baseUri, fragment);
            logger.error(fullUri.toString());
            assertTrue(FileUtils.fileExists(fullUri));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}























