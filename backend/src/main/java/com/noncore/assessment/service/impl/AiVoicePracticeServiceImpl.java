package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.AiVoiceTurn;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AiVoiceSessionMapper;
import com.noncore.assessment.mapper.AiVoiceTurnMapper;
import com.noncore.assessment.service.AiVoicePracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AiVoicePracticeServiceImpl implements AiVoicePracticeService {

    private final AiVoiceSessionMapper sessionMapper;
    private final AiVoiceTurnMapper turnMapper;

    @Override
    public AiVoiceSession createSession(Long userId, String title, String model, String mode, String locale, String scenario) {
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED_OPERATION, "未登录");
        AiVoiceSession s = AiVoiceSession.builder()
                .userId(userId)
                .title(title == null ? null : title.trim())
                .model(model == null ? null : model.trim())
                .mode(mode == null ? null : mode.trim())
                .locale(locale == null ? null : locale.trim())
                .scenario(scenario == null ? null : scenario.trim())
                .build();
        sessionMapper.insert(s);
        return s;
    }

    @Override
    @Transactional(readOnly = true)
    public AiVoiceSession getSession(Long userId, Long sessionId) {
        if (sessionId == null) throw new BusinessException(ErrorCode.INVALID_PARAMETER, "sessionId 不能为空");
        AiVoiceSession s = sessionMapper.selectByIdAndUser(sessionId, userId);
        if (s == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "语音会话不存在");
        return s;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiVoiceSession> listSessions(Long userId, String q, Integer page, Integer size) {
        int sz = size == null ? 20 : Math.min(50, Math.max(1, size));
        int pg = page == null ? 1 : Math.max(1, page);
        int offset = (pg - 1) * sz;
        String qq = (q == null || q.isBlank()) ? null : q.trim();
        return sessionMapper.listByUser(userId, qq, offset, sz);
    }

    @Override
    public AiVoiceSession updateSession(Long userId, Long sessionId, String title, Boolean pinned) {
        AiVoiceSession s = getSession(userId, sessionId);

        boolean changed = false;
        if (title != null) {
            String t = title.trim();
            if (t.isBlank()) throw new BusinessException(ErrorCode.INVALID_PARAMETER, "title 不能为空");
            int ok = sessionMapper.updateTitle(sessionId, userId, t);
            if (ok <= 0) throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新会话标题失败");
            changed = true;
        }
        if (pinned != null) {
            int ok = sessionMapper.updatePinned(sessionId, userId, pinned);
            if (ok <= 0) throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新会话置顶状态失败");
            changed = true;
        }
        if (!changed) return s;
        return getSession(userId, sessionId);
    }

    @Override
    public void deleteSession(Long userId, Long sessionId) {
        AiVoiceSession s = getSession(userId, sessionId);
        int ok = sessionMapper.softDelete(s.getId(), userId);
        if (ok <= 0) throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除会话失败");
    }

    @Override
    public AiVoiceTurn appendTurn(Long userId,
                                  Long sessionId,
                                  String model,
                                  String userTranscript,
                                  String assistantText,
                                  Long userAudioFileId,
                                  Long assistantAudioFileId,
                                  String locale,
                                  String scenario) {
        // ownership check
        AiVoiceSession s = getSession(userId, sessionId);

        String uText = userTranscript == null ? "" : userTranscript.trim();
        String aText = assistantText == null ? "" : assistantText.trim();
        if (uText.isBlank() && userAudioFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "至少需要提供 userTranscript 或 userAudioFileId");
        }

        // turn insert
        AiVoiceTurn t = AiVoiceTurn.builder()
                .sessionId(s.getId())
                .userId(userId)
                .userTranscript(uText)
                .assistantText(aText)
                .userAudioFileId(userAudioFileId)
                .assistantAudioFileId(assistantAudioFileId)
                .build();
        turnMapper.insert(t);
        return t;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiVoiceTurn> listTurns(Long userId, Long sessionId, Integer page, Integer size) {
        getSession(userId, sessionId); // ownership check
        int sz = size == null ? 200 : Math.min(500, Math.max(1, size));
        int pg = page == null ? 1 : Math.max(1, page);
        int offset = (pg - 1) * sz;
        return turnMapper.listBySession(sessionId, userId, offset, sz);
    }
}

