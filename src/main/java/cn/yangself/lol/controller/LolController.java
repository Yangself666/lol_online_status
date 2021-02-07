package cn.yangself.lol.controller;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yangself.lol.service.IService;
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
    @Autowired
    private IService service;

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
        service.setToken(token);
        service.setNotice(false);
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
        service.sendMessage(content);
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "消息发送成功！");
        return result;
    }

    @PostMapping("/start")
    public Map<String,Object> start(){
        Map<String,Object> result = new HashMap<>();
        service.setEnable(true);
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "服务开启成功！");
        return result;
    }

    @PostMapping("/stop")
    public Map<String,Object> stop(){
        Map<String,Object> result = new HashMap<>();
        service.setEnable(false);
        result.put("code", 200);
        result.put("msg", "请求成功！");
        result.put("result", "服务关闭成功！");
        return result;
    }
}
