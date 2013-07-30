package be.cytomine.sql

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import com.vividsolutions.jts.io.WKTReader

/**
 * User: lrollus
 * Date: 31/05/13
 * GIGA-ULg
 *
 *
 *

 *
 */
abstract class AnnotationListing {
    /**
     *  default property group to show
     */
    static final def availableColumnDefault = ['basic','meta','term']

    /**
     *  all properties group available, each value is a list of assoc [propertyName, SQL columnName/methodName)
     *  If value start with #, don't use SQL column, its a "trensiant property"
     */
    abstract def availableColumn

    def columnToPrint

    def project = null
    def user = null
    def term = null
    def image = null
    def suggestedTerm = null
    def userForTermAlgo = null
//    def jobForTermAlgo = null
    def users = null //for user that draw annotation
    def usersForTerm = null //for user that add a term to annotation
    def usersForTermAlgo = null
    def terms = null
    def images = null

    def suggestedTerms = null

    def notReviewedOnly = false
    def noTerm = false
    def noAlgoTerm = false
    def multipleTerm = false


    def bbox = null

    def parents

    //not used for search critera (just for specific request
    def avoidEmptyCentroid = false
    def excludedAnnotation = null
    def  kmeans = false

    def kmeansValue = 3

    abstract def getFrom()

    abstract def getDomainClass()
    
    abstract def  buildExtraRequest()

    def extraColmun = [:]

    def orderBy = null

    def addExtraColumn(def propName, def column) {
        extraColmun[propName]= column
    }


    /**
     * Get all properties name available
     * If group argument is provieded, just get properties from these groups
     */
    def getAllPropertiesName(List groups = getAvailableColumn().collect { it.key }) {
        def propNames = []
        groups.each { groupName ->
           getAvailableColumn().get(groupName).each { assoc ->
                assoc.each {
                    propNames << it.key
                }
            }
        }
        propNames
    }

    /**
     * Get all properties to print
     */
    def buildColumnToPrint() {
        println "availableColumnDefault=$availableColumnDefault"
        if(!columnToPrint) {
            columnToPrint = availableColumnDefault.clone()
        }
        columnToPrint.add('basic') //mandatory to have id
        columnToPrint = columnToPrint.unique()

        def columns = []
        println "columnToPrint=$columnToPrint"

        getAvailableColumn().each {
           if(columnToPrint.contains(it.key)) {
               it.value.each { columnAssoc ->
                   columns << columnAssoc
               }
           }
        }
        extraColmun.each {
            println it
            columns << it
        }
        return columns
    }


    /**
     * Get container for security check
     */
    CytomineDomain container() {
        if(project) return Project.read(project)
        if(image) return ImageInstance.read(image)?.container()
        if(images) {
            def projectList = images.collect{ImageInstance.read(it).project}.unique()
            if(projectList.size()>1) {
                throw new WrongArgumentException("Images from filter must all be from the same project!")
            }
            return projectList.first()
        }
        throw new WrongArgumentException("There is no project or image filter. We cannot check acl!")
    }

    /**
     * Generate SQL request string
     */
    def getAnnotationsRequest() {
        
        buildExtraRequest()

        def columns = buildColumnToPrint()
        def sqlColumns = []
        def postComputedColumns = []

        columns.each {
            if(!it.value.startsWith("#"))
                sqlColumns << it
            else
                postComputedColumns <<  it
        }

        String whereRequest =
                getProjectConst() +
                getUsersConst() +
                getImagesConst() +
                getImageConst() +
                getTermConst() +
                getTermsConst() +
                getUserConst() +
                getParentsConst() +
                getUsersForTermAlgoConst()+
                getExcludedAnnotationConst() +
                getUserForTermAlgoConst() +
//                getJobForTermAlgoConst() +
                getSuggestedTermConst()+
                getSuggestedTermsConst() +
                getNotReviewedOnlyConst() +
                getUsersForTermConst() +
                getAvoidEmptyCentroidConst() +
                getIntersectConst() +
                createOrderBy()

        return  getSelect(sqlColumns) + getFrom() + whereRequest

    }

    /**
      * Generate SQL string for SELECT with only asked properties
      */
     def getSelect(def columns) {
         if(!(kmeansValue<3)) {
             def requestHeadList = []
             columns.each {
                 requestHeadList << it.value + " as " + it.key
             }
             return "SELECT " +  requestHeadList.join(', ')  + " \n"
         } else {
             return "SELECT kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n"
         }

     }
    /**
      * Add property group to show if use in where constraint.
      * E.g: if const with term_id = x, we need to make a join on annotation_term.
      * So its mandatory to add "term" group properties (even if not asked)
      */
     def addIfMissingColumn(def column) {
         if(!columnToPrint.contains(column)) {
             columnToPrint.add(column)
         }
     }

     def getProjectConst() {
         return (project? "AND a.project_id = $project\n" : "")
     }

     def getUsersConst() {
         return (users? "AND a.user_id IN (${users.join(",")})\n" : "")
     }

     def getUsersForTermConst() {
         if(usersForTerm) {
             addIfMissingColumn('term')
             return "AND at.user_id IN (${usersForTerm.join(",")})\n"
         } else return ""
     }

     def getImagesConst() {
         if(images && project && images.size()==Project.read(project).countImages) {
             return "" //images number equals to project image number, no const needed
         } else {
             return (images? "AND a.image_id IN (${images.join(",")})\n" : "")
         }

     }

     def getImageConst() {
         if(image) {
             if(!ImageInstance.read(image)) {
                throw new ObjectNotFoundException("Image $image not exist!")
            }
             return "AND a.image_id = ${image}\n"
         } else {
            return ""
         }
     }

     def getUserConst() {
         if(user) {
             if(!SecUser.read(user)) {
                 throw new ObjectNotFoundException("User $user not exist!")
             }
             return "AND a.user_id = ${user}\n"
         } else {
             return ""
         }
     }

     abstract def getNotReviewedOnlyConst()

     def getIntersectConst() {
         return (bbox? "AND ST_Intersects(a.location,ST_GeometryFromText('${bbox.toString()}',0))\n" : "")
     }

     def getAvoidEmptyCentroidConst() {
         return (avoidEmptyCentroid? "AND ST_IsEmpty(st_centroid(a.location))=false\n":"")
     }

     def getTermConst() {
         if(term) {
             if(!Term.read(term)) {
                 throw new ObjectNotFoundException("Term $term not exist!")
             }
             addIfMissingColumn('term')
             return " AND at.term_id = ${term}\n"
         } else return ""
     }

    def getParentsConst() {
        if(parents) {
            return " AND a.parent_ident IN (${parents.join(",")})\n"
        } else return ""
    }


     def getTermsConst() {
         if(terms) {
             addIfMissingColumn('term')
             return "AND at.term_id IN (${terms.join(',')})\n"
         } else return ""
     }

     def getExcludedAnnotationConst() {
         return (excludedAnnotation? "AND a.id <> ${excludedAnnotation}\n" : "")
     }

     def getSuggestedTermConst() {
         if(suggestedTerm) {
             if(!Term.read(suggestedTerm)) {
                 throw new ObjectNotFoundException("Term $suggestedTerm not exist!")
             }
             println "******************** 1"
             addIfMissingColumn('algo')
             return "AND aat.term_id = ${suggestedTerm}\n"
         } else return ""
     }

    def getSuggestedTermsConst() {
        println "2suggestedTerms=$terms"
        if(suggestedTerms) {
            println "******************** 2"
            addIfMissingColumn('algo')
            return "AND aat.term_id IN (${suggestedTerms.join(",")})\n"
        } else return ""
    }

     def getUserForTermAlgoConst() {
         if(userForTermAlgo) {
             println "******************** 3"
             addIfMissingColumn('term')
             addIfMissingColumn('algo')
             return "AND aat.user_job_id = ${userForTermAlgo}\n"
         } else return ""
     }

//    def getJobForTermAlgoConst() {
//        if(jobForTermAlgo) {
//            println "******************** 3"
//            addIfMissingColumn('term')
//            addIfMissingColumn('algo')
//            return "AND aat.user_job_id = (SELECT id FROM sec_user${jobForTermAlgo})\n"
//        } else return ""
//    }

    def getUsersForTermAlgoConst() {
        if(usersForTermAlgo) {
            println "******************** 5"
            addIfMissingColumn('algo')
            addIfMissingColumn('term')
            return "AND aat.user_job_id IN (${usersForTermAlgo.join(',')})\n"
        } else return ""
    }

    abstract def createOrderBy()

}

class UserAnnotationListing extends AnnotationListing {

    def getDomainClass() {
        return "be.cytomine.ontology.UserAnnotation"
    }

    /**
     *  all properties group available, each value is a list of assoc [propertyName, SQL columnName/methodName)
     *  If value start with #, don't use SQL column, its a "trensiant property"
     */
    def availableColumn =
        [
            basic : [id:'a.id'],
            meta : [
                    countReviewedAnnotations : 'a.count_reviewed_annotations',
                    reviewed : '(a.count_reviewed_annotations>0)',
                    image : 'a.image_id',
                    project : 'a.project_id',
                    container : "a.project_id",
                    created : 'extract(epoch from a.created)*1000',
                    updated : 'extract(epoch from a.updated)*1000',
                    user : 'a.user_id',
                    countComments : 'a.count_comments',
                    geometryCompression : 'a.geometry_compression',
                    cropURL : '#cropURL',
                    smallCropURL : '#smallCropURL',
                    url: '#url',
                    imageURL : '#imageURL'

            ],
            wkt : [location:'a.wkt_location'],
            gis : [area: 'area',areaUnit: 'area_unit', perimeter:'perimeter',perimeterUnit : 'perimeter_unit', x:'ST_X(ST_centroid(a.location))',y:'ST_Y(ST_centroid(a.location))'],
            term : [term : 'at.term_id', annotationTerms : 'at.id', userTerm: 'at.user_id'],
            image : [originalfilename : 'ai.original_filename'],
            algo : [id:'aat.id',rate:'aat.rate',idTerm:'aat.term_id',idExpectedTerm:'aat.expected_term_id'],
            user : [creator:'u.username',lastname: 'u.lastname',firstname: 'u.firstname']
        ]


    /**
      * Generate SQL string for FROM
      * FROM depends on data to print (if image name is aksed, need to join with imageinstance+abstractimage,...)
      */
     def getFrom() {

         def from  = "FROM user_annotation a "
         def where = "WHERE true\n"


          if(multipleTerm) {
              from = "$from, annotation_term at, annotation_term at2 "
              where = "$where" +
                      "AND a.id = at.user_annotation_id\n" +
                     " AND a.id = at2.user_annotation_id\n" +
                     " AND at.id <> at2.id \n" +
                     " AND at.term_id <> at2.term_id \n"
          } else if(noTerm) {
              from = "$from LEFT JOIN (SELECT * from annotation_term x ${users ? "where x.user_id IN (${users.join(",")})":""}) at ON a.id = at.user_annotation_id "
              where = "$where AND at.id IS NULL \n"
          } else if(noAlgoTerm) {
              from = "$from LEFT JOIN (SELECT * from algo_annotation_term x ${users ? "where x.user_id IN (${users.join(",")})":""}) aat ON a.id = aat.annotation_ident "
              where = "$where AND aat.id IS NULL \n"
          } else {
              if(columnToPrint.contains('term')) {
                  from = "$from LEFT OUTER JOIN annotation_term at ON a.id = at.user_annotation_id"
              }

          }

         if(columnToPrint.contains('user')) {
             from = "$from, sec_user u "
             where = "$where AND a.user_id = u.id \n"
         }

         if(columnToPrint.contains('image')) {
             from = "$from, abstract_image ai, image_instance ii "
             where = "$where AND a.image_id = ii.id \n" +
                     "AND ii.base_image_id = ai.id\n"
         }
           println "columnToPrint=$columnToPrint"
          println "columnToPrint=${columnToPrint.contains('algo')}"
          if(columnToPrint.contains('algo')) {
              from = "$from, algo_annotation_term aat "
              where = "$where AND aat.annotation_ident = a.id\n"
          }

        return from +"\n" + where
     }
    def buildExtraRequest() {
        
    }

    def getNotReviewedOnlyConst() {
        return (notReviewedOnly? "AND a.count_reviewed_annotations=0\n" : "" )
    }

    def createOrderBy() {
        if(kmeansValue<3) return ""
        def orderByRate = (usersForTermAlgo || userForTermAlgo || suggestedTerm || suggestedTerms)
        if(orderByRate) {
            return "ORDER BY aat.rate desc"
        }else if(!orderBy) {
            return "ORDER BY a.id desc " + (columnToPrint.contains("term")? ", term " : "")
        } else {
            return "ORDER BY " + orderBy.collect{it.key + " " + it.value}.join(", ")
        }
     }


//    def createOrderBy() {
//
//        if(kmeansValue<3) return ""
//        if(!orderBy) {
//            return "ORDER BY " + (columnToPrint.contains("term")? "aat.rate desc ," : "")  + " a.id desc "
//        }else {
//            return "ORDER BY " + orderBy.collect{it.key + " " + it.value}.join(", ")
//        }
//     }

}



class AlgoAnnotationListing extends AnnotationListing {
 //parentIdent : 'a.parent_ident',
 //user -> user_job_id?
 //algo rate

     def getDomainClass() {
         return "be.cytomine.ontology.AlgoAnnotation"
     }

    /**
     *  all properties group available, each value is a list of assoc [propertyName, SQL columnName/methodName)
     *  If value start with #, don't use SQL column, its a "trensiant property"
     */
    def availableColumn =
        [
            basic : [id:'a.id'],
            meta : [
                    countReviewedAnnotations : 'a.count_reviewed_annotations',
                    reviewed : '(a.count_reviewed_annotations>0)',
                    image : 'a.image_id',
                    project : 'a.project_id',
                    container : "a.project_id",
                    created : 'extract(epoch from a.created)*1000',
                    updated : 'extract(epoch from a.updated)*1000',
                    user : 'a.user_id',
                    countComments : 'a.count_comments',
                    geometryCompression : 'a.geometry_compression',
                    cropURL : '#cropURL',
                    smallCropURL : '#smallCropURL',
                    url: '#url',
                    imageURL : '#imageURL'

            ],
            wkt : [location:'a.wkt_location'],
            gis : [area: 'area',areaUnit: 'area_unit', perimeter:'perimeter',perimeterUnit : 'perimeter_unit',x:'ST_X(ST_centroid(a.location))',y:'ST_Y(ST_centroid(a.location))'],
            term : [term : 'aat.term_id', annotationTerms : 'aat.id', userTerm: 'aat.user_job_id',rate:'aat.rate'],
            image : [originalfilename : 'ai.original_filename'],
            user : [creator:'u.username',software: 's.name',job: 'j.created']
        ]


    /**
      * Generate SQL string for FROM
      * FROM depends on data to print (if image name is aksed, need to join with imageinstance+abstractimage,...)
      */
     def getFrom() {

         def from  = "FROM algo_annotation a "
         def where = "WHERE true\n"


          if(multipleTerm) {
              from = "$from, algo_annotation_term aat, algo_annotation_term aat2 "
              where = "$where" +
                      "AND a.id = aat.annotation_ident\n" +
                     " AND a.id = aat2.annotation_ident\n" +
                     " AND aat.id <> aat2.id \n" +
                     " AND aat.term_id <> aat2.term_id \n"
          } else if(noTerm || noAlgoTerm) {
              from = "$from LEFT JOIN (SELECT * from algo_annotation_term x ${users ? "where x.user_job_id IN (${users.join(",")})":""}) aat ON a.id = aat.annotation_ident "
              where = "$where AND aat.id IS NULL \n"

          } else {
              if(columnToPrint.contains('term')) {
                  from = "$from LEFT OUTER JOIN algo_annotation_term aat ON a.id = aat.annotation_ident"
              }

             if(columnToPrint.contains('image')) {
                 from = "$from, abstract_image ai, image_instance ii "
                 where = "$where AND a.image_id = ii.id \n" +
                         "AND ii.base_image_id = ai.id\n"
             }

              if(columnToPrint.contains('user')) {
                  from = "$from, sec_user u, job j, software s "
                  where = "$where AND a.user_id = u.id \n" +
                          "AND u.job_id = j.id\n" +
                          "AND j.software_id = s.id\n"
              }
          }

        return from +"\n" + where
     }
    
    def buildExtraRequest() {
        
    }

    def getTermConst() {
        if(term) {
            addIfMissingColumn('term')
            return " AND aat.term_id = ${term}\n"
        } else return ""
    }

    def getTermsConst() {
        println "2terms=$terms"
        if(terms) {
            addIfMissingColumn('term')
            return "AND aat.term_id IN (${terms.join(',')})\n"
        } else return ""
    }


    def getUserConst() {
        return (user? "AND a.user_id = ${user}\n" : "")
    }

    def getUsersConst() {
        return (users? "AND a.user_id IN (${users.join(",")})\n" : "")
    }

    def getNotReviewedOnlyConst() {
        return (notReviewedOnly? "AND a.count_reviewed_annotations=0\n" : "" )
    }

    def createOrderBy() {

        if(kmeansValue<3) return ""
        if(!orderBy) {
            return "ORDER BY " + (columnToPrint.contains("term")? "aat.rate desc ," : "")  + " a.id desc "
        }else {
            return "ORDER BY " + orderBy.collect{it.key + " " + it.value}.join(", ")
        }
     }
}



class ReviewedAnnotationListing extends AnnotationListing {

    def getDomainClass() {
        return "be.cytomine.ontology.ReviewedAnnotation"
    }

    /**
     *  all properties group available, each value is a list of assoc [propertyName, SQL columnName/methodName)
     *  If value start with #, don't use SQL column, its a "trensiant property"
     */
    def availableColumn =
        [
            basic : [id:'a.id'],
            meta : [
                    reviewed : 'true',
                    image : 'a.image_id',
                    project : 'a.project_id',
                    container : "a.project_id",
                    created : 'extract(epoch from a.created)*1000',
                    updated : 'extract(epoch from a.updated)*1000',
                    user : 'a.user_id',
                    countComments : 'a.count_comments',
                    geometryCompression : 'a.geometry_compression',
                    cropURL : '#cropURL',
                    smallCropURL : '#smallCropURL',
                    url: '#url',
                    imageURL : '#imageURL',
                    parentIdent : 'parent_ident'

            ],
            wkt : [location:'a.wkt_location'],
            gis : [area: 'area',areaUnit: 'area_unit', perimeter:'perimeter',perimeterUnit : 'perimeter_unit',x:'ST_X(ST_centroid(a.location))',y:'ST_Y(ST_centroid(a.location))'],
            term : [term : 'at.term_id', annotationTerms: "0",userTerm: 'a.user_id'],//user who add the term, is the user that create reviewedannotation (a.user_id)
            image : [originalfilename : 'ai.original_filename'],
            algo : [id:'aat.id',rate:'aat.rate'],
            user : [creator:'u.username',lastname: 'u.lastname',firstname: 'u.firstname']
        ]


    /**
      * Generate SQL string for FROM
      * FROM depends on data to print (if image name is aksed, need to join with imageinstance+abstractimage,...)
      */
     def getFrom() {

         def from  = "FROM reviewed_annotation a "
         def where = "WHERE true\n"


          if(multipleTerm) {
              from = "$from, reviewed_annotation_term at, reviewed_annotation_term at2 "
              where = "$where" +
                      "AND a.id = at.reviewed_annotation_terms_id\n" +
                     " AND a.id = at2.reviewed_annotation_terms_id\n" +
                     " AND at.id <> at2.id \n" +
                     " AND at.term_id <> at2.term_id \n"
          } else if(noTerm) {
              from = "$from LEFT OUTER JOIN reviewed_annotation_term at ON a.id = at.reviewed_annotation_terms_id "
              where = "$where AND at.reviewed_annotation_terms_id IS NULL \n"
          }  else {
              if(columnToPrint.contains('term')) {
                  from = "$from LEFT OUTER JOIN reviewed_annotation_term at ON a.id = at.reviewed_annotation_terms_id"
              }

          }

         if(columnToPrint.contains('image')) {
             from = "$from, abstract_image ai, image_instance ii "
             where = "$where AND a.image_id = ii.id \n" +
                     "AND ii.base_image_id = ai.id\n"
         }

         if(columnToPrint.contains('user')) {
             from = "$from, sec_user u "
             where = "$where AND a.user_id = u.id \n"
         }

        return from +"\n" + where
     }


    def buildExtraRequest() {

        if(kmeansValue==3 && image && bbox) {
            /**
             * We will sort annotation so that big annotation that covers a lot of annotation comes first (appear behind little annotation so we can select annotation behind other)
             * We compute in 'gc' the set of all other annotation that must be list
             * For each review annotation, we compute the number of other annotation that cover it (ST_CoveredBy => t or f => 0 or 1)
             *
             * ST_CoveredBy will return false if the annotation is not perfectly "under" the compare annotation (if some points are outside)
             * So in gc, we increase the size of each compare annotation just for the check
             * So if an annotation x is under y but x has some point next outside y, x will appear top (if no resize, it will appear top or behind).
             */
            def xfactor = "1.28"
            def yfactor = "1.28"
            def image = ImageInstance.read(image)
            //TODO:: get zoom info from UI client, display with scaling only with hight zoom (< annotations)

            double imageWidth = image.baseImage.width
            def bboxLocal = new WKTReader().read(bbox)
            double bboxWidth = bboxLocal.getEnvelopeInternal().width
            double ratio = bboxWidth/imageWidth*100

            boolean zoomToLow = ratio > 50

            println "imageWidth=$imageWidth"
            println "bboxWidth=$bboxWidth"
            println "ratio=$ratio"

            println "zoomToLow="+zoomToLow
            String subRequest
            if (zoomToLow) {
                subRequest = "(SELECT SUM(ST_CoveredBy(ga.location,gb.location )::integer) "
            } else {
                //too heavy to use with little zoom
                subRequest = "(SELECT SUM(ST_CoveredBy(ga.location,ST_Translate(ST_Scale(gb.location, $xfactor, $yfactor), ST_X(ST_Centroid(gb.location))*(1 - $xfactor), ST_Y(ST_Centroid(gb.location))*(1 - $yfactor) ))::integer) "

            }

            subRequest =  subRequest +
                    "FROM reviewed_annotation ga, reviewed_annotation gb " +
                "WHERE ga.id=a.id " +
                "AND ga.id<>gb.id " +
                "AND ga.image_id=gb.image_id " +
                "AND ST_Intersects(gb.location,ST_GeometryFromText('" + bbox + "',0)))\n"

            //orderBy = ['numberOfCoveringAnnotation':'asc','id':'asc']
            orderBy = ['id':'desc']
            //addExtraColumn("numberOfCoveringAnnotation",subRequest)
        }
    }
    def getNotReviewedOnlyConst() {
        return ""
    }
    def createOrderBy() {
        if(kmeansValue<3) return ""
        if(!orderBy) {
            return "ORDER BY a.id desc " + (columnToPrint.contains("term")? ", term " : "")
        } else {
            return "ORDER BY " + orderBy.collect{it.key + " " + it.value}.join(", ")
        }
     }


}