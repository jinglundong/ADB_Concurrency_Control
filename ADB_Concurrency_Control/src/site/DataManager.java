package site;

import java.util.HashMap;
import java.util.Set;

public class DataManager {
    
    //data in DB, <resource, value> i.e. <x3, 30>
    private HashMap<String, String> data;
    
    //include resource which shows only in this site
    private final Set<String> unique; 
    
    //TransactionLog contains dirty data which has not been committed,
    //The value of outer map is a transactionLog. 
    //The inner map is <resource, value> pair
    private HashMap<String, HashMap<String, String>> transactionLog;
    
    //Snapshot for read only transactions, clean data.
    private HashMap<String, HashMap<String, String>> snapshot;
        
    /**
     * constructor of DataManager
     * @param data a HashMap of <variable, value> pairs
     * @param unique a set of resource name of unique variables
     */
    public DataManager(HashMap<String, String> data, Set<String> unique){
        this.data = data;     
        this.unique = unique;
        transactionLog = new HashMap<String, HashMap<String, String>>();
        snapshot = new HashMap<String, HashMap<String, String>>();        
    }

    
    /**
     * get a set of resource which is stored in at least one other site
     * @return a set of replicated variables
     */
    public Set<String> getReplicatedResource(){
        Set<String> result = data.keySet();
        result.removeAll(unique);
        return result;
    }

    
    /**
     * getter of data
     * @return data
     */
    public HashMap<String, String> getData() {
        return data;
    }

    
    /**
     * getter of unique
     * @return unique
     */
    public Set<String> getUnique() {
        return unique;
    }
     
    
    /**
     * Log a write to transactionLog
     * @param transactionId Transaction ID as String
     * @param resource resource name as String
     * @param value new value of given write transaction
     */
    private void logTransaction(String transactionId, String resource, String value){        
        if (!transactionLog.containsKey(transactionId)){
            transactionLog.put(transactionId, new HashMap<String, String>());
        }
        HashMap<String, String> tmpLog = transactionLog.get(transactionId);
        tmpLog.put(resource, value);
        transactionLog.put(transactionId, tmpLog);
    }
    
    
    /**
     * Read value of given resource, return empty string if no such resource
     * @param transactionId transaction ID as String
     * @param resource resource name
     * @param isReadOnly true if it is a read only transaction
     * @return
     */
    public String read(String transactionId, String resource, boolean isReadOnly){
        if (isReadOnly){
            if (!snapshot.containsKey(transactionId)){
                throw new IllegalArgumentException(
                        "A read transaction which has no snapshot");
            }
            if(!snapshot.get(transactionId).containsKey(resource)){
                throw new IllegalArgumentException(
                        "snapshot doesn't contains resource: " 
                        + resource + "for transaction: " + transactionId);
            }
            return snapshot.get(transactionId).get(resource);
        }
        else{
            //check if resource is in a write log
            if (this.transactionLog.containsKey(transactionId)){
                if (transactionLog.get(transactionId).containsKey(resource)){
                    return transactionLog.get(transactionId).get(resource);
                }
            }
            //read from database directly
            return this.data.get(resource);
        }
    }
    
    public void write(String transactionId, String resource, String value){
        
    }
    
    public void createSnapshot(String transactionId){        
        if (snapshot.containsKey(transactionId)){
            throw new IllegalArgumentException("snapshot already exist");
        }
        snapshot.put(transactionId, new HashMap<String, String>(data));
    }

}
