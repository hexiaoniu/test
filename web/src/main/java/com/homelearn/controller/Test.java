package com.homelearn.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description:</p>
 *
 * @author hexiao
 * @date 2018-07-19 上午10:24
 */
@RestController
@RequestMapping(value = "/api/test")
public class Test {

    @RequestMapping(value = "/aaa")
    public String test(){
        return "aaa";
    }
}
