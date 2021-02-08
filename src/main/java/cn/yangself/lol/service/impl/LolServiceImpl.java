package cn.yangself.lol.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.yangself.lol.entity.Lol;
import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.mapper.LolMapper;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.ILolService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yangself
 */

@Service
@Slf4j
public class LolServiceImpl extends ServiceImpl<LolMapper,Lol> implements ILolService {

    private String MY_WEGAME_STATUS_URL = "";
    private String DING_TALK_URL = "";
    private String WEGAME_STATUS_URL = "";
    private String COOKIE_TEMPLATE = "tgp_ticket=${tgp_ticket}; channel_number=ios; skey=MiXhJzcQxO; machine_type=iPhone; client_type=602; platform=qq; account=3569762428; app_id=10001; mac=ADF0FD0D-8117-4540-9B3F-84B0AD133A36; app_version=51001; tgp_id=215863828";
    private String TGP_TICKET = "";

    //配置项的service
    private ISystemConfigService configService;

    @Autowired
    public void setConfigService(ISystemConfigService configService) {
        this.configService = configService;
    }

    /**
     * personastate 1为在线 0为不在线
     * gameid   正在玩的游戏ID
     * gameextrainfo 正在玩的游戏名称
     *
     */
    private static List<Map<String,Object>> gamers = new ArrayList<Map<String,Object>>(){{
       add(new HashMap<String,Object>(){{
           put("id", "183555768");
           put("isOnline",false);
           put("onlineMessage","法外狂徒高忠诚已上线！请注意！");
           put("offlineMessage", "法外狂徒高忠诚下线啦！！！");
       }});
       add(new HashMap<String,Object>(){{
           put("id", "43550649");
           put("isOnline",false);
           put("onlineMessage","韩嘉旺在线执法！");
           put("offlineMessage", "执法者韩嘉旺已下线！");
       }});
       add(new HashMap<String,Object>(){{
           put("id", "55070153");
           put("isOnline",false);
           put("onlineMessage","纳什男爵已刷新！");
           put("offlineMessage", "纳什男爵已被敌方击杀！");
       }});
       add(new HashMap<String,Object>(){{
           put("id", "215863828");
           put("isOnline",false);
           put("onlineMessage","DeBuff加成已开启！");
           put("offlineMessage", "DeBuff消失！");
       }});

    }};

    @Scheduled(cron = "0 * * * * ?")
    @Override
    public void checkStatus() {
        //系统状态
        SystemConfig enableConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "ENABLE"));
        Boolean enable = Boolean.parseBoolean(enableConfig.getConfigValue());

        //WeGameToken获取
        SystemConfig tokenConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "TGP_TICKET"));
        this.TGP_TICKET = tokenConfig.getConfigValue();

        //获取Cookie模板
        SystemConfig cookieTemplateConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "COOKIE_TEMPLATE"));
        this.COOKIE_TEMPLATE = cookieTemplateConfig.getConfigValue();

        //获取Cookie模板
        SystemConfig wegameUrlConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "WEGAME_STATUS_URL"));
        this.WEGAME_STATUS_URL = wegameUrlConfig.getConfigValue();

        //获取钉钉消息URL
        SystemConfig dingTalkUrlConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "DING_TALK_URL"));
        this.DING_TALK_URL = dingTalkUrlConfig.getConfigValue();

        //获取钉钉消息URL
        SystemConfig myWegameStatusUrlConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "MY_WEGAME_STATUS_URL"));
        this.MY_WEGAME_STATUS_URL = myWegameStatusUrlConfig.getConfigValue();

        log.info(DateUtil.now() + " - 运行状态 -> " + enable);
        log.info("TGP_TICKET -> " + TGP_TICKET);

        if(enable) {
            //获取WeGame在线好友列表
            List<String> list = queryStatus();
            List<Lol> gamers = this.list();

            if (list == null) {
                for (Lol lol : gamers) {
                    //遍历需要检测的玩家
                    if (lol.getOnlineStatus()) {
                        lol.setOnlineStatus(false);
                        this.updateById(lol);
                        sendMessage(lol.getOfflineMessage());
                    }
                }
            } else {
                for (Lol lol : gamers) {
                    if (list.contains(String.valueOf(lol.getAccountId()))) {
                        //遍历需要检测的玩家
                        if (!lol.getOnlineStatus()) {
                            lol.setOnlineStatus(true);
                            this.updateById(lol);
                            sendMessage(lol.getOnlineMessage());
                        }
                    } else {
                        if (lol.getOnlineStatus()) {
                            lol.setOnlineStatus(false);
                            this.updateById(lol);
                            sendMessage(lol.getOfflineMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送钉钉消息
     * @param content
     */
    @Override
    public void sendMessage(String content) {
//        String body = "{\"msgtype\":\"text\",\"text\":{\"content\":\"【注意】" + content + "\"}}";
//        String result = HttpUtil.createPost(DING_TALK_URL).header("Content-Type", "application/json").body(body).execute().body();
//        JSONObject resultJson = JSON.parseObject(result);
//        Integer errcode = resultJson.getInteger("errcode");
//        if (errcode != 0){
//            System.out.println("消息发送失败 -> msg:" + resultJson.getString("errmsg"));
//        }else{
//            System.out.println("消息发送成功！");
//        }
        log.info("消息发送成功！");
    }

    /**
     * 查询好友是否在线
     * @return
     */
    private List<String> queryStatus(){
        List<String> onlineList = new ArrayList<>();

        //获取是否提醒过
        SystemConfig isNoticeConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "isNotice"));
        Boolean isNotice = Boolean.parseBoolean(isNoticeConfig.getConfigValue());

        try {
            if (StrUtil.isBlank(TGP_TICKET)) {
                log.warn("未设置token");
                //如果没有提醒过
                if (!isNotice){
                    log.info("-> 没发过消息，发送消息 <-");

                    //更新发送状态为true
                    isNoticeConfig.setConfigValue("true");
                    configService.updateById(isNoticeConfig);

                    //发送钉钉消息
                    sendMessage("Token未设置！");
                }
                return null;
            }

            String cookie = COOKIE_TEMPLATE.replace("${tgp_ticket}", TGP_TICKET);

            //扭曲丛林
            // String data1 = "{\"user_id\":\"215863828\",\"game_id\":26,\"area_id\":20}";
            //德玛西亚
            String data = "{\"user_id\":\"215863828\",\"game_id\":26,\"area_id\":6}";

            //开始请求
            String result = HttpUtil.createPost(WEGAME_STATUS_URL)
                    .contentType("application/json")
                    .header("Cookie", cookie)
                    .header("accept-language", "zh-Hans-CN;q=1")
                    .header("User-Agent", "WeGame/5.10.1 (iPhone; iOS 14.3; Scale/2.00)")
                    .body(data)
                    .execute()
                    .body();
            log.info("获取好友状态请求结果 -> " + result);

            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") == -80005) {
                System.out.println("token失效");
                if (!isNotice){
                    log.info("-> 没发过消息，发送消息 <-");

                    //更新发送状态为true
                    isNoticeConfig.setConfigValue("true");
                    configService.updateById(isNoticeConfig);

                    sendMessage("Token已失效！");
                }
                return null;
            } else if (jsonObject.getInteger("code") == 0){
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("online_state_infos");
                if(jsonArray != null){
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject status = jsonArray.getJSONObject(i);
                        String id = status.getString("friend_uid");
                        Integer state = status.getInteger("state");
                        if (state == 1){
                            onlineList.add(id);
                        }
                    }
                }
            }else{
                log.info("---> " + jsonObject.getString("msg"));
            }

            if(queryMyStatus()){
                onlineList.add("215863828");
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return onlineList;
    }

    /**
     * 查询我的在线状态
     * @return
     */
    private Boolean queryMyStatus(){
        try {
            if (StrUtil.isBlank(TGP_TICKET)) {
                System.out.println("未设置token");
                return null;
            }
            String cookie = COOKIE_TEMPLATE.replace("${tgp_ticket}", TGP_TICKET);

            String data = "{\"dst\":\"215863828\",\"uid\":\"215863828\"}";

            String result = HttpUtil.createPost(MY_WEGAME_STATUS_URL)
                    .contentType("application/json")
                    .header("Cookie", cookie)
                    .header("accept-language", "zh-Hans-CN;q=1")
                    .header("User-Agent", "WeGame/5.10.1 (iPhone; iOS 14.3; Scale/2.00)")
                    .body(data)
                    .execute()
                    .body();
            log.info("查询自己的在线状态结果 -> " + result);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") != 0) {
                System.out.println(jsonObject.getString("msg"));
                return null;
            } else {
                JSONObject online_status = jsonObject.getJSONObject("data").getJSONObject("online_status");
                if (online_status == null){
                    return false;
                }
                Integer status = online_status.getInteger("status");
                if (status == 0) {
                    return false;
                } else if (status == 2){
                    return true;
                }else{
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
