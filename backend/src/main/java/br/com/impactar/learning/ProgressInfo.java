package br.com.impactar.learning;

import java.util.Collection;

/** Progresso em aulas concluídas / total (curso ou trilha). Percentual arredondado para baixo. */
public record ProgressInfo(int completedLessons, int totalLessons, int percent) {

    public static final ProgressInfo EMPTY = new ProgressInfo(0, 0, 0);

    public static ProgressInfo of(long completed, long total) {
        if (total <= 0) {
            return EMPTY;
        }
        long done = Math.min(completed, total);
        return new ProgressInfo((int) done, (int) total, (int) Math.floor(done * 100.0 / total));
    }

    /** Soma de vários cursos (trilha): pondera pelo número de aulas. */
    public static ProgressInfo sum(Collection<ProgressInfo> parts) {
        long completed = 0;
        long total = 0;
        for (ProgressInfo part : parts) {
            completed += part.completedLessons();
            total += part.totalLessons();
        }
        return of(completed, total);
    }

    public boolean isComplete() {
        return totalLessons > 0 && completedLessons >= totalLessons;
    }
}
