package test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import remindme.Helpers.TimeRange;

public class TimeRangeTest {

    private final LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    private final LocalDateTime end   = LocalDateTime.of(2025, 1, 1, 12, 0);
    private final TimeRange range = new TimeRange(start, end);

    // ---- Constructor & factory ----

    @Test
    public void constructor_shouldThrowException_whenEndIsBeforeStart() {
        LocalDateTime invalidEnd = start.minusHours(1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeRange(start, invalidEnd)
        );

        assertEquals("End time must be after start time", ex.getMessage());
    }

    @Test
    public void factoryMethod_shouldCreateEquivalentInstance() {
        TimeRange other = TimeRange.of(start, end);
        assertEquals(range, other);
    }

    // ---- contains (inclusive) ----

    @Test
    public void contains_shouldReturnTrue_forStartAndEnd() {
        assertTrue(range.contains(start));
        assertTrue(range.contains(end));
    }

    @Test
    public void contains_shouldReturnTrue_forDateInsideRange() {
        LocalDateTime inside = start.plusMinutes(30);
        assertTrue(range.contains(inside));
    }

    @Test
    public void contains_shouldReturnFalse_forDateOutsideRange() {
        assertFalse(range.contains(start.minusSeconds(1)));
        assertFalse(range.contains(end.plusSeconds(1)));
    }

    // ---- containsExclusive ----

    @Test
    public void containsExclusive_shouldExcludeStartAndEnd() {
        assertFalse(range.containsExclusive(start));
        assertFalse(range.containsExclusive(end));
    }

    @Test
    public void containsExclusive_shouldReturnTrue_forDateStrictlyInside() {
        LocalDateTime inside = start.plusMinutes(1);
        assertTrue(range.containsExclusive(inside));
    }

    // ---- overlaps ----

    @Test
    public void overlaps_shouldReturnTrue_whenRangesOverlap() {
        TimeRange other = new TimeRange(
                start.plusHours(1),
                end.plusHours(1)
        );

        assertTrue(range.overlaps(other));
    }

    @Test
    public void overlaps_shouldReturnFalse_whenRangesDoNotOverlap() {
        TimeRange other = new TimeRange(
                end,
                end.plusHours(1)
        );

        assertFalse(range.overlaps(other));
    }

    @Test
    public void overlaps_shouldReturnFalse_whenRangesJustTouch() {
        TimeRange other = new TimeRange(
                end,
                end.plusMinutes(30)
        );

        assertFalse(range.overlaps(other));
    }

    // ---- duration ----

    @Test
    public void duration_shouldReturnCorrectDuration() {
        Duration expected = Duration.ofHours(2);
        assertEquals(expected, range.duration());
    }

    // ---- equals & hashCode ----

    @Test
    public void equals_shouldReturnTrue_forSameValues() {
        TimeRange other = new TimeRange(start, end);
        assertEquals(range, other);
        assertEquals(range.hashCode(), other.hashCode());
    }

    @Test
    public void equals_shouldReturnFalse_forDifferentValues() {
        TimeRange other = new TimeRange(start, end.plusMinutes(1));
        assertNotEquals(range, other);
    }

    // ---- toString ----

    @Test
    public void toString_shouldContainStartAndEnd() {
        String text = range.toString();
        assertTrue(text.contains(start.toString()));
        assertTrue(text.contains(end.toString()));
    }

    // ---- getters ----

    @Test
    public void getters_shouldReturnCorrectValues() {
        assertEquals(start, range.start());
        assertEquals(end, range.end());
    }
}
