package org.mobinets.dtlab.transfer.sse;

import org.mobinets.dtlab.common.exception.SSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SSEServiceImpl implements SSEService{

    @Autowired
    private SSEManager manager;

    private static final Logger logger = LoggerFactory.getLogger(SSEServiceImpl.class);

    @Override
    public SseEmitter connect(String clientId) {
        if (manager.exist(clientId)){
            logger.info("Duplicate connections for id:" + clientId);
            return manager.getEmitter(clientId);
        }
        //timeout 30s
        SseEmitter emitter = new SseEmitter(0L);
        manager.addConnection(clientId, emitter);
        try {
            emitter.send(SseEmitter.event().name("init").data("Connection established"));
        } catch (IOException e) {
            logger.error("Error sending message to client:" + clientId);
            emitter.completeWithError(new SSEException("Error sending message"));
            manager.removeConnection(clientId);
        }
        return emitter;
    }

    @Override
    public Boolean closeConn(String clientId) {
        System.out.println("close ");
        return manager.removeConnection(clientId);
    }

    @Override
    public Boolean sendMsg(String clientId, String msg) {
        SseEmitter emitter = manager.getEmitter(clientId);
        if (emitter != null){
            try {
                emitter.send(msg);
                return true;
            } catch (Exception e) {
                logger.error("Error sending message to client:" + clientId);
                emitter.completeWithError(new SSEException("Error sending message"));
                manager.removeConnection(clientId);
            }
        }
        return false;
    }

    @Override
    public void sendMsgToAll(String msg) {
        for (String clientId: manager.getAllClients()){
            sendMsg(clientId, msg);
        }
    }
}
