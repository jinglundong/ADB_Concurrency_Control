package site;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import site.entity.LockType;
import site.entity.Request;
import site.entity.RequestType;



/**
 * 
 * @author jinglun
 *
 */
public class ImpSite implements Site{
    
    private boolean isRunning;
    
    private ImpLockManager lockManager;
    
    private ImpDataManager dataManager;
    
    private int siteNum;
    
    public ImpSite(int siteNum, HashMap<String, String> data, Set<String> unique){      
        this.siteNum = siteNum;
        lockManager = new ImpLockManager();
        dataManager = new ImpDataManager(data, unique);
    }
    
    private void initiateData(Map<String, String> data, Set<String> unique){
//        if (unique == null || data == null){
//            throw new IllegalArgumentException("arguments can not be null");
//        }
//        if (siteNum % 2 != 0){
//            unique.add("x" + String.valueOf(siteNum-1));            
//            unique.add("x" + String.valueOf(siteNum - 1 + 10));
//            data.put("x" + String.valueOf(siteNum-1), String.valueOf(siteNum -1));
//            data.put("x" + String.valueOf(siteNum-1+10), String.valueOf(siteNum -1+10));
//        }
//        for (int i=2; i<=20; i++){
//            data.put("x" + i, String.valueOf(i*10));
//        }                
    }
    
    @Override
    public Set<String> checkConflict(Request request){  
        if (request.requestType != RequestType.READ 
                && request.requestType != RequestType.WRITE){
            throw new IllegalArgumentException("request type must be either read or write");
        }
        if (request.requestType == RequestType.READ){
            return lockManager.checkConflict(request.resource, 
                    request.transaction, LockType.READ);
        }
        else{   //Write
            return lockManager.checkConflict(request.resource, 
                    request.transaction, LockType.WRITE);
        }
    }

    @Override
    public String exeRequest(Request request) {
        RequestType requestType = request.requestType;
        switch (requestType){
        case READ:
            lockManager.setLock(request.resource, request.transaction, LockType.READ);
            dataManager.read(request.transaction, request.resource, false);
            break;
        case WRITE:
            lockManager.setLock(request.resource, request.transaction, LockType.WRITE);
            if (request.value == null || request.value.isEmpty()){
                throw new IllegalArgumentException("value to be written to database is null");
            }
            dataManager.write(request.transaction, request.resource, request.value);
        case ROREAD:
            lockManager.setLock(request.resource, request.transaction, LockType.READ);
            dataManager.read(request.transaction, request.resource, true);
            break;
        case DUMP:
            
        case COMMIT:
        case ABORT:
        default:
            throw new IllegalArgumentException("request type not supported");
        }        
        return "SUCCESS";        
    }

    @Override
    public void fail() {
        this.isRunning = false;    
        
    }
    
    public boolean isRunning(){
        return this.isRunning;
    }
}
