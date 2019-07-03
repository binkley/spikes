package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u0007"}, d2 = {"Lhm/binkley/basilisk/HelloController;", "", "()V", "greeting", "Lhm/binkley/basilisk/HelloResponse;", "request", "Lhm/binkley/basilisk/HelloRequest;", "kotlin-micronaut"})
@io.micronaut.http.annotation.Controller(value = "/hello")
public final class HelloController {
    
    @org.jetbrains.annotations.NotNull()
    @io.micronaut.http.annotation.Post()
    public final hm.binkley.basilisk.HelloResponse greeting(@org.jetbrains.annotations.NotNull()
    hm.binkley.basilisk.HelloRequest request) {
        return null;
    }
    
    public HelloController() {
        super();
    }
}