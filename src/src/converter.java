package src;

import com.opencsv.CSVWriter;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class converter {
    public void create_CSV(String fname, List<String> art, String cat, int h) throws Exception {

        File file = new File(fname);
        FileWriter outputfile = new FileWriter(file, true);

        CSVWriter writer = new CSVWriter(outputfile);

        if (h==1) {
            String[] header = {"article", "category"};
            writer.writeNext(header);
        }
        List<String> row = new ArrayList<>();
        row.add(String.join(" ", art));
        row.add(cat);
        writer.writeNext(row.toArray(new String[row.size()]));
        writer.close();
    }

    public void convert_csv_to_arff(String path, String filename) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(path));
        Instances data = loader.getDataSet();
        System.out.println(data);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(filename));
        saver.writeBatch();
    }
}
