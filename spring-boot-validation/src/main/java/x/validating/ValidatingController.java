package x.validating;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
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
    public void post(@RequestBody final @Valid Validish request) {
        logger.info("GOT {}", request);
    }

    @PutMapping
    public void put(@RequestBody final String request,
            final BindingResult errors)
            throws IOException, NoSuchMethodException,
            MethodArgumentNotValidException {
        final var validish = objectMapper.readValue(request, Validish.class);
        validator.validate(validish, errors);
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(new MethodParameter(
                    getClass().getMethod(
                            "put", String.class, BindingResult.class), 0),
                    errors);
        }
        logger.info("GOT {}", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    void handleValidationFailure() {
    }
}