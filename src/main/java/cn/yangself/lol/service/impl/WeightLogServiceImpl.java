package cn.yangself.lol.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.entity.WeightLog;
import cn.yangself.lol.mapper.SystemConfigMapper;
import cn.yangself.lol.mapper.WeightLogMapper;
import cn.yangself.lol.service.ILolService;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.IWeightLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class WeightLogServiceImpl extends ServiceImpl<WeightLogMapper, WeightLog> implements IWeightLogService {
    private static final String NORMAL_CLOCK_IN_REGEX = "打卡\\d{2}\\.\\d{1,}";
    private static final String CLOCK_IN_WITH_DATE_REGEX = "\\d{1,2}\\.\\d{1,2}打卡\\d{2}\\.\\d{1,}";
    private static final String CLOCK_IN_STRING = "打卡";

    private ILolService lolService;

    @Autowired
    public void setLolService(ILolService lolService) {
        this.lolService = lolService;
    }

    /**
     * 处理体重的信息
     *
     * @param content
     */
    @Override
    public String messageHandle(String content) {
        String result = "";
        /*
        需要包含的内容
        1. 打卡90.50          ->  当天打卡90.5KG
        2. 1.24打卡90.50      ->  1.24打卡90.50KG
        3. 打卡记录            ->  播报列表
        4. 食谱               ->  发送今日食谱
        5. 打卡帮助            ->  返回系统帮助信息
         */

        if (ReUtil.contains(CLOCK_IN_WITH_DATE_REGEX, content)) {
            // 1.24打卡90.50
            String s = ReUtil.get(CLOCK_IN_WITH_DATE_REGEX, content, 0);
            String[] split = s.split(CLOCK_IN_STRING);
            DateTime date = DateUtil.parseDate("2021." + split[0]);
            //传入的体重数据
            BigDecimal weightValue = new BigDecimal(split[1]);
            //查询设置的那一天有没有数据
            WeightLog data = this.getOne(new QueryWrapper<WeightLog>().lambda().between(WeightLog::getUpdateTime, DateUtil.beginOfDay(date), DateUtil.endOfDay(date)), false);
            String pre = "";
            if (data == null) {
                //没有数据，新建数据
                this.save(new WeightLog() {{
                    setWeightValue(weightValue);
                    setUpdateTime(date);
                }});
                pre = split[0] + "体重已记录！\n";
            } else {
                //存在数据，更新数据
                data.setWeightValue(weightValue);
                this.updateById(data);
                pre = split[0] + "体重已更新！\n";
            }

            String statistics = weightStatistics();
            result = pre + statistics;

        } else if (ReUtil.contains(NORMAL_CLOCK_IN_REGEX, content)) {
            //今日的日期
            DateTime todayDate = DateUtil.date();
            //打卡90.50
            String s = ReUtil.get(NORMAL_CLOCK_IN_REGEX, content, 0);
            String[] split = s.split(CLOCK_IN_STRING);
            //获取到的体重
            BigDecimal weightValue = new BigDecimal(split[1]);
            //查询今天是否有数据
            WeightLog data = this.getOne(new QueryWrapper<WeightLog>().lambda().between(WeightLog::getUpdateTime, DateUtil.beginOfDay(todayDate), DateUtil.endOfDay(todayDate)), false);
            String pre = "";
            if (data == null) {
                //没有数据，新建数据
                this.save(new WeightLog() {{
                    setWeightValue(weightValue);
                    setUpdateTime(todayDate);
                }});
                pre = "今日体重已记录！\n";
            } else {
                //存在数据，更新数据
                data.setWeightValue(weightValue);
                this.updateById(data);
                pre = "今日体重已更新！\n";
            }
            String statistics = weightStatistics();
            result = pre + statistics;
        }else if(content.indexOf("打卡记录") > -1){
            result =  weightStatistics();
        }else{
            result = "发的啥玩意，能不能好好发，老子看不懂";
        }
        return result;
    }

    public String weightStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        //默认记录今天的体重，然后计算出与第一天的对比 与昨天的对比
        /*
        今日减脂第x天！
        累计减重x.xxkg，昨日减重x.xxkg。
        近五天减重记录表
        2.22 95.30KG ↓12
        2.23 95.12KG ↑10
        2.23 95.12KG -0
        距离结束还有xx天，坚持就是胜利！
         */

        //今日的日期
        DateTime todayDate = DateUtil.date();
        // 减肥第一天的数据
        WeightLog firstDay = this.getOne(new QueryWrapper<WeightLog>().lambda().orderByAsc(WeightLog::getUpdateTime), false);
        //计算相差的天数
        long between = DateUtil.betweenDay(firstDay.getUpdateTime(), todayDate, true);
        sb.append("今日减脂第" + (between + 1) + "天！\n");
        // 查询今天的数据
        WeightLog today = this.getOne(new QueryWrapper<WeightLog>().lambda().between(WeightLog::getUpdateTime, DateUtil.beginOfDay(todayDate), DateUtil.endOfDay(todayDate)), false);
        if (today == null) {
            sb.append("今日体重未记录！请使用\"打卡+体重\"（例如：打卡88.88）记录今日体重。\n\n");
        } else {
            DateTime yesterday = DateUtil.yesterday();
            WeightLog yesterdayData = this.getOne(new QueryWrapper<WeightLog>().lambda().between(WeightLog::getUpdateTime, DateUtil.beginOfDay(yesterday), DateUtil.endOfDay(yesterday)), false);

            sb.append("累计减重" + (firstDay.getWeightValue().subtract(today.getWeightValue())) + "KG，昨日减重" + (yesterdayData.getWeightValue().subtract(today.getWeightValue())) + "KG。\n\n");
        }
        sb.append("近五天减重记录表\n");
        Page<WeightLog> page = this.page(new Page<WeightLog>(1, 6), new QueryWrapper<WeightLog>().orderByDesc("update_time"));
        List<WeightLog> records = page.getRecords();
        Collections.reverse(records);
        if (records.size() >= 6) {
            //为6条 数据足够
            for (int i = 1; i < 6; i++) {
                String dataString = DateUtil.date(records.get(i).getUpdateTime()).toString("MM.dd");
                sb.append(dataString + " " + records.get(i).getWeightValue() + "KG ");
                BigDecimal delta = records.get(i).getWeightValue().subtract(records.get(i - 1).getWeightValue()).abs();
                int compare = records.get(i).getWeightValue().compareTo(records.get(i - 1).getWeightValue());
                if (compare == 0) {
                    sb.append("-" + delta + "\n");
                } else if (compare < 0) {
                    sb.append("↓" + delta + "\n");
                } else {
                    sb.append("↑" + delta + "\n");
                }
            }
        } else {
            sb.append(DateUtil.date(records.get(0).getUpdateTime()).toString("MM.dd") + " " + records.get(0).getWeightValue() + "KG " + "-0.00" + "\n");
            for (int i = 1; i < records.size(); i++) {
                String dataString = DateUtil.date(records.get(i).getUpdateTime()).toString("MM.dd");
                sb.append(dataString + " " + records.get(i).getWeightValue() + "KG ");
                BigDecimal delta = records.get(i).getWeightValue().subtract(records.get(i - 1).getWeightValue()).abs();
                int compare = records.get(i).getWeightValue().compareTo(records.get(i - 1).getWeightValue());
                if (compare == 0) {
                    sb.append("-" + delta + "\n");
                } else if (compare < 0) {
                    sb.append("↓" + delta + "\n");
                } else {
                    sb.append("↑" + delta + "\n");
                }
            }
        }
        sb.append("\n距离结束还有" + (83 - between) + "天，坚持就是胜利！");
        return sb.toString();
    }

}
