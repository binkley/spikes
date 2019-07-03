package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'\u00a8\u0006\u0006"}, d2 = {"Lhm/binkley/basilisk/HelloClient;", "", "greet", "Lhm/binkley/basilisk/HelloResponse;", "request", "Lhm/binkley/basilisk/HelloRequest;", "kotlin-micronaut"})
@io.micronaut.http.client.annotation.Client(value = "/hello")
public abstract interface HelloClient {
    
    @org.jetbrains.annotations.NotNull()
    @io.micronaut.http.annotation.Post()
    public abstract hm.binkley.basilisk.HelloResponse greet(@org.jetbrains.annotations.NotNull()
    hm.binkley.basilisk.HelloRequest request);
}