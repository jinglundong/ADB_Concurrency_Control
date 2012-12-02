package site;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import site.entity.LockType;

import Entity.Request;
import Entity.RequestType;

public class ImpSiteTest {

    ImpSite site;
    Request readReqT1;
    Request writeReqT1;
    Request writeReqT2;
    
    @Before
    public void initialize(){
        HashMap<String, String> dataCommon = new HashMap<String, String>();
        for (int i=2; i<=20; i+=2){
            dataCommon.put("x"+i, String.valueOf(i*10));
        }
        dataCommon.put("x3", "30");
        dataCommon.put("x13", "130");
        Set<String> unique = new HashSet<String>();
        unique.add("x3");
        unique.add("x13");
        site = new ImpSite(4, dataCommon, unique);
        String resource = "x4";
        String transaction = "T1";
        RequestType requestType = RequestType.READ;
        String value = "20";        
        readReqT1 = new Request(resource, transaction, requestType, value);
        requestType = RequestType.WRITE;
        writeReqT1 = new Request(resource, transaction, requestType, value); 
        transaction = "T2";
        writeReqT2 = new Request(resource, transaction, requestType, value);
    }
    
    @Test
    public void testImpSite() {
        assertEquals(site.getSiteNum(), 4);
        assertEquals(site.isRunning(), true);
        Set<String> repRes = new HashSet<String>();
        for (int i=2; i<= 20; i+=2){
            repRes.add("x" + i);
        }
        Set<String> unique = new HashSet<String>();
        unique.add("x3");
        unique.add("x13");
        assertEquals(site.getDataManager().getUnique(), unique);
        for (String str: repRes){
            assertFalse(site.getLockManager().isRecoverying(str));
        }
    }

    @Test
    public void testCheckConflict() {
        String resource = "x4";
        String transaction = "T1";
        RequestType requestType = RequestType.READ;
        String value = "";        
        Request request = new Request(resource, transaction, requestType, value);          
        assertTrue(site.checkConflict(request).isEmpty());
        
        site.getDataManager().read(transaction, resource, false);
        site.getLockManager().setLock(resource, transaction, LockType.READ);
        assertTrue(site.checkConflict(request).isEmpty());
        
        transaction = "T2";
        site.getLockManager().setLock(resource, transaction, LockType.READ);
        request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).isEmpty());
        
        requestType = RequestType.WRITE;
        request = new Request(resource, transaction, requestType, "20");        
        assertEquals(site.checkConflict(request).size(), 2);
    }

    @Test (expected = RuntimeException.class)
    public void testExeRequestException(){
        site.fail();
        String resource = "x4";
        String transaction = "T1";
        RequestType requestType = RequestType.READ;
        String value = "";        
        Request request = new Request(resource, transaction, requestType, value);  
        site.exeRequest(request);
    }
    
    @Test
    public void testExeRequest() {
        //read/write conflict
        site.exeRequest(readReqT1);
        assertEquals(site.checkConflict(writeReqT2).size(), 1);
        site.getLockManager().removeAllLocks();
        //write/read conflict
        site.exeRequest(writeReqT1);
        assertEquals(site.checkConflict(readReqT1).size(), 0);
        assertEquals(site.checkConflict(writeReqT2).size(), 1);
        
    }

    @Test
    public void testFail() {
        fail("Not yet implemented");
    }

    @Test
    public void testIsRunning() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSiteNum() {
        fail("Not yet implemented");
    }

}
