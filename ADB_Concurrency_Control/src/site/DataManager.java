package site;

import site.entity.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import site.entity.CacheEntity;
import site.entity.TransactionLogEntity;

public class DataManager {
    
    //data in DB, <variable name, value> i.e. <x3, 30>
    private HashMap<String, String> data;
    
    //include variable which shows only in this site
    private final Set<String> unique; 
    
    //transactionLog contains dirty data which has not been committed
    private HashMap<String, ArrayList<TransactionLogEntity>> transactionLog;
    
    //cached data for read only transactions, clean data.
    private HashMap<String, ArrayList<CacheEntity>> cache;
    
    /**
     * constructor of DataManager
     * @param data a HashMap of <variable, value> pairs
     * @param unique a set of variable name of unique variables
     */
    public DataManager(HashMap<String, String> data, Set<String> unique){
        this.data = data;     
        this.unique = unique;
        transactionLog = new HashMap<String, ArrayList<TransactionLogEntity>>();
        cache = new HashMap<String, ArrayList<CacheEntity>>();        
    }

    /**
     * get a set of variables which is stored in at least one other site
     * @return a set of replicated variables
     */
    public Set<String> getReplicatedVariables(){
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
    
    
}
