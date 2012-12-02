package site;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jinglun
 *
 */
public class DataManagerTest {

    HashMap<String, String> dataCommon;
    ImpDataManager dmCommon;
    
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
        dmCommon = new ImpDataManager(dataCommon, unique);
    }
    
    @Test
    public void testDataManager() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("x2", "20");
        data.put("x4", "40");
        data.put("x3", "30");
        Set<String> unique = new HashSet<String>();
        unique.add("x3");
        ImpDataManager dm = new ImpDataManager(data, unique);
        assertEquals(dm.getUnique().size(), 1);
        assertEquals(dm.getData().size(), 3);
    }

    @Test
    public void testGetReplicatedVariables() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("x2", "20");
        data.put("x4", "40");
        data.put("x3", "30");
        Set<String> unique = new HashSet<String>();
        unique.add("x3");
        ImpDataManager dm = new ImpDataManager(data, unique);
        assertEquals(dm.getReplicatedResource().size(), 2);
    }

    @Test
    public void testRead() {
        assertEquals(dmCommon.read("T1", "x3", false), "30");
        dmCommon.createSnapshot("T2");
        assertEquals(dmCommon.read("T2", "x3", true), "30");
        dmCommon.write("T1", "x3", "40");
        assertEquals(dmCommon.read("T2", "x3", true), "30");
        assertEquals(dmCommon.read("T1", "x3", false), "40");
    }
    
    @Test (expected = RuntimeException.class)
    public void testReadNotExist(){
        dmCommon.read("T1", "x1", false);
        dmCommon.createSnapshot("T2");
        dmCommon.read("T2", "x1", true);
    }

    @Test
    public void testWrite() {
        assertEquals(dmCommon.read("T50", "x3", false), "30");
        assertEquals(dmCommon.read("T1", "x3", false), "30");
        dmCommon.write("T1", "x3", "40");
        assertEquals(dmCommon.getWriteLog().size(), 1);
        assertEquals(dmCommon.read("T1", "x3", false), "40");        
    }

    @Test
    public void testCreateSnapshot() {
        assertEquals(dmCommon.getSnapshot().size(), 0);
        dmCommon.createSnapshot("T2");
        assertEquals(dmCommon.getSnapshot().size(), 1);
        dmCommon.write("T1", "x3", "40");        
        dmCommon.commit("T1");
        dmCommon.createSnapshot("T3");
        assertEquals(dmCommon.getSnapshot().size(), 2);
        assertEquals(dmCommon.read("T2", "x3", true), "30");
        assertEquals(dmCommon.read("T3", "x3", true), "40");
        assertEquals(dmCommon.getWriteLog().size(), 0);
    }

    @Test
    public void testCommit() {
        assertEquals(dmCommon.getWriteLog().size(), 0);
        assertEquals(dmCommon.getReadLog().size(), 0);
        dmCommon.write("T1", "x3", "40");
        assertEquals(dmCommon.getWriteLog().size(), 1);
        assertEquals(dmCommon.read("T1", "x3", false), "40");
        assertEquals(dmCommon.getReadLog().size(), 1);
        dmCommon.commit("T1");
        assertEquals(dmCommon.getWriteLog().size(), 0);
        assertEquals(dmCommon.getReadLog().size(), 0);
    }

    @Test
    public void testFail() {
        dmCommon.fail();
        assertEquals(dmCommon.getData().size(), 12);
        assertEquals(dmCommon.getReadLog().size(), 0);
        assertEquals(dmCommon.getSnapshot().size(), 0);
        assertEquals(dmCommon.getWriteLog().size(), 0);
    }
    
    @Test
    public void testTerminateTransaction(){
        assertEquals(dmCommon.getWriteLog().size(), 0);
        assertEquals(dmCommon.getReadLog().size(), 0);
        dmCommon.write("T1", "x3", "40");
        assertEquals(dmCommon.getWriteLog().size(), 1);
        assertEquals(dmCommon.read("T1", "x3", false), "40");
        assertEquals(dmCommon.getReadLog().size(), 1);
        dmCommon.terminateTransaction("T1");
        assertEquals(dmCommon.getWriteLog().size(), 0);
        assertEquals(dmCommon.getReadLog().size(), 0);      
        assertEquals(dmCommon.read("T3", "x3", false), "30");
    }

    @Test
    public void testDump(){
        assertEquals(dmCommon.dumpResource("x3"), "x3: 30 ");
        assertEquals(dmCommon.dumpResource("x2"), "x2: 20 ");
        dmCommon.write("T1", "x3", "40");
        assertEquals(dmCommon.dumpResource("x3"), "x3: 30 ");
        dmCommon.commit("T1");
        assertEquals(dmCommon.dumpResource("x3"), "x3: 40 ");
        assertEquals(dmCommon.dumpResource("x1"), "x1: NULL ");
    }
    
    @Test
    public void testDumpSite(){
        assertEquals(dmCommon.dumpSite().split(" ").length, 24);
    }
}
