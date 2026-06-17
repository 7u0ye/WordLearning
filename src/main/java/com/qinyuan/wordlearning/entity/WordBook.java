package com.qinyuan.wordlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("word_book")
public class WordBook {
        @TableId(type = IdType.AUTO)
        private Long id;
        private String name;
        private String description;
        private String targetAudience;
        private Integer wordCount;
        private LocalDateTime createdAt;
}
