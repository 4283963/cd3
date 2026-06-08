package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.Script;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScriptMapper extends BaseMapper<Script> {
}
