package cn.yangself.lol.service.impl;

import cn.yangself.lol.entity.Steam;
import cn.yangself.lol.mapper.SteamMapper;
import cn.yangself.lol.service.ISteamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SteamServiceImpl extends ServiceImpl<SteamMapper, Steam> implements ISteamService {
}
