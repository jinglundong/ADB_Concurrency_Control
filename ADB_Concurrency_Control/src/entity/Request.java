package entity;

/**
 * 
 * @author jinglun
 *
 */
public class Request {
    public final String resource;
    
    public final String transaction;
    
    public final RequestType requestType;
    
    public final String value;
    
    public final String site;
        
    /**
     * An immutable request object
     * @param resource
     * @param transaction
     * @param requestType
     * @param value
     */
    public Request(String resource, String transaction, RequestType requestType, String value) {
        super();
        this.resource = resource;
        this.transaction = transaction;
        this.requestType = requestType;
        this.value = value;
        this.site = null;
    }
    
    
    /**
     * A constructor of fail/recover request
     * @param requestType
     * @param site
     */
    public Request(RequestType requestType, String site){
        super();
        this.resource = null;
        this.transaction = null;
        this.requestType = requestType;
        this.value = null;
        this.site = site;
    }
            
    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("transaction: ");
        buffer.append(this.transaction);
        buffer.append(", resource: ");
        buffer.append(this.resource);
        buffer.append(", requestType: ");
        buffer.append(String.valueOf(this.requestType));
        buffer.append(", value: ");
        buffer.append(this.value);
        buffer.append(", site: ");
        buffer.append(this.site);
        return buffer.toString();        
    }
}
