package com.github.jaryur.sequence.range;

import com.github.jaryur.sequence.exception.SequenceException;
import com.github.jaryur.sequence.support.SequenceConstants;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class InnerSequenceRange {


    private SequenceSegment segment;

    private long min;

    private long max;

    private long step = SequenceConstants.DEFAULT_STEP;

    /**
     * 当前可用
     */
    private AtomicLong current;

    public InnerSequenceRange(SequenceSegment sequenceSegment, long min, long max, long step) {
        this.segment = sequenceSegment;
        this.step = step;
        this.min = min;
        this.max = max;
        if (segment.getSegmentMax() > Long.MAX_VALUE) {
            throw new SequenceException("max sequence cannot exceed " + Long.MAX_VALUE);
        }
        current = new AtomicLong(min);
    }

    public long getNext(int skip) {
        long value = current.getAndAdd(skip + 1);
        if (value > max) {
            return -1;
        }
        return value;
    }

    public boolean needRetrieve() {
        return current.get() > max;
    }

    public long getMin() {
        return this.min;
    }

    public long getMax() {
        return this.max;
    }

    public long getStep() {
        return step;
    }

    public SequenceSegment getSegment() {
        return segment;
    }

    public void setSegment(SequenceSegment segment) {
        this.segment = segment;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public AtomicLong getCurrent() {
        return current;
    }

    public void setCurrent(AtomicLong current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "range min:" + min + ",max:" + max + ",step:" + step + ",current segment:[" + segment.toString() + "]";
    }

}
