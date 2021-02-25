package cn.yangself.lol.service;

import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.entity.WeightLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IWeightLogService extends IService<WeightLog> {
    /**
     * 处理体重的信息
     * @param content
     */
    String messageHandle(String content);
}
