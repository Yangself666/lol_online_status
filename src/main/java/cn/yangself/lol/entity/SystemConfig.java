package cn.yangself.lol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置项类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    /**
     * 配置项主键
     */
    @TableId
    private Integer configId;
    /**
     * 配置项Key
     */
    private String configKey;
    /**
     * 配置项value
     */
    private String configValue;
}
