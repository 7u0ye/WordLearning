package com.qinyuan.wordlearning.controller;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.service.WordBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private WordBookService wordBookService;

    @GetMapping
    public Result getAllBooks() {
        return wordBookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Result getBookById(@PathVariable Long id) {
        return wordBookService.getBookById(id);
    }

    @GetMapping("/{id}/words")
    public Result getWordsByBookId(@PathVariable Long id) {
        return wordBookService.getWordsByBookId(id);
    }
}