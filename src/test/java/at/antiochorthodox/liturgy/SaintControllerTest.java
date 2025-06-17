package at.antiochorthodox.liturgy;

import at.antiochorthodox.liturgy.model.Saint;
import at.antiochorthodox.liturgy.repository.SaintRepository;
import at.antiochorthodox.liturgy.service.SaintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaintService saintService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetSaintsByLanguage() throws Exception {
        List<Saint> mockSaints = List.of(
                Saint.builder()
                        .id("1")
                        .name("مار جرجس")
                        .title("شهيد")
                        .lang("ar")
                        .build()
        );

        Mockito.when(saintService.getByLanguage("ar")).thenReturn(mockSaints);

        mockMvc.perform(get("/api/saints/lang/ar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("مار جرجس"))
                .andExpect(jsonPath("$[0].lang").value("ar"));
    }
}
