package com.qinyuan.wordlearning.service.impl;

import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.BatchStatusDTO;
import com.qinyuan.wordlearning.entity.UserWord;
import com.qinyuan.wordlearning.entity.Word;
import com.qinyuan.wordlearning.mapper.UserWordMapper;
import com.qinyuan.wordlearning.mapper.WordBookMapper;
import com.qinyuan.wordlearning.mapper.WordMapper;
import com.qinyuan.wordlearning.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private UserWordMapper userWordMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @Override
    @Transactional
    public Result getStudyWords(Long bookId, Integer count) {
        //确认单词书存在
        if(wordBookMapper.selectById(bookId) == null){
            return Result.error("单词书不存在");
        }

        Long userId = BaseContext.getCurrentId();

        //查出该单词书的所有单词
        List<Word> allWords = wordMapper.selectByWordBookId(bookId);
        if(allWords == null || allWords.isEmpty()){
            return Result.error("该单词书中没单词");
        }

        //查出该用户在该书中的已有的学习记录，变成map形式
        List<UserWord> records = userWordMapper.selectByUserAndWord(userId,bookId);
        Map<Long,UserWord> recordMap = new HashMap<>();
        for(UserWord uw:records){
            recordMap.put(uw.getWordId(),uw);
        }

        //已经记住的不要，出现次数>=2的不要
        List<Word> candidates = new ArrayList<>();
        for(Word word : allWords){
            UserWord uw = recordMap.get(word.getId());
            if(uw==null){
                //新单词，加入候选
                candidates.add(word);
            } else if (uw.getStatus()==0 && uw.getAppearCount()<2) {
                //未记住且出现次数不足2，加入候选
                candidates.add(word);
            }
            //status=1(已记住) 或appearCount>=2 跳过
        }

        //随机打乱，取count个
        Collections.shuffle(candidates);
        int total = Math.min(count,candidates.size());
        List<Word> selected = candidates.subList(0,total);

        //更新单词的出现次数appear_count
        List<BatchStatusDTO.UpdateItem> updateItems = new ArrayList<>();
        for(Word word : selected){
            BatchStatusDTO.UpdateItem item = new BatchStatusDTO.UpdateItem();
            item.setWordId(word.getId());
            item.setStatus(0);  // 默认未记住
            updateItems.add(item);
        }
        if(!updateItems.isEmpty()){
            userWordMapper.batchUpdateStatus(userId,updateItems);
        }

        //返回
        List<Map<String,Object>> result = new ArrayList<>();
        for(Word word:selected){
            Map<String,Object> map = new HashMap<>();
            map.put("id",word.getId());
            map.put("english",word.getEnglish());
            map.put("chinese",word.getChinese());
            UserWord userWord = recordMap.get(word.getId());
            map.put("status",userWord == null ? 0 : userWord.getStatus());
            result.add(map);
        }
        return Result.success(result);



    }

    @Transactional
    @Override
    public Result batchUpdateStatus(BatchStatusDTO batchStatusDTO) {
        Long userId = BaseContext.getCurrentId();
        if (batchStatusDTO.getUpdates() == null || batchStatusDTO.getUpdates().isEmpty()) {
            return Result.success();
        }
        userWordMapper.batchUpdateStatus(userId, batchStatusDTO.getUpdates());
        return Result.success();
    }
}
