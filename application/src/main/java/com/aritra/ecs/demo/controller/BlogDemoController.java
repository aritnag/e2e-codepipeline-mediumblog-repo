package com.aritra.ecs.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/ecs/demo")
public class BlogDemoController {

    @RequestMapping(method = RequestMethod.GET)
    public String newendpoint() {
       // Get the current timestamp
       Date currentDate = new Date();
        
       // Format the timestamp as desired (e.g., "yyyy-MM-dd HH:mm:ss")
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String formattedTimestamp = dateFormat.format(currentDate);
       
       return "Demo Worked - Current Timestamp: " + formattedTimestamp;
    }
}
