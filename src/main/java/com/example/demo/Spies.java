package com.example.demo;

import lombok.experimental.UtilityClass;

import static java.lang.String.format;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

@UtilityClass
class Spies {
    @SuppressWarnings("unchecked")
    private static <T> T noisySpy(final T realInstance,
            final boolean noisy) {
        var mockSettings = withSettings()
                .spiedInstance(realInstance)
                .defaultAnswer(CALLS_REAL_METHODS);
        if (noisy) mockSettings = mockSettings.verboseLogging();

        return (T) mock(realInstance.getClass(), mockSettings);
    }

    @SuppressWarnings("unchecked")
    static <T> T spyOn(final Object target,
            final String fieldName, final boolean noisy) {
        final var field = findField(target.getClass(), fieldName);
        if (null == field) throw new IllegalStateException(
                format("No field '%s' found in %s or any superclass",
                        fieldName, target.getClass()));
        makeAccessible(field);

        final T fieldValue = (T) getField(field, target);
        final T spy = noisySpy(fieldValue, noisy);

        setField(field, target, spy);

        return spy;
    }
}
