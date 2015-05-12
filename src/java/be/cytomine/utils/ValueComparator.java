package be.cytomine.utils;

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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