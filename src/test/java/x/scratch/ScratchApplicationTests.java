package x.scratch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
class ScratchApplicationTests {
    @Test
    void contextLoads() {
    }
}
