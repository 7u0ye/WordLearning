package com.qinyuan.wordlearning.controller;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.ExamStartDTO;
import com.qinyuan.wordlearning.dto.ExamSubmitDTO;
import com.qinyuan.wordlearning.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
public class ExamController {
    @Autowired
    private ExamService examService;

    @PostMapping("/start")
    public Result startExam(@RequestBody ExamStartDTO dto) {
        return examService.startExam(dto);
    }

    @PostMapping("/{id}/submit")
    public Result submitExam(@PathVariable Long id, @RequestBody ExamSubmitDTO dto) {
        return examService.submitExam(id, dto);
    }

    @GetMapping("/current")
    public Result getCurrentExam() {
        return examService.getCurrentExam();
    }

    @GetMapping("/records")
    public Result getExamRecords() {
        return examService.getExamRecords();
    }

    @GetMapping("/records/{id}")
    public Result getExamRecordDetail(@PathVariable Long id) {
        return examService.getExamDetail(id);
    }
}
