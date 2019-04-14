package com.github.jaryur.sequence.support;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class SequenceConstants {

    public static final String DEFAULT_APPLICATION_NAME = "default-application";

    public static final String DEFAULT_SEQUENCE_NAME = "default_sequence";

    public static final int DEFAULT_RETRY_TIMES = 5;

    public static final int DEFAULT_TIMEOUT_SECONDS = 3;

    public static final int DEFAULT_STEP = 300;

    public static final int DEFAULT_CACHEN_STEP = 1;

    public static final String[] columns = new String[]{"id", "sequence_name", "step", "min", "max", "current_segment",
            "version"};

    public static final String sequenceTableName = "sequence";

    public static final String segmentTableName = "segment";

    public static final String DEFAULT_ZONE = "default_zone";

}
