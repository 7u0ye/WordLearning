package com.qinyuan.wordlearning.service.impl;

import com.qinyuan.wordlearning.common.BaseContext;
import com.qinyuan.wordlearning.common.OptionGenerator;
import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.ExamQuestionDTO;
import com.qinyuan.wordlearning.dto.ExamStartDTO;
import com.qinyuan.wordlearning.dto.ExamSubmitDTO;
import com.qinyuan.wordlearning.entity.ExamDetail;
import com.qinyuan.wordlearning.entity.ExamRecord;
import com.qinyuan.wordlearning.entity.Word;
import com.qinyuan.wordlearning.mapper.ExamDetailMapper;
import com.qinyuan.wordlearning.mapper.ExamRecordMapper;
import com.qinyuan.wordlearning.mapper.WordBookMapper;
import com.qinyuan.wordlearning.mapper.WordMapper;
import com.qinyuan.wordlearning.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ExamServiceImpl implements ExamService {
    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamDetailMapper examDetailMapper;

    //组装考试结果
    private Result buildExamResult(ExamRecord examRecord) {
        List<ExamDetail> details = examDetailMapper.selectByExamId(examRecord.getId());

        // 查出该单词书所有单词，用于生成选项和题目文字
        List<Word> allWords = wordMapper.selectByWordBookId(examRecord.getBookId());
        Map<Long, Word> wordMap = new HashMap<>();
        for (Word w : allWords) {
            wordMap.put(w.getId(), w);
        }

        List<ExamQuestionDTO.QuestionItem> questions = new ArrayList<>();
        for (ExamDetail examDetail : details) {
            Word word = wordMap.get(examDetail.getWordId());

            ExamQuestionDTO.QuestionItem questionItem = new ExamQuestionDTO.QuestionItem();
            questionItem.setDetailId(examDetail.getId());
            questionItem.setQuestionType(examDetail.getQuestionType());
            // 题目文字：看中识英显示中文，看英识中显示英文
            questionItem.setQuestion(examDetail.getQuestionType().equals("cnToEn")
                    ? word.getChinese() : word.getEnglish());
            // 重新生成 4 个选项
            questionItem.setOptions(OptionGenerator.generate(word, examDetail.getQuestionType(), allWords));
            questionItem.setUserAnswer(examDetail.getUserAnswer());
            questions.add(questionItem);
        }

        ExamQuestionDTO examQuestionDTO = new ExamQuestionDTO();
        examQuestionDTO.setExamId(examRecord.getId());
        examQuestionDTO.setDurationMinutes(examRecord.getDurationSeconds() / 60);
        examQuestionDTO.setQuestions(questions);

        Map<String, Object> result = new HashMap<>();
        result.put("record", examRecord);
        result.put("details", examQuestionDTO);
        return Result.success(result);
    }


    @Transactional
    @Override
    public Result startExam(ExamStartDTO examStartDTO) {
        Long userId = BaseContext.getCurrentId();

        //检查单词书是否存在
        if(wordBookMapper.selectById(examStartDTO.getBookId())==null){
            return Result.error("单词书不存在");
        }
        //查出该书所有单词
        List<Word> allWords = wordMapper.selectByWordBookId(examStartDTO.getBookId());
        if(allWords.size()<4){
            return Result.error("该单词书单词不足四个，无法生成题目");
        }

        //随机抽取count个单词
        List<Word> copy = new ArrayList<>(allWords);
        Collections.shuffle(copy);
        int questionCount = Math.min(examStartDTO.getCount(),copy.size());
        List<Word> selected = copy.subList(0,questionCount);

        //分配题型比例，题型有看中识英，看英识中
        int cnToEnCount = (int)Math.round(questionCount*examStartDTO.getCnToEnRatio()/100.0);
        //前cnToEnCount道是看中识英，后面是看英识中
        List<String> types = new ArrayList<>();
        for(int i = 0;i<questionCount;i++){
            types.add(i<cnToEnCount?"cnToEn":"enToCn");
        }
        Collections.shuffle(types);

        //创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setBookId(examStartDTO.getBookId());
        record.setTotalCount(questionCount);
        record.setCorrectCount(0);
        record.setStatus(0);//考试进行ing
        record.setDurationSeconds(examStartDTO.getDurationMinutes()*60);
        record.setTestType("cnToEn:"+examStartDTO.getCnToEnRatio()+",enToCn:"+(100-examStartDTO.getCnToEnRatio()));
        examRecordMapper.insert(record);

        //为每道题生成选项，创建exam_detail
        List<ExamQuestionDTO.QuestionItem> questions = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            Word word = selected.get(i);
            String type = types.get(i);

            //生成四个选项（1正确+3干扰）
            List<String> options = OptionGenerator.generate(word, type, allWords);

            //存入exam_detail,正确答案不返回前端
            ExamDetail detail = new ExamDetail();
            detail.setExamId(record.getId());
            detail.setWordId(word.getId());
            detail.setQuestionType(type);
            detail.setCorrectAnswer(type.equals("cnToEn")?word.getEnglish():word.getChinese());
            detail.setIsCorrect(0);
            examDetailMapper.insert(detail);

            //组装返回前端
            ExamQuestionDTO.QuestionItem questionItem = new ExamQuestionDTO.QuestionItem();
            questionItem.setDetailId(detail.getId());
            questionItem.setQuestionType(type);
            questionItem.setQuestion(type.equals("cnToEn") ? word.getChinese() : word.getEnglish());
            questionItem.setOptions(options);
            questions.add(questionItem);
        }

        //返回
        ExamQuestionDTO result = new ExamQuestionDTO();
        result.setExamId(record.getId());
        result.setDurationMinutes(examStartDTO.getDurationMinutes());
        result.setQuestions(questions);
        return Result.success(result);
    }

    @Transactional
    @Override
    public Result submitExam(Long examId, ExamSubmitDTO examSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        //查考试记录
        ExamRecord record = examRecordMapper.selectById(examId);
        if(record == null || !record.getUserId().equals(userId)){
            return Result.error("考试记录不存在");
        }
        //查出所有题目
        List<ExamDetail> details = examDetailMapper.selectByExamId(examId);
        Map<Long, ExamDetail> detailMap = new HashMap<>();
        for (ExamDetail d : details) {
            detailMap.put(d.getId(), d);
        }

        // 逐题判分（只改内存对象，不逐条更新）
        int correct = 0;
        for (ExamSubmitDTO.AnswerItem answer : examSubmitDTO.getAnswers()) {
            ExamDetail examDetail = detailMap.get(answer.getDetailId());
            if (examDetail == null) { continue; }
            examDetail.setUserAnswer(answer.getAnswer());
            if (answer.getAnswer() != null &&
                    answer.getAnswer().trim().equalsIgnoreCase(examDetail.getCorrectAnswer().trim())) {
                examDetail.setIsCorrect(1);
                correct++;
            } else {
                examDetail.setIsCorrect(0);
            }
            // 不再逐条 updateById，collect 起来一起更新
        }

        // 一条 SQL 批量更新所有 exam_detail
        if (!details.isEmpty()) {
            examDetailMapper.batchUpdateAnswers(details);
        }

        //更新考试记录
        record.setCorrectCount(correct);
        BigDecimal score = BigDecimal.valueOf(correct)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(record.getTotalCount()),2, RoundingMode.HALF_UP);
        record.setScore(score);
        record.setStatus(1);//考试完成
        examRecordMapper.updateById(record);

        //返回结果
        Map<String,Object> result = new HashMap<>();
        result.put("totalCount",record.getTotalCount());
        result.put("correctCount",correct);
        result.put("score",score);
        return Result.success(result);
    }

    @Override
    public Result getCurrentExam() {
        Long userId = BaseContext.getCurrentId();
        ExamRecord record = examRecordMapper.selectInProgress(userId);
        if (record == null) {
            return Result.success(null);  // 没有进行中的考试
        }
        return buildExamResult(record);
    }

    @Override
    public Result getExamRecords() {
        Long userId = BaseContext.getCurrentId();
        List<ExamRecord> records = examRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 1)
                        .orderByDesc(ExamRecord::getCreatedAt)
        );
        return Result.success(records);
    }

    @Override
    public Result getExamDetail(Long examId) {
        Long userId = BaseContext.getCurrentId();
        ExamRecord record = examRecordMapper.selectById(examId);
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error("考试记录不存在");
        }
        return buildExamResult(record);
    }
}
