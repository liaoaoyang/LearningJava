package co.iay.learn.learningjava.spring.springstudy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HCTestController {
    @RequestMapping("/post/test/case/1")
    public @ResponseBody
    String postTestCase1(@RequestParam String v) {
        return v;
    }
}
