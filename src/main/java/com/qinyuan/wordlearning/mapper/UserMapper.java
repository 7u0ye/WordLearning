package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
