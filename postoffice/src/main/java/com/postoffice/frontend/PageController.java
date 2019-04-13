package com.postoffice.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@Controller
@RequestMapping("/message/broker")
public class PageController {

    Logger logger = Loggers.getLogger(PageController.class);

    @GetMapping("/demo/page")
    public String brokerDemoPage() {
        return "sseClient";
    }

}
