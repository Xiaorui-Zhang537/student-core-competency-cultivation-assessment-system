package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Chapter;
import com.noncore.assessment.mapper.ChapterMapper;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapter Management")
public class ChapterController {

    private final ChapterMapper chapterMapper;

    @GetMapping("/course/{courseId}")
    @Operation(summary = "List chapters of a course")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<List<Chapter>> listByCourse(@PathVariable Long courseId) {
        return ApiResponse.success(chapterMapper.selectChaptersByCourse(courseId));
    }

    @PostMapping
    @Operation(summary = "Create chapter")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Chapter> create(@RequestBody Chapter chapter) {
        chapterMapper.insertChapter(chapter);
        return ApiResponse.success(chapter);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update chapter")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Chapter chapter) {
        chapter.setId(id);
        chapterMapper.updateChapter(chapter);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete chapter")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chapterMapper.deleteChapter(id);
        return ApiResponse.success();
    }
}


