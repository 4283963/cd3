package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Clue;
import com.scriptkill.mapper.ClueMapper;
import com.scriptkill.vo.ClueTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClueService extends ServiceImpl<ClueMapper, Clue> {

    public List<Clue> getByScriptId(Long scriptId) {
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();
        wrapper.eq("script_id", scriptId);
        wrapper.orderByAsc("level", "sort_order", "id");
        return list(wrapper);
    }

    public List<ClueTreeVO> getClueTree(Long scriptId) {
        List<Clue> allClues = getByScriptId(scriptId);
        return buildTree(allClues, 0L);
    }

    private List<ClueTreeVO> buildTree(List<Clue> allClues, Long parentId) {
        List<ClueTreeVO> result = new ArrayList<>();
        for (Clue clue : allClues) {
            if (clue.getParentId() != null && clue.getParentId().equals(parentId)) {
                ClueTreeVO vo = new ClueTreeVO();
                BeanUtils.copyProperties(clue, vo);
                vo.setChildren(buildTree(allClues, clue.getId()));
                result.add(vo);
            }
        }
        return result;
    }

    public List<Clue> getByParentId(Long scriptId, Long parentId) {
        return baseMapper.selectByScriptIdAndParentId(scriptId, parentId);
    }

    public List<Clue> getPublicClues(Long scriptId) {
        return baseMapper.selectPublicCluesByScriptId(scriptId);
    }

    public Clue getById(Long id) {
        return baseMapper.selectById(id);
    }

    public boolean hasChildren(Long clueId) {
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", clueId);
        return count(wrapper) > 0;
    }
}
