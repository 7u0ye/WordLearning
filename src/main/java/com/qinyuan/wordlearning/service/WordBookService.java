package com.qinyuan.wordlearning.service;

import com.qinyuan.wordlearning.common.Result;

public interface WordBookService {

    Result getAllBooks();

    Result getBookById(Long id);

    Result getWordsByBookId(Long bookId);
}
