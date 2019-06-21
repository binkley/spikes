package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@XmlRootElement(name = "nillity")
public class NillityJaxb {
    @XmlElement(name = "outer")
    public List<OuterJaxb> outer;
}
