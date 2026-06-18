package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
    //查询用户进行中的考试
    ExamRecord selectInProgress(@Param("userId") Long userId);
}
