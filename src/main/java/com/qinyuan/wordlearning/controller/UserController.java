package com.qinyuan.wordlearning.controller;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.LoginDTO;
import com.qinyuan.wordlearning.dto.RegisterDTO;
import com.qinyuan.wordlearning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result Register(@RequestBody RegisterDTO registerDTO){
        return userService.register(registerDTO);
    }

    @PostMapping("/login")
    public Result Login(@RequestBody LoginDTO loginDTO){
        return userService.login(loginDTO);
    }

    @GetMapping("/info")
    public Result info(){
        return userService.getUserInfo();
    }
}
