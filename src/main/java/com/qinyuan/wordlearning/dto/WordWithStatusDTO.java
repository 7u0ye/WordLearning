package com.qinyuan.wordlearning.dto;

import lombok.Data;

@Data
public class WordWithStatusDTO {
    private Long id;
    private Integer status;
    private Integer forgetCount;
    private String english;
    private String chinese;
}
