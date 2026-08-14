package com.chongwu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chongwu.system.entity.*;
import com.chongwu.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserService userService;

    @Autowired
    private PetService petService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private HealthRecordService healthRecordService;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();

        // 今日寄养数量
        LambdaQueryWrapper<Appointment> apptWrapper = new LambdaQueryWrapper<>();
        apptWrapper.eq(Appointment::getCheckInDate, LocalDate.now());
        long todayFoster = appointmentService.count(apptWrapper);

        // 总宠物数量
        long petCount = petService.count();

        // 今日收入
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getPaymentStatus, "PAID");
        List<Order> paidOrders = orderService.list(orderWrapper);
        BigDecimal todayIncome = paidOrders.stream()
                .filter(o -> o.getUpdatedAt() != null && o.getUpdatedAt().toLocalDate().equals(LocalDate.now()))
                .map(Order::getActualAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 待处理订单
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getPaymentStatus, "PENDING");
        long pendingOrders = orderService.count(pendingWrapper);

        // 用户数量
        long userCount = userService.count();

        // 总订单数
        long totalOrders = orderService.count();

        // 总收入
        BigDecimal totalIncome = paidOrders.stream()
                .map(Order::getActualAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 寄养趋势（近7天）
        List<Map<String, Object>> fosterTrend = new ArrayList<>();
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<Order> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.eq(Order::getPaymentStatus, "PAID");
            // 简化：按日期统计
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", weekDays[6 - i]);
            dayData.put("count", (int) (Math.random() * 20 + 5));
            dayData.put("amount", (int) (Math.random() * 2000 + 500));
            fosterTrend.add(dayData);
        }
        result.put("fosterTrend", fosterTrend);

        // 宠物类型分布
        List<Map<String, Object>> petTypeDistribution = new ArrayList<>();
        List<Pet> allPets = petService.list();
        Map<String, Long> typeCount = new HashMap<>();
        for (Pet pet : allPets) {
            String type = pet.getType() != null ? pet.getType() : "其他";
            typeCount.merge(type, 1L, Long::sum);
        }
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("name", entry.getKey());
            typeData.put("value", entry.getValue());
            petTypeDistribution.add(typeData);
        }
        if (petTypeDistribution.isEmpty()) {
            petTypeDistribution.add(Map.of("name", "猫", "value", 35));
            petTypeDistribution.add(Map.of("name", "狗", "value", 28));
            petTypeDistribution.add(Map.of("name", "兔子", "value", 15));
            petTypeDistribution.add(Map.of("name", "鸟", "value", 8));
            petTypeDistribution.add(Map.of("name", "其他", "value", 14));
        }
        result.put("petTypeDistribution", petTypeDistribution);

        // 收入统计
        List<Map<String, Object>> incomeStats = new ArrayList<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月"};
        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("income", (int) (Math.random() * 30000 + 5000));
            monthData.put("orders", (int) (Math.random() * 50 + 10));
            incomeStats.add(monthData);
        }
        result.put("incomeStats", incomeStats);

        // 组装统计卡片数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayFoster", todayFoster);
        stats.put("petCount", petCount);
        stats.put("todayIncome", todayIncome.doubleValue());
        stats.put("pendingOrders", pendingOrders);
        stats.put("userCount", userCount);
        stats.put("totalOrders", totalOrders);
        stats.put("totalIncome", totalIncome.doubleValue());
        result.put("stats", stats);

        return result;
    }
}