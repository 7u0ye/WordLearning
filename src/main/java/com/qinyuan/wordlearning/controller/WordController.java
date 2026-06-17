package com.qinyuan.wordlearning.controller;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.BatchStatusDTO;
import com.qinyuan.wordlearning.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/words")
public class WordController {
    @Autowired
    private WordService wordService;

    @GetMapping("/study")
    public Result getStudyWords(@RequestParam Long bookId, @RequestParam Integer count) {
        return wordService.getStudyWords(bookId, count);
    }

    @PutMapping("/batch-status")
    public Result batchUpdateStatus(@RequestBody BatchStatusDTO dto) {
        return wordService.batchUpdateStatus(dto);
    }
}
