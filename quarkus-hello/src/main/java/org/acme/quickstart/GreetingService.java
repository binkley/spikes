package org.acme.quickstart;

import javax.enterprise.context.ApplicationScoped;

import static java.lang.String.format;

@ApplicationScoped
public class GreetingService {
    public String greeting(final String name) {
        return format("Hello, %s!", name);
    }
}
