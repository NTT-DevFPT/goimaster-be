package com.goimaster.service;

import com.goimaster.dto.request.CreateQuizSessionRequest;
import com.goimaster.model.QuizSession;
import com.goimaster.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuizSessionService {

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private StudyStreakService studyStreakService;

    public List<QuizSession> getQuizSessionsByLessonId(UUID lessonId) {
        return quizSessionRepository.findByLessonIdOrderByCompletedAtDesc(lessonId);
    }

    public List<QuizSession> getQuizSessionsByUserAndLesson(UUID userId, UUID lessonId) {
        return quizSessionRepository.findByUserIdAndLessonIdOrderByCompletedAtDesc(userId, lessonId);
    }

    public QuizSession createQuizSession(UUID userId, UUID lessonId, CreateQuizSessionRequest request) {
        QuizSession session = new QuizSession();
        session.setUserId(userId);
        session.setLessonId(lessonId);
        session.setMode(request.getMode());
        session.setDurationSeconds(request.getDurationSeconds());
        session.setTotalQuestions(request.getTotalQuestions());
        session.setCorrectAnswers(request.getCorrectAnswers());
        session.setStartedAt(LocalDateTime.now());
        session.setCompletedAt(LocalDateTime.now());

        if (request.getDetails() != null) {
            List<com.goimaster.model.QuizSessionDetail> details = request.getDetails().stream()
                    .map(detailReq -> {
                        com.goimaster.model.QuizSessionDetail detail = new com.goimaster.model.QuizSessionDetail();
                        detail.setSession(session);
                        detail.setWordId(detailReq.getWordId());
                        detail.setCorrect(detailReq.isCorrect());
                        detail.setKanji(detailReq.getKanji());
                        detail.setMeaning(detailReq.getMeaning());
                        return detail;
                    })
                    .collect(java.util.stream.Collectors.toList());
            session.getDetails().addAll(details);
        }

        QuizSession saved = quizSessionRepository.save(session);

        // Record study streak
        studyStreakService.recordStudySession(userId);

        return saved;
    }

    public QuizSession getQuizSessionById(UUID sessionId) {
        return quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz session not found"));
    }
}
