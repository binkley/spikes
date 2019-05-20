package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(LoggyController.class)
class LoggyControllerTest {
    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private Logger logger;
    @MockBean
    private LoggyRemote loggy;
    @MockBean
    private NotFoundRemote notFound;
    @MockBean
    private UnknownHostRemote unknownHost;
    @MockBean
    private ConflictRemote conflict;

    @Test
    void shouldAccept()
            throws Exception {
        mvc.perform(post("/postish")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(
                        LoggyRequest.builder()
                                .blinkenLights(2)
                                .when(LocalDate.of(9876, 5, 4))
                                .build())))
                .andExpect(status().isAccepted());
    }

    @TestConfiguration
    public static class MyTestConfiguration {
        @Bean
        public Clock testClock() {
            return Clock.fixed(Instant.ofEpochSecond(1_000_000L), UTC);
        }
    }
}
