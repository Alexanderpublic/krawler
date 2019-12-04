package src;

import com.opencsv.CSVWriter;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import weka.core.Stopwords;

import java.io.*;
import java.util.*;

//https://try.jsoup.org/
public class spiderLeg {
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<>();
    File file = new File("dataset.csv");

    /*
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     *
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */

    static void docWriter(String s, String fname) {
        try (FileWriter writer = new FileWriter(fname, true)) {
            writer.write(s);
            writer.append('\n');
            writer.flush();
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
        System.out.println("Успешно записан в файл");
    }

    public boolean crawl(String url, BulkProcessor proc, String goal, String exclude, String include, String noneq, Scanner in, String category)
    {
        boolean r = false;
        try
        {
            System.out.println(url);
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            JSONObject obj=new JSONObject();
            String text;
            Elements comms = null;
            converter con = new converter();
            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
            // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + url);
                int hashLink = 0;
                Elements linksOnPage = htmlDocument.select("a[href]");
                //System.out.println(linksOnPage);
                for(Element link : linksOnPage)
                {
                    if ((link.absUrl("href").startsWith(include))
                            &&!(link.absUrl("href").contains(exclude))
                            &&(!(link.absUrl("href").contains("&"))||(link.absUrl("href").contains("&start")))
                            &&!(link.absUrl("href").contains("#"))
                            &&!(link.absUrl("href").contains("?theme="))
                            &&!(link.absUrl("href").equals(noneq))
                            &&!(link.absUrl("href").equals("https://arstechnica.com"))
                            &&(link.absUrl("href").startsWith(noneq)))
                    {
                        this.links.add(link.absUrl("href"));
                    }
                }
                DataPrep prep = new DataPrep();
                if (goal != "comments"){
                    text = htmlDocument.select("div[class=\"article-content post-page\"]").text();
                    text = text.replace(htmlDocument.select("aside[id=social-left]").text(),"");
                    text = text.replace("Enlarge","");
                    text = text.trim();
                    obj.put("url", url);
                    obj.put("header", htmlDocument.select("h1").text());
                    obj.put("text", text);
                    obj.put("hash", hashLink);
                    //docWriter(htmlDocument.select("h1").text(), text);
                    List<String> art = prep.prepareData(text);
                    if (art.size()>=50) {
                        con.create_CSV("mk2.csv",art, category,0);
                        con.create_CSV("corp.csv", Collections.singletonList(text), category,0);
                    }
                    //proc.add(new IndexRequest("ars", goal).source(obj, XContentType.JSON));

                } else {
                    comms = htmlDocument.select("li:has(header)[id^=comment]");
                    for (Element comm : comms) {
                        String author = comm.select("> header > div > span > a[href]").text();
                        String date = comm.select("> header > aside > a[title!=reply]").text();
                        String tmp = comm.select("> div").text().replace(comm.select("> div > div[class=quotetitle]").text(), "");
                        tmp = tmp.replace(comm.select("> div > div[class=quotecontent]").text(), "").trim();
                        obj.put("author", author);
                        obj.put("date", date);
                        obj.put("text", tmp);
                        //System.out.println(obj.toString());
                        proc.add(new IndexRequest("ars", goal).source(obj, XContentType.JSON));

                        String[] sp= new String[50];
                        for (int i =0;i<50;i++){
                            sp[i] = " ";
                        }
                        String [] s = tmp.replaceAll("\\p{Punct}", "").split(" ");
                        int w=0;
                        if (s.length>=sp.length){
                            w=sp.length;
                        }else {
                            w=s.length;
                        }
                        for (int i =0;i<w;i++){
                            sp[i] = s[i];
                        }
                        FileWriter wr = new FileWriter(file, true);
                        CSVWriter writer = new CSVWriter(wr);
                        List<String[]> instance = new ArrayList<String[]>();
                        sp = DictionaryLemmatizer(sp);
                        sp = removeStopwords_list(sp);
                        sp = strTrim(sp);
                        if (tmp.length() < 300) {
                            System.out.print(tmp + ": ");
                            int num = in.nextInt();
                            String q = "";
                            if (num == 0) {
                                q = "NEGATIVE";
                            } else if (num == 1) {
                                q = "NEUTRAL";
                            } else {
                                q = "POSITIVE";
                            }
                        instance.add(sp);
                        writer.writeAll(instance);
                        writer.close();
                    }
                    }
                }
                System.out.println("Found (" + links.size() + ") links");
                r = true;
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
            }
        }
        catch(IOException e)
        {
            // We were not successful in our HTTP request
            r = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public static String[] removeStopwords_list (String[] l){
        String temp = "";
        //System.out.println("Start removing");
        //int prev_size = l.size();
        //int new_size;
        Stopwords checker = new Stopwords();
        for(int i=0; i< l.length; i++){
            temp = l[i];
            //System.out.println("Check");
            if(checker.is(temp)){
                l[i]=" ";
                for(int j=0; i+j < l.length-1; j++) {
                    String h = "";
                    h = l[i+j];
                    l[i+j] = l[i+j+1];
                    l[i+j+1] = h;
                }
                //System.out.println("Removed " + l[i]);
                i--;
                ////tln("Removed stopword: " + temp);
            }else{
                ////tln("## Didnt Remove stopword: " + temp);
            }
        }

        //new_size = l.size();


        return l;
    }

    public String[] strTrim (String[] l){
        int t = 0;
        for (int i = 0; i<l.length;i++){
            if (!(l[i].equals(""))){
                t++;
            }
        }
        String[] e = new String[t];
        int y = 0;
        for (int i = 0; i<l.length;i++){
            if (!(l[i].equals(""))){
                e[y]=l[i];
                y++;
            }
        }
        return e;
    }

    public List<String> strToList (String[] l){
        List<String> e = new LinkedList<>();
        for (int i = 0; i<l.length;i++){
            if (!(l[i].equals(""))){
                e.add(l[i]);
            }
        }
        return e;
    }




    /**
     * Dictionary Lemmatizer Example in Apache OpenNLP
     */

        public static String[] DictionaryLemmatizer(String[] tokens){
            String[] lemmas = new String[0];
            try{
                // test sentence

                // Parts-Of-Speech Tagging
                // reading parts-of-speech model to a stream
                InputStream posModelIn = new FileInputStream("en-pos-maxent.bin");
                // loading the parts-of-speech model from stream
                POSModel posModel = new POSModel(posModelIn);
                // initializing the parts-of-speech tagger with model
                POSTaggerME posTagger = new POSTaggerME(posModel);
                // Tagger tagging the tokens
                String tags[] = posTagger.tag(tokens);

                // loading the dictionary to input stream
                InputStream dictLemmatizer = new FileInputStream("en_lemmatizer.txt");
                // loading the lemmatizer with dictionary
                DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(dictLemmatizer);

                // finding the lemmas
                lemmas = lemmatizer.lemmatize(tokens, tags);

                // printing the results
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lemmas;
        }

    public List<String> getLinks()
    {
        return this.links;
    }

}
