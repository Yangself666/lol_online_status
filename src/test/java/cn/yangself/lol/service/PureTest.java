package cn.yangself.lol.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.junit.Test;

public class PureTest {
    @Test
    public void DateDelta() {
        DateTime dateTime = DateUtil.parseDate("2021.2.22");
        DateTime dateTime2 = DateUtil.parseDate("2021.2.28");
        long between = DateUtil.between(dateTime, dateTime2, DateUnit.DAY);
        System.out.println("(between+1) = " + (between+1));
    }
}
