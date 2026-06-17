package com.qinyuan.wordlearning.service;

import com.qinyuan.wordlearning.common.Result;
import com.qinyuan.wordlearning.dto.BatchStatusDTO;

public interface WordService {
    //获取本次背诵的单词列表
    Result getStudyWords(Long bookId,Integer count);
    //批量更新单词状态
    Result batchUpdateStatus(BatchStatusDTO batchStatusDTO);
}
