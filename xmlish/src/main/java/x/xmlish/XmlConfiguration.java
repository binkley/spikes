package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        final var objectMapper = new XmlMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
}
