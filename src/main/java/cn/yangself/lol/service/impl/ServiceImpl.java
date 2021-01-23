package cn.yangself.lol.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.yangself.lol.service.IService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yangself
 */
@Service
public class ServiceImpl implements IService {
    private static final String WEGAME_STATUS_URL = "https://m.wegame.com.cn/api/mobile/lua/imsnssvr/get_game_friend_online_state";
    private static final String MY_WEGAME_STATUS_URL = "https://m.wegame.com.cn/api/mobile/lua/user_center/get_user_center_header_info";
    private static final String DING_TALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=25bdbad112a8b8c9788aebf6b76dce33a9d4ca6b2fadaa69bbdbd21d35331f4c";
    private static final String COOKIE_TEMPLATE = "tgp_ticket=${tgp_ticket}; channel_number=ios; skey=MiXhJzcQxO; machine_type=iPhone; client_type=602; platform=qq; account=3569762428; app_id=10001; mac=ADF0FD0D-8117-4540-9B3F-84B0AD133A36; app_version=51001; tgp_id=215863828";
    private String TGP_TICKET = "";
    private Boolean ENABLE = true;

    @Override
    public void setToken(String token) {
        this.TGP_TICKET = token;
    }

    @Override
    public void setEnable(Boolean enable) {
        this.ENABLE = enable;
    }

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
        System.out.println(DateUtil.now() + " - 运行状态 -> " + ENABLE);
        System.out.println("TGP_TICKET -> " + TGP_TICKET);
        if(ENABLE) {
            List<String> list = queryStatus();
            if (list == null) {
                for (Map<String, Object> map : gamers) {
                    //遍历需要检测的玩家
                    if ((Boolean) map.get("isOnline")) {
                        map.put("isOnline", false);
                        String msg = (String)map.get("offlineMessage");
                        sendMessage(msg);
                    }
                }
            } else {
                for (Map<String, Object> map : gamers) {
                    if (list.contains(map.get("id"))) {
                        //遍历需要检测的玩家
                        if (!(Boolean) map.get("isOnline")) {
                            map.put("isOnline", true);
                            String msg = (String) map.get("onlineMessage");
                            sendMessage(msg);
                        }
                    } else {
                        if ((Boolean) map.get("isOnline")) {
                            map.put("isOnline", false);
                            String msg = (String) map.get("offlineMessage");
                            sendMessage(msg);
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
        String body = "{\"msgtype\":\"text\",\"text\":{\"content\":\"【注意】" + content + "\"}}";
        String result = HttpUtil.createPost(DING_TALK_URL).header("Content-Type", "application/json").body(body).execute().body();
        JSONObject resultJson = JSON.parseObject(result);
        Integer errcode = resultJson.getInteger("errcode");
        if (errcode != 0){
            System.out.println("消息发送失败 -> msg:" + resultJson.getString("errmsg"));
        }else{
            System.out.println("消息发送成功！");
        }
    }

    /**
     * 查询好友是否在线
     * @return
     */
    private List<String> queryStatus(){
        List<String> onlineList = new ArrayList<>();
        try {
            if (StrUtil.isBlank(TGP_TICKET)) {
                System.out.println("未设置token");
                sendMessage("Token未设置！");
                return null;
            }
            String cookie = COOKIE_TEMPLATE.replace("${tgp_ticket}", TGP_TICKET);

            // String data1 = "{\"user_id\":\"215863828\",\"game_id\":26,\"area_id\":20}";
            String data = "{\"user_id\":\"215863828\",\"game_id\":26,\"area_id\":6}";
            String result = HttpUtil.createPost(WEGAME_STATUS_URL)
                    .contentType("application/json")
                    .header("Cookie", cookie)
                    .header("accept-language", "zh-Hans-CN;q=1")
                    .header("User-Agent", "WeGame/5.10.1 (iPhone; iOS 14.3; Scale/2.00)")
                    .body(data)
                    .execute()
                    .body();
            System.out.println("result = " + result);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") != 0) {
                System.out.println(jsonObject.getString("msg"));
                sendMessage(jsonObject.getString("msg"));
                return null;
            } else {
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("online_state_infos");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject status = jsonArray.getJSONObject(i);
                    String id = status.getString("friend_uid");
                    Integer state = status.getInteger("state");
                    if (state == 1){
                        onlineList.add(id);
                    }
                }
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
            System.out.println("result = " + result);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") != 0) {
                System.out.println(jsonObject.getString("msg"));
                return null;
            } else {
                Integer status = jsonObject.getJSONObject("data").getJSONObject("online_status").getInteger("status");
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
