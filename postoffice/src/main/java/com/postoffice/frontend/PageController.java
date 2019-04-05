package com.postoffice.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/message/broker")
public class PageController {

    @GetMapping("/demo/page")
    public String brokerDemoPage() {
        return "sseClient";
    }
}
