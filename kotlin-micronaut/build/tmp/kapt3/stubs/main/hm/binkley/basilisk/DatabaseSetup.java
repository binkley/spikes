package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lhm/binkley/basilisk/DatabaseSetup;", "", "dataSource", "Ljavax/sql/DataSource;", "(Ljavax/sql/DataSource;)V", "seeSchemaInStdOut", "", "kotlin-micronaut"})
@io.micronaut.context.annotation.Infrastructure()
@io.micronaut.context.annotation.Context()
public final class DatabaseSetup {
    private final boolean seeSchemaInStdOut = true;
    
    public DatabaseSetup(@org.jetbrains.annotations.NotNull()
    javax.sql.DataSource dataSource) {
        super();
    }
}