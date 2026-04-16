package com.example.SmartLearning.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.SmartLearning.DTO.CreateQuestionOptionRequest;
import com.example.SmartLearning.DTO.CreateQuestionRequest;
import com.example.SmartLearning.DTO.CreateQuizRequest;
import com.example.SmartLearning.DTO.QuestionOptionResponse;
import com.example.SmartLearning.DTO.QuestionResponse;
import com.example.SmartLearning.DTO.QuestionResultResponse;
import com.example.SmartLearning.DTO.QuizResponse;
import com.example.SmartLearning.DTO.QuizResultResponse;
import com.example.SmartLearning.DTO.SubmitQuizRequest;
import com.example.SmartLearning.Enum.QuestionType;
import com.example.SmartLearning.Repository.ChapterRepository;
import com.example.SmartLearning.Repository.QuestionOptionRepository;
import com.example.SmartLearning.Repository.QuestionRepository;
import com.example.SmartLearning.Repository.QuizRepository;
import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Question;
import com.example.SmartLearning.model.QuestionOption;
import com.example.SmartLearning.model.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ChapterRepository chapterRepository;
    private final BadgeService badgeService; // ✅ NEW
    
    @Transactional
    public QuizResponse createQuiz(Long courseId, Long chapterId, CreateQuizRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        if (quizRepository.findByChapterId(chapterId).isPresent()) {
            throw new RuntimeException("Quiz already exists for this chapter");
        }
        
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setChapter(chapter);
        
        Quiz savedQuiz = quizRepository.save(quiz);
        
        List<Question> questions = createQuestions(savedQuiz, request.getQuestions());
        savedQuiz.setQuestions(questions);
        
        return convertToDTO(savedQuiz, true);
    }
    
    public QuizResponse getQuizByChapterId(Long chapterId, boolean includeCorrectAnswers) {
        Quiz quiz = quizRepository.findByChapterId(chapterId).orElse(null);
        
        if (quiz == null) {
            return null;
        }
        
        return convertToDTO(quiz, includeCorrectAnswers);
    }
    
    @Transactional
    public QuizResponse updateQuiz(Long quizId, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setPassingScore(request.getPassingScore());

        quiz.getQuestions().clear();
        quizRepository.saveAndFlush(quiz);

        for (CreateQuestionRequest qReq : request.getQuestions()) {
            Question question = new Question();
            question.setQuestionText(qReq.getQuestionText());
            question.setQuestionType(qReq.getQuestionType());
            question.setOrderIndex(qReq.getOrderIndex());
            question.setPoints(qReq.getPoints());
            question.setQuiz(quiz);

            if (qReq.getQuestionType() == QuestionType.SHORT_ANSWER ||
                qReq.getQuestionType() == QuestionType.EDITOR_ANSWER) {
                question.setCorrectAnswer(qReq.getCorrectAnswer());
                question.setOptions(new ArrayList<>());
            } else {
                question.setCorrectAnswer(null);
                question.setOptions(new ArrayList<>()); 
            }

            questionRepository.save(question);

            if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                List<QuestionOption> options = new ArrayList<>();
                for (CreateQuestionOptionRequest oReq : qReq.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionText(oReq.getOptionText());
                    option.setIsCorrect(oReq.getIsCorrect());
                    option.setOrderIndex(oReq.getOrderIndex());
                    option.setQuestion(question);
                    options.add(questionOptionRepository.save(option));
                }
                question.setOptions(options);
                questionRepository.save(question); 
            }

            quiz.getQuestions().add(question);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToDTO(updatedQuiz, true);
    }
    
    @Transactional
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found: " + quizId);
        }
        questionOptionRepository.deleteByQuizId(quizId);
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteByIdNative(quizId);
    }
    
    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, SubmitQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        List<Question> questions = questionRepository.findByQuizOrderByOrderIndexAsc(quiz);
        
        int correctAnswers = 0;
        List<QuestionResultResponse> results = new ArrayList<>();
        
        for (Question question : questions) {
            boolean isCorrect = false;
            Long selectedOptionId = null;
            Long correctOptionId = null;
            String textAnswer = null;
            String correctAnswer = null;
            
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {

                List<Long> selectedOptionIds = request.getMultiAnswers() != null
                    ? request.getMultiAnswers().getOrDefault(question.getId(), new ArrayList<>())
                    : new ArrayList<>();

                List<Long> correctOptionIds = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(QuestionOption::getId)
                    .collect(Collectors.toList());

                isCorrect = !selectedOptionIds.isEmpty()
                    && selectedOptionIds.size() == correctOptionIds.size()
                    && selectedOptionIds.containsAll(correctOptionIds);

                correctOptionId = null;
                selectedOptionId = null;

            } else if (question.getQuestionType() == QuestionType.TRUE_FALSE) {

                if (request.getAnswers() != null) {
                    selectedOptionId = request.getAnswers().get(question.getId());
                }

                QuestionOption correctOption = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .findFirst()
                    .orElse(null);

                correctOptionId = correctOption != null ? correctOption.getId() : null;
                isCorrect = correctOption != null
                    && selectedOptionId != null
                    && correctOption.getId().equals(selectedOptionId);

            } else {
                if (request.getTextAnswers() != null) {
                    textAnswer = request.getTextAnswers().get(question.getId());
                }
                correctAnswer = question.getCorrectAnswer();
                if (textAnswer != null && correctAnswer != null) {
                    isCorrect = normalizeAnswer(textAnswer).equals(normalizeAnswer(correctAnswer));
                }
            }
            
            if (isCorrect) {
                correctAnswers++;
            }
            
            List<Long> selectedOptionIds = question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                && request.getMultiAnswers() != null
                ? request.getMultiAnswers().getOrDefault(question.getId(), new ArrayList<>())
                : new ArrayList<>();

            List<Long> correctOptionIds = question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                ? question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(QuestionOption::getId)
                    .collect(Collectors.toList())
                : new ArrayList<>();

            results.add(QuestionResultResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .selectedOptionId(selectedOptionId)
                .selectedOptionIds(selectedOptionIds)
                .correctOptionId(correctOptionId)
                .correctOptionIds(correctOptionIds)
                .textAnswer(textAnswer)
                .correctAnswer(correctAnswer)
                .isCorrect(isCorrect)
                .build());
        }
        
        int totalQuestions = questions.size();
        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        boolean passed = score >= quiz.getPassingScore();

        // ✅ NEW: Award badges based on quiz result
        if (passed && request.getApprenantId() != null) {
            try {
                // Award QUIZ_MASTER if score is 100%
                badgeService.onQuizPassed(request.getApprenantId(), score);
                // Check all other badges too
                badgeService.checkAndAwardBadges(request.getApprenantId());
            } catch (Exception e) {
                // Don't fail quiz submission if badge awarding fails
                System.err.println("Badge awarding failed: " + e.getMessage());
            }
        }
        
        return QuizResultResponse.builder()
            .quizId(quiz.getId())
            .totalQuestions(totalQuestions)
            .correctAnswers(correctAnswers)
            .score(score)
            .passed(passed)
            .passingScore(quiz.getPassingScore())
            .questionResults(results)
            .build();
    }
    
    private List<Question> createQuestions(Quiz quiz, List<CreateQuestionRequest> questionRequests) {
        List<Question> questions = new ArrayList<>();
        
        for (CreateQuestionRequest qReq : questionRequests) {
            Question question = new Question();
            question.setQuestionText(qReq.getQuestionText());
            question.setQuestionType(qReq.getQuestionType());
            question.setOrderIndex(qReq.getOrderIndex());
            question.setPoints(qReq.getPoints());
            question.setQuiz(quiz);
            
            if (qReq.getQuestionType() == com.example.SmartLearning.Enum.QuestionType.SHORT_ANSWER ||
                qReq.getQuestionType() == com.example.SmartLearning.Enum.QuestionType.EDITOR_ANSWER) {
                question.setCorrectAnswer(qReq.getCorrectAnswer());
                question.setOptions(new ArrayList<>());
            } else {
                question.setOptions(new ArrayList<>());
            }
            
            Question savedQuestion = questionRepository.save(question);
            
            if (qReq.getQuestionType() != com.example.SmartLearning.Enum.QuestionType.SHORT_ANSWER &&
                qReq.getQuestionType() != com.example.SmartLearning.Enum.QuestionType.EDITOR_ANSWER &&
                qReq.getOptions() != null) {
                
                List<QuestionOption> options = new ArrayList<>();
                for (CreateQuestionOptionRequest oReq : qReq.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionText(oReq.getOptionText());
                    option.setIsCorrect(oReq.getIsCorrect());
                    option.setOrderIndex(oReq.getOrderIndex());
                    option.setQuestion(savedQuestion); 
                    options.add(questionOptionRepository.save(option));
                }
                savedQuestion.setOptions(options);
            }
            
            questions.add(savedQuestion);
        }
        
        return questions;
    }
    
    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        String normalized = answer.replaceAll("<[^>]*>", "").trim();
        return normalized.toLowerCase();
    }
    
    private QuizResponse convertToDTO(Quiz quiz, boolean includeCorrectAnswers) {
        List<QuestionResponse> questionResponses = quiz.getQuestions().stream()
            .map(q -> QuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType())
                .orderIndex(q.getOrderIndex())
                .points(q.getPoints())
                .correctAnswer(includeCorrectAnswers ? q.getCorrectAnswer() : null)
                .options(q.getOptions() != null ? q.getOptions().stream()
                    .map(o -> QuestionOptionResponse.builder()
                        .id(o.getId())
                        .optionText(o.getOptionText())
                        .isCorrect(includeCorrectAnswers ? o.getIsCorrect() : null)
                        .orderIndex(o.getOrderIndex())
                        .build())
                    .collect(Collectors.toList()) : new ArrayList<>())
                .build())
            .collect(Collectors.toList());
        
        return QuizResponse.builder()
            .id(quiz.getId())
            .title(quiz.getTitle())
            .description(quiz.getDescription())
            .passingScore(quiz.getPassingScore())
            .chapterId(quiz.getChapter().getId())
            .questions(questionResponses)
            .build();
    }
}