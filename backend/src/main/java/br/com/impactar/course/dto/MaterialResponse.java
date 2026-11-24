package br.com.impactar.course.dto;

import java.util.UUID;

import br.com.impactar.course.LessonMaterial;

public record MaterialResponse(
        UUID id,
        String title,
        String url,
        int orderIndex
) {

    public static MaterialResponse from(LessonMaterial material) {
        return new MaterialResponse(material.getId(), material.getTitle(), material.getUrl(), material.getOrderIndex());
    }
}
