package io.graphys.wfdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wfdb.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NativeLibraryTests {
    private static Logger logger = LogManager.getLogger(NativeLibraryTests.class);
    @BeforeAll
    static void initNativeLibrary() {
        wfdb.wfdbquit();
        wfdb.setwfdb("https://physionet.org/files/");
    }

    @Test
    void testIfNativeLibLoaded() {

    }

    @Test
    void testConcurrentReading() throws InterruptedException {
        var random = new Random();
        var nSample = 1000;
        var records = new LinkedList<String>();
        records.add("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927");
        records.add("mimic4wdb/0.1.0/waves/p100/p10019003/87033314/87033314");
        //records.add("mimic4wdb/0.1.0/waves/p100/p10020306/83404654/83404654");
        //records.add("mimic4wdb/0.1.0/waves/p100/p10039708/83411188/83411188");
        //records.add("mimic4wdb/0.1.0/waves/p100/p10039708/85583557/85583557");
        //records.add("mimic4wdb/0.1.0/waves/p100/p10079700/85594648/85594648");

        /*records.stream()
                //.parallel()
                .forEach(record -> printingTask(record, nSample));*/

        /*records.stream()
                .forEach(record -> {
                    var thread = new Thread(() -> printingTask(record, nSample));
                    thread.start();
                });*/

        var es = Executors.newFixedThreadPool(3);

        //var es = Executors.newFixedThreadPool(3);

        /*records.stream()
                .forEach(record -> {
                    es.submit(() -> printingTask(record, nSample));
                });*/

        for (var record: records) {
            /*try {
                Thread.sleep(random.nextInt(100));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            es.execute(() -> printingTask(record, nSample));

            /*var thread = new Thread(() -> printingTask(record, nSample));
            thread.start();*/
        }

        es.shutdown();
        es.awaitTermination(30, TimeUnit.SECONDS);
        es.close();


    }

    private static void printingTask(String record, int nSample) {
        var random = new Random();


        System.loadLibrary("wfdbjava");
        wfdb.wfdbquit();
        wfdb.setwfdb("https://physionet.org/files/");


        int nSig = wfdb.isigopen(record, null, 0);

        var samples = new WFDB_SampleArray(nSig);

        var siArray = new WFDB_SiginfoArray(nSig);

        wfdb.isigopen(record, siArray.cast(), nSig);

        var fileName = record.replace("/", "-");

        /*for (int i = 0; i < nSample; i++) {
            var samples = new WFDB_SampleArray(nSig);
            wfdb.getvec(samples.cast());
            try {
                Thread.sleep(random.nextInt(10));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        try (var out = new PrintWriter(fileName)) {
            IntStream.range(0, nSample)
                    .mapToObj(i -> {
                        wfdb.getvec(samples.cast());
                        return IntStream.range(0, nSig)
                                .mapToObj(samples::getitem)
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
                    })
                    .forEach(out::println);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        wfdb.wfdbquit();
    }

    @Test
    void testSignalGroup() {
        var records = new LinkedList<String>();
        //records.add("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927_0001");
        records.add("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927");

        var record = records.getFirst();

        var nSig = wfdb.isigopen(record, null, 0);
        var siArray = new WFDB_SiginfoArray(nSig);

        wfdb.isigopen(record, siArray.cast(), -nSig);

        IntStream.range(0, nSig)
                .mapToObj(siArray::getitem)
                .forEach(si -> logger.info(SignalInfo.from(si)));
    }


}
