package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u0000 \u00172\u00020\u0001:\u0001\u0017B\u0013\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0002\u0010\u0005R+\u0010\b\u001a\u00020\u00072\u0006\u0010\u0006\u001a\u00020\u00078F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\r\u0010\u000e\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR+\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0006\u001a\u00020\u000f8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0015\u0010\u0016\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006\u0018"}, d2 = {"Lhm/binkley/basilisk/RecipeRecord;", "Lorg/jetbrains/exposed/dao/IntEntity;", "id", "Lorg/jetbrains/exposed/dao/EntityID;", "", "(Lorg/jetbrains/exposed/dao/EntityID;)V", "<set-?>", "Lhm/binkley/basilisk/ChefRecord;", "chef", "getChef", "()Lhm/binkley/basilisk/ChefRecord;", "setChef", "(Lhm/binkley/basilisk/ChefRecord;)V", "chef$delegate", "Lorg/jetbrains/exposed/dao/Reference;", "", "name", "getName", "()Ljava/lang/String;", "setName", "(Ljava/lang/String;)V", "name$delegate", "Lorg/jetbrains/exposed/sql/Column;", "Companion", "kotlin-micronaut"})
public final class RecipeRecord extends org.jetbrains.exposed.dao.IntEntity {
    @org.jetbrains.annotations.NotNull()
    private final org.jetbrains.exposed.sql.Column name$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final org.jetbrains.exposed.dao.Reference chef$delegate = null;
    public static final hm.binkley.basilisk.RecipeRecord.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    public final void setName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final hm.binkley.basilisk.ChefRecord getChef() {
        return null;
    }
    
    public final void setChef(@org.jetbrains.annotations.NotNull()
    hm.binkley.basilisk.ChefRecord p0) {
    }
    
    public RecipeRecord(@org.jetbrains.annotations.NotNull()
    org.jetbrains.exposed.dao.EntityID<java.lang.Integer> id) {
        super(null);
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lhm/binkley/basilisk/RecipeRecord$Companion;", "Lorg/jetbrains/exposed/dao/IntEntityClass;", "Lhm/binkley/basilisk/RecipeRecord;", "()V", "kotlin-micronaut"})
    public static final class Companion extends org.jetbrains.exposed.dao.IntEntityClass<hm.binkley.basilisk.RecipeRecord> {
        
        private Companion() {
            super(null, null);
        }
    }
}