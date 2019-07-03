package hm.binkley.basilisk;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB\u0013\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0002\u0010\u0005R+\u0010\b\u001a\u00020\u00072\u0006\u0010\u0006\u001a\u00020\u00078F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\r\u0010\u000e\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\f\u00a8\u0006\u0010"}, d2 = {"Lhm/binkley/basilisk/SourceRecord;", "Lorg/jetbrains/exposed/dao/IntEntity;", "id", "Lorg/jetbrains/exposed/dao/EntityID;", "", "(Lorg/jetbrains/exposed/dao/EntityID;)V", "<set-?>", "", "name", "getName", "()Ljava/lang/String;", "setName", "(Ljava/lang/String;)V", "name$delegate", "Lorg/jetbrains/exposed/sql/Column;", "Companion", "kotlin-micronaut"})
public final class SourceRecord extends org.jetbrains.exposed.dao.IntEntity {
    @org.jetbrains.annotations.NotNull()
    private final org.jetbrains.exposed.sql.Column name$delegate = null;
    public static final hm.binkley.basilisk.SourceRecord.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    public final void setName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public SourceRecord(@org.jetbrains.annotations.NotNull()
    org.jetbrains.exposed.dao.EntityID<java.lang.Integer> id) {
        super(null);
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lhm/binkley/basilisk/SourceRecord$Companion;", "Lorg/jetbrains/exposed/dao/IntEntityClass;", "Lhm/binkley/basilisk/SourceRecord;", "()V", "kotlin-micronaut"})
    public static final class Companion extends org.jetbrains.exposed.dao.IntEntityClass<hm.binkley.basilisk.SourceRecord> {
        
        private Companion() {
            super(null, null);
        }
    }
}