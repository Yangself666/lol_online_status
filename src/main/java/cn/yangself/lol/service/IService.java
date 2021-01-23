package cn.yangself.lol.service;

/**
 * @author yangself
 */
public interface IService {

    /**
     * 设置Token
     * @param token
     */
    public void setToken(String token);

    /**
     * 开启关闭服务
     * @param enable
     */
    public void setEnable(Boolean enable);

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
