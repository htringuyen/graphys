package io.graphys.wfdb.player;

public interface RecordPlayer extends AutoCloseable {
    void start();

    FrameChunk playNext(int nFrame);

    String getId();

    int getSampleTime();
}
