package org.acme.quickstart;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/hello")
public class GreetingResource {
    @Inject
    GreetingService service;

    @GET
    @Produces(TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/greeting/{name}")
    @Produces(TEXT_PLAIN)
    public String greeting(@PathParam("name") final String name) {
        return service.greeting(name);
    }
}
