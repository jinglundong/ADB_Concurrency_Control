package trancmng;

import java.util.List;
import java.util.Queue;

import site.Site;

import entity.Request;

public interface TransactionManager {
    /**
     * Handle the request parsed by main server
     * @param request
     */
    public void handleRequests(Queue<Request> request);
    
}