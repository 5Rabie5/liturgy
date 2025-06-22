package at.antiochorthodox.liturgy;

import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaschaDateCalculatorTest {
private PaschaDateCalculator paschaDateCalculator;

    @BeforeEach
    void setUp() {
        paschaDateCalculator = new PaschaDateCalculator();
    }

    @Test
    void testPascha2025() {
        LocalDate expected = LocalDate.of(2025, 4, 20);
        LocalDate actual = paschaDateCalculator.getPaschaDate(2025);
       System.out.println(paschaDateCalculator.getPaschaDate(2026));
        assertEquals(expected, actual);
    }

    @Test
    void testPascha2024() {
        LocalDate expected = LocalDate.of(2024, 5, 5);
        LocalDate actual = paschaDateCalculator.getPaschaDate(2024);
        assertEquals(expected, actual);
    }

    @Test
    void testPascha2023() {
        LocalDate expected = LocalDate.of(2023, 4, 16);
        LocalDate actual = paschaDateCalculator.getPaschaDate(2023);
        assertEquals(expected, actual);
    }

    @Test
    void testPascha2000() {
        LocalDate expected = LocalDate.of(2000, 4, 30);
        LocalDate actual = paschaDateCalculator.getPaschaDate(2000);
        assertEquals(expected, actual);
    }

    @Test
    void testPascha1990() {
        LocalDate expected = LocalDate.of(1990, 4, 15);
        LocalDate actual = paschaDateCalculator.getPaschaDate(1990);
        assertEquals(expected, actual);
    }
}
