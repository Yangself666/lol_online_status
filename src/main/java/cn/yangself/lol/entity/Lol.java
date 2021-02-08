package cn.yangself.lol.entity;

import com.baomidou.mybatisplus.annotation.TableId;

public class Lol {
    /**
     * 数据库主键
     */
    @TableId
    private Integer lolId;
    /**
     * lol账号ID
     */
    private Integer accountId;
    /**
     * 在线状态
     */
    private Boolean onlineStatus;
    /**
     * 上线提示
     */
    private String onlineMessage;
    /**
     * 离线提示
     */
    private String offlineMessage;

    public Lol() {
    }

    public Lol(Integer lolId, Integer accountId, Boolean onlineStatus, String onlineMessage, String offlineMessage) {
        this.lolId = lolId;
        this.accountId = accountId;
        this.onlineStatus = onlineStatus;
        this.onlineMessage = onlineMessage;
        this.offlineMessage = offlineMessage;
    }

    public Integer getLolId() {
        return lolId;
    }

    public void setLolId(Integer lolId) {
        this.lolId = lolId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Boolean getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getOnlineMessage() {
        return onlineMessage;
    }

    public void setOnlineMessage(String onlineMessage) {
        this.onlineMessage = onlineMessage;
    }

    public String getOfflineMessage() {
        return offlineMessage;
    }

    public void setOfflineMessage(String offlineMessage) {
        this.offlineMessage = offlineMessage;
    }

    @Override
    public String toString() {
        return "Lol{" +
                "lolId=" + lolId +
                ", accountId=" + accountId +
                ", onlineStatus=" + onlineStatus +
                ", onlineMessage='" + onlineMessage + '\'' +
                ", offlineMessage='" + offlineMessage + '\'' +
                '}';
    }
}
