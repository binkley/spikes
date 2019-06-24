package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@EqualsAndHashCode
@ToString
@XmlRootElement(name = "nillity")
public final class NillityJaxb {
    public List<OuterJaxb> outer;
}
