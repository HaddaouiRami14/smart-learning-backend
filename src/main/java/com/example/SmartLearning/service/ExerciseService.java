package com.example.SmartLearning.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.SmartLearning.DTO.CreateExerciseRequest;
import com.example.SmartLearning.DTO.CreateTestCaseRequest;
import com.example.SmartLearning.DTO.ExerciseResponse;
import com.example.SmartLearning.DTO.ExerciseResultResponse;
import com.example.SmartLearning.DTO.SubmitExerciseRequest;
import com.example.SmartLearning.DTO.TestCaseResponse;
import com.example.SmartLearning.DTO.TestResultResponse;
import com.example.SmartLearning.Enum.ProgrammingLanguage;
import com.example.SmartLearning.Repository.ChapterRepository;
import com.example.SmartLearning.Repository.ExerciseRepository;
import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Exercise;
import com.example.SmartLearning.model.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ChapterRepository chapterRepository;
    private final JDoodleService jDoodleService;


    @Transactional
    public ExerciseResponse createExercise(Long courseId, Long chapterId, CreateExerciseRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Exercise exercise = new Exercise();
        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        exercise.setLanguage(request.getLanguage());
        exercise.setStarterCode(request.getStarterCode());
        exercise.setHints(request.getHints());
        exercise.setPoints(request.getPoints());
        exercise.setOrderIndex(request.getOrderIndex());
        exercise.setTimeLimit(request.getTimeLimit());
        exercise.setChapter(chapter);

        
        if (request.getTestCases() != null) {
            for (CreateTestCaseRequest tcReq : request.getTestCases()) {
                TestCase testCase = new TestCase();
                testCase.setInput(tcReq.getInput());
                testCase.setExpectedOutput(tcReq.getExpectedOutput());
                testCase.setIsHidden(tcReq.getIsHidden());
                testCase.setOrderIndex(tcReq.getOrderIndex());
                testCase.setExercise(exercise);
                exercise.getTestCases().add(testCase); 
            }
        }

       
        Exercise savedExercise = exerciseRepository.save(exercise);

        return convertToDTO(savedExercise, true, null);
    }

   
    public List<ExerciseResponse> getExercisesByChapter(Long chapterId, String userEmail) {
        List<Exercise> exercises = exerciseRepository.findByChapterIdOrderByOrderIndexAsc(chapterId);
        return exercises.stream()
            .map(exercise -> convertToDTO(exercise, true, null))
            .collect(Collectors.toList());
    }

    public ExerciseResponse getExerciseById(Long exerciseId, String userEmail, boolean includeHiddenTests) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("Exercise not found"));
        return convertToDTO(exercise, includeHiddenTests, null);
    }

    
    @Transactional
    public ExerciseResponse updateExercise(Long exerciseId, CreateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("Exercise not found"));

        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        exercise.setLanguage(request.getLanguage());
        exercise.setStarterCode(request.getStarterCode());
        exercise.setHints(request.getHints());
        exercise.setPoints(request.getPoints());
        exercise.setOrderIndex(request.getOrderIndex());
        exercise.setTimeLimit(request.getTimeLimit());

        
        exercise.getTestCases().clear();

        
        if (request.getTestCases() != null) {
            for (CreateTestCaseRequest tcReq : request.getTestCases()) {
                TestCase testCase = new TestCase();
                testCase.setInput(tcReq.getInput());
                testCase.setExpectedOutput(tcReq.getExpectedOutput());
                testCase.setIsHidden(tcReq.getIsHidden());
                testCase.setOrderIndex(tcReq.getOrderIndex());
                testCase.setExercise(exercise);
                exercise.getTestCases().add(testCase);
            }
        }

        Exercise updated = exerciseRepository.save(exercise);
        return convertToDTO(updated, true, null);
    }

    
    @Transactional
    public void deleteExercise(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("Exercise not found"));
        exerciseRepository.delete(exercise);
    }

    
    @Transactional
    public ExerciseResultResponse submitExercise(Long exerciseId, String userEmail, SubmitExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("Exercise not found"));

        List<TestResultResponse> testResults = new ArrayList<>();
        int testsPassed = 0;
        double totalExecutionTime = 0.0;

        List<TestCase> testCases = exercise.getTestCases();

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);

            try {
                String resultJson = jDoodleService.executeCode(
                    exercise.getLanguage(),
                    ///request.getCode(),
                    wrapCode(exercise.getLanguage(), request.getCode(), testCase.getInput()),
                    testCase.getInput()
                );

                JSONObject parsedResult = jDoodleService.parseResult(resultJson);

                int statusId                = parsedResult.getInt("status_id");
                String statusDescription    = parsedResult.getString("status_description");
                String actualOutput         = parsedResult.optString("stdout", "");
                String stderr               = parsedResult.optString("stderr", "");
                String compileOutput        = parsedResult.optString("compile_output", "");
                double executionTime        = parsedResult.optDouble("time", 0.0);

                totalExecutionTime += executionTime;

                boolean passed = false;
                String errorMessage = null;

                if (statusId == 3) {
                   
                    passed = actualOutput.trim().equals(testCase.getExpectedOutput().trim());
                    if (!passed) {
                        errorMessage = "Wrong answer";
                    }
                } else if (statusId == 6) {
                    
                    errorMessage = "Compilation Error: " + compileOutput;
                } else if (statusId == 11) {
                    
                    errorMessage = "Runtime Error: " + stderr;
                } else if (statusId == 5) {
                    errorMessage = "Time Limit Exceeded";
                } else {
                    errorMessage = statusDescription;
                }

                if (passed) {
                    testsPassed++;
                }

                testResults.add(TestResultResponse.builder()
                    .testNumber(i + 1)
                    .passed(passed)
                    .input(testCase.getIsHidden() ? null : testCase.getInput())
                    .expectedOutput(testCase.getIsHidden() ? null : testCase.getExpectedOutput())
                    .actualOutput(testCase.getIsHidden() ? null : actualOutput)
                    .isHidden(testCase.getIsHidden())
                    .status(statusDescription)
                    .errorMessage(errorMessage)
                    .build());

            } catch (Exception e) {
                testResults.add(TestResultResponse.builder()
                    .testNumber(i + 1)
                    .passed(false)
                    .isHidden(testCase.getIsHidden())
                    .status("Error")
                    .errorMessage("Execution error: " + e.getMessage())
                    .build());
            }
        }

        int totalTests = testCases.size();
        boolean passed = testsPassed == totalTests;
        int score = passed ? exercise.getPoints() : 0;

        return ExerciseResultResponse.builder()
            .exerciseId(exercise.getId())
            .testsPassed(testsPassed)
            .totalTests(totalTests)
            .score(score)
            .passed(passed)
            .executionTime(totalExecutionTime)
            .testResults(testResults)
            .build();
    }

    
    private ExerciseResponse convertToDTO(Exercise exercise, boolean includeHiddenTests, Object apprenant) {
        List<TestCaseResponse> testCaseResponses = exercise.getTestCases().stream()
            .filter(tc -> includeHiddenTests || !tc.getIsHidden())
            .map(tc -> TestCaseResponse.builder()
                .id(tc.getId())
                .input(tc.getInput())
                .expectedOutput(includeHiddenTests ? tc.getExpectedOutput() : null)
                .isHidden(tc.getIsHidden())
                .orderIndex(tc.getOrderIndex())
                .build())
            .collect(Collectors.toList());

        return ExerciseResponse.builder()
            .id(exercise.getId())
            .title(exercise.getTitle())
            .description(exercise.getDescription())
            .language(exercise.getLanguage())
            .starterCode(exercise.getStarterCode())
            .hints(exercise.getHints())
            .points(exercise.getPoints())
            .orderIndex(exercise.getOrderIndex())
            .timeLimit(exercise.getTimeLimit())
            .chapterId(exercise.getChapter().getId())
            .testCases(testCaseResponses)
            .isCompleted(null)
            .bestScore(null)
            .build();
    }
    private String wrapCode(ProgrammingLanguage language, String userCode, String input) {
    switch (language) {
        case PYTHON:
            return userCode + "\nn = int(input())\nprint(solution(n))";
        case JAVASCRIPT:
            return userCode + "\nconst lines = require('fs').readFileSync('/dev/stdin','utf8').trim().split('\\n');\nconsole.log(solution(parseInt(lines[0])));";
        case JAVA:
            return userCode; 
        default:
            return userCode;
    }
}
}