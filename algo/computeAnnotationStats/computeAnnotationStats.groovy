//host:
//publicKey:
//privateKey:
//annotation:
//term:

/*

groovy -cp "algo/computeAnnotationStats/Cytomine-Java-Client.jar:algo/computeAnnotationStats/jts-1.13.jar" algo/computeAnnotationStats/computeAnnotationStats.groovy http://localhost:8080 29f51819-3dc6-468c-8aa7-9c81b9bc236b db214699-0384-498c-823f-801654238a67 92843690 20202
92843753

 */

import com.vividsolutions.jts.geom.*
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.client.models.*;
import be.cytomine.client.collections.*;
import be.cytomine.client.*;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;
import com.vividsolutions.jts.geom.PrecisionModel;


println args

String job = args[0]
String userjob = args[1]
String host= args[2]
String publickey = args[3]
String privatekey= args[4]
String annotation= args[5]
String idTerm = args[6]


Cytomine cytomine = new Cytomine(host, publickey, privatekey, "./");
Annotation baseAnnotation = cytomine.getAnnotation(Long.parseLong(annotation))
Term term = cytomine.getTerm(Long.parseLong(idTerm))

String propertyNumber = "NUMBER_OF_"+term.getStr("name")
String propertyArea = "AREA_OF_"+term.getStr("name")

def filters = [:]
filters.put("project",baseAnnotation.getStr("project"));
filters.put("image",baseAnnotation.getStr("image"));
filters.put("term",idTerm+"");
filters.put("showWKT","true");
filters.put("showTerm","true");
filters.put("showMeta","true");
filters.put("showBasic","true");
filters.put("bbox",URLEncoder.encode(baseAnnotation.getStr("location"), "UTF-8"));

AnnotationCollection annotationsSameTerm = cytomine.getAnnotations(filters);


Long size = 0l

def baseGeometry = new WKTReader().read(baseAnnotation.get('location'))
def geometries = []

for(int i=0;i<annotationsSameTerm.size();i++) {
    geometries << new WKTReader().read(annotationsSameTerm.get(i).getStr('location'))
}



def insideBaseGeometry = geometries.collect{it.intersection(baseGeometry)}
insideBaseGeometry.each {
    println "size="+it.area
    size = size + it.area
}

addProperty(cytomine,baseAnnotation.getId(),propertyNumber,annotationsSameTerm.size()+"")
addProperty(cytomine,baseAnnotation.getId(),propertyArea,size + baseAnnotation.getStr("areaUnit"))



println size


def addProperty(def cytomine,Long idAnnotation, String key, String value) {
    Property property = null
    try {
        property = cytomine.getPropertyByDomainAndKey("annotation", idAnnotation,property);
        cytomine.deleteDomainProperty("annotation", idAnnotation,property.getId())
    } catch(Exception e) {
        println e
    }
    cytomine.addDomainProperties("annotation", idAnnotation,key,value)
}