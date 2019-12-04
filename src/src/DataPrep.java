package src;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class DataPrep {
    StanfordCoreNLP pipeline = null;
    public DataPrep(){
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        pipeline = new StanfordCoreNLP(props);
    }
    public List<String> prepareData(String article){
        article = article.toLowerCase();
        article = article.replaceAll("[^a-zA-Z ]", "");
        List<String> lemmas = new LinkedList<>();
        Annotation annot = new Annotation(article);
        pipeline.annotate(annot);
        List<CoreMap> sentences = annot.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmas;
    }
}