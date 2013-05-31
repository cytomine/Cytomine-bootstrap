package be.cytomine.sql

import be.cytomine.api.UrlApi
import groovy.sql.Sql

import java.sql.ResultSet
import java.sql.ResultSetMetaData

/**
 * User: lrollus
 * Date: 31/05/13
 * GIGA-ULg
 *
 */
class AnnotationListing {

   def columnToPrint
   def project
   def users
   def usersForTerm
   def images
   def notReviewedOnly
   def noTerm
   def multipleTerm

    static def availableColumn =
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
                    geometryCompression : 'a.geometry_compression'

            ],
            wkt : [location:'a.wkt_location'],
            gis : [area: 'ST_area(a.location)',perimeter:'ST_perimeter(a.location)',x:'ST_X(ST_centroid(a.location))',y:'ST_Y(ST_centroid(a.location))'],
            term : [term : 'at.term_id', annotationTerms : 'at.id', userTerm: 'at.user_id'],
            url : [cropURL : '#cropURL', smallCropURL : '#smallCropURL', url: '#url', imageURL : '#imageURL'],


        ]


    static def getAllPropertiesName(List groups = null) {

        def groupFilter = []
        if(!groups) {
            availableColumn.each { group ->
                groupFilter << group
            }
        } else {
            availableColumn.each { group ->
                if(groups.contains(group.key.toString())) {
                    groupFilter << group
                }
            }
        }


        def propNames = []

        groupFilter.each { group ->
            group.value.each { assoc ->
                assoc.each {
                    propNames << it.key
                }
            }
        }
        propNames
    }

    //parentIdent : 'a.parent_ident',
    //user -> user_job_id?
    //algo rate





    def buildColumnToPrint() {
        if(!columnToPrint) {
            columnToPrint = availableColumn.collect{it.key}
        }

        def columns = []
        println "columnToPrint=$columnToPrint"

        availableColumn.each {
            println "group=${it.key}"
           if(columnToPrint.contains(it.key)) {
               println "OK"
               it.value.each { columnAssoc ->
                   columns << columnAssoc
               }
           }
        }
        println "columns=${columns}"
        return columns
    }




    def generateSQLSelect(def columns) {
        def requestHeadList = []
        columns.each {
            requestHeadList << it.value + " as " + it.key
        }
        String requestHead = "SELECT " +  requestHeadList.join(', ')  + " \n"
        requestHead
    }


    def getAnnotationsRequest() {

        def columns = buildColumnToPrint()
        def sqlColumns = []
        def postComputedColumns = []

        columns.each {
            if(!it.value.startsWith("#"))
                sqlColumns << it
            else
                postComputedColumns <<  it
        }

        String request = generateSQLSelect(sqlColumns) +
                getTables() +
                getProjectConst() +
                getUsersConst() +
                getImagesConst() +
                getNotReviewedOnlyConst() +
                getUsersForTermConst() +
                getOrderBy()
        return  request

    }

    def getOrderBy() {
       return "ORDER BY id desc " + (columnToPrint.contains("term")? ", term " : "")
    }

    def getTables() {
         if(multipleTerm) {
            return "FROM user_annotation a, annotation_term at, annotation_term at2 " +
                    "WHERE a.id = at.user_annotation_id\n" +
                    " AND a.id = at2.user_annotation_id\n" +
                    " AND at.id <> at2.id \n" +
                    " AND at.term_id <> at2.term_id \n"
         } else if(noTerm) {
             return "FROM user_annotation a LEFT JOIN (SELECT * from annotation_term x where x.user_id IN (" + users.join(",") + ")) at ON a.id = at.user_annotation_id " +
                     "WHERE at.id IS NULL \n"


         } else if(columnToPrint.contains('term')){
            return "FROM user_annotation a LEFT OUTER JOIN annotation_term at ON a.id = at.user_annotation_id WHERE true "
        }  else {
             return "FROM user_annotation a WHERE true "
         }
    }

    def getProjectConst() {
        return (project? "AND a.project_id = $project\n" : "")
    }

    def getUsersConst() {
        return (users? "AND a.user_id IN (${users.join(",")})\n" : "")
    }

    def getUsersForTermConst() {
        return (usersForTerm? "AND at.user_id IN (${usersForTerm.join(",")})\n" : "")
    }

    def getImagesConst() {
        return (images? "AND a.image_id IN (${images.join(",")})\n" : "")
    }

    def getNotReviewedOnlyConst() {
        return (notReviewedOnly? "AND a.count_reviewed_annotations=0\n" : "" )
    }


}
