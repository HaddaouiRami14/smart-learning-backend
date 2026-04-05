package com.example.SmartLearning.recommendation;

import java.util.List;
import java.util.Map;
 
/**
 * Contrat commun à toutes les stratégies.
 * score() retourne Map<courseId, score entre 0 et 100>
 */
public interface RecommendationStrategy {
    String             getName();
    double             getWeight();   // somme des poids = 1.0
    Map<Long, Integer> score(Long apprenantId, List<Long> candidateIds);
}