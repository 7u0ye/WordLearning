package com.qinyuan.wordlearning.dto;

import lombok.Data;

@Data
public class ExamStartDTO {
    private Long bookId;
    private Integer count;           // 出题数量
    private Integer cnToEnRatio;     // 中选英比例（0~100）
    private Integer durationMinutes; // 考试时长（分钟）
}
