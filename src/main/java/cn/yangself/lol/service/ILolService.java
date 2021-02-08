package cn.yangself.lol.service;

import cn.yangself.lol.entity.Lol;
import cn.yangself.lol.entity.Steam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 获取Steam好友的动态
     * @return
     */
    List<Steam> steamGetPlayerSummaries();
}
