package src;

public class test {
    public static void main(String[] args) throws Exception {
        int spider_threads = 8;
        converter con = new converter();
        //con.create_CSV(Collections.singletonList("article"), "category", 1);
        /*Threading thread;
        spider spider = new spider("https://arstechnica.com/science/2019/06/", "science/2019", "/?", "",
                "https://arstechnica.com/science/", "science");
        spider.search();*/
        spider spider = new spider("https://arstechnica.com/science/2019/06/", "science/2019", "/?", "",
                "https://arstechnica.com/science/", "science");
        for (int i = 0; i < spider_threads; i++) {
            Threading thread;
            thread = new Threading(spider);
            thread.start();
        }
        spider = new spider("https://arstechnica.com/cars/2019/06/", "cars/2019", "/?", "",
                "https://arstechnica.com/cars/", "cars");
        for (int i = 0; i < spider_threads; i++) {
            Threading thread;
            thread = new Threading(spider);
            thread.start();
        }
        spider = new spider("https://arstechnica.com/gaming/2019/06/", "gaming/2019", "/?", "",
                "https://arstechnica.com/gaming/", "gaming");
        for (int i = 0; i < spider_threads; i++) {
            Threading thread;
            thread = new Threading(spider);
            thread.start();
        }
        spider = new spider("https://arstechnica.com/gadgets/2019/06/", "gadgets/2019", "/?", "",
                "https://arstechnica.com/gadgets/", "gadgets");
        for (int i = 0; i < spider_threads; i++) {
            Threading thread;
            thread = new Threading(spider);
            thread.start();
        }
        spider = new spider("https://arstechnica.com/information-technology/2019/06/", "information-technology/2019", "/?", "",
                "https://arstechnica.com/information-technology/", "IT");
        for (int i = 0; i < spider_threads; i++) {
            Threading thread;
            thread = new Threading(spider);
            thread.start();
        }


        Weka ml = new Weka();

        con.convert_csv_to_arff("./mk2.csv", "./train.arff");
        //con.convert_csv_to_arff("File2.csv", "test.arff");
        ml.trainingNB("train.arff","test.arff", "NaiveBayesJava.model");
        ml.test_model("NaiveBayesJava.model", "test.arff");
        //test.main(new String[0]);
        // 9200 - elastic 5601 - kibana
    }
}
