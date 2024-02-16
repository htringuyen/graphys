package io.graphys.wfdb.player;

public interface FrameChunk {
    int getStartSampleTime();

    int getSize();

    int[][] getFrames();

    int[] getFrame(int order);

    boolean isFilledUp();

    void waitUntilFilledUp();

    void fillUp(int[][] frames);
}
