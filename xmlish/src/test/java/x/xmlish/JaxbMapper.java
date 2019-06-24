package x.xmlish;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public class JaxbMapper<T> {
    private final Class<T> type;
    private final Unmarshaller unmarshaller;

    public JaxbMapper(final Class<T> type)
            throws JAXBException {
        this.type = type;
        unmarshaller = JAXBContext.newInstance(type).createUnmarshaller();
    }

    public T readValue(final String xml)
            throws JAXBException {
        return type.cast(unmarshaller.unmarshal(new StringReader(xml)));
    }
}
