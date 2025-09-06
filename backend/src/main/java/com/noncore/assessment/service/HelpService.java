package com.noncore.assessment.service;

import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;

import java.util.List;

public interface HelpService {
    List<HelpCategory> listCategories();
    List<HelpArticle> listArticles(String q, Long categoryId, String tag, String sort);
    HelpArticle getArticleBySlug(String slug, boolean increaseView);
    int submitArticleFeedback(HelpArticleFeedback feedback);
    HelpTicket createTicket(Long userId, String title, String description);
    List<HelpTicket> listMyTickets(Long userId);
    HelpTicket updateMyTicket(Long userId, Long ticketId, String title, String description);
    void deleteMyTicket(Long userId, Long ticketId);
}


