package com.github.jaryur.sequence;

/**
 * Created by Jaryur
 * Comment:
 */
public class DefaultProperties {

    private String application = "default-application";

    private int defaultCacheNum = 1;

    private int defaultStep = 30;

    private boolean lazyMode = false;


    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public int getDefaultCacheNum() {
        return defaultCacheNum;
    }

    public void setDefaultCacheNum(int defaultCacheNum) {
        this.defaultCacheNum = defaultCacheNum;
    }

    public int getDefaultStep() {
        return defaultStep;
    }

    public void setDefaultStep(int defaultStep) {
        this.defaultStep = defaultStep;
    }

    public boolean isLazyMode() {
        return lazyMode;
    }

    public void setLazyMode(boolean lazyMode) {
        this.lazyMode = lazyMode;
    }
}

