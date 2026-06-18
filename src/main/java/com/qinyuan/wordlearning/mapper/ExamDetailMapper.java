package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.entity.ExamDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamDetailMapper extends BaseMapper<ExamDetail> {

    //根据考试id查询单词
    List<ExamDetail> selectByExamId(@Param("examId") Long examId);

}
