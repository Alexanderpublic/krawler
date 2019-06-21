package src;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;


public class spiderLeg {
    private Map<Integer, String> hashMap = new HashMap<>(); //
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<>();

    /*
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     *
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */

    public boolean crawl(String url, BulkProcessor proc)
    {
        boolean r = false;
        try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            JSONObject obj=new JSONObject();

            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
            // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + url);
                String trash = "Skip to main content Biz & IT Tech Science Policy Cars Gaming & Culture Store Forums Subscribe Close " +
                        "Navigate Store Subscribe Videos Features Reviews RSS Feeds Mobile Site About Ars Staff Directory Contact Us Advertise " +
                        "with Ars Reprints Filter by topic Biz & IT Tech Science Policy Cars Gaming & Culture Store Forums Settings Front page " +
                        "layout Grid List Site theme Black on white White on black Sign in Comment activity Sign up or login to join the " +
                        "discussions! Stay logged in | Having trouble? Sign up to comment and more Sign up ";
                String trash1 = "You must login or create an account to comment. Channel Ars Technica ← Previous story " +
                        "Next story → Related Stories Sponsored Stories Powered by Today on Ars Store Subscribe About Us RSS Feeds " +
                        "View Mobile Site Contact Us Staff Advertise with us Reprints Newsletter Signup Join the Ars Orbital " +
                        "Transmission mailing list to get weekly updates delivered to your inbox. CNMN Collection WIRED Media Group © 2019 " +
                        "Condé Nast. All rights reserved. Use of and/or registration on any portion of this site constitutes acceptance of our " +
                        "User Agreement (updated 5/25/18) and Privacy Policy and Cookie Statement (updated 5/25/18) and " +
                        "Ars Technica Addendum (effective 8/21/2018). Ars may earn compensation on sales from links on this site. " +
                        "Read our affiliate link policy. Your California Privacy Rights The material on this site may not be reproduced, " +
                        "distributed, transmitted, cached or otherwise used, except with the prior written permission of Condé Nast. Ad Choices";
                String text = htmlDocument.select("body").text();
                text=text.trim();
                if (text.startsWith(trash)) text = text.replace(trash, "");
                if (text.endsWith(trash1)) text = text.replace(trash1, "");
                String fulltext = new StringBuilder().append(text).toString();
                //}
                Elements linksOnPage = htmlDocument.select("a[href]");
                for(Element link : linksOnPage)
                {
                    int hashLink = link.toString().hashCode();
                    hashMap.put(hashLink, link.toString());
                    if ((link.toString().contains("arstechnica.com/science/201"))&&!(link.toString().contains("/?"))&&!(link.toString().equals("http://arstechnica.com/science/")))
                    {
                        if (doubleS(hashLink)) { //doubles check
                            this.links.add(link.absUrl("href"));
                        }
                    }
                }

                obj.put("url", url);
                obj.put("header", htmlDocument.select("h1").text());
                obj.put("text", fulltext);

                proc.add(new IndexRequest("ars", "science").source(obj, XContentType.JSON));
                System.out.println("Found (" + linksOnPage.size() + ") links");
                r=true;
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
            }
        }
        catch(IOException e)
        {
            // We were not successful in our HTTP request
            r=false;
        }
        return r;
    }

    private boolean doubleS(int key){
        return hashMap.get(key) != null;
    }

    public List<String> getLinks()
    {
        return this.links;
    }


}
