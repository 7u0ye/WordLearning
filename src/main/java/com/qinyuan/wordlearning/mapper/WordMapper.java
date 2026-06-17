package com.qinyuan.wordlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qinyuan.wordlearning.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface WordMapper extends BaseMapper<Word> {

    //根据单词书Id查该书的全部单词
    @Select("select * from word where book_id = #{bookId} ")
    List<Word> selectByWordBookId(@Param("bookId") Long wordBookId);
}
