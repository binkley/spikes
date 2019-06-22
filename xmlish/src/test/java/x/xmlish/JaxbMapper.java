package x.xmlish;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

public class JaxbMapper {
    public <T> T readValue(final String xml, final Class<T> type)
            throws JAXBException {
        return type.cast(JAXBContext.newInstance(type)
                .createUnmarshaller()
                .unmarshal(new StringReader(xml)));
    }
}
