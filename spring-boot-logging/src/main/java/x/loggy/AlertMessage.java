package x.loggy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.reverse;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface AlertMessage {
    String value();

    class MessageFinder {
        public static String findAlertMessage(final Throwable throwable) {
            final var throwables = new ArrayList<Throwable>();
            for (Throwable x = throwable; null != x; x = x.getCause())
                throwables.add(x);
            reverse(throwables); // Search bottom up

            for (final var t : throwables) {
                final var message = findAlertMessage(t.getStackTrace());
                if (null != message)
                    return message;
            }

            return null;
        }

        private static String findAlertMessage(
                final StackTraceElement[] stackTrace) {
            for (final var frame : stackTrace) {
                final var message = findAlertMessage(frame);
                if (null != message)
                    return message;
            }

            return null;
        }

        private static String findAlertMessage(
                final StackTraceElement frame) {
            try {
                final var methods = getSystemClassLoader()
                        .loadClass(frame.getClassName())
                        .getDeclaredMethods();

                for (final var method : methods) {
                    final var message = findAlertMessage(
                            frame.getMethodName(), method);
                    if (null != message)
                        return message;
                }

                return null;
            } catch (final ClassNotFoundException e) {
                throw new Bug("Cannot load frame: " + frame, e);
            }
        }

        private static String findAlertMessage(final String methodName,
                final Method method) {
            if (!methodName.equalsIgnoreCase(method.getName()))
                return null;
            final var alertMessage = method.getAnnotation(AlertMessage.class);
            if (null != alertMessage)
                return alertMessage.value();

            return null;
        }
    }
}
