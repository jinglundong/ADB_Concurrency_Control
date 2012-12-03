package site;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import entity.Request;
import entity.RequestType;

import site.entity.LockType;


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
        //read only read
        String resource = "x4";
        String transaction = "T3";
        RequestType requestType = RequestType.ROREAD;
        String value = "";      
        site.getDataManager().createSnapshot("T3");
        Request request = new Request(resource, transaction, requestType, value);
        assertEquals(site.exeRequest(request), "40");
        //dump x4
        requestType = RequestType.DUMP;
        request = new Request(resource, transaction, requestType, value);
        assertEquals(site.exeRequest(request), "");        
    }

    @Test
    public void testFail() {
        assertTrue(site.isRunning());
        site.fail();
        assertTrue(!site.isRunning());        
    }

    @Test
    public void testGetSiteNum() {
        assertEquals(site.getSiteNum(), 4);
    }
        

    @Test
    public void testCase1(){
        String resource = "x1";
        String transaction = "T1";
        RequestType requestType = RequestType.WRITE;
        String value = "101";              
        Request request = new Request(resource, transaction, requestType, value);
        assertEquals(site.checkConflict(request).size(), 0);
        site.exeRequest(request);
        
        resource = "x2";
        transaction = "T2";
        value = "202";
        request = new Request(resource, transaction, requestType, value);
        assertEquals(site.checkConflict(request).size(), 0);
        site.exeRequest(request);
        
        resource = "x2";
        transaction = "T1";
        value = "102";
        request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).contains("T2"));
    }
    
    @Test
    public void testCase2(){
        String resource = "x2";
        String transaction = "T1";
        RequestType requestType = RequestType.WRITE;
        String value = "101";              
        Request request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).isEmpty());
        site.exeRequest(request);
        
        site.getDataManager().createSnapshot("T2");
        resource = "x4";
        transaction = "T2";
        requestType = RequestType.ROREAD;
        request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).isEmpty());
        site.exeRequest(request);
                
        transaction = "T1";
        requestType = RequestType.WRITE;
        value = "102";
        request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).isEmpty());
        site.exeRequest(request);
        
        resource = "x2";
        transaction = "T2";
        requestType = RequestType.ROREAD;
        request = new Request(resource, transaction, requestType, value);
        assertTrue(site.checkConflict(request).isEmpty());
        site.exeRequest(request);
                
        assertEquals(site.getDataManager().getWriteLog().get("T1").get("x2"), "101");
        assertEquals(site.getDataManager().getWriteLog().get("T1").get("x4"), "102");               
    }
    

    
}
