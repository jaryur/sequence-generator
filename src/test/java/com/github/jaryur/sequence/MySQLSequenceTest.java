package com.github.jaryur.sequence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MySQLSequenceTest {
    String SequanceName = "test_sequence";

    @Autowired
    private MysqlSequenceGenerator mysqlSequenceGenerator;

    @Test
    public void next() {
    }

    @Test
    public void test_getNextInt() {
        for (int i = 0; i < 100; i++) {
            int cur = mysqlSequenceGenerator.getNextInt(SequanceName, 1, TimeUnit.SECONDS);
            int next = mysqlSequenceGenerator.getNextInt(SequanceName, 1, TimeUnit.SECONDS);
            Assert.assertTrue(next - cur == 1);

        }
    }

    @Test
    public void test_getRange() {
        int count = 99;
        SequenceRange range = mysqlSequenceGenerator.getRange(SequanceName, count, 1, TimeUnit.SECONDS);
        Assert.assertTrue(range.getMax() - range.getMin() == count - 1);
    }

    @Test
    public void test_getRangeNext() {
        int count = 4;
        SequenceRange range = mysqlSequenceGenerator.getRange(SequanceName, count, 1, TimeUnit.SECONDS);
        for (int i = 0; i < count; i++) {
            Assert.assertTrue(range.getNext() - range.getMin() == i);
        }
    }
}