package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.ScriptRole;
import com.scriptkill.mapper.ScriptMapper;
import com.scriptkill.vo.ScriptDetailVO;
import com.scriptkill.vo.ScriptRoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScriptService extends ServiceImpl<ScriptMapper, Script> {

    @Autowired
    private ScriptRoleService scriptRoleService;

    public Page<Script> getPage(Long current, Long size, String keyword) {
        Page<Script> page = new Page<>(current, size);
        QueryWrapper<Script> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("title", keyword).or().like("author", keyword);
        }
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    public ScriptDetailVO getDetail(Long id) {
        Script script = getById(id);
        if (script == null) {
            return null;
        }
        ScriptDetailVO vo = new ScriptDetailVO();
        BeanUtils.copyProperties(script, vo);
        List<ScriptRole> roles = scriptRoleService.getByScriptId(id);
        List<ScriptRoleVO> roleVOs = roles.stream().map(role -> {
            ScriptRoleVO roleVO = new ScriptRoleVO();
            BeanUtils.copyProperties(role, roleVO);
            return roleVO;
        }).collect(Collectors.toList());
        vo.setRoles(roleVOs);
        return vo;
    }

    public List<Script> listAll() {
        QueryWrapper<Script> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }
}
