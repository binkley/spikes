package x.validating;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.util.ReflectionUtils.findMethod;
import static x.validating.ReadJson.readJson;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidatingController {
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    public Validish get()
            throws IOException {
        return objectMapper
                .readValue(readJson("good-validish"), Validish.class);
    }

    @PostMapping
    public Validish post(@RequestBody final @Valid Validish request) {
        return request;
    }

    @PutMapping
    public Validish put(@RequestBody final String request,
            final BindingResult errors)
            throws Exception {
        final var validish = objectMapper.readValue(request, Validish.class);
        validator.validate(validish, errors);
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(new MethodParameter(
                    findMethod(getClass(), "put",
                            String.class, BindingResult.class), 0),
                    errors);
        }

        return validish;
    }
}
