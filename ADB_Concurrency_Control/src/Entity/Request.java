package Entity;

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
    }
            
}
