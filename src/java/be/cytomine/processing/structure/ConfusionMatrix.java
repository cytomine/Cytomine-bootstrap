package be.cytomine.processing.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: lrollus
 * Date: 2/02/12
 * GIGA-ULg
 */
public class ConfusionMatrix {


    public Map<String, Integer> header;
    public Map<Integer, String> headerInverse;
    public Integer[][] matrix;
    public double[] result;

    public ConfusionMatrix(List<String> className) {
        System.out.println("Before ordering:"+ className);
        Collections.sort(className);
        System.out.println("After ordering:"+ className);
        header = new HashMap<String,Integer>();
        headerInverse = new HashMap<Integer,String>();
        for(int i=0;i<className.size();i++) {
            header.put(className.get(i),i);
            headerInverse.put(i,className.get(i));
        }
        matrix = new Integer[className.size()][className.size()];

        //fill with 0
        for(int i=0;i<matrix.length;i++) {
            for(int j=0;j<matrix.length;j++) {
                matrix[i][j]=0;
            }
        }

        result = new double[className.size()];
        for(int i=0;i<result.length;i++) {
             result[i]=-1d;
        }
    }

    public void addEntry(String termReal, String termSuggest) {
        int i =  header.get(termReal);
        int j =  header.get(termSuggest);
        Integer oldValue = matrix[i][j];
        //System.out.println("oldValue:"+oldValue);
        matrix[i][j]=oldValue+1;
        updateResult(i);
    }

    public void updateResult(int i) {
        double sum = 0;
        for(int j=0;j<matrix[i].length;j++) {
            sum=sum+matrix[i][j];
        }
        result[i] = (double)matrix[i][i]/sum;
    }

    public double[] getResults() {
        return result;
    }

    public double getDiagonalSum() {
        double sum = 0;
        for(int i=0;i<matrix.length;i++) {
            sum=sum+matrix[i][i];
        }
        return sum;
    }

    public double getTotalSum() {
        double sum = 0;
        for(int i=0;i<matrix.length;i++) {
            for(int j=0;j<matrix[i].length;j++) {
                sum=sum+matrix[i][j];
            }
        }
        return sum;
    }

    public void print() {
        System.out.println("******************************************");
        System.out.print("X;");
        for(int j=0;j<matrix.length;j++) {
           System.out.print(headerInverse.get(j) + ";");
        }
        System.out.println("");

        for(int i=0;i<matrix.length;i++) {
            System.out.print(headerInverse.get(i)+";");
            for(int j=0;j<matrix.length;j++) {
                System.out.print(matrix[i][j]+";");
            }
            System.out.println(result[i] + ";");

        }
        System.out.println("******************************************");
    }


    public String toJSON() {
        //start a new array
        String json  = "[";
        //start a new line
        json = json + "[ 0,";
        for(int j=0;j<matrix.length;j++) {
           json = json + headerInverse.get(j)+",";
        }
        json = json + "0";
        //json = json.substring(0,json.length()-2);
        json = json+"],";
        //json = json + "\n";

        for(int i=0;i<matrix.length;i++) {
            json = json + "[";
            json = json + headerInverse.get(i)+",";
            for(int j=0;j<matrix.length;j++) {
                json = json + matrix[i][j]+",";
            }
            json = json + result[i];
            json = json+"],";
            //json = json + "\n";
        }
        json = json.substring(0,json.length()-2);
        json = json+"]";
         json = json+"]";
        return json;
    }
}
