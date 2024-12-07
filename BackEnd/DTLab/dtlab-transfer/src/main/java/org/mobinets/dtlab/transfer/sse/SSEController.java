package org.mobinets.dtlab.transfer.sse;

import org.mobinets.dtlab.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SSEController {

    @Autowired
    private SSEService service;

    @GetMapping(value = "/connect",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String clientId) {
        return service.connect(clientId);
    }

    @GetMapping("/close")
    public Response<?> close(@RequestParam String clientId) {
        service.closeConn(clientId);
        return Response.success("sse connection for" + clientId+ " closed");
    }

}
