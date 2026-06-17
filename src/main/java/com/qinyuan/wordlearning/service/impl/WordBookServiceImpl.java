package com.qinyuan.wordlearning.service.impl;

import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.WordWithStatusDTO;
import com.qinyuan.wordlearning.entity.WordBook;
import com.qinyuan.wordlearning.mapper.WordBookMapper;
import com.qinyuan.wordlearning.service.WordBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;

@Service
public class WordBookServiceImpl implements WordBookService {

    @Autowired
    private WordBookMapper wordBookMapper;

    @Override
    public Result getAllBooks() {
        //查询全部单词书
        List<WordBook> books = wordBookMapper.selectList(null);
        return Result.success(books);
    }

    @Override
    public Result getBookById(Long id) {
        WordBook wordBook = wordBookMapper.selectById(id);
        if(wordBook == null){
            return Result.error("单词书不存在");
        }
        return Result.success(wordBook);
    }

    @Override
    public Result getWordsByBookId(Long bookId) {
        WordBook wordBook = wordBookMapper.selectById(bookId);
        if(wordBook == null){
            return Result.error("单词书不存在");
        }
        Long userId = BaseContext.getCurrentId();
        List<WordWithStatusDTO> words = wordBookMapper.selectWordsWithStatus(bookId, userId);
        return Result.success(words);
    }
}
