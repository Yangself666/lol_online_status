package cn.yangself.lol.service.impl;

import cn.yangself.lol.entity.SystemConfig;
import cn.yangself.lol.mapper.SystemConfigMapper;
import cn.yangself.lol.service.ISystemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements ISystemConfigService {
}
