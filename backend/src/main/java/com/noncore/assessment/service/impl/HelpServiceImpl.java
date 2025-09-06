package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.mapper.HelpMapper;
import com.noncore.assessment.service.HelpService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelpServiceImpl implements HelpService {

    private final HelpMapper helpMapper;

    public HelpServiceImpl(HelpMapper helpMapper) {
        this.helpMapper = helpMapper;
    }

    @Override
    public List<HelpCategory> listCategories() {
        return helpMapper.listCategories();
    }

    @Override
    public List<HelpArticle> listArticles(String q, Long categoryId, String tag, String sort) {
        return helpMapper.listArticles(q, categoryId, tag, sort);
    }

    @Override
    public HelpArticle getArticleBySlug(String slug, boolean increaseView) {
        HelpArticle a = helpMapper.getArticleBySlug(slug);
        if (a != null && increaseView) {
            helpMapper.incrementArticleViews(a.getId());
        }
        return a;
    }

    @Override
    public int submitArticleFeedback(HelpArticleFeedback feedback) {
        return helpMapper.insertArticleFeedback(feedback);
    }

    @Override
    public HelpTicket createTicket(Long userId, String title, String description) {
        HelpTicket t = new HelpTicket();
        t.setUserId(userId);
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus("open");
        helpMapper.insertTicket(t);
        return t;
    }

    @Override
    public List<HelpTicket> listMyTickets(Long userId) {
        return helpMapper.listTicketsByUser(userId);
    }

    @Override
    public HelpTicket updateMyTicket(Long userId, Long ticketId, String title, String description) {
        HelpTicket cur = helpMapper.listTicketsByUser(userId).stream()
                .filter(t -> t.getId().equals(ticketId)).findFirst().orElse(null);
        if (cur == null) return null;
        helpMapper.updateTicket(ticketId, title, description);
        return helpMapper.listTicketsByUser(userId).stream()
                .filter(t -> t.getId().equals(ticketId)).findFirst().orElse(null);
    }

    @Override
    public void deleteMyTicket(Long userId, Long ticketId) {
        helpMapper.deleteTicket(ticketId, userId);
    }
}


