package com.chongwu.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chongwu.system.entity.Notice;
import com.chongwu.system.mapper.NoticeMapper;
import com.chongwu.system.service.NoticeService;
import org.springframework.stereotype.Service;

/**
 * 公告服务实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
}