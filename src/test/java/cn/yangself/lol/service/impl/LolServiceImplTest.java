package cn.yangself.lol.service.impl;

import cn.yangself.lol.entity.Lol;
import cn.yangself.lol.entity.Steam;
import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.entity.WeightLog;
import cn.yangself.lol.service.ILolService;
import cn.yangself.lol.service.ISteamService;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.IWeightLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LolServiceImplTest {

    @Autowired
    private ILolService lolService;

    @Autowired
    private ISystemConfigService configService;

    @Autowired
    private ISteamService steamService;

    @Autowired
    private IWeightLogService weightLogService;

    @Test
    public void lolServiceTest() {
        List<Lol> list = lolService.list();
        System.out.println("list = " + list);
    }

    @Test
    public void updateTest() {
        configService.update(new SystemConfig(){{
            setConfigValue("false");
        }},new UpdateWrapper<SystemConfig>(new SystemConfig(){{
            setConfigKey("ENABLE");
        }}));
    }

    @Test
    public void listTest() {
        Page<WeightLog> page = weightLogService.page(new Page<WeightLog>(1, 5), new QueryWrapper<WeightLog>().orderByDesc("update_time"));
        System.out.println("list = " + page.getRecords());
    }

    @Test
    public void steamList() {
        List<Steam> steams = lolService.steamGetPlayerSummaries();
        System.out.println("steams = " + steams);
    }
}