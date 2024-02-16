package io.graphys.wfdb.player;

import io.graphys.wfdb.*;
import io.graphys.wfdb.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wfdb.wfdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ForwardingRecordPlayerTests extends BaseTests {
    private RecordRetriever recordRetriever;

    @BeforeEach
    public void initialize() {
        var dbInfo = DatabaseInfo.builder()
                .name("mimic4wdb")
                .version("0.1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        wfdb.setwfdb(dbInfo.localHome() + ";" + dbInfo.remoteHome());
        var recStore = new RecordStoreImpl(dbInfo, PATH_CACHING_LIMIT, RECORD_CACHING_LIMIT);

        recordRetriever = recStore.recordRetriever();
    }

    @Test
    void testConcurrentRecordPlaying() {
        var records = new LinkedList<Record>();

        records.add(recordRetriever.getByName("81739927"));
        //records.add(recordRetriever.getByName("87033314"));
        //records.add(recordRetriever.getByName("83404654"));
        //records.add(recordRetriever.getByName("83411188"));

        int nRequests = 100;
        int chunkSize = 100;

        records.stream()
                .parallel()
                .forEach(record -> {
                    try (
                            var player = ForwardingRecordPlayer.of(record);
                            var out = new PrintWriter(record.getId())
                    ) {
                        player.start();

                        for (int i = 0; i < nRequests; i++) {

                            var chunk = player.playNext(chunkSize);

                            IntStream.range(0, chunkSize)
                                    .mapToObj(chunk::getFrame)
                                    .map(a ->
                                            Arrays.stream(a)
                                                    .mapToObj(String::valueOf)
                                                    .collect(Collectors.joining(","))
                                    )
                                    .forEach(out::println);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void testRecordConcurrentPlaying_2() throws InterruptedException, IOException {
        var records = new LinkedList<Record>();
        var players = new LinkedList<RecordPlayer>();
        var outs = new LinkedList<PrintWriter>();


        int nRequests = 100;
        int chunkSize = 100;

        records.add(recordRetriever.getByName("81739927"));
        records.add(recordRetriever.getByName("87033314"));
        records.add(recordRetriever.getByName("83404654"));
        records.add(recordRetriever.getByName("83411188"));

        records.stream().forEach(record -> {
            var player = ForwardingRecordPlayer.of(record);
            players.add(player);
            player.start();
        });

        for (var record: records) {
            outs.add(new PrintWriter(record.getId() + ".csv"));
        }

        for (int i = 0; i < nRequests; i++) {
            for (int j = 0; j < records.size(); j++) {
                var player = players.get(j);
                var out = outs.get(j);

                var frames = player.playNext(chunkSize);

                Arrays.stream(frames.getFrames())
                        .map(frame -> Arrays.stream(frame)
                                .mapToObj(String::valueOf)
                                .collect(Collectors.joining(","))
                        )
                        .forEach(out::println);
            }
        }

        /*players.stream()
                .parallel()
                .forEach(player -> {
                    for (int i = 0; i < nRequests; i++) {
                        player.playNext(chunkSize);
                    }
                });*/
    }

}
















