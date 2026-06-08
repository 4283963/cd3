package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.ScriptRole;
import com.scriptkill.mapper.ScriptRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScriptRoleService extends ServiceImpl<ScriptRoleMapper, ScriptRole> {

    public List<ScriptRole> getByScriptId(Long scriptId) {
        QueryWrapper<ScriptRole> wrapper = new QueryWrapper<>();
        wrapper.eq("script_id", scriptId);
        wrapper.orderByAsc("sort_order", "id");
        return list(wrapper);
    }

    public ScriptRole getById(Long id) {
        return baseMapper.selectById(id);
    }
}
