package com.github.jaryur.sequence.configuration;

import com.github.jaryur.sequence.support.SequenceConstants;

/**
 * Created by Jaryur
 *
 * Comment:Sequence Specific Config
 */
public class SequenceSpecConfig {

    private int step = SequenceConstants.DEFAULT_STEP;

    private int cacheNSteps = SequenceConstants.DEFAULT_CACHEN_STEP;

    public SequenceSpecConfig(int step, int cacheNSteps) {
        if (step < 1) {
            throw new IllegalArgumentException("step should be greater than 0");
        }
        if (cacheNSteps < 1) {
            throw new IllegalArgumentException("cacheNSteps should be greater than 0");
        }
        this.step = step;
        this.cacheNSteps = cacheNSteps;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getCacheNSteps() {
        return cacheNSteps;
    }

    public void setCacheNSteps(int cacheNSteps) {
        this.cacheNSteps = cacheNSteps;
    }
}
