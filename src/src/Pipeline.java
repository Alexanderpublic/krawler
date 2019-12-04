package src;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class Pipeline {

    public static String text = "On Thursday night, Tesla CEO Elon Musk revealed his company's take on that most quintessentially " +
            "American of automobiles, the pickup truck. " +
            "\"Trucks have been basically the same for 100 years. " +
            "We want to do something different,\" Musk told a rapturous audience. " +
            "He wasn't underselling things. " +
            "Tesla's design is called the Cybertruck, and it looks like a cross between the Aston Martin Bulldog—a wedge-shaped" +
            " concept from the early 1980s—and that cool APC you remember from Aliens.\n" +
            "\"We moved the mass to the outside,\" Musk said, referring to the fact that the Cybertruck has a stainless steel monocoque construction, like the Model 3." +
            " Criticizing the body-on-frame construction technique used for most heavy trucks on sale today, Musk told attendees that \"the body and " +
            "the bed don't do anything useful,\" before launching into a lengthy demonstration of people hitting or shooting body panels" +
            " and glass from the Cybertruck to prove the toughness of the exterior.\n" +
            "The shape is highly unconventional, but the size could have been picked by a focus group—almost exactly as wide and tall " +
            "as a Ford F-150 and about as long as some four-seat versions of America's favorite pickup. At the rear, the 6.5-foot (2m) " +
            "bed—called the Cybertruck Vault here—has a lockable aerodynamic cover that gives the vehicle 100 cubic feet (2,831L) of " +
            "protected cargo storage. The Vault will also support loads of up to 3,500lbs (1,588kg).\n" +
            "Some of the Cybertruck's other features suggest that Musk might be paying attention to Bollinger, which is working on a very" +
            " un-Tesla-like range of boutique battery EV off-roaders. A Bollinger will have 15 inches of ground clearance via its air suspension," +
            " so the Cybertruck will have 16 inches, Musk revealed. Like the Bollinger, the Cybertruck will also offer 110V and 220V AC outlets, " +
            "so the vehicle can act as a power source on remote job sites.\n" +
            "There will be three versions of the Cybertruck. The single (rear) motor configuration will have a range of 250 miles (400km)" +
            " with a towing capacity of 7,500lbs (3,402kg) for $39,900. For an extra $10,000, there's a dual motor (all-wheel drive) variant, " +
            "which ups the towing capacity to 10,000lbs (4,536kg) and drops the 0-60mph time by two seconds. A trimotor Cybertruck—presumably " +
            "with one front motor and two rear motors—will cost $69,900 and is tow-rated for 14,000lbs (6,350kg), but you get 500 miles (800km) of range.\n" +
            "Tesla is now accepting $100 refundable deposits for the Cybertruck, which the order page says will go into production in late 2021, " +
            "with the three-motor version following a year later. ";

    static int sentCount(String s){
        return s.length() - s.replace(".", "").length() - 1;
    }

    static int wordCount(String s){
        return s.length() - s.replace(" ", "").length() - 1;
    }

    static void docWriter(String s) {
        try (FileWriter writer = new FileWriter("test.txt", true)) {
            writer.write(s);
            writer.append('\n');
            writer.flush();
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
        System.out.println("Успешно записан в файл");
    }

    static void fonEncode(String s, Metaphone3 m){
        m.SetWord(s);
        m.Encode();
        System.out.println("Word: " + s);
        System.out.println("Encoded: " + m.GetMetaph());
    }

    private static final String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~1234567890";

    public static String removePunct(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (PUNCT.indexOf(c) < 0) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Metaphone3 m3 = new Metaphone3();
        String r = "";

        r = removePunct(text);
        int e = wordCount(text);
        String[] a;
        a = r.split(" ");

        for (int i = 0; i< e;i++){
            fonEncode(a[i], m3);
        }

        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("ner.useSUTime", "false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        for (int i = 0; i < sentCount(text); i++) {
            String o = "";
            CoreSentence sentence = document.sentences().get(i);
            System.out.println("Sentence:");
            System.out.println(sentence.text());
            o+=sentence.text() + "\n"+ "\n";
            System.out.println();
            List<String> posTags = sentence.posTags();
            System.out.println("pos tags");
            System.out.println(posTags);
            o+=posTags + "\n"+ "\n";
            System.out.println();
            SemanticGraph dependencyParse = sentence.dependencyParse();
            System.out.println("Dependency parse");
            System.out.println(dependencyParse);
            o+=dependencyParse + "\n";
            System.out.println();
            //Tree constituencyParse = sentence.constituencyParse();
            //System.out.println("Constituency parse");
            //System.out.println(constituencyParse);
            //o+=constituencyParse + "\n";
            //System.out.println();
            docWriter(o);
        }
    }

}


