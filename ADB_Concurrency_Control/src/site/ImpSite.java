package site;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import site.entity.Request;


public class ImpSite implements Site{
    
    private boolean isRunning;
    
    private LockManager lockManager;
    
    private DataManager dataManager;
    
    private int siteNum;
    
    public ImpSite(int siteNum, HashMap<String, String> data, Set<String> unique){      
        this.siteNum = siteNum;
        lockManager = new LockManager();
        dataManager = new DataManager(data, unique);
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
        return null;
    }

    @Override
    public String tryRequest(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fail() {
        // TODO Auto-generated method stub
        
    }
}
