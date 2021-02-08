package cn.yangself.lol.service;

import cn.yangself.lol.entity.Lol;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author yangself
 */
public interface ILolService extends IService<Lol> {

    /**
     * 查询在线状态的定时任务
     */
    void checkStatus();

    /**
     * 发送钉钉消息
     * @param content
     */
    void sendMessage(String content);
}
