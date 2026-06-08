package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.PlayerClue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerClueMapper extends BaseMapper<PlayerClue> {

    @Select("SELECT * FROM player_clue WHERE player_id = #{playerId} AND session_id = #{sessionId} ORDER BY create_time ASC")
    List<PlayerClue> selectByPlayerIdAndSessionId(@Param("playerId") Long playerId, @Param("sessionId") Long sessionId);

    @Select("SELECT pc.* FROM player_clue pc " +
            "INNER JOIN clue c ON pc.clue_id = c.id " +
            "WHERE pc.player_id = #{playerId} AND pc.session_id = #{sessionId} AND c.parent_id = #{parentId} " +
            "ORDER BY c.sort_order ASC, c.id ASC")
    List<PlayerClue> selectByPlayerIdAndParentId(@Param("playerId") Long playerId,
                                                  @Param("sessionId") Long sessionId,
                                                  @Param("parentId") Long parentId);
}
