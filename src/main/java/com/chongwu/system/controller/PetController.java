package com.chongwu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chongwu.system.common.Result;
import com.chongwu.system.entity.Pet;
import com.chongwu.system.service.PetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物管理控制器
 */
@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/my")
    public Result<Object> getMyPets(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        return Result.success(petService.list(wrapper));
    }

    @GetMapping("/list")
    public Result<Object> getPetList(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(petService.page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    public Result<Pet> getPetById(@PathVariable Long id) {
        return Result.success(petService.getById(id));
    }

    @PostMapping
    public Result<Pet> addPet(@RequestBody Pet pet, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        pet.setUserId(userId);
        petService.save(pet);
        return Result.success("添加成功", pet);
    }

    @PutMapping("/{id}")
    public Result<Pet> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        petService.updateById(pet);
        return Result.success("更新成功", pet);
    }

    @DeleteMapping("/{id}")
    public Result<?> deletePet(@PathVariable Long id) {
        petService.removeById(id);
        return Result.success("删除成功");
    }
}