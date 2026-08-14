package com.chongwu.system.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /** 获取仪表盘数据 */
    Map<String, Object> getDashboardData();
}