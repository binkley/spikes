package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001d\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\b\u00a8\u0006\f"}, d2 = {"Lhm/binkley/basilisk/RecipeRepository;", "Lorg/jetbrains/exposed/dao/IntIdTable;", "()V", "chef", "Lorg/jetbrains/exposed/sql/Column;", "Lorg/jetbrains/exposed/dao/EntityID;", "", "getChef", "()Lorg/jetbrains/exposed/sql/Column;", "name", "", "getName", "kotlin-micronaut"})
public final class RecipeRepository extends org.jetbrains.exposed.dao.IntIdTable {
    @org.jetbrains.annotations.NotNull()
    private static final org.jetbrains.exposed.sql.Column<java.lang.String> name = null;
    @org.jetbrains.annotations.NotNull()
    private static final org.jetbrains.exposed.sql.Column<org.jetbrains.exposed.dao.EntityID<java.lang.Integer>> chef = null;
    public static final hm.binkley.basilisk.RecipeRepository INSTANCE = null;
    
    @org.jetbrains.annotations.NotNull()
    public final org.jetbrains.exposed.sql.Column<java.lang.String> getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final org.jetbrains.exposed.sql.Column<org.jetbrains.exposed.dao.EntityID<java.lang.Integer>> getChef() {
        return null;
    }
    
    private RecipeRepository() {
        super(null, null);
    }
}