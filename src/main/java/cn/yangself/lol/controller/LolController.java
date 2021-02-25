package cn.yangself.lol.controller;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.service.ISystemConfigService;
import cn.yangself.lol.service.ILolService;
import cn.yangself.lol.service.IWeightLogService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangself
 */
@Controller
@ResponseBody
@CrossOrigin
public class LolController {
    private ISystemConfigService configService;
    private ILolService lolService;
    private IWeightLogService weightLogService;

    @Autowired
    public void setConfigService(ISystemConfigService configService) {
        this.configService = configService;
    }

    @Autowired
    public void setLolService(ILolService lolService) {
        this.lolService = lolService;
    }

    @Autowired
    public void setWeightLogService(IWeightLogService weightLogService) {
        this.weightLogService = weightLogService;
    }


    @PostMapping("/editToken")
    public Map<String,Object> login(@RequestBody Map<String,String> sendMap){
        Map<String,Object> result = new HashMap<>();
        if (StrUtil.isBlank(sendMap.get("token"))){
            result.put("code", 201);
            result.put("msg", "请求成功！");
            result.put("result", "参数Token为空！");
            return result;
        }
        String token = sendMap.get("token");
        if (!ReUtil.isMatch("^[A-Z0-9]{256}$",token)){
            result.put("code", 202);
            result.put("msg", "请求成功！");
            result.put("result", "Token格式不正确！");
            return result;
        }
        configService.update(new SystemConfig(){{
            setConfigValue(token);
        }},new UpdateWrapper<SystemConfig>(new SystemConfig(){{
            setConfigKey("TGP_TICKET");
        }}));
        configService.update(new SystemConfig(){{
            setConfigValue("false");
        }},new UpdateWrapper<SystemConfig>(new SystemConfig(){{
            setConfigKey("isNotice");
        }}));

        lolService.sendMessage("Token已成功设置！");

        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "Token已成功设置！");
        return result;
    }

    @PostMapping("/sendMessage")
    public Map<String,Object> sendMessage(@RequestBody Map<String,String> sendMap){
        Map<String,Object> result = new HashMap<>();
        if (StrUtil.isBlank(sendMap.get("content"))){
            result.put("code", 201);
            result.put("msg", "请求成功！");
            result.put("result", "参数content为空！");
            return result;
        }
        String content = sendMap.get("content");
        lolService.sendMessage(content);
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "消息发送成功！");
        return result;
    }

    @PostMapping("/start")
    public Map<String,Object> start(){
        Map<String,Object> result = new HashMap<>();
        configService.update(new SystemConfig(){{
            setConfigValue("true");
        }},new UpdateWrapper<SystemConfig>(new SystemConfig(){{
            setConfigKey("ENABLE");
        }}));
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "服务开启成功！");
        return result;
    }

    @PostMapping("/stop")
    public Map<String,Object> stop(){
        Map<String,Object> result = new HashMap<>();
        configService.update(new SystemConfig(){{
            setConfigValue("false");
        }},new UpdateWrapper<SystemConfig>(new SystemConfig(){{
            setConfigKey("ENABLE");
        }}));
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "服务关闭成功！");
        return result;
    }

    /**
     * 接收消息处理
     * @return
     */
    @PostMapping("/handle")
    public Map<String,Object> handle(@RequestBody JSONObject body){
        Map<String,Object> result = new HashMap<>();
        System.out.println(body.toString());
        String string = body.getJSONObject("text").getString("content");
        String[] noContent = new String[]{"没有事情不要叫人家啦～","干什么玩愣，能不能说话！","没事别艾特我，忙奥！"};
        String[] hasContent = new String[]{"哎，我在！","怎么啦，你说吧","没事别艾特我，忙奥！"};
        Map<String,String> content = new HashMap<String,String>();
        if (StrUtil.isBlank(string)){
            int index = NumberUtil.generateRandomNumber(0, 3, 1)[0];
            content.put("content", noContent[index]);
        }else{
            if("小诚小诚".equals(string.trim())){
                int index = NumberUtil.generateRandomNumber(0, 3, 1)[0];
                content.put("content", hasContent[index]);
            }else if(string.indexOf("打卡") > -1){
                //如果艾特消息里包含 打卡 两个字
                if("韩嘉旺".equals(body.getString("senderNick").trim())){
                    //如果是韩嘉旺发的，进行处理
                    String handleResult = weightLogService.messageHandle(string);
                    content.put("content", handleResult);
                }
            } else{
                content.put("content", "别发了，发了老子也看不懂[二哈][二哈][二哈]");
            }
        }
        result.put("msgtype", "text");
        result.put("text",content);
        return result;
    }
}
