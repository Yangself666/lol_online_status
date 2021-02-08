package cn.yangself.lol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Steam {
    /**
     * Steam数据库主键
     */
    @TableId
    private Integer steamId;
    /**
     * Steam64位ID
     */
    private String accountId;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * Steam昵称
     */
    private String playerName;
    /**
     * 正在玩的游戏名称
     */
    private String playingGame;
    /**
     * 上线发送消息
     */
    private String onlineMessage;
    /**
     * 离线发送消息
     */
    private String offlineMessage;
    /**
     * 在线状态
     */
    private Boolean onlineStatus;
}
