package site;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DataManagerTest {

    @Test
    public void testDataManager() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("x2", "20");
        data.put("x4", "40");
        data.put("x3", "30");
        Set<String> unique = new HashSet<String>();
        unique.add("x3");
        DataManager dm = new DataManager(data, unique);
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
        DataManager dm = new DataManager(data, unique);
        assertEquals(dm.getReplicatedResource().size(), 2);
    }

}
