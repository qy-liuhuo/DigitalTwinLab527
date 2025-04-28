package com.mobinets.digitaltwinlab.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobinets.digitaltwinlab.entity.Target;
import com.mobinets.digitaltwinlab.entity.Targets;
import com.mobinets.digitaltwinlab.sse.SSEService;
import com.mobinets.digitaltwinlab.util.CoordinateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqttMessageHandler {

    @Autowired
    private SSEService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        try {
            Targets targets = mapper.readValue(message.getPayload(), Targets.class);
            for(Target target: targets.getTargets()) {
                target.setCoordinates(CoordinateConverter.convert(target.getCoordinates()));
            }
            service.sendMsgToAll(mapper.writeValueAsString(targets));
        }
        catch (IOException e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }catch (Exception e){
            System.err.println("Unknown error: " + e.getMessage());
        }
    }
}
