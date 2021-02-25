package cn.yangself.lol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeightLog {
    @TableId
    private Integer weightLogId;
    private BigDecimal weightValue;
    private Date updateTime;
}
