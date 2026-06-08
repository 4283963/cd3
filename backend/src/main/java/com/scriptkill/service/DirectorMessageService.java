package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.DirectorMessage;
import com.scriptkill.mapper.DirectorMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectorMessageService extends ServiceImpl<DirectorMessageMapper, DirectorMessage> {

    private static final Logger log = LoggerFactory.getLogger(DirectorMessageService.class);

    @Autowired
    private WebSocketPushService webSocketPushService;

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

        try {
            webSocketPushService.pushMessage(sessionId, msg);
        } catch (Exception e) {
            log.warn("消息WebSocket推送失败: sessionId={}", sessionId, e);
        }

        return msg;
    }
}
