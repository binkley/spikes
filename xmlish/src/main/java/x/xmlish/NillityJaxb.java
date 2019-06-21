package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER;

@EqualsAndHashCode
@ToString
@XmlAccessorType(PUBLIC_MEMBER)
@XmlRootElement(name = "nillity")
public class NillityJaxb {
    @XmlElement(name = "outer")
    public List<OuterJaxb> outer;
}
