package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.DirectorMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DirectorMessageMapper extends BaseMapper<DirectorMessage> {

    @Select("SELECT * FROM director_message WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<DirectorMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("SELECT * FROM director_message WHERE session_id = #{sessionId} AND (receiver_type = 1 OR FIND_IN_SET(#{playerId}, receiver_ids)) ORDER BY create_time DESC")
    List<DirectorMessage> selectBySessionIdAndPlayerId(@Param("sessionId") Long sessionId, @Param("playerId") Long playerId);
}
