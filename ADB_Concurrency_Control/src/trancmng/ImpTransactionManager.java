package trancmng;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import entity.Request;
import entity.RequestType;
import site.Site;
import trancmng.entity.tranStatus;
import trancmng.entity.transactionEntity;

public class ImpTransactionManager implements TransactionManager {

    /**
     * Contains all Site object in order
     */
    private Collection<Site> sites;
    /**
     * Map from site name to its reference
     */
    private Map<String, Site> siteMap;

    /**
     * Contains all the resources that available at least on one site.
     */
    private Set<String> resources;

    /**
     * A map from the resource to the set of sites that holds it
     */
    private Map<String, List<Site>> sitesAvaliable;

    /**
     * The Queue of waiting requests
     */
    private Queue<Request> waitingList;

    /**
     * A map from Site to the set of transactions that have visiting it.
     */
    private Map<Site, Set<String>> visitingTrans;

    /**
     * A map from the transaction to its the information it holds
     * 
     * @see transactionEntity
     */
    private Map<String, transactionEntity> transInfo;

    /**
     * Initialize all fields
     * 
     * @param sites
     *            List of all the reference of the sites
     * @param resources
     *            Set of all the available resources
     */
    public ImpTransactionManager(Map<String, Site> siteMap,
            Set<String> resources) {
        this.sites = siteMap.values();
        this.siteMap = siteMap;
        this.resources = resources;
        this.sitesAvaliable = new HashMap<String, List<Site>>();
        this.waitingList = new LinkedList<Request>();
        this.visitingTrans = new HashMap<Site, Set<String>>();
        for (Site site : sites)
            this.visitingTrans.put(site, new HashSet<String>());
        this.transInfo = new HashMap<String, transactionEntity>();
        this.init();
    }

    private void init() {
        // Create the map from resource to Sites that contains it
        // and remove the resources that no site contains it
        Set<String> removeResources = new HashSet<String>();
        for (String resource : resources) {
            List<Site> temp = new LinkedList<Site>();
            for (Site site : sites)
                if (site.containsResource(resource))
                    temp.add(site);
            if (temp.size() > 0)
                sitesAvaliable.put(resource, temp);
            else
                removeResources.add(resource);
        }
        this.resources.removeAll(removeResources);
    }

    /**
     * 
     * return if "transaction" needs to aborted when it conflicts with the
     * transactions in Set "conflicts" <br>
     * return null if it just wait return one transaction that it conflicts with
     * and is older than it.
     * 
     * @param transaction
     * @param conflicts
     * @return
     */
    private String needAbort(String transaction, Set<String> conflicts) {
        transactionEntity thisone = transInfo.get(transaction);
        for (String conflict : conflicts)
            if (thisone.timestamp > transInfo.get(conflict).timestamp)
                return conflict;
        return null;
    }

    @Override
    public void handleRequests(Queue<Request> requests) {
        
        this.handleWaitingList();

        Request tempR;
        while ((tempR = requests.poll()) != null) {
            this.handleRequest(tempR);
        }

    }

    private void handleWaitingList() {

        boolean haveEnd = false;
        Queue tempQ = this.waitingList;
        this.waitingList = new LinkedList<Request>();
        Iterator<Request> it = tempQ.iterator();
        while(it.hasNext()){
            Request request = it.next();
            this.transInfo.get(request.transaction).status = tranStatus.Running;
            if(this.handleRequest(request)){
                it.remove();
                if(request.requestType == RequestType.END)
                    haveEnd = true;
            }
        }
        
        if(haveEnd)
            this.handleWaitingList();
    }

    private boolean handleRequest(Request request) {

        System.out.println(request + "<<<<<<<<<<<<");
        switch (request.requestType) {
        case BEGIN:
        case BEGINRO:
            return this.beginRequest(request);
        case READ:
            if (!checkRequestResourceExists(request))
                return false;
            if (!checkRequestTransactionRunning(request))
                return false;
            if (this.transInfo.get(request.transaction).isReadOnly())
                return this.readOnlyRequest(request);
            return this.readRequest(request);
        case WRITE:
            if (!checkRequestResourceExists(request))
                return false;
            if (!checkRequestTransactionRunning(request))
                return false;
            return this.writeRequest(request);
        case ABORT:
            if (!this.checkRequestTransactionLiving(request))
                return false;
            return this.abortRequest(request);
        case END:
            if (!this.checkRequestTransactionLiving(request))
                return false;
            return this.endRequest(request);
        case DUMP:
            return this.dumpRequest(request);
        case FAIL:
            return this.failRequest(request);
        case RECOVER:
            return this.recoverRequest(request);

        }
        return false;
    }

    private boolean readOnlyRequest(Request request) {
        for (Site site : this.transInfo.get(request.transaction).visitedSites) {

            if (!site.isRunning())
                continue;
            if (site.isRecovering(request.resource))
                continue;

            site.exeRequest(new Request(request.resource, request.transaction,
                    RequestType.ROREAD, null));
            return true;
        }

        System.out
                .println("["
                        + request.transaction
                        + "] is going into the wail list because there is no site have avaliable data currently");
        this.transInfo.get(request.transaction).status = tranStatus.Waiting;
        this.waitingList.add(request);
        return false;
    }

    private boolean checkRequestResourceExists(Request request) {
        // check if this resource is contained in some site
        if (!this.resources.contains(request.resource)) {
            System.out.println("error: no site hold the resources ["
                    + request.resource + "]");
            return false;
        }
        return true;
    }

    private boolean checkReqeustTransactionExists(Request request) {
        if (this.transInfo.containsKey(request.transaction))
            return true;

        System.out.println("error: transaction [" + request.transaction
                + "] have not begun");
        return false;
    }

    private boolean checkRequestTransactionRunning(Request request) {

        if (!this.checkReqeustTransactionExists(request))
            return false;

        transactionEntity tempT = this.transInfo.get(request.transaction);
        switch (tempT.status) {
        case Running:
            return true;
        case Aborted:
            System.out.println("error: transaction [" + request.transaction
                    + "] have been aborted");
            return false;
        case Commited:
            System.out.println("error: transaction [" + request.transaction
                    + "] have been commited");
            return false;
        case Waiting:
            System.out
                    .println("Warning: transaction ["
                            + request.transaction
                            + "] is in waiting queue, this request is putting into waiting queue");
            this.waitingList.add(request);
            return false;
        }
        return false;
    }

    private boolean checkRequestTransactionLiving(Request request) {

        if (!this.checkReqeustTransactionExists(request))
            return false;

        // check the transaction status
        transactionEntity tempT = this.transInfo.get(request.transaction);

        switch (tempT.status) {
        case Running:
            return true;
        case Aborted:
            System.out.println("error: transaction [" + request.transaction
                    + "] have been aborted");
            return false;
        case Commited:
            System.out.println("error: transaction [" + request.transaction
                    + "] have been commited");
            return false;
        case Waiting:
            return true;
        }
        return false;
    }

    /**
     * Try to handle a read request. return true if the request is accepted by
     * some site. false if the request is rejected.
     * 
     * @param request
     * @return
     */
    private boolean readRequest(Request request) {
        String resource = request.resource;

        // try all sites that holds the key
        // There should be at least one site that holds this key
        for (Site site : sitesAvaliable.get(resource)) {

            if (!site.isRunning())
                continue;
            if (site.isRecovering(resource))
                continue;

            // Check if there is conflict
            Set<String> conflicts = site.checkConflict(request);
            if (conflicts.size() > 0) {
                System.out
                        .println("warning: There is conflict with current lockers.");
                String tempS = needAbort(request.transaction, conflicts);
                transactionEntity tempT = this.transInfo
                        .get(request.transaction);
                if (tempS == null) {
                    System.out.println("    [" + request.transaction
                            + "] is going into the wail list");
                    tempT.status = tranStatus.Waiting;
                    this.waitingList.add(request);
                } else {
                    System.out.println("    [" + request.transaction
                            + "] is aborted because it is conflict with ["
                            + tempS + "]");
                    this.abortRequest(new Request(null, request.transaction,
                            RequestType.ABORT, null));
                }
                return false;
            }

            // coming here means no conflict
            System.out.println(site.exeRequest(request));
            this.visitingTrans.get(site).add(request.transaction);
            this.transInfo.get(request.transaction).visitedSites.add(site);
            return true;
        }

        // if reaching here it means there is no running sites that holds the
        // request

        System.out
                .println("["
                        + request.transaction
                        + "] is abourted because there is no site have avaliable data currently");
//        this.transInfo.get(request.transaction).status = tranStatus.Waiting;
//        this.waitingList.add(request);
        this.abortRequest(new Request(null, request.transaction,
                RequestType.ABORT, null));
        return false;
    }

    private boolean writeRequest(Request request) {

        String resource = request.resource;

        // try all sites that holds the key
        // There should be at least one site that holds this key
        // Only checks if there is conflict with any living site
        boolean haveConflict = false;
        boolean needAbort = false;
        String older = null;
        for (Site site : sitesAvaliable.get(resource)) {
            if (!site.isRunning())
                continue;

            if (site.isRecovering(resource))
                continue;

            // Check if there is conflict
            Set<String> conflicts = site.checkConflict(request);
            if (conflicts.size() > 0) {
                String tempS = needAbort(request.transaction, conflicts);
                if (tempS != null) {
                    needAbort = true;
                    older = tempS;
                }
                haveConflict = true;
            }
        }

        if (haveConflict) {
            System.out
                    .println("warning: There is conflict with current lockers.");
            if (needAbort) {
                System.out.println("    [" + request.transaction
                        + "] is aborted because it is conflict with [" + older
                        + "]");
                this.abortRequest(new Request(null, request.transaction,
                        RequestType.ABORT, null));
            } else {
                System.out.println("    [" + request.transaction
                        + "] is going into the wail list");
                this.transInfo.get(request.transaction).status = tranStatus.Waiting;
                this.waitingList.add(request);
            }
            return false;
        }

        boolean havesite = false;
        for (Site site : sitesAvaliable.get(resource)) {
            if (!site.isRunning())
                continue;
            site.exeRequest(request);
            this.visitingTrans.get(site).add(request.transaction);
            this.transInfo.get(request.transaction).visitedSites.add(site);
            havesite = true;
        }

        if (!havesite) {
            System.out
                    .println("["
                            + request.transaction
                            + "] is going into the wail list because there is no site have avaliable data currently");
            this.transInfo.get(request.transaction).status = tranStatus.Waiting;
            this.waitingList.add(request);
        }

        return havesite;
    }

    private boolean failRequest(Request request) {
        Site tempSite = this.siteMap.get(request.site);
        if (tempSite == null) {
            System.out.println("error: site [" + request.site
                    + "] does not exists");
            return false;
        }

        tempSite.fail();
        for (String transaction : this.visitingTrans.get(tempSite)) {
            this.abortRequest(new Request(null, transaction, RequestType.ABORT,
                    null));
        }
        return true;
    }

    private boolean dumpRequest(Request request) {

        if (request.resource != null)
            if (!this.resources.contains(request.resource)) {
                System.out.println("error: Dump request resource ["
                        + request.resource + "] does not exists");
                return false;
            }

        if (request.site != null)
            if (!this.siteMap.containsKey(request.site)) {
                System.out.println("error: Dump request site [" + request.site
                        + "] does not exists");
                return false;
            }

        if (request.resource == null && request.site == null) {
            for (Site site : sites)
                System.out.println(site.exeRequest(request));
            return true;
        }
        if (request.resource != null && request.site == null) {
            for (Site site : sites)
                System.out.println(site.exeRequest(request));
            return true;
        }
        if (request.resource == null && request.site != null) {
            System.out.println(this.siteMap.get(request.site).exeRequest(request));
            return true;
        }
        if (request.resource != null && request.site != null) {
            System.out.println(this.siteMap.get(request.site).exeRequest(request));
            return true;
        }
        System.out.println("error: Dump request invalid");
        return false;
    }

    private boolean recoverRequest(Request request) {
        if (request.site == null) {
            System.out.println("error: recovery request have no site");
            return false;
        }

        if (request.site != null)
            if (!this.siteMap.containsKey(request.site)) {
                System.out.println("error: recovery request site ["
                        + request.site + "] does not exists");
                return false;
            }

        this.siteMap.get(request.site).exeRequest(request);
        return true;
    }

    private boolean beginRequest(Request request) {

        transactionEntity tempT = new transactionEntity(request.transaction,
                request.requestType == RequestType.BEGINRO);
        this.transInfo.put(tempT.name, tempT);

        if (request.requestType == RequestType.BEGINRO) {
            for (Site site : sites) {
                if (!site.isRunning())
                    continue;

                site.exeRequest(new Request(null, tempT.name,
                        RequestType.SNAPSHOT, null));
                tempT.visitedSites.add(site);
            }
        }
        return true;
    }

    private boolean abortRequest(Request request) {

        transactionEntity tempT = this.transInfo.get(request.transaction);

        if (tempT.status == tranStatus.Waiting) {
            Set<Request> removing = new HashSet<Request>();
            for (Request wait : this.waitingList) {
                if (wait.transaction.equals(request.transaction))
                    removing.add(wait);
            }
            this.waitingList.removeAll(removing);
        }

        for (Site site : tempT.visitedSites) {
            if (!site.isRunning())
                continue;
            site.exeRequest(request);
            this.visitingTrans.get(site).remove(tempT.name);
        }

        tempT.status = tranStatus.Aborted;
        return true;
    }

    private boolean endRequest(Request request) {

        if (!this.checkRequestTransactionLiving(request))
            return false;

        transactionEntity tempT = this.transInfo.get(request.transaction);

        if (tempT.status == tranStatus.Waiting) {
            this.waitingList.add(request);
            return false;
        }

        for (Site site : tempT.visitedSites) {
            if (!site.isRunning())
                continue;
            site.exeRequest(new Request(null, request.transaction,
                    RequestType.COMMIT, null));
            this.visitingTrans.get(site).remove(tempT.name);
        }

        tempT.status = tranStatus.Commited;
        return true;
    }

}
