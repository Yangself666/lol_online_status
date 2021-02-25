package cn.yangself.lol.service.impl;

import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.entity.WeightLog;
import cn.yangself.lol.mapper.SystemConfigMapper;
import cn.yangself.lol.mapper.WeightLogMapper;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.IWeightLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WeightLogServiceImpl extends ServiceImpl<WeightLogMapper, WeightLog> implements IWeightLogService {
    /**
     * 处理体重的信息
     *
     * @param content
     */
    @Override
    public String messageHandle(String content) {
        //默认记录今天的体重，然后计算出与第一天的对比 与昨天的对比
        /*
        体重已记录！今日减脂第x天！
        累计减重x.xxkg，昨日减重x.xxkg。
        近五天减重记录表
        2.22 95.30KG ↓12
        2.23 95.12KG ↑10
        2.23 95.12KG -10
        距离结束还有xx天，坚持就是胜利！
         */
        return null;
    }

}
