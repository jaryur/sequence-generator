package com.github.jaryur.sequence;

/**
 * Created by Jaryur
 *
 * Comment:单个sequence的生产类
 */
public interface SingleSequenceGenerator {

    /**
     * 获取下一个int类型序号
     *
     * @return int类型序号
     */
    int getNextInt();


    /**
     * 获取下一个long类型序号
     *
     * @return long类型序号
     */
    long getNextLong();


}
