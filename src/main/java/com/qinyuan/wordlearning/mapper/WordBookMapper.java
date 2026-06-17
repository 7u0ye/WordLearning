package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.dto.WordWithStatusDTO;
import com.qinyuan.wordlearning.entity.WordBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WordBookMapper extends BaseMapper<WordBook> {
    List<WordWithStatusDTO> selectWordsWithStatus(@Param("bookId") Long bookId, @Param("userId") Long userId);
}
