
package at.antiochorthodox.liturgy;
import at.antiochorthodox.liturgy.model.Saint;
import at.antiochorthodox.liturgy.service.SaintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SaintServiceIntegrationTest {

    @Autowired
    private SaintService saintService;

    @Test
    public void testFindArabicSaints() {
        List<Saint> saints = saintService.getByLanguage("ar");
        System.out.println("Number of Arabic saints in DB: " + saints.size());
        saints.stream().limit(5).forEach(s -> System.out.println(s.getName()));
        // Optional: Simple assertion to make sure you have data
        // Assertions.assertFalse(saints.isEmpty(), "No saints found in DB!");
    }
}
