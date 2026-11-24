package br.com.impactar.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProgressInfoTest {

    @Test
    void calculatesPercentRoundingDown() {
        ProgressInfo progress = ProgressInfo.of(2, 3);
        assertEquals(66, progress.percent());
        assertFalse(progress.isComplete());
    }

    @Test
    void courseWithoutLessonsIsZeroAndNotComplete() {
        assertEquals(ProgressInfo.EMPTY, ProgressInfo.of(0, 0));
        assertFalse(ProgressInfo.of(0, 0).isComplete());
    }

    @Test
    void completedNeverExceedsTotal() {
        ProgressInfo progress = ProgressInfo.of(5, 4);
        assertEquals(4, progress.completedLessons());
        assertEquals(100, progress.percent());
        assertTrue(progress.isComplete());
    }

    @Test
    void trailProgressIsWeightedByLessons() {
        // curso A: 10/10, curso B: 0/30 -> 10/40 = 25% (e não a média simples de 50%)
        ProgressInfo trail = ProgressInfo.sum(List.of(ProgressInfo.of(10, 10), ProgressInfo.of(0, 30)));
        assertEquals(25, trail.percent());
        assertEquals(40, trail.totalLessons());
    }
}
