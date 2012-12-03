package trancmng.entity;

import java.util.HashSet;
import java.util.Set;

import site.Site;

import entity.TimeStamp;

public class transactionEntity {
    
    public final String name;
    public final Integer timestamp;
    private final boolean readonly;
    public tranStatus status;
    public StringBuffer log;
    public Set<Site> visitedSites;
    
    public transactionEntity(String name,boolean readonly) {
        this.name = name;
        this.timestamp = TimeStamp.getit();
        status = tranStatus.Running;
        this.readonly = readonly;
        this.log = new StringBuffer();
        this.visitedSites = new HashSet<Site>();
    }
    
    public boolean isReadOnly(){
        return readonly;
    }
}
