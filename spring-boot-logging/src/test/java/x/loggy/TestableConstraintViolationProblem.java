package x.loggy;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

/**
 * Makes a testable version of {@code ConstraintViolationProblem}.  Only the
 * whole, Zalando Problem is a beautiful library.  However, the types lacks
 * equals/hashCode, so are difficult to reuse in tests.  This data struct is
 * Jackson- and AssertJ-friendly.
 */
@Builder
@Data
public class TestableConstraintViolationProblem {
    public final String type
            = "https://zalando.github.io/problem/constraint-violation";
    public final int status = UNPROCESSABLE_ENTITY.value();
    public final String title = "Constraint Violation";
    @Singular
    public List<TestableViolation> violations;

    @Builder
    @Data
    public static class TestableViolation {
        public String field;
        public String message;
    }
}
