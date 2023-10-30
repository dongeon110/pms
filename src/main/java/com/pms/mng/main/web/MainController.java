package com.pms.mng.main.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 메인 화면을 처리하기 위한 컨트롤러
 */
@Slf4j
@Controller
public class MainController {

    /**
     * 첫 메인 홈페이지
     */
    @GetMapping("/home")
    public String home(Model model) {
        // Hello World for Test
        model.addAttribute("hello", "Hello, World!");
        return "/home";
    }
}
