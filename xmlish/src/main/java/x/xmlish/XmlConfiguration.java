package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

/**
 * @todo Apparently there are TWO Jackson mappers running around: the one
 * created here, used when explicitly asked for, and the one customized
 * internally by Spring Boot used by the controllers.  At least I didn't
 * figure out how to make this work without both.
 */
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

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer makeMeXml() {
        return builder -> builder
                .createXmlMapper(true)
                .defaultUseWrapper(false)
                .featuresToDisable(WRITE_DATES_AS_TIMESTAMPS);
    }
}
