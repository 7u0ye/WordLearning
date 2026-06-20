package com.qinyuan.wordlearning.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudyWordDTO {
    private Long wordId;
    private String questionType;     // "enToCn" 或 "cnToEn"
    private String question;         // 题目文字（英文或中文）
    private List<String> options;    // 4 个选项
    private String correctAnswer;    // 正确答案（前端即时反馈用）
}
