package com.chongwu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chongwu.system.common.Result;
import com.chongwu.system.entity.HealthRecord;
import com.chongwu.system.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 健康记录控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @GetMapping("/pet/{petId}")
    public Result<Object> getByPetId(@PathVariable Long petId) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getPetId, petId).orderByDesc(HealthRecord::getCreatedAt);
        return Result.success(healthRecordService.list(wrapper));
    }

    @GetMapping("/list")
    public Result<Object> getList(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(healthRecordService.page(new Page<>(page, size)));
    }

    @PostMapping
    public Result<HealthRecord> add(@RequestBody HealthRecord record) {
        healthRecordService.save(record);
        return Result.success("添加成功", record);
    }

    @PutMapping("/{id}")
    public Result<HealthRecord> update(@PathVariable Long id, @RequestBody HealthRecord record) {
        record.setId(id);
        healthRecordService.updateById(record);
        return Result.success("更新成功", record);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        healthRecordService.removeById(id);
        return Result.success("删除成功");
    }
}