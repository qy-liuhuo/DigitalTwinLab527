package org.mobinets.dtlab.transfer.sse;

import org.mobinets.dtlab.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Controller
@RequestMapping("/sse")
public class SSEController {

    @Autowired
    private SSEService service;

    @GetMapping(value = "test/{clientId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter connect(@PathVariable("clientId") String clientId) {
        final SseEmitter emitter = service.connect(clientId);
        return emitter;
    }

    @GetMapping("closeConn/{clientId}")
    public Response<?> closeConn(@PathVariable("clientId") String clientId) {
        service.closeConn(clientId);
        return Response.success("sse connection for" + clientId+ " closed");
    }

}
