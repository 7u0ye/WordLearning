package com.qinyuan.wordlearning.service;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.ExamStartDTO;
import com.qinyuan.wordlearning.dto.ExamSubmitDTO;

public interface ExamService {
    //开始考试
    Result startExam(ExamStartDTO examStartDTO);

    //交卷
    Result submitExam(Long examId,ExamSubmitDTO examSubmitDTO);

    //获取进行中的考试
    Result getCurrentExam();

    //历史考试记录
    Result getExamRecords();

    //某次考试详情
    Result getExamDetail(Long examId);
}
