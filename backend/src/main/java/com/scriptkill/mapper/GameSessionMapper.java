package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.GameSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GameSessionMapper extends BaseMapper<GameSession> {

    @Select("SELECT * FROM game_session WHERE session_code = #{sessionCode}")
    GameSession selectBySessionCode(@Param("sessionCode") String sessionCode);
}
