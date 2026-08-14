package com.chongwu.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chongwu.system.entity.Pet;
import com.chongwu.system.mapper.PetMapper;
import com.chongwu.system.service.PetService;
import org.springframework.stereotype.Service;

/**
 * 宠物服务实现类
 */
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {
}