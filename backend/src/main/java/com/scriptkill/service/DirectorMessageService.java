package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.DirectorMessage;
import com.scriptkill.mapper.DirectorMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectorMessageService extends ServiceImpl<DirectorMessageMapper, DirectorMessage> {

    public List<DirectorMessage> getSessionMessages(Long sessionId) {
        return baseMapper.selectBySessionId(sessionId);
    }

    public List<DirectorMessage> getPlayerMessages(Long sessionId, Long playerId) {
        return baseMapper.selectBySessionIdAndPlayerId(sessionId, playerId);
    }

    public DirectorMessage sendMessage(Long sessionId, Long senderId, Integer receiverType,
                                       List<Long> receiverIds, Integer msgType, String title, String content) {
        DirectorMessage msg = new DirectorMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setReceiverType(receiverType);
        if (receiverIds != null && !receiverIds.isEmpty()) {
            msg.setReceiverIds(receiverIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        msg.setMsgType(msgType);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        save(msg);
        return msg;
    }
}
