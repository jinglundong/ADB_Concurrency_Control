package site;

import java.util.Set;

import entity.Request;



public interface Site {
    /**
     * 
     * Return the conflict transactionIDs if there is transaction <br>
     * Return empty set if there is no conflict <br>
     * Return a set with only one element "RECOVERY" if there is a recovery lock
     * on the resource.
     * 
     * @param request
     *            request from the transaction manager
     * @return the conflict transactionIDs *
     */
    public Set<String> checkConflict(Request request);

    /**
     * This should be called strictly after calling the CheckConflict <br>
     * Assuming that there is no Conflict.
     * 
     * @param request
     *            request from the transaction manger
     * @return "SUCCESS" if the request is successfully processed. <br>
     * return error messages if there is error.
     */
    public String exeRequest(Request request);
    
    
    /**
     * Site Fail. Release all read/write lock on data. Set site status to down. 
     */
    public void fail();
  
    
    /**
     * Check site status
     * @return return true if site is running
     */
    public boolean isRunning();
    
    
    /**
     * check if this site contains requested resource
     * @param resource
     * @return
     */
    public boolean containsResource(String resource);
    
    
    /**
     * Getter of site number
     * @return
     */
    public int getSiteNum();
    
    
    /**
     * Check if given resource is recovering 
     * @return return true if it is recovering
     */
    public boolean isRecovering(String resource);
        
    
    /**
     * create snapshot for read only transaction
     * @param transaction
     */
    public void createSnapshot(String transaction);
}