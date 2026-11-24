package br.com.impactar.course.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.course.Lesson;
import br.com.impactar.course.YouTube;

/** videoId pronto para o player: https://www.youtube-nocookie.com/embed/{videoId} */
public record LessonResponse(
        UUID id,
        UUID moduleId,
        String title,
        String description,
        String videoUrl,
        String videoId,
        int orderIndex,
        List<MaterialResponse> materials
) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getVideoUrl(),
                YouTube.extractVideoId(lesson.getVideoUrl()).orElse(null),
                lesson.getOrderIndex(),
                lesson.getMaterials().stream().map(MaterialResponse::from).toList());
    }
}
