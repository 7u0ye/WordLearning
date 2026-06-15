package com.qinyuan.wordlearning.service;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.LoginDTO;
import com.qinyuan.wordlearning.dto.RegisterDTO;
import org.springframework.stereotype.Service;


public interface UserService {
    Result register(RegisterDTO registerDTO);

    Result login(LoginDTO loginDTO);

    Result getUserInfo();
}
