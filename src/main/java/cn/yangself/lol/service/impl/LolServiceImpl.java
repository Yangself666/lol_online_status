package cn.yangself.lol.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.yangself.lol.entity.Lol;
import cn.yangself.lol.entity.Steam;
import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.mapper.LolMapper;
import cn.yangself.lol.service.ISteamService;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.ILolService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
public class LolServiceImpl extends ServiceImpl<LolMapper, Lol> implements ILolService {

    private String MY_WEGAME_STATUS_URL = "";
    private String DING_TALK_URL = "";
    private String WEGAME_STATUS_URL = "";
    private String COOKIE_TEMPLATE = "tgp_ticket=${tgp_ticket}; channel_number=ios; skey=MiXhJzcQxO; machine_type=iPhone; client_type=602; platform=qq; account=3569762428; app_id=10001; mac=ADF0FD0D-8117-4540-9B3F-84B0AD133A36; app_version=51001; tgp_id=215863828";
    private String TGP_TICKET = "";

    //配置项的service
    private ISystemConfigService configService;
    private ISteamService steamService;

    @Autowired
    public void setConfigService(ISystemConfigService configService) {
        this.configService = configService;
    }

    @Autowired
    public void setSteamService(ISteamService steamService) {
        this.steamService = steamService;
    }

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

        if (enable) {
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
            List<Steam> steamList = steamGetPlayerSummaries();
            List<Steam> previousList = steamService.list();
            if (steamList != null){
                //遍历旧的状态信息
                for (Steam steam: previousList) {
                    // 遍历获取到的Steam用户状态
                    for (Steam steamNew: steamList) {
                        if (steamNew.getAccountId().equals(steam.getAccountId())){
                            //匹配到了相同的账号

                            //查看是否在线
                            if (steamNew.getOnlineStatus() != steam.getOnlineStatus() ){
                                //与之前的在线状态不一样
                                if (steamNew.getOnlineStatus()){
                                    //说明上线
                                    sendMessage(steam.getOnlineMessage());
                                }else{
                                    //说明下线
                                    sendMessage(steam.getOfflineMessage());
                                }
                            }

                            // 查看是否玩游戏
                            // 之前没有玩游戏
                            if(StrUtil.isBlank(steam.getPlayingGame())){
                                // 现在玩游戏了
                                if (StrUtil.isNotBlank(steamNew.getPlayingGame())){
                                    sendMessage(steam.getRealName() + "正在玩" + steamNew.getPlayingGame());
                                }
                            }else{
                                // 之前玩游戏了并且现在也在玩游戏
                                if (StrUtil.isNotBlank(steamNew.getPlayingGame())){
                                    // 如果玩的游戏不相同
                                    if (!steamNew.getPlayingGame().equals(steam.getPlayingGame())){
                                        sendMessage(steam.getRealName() + "不玩" + steam.getPlayingGame() + "了，正在玩" + steamNew.getPlayingGame());
                                    }
                                }else{
                                    // 之前玩游戏现在不玩游戏了
                                    sendMessage(steam.getRealName() + "不玩" + steam.getPlayingGame() + "了");
                                }
                            }

                            //更新入数据库
                            steam.setPlayerName(steamNew.getPlayerName());
                            steam.setOnlineStatus(steamNew.getOnlineStatus());
                            steam.setPlayingGame(steamNew.getPlayingGame());
                            steamService.updateById(steam);

                            //找到相同的，处理完成就退出这个小循环
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送钉钉消息
     *
     * @param content
     */
    @Override
    public void sendMessage(String content) {
       String body = "{\"msgtype\":\"text\",\"text\":{\"content\":\"【注意】" + content + "\"}}";
       String result = HttpUtil.createPost(DING_TALK_URL).header("Content-Type", "application/json").body(body).execute().body();
       JSONObject resultJson = JSON.parseObject(result);
       Integer errcode = resultJson.getInteger("errcode");
       if (errcode != 0){
           System.out.println("消息发送失败 -> msg:" + resultJson.getString("errmsg"));
       }else{
           System.out.println("消息发送成功！");
       }
//         log.info("发送消息 -> " + content);
    }

    /**
     * 查询好友是否在线
     *
     * @return
     */
    private List<String> queryStatus() {
        List<String> onlineList = new ArrayList<>();

        //获取是否提醒过
        SystemConfig isNoticeConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "isNotice"));
        Boolean isNotice = Boolean.parseBoolean(isNoticeConfig.getConfigValue());

        try {
            if (StrUtil.isBlank(TGP_TICKET)) {
                log.warn("未设置token");
                //如果没有提醒过
                if (!isNotice) {
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
                if (!isNotice) {
                    log.info("-> 没发过消息，发送消息 <-");

                    //更新发送状态为true
                    isNoticeConfig.setConfigValue("true");
                    configService.updateById(isNoticeConfig);

                    sendMessage("Token已失效！");
                }
                return null;
            } else if (jsonObject.getInteger("code") == 0) {
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("online_state_infos");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject status = jsonArray.getJSONObject(i);
                        String id = status.getString("friend_uid");
                        Integer state = status.getInteger("state");
                        if (state == 1) {
                            onlineList.add(id);
                        }
                    }
                }
            } else {
                log.info("---> " + jsonObject.getString("msg"));
            }

            if (queryMyStatus()) {
                onlineList.add("215863828");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return onlineList;
    }

    /**
     * 查询我的在线状态
     *
     * @return
     */
    private Boolean queryMyStatus() {
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
                if (online_status == null) {
                    return false;
                }
                Integer status = online_status.getInteger("status");
                if (status == 0) {
                    return false;
                } else if (status == 2) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取Steam好友的动态
     *
     * @return
     */
    public List<Steam> steamGetPlayerSummaries() {
        List<Steam> resultList = new ArrayList<>();
        try {
            List<Steam> list = steamService.list();
            String ids = "";
            for (Steam s : list) {
                ids += s.getAccountId() + ",";
            }
            //获取Steam相关配置
            SystemConfig steamApiConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "getPlayerSummaries"));
            String steamApi = steamApiConfig.getConfigValue();
            SystemConfig steamKeyConfig = configService.getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "steamKey"));
            String steamKey = steamKeyConfig.getConfigValue();

            String result = HttpUtil.createGet(steamApi)
                    .form("key", steamKey)
                    .form("steamids", ids)
                    .execute().body();
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject response = jsonObject.getJSONObject("response");
            if (response != null) {
                JSONArray players = response.getJSONArray("players");
                if (players != null) {
                    for (int i = 0; i < players.size(); i++) {
                        JSONObject player = players.getJSONObject(i);
                        String steamId = player.getString("steamid");
                        Integer personState = player.getInteger("personastate");
                        String playerName = player.getString("personaname");
                        Boolean onlineStatus = false;
                        String gameExtraInfo = "";
                        if (personState > 0) {
                            onlineStatus = true;
                            String gameId = player.getString("gameid");
                            if (StrUtil.isNotBlank(gameId)) {
                                gameExtraInfo = player.getString("gameextrainfo");
                            }
                        }
                        //添加查询到的用户到返回的结果中
                        resultList.add(
                                Steam.builder()
                                .accountId(steamId)
                                .playingGame(gameExtraInfo)
                                .playerName(playerName)
                                .onlineStatus(onlineStatus)
                                .build()
                        );
                    }
                }
            }
            return resultList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
