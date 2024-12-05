package org.mobinets.dtlab.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mobinets.dtlab.common.pojo.Target;
import org.mobinets.dtlab.common.pojo.Targets;
import org.mobinets.dtlab.common.utils.CoordinateConverter;
import org.mobinets.dtlab.transfer.sse.SSEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqttMessageHandler {

    Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Autowired
    private SSEService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        logger.info("Received message: " + message.getPayload());
        try {
            Targets targets = mapper.readValue(message.getPayload(), Targets.class);
            for(Target target: targets.getTargets()) {
                target.setCoordinates(CoordinateConverter.convert(target.getCoordinates()));
            }
//            logger.info(targets.toString());
            service.sendMsgToAll(mapper.writeValueAsString(targets));
        }
        catch (IOException e){
            logger.error("Error parsing message: " + e.getMessage());
        }
    }
}
