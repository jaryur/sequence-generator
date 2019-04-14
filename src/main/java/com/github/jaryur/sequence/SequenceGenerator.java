package com.github.jaryur.sequence;

import java.util.concurrent.TimeUnit;

/**
 * Created by Jaryur
 *
 * Comment:全局ID生成接口，整体自增
 */
public interface SequenceGenerator {


    /**
     * 获取下一个序号
     * 默认1s超时
     *
     * @param sequenceName 序列名
     * @return
     */
    int getNextInt(String sequenceName);

    /**
     * 获取下一个long类型序号
     * 默认1s超时
     *
     * @param sequenceName 序列名
     * @return
     */
    long getNextLong(String sequenceName);

    /**
     * 获取下一个序号，并指定超时时间
     *
     * @param sequenceName 序列名
     * @param timeout      超时时间
     * @param timeUnit     超时时间单位
     * @return
     */
    int getNextInt(String sequenceName, long timeout, TimeUnit timeUnit);

    /**
     * 获取下一个long类型序号
     *
     * @param sequenceName 序列名
     * @param timeout      超时时间
     * @param timeUnit     超时时间单位
     * @return
     */
    long getNextLong(String sequenceName, long timeout, TimeUnit timeUnit);


    /**
     * 获取sequence段，用于批量获取场景
     * @param sequenceName
     * @return
     */
    SequenceRange getRange(String sequenceName, long count);

    /**
     * 获取sequence段，用于批量获取场景
     * @param sequenceName
     * @param count
     * @param timeout
     * @param timeUnit
     * @return
     */
    SequenceRange getRange(String sequenceName, long count, long timeout, TimeUnit timeUnit);


}
