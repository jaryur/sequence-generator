package com.github.jaryur.sequence;

import com.github.jaryur.sequence.exception.SequenceException;

/**
 * Created by Jaryur
 *
 * Comment:sequence段
 */

public class SequenceRange {

    private long min;

    private long current;

    private long max;

    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.current = min;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    /**
     * 获取下一个sequence值
     * <p>
     * WARNING:非线程安全
     *
     * @return
     */
    public long getNext() {
        if (current > max) {
            throw new SequenceException("sequence range is used up,min:" + min + ",max:" + max + ",current:" + current);
        }
        return current++;
    }
}
