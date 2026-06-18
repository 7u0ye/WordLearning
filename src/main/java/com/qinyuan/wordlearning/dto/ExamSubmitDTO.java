package com.qinyuan.wordlearning.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitDTO {
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long detailId;   // 对应 exam_detail 的 id
        private String answer;   // 用户的答案
    }
}
