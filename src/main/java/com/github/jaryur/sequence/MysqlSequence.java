package com.github.jaryur.sequence;

import com.github.jaryur.sequence.exception.SequenceException;
import com.github.jaryur.sequence.range.InnerSequenceRange;
import com.github.jaryur.sequence.range.MySQLSequenceRangeGetter;
import com.github.jaryur.sequence.support.SequenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Jaryur
 * Comment:
 */
public class MysqlSequence implements Sequence {


    private Logger logger = LoggerFactory.getLogger(MySQLCachedSequence.class);

    private SequenceDatasource dataSource;

    private String application;

    private String sequenceName;

    private int step;

    private int skip;

    private volatile InnerSequenceRange sequenceRange;

    private ReentrantLock rangeLock = new ReentrantLock();


    public MysqlSequence(SequenceDatasource dataSource, String application, String sequenceName, int step, int skip) {
        this.dataSource = dataSource;
        this.application = application;
        this.sequenceName = sequenceName;
        this.step = step;
        this.skip = skip;
    }


    @Override
    public void init() {

    }


    @Override
    public long next(long timeout, TimeUnit timeUnit) {
        long timeoutMills = timeUnit.toMillis(timeout);
        long start = System.currentTimeMillis();
        long sequence = -1;
        while (true) {
            if (sequence != -1) {
                break;
            }
            if (System.currentTimeMillis() - start > timeoutMills) {
                throw new SequenceException("fail to get sequence range after trying " + timeoutMills + " mills, sequence" +
                        " name:" + sequenceName);
            }
            for (int i = 0; i < SequenceConstants.DEFAULT_RETRY_TIMES; i++) {
                if (sequenceRange == null || sequenceRange.needRetrieve()) {
                    resetSequenceRange();
                }
                if (sequenceRange != null) {
                    sequence = sequenceRange.getNext(skip);
                }
                if (sequence > 0) {
                    return sequence;
                }
            }
        }
        return sequence;
    }

    @Override
    public SequenceRange getRange(long count, long timeout, TimeUnit timeUnit) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < SequenceConstants.DEFAULT_RETRY_TIMES; i++) {
            InnerSequenceRange sequenceRange = MySQLSequenceRangeGetter.getInstance(dataSource).getNextRange(application, sequenceName, count);
            if (sequenceRange != null) {
                return new SequenceRange(sequenceRange.getMin(), sequenceRange.getMax());
            }
        }
        throw new SequenceException("fail to get sequence range after trying " + (System.currentTimeMillis() - start) +
                " mills, sequence name:" + sequenceName);

    }

    public void resetSequenceRange() {
        long start = System.nanoTime();
        try {
            boolean flag = rangeLock.tryLock(1, TimeUnit.SECONDS);
            if (flag) {
                try {
                    if (sequenceRange != null && !sequenceRange.needRetrieve()) {
                        return;
                    }
                    for (int i = 0; i < SequenceConstants.DEFAULT_RETRY_TIMES; i++) {
                        sequenceRange = MySQLSequenceRangeGetter.getInstance(dataSource).getNextRange
                                (application, sequenceName, step);
                        if (sequenceRange != null) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("resetSequenceRange failed", e);
                } finally {
                    rangeLock.unlock();
                    long cost = System.nanoTime() - start;
                    if (cost > 1000000) {
                        logger.warn("sequence monitor stats[ nano cost this time:{} ]", cost);
                    } else {
                        logger.info("sequence monitor stats[ nano cost this time:{}]", cost);
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.error("fail to get range lock", e);
        }


    }


    @Override
    public void destory() {

    }

}

