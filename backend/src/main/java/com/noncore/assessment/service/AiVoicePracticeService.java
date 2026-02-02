package com.noncore.assessment.service;

import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.AiVoiceTurn;

import java.util.List;

public interface AiVoicePracticeService {

    AiVoiceSession createSession(Long userId, String title, String model, String mode, String locale, String scenario);

    AiVoiceSession getSession(Long userId, Long sessionId);

    List<AiVoiceSession> listSessions(Long userId, String q, Integer page, Integer size);

    AiVoiceSession updateSession(Long userId, Long sessionId, String title, Boolean pinned);

    void deleteSession(Long userId, Long sessionId);

    AiVoiceTurn appendTurn(Long userId,
                           Long sessionId,
                           String model,
                           String userTranscript,
                           String assistantText,
                           Long userAudioFileId,
                           Long assistantAudioFileId,
                           String locale,
                           String scenario);

    List<AiVoiceTurn> listTurns(Long userId, Long sessionId, Integer page, Integer size);
}

