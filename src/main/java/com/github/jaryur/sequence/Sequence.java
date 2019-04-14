package com.github.jaryur.sequence;

import java.util.concurrent.TimeUnit;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public interface Sequence {

    void init();

    void destory();

    long next(long timeout, TimeUnit timeUnit);

    SequenceRange getRange(long count, long timeout, TimeUnit timeUnit);


}
