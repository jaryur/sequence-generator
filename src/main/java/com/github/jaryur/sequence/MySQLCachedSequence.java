package com.github.jaryur.sequence;

import com.github.jaryur.sequence.exception.InvalidSequenceException;
import com.github.jaryur.sequence.exception.SequenceException;
import com.github.jaryur.sequence.exception.SequenceModifiedException;
import com.github.jaryur.sequence.range.InnerSequenceRange;
import com.github.jaryur.sequence.range.MySQLSequenceRangeGetter;
import com.github.jaryur.sequence.support.SequenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class MySQLCachedSequence implements Sequence {

    private Logger logger = LoggerFactory.getLogger(MySQLCachedSequence.class);

    private volatile boolean initialized = false;

    private SequenceDatasource dataSource;

    private ReentrantLock rangeLock = new ReentrantLock();

    private String application;

    private String sequenceName;

    private int step;

    private int skip;

    private volatile InnerSequenceRange sequenceRange;

    private BlockingQueue<InnerSequenceRange> blockingQueue = null;

    public void startProducer() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    if (blockingQueue.remainingCapacity() <= 0) {
                        Thread.sleep(5);
                        continue;
                    }
                    InnerSequenceRange range = MySQLSequenceRangeGetter.getInstance(dataSource).getNextRange
                            (application, sequenceName, step);
                    if (range != null) {
                        blockingQueue.put(range);
                    }
                } catch (SequenceModifiedException e) {
                    logger.info("clear sequence range queue,name:{},current size:{}", sequenceName, blockingQueue.size());
                    blockingQueue.clear();
                } catch (InvalidSequenceException e) {
                    Thread.sleep(1000);
                    logger.error(e.getMessage());
                } catch (Exception e) {
                    logger.error("sequence range producer failed", e);
                }
            }

        });
    }


    public MySQLCachedSequence(SequenceDatasource dataSource, String application, String sequenceName, int step, int
            cacheNSteps, int skip) {
        this.dataSource = dataSource;
        this.application = application;
        this.sequenceName = sequenceName;
        this.step = step;
        this.skip = skip;
        blockingQueue = new LinkedBlockingQueue<>(cacheNSteps);
        init();
    }


    @Override
    public void init() {
        if (initialized == false) {
            synchronized (this) {
                if (initialized == false) {
                    startProducer();
                }
                initialized = true;
            }
        }
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
            boolean flag = rangeLock.tryLock(3, TimeUnit.SECONDS);
            if (flag) {
                try {
                    if (sequenceRange != null && !sequenceRange.needRetrieve()) {
                        return;
                    }
                    for (int i = 0; i < SequenceConstants.DEFAULT_RETRY_TIMES; i++) {
                        if (blockingQueue.size() != 0) {
                            sequenceRange = blockingQueue.poll(50L, TimeUnit.MILLISECONDS);
                            if (sequenceRange != null) {
                                break;
                            }
                        } else {
                            Thread.sleep(200);
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