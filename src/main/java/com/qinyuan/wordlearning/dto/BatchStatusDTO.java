package com.qinyuan.wordlearning.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchStatusDTO {
    private List<UpdateItem> updates;

    @Data
    public static class UpdateItem {
        private Long wordId;
        private Integer status;   // 1=记得, 0=不记得
        private String answer;    // 用户选的答案（选择题模式用，后端自动判对错）
    }
}