package be.cytomine.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/11/11
 * Time: 10:13
 * This util class allow to sort a ordred map (treemap, not hashmap!) by its value  (desc order)
 * [a:10,b:5,c:12] will be [c:12,a:10,b:5]
 */
public class ValueComparator implements Comparator {

  Map base;
  public ValueComparator(Map base) {
      this.base = base;
  }

  public int compare(Object a, Object b) {

    if((Double)base.get(a) < (Double)base.get(b)) {
      return 1;
    } else if((Double)base.get(a) == (Double)base.get(b)) {
      return 0;
    } else {
      return -1;
    }
  }
}