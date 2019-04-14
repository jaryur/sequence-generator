package com.github.jaryur.sequence.range;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public interface InnerSequenceRangeGetter {

    InnerSequenceRange getNextRange(String application, String sequenceName, long step);

}
