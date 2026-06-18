package com.qinyuan.wordlearning.service.impl;

import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.ExamStartDTO;
import com.qinyuan.wordlearning.dto.ExamSubmitDTO;
import com.qinyuan.wordlearning.mapper.ExamDetailMapper;
import com.qinyuan.wordlearning.mapper.ExamRecordMapper;
import com.qinyuan.wordlearning.mapper.WordBookMapper;
import com.qinyuan.wordlearning.mapper.WordMapper;
import com.qinyuan.wordlearning.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExamServiceImpl implements ExamService {
    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamDetailMapper examDetailMapper;

    @Override
    public Result startExam(ExamStartDTO examStartDTO) {
        Long userId = BaseContext.getCurrentId();
        //检查单词书是否存在


        //查出该书所有单词

        //随机抽取count个单词

        //分配题型比例，题型有看中识英，看英识中

        //创建考试记录

        //为每道题生成选项，创建exam_detail

        //组装返回前端

        //返回
        return null;
    }

    @Override
    public Result submitExam(Long examId, ExamSubmitDTO examSubmitDTO) {
        //查考试记录


        //查出所有题目

        //逐题判分

        //更新考试记录

        //返回结果
        return null;
    }

    @Override
    public Result getCurrentExam() {
        return null;
    }

    @Override
    public Result getExamRecords() {
        return null;
    }

    @Override
    public Result getExamDetail(Long examId) {
        return null;
    }
}
