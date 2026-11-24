package br.com.impactar.learning.dto;

import java.util.List;

/** Home do aluno: retomada, matrículas e catálogo (somente itens publicados). */
public record HomeResponse(
        ContinueLearningResponse continueLearning,
        List<TrailCardResponse> myTrails,
        List<CourseCardResponse> myCourses,
        List<TrailCardResponse> availableTrails,
        List<CourseCardResponse> availableCourses
) {
}
