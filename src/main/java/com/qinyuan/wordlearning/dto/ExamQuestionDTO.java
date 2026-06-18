package com.qinyuan.wordlearning.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionDTO {
    private Long examId;
    private Integer durationMinutes;
    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {
        private Long detailId;         // 对应 exam_detail 的 id（交卷时要用）
        private String questionType;   // cn2en / en2cn
        private String question;       // 题目内容（中文或英文）
        private List<String> options;  // 4 个选项
        private String userAnswer;     // 恢复考试时已有答案，首次为空
    }
}
