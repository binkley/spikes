package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class XmlConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        final var objectMapper = new XmlMapper();
        objectMapper.findAndRegisterModules()
                .configure(WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
