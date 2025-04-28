package com.mobinets.digitaltwinlab.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SSEService {
    public SseEmitter connect(String clientId);

    public Boolean closeConn(String clientId);

    public Boolean sendMsg(String clientId, String msg);

    public void sendMsgToAll(String msg);

}
