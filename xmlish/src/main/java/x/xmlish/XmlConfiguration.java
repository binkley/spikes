package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class XmlConfiguration {
    @Bean
    public ObjectMapper xmlObjectMapper() {
        final var xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);

        return new XmlMapper(xmlModule)
                .findAndRegisterModules()
                .configure(WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
