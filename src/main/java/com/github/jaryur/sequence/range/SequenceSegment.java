package com.github.jaryur.sequence.range;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class SequenceSegment {

    private String sequenceName;

    private long segmentId;

    private long segmentUnit;

    private long segmentMin = 1;

    private long segmentMax = Integer.MAX_VALUE;

    public SequenceSegment(String sequenceName, long segmentId, long segmentUnit, long segmentMin, long segmentMax) {
        this.sequenceName = sequenceName;
        this.segmentId = segmentId;
        this.segmentUnit = segmentUnit;
        this.segmentMin = segmentMin;
        this.segmentMax = segmentMax;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(int segmentId) {
        this.segmentId = segmentId;
    }

    public long getSegmentUnit() {
        return segmentUnit;
    }

    public void setSegmentUnit(int segmentUnit) {
        this.segmentUnit = segmentUnit;
    }

    public long getSegmentMin() {
        return segmentMin;
    }

    public void setSegmentMin(int segmentMin) {
        this.segmentMin = segmentMin;
    }

    public long getSegmentMax() {
        return segmentMax;
    }

    public void setSegmentMax(int segmentMax) {
        this.segmentMax = segmentMax;
    }


    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public String toString() {
        return "sequenceName:" + sequenceName + ",segmentId:" + segmentId + ",segmentUnit:" + segmentUnit +
                ",segmentMin:" + segmentMin + ",segmentMax:" + segmentMax;
    }
}
