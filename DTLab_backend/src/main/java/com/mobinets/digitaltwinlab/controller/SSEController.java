package com.mobinets.digitaltwinlab.controller;


import com.mobinets.digitaltwinlab.sse.SSEService;
import com.mobinets.digitaltwinlab.util.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Controller
@RequestMapping("/sse")
public class SSEController {

    @Autowired
    private SSEService service;

    @GetMapping(value = "/connect",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String clientId) {
        System.out.println("clientId: " + clientId);
        return service.connect(clientId);
    }

    @GetMapping("/close")
    public Response close(@RequestParam String clientId) {
        service.closeConn(clientId);
        return Response.success("sse connection for" + clientId+ " closed");
    }

}
