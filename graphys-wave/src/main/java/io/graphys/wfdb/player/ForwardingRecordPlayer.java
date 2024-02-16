package io.graphys.wfdb.player;

import io.graphys.wfdb.Record;

import wfdb.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class ForwardingRecordPlayer implements RecordPlayer {

    private Record record;

    private int sampleTime;

    private boolean started;

    private BlockingQueue<FrameChunk> playingQueue;

    private Thread playingThread;

    private WFDB_SampleArray sampleHolder;

    private ForwardingRecordPlayer(Record record) {
        this.record = record;
        this.sampleTime = 0;
        this.playingQueue = new LinkedBlockingQueue<>();
        this.started = false;
    }

    public static RecordPlayer of(Record record) {
        return new ForwardingRecordPlayer(record);
    }

    @Override
    public String getId() {
        return record.getId();
    }

    @Override
    public int getSampleTime() {
        return sampleTime;
    }

    @Override
    public void start() {
        initializeAndOpenSignals();

        //playingThread = Thread.startVirtualThread(player());

        playingThread = new Thread(player());
        playingThread.start();
    }

    @Override
    public FrameChunk playNext(int nFrames) {
        var chunk = new FrameChunkImpl(sampleTime, nFrames);

        try {
            playingQueue.put(chunk);
            chunk.waitUntilFilledUp();
            return chunk;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when put into playing queue");
        }


    }

    @Override
    public void close() {
        playingThread.interrupt();
    }



    private Runnable player() {
        return () -> {
            if (started) {
                throw new IllegalStateException("This player has been started.");
            }

            boolean shouldContinue = true;

            while (shouldContinue) {
                try {
                    var chunk = playingQueue.take();

                    /*if (sampleTime != chunk.getStartSampleTime()) {
                        wfdb.isigsettime(chunk.getStartSampleTime());
                    }*/

                    var frames = IntStream
                            .range(0, chunk.getSize())
                            .mapToObj(i -> getNextVec())
                            .toArray(int[][]::new);

                    chunk.fillUp(frames);
                    sampleTime += chunk.getSize();
                }
                catch (InterruptedException e) {
                    shouldContinue = false;
                }
            }

        };
    }


    private void initializeAndOpenSignals() {
        wfdb.wfdbquit();

        sampleHolder = new WFDB_SampleArray(record.getNumOfSignals());

        var siArray = new WFDB_SiginfoArray(record.getNumOfSignals());

        wfdb.isigopen(
                record.getPath().getFullPath(),
                siArray.cast(),
                record.getNumOfSignals()
        );
    }

    private int[] getNextVec() {
        //var samples = new WFDB_SampleArray(record.getNumOfSignals());

        wfdb.getvec(sampleHolder.cast());

        return IntStream.range(0, record.getNumOfSignals())
                .map(sampleHolder::getitem)
                .toArray();
    }
}

































