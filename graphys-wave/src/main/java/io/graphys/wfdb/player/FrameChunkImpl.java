package io.graphys.wfdb.player;

import lombok.NonNull;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FrameChunkImpl implements FrameChunk {
    private int startSampleTime;

    private int size;

    private int[][] frames;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition filledUp = lock.newCondition();

    FrameChunkImpl(int startSampleTime, int size) {
        this.startSampleTime = startSampleTime;
        this.size = size;
    }

    @Override
    public int getStartSampleTime() {
        return startSampleTime;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int[][] getFrames() {
        return frames;
    }

    @Override
    public int[] getFrame(int order) {
        return frames[order];
    }

    @Override
    public boolean isFilledUp() {
        lock.lock();

        try {
            return frames != null && frames.length == size;
        }
        finally {
            lock.unlock();
        }
    }


    @Override
    public void fillUp(@NonNull int[][] toFillFrames) {
        if (toFillFrames == null || toFillFrames.length != size) {
            throw new IllegalArgumentException("The filling frames must be non null and of length " + size);
        }

        if (isFilledUp()) {
             throw new IllegalStateException("This frame chunk have been filled");
        }

        lock.lock();
        try {
            this.frames = toFillFrames;
            filledUp.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void waitUntilFilledUp() {
        lock.lock();
        try {
            while (! isFilledUp()) {
                filledUp.await();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }
}
