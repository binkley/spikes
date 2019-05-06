package x.etaggy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@Import(RestConfiguration.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(EtaggyController.class)
class EtaggyControllerTest {
    private final MockMvc mvc;

    @Test
    void shouldSeeEtag()
            throws Exception {
        mvc.perform(get("/"))
                .andExpect(header().string(ETAG, notNullValue()));
    }
}
