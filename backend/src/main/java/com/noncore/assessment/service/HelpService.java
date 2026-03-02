package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.HelpArticleUpsertRequest;
import com.noncore.assessment.dto.request.HelpCategoryCreateRequest;
import com.noncore.assessment.dto.request.HelpTicketCreateRequest;
import com.noncore.assessment.dto.response.HelpTicketDetailResponse;
import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.util.PageResult;

import java.util.List;

public interface HelpService {
    List<HelpCategory> listCategories();
    HelpCategory createCategoryForAdmin(HelpCategoryCreateRequest request);
    List<HelpArticle> listArticles(String q, Long categoryId, String tag, String sort);
    List<HelpArticle> listArticlesForAdmin(String q, Long categoryId, Boolean published);
    HelpArticle getArticleBySlug(String slug, boolean increaseView);
    HelpArticle createArticleForAdmin(HelpArticleUpsertRequest request);
    HelpArticle updateArticleForAdmin(Long articleId, HelpArticleUpsertRequest request);
    void deleteArticleForAdmin(Long articleId);
    HelpArticle submitArticleFeedback(HelpArticleFeedback feedback);
    HelpTicket createTicket(Long userId, String sourceRole, HelpTicketCreateRequest request);
    List<HelpTicket> listMyTickets(Long userId);
    HelpTicketDetailResponse getMyTicketDetail(Long userId, Long ticketId);
    HelpTicketDetailResponse replyMyTicket(Long userId, String sourceRole, Long ticketId, String content);
    HelpTicket updateMyTicket(Long userId, Long ticketId, HelpTicketCreateRequest request);
    void deleteMyTicket(Long userId, Long ticketId);
    PageResult<HelpTicket> pageTicketsForAdmin(Integer page, Integer size, String status, String channel, String priority, String sourceRole, String keyword);
    HelpTicketDetailResponse getAdminTicketDetail(Long ticketId);
    HelpTicketDetailResponse adminReplyTicket(Long adminId, String adminRole, Long ticketId, String content);
    HelpTicket updateTicketStatus(Long adminId, Long ticketId, String status);
}
