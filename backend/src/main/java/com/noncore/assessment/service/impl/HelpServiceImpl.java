package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.HelpTicketCreateRequest;
import com.noncore.assessment.dto.response.HelpTicketDetailResponse;
import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.entity.HelpTicketMessage;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.HelpMapper;
import com.noncore.assessment.service.HelpService;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class HelpServiceImpl implements HelpService {

    private final HelpMapper helpMapper;
    private final NotificationService notificationService;

    public HelpServiceImpl(HelpMapper helpMapper, NotificationService notificationService) {
        this.helpMapper = helpMapper;
        this.notificationService = notificationService;
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
    public HelpTicket createTicket(Long userId, String sourceRole, HelpTicketCreateRequest request) {
        String content = requireContent(request);
        String title = requireTitle(request == null ? null : request.getTitle());
        HelpTicket t = new HelpTicket();
        t.setUserId(userId);
        t.setTitle(title);
        t.setDescription(content);
        t.setChannel(normalizeChannel(request.getChannel()));
        t.setTicketType(normalizeTicketType(request.getTicketType()));
        t.setPriority(normalizePriority(request.getPriority()));
        t.setSourceRole(normalizeRole(sourceRole));
        t.setContact(request.getContact());
        t.setAnonymous(Boolean.TRUE.equals(request.getAnonymous()));
        t.setStatus("open");
        t.setLastReplyAt(LocalDateTime.now());
        helpMapper.insertTicket(t);
        helpMapper.insertTicketMessage(HelpTicketMessage.builder()
                .ticketId(t.getId())
                .senderId(userId)
                .senderRole(t.getSourceRole())
                .senderSide("user")
                .content(content)
                .build());
        return t;
    }

    @Override
    public List<HelpTicket> listMyTickets(Long userId) {
        return helpMapper.listTicketsByUser(userId);
    }

    @Override
    public HelpTicketDetailResponse getMyTicketDetail(Long userId, Long ticketId) {
        HelpTicket ticket = helpMapper.selectTicketByIdAndUser(ticketId, userId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限查看该工单");
        }
        return toDetail(ticket);
    }

    @Override
    public HelpTicketDetailResponse replyMyTicket(Long userId, String sourceRole, Long ticketId, String content) {
        HelpTicket ticket = helpMapper.selectTicketByIdAndUser(ticketId, userId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限回复该工单");
        }
        String body = requireContent(content);
        helpMapper.insertTicketMessage(HelpTicketMessage.builder()
                .ticketId(ticketId)
                .senderId(userId)
                .senderRole(normalizeRole(sourceRole))
                .senderSide("user")
                .content(body)
                .build());
        HelpTicket update = new HelpTicket();
        update.setId(ticketId);
        update.setDescription(body);
        update.setStatus("open");
        update.setClosedAt(null);
        update.setLastReplyAt(LocalDateTime.now());
        helpMapper.updateTicket(update);
        return getMyTicketDetail(userId, ticketId);
    }

    @Override
    public HelpTicket updateMyTicket(Long userId, Long ticketId, HelpTicketCreateRequest request) {
        HelpTicket cur = helpMapper.selectTicketByIdAndUser(ticketId, userId);
        if (cur == null) return null;
        if (!"open".equals(cur.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "仅 open 状态的支持单可编辑");
        }
        HelpTicket update = new HelpTicket();
        update.setId(ticketId);
        update.setTitle(requireTitle(request == null ? null : request.getTitle()));
        String content = request.getResolvedContent();
        if (content != null && !content.isBlank()) {
            update.setDescription(content.trim());
        }
        update.setChannel(normalizeChannel(request.getChannel()));
        update.setTicketType(normalizeTicketType(request.getTicketType()));
        update.setPriority(normalizePriority(request.getPriority()));
        update.setContact(request.getContact());
        update.setAnonymous(request.getAnonymous());
        helpMapper.updateTicket(update);
        return helpMapper.selectTicketByIdAndUser(ticketId, userId);
    }

    @Override
    public void deleteMyTicket(Long userId, Long ticketId) {
        helpMapper.deleteTicket(ticketId, userId);
    }

    @Override
    public PageResult<HelpTicket> pageTicketsForAdmin(Integer page, Integer size, String status, String channel, String priority, String sourceRole, String keyword) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : Math.min(size, 100);
        int offset = (safePage - 1) * safeSize;
        List<HelpTicket> items = helpMapper.pageTicketsForAdmin(
                blankToNull(status),
                blankToNull(channel),
                blankToNull(priority),
                blankToNull(normalizeRole(sourceRole)),
                blankToNull(keyword),
                offset,
                safeSize
        );
        long total = helpMapper.countTicketsForAdmin(
                blankToNull(status),
                blankToNull(channel),
                blankToNull(priority),
                blankToNull(normalizeRole(sourceRole)),
                blankToNull(keyword)
        );
        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil(total / (double) safeSize);
        return PageResult.of(items, safePage, safeSize, total, totalPages);
    }

    @Override
    public HelpTicketDetailResponse getAdminTicketDetail(Long ticketId) {
        HelpTicket ticket = helpMapper.selectTicketById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "工单不存在");
        }
        return toDetail(ticket);
    }

    @Override
    public HelpTicketDetailResponse adminReplyTicket(Long adminId, String adminRole, Long ticketId, String content) {
        HelpTicket ticket = helpMapper.selectTicketById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "工单不存在");
        }
        String body = requireContent(content);
        helpMapper.insertTicketMessage(HelpTicketMessage.builder()
                .ticketId(ticketId)
                .senderId(adminId)
                .senderRole(normalizeRole(adminRole))
                .senderSide("admin")
                .content(body)
                .build());
        HelpTicket update = new HelpTicket();
        update.setId(ticketId);
        update.setDescription(body);
        update.setStatus("in_progress");
        update.setAssigneeAdminId(adminId);
        update.setClosedAt(null);
        update.setLastReplyAt(LocalDateTime.now());
        helpMapper.updateTicket(update);
        notifyTicketOwner(ticket, adminId, body);
        return getAdminTicketDetail(ticketId);
    }

    @Override
    public HelpTicket updateTicketStatus(Long adminId, Long ticketId, String status) {
        HelpTicket ticket = helpMapper.selectTicketById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "工单不存在");
        }
        String resolvedStatus = normalizeStatus(status);
        HelpTicket update = new HelpTicket();
        update.setId(ticketId);
        update.setStatus(resolvedStatus);
        update.setAssigneeAdminId(adminId);
        update.setClosedAt(("resolved".equals(resolvedStatus) || "closed".equals(resolvedStatus)) ? LocalDateTime.now() : null);
        helpMapper.updateTicket(update);
        return helpMapper.selectTicketById(ticketId);
    }

    private HelpTicketDetailResponse toDetail(HelpTicket ticket) {
        return new HelpTicketDetailResponse(ticket, helpMapper.listTicketMessages(ticket.getId()));
    }

    private void notifyTicketOwner(HelpTicket ticket, Long adminId, String body) {
        try {
            String brief = body.length() > 80 ? body.substring(0, 80) + "..." : body;
            notificationService.sendNotification(
                    ticket.getUserId(),
                    adminId,
                    "支持单有新回复",
                    brief,
                    "system",
                    "helpTicketReply",
                    "normal",
                    "help_ticket",
                    ticket.getId()
            );
        } catch (Exception ignore) {
        }
    }

    private String requireContent(HelpTicketCreateRequest request) {
        return requireContent(request == null ? null : request.getResolvedContent());
    }

    private String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "支持单标题不能为空");
        }
        return title.trim();
    }

    private String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "支持单内容不能为空");
        }
        return content.trim();
    }

    private String normalizeChannel(String channel) {
        String value = blankToNull(channel);
        if (value == null) return "support";
        value = value.toLowerCase(Locale.ROOT);
        return "feedback".equals(value) ? "feedback" : "support";
    }

    private String normalizeTicketType(String ticketType) {
        String value = blankToNull(ticketType);
        if (value == null) return "other";
        return value.trim();
    }

    private String normalizePriority(String priority) {
        String value = blankToNull(priority);
        if (value == null) return "medium";
        value = value.toLowerCase(Locale.ROOT);
        return switch (value) {
            case "low", "medium", "high", "urgent" -> value;
            default -> "medium";
        };
    }

    private String normalizeStatus(String status) {
        String value = blankToNull(status);
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "状态不能为空");
        }
        value = value.toLowerCase(Locale.ROOT);
        return switch (value) {
            case "open", "in_progress", "resolved", "closed" -> value;
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的状态: " + value);
        };
    }

    private String normalizeRole(String role) {
        String value = blankToNull(role);
        if (value == null) return null;
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("role_")) {
            value = value.substring(5);
        }
        return value;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
