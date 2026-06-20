package com.qinyuan.wordlearning.service.impl;

import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.OptionGenerator;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.BatchStatusDTO;
import com.qinyuan.wordlearning.dto.StudyWordDTO;
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
        if (wordBookMapper.selectById(bookId) == null) {
            return Result.error("单词书不存在");
        }

        Long userId = BaseContext.getCurrentId();

        List<Word> allWords = wordMapper.selectByWordBookId(bookId);
        if (allWords == null || allWords.isEmpty()) {
            return Result.error("该单词书中没单词");
        }

        List<UserWord> records = userWordMapper.selectByUserAndWord(userId, bookId);
        Map<Long, UserWord> recordMap = new HashMap<>();
        for (UserWord uw : records) {
            recordMap.put(uw.getWordId(), uw);
        }

        List<Word> candidates = new ArrayList<>();
        for (Word word : allWords) {
            UserWord uw = recordMap.get(word.getId());
            if (uw == null) {
                candidates.add(word);
            } else if (uw.getStatus() == 0 && uw.getAppearCount() < 2) {
                candidates.add(word);
            }
        }

        Collections.shuffle(candidates);
        int total = Math.min(count, candidates.size());
        List<Word> selected = candidates.subList(0, total);

        List<BatchStatusDTO.UpdateItem> updateItems = new ArrayList<>();
        for (Word word : selected) {
            BatchStatusDTO.UpdateItem item = new BatchStatusDTO.UpdateItem();
            item.setWordId(word.getId());
            item.setStatus(0);
            updateItems.add(item);
        }
        if (!updateItems.isEmpty()) {
            userWordMapper.batchUpdateStatus(userId, updateItems);
        }

        List<StudyWordDTO> result = new ArrayList<>();
        for (Word word : selected) {
            String type = Math.random() < 0.5 ? "enToCn" : "cnToEn";

            StudyWordDTO dto = new StudyWordDTO();
            dto.setWordId(word.getId());
            dto.setQuestionType(type);
            dto.setQuestion(type.equals("cnToEn") ? word.getChinese() : word.getEnglish());
            dto.setOptions(OptionGenerator.generate(word, type, allWords));
            dto.setCorrectAnswer(type.equals("cnToEn") ? word.getEnglish() : word.getChinese());
            result.add(dto);
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

        // 1. 先收集所有需要判分的 wordId
        List<Long> wordIds = batchStatusDTO.getUpdates().stream()
                .filter(item -> item.getAnswer() != null && !item.getAnswer().isEmpty())
                .map(BatchStatusDTO.UpdateItem::getWordId)
                .distinct()  // 去重，万一同一单词出现了两次
                .toList();

        // 2. 一次性批量查出所有 word，放进 Map
        Map<Long, Word> wordMap = new HashMap<>();
        if (!wordIds.isEmpty()) {
            List<Word> words = wordMapper.selectBatchIds(wordIds);  // ← 1 条 SQL，IN 查询
            for (Word w : words) {
                wordMap.put(w.getId(), w);
            }
        }

        // 3. 循环里从 Map 取，不再查库
        for (BatchStatusDTO.UpdateItem item : batchStatusDTO.getUpdates()) {
            if (item.getAnswer() != null && !item.getAnswer().isEmpty()) {
                Word word = wordMap.get(item.getWordId());  // ← 内存操作，O(1)
                if (word != null) {
                    boolean correct = item.getAnswer().trim().equalsIgnoreCase(word.getEnglish().trim())
                            || item.getAnswer().trim().equalsIgnoreCase(word.getChinese().trim());
                    item.setStatus(correct ? 1 : 0);
                }
            }
        }

        userWordMapper.batchUpdateStatus(userId, batchStatusDTO.getUpdates());
        return Result.success();
    }
}
