package com.github.jaryur.sequence;

import com.github.jaryur.sequence.configuration.SequenceSpecConfig;
import com.github.jaryur.sequence.exception.SequenceException;
import com.github.jaryur.sequence.support.SequenceConstants;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jaryur
 *
 * Comment:
 */

public class MysqlSequenceGenerator implements SequenceGenerator {

    private String application;

    private final String splitChar = ".";

    private DataSource dataSource;

    private List<String> sequenceNames;

    private Map<String, Sequence> sequenceCacheMap = new ConcurrentHashMap<>();

    private Map<String, SequenceSpecConfig> sequenceSpecConfigMap;

    private int step;

    private int cacheNSteps;

    private int skip;

    public MysqlSequenceGenerator(DataSource dataSource, String application, List<String> sequenceNames, int step, int
            cacheNSteps, int skip, Map<String, SequenceSpecConfig> sequenceSpecConfigMap) {
        if (skip < 0) {
            throw new RuntimeException("illegal sequence config:skip=" + skip + ", must be positive");
        }

        if (cacheNSteps <= 0) {
            throw new RuntimeException("illegal sequence config:cacheNSteps=" + cacheNSteps + ", must be greater than 0");
        }
        this.application = application;
        this.dataSource = dataSource;
        this.sequenceNames = sequenceNames;
        this.step = step;
        this.skip = skip;
        this.cacheNSteps = cacheNSteps;
        this.sequenceSpecConfigMap = sequenceSpecConfigMap;
        init();
    }

    @Override
    public int getNextInt(String sequenceName) {
        return getNextInt(sequenceName, SequenceConstants.DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public long getNextLong(String sequenceName) {
        return getNextLong(sequenceName, SequenceConstants.DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public int getNextInt(String sequenceName, long timeout, TimeUnit timeUnit) {
        long value = getNextLong(sequenceName, timeout, timeUnit);
        if (value > Integer.MAX_VALUE) {
            throw new RuntimeException("sequence value exceeds max int:" + Integer.MAX_VALUE);
        }
        return (int) value;
    }


    public void init() {
        if (dataSource == null) {
            throw new SequenceException("datasource cannot be null");
        }
        if (sequenceNames == null || sequenceNames.size() == 0) {
            throw new SequenceException("sequenceNames config cannot be null");
        }

        for (String sequenceName : sequenceNames) {
            if (sequenceName != null && sequenceName.length() > 0) {
                SequenceSpecConfig specConfig = sequenceSpecConfigMap.get(sequenceName);
                SequenceDatasource datasource = new SequenceDatasource(dataSource, sequenceNames);
                Sequence sequence = specConfig == null ? new MySQLSequence(datasource, application, sequenceName,
                        step, cacheNSteps, skip)
                        : new MySQLSequence(datasource, application, sequenceName, specConfig.getStep(), specConfig
                        .getCacheNSteps(), skip);
                sequenceCacheMap.putIfAbsent(getKey(sequenceName), sequence);
            }
        }
    }

    @Override
    public long getNextLong(String sequenceName, long timeout, TimeUnit timeUnit) {
        return getSequence(sequenceName).next(timeout, timeUnit);
    }

    @Override
    public SequenceRange getRange(String sequenceName, long count) {
        return getRange(sequenceName, count, SequenceConstants.DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public SequenceRange getRange(String sequenceName, long count, long timeout, TimeUnit timeUnit) {
        return getSequence(sequenceName).getRange(count, timeout, timeUnit);
    }

    private Sequence getSequence(String sequenceName) {
        Sequence sequence = sequenceCacheMap.get(getKey(sequenceName));
        if (sequence == null) {
            SequenceSpecConfig specConfig = sequenceSpecConfigMap.get(sequenceName);
            SequenceDatasource datasource = new SequenceDatasource(dataSource, sequenceNames);
            sequence = specConfig == null ? new MySQLSequence(datasource, application, sequenceName, step,
                    cacheNSteps, skip)
                    : new MySQLSequence(datasource, application, sequenceName, specConfig.getStep(), specConfig
                    .getCacheNSteps(), skip);
            sequenceCacheMap.putIfAbsent(sequenceName, sequence);
        }
        return sequenceCacheMap.get(getKey(sequenceName));
    }

    public String getKey(String sequenceName) {
        return String.join(splitChar, application, sequenceName);
    }

}
