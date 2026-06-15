package com.qinyuan.wordlearning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.JwtUtil;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.LoginDTO;
import com.qinyuan.wordlearning.dto.RegisterDTO;
import com.qinyuan.wordlearning.entity.User;
import com.qinyuan.wordlearning.mapper.UserMapper;
import com.qinyuan.wordlearning.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result register(RegisterDTO registerDTO) {
        //检查用户名是否已存在
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,registerDTO.getUsername()));
        if(exist!=null){
            return Result.error("用户名已存在");
        }

        //密码加密后保存
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(),BCrypt.gensalt()));
        user.setEmail(registerDTO.getEmail());
        userMapper.insert(user);

        return Result.success();

    }

    @Override
    public Result login(LoginDTO loginDTO) {
        //查用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,loginDTO.getUsername()));

        //验密码
        if(user==null || !BCrypt.checkpw(loginDTO.getPassword(),user.getPassword())){
            return Result.error("用户名或密码错误");
        }

        //生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
    }

    @Override
    public Result getUserInfo() {
        //从ThreadLocal拿当前用户id
        Long id = BaseContext.getCurrentId();
        User user = userMapper.selectById(id);
        if(user==null){
            return Result.error("用户不存在");
        }
        //不返回密码
        user.setPassword(null);
        return Result.success(user);
    }
}
