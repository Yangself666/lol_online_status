package cn.yangself.lol.service.impl;

import cn.yangself.lol.service.IWeightLogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;


@SpringBootTest
@RunWith(SpringRunner.class)
public class WeightLogServiceImplTest {
    @Autowired
    private IWeightLogService weightLogService;

    @Test
    public void testMessageHandle() {
        // weightLogService.messageHandle("sdfsd2.21打卡22.50fsdf");
        weightLogService.messageHandle("sdfsd打卡88.66fsdf");
    }

    @Test
    public void testWeightStatistics() {
        String statistics = weightLogService.weightStatistics();
        System.out.println("statistics = " + statistics);
    }
    
    @Test
    public void decimalCompare() {
        System.out.println("compare = " + new BigDecimal(90.0).compareTo(new BigDecimal(89.5))); // > 1
        System.out.println("compare = " + new BigDecimal(90.0).compareTo(new BigDecimal(90.0))); // = 0
        System.out.println("compare = " + new BigDecimal(90.0).compareTo(new BigDecimal(90.1))); // < -1
    }
}