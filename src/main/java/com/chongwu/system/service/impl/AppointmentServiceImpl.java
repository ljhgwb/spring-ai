package com.chongwu.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chongwu.system.entity.Appointment;
import com.chongwu.system.mapper.AppointmentMapper;
import com.chongwu.system.service.AppointmentService;
import org.springframework.stereotype.Service;

/**
 * 寄养预约服务实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {
}