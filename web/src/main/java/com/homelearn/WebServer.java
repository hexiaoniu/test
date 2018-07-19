package com.homelearn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Description: Web工程Main Class
 * @author hexiao
 * @date  2018/6/13 11:37
 */
@SpringBootApplication
public class WebServer {
    private static Logger logger = LoggerFactory.getLogger(WebServer.class);

    public static void main(String[] args) {
        SpringApplication.run(WebServer.class, args);
        logger.info("WebServer is started...");
    }
}
