package cn.yangself.lol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
}
