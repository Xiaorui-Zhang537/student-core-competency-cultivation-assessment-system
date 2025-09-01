package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChapterMapper {
    int insertChapter(Chapter chapter);
    List<Chapter> selectChaptersByCourse(@Param("courseId") Long courseId);
    int updateChapter(Chapter chapter);
    int deleteChapter(@Param("id") Long id);
}


