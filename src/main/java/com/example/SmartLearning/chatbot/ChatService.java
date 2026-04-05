package com.example.SmartLearning.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.SmartLearning.DTO.ChatRequest;
import com.example.SmartLearning.DTO.ChatResponse;
import com.example.SmartLearning.DTO.RecommendationsResponse;
import com.example.SmartLearning.DTO.RecommendedCourseDto;
import com.example.SmartLearning.recommendation.RecommendationService;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.SkillsProgressService;

import java.util.ArrayList;
import java.util.List;
 
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
 
    private final RecommendationService  recommendationService;
    private final SkillsProgressService  skillsProgressService;
    private final OpenRouterClient       openRouterClient;
 
    
    public ChatResponse chat(ChatRequest request) {
 
        // --- CHANGEMENT ICI : Récupérer l'ID depuis la connexion sécurisée ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long apprenantId = null;

        if (authentication != null && authentication.getPrincipal() instanceof JwtUserPrincipal) {
            JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
            // Assurez-vous que votre classe JwtUserPrincipal a une méthode getUserId() ou getId()
            apprenantId = principal.getId(); 
        } else {
            // Fallback si l'auth n'est pas du type attendu (rare avec votre config)
            log.error("Impossible de récupérer l'ID de l'utilisateur connecté");
            return buildErrorResponse("Erreur d'authentification.");
        }
        
        if (apprenantId == null) {
            log.error("L'ID de l'utilisateur est null après récupération");
            return buildErrorResponse("Utilisateur inconnu.");
        }
         // ── 1. Données de l'algorithme pondéré ───────────────────────────────
        var recommendations = recommendationService.recommend(apprenantId, 5, null);
        var dashboard = skillsProgressService.getDashboard(apprenantId);

        // ... Le reste de votre code reste identique ...
        
        // ── 2. Construction du system prompt ─────────────────────────────────
        String systemPrompt = buildSystemPrompt(dashboard, recommendations);

        // ── 3. Appel OpenRouter ───────────────────────────────────────────────
        String llmReply = openRouterClient.chat(systemPrompt, request.getMessage());

        if (llmReply == null) {
            return buildFallbackResponse(recommendations);
        }

        // ── 4. Construit la réponse finale ────────────────────────────────────
        var courseCards = mapToCourseCards(recommendations.getItems(), llmReply);

        return ChatResponse.builder()
                .reply(llmReply)
                .recommendations(courseCards)
                .build();
    }

 
    // ─── System prompt ────────────────────────────────────────────────────────
 
    private String buildSystemPrompt(
            com.example.SmartLearning.DTO.SkillProgressDTO.SkillsDashboardDTO dashboard,
            RecommendationsResponse recommendations
    ) {
        var sb = new StringBuilder();
 
        sb.append("""
            Tu es un assistant pédagogique de la plateforme SmartLearning.
            Tu aides les apprenants à choisir leurs prochains cours en fonction de leur profil.
            Réponds toujours en français, de manière encourageante et concise.
            Quand tu recommandes des cours, cite leur titre EXACTEMENT tel qu'il apparaît
            dans la liste ci-dessous et explique brièvement pourquoi ce cours correspond
            au profil de l'apprenant.
            """);
 
        // Profil de l'apprenant
        sb.append("\n## Profil de l'apprenant\n");
        sb.append("- Nom : ").append(dashboard.getLearnerName()).append("\n");
        sb.append("- Progression globale : ").append(dashboard.getOverallProgressPercentage()).append("%\n");
        sb.append("- Cours inscrits : ").append(dashboard.getTotalEnrolledCourses()).append("\n");
        sb.append("- Cours terminés : ").append(dashboard.getTotalCompletedCourses()).append("\n");
 
        // Niveau par catégorie
        sb.append("\n## Niveau par compétence\n");
        for (var skill : dashboard.getSkills()) {
            sb.append("- ").append(skill.getCategoryLabel())
              .append(" : ").append(skill.getLevel())
              .append(" (").append(skill.getProgressPercentage()).append("%)\n");
        }
 
        // Cours recommandés par l'algorithme
        sb.append("\n## Cours recommandés par notre algorithme (classés par pertinence)\n");
        int rank = 1;
        for (var course : recommendations.getItems()) {
            sb.append(rank++).append(". **").append(course.getTitle()).append("**");
            if (course.getDescription() != null)
                sb.append(" – ").append(truncate(course.getDescription(), 100));
            sb.append(" [score: ").append(course.getScore()).append("/100]");
            sb.append(" [raisons: ").append(String.join(", ", course.getReasons())).append("]\n");
        }
 
        sb.append("""
 
            ## Instructions
            - Utilise uniquement les cours listés ci-dessus pour tes recommandations.
            - Ne crée pas de cours fictifs.
            - Si l'apprenant pose une question hors sujet, recentre poliment vers son apprentissage.
            - Maximum 3 cours recommandés par réponse.
            """);
 
        return sb.toString();
    }
 
    // ─── Mapping réponse LLM → CourseCards ───────────────────────────────────
 
    private List<ChatResponse.CourseCard> mapToCourseCards(
            List<RecommendedCourseDto> algoResults,
            String llmReply
    ) {
        var cards = new ArrayList<ChatResponse.CourseCard>();
 
        // Cherche quels cours de l'algo sont mentionnés dans la réponse du LLM
        for (var course : algoResults) {
            if (llmReply.contains(course.getTitle())) {
                // Extrait la phrase qui suit le titre comme "reason"
                String reason = extractSentenceAfter(llmReply, course.getTitle());
                cards.add(ChatResponse.CourseCard.builder()
                        .courseId(course.getCourseId())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .score(course.getScore())
                        .reason(reason)
                        .build());
            }
        }
 
        // Si le LLM n'a cité aucun cours reconnu → on inclut le top 3 de l'algo
        if (cards.isEmpty()) {
            algoResults.stream().limit(3).forEach(c ->
                cards.add(ChatResponse.CourseCard.builder()
                        .courseId(c.getCourseId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .score(c.getScore())
                        .reason("Recommandé selon votre profil")
                        .build())
            );
        }
 
        return cards;
    }
 
    // ─── Fallback sans API ────────────────────────────────────────────────────
 
    private ChatResponse buildFallbackResponse(RecommendationsResponse reco) {
        var cards = reco.getItems().stream().limit(3).map(c ->
            ChatResponse.CourseCard.builder()
                .courseId(c.getCourseId())
                .title(c.getTitle())
                .description(c.getDescription())
                .score(c.getScore())
                .reason("Sélectionné selon votre progression et vos compétences")
                .build()
        ).toList();
 
        return ChatResponse.builder()
                .reply("Voici les cours que je vous recommande en priorité selon votre profil :")
                .recommendations(cards)
                .build();
    }
 
    // ─── Utilitaires ──────────────────────────────────────────────────────────
 
    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
 
    private String extractSentenceAfter(String text, String keyword) {
        int idx = text.indexOf(keyword);
        if (idx == -1) return "";
        int start = idx + keyword.length();
        int end   = text.indexOf(".", start);
        if (end == -1 || end - start > 200) end = Math.min(start + 150, text.length());
        return text.substring(start, end).replaceAll("^[\\s:–-]+", "").trim();
    }

    private ChatResponse buildErrorResponse(String message) {
        return ChatResponse.builder()
                .reply(message)
                .recommendations(new ArrayList<>())
                .build();
    }
}
