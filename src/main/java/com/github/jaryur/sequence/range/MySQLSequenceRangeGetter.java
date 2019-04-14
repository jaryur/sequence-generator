package com.github.jaryur.sequence.range;

import com.github.jaryur.sequence.SequenceDatasource;
import com.github.jaryur.sequence.exception.InvalidSequenceException;
import com.github.jaryur.sequence.exception.SequenceConfigException;
import com.github.jaryur.sequence.exception.SequenceException;
import com.github.jaryur.sequence.exception.SequenceModifiedException;
import com.github.jaryur.sequence.support.JDBCSupport;
import com.github.jaryur.sequence.support.SequenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class MySQLSequenceRangeGetter implements InnerSequenceRangeGetter {

    private Logger logger = LoggerFactory.getLogger(MySQLSequenceRangeGetter.class);

    private String segmentQuerySQL = "select id,`min`,`max`,`current`,current_segment,version from %s where sequence_name=" +
            "? and application=? for update";

    private String sequenceQuerySQL = "select id,step,`min`,`max`,current_segment,segment_unit,version from %s where " +
            "sequence_name= ? and application=?  for update";

    private String sequenceUpdateSQL = "update %s set current_segment =?,version=version+1  where id=? and version = ?";

    private String nextSegmentRangeUpdateSQL = "update %s set `current` = ?, version=version +1 " +
            " where id= ? and `current` = ? and version = ?";

    private String nextSegmentUpdateSQL = "update %s set `min`=? ,`max` =?,`current` = ?,current_segment=?,version=version +1 " +
            " where id= ? and `current` = ? and version = ?";

    private String segmentInsertSQL = "insert into %s ( `min`, `max`, `current`, `current_segment`,`sequence_name`," +
            "`application`) values (?, ?, ?, ?, ?, ?);";

    private SequenceDatasource dataSource;

    private static volatile MySQLSequenceRangeGetter instance;

    private LongAdder totalConnCostAdder = new LongAdder();

    private LongAdder connCountAdder = new LongAdder();

    private static volatile boolean initilized = false;

    {
        segmentQuerySQL = String.format(this.segmentQuerySQL, SequenceConstants.segmentTableName);
        sequenceQuerySQL = String.format(this.sequenceQuerySQL, SequenceConstants.sequenceTableName);
        nextSegmentRangeUpdateSQL = String.format(this.nextSegmentRangeUpdateSQL, SequenceConstants.segmentTableName);
        nextSegmentUpdateSQL = String.format(this.nextSegmentUpdateSQL, SequenceConstants.segmentTableName);
        segmentInsertSQL = String.format(this.segmentInsertSQL, SequenceConstants.segmentTableName);
        sequenceUpdateSQL = String.format(this.sequenceUpdateSQL, SequenceConstants.sequenceTableName);
    }

    public MySQLSequenceRangeGetter(SequenceDatasource dataSource) {
        this.dataSource = dataSource;
    }

    public static MySQLSequenceRangeGetter getInstance(SequenceDatasource dataSource) {
        if (instance == null) {
            synchronized (InnerSequenceRangeGetter.class) {
                if (instance == null) {
                    instance = new MySQLSequenceRangeGetter(dataSource);
                    instance.init();
                    initilized = true;
                }
            }
        }
        return instance;
    }

    private void init() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            checkTableSchema(connection);
        } catch (SQLException e) {
            throw new SequenceConfigException("fail to establish mysql connection,reason:" + e.getMessage(), e);
        } finally {
            JDBCSupport.closeConnection(connection);
        }
    }

    private void checkTableSchema(Connection connection) {
        boolean isNewConnection = false;
        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                isNewConnection = true;
            }
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columns = databaseMetaData.getColumns(null, null, SequenceConstants.sequenceTableName, null);
            List<String> tableColumnList = new ArrayList<>();
            while (columns.next()) {
                String column = columns.getString("COLUMN_NAME");
                tableColumnList.add(column);
            }
            for (String col : SequenceConstants.columns) {
                if (!tableColumnList.contains(col)) {
                    logger.error("sequence table column not found:" + col);
                    throw new SequenceConfigException("column not found:" + col + ", please check sequence table schema:" +
                            SequenceConstants.sequenceTableName);
                }
            }

        } catch (SQLException e) {
            throw new SequenceConfigException("unknown sql exception:" + e.getMessage(), e);
        } finally {
            if (isNewConnection) {
                JDBCSupport.closeConnection(connection);
            }
        }
    }


    @Override
    public InnerSequenceRange getNextRange(String application, String sequenceName, long step) {
        if (initilized == false) {
            this.init();
        }
        InnerSequenceRange sequenceRange = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        long start = System.currentTimeMillis();

        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
            }

            /**
             * 查询sequence和segment信息
             */
            selectStatement = connection.prepareStatement(sequenceQuerySQL);
            selectStatement.setString(1, sequenceName);
            selectStatement.setString(2, application);
            ResultSet selectResult = selectStatement.executeQuery();
            long sequenceId;
            long dbStep;
            long sequenceMin;
            long sequenceMax;
            long currentSequenceSegmentNo;
            long sequenceVersion;
            long segmentUnit;
            if (selectResult.next()) {
                sequenceId = selectResult.getLong(1);
                dbStep = selectResult.getLong(2);
                sequenceMin = selectResult.getLong(3);
                sequenceMax = selectResult.getLong(4);
                currentSequenceSegmentNo = selectResult.getLong(5);
                segmentUnit = selectResult.getLong(6);
                sequenceVersion = selectResult.getLong(7);
            } else {
                throw new InvalidSequenceException("No available sequence range found for application:" + application + "," +
                        "schemaName:'" + sequenceName + "', sequenceTableName:'" + sequenceName + "'");
            }
            if (sequenceMin >= sequenceMax) {
                throw new SequenceException("illegal sequence config,min value:" + sequenceMin + " must be smaller than" +
                        " max value:" + sequenceMax);
            }
            selectStatement = connection.prepareStatement(segmentQuerySQL);
            selectStatement.setString(1, sequenceName);
            selectStatement.setString(2, application);
            selectResult = selectStatement.executeQuery();
            long segmentId;
            long segmentMin;
            long segmentMax;
            long currentSegmentValue;
            long currentSegmentNo;
            long segmentVersion;
            if (selectResult.next()) {
                segmentId = selectResult.getLong(1);
                segmentMin = selectResult.getLong(2);
                segmentMax = selectResult.getLong(3);
                currentSegmentValue = selectResult.getLong(4);
                currentSegmentNo = selectResult.getLong(5);
                segmentVersion = selectResult.getLong(6);
            } else {
                //初始化Segment
                long tempSegmentMin = sequenceMin;
                long tempSegmentMax = tempSegmentMin + segmentUnit - 1;
                //特殊处理
                long tempSegmentCurrentValue = tempSegmentMin - 1;
                PreparedStatement insertStatement = connection.prepareStatement(segmentInsertSQL);
                insertStatement.setLong(1, tempSegmentMin);
                insertStatement.setLong(2, tempSegmentMax);
                insertStatement.setLong(3, tempSegmentCurrentValue);
                insertStatement.setLong(4, currentSequenceSegmentNo);
                insertStatement.setString(5, sequenceName);
                insertStatement.setString(6, application);
                insertStatement.execute();
                connection.commit();
                return null;
            }
            /**
             * 是否需要修正segment min和max数据（人为修改了sequence的min和max）
             */
            if (segmentMin < sequenceMin) {
                long newSegmentNo = currentSegmentNo + 1;
                long newSegmentCurrentValue = sequenceMin - 1;
                long tempSegmentMin = sequenceMin;
                long tempSegmentMax;
                tempSegmentMax = Math.max(sequenceMin, segmentMax);
                updateStatement = connection.prepareStatement(sequenceUpdateSQL);
                updateStatement.setLong(1, newSegmentNo);
                updateStatement.setLong(2, sequenceId);
                updateStatement.setLong(3, sequenceVersion);
                int tempSequenceRow = updateStatement.executeUpdate();
                if (tempSequenceRow <= 0) {
                    connection.rollback();
                    return null;
                }
                updateStatement = connection.prepareStatement(nextSegmentUpdateSQL);
                updateStatement.setLong(1, tempSegmentMin);
                updateStatement.setLong(2, tempSegmentMax);
                updateStatement.setLong(3, newSegmentCurrentValue);
                updateStatement.setLong(4, newSegmentNo);
                updateStatement.setLong(5, segmentId);
                updateStatement.setLong(6, currentSegmentValue);
                updateStatement.setLong(7, segmentVersion);
                int tempSegmentRow = updateStatement.executeUpdate();
                if (tempSegmentRow <= 0) {
                    connection.rollback();
                    return null;
                } else {
                    connection.commit();
                    throw new SequenceModifiedException();
                }
            }
            if (segmentMax > sequenceMax) {
                long newSegmentNo = currentSegmentNo + 1;
                long newSegmentCurrentValue = currentSegmentValue;
                long tempSegmentMin = segmentMin;
                long tempSegmentMax = Math.min(sequenceMax, segmentMax);
                updateStatement = connection.prepareStatement(nextSegmentUpdateSQL);
                updateStatement.setLong(1, tempSegmentMin);
                updateStatement.setLong(2, tempSegmentMax);
                updateStatement.setLong(3, newSegmentCurrentValue);
                updateStatement.setLong(4, newSegmentNo);
                updateStatement.setLong(5, segmentId);
                updateStatement.setLong(6, currentSegmentValue);
                updateStatement.setLong(7, segmentVersion);
                int tempSegmentRow = updateStatement.executeUpdate();
                if (tempSegmentRow <= 0) {
                    connection.rollback();
                    return null;
                } else {
                    connection.commit();
                    throw new SequenceModifiedException();
                }
            }

            /**
             * 修正segmentNo
             */
            if (currentSegmentNo != currentSequenceSegmentNo) {
                long fixedSegmentNo = Math.max(currentSegmentNo, currentSequenceSegmentNo);
                if (currentSegmentNo < currentSequenceSegmentNo) {
                    updateStatement = connection.prepareStatement(nextSegmentUpdateSQL);
                    updateStatement.setLong(1, segmentMin);
                    updateStatement.setLong(2, segmentMax);
                    updateStatement.setLong(3, currentSegmentValue);
                    updateStatement.setLong(4, fixedSegmentNo);
                    updateStatement.setLong(5, segmentId);
                    updateStatement.setLong(6, currentSegmentValue);
                    updateStatement.setLong(7, segmentVersion);
                    int tempSegmentRow = updateStatement.executeUpdate();
                    if (tempSegmentRow <= 0) {
                        connection.rollback();
                    } else {
                        connection.commit();
                    }
                } else {
                    updateStatement = connection.prepareStatement(sequenceUpdateSQL);
                    updateStatement.setLong(1, fixedSegmentNo);
                    updateStatement.setLong(2, sequenceId);
                    updateStatement.setLong(3, sequenceVersion);
                    int tempSequenceRow = updateStatement.executeUpdate();
                    if (tempSequenceRow <= 0) {
                        connection.rollback();
                    }
                }
                return null;
            }


            //  自定义步长优先
            if (step <= 0) {
                step = dbStep;
            }
            if (currentSegmentValue >= sequenceMax) {
                throw new RuntimeException("sequence:" + sequenceName + " used up,max:" + sequenceMax + ", current:"
                        + currentSegmentValue);
            }

            /**
             * 获取下一个range
             */
            segmentMin = currentSegmentValue + 1;
            long rangeBegin;
            long rangeEnd;
            //segment剩余range不够用，需要跨段
            if (currentSegmentValue + step > segmentMax) {
                long newSegmentNo = currentSegmentNo + 1;
                rangeBegin = currentSegmentValue + 1;
                rangeEnd = currentSegmentValue + step;
                long newCurrentValue = rangeEnd;
                segmentMin = Math.max(sequenceMin, currentSegmentValue + 1);
                segmentMax = Math.min(sequenceMax, currentSegmentValue + segmentUnit);
                updateStatement = connection.prepareStatement(nextSegmentUpdateSQL);
                updateStatement.setLong(1, segmentMin);
                updateStatement.setLong(2, segmentMax);
                updateStatement.setLong(3, newCurrentValue);
                updateStatement.setLong(4, newSegmentNo);
                updateStatement.setLong(5, segmentId);
                updateStatement.setLong(6, currentSegmentValue);
                updateStatement.setLong(7, segmentVersion);
                int row = updateStatement.executeUpdate();
                if (row < 1) {
                    logger.info("");
                    connection.rollback();
                    return null;
                }
                updateStatement = connection.prepareStatement(sequenceUpdateSQL);
                updateStatement.setLong(1, newSegmentNo);
                updateStatement.setLong(2, sequenceId);
                updateStatement.setLong(3, sequenceVersion);
                row = updateStatement.executeUpdate();
                if (row > 0) {
                    connection.commit();
                    sequenceRange = new InnerSequenceRange(new SequenceSegment(sequenceName, segmentId, segmentUnit,
                            segmentMin, segmentMax), rangeBegin, rangeEnd, step);
                    if (logger.isInfoEnabled()) {
                        logger.info("[Get_Range]: " + sequenceRange);
                    }
                    return sequenceRange;
                } else {
                    connection.rollback();
                    return null;
                }
            } else {
                rangeBegin = currentSegmentValue + 1;
                rangeEnd = currentSegmentValue + step;
                long newSegmentCurrentValue = rangeEnd;
                updateStatement = connection.prepareStatement(nextSegmentRangeUpdateSQL);
                updateStatement.setLong(1, newSegmentCurrentValue);
                updateStatement.setLong(2, segmentId);
                updateStatement.setLong(3, currentSegmentValue);
                updateStatement.setLong(4, segmentVersion);
                int row = updateStatement.executeUpdate();
                if (row > 0) {
                    connection.commit();
                    sequenceRange = new InnerSequenceRange(new SequenceSegment(sequenceName, segmentId, segmentUnit,
                            segmentMin, segmentMax),
                            rangeBegin, rangeEnd, step);
                    if (logger.isInfoEnabled()) {
                        logger.info("[Get_Range]: " + sequenceRange);
                    }
                    return sequenceRange;
                } else {
                    connection.rollback();
                    return null;
                }

            }

        } catch (SequenceModifiedException e) {
            throw e;
        } catch (Exception e) {
            String err = "fail to invoke method getNextRange, sequence name:" + sequenceName +
                    ", reason:" + e.getMessage();
            logger.error(err, e);
            throw new SequenceException(err, e);
        } finally {
            connCountAdder.increment();
            totalConnCostAdder.add(System.currentTimeMillis() - start);
            logger.info("mysql connection stats:total establishment count:{},total ms cost:{}" +
                            ",avg cost:{}", connCountAdder.longValue(), totalConnCostAdder.longValue(),
                    totalConnCostAdder.longValue() / connCountAdder.longValue());
            if (connCountAdder.longValue() > 10000) {
                connCountAdder.reset();
                totalConnCostAdder.reset();
            }
            JDBCSupport.closeStatement(selectStatement);
            JDBCSupport.closeStatement(updateStatement);
            JDBCSupport.closeConnection(connection);
        }
    }

}
