package mainserver;

import static org.junit.Assert.*;

import org.junit.Test;

public class MainserverTest {

    @Test
    public void testMain() {
        //fail("Not yet implemented");
    }

    @Test
    public void testCreateData() {
        MainServer mainServer = new MainServer();
        assertEquals(mainServer.createData(2).size(), 12);
        assertTrue(mainServer.createData(2).containsKey("x1"));
        assertTrue(mainServer.createData(2).containsKey("x11"));
        assertEquals(mainServer.createData(3).size(), 10);
        assertTrue(!mainServer.createData(3).containsKey("x1"));
        assertTrue(!mainServer.createData(3).containsKey("x11"));
    }
    
    @Test
    public void testCreateUnique() {
        MainServer mainServer = new MainServer();
        assertEquals(mainServer.createUnique(2).size(), 2);
        assertTrue(mainServer.createUnique(2).contains("x1"));
        assertTrue(mainServer.createUnique(2).contains("x11"));
        assertEquals(mainServer.createUnique(3).size(), 0);        
    }
    

}
