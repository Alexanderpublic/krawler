package src;

import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.stopwords.Rainbow;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Weka {

    public void trainingNB(String train,String test, String modelPath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(train);
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            System.out.println("reset index...");
            data.setClassIndex(data.numAttributes() - 1);
        }
        source = new ConverterUtils.DataSource(test);
        Instances newdata = source.getDataSet();
        if (newdata.classIndex() == -1) {
            newdata.setClassIndex(newdata.numAttributes()-1);
        }
        newdata.stratify(214);

        Rainbow r = new Rainbow();
        NGramTokenizer tok = new NGramTokenizer();
        tok.setOptions(new String[]{"max 3", "min 1", "-delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\'\\\\\\\"()?!\\\""});
        //SnowballStemmer stemmer = new SnowballStemmer();*/

        NaiveBayesMultinomialText bayes = new NaiveBayesMultinomialText();
        //bayes.setStemmer(stemmer);
        bayes.setTokenizer(tok);
        bayes.setStopwordsHandler(r);
        bayes.setNorm(1.0);
        bayes.setLNorm(2.0);
        bayes.setMinWordFrequency(3.0);

        bayes.buildClassifier(data);

        //StringToWordVector filter = new StringToWordVector();
        //FilteredClassifier classifier = new FilteredClassifier();
        //classifier.setFilter(filter);
        //classifier.setClassifier(bayes);
        //classifier.buildClassifier(data);

        //Evaluation evaluation = new Evaluation(newdata);
        //evaluation.evaluateModel(bayes, newdata);
        //System.out.println(evaluation.toSummaryString("title",true));
        //System.out.println("Precision: " + evaluation.precision(1));
        //System.out.println("Recall: " + evaluation.precision(1));
        weka.core.SerializationHelper.write(modelPath, bayes);
    }

    public void trainingKNN(String path, String modelPath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("File.arff");
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        CorrelationAttributeEval evall = new CorrelationAttributeEval();
        Ranker ranker = new Ranker();
        AttributeSelection selector = new AttributeSelection();
        selector.setEvaluator(evall);
        selector.setSearch(ranker);
        selector.setInputFormat(data);
        data = Filter.useFilter(data, selector);
        data.stratify(214);
        Instances[][] split = new Instances[2][214];

        for (int i = 0; i < 214; i++) {
            split[0][i] = data.trainCV(214, i);
            split[1][i] = data.testCV(214, i);
        }
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];
        StringToWordVector filter = new StringToWordVector();
        FilteredClassifier classifier = new FilteredClassifier();
        classifier.setFilter(filter);

        classifier.setClassifier((Classifier) new LinearNNSearch());


        for (int i = 0; i < trainingSplits.length; i++) {
            classifier.buildClassifier(trainingSplits[i]);
            Evaluation evaluation = new Evaluation(trainingSplits[i]);

            evaluation.evaluateModel(classifier, testingSplits[i]);
            System.out.println(evaluation.toSummaryString());

            weka.core.SerializationHelper.write(modelPath, classifier);
        }
    }

    public void test_model(String model, String path) throws Exception {
        Classifier cls = (Classifier) weka.core.SerializationHelper.read(model);

        ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(path);
        Instances test = source2.getDataSet();

        if (test.classIndex() == -1)
            test.setClassIndex(1);

        Evaluation evaluation = new Evaluation(test);
        evaluation.evaluateModel(cls, test);
        System.out.println(evaluation.toSummaryString("title",true));
        System.out.println("Precision: " + evaluation.precision(1));
        System.out.println("Recall: " + evaluation.precision(1));
        System.out.println("PRC AUC: " + evaluation.areaUnderPRC(1));
        System.out.println("ROC AUC: " + evaluation.areaUnderROC(1));
        weka.core.SerializationHelper.write(model,cls);
    }
}
