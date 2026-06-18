package com.qinyuan.wordlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer totalCount;
    private Integer correctCount;
    private BigDecimal score;
    private Integer durationSeconds;
    private String testType;  //考试方式：已知中文考英文，已知英文考中午
    private Integer status;       //0 进行中 1 已完成
    private LocalDateTime createdAt;
}
