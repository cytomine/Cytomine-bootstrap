package be.cytomine.processing.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: lrollus
 * Date: 2/02/12
 * GIGA-ULg
 * This class implement a confusion matrix
 * Each column of the matrix represents the instances in a predicted class,
 * while each row represents the instances in an actual class
 */
public class ConfusionMatrix {

    /**
     * Match class and its position (in row and column)
     */
    private Map<String, Integer> header;

    /**
     * Match position and its class
     */
    private Map<Integer, String> headerInverse;

    /**
     * Matrix without header
     */
    private Integer[][] matrix;

    /**
     * Last column of matrix (result)
     */
    private double[] result;

    /**
     * Build a confusion matrix with class list
     * @param className Class list
     */
    public ConfusionMatrix(List<String> className) {

        //build header info
        header = new HashMap<String,Integer>();
        headerInverse = new HashMap<Integer,String>();
        for(int i=0;i<className.size();i++) {
            header.put(className.get(i),i);
            headerInverse.put(i,className.get(i));
        }

        //build matrix and fill with 0
        matrix = new Integer[className.size()][className.size()];
        for(int i=0;i<matrix.length;i++) {
            for(int j=0;j<matrix.length;j++) {
                matrix[i][j]=0;
            }
        }

        //build result column and fill with -1
        result = new double[className.size()];
        for(int i=0;i<result.length;i++) {
             result[i]=-1d;
        }
    }

    /**
     * Increment an entry from matrix; Add 1 to the cell corresponding to [termReal,termSuggest]
     * @param termReal Real class
     * @param termSuggest Suggest class
     */
    public void incrementEntry(String termReal, String termSuggest) {
        int i =  header.get(termReal);
        int j =  header.get(termSuggest);
        Integer oldValue = matrix[i][j];
        matrix[i][j]=oldValue+1;
        updateResult(i);
    }

    /**
     * Compute result column for line i
     * @param i Line
     */
    public void updateResult(int i) {
        double sum = 0;
        for(int j=0;j<matrix[i].length;j++) {
            sum=sum+matrix[i][j];
        }
        result[i] = (double)matrix[i][i]/sum;
    }

    /**
     * Convert confusion matrix object to JSON
     * @return confusion matrix in JSON
     */
    public String toJSON() {
        //start a new array
        String json  = "[";
        //start a new line
        json = json + "[ 0,";
        for(int j=0;j<matrix.length;j++) {
           json = json + headerInverse.get(j)+",";
        }
        json = json + "0";
        json = json+"],";

        for(int i=0;i<matrix.length;i++) {
            json = json + "[";
            json = json + headerInverse.get(i)+",";
            for(int j=0;j<matrix.length;j++) {
                json = json + matrix[i][j]+",";
            }
            json = json + result[i];
            json = json+"],";
        }
        json = json.substring(0,json.length()-2);
        json = json+"]";
        json = json+"]";
        return json;
    }
}
