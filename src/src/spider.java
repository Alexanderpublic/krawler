package src;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;

import java.util.*;

public class spider {
    // Fields
    Client client;
    BulkProcessor bulkProcessor = null;
    private static final int MAX_PAGES_TO_SEARCH = 1200;
    private Set<String> pagesVisited = Collections.synchronizedSet(new HashSet<>());
    private List<String> pagesToVisit = Collections.synchronizedList(new LinkedList<>());
    private byte[] local = new byte[]{127, 0, 0, 1};
    Scanner in = new Scanner(System.in);
    private String url;
    private String goal;
    private String exclude;
    private String startWith;
    private String noneq;
    private String category;

    public spider(String u, String g, String e, String s, String n, String c){
        url = u;
        goal = g;
        exclude = e;
        startWith = s;
        noneq = n;
        category = c;
        pagesToVisit.add(u);
    }

    private String nextUrl() {
        //System.out.println("nurl");
        String nextUrl;
        do {
            nextUrl = this.pagesToVisit.remove(0);
            if (this.pagesToVisit.size()==0){
                System.out.println("next url " + this.pagesToVisit.size());}
        } while (this.pagesVisited.contains(nextUrl));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }

    private String currUrl() {
        //System.out.println("curl");
        String currentUrl = null;
        if (this.pagesToVisit.isEmpty()) {
            //System.out.println("no new urls");
        } else {
            currentUrl = this.nextUrl();
        }
        return currentUrl;
    }

    synchronized void task(spiderLeg leg, Scanner in) {
        leg.crawl(currUrl(), bulkProcessor, goal, exclude, startWith, noneq, this.in, category); // Lots of stuff happening here. Look at the crawl method in
        // spiderLeg
        System.out.println(String.format("**Done** Visited %s web page(s)", this.pagesVisited.size()));
        List<String> r = leg.getLinks();
        this.pagesToVisit.addAll(r);
        System.out.println(this.pagesToVisit.size()+" links left");
    }

    public void search() throws InterruptedException {
        spiderLeg leg = new spiderLeg();
        /*{
            try {
                Settings settings = Settings.builder()
                        .put("cluster.name","elasticsearch")
                        .put("client.transport.ping_timeout","10")
                        .put("client.transport.sniff", true)
                        .build();
                client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByAddress(local), 9300));
                //System.out.println("ElasticSearch connected");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("Not connected");
            }
        }
        final long[] t = {0};
        bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                        t[0] = currentTimeMillis();
                        System.out.println("Number of actions: " + request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        System.out.println("Time taken for bulk: " + (currentTimeMillis()-t[0]));
                        System.out.println("Failures: " + response.hasFailures());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) { failure.printStackTrace(); }
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(3))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();*/
        while (this.pagesVisited.size() <= MAX_PAGES_TO_SEARCH) {
            task(leg,in);
            Thread.sleep(1000);
        }
        in.close();
        //client.close();
    }

}
