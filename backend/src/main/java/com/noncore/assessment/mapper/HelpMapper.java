package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HelpMapper {
    List<HelpCategory> listCategories();

    List<HelpArticle> listArticles(@Param("q") String q,
                                   @Param("categoryId") Long categoryId,
                                   @Param("tag") String tag,
                                   @Param("sort") String sort);

    HelpArticle getArticleBySlug(@Param("slug") String slug);

    HelpArticle getArticleById(@Param("id") Long id);

    void incrementArticleViews(@Param("id") Long id);

    int insertArticleFeedback(HelpArticleFeedback feedback);

    int insertTicket(HelpTicket ticket);

    List<HelpTicket> listTicketsByUser(@Param("userId") Long userId);

    int updateTicket(@Param("id") Long id, @Param("title") String title, @Param("description") String description);

    int deleteTicket(@Param("id") Long id, @Param("userId") Long userId);
}


