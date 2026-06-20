package com.qinyuan.wordlearning.service.impl;

import com.qinyuan.wordlearning.common.BaseContext;
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

    private List<String> generateOptions(Word correctWord, String type, List<Word> allWords) {
        String correct = type.equals("cnToEn") ? correctWord.getEnglish() : correctWord.getChinese();

        List<String> wrongs = new ArrayList<>();
        List<Word> others = new ArrayList<>(allWords);
        others.removeIf(w -> w.getId().equals(correctWord.getId()));
        Collections.shuffle(others);

        for (Word w : others) {
            String wrong = type.equals("cnToEn") ? w.getEnglish() : w.getChinese();
            if (!wrong.equals(correct) && wrongs.size() < 3) {
                wrongs.add(wrong);
            }
        }

        List<String> options = new ArrayList<>();
        options.add(correct);
        options.addAll(wrongs);
        Collections.shuffle(options);
        return options;
    }

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
            dto.setOptions(generateOptions(word, type, allWords));
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

        for (BatchStatusDTO.UpdateItem item : batchStatusDTO.getUpdates()) {
            if (item.getAnswer() != null && !item.getAnswer().isEmpty()) {
                Word word = wordMapper.selectById(item.getWordId());
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
