package com.qinyuan.wordlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_detail")
public class ExamDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long wordId;
    private String questionType;     // cn2en / en2cn
    private String userAnswer;
    private String correctAnswer;
    private Integer isCorrect;       // 0=错, 1=对
}
