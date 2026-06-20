package com.qinyuan.wordlearning.common;

import com.qinyuan.wordlearning.entity.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionGenerator {
    public static List<String> generate(Word correctWord, String type, List<Word> allWords) {
        // 根据题型确定正确答案是英文还是中文
        String correct = type.equals("cnToEn")
                ? correctWord.getEnglish()
                : correctWord.getChinese();

        // 从其他单词中随机选 3 个不同的干扰项
        List<String> wrongs = new ArrayList<>();
        List<Word> others = new ArrayList<>(allWords);
        others.removeIf(w -> w.getId().equals(correctWord.getId()));
        Collections.shuffle(others);

        for (Word w : others) {
            String wrong = type.equals("cnToEn") ? w.getEnglish() : w.getChinese();
            // 跳过和正确答案相同的干扰项，凑满 3 个
            if (!wrong.equals(correct) && wrongs.size() < 3) {
                wrongs.add(wrong);
            }
        }

        // 合并正确答案和干扰项，打乱顺序
        List<String> options = new ArrayList<>();
        options.add(correct);
        options.addAll(wrongs);
        Collections.shuffle(options);
        return options;
    }
}
