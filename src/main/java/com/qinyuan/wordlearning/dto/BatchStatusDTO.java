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
    }
}