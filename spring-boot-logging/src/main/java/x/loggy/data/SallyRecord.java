package x.loggy.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import x.loggy.data.HowardRecord.HowardRef;

import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode
@Table("SALLY")
@ToString
public class SallyRecord {
    @Id
    public Long id;
    public String name;
    @Column("SALLY_ID")
    public Set<HowardRef> howards = new LinkedHashSet<>();
}
