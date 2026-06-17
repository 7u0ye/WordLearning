package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.dto.BatchStatusDTO;
import com.qinyuan.wordlearning.entity.UserWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserWordMapper extends BaseMapper<UserWord> {
    //根据用户id和单词书id,查该用户在该书的所有学习记录
    List<UserWord> selectByUserAndWord(@Param("userId") Long userId,@Param("bookId") Long bookId);

    //批量更新学习状态
    int batchUpdateStatus(@Param("userId") Long userId, @Param("updates")List<BatchStatusDTO.UpdateItem> updates);
}
