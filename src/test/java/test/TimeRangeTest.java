package test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import remindme.Helpers.TimeRange;

public class TimeRangeTest {

    private final LocalTime start = LocalTime.of(10, 0);
    private final LocalTime end   = LocalTime.of(12, 0);
    private final TimeRange range = new TimeRange(start, end);

    // ---- Constructor & factory ----

    @Test
    public void constructor_shouldThrowException_whenEndIsBeforeStart() {
        LocalTime invalidEnd = start.minusHours(1);

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
    public void contains_shouldReturnTrue_forTimeInsideRange() {
        LocalTime inside = start.plusMinutes(30);
        assertTrue(range.contains(inside));
    }

    @Test
    public void contains_shouldReturnFalse_forTimeOutsideRange() {
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
    public void containsExclusive_shouldReturnTrue_forTimeStrictlyInside() {
        LocalTime inside = start.plusMinutes(1);
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
        Duration expected = Duration.between(start, end);
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

    // ---- null inputs ----

    @Test
    public void constructor_shouldThrowException_whenStartIsNull() {
        LocalTime nullStart = null;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new TimeRange(nullStart, end)
        );

        assertEquals("Start time cannot be null", ex.getMessage());
    }

    @Test
    public void constructor_shouldThrowException_whenEndIsNull() {
        LocalTime nullEnd = null;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new TimeRange(start, nullEnd)
        );

        assertEquals("End time cannot be null", ex.getMessage());
    }

    @Test
    public void contains_shouldThrowException_whenInputIsNull() {
        LocalTime nullTime = null;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> range.contains(nullTime)
        );

        assertEquals("Cannot invoke \"java.time.LocalTime.isAfter(java.time.LocalTime)\" because \"localTime\" is null", ex.getMessage());
    }

    @Test
    public void containsExclusive_shouldThrowException_whenInputIsNull() {
        LocalTime nullTime = null;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> range.containsExclusive(nullTime)
        );

        assertEquals("Cannot invoke \"java.time.LocalTime.isAfter(java.time.LocalTime)\" because \"localTime\" is null", ex.getMessage());
    }

}
