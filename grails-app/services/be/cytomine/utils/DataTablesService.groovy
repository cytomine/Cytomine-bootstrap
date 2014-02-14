package be.cytomine.utils

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import org.hibernate.FetchMode

class DataTablesService {

//&iColumns=14&mDataProp_0=id&mDataProp_1=baseImage.macroURL&mDataProp_2=baseImage.originalFilename&mDataProp_3=baseImage.width&mDataProp_4=baseImage.height&mDataProp_5=baseImage.magnification&mDataProp_6=baseImage.resolution&mDataProp_7=countImageAnnotations&mDataProp_8=countImageJobAnnotations&mDataProp_9=countImageReviewedAnnotations&mDataProp_10=baseImage.mime.extension&mDataProp_11=created&mDataProp_12=reviewStart&mDataProp_13=updated&sSearch=&bRegex=false&sSearch_0=&bRegex_0=false&bSearchable_0=false&sSearch_1=&bRegex_1=false&bSearchable_1=false&sSearch_2=&bRegex_2=false&bSearchable_2=true&sSearch_3=&bRegex_3=false&bSearchable_3=false&sSearch_4=&bRegex_4=false&bSearchable_4=false&sSearch_5=&bRegex_5=false&bSearchable_5=false&sSearch_6=&bRegex_6=false&bSearchable_6=false&sSearch_7=&bRegex_7=false&bSearchable_7=false&sSearch_8=&bRegex_8=false&bSearchable_8=false&sSearch_9=&bRegex_9=false&bSearchable_9=false&sSearch_10=&bRegex_10=false&bSearchable_10=true&sSearch_11=&bRegex_11=false&bSearchable_11=false&sSearch_12=&bRegex_12=false&bSearchable_12=false&sSearch_13=&bRegex_13=false&bSearchable_13=false&iSortCol_0=5&sSortDir_0=asc&iSortingCols=1&bSortable_0=true&bSortable_1=true&bSortable_2=true&bSortable_3=true&bSortable_4=true&bSortable_5=true&bSortable_6=true&bSortable_7=true&bSortable_8=true&bSortable_9=true&bSortable_10=true&bSortable_11=true&bSortable_12=true&bSortable_13=true&datatables=true&_=1392363731906
    //TODO: document this methods + params

    def process(params, domain, restrictions, returnFields, project) {
        println "process"
        params.max = params.iDisplayLength ? params.iDisplayLength as int : 10;
        params.offset = params.iDisplayStart ? params.iDisplayStart as int : 0;
        def dataToRender = [:]
//        dataToRender.sEcho = params.sEcho
//        dataToRender.aaData

        String abstractImageAlias = "ai"
        String _search = params.sSearch ? "%"+params.sSearch+"%" : "%"

        List<ImageInstance> images = ImageInstance.createCriteria().list() {
            createAlias("baseImage", abstractImageAlias)
            eq("project", project)
            isNull("parent")
            fetchMode 'baseImage', FetchMode.JOIN
            ilike(abstractImageAlias + ".originalFilename", _search)
        }
        //dataToRender.aaData = images
        return images
    }


    // TODO: document this methods + params
    def process2(params, domain, restrictions, returnFields) {
        def filters = []


        def query
        if(restrictions == "") {
            query = new StringBuilder("from ${domain.name} as t");
            if(params.sSearch) {
                query.append(" WHERE ${filter}");
            }
            dataToRender.iTotalRecords = domain.count();
        } else {
            query = new StringBuilder("from ${domain.name} WHERE ${restrictions}");
            if(params.sSearch) {
                query.append(" AND (${filter})");
            }
            dataToRender.iTotalRecords = domain.executeQuery("SELECT COUNT(*) FROM ${domain.name} WHERE ${restrictions}")[0]
        }

        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords;

        def sortDir = params.sSortDir_0?.equalsIgnoreCase('asc') ? 'asc' : 'desc';
        int sortCol = (params?.iSortCol_0)?((params.iSortCol_0.isNumber())?params.iSortCol_0.toInteger():0):0;
        def sortProperty = params['mDataProp_' + sortCol];
        query.append(" ORDER BY ${sortProperty} ${sortDir}");

        def rows = [];

        if ( params.sSearch ) {
            def countQuery
            if(restrictions == "") {
                countQuery = new StringBuilder("SELECT COUNT(*) FROM ${domain.name} WHERE " )
            } else {
                countQuery = new StringBuilder("SELECT COUNT(*) FROM ${domain.name} WHERE ${restrictions} AND" )
            }

            countQuery.append(" (${filter})")
            println "{params.sSearch} " +params.sSearch
            def result = domain.executeQuery(countQuery.toString(), [filter: "%${params.sSearch}%"])

            if ( result ) {
                dataToRender.iTotalDisplayRecords = result[0]
            }
            rows = domain.findAll(
                    query.toString(),
                    [filter: "%${params.sSearch}%"],
                    [max: params.iDisplayLength as int, offset: params.iDisplayStart as int])
        } else {

            int ma = params.iDisplayLength ? params.iDisplayLength as int : 10;
            int off = params.iDisplayStart ? params.iDisplayStart as int : 0;

            rows = domain.findAll(query.toString(), [max: ma ,  offset: off])
        }

        rows?.each { row ->
            def returnRow = [:]
            for(int i=0; i<params.int("iColumns"); i++) {

                if(returnFields[params['mDataProp_' + i]]) { //Custom
                    returnRow[params['mDataProp_' + i]] = Eval.me('row', row, returnFields[params['mDataProp_' + i]]).replace(".", "__")
                } else {
                   returnRow[params['mDataProp_' + i]] = Eval.me('row', row, 'row.' + params['mDataProp_' + i].replaceAll("->", "."))
                }
            }

            dataToRender.aaData << returnRow
        }
        return dataToRender;

    }

//    // TODO: document this methods + params
    //TOO SLOW
//    def process2(params, domain, restrictions, returnFields) {
//        def filters = []
//        for(int i=0; i<params.int("iColumns"); i++) {
//            if(params['bSearchable_' + i] == "true") {
//                filters << "${params['mDataProp_' + i].replaceAll("->", ".")} like :filter"
//            }
//        }
//
//        def filter = filters.join(" OR ")
//
//        def dataToRender = [:];
//        dataToRender.sEcho = params.sEcho;
//        dataToRender.aaData=[];
//
//        def query
//        if(restrictions == "") {
//            query = new StringBuilder("from ${domain.name} as t");
//            if(params.sSearch) {
//                query.append(" WHERE ${filter}");
//            }
//            dataToRender.iTotalRecords = domain.count();
//        } else {
//            query = new StringBuilder("from ${domain.name} WHERE ${restrictions}");
//            if(params.sSearch) {
//                query.append(" AND (${filter})");
//            }
//            dataToRender.iTotalRecords = domain.executeQuery("SELECT COUNT(*) FROM ${domain.name} WHERE ${restrictions}")[0]
//        }
//
//        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords;
//
//        def sortDir = params.sSortDir_0?.equalsIgnoreCase('asc') ? 'asc' : 'desc';
//        int sortCol = (params?.iSortCol_0)?((params.iSortCol_0.isNumber())?params.iSortCol_0.toInteger():0):0;
//        def sortProperty = params['mDataProp_' + sortCol];
//        query.append(" ORDER BY ${sortProperty} ${sortDir}");
//
//        def rows = [];
//
//        if ( params.sSearch ) {
//            def countQuery
//            if(restrictions == "") {
//                countQuery = new StringBuilder("SELECT COUNT(*) FROM ${domain.name} WHERE " )
//            } else {
//                countQuery = new StringBuilder("SELECT COUNT(*) FROM ${domain.name} WHERE ${restrictions} AND" )
//            }
//
//            countQuery.append(" (${filter})")
//            println "{params.sSearch} " +params.sSearch
//            def result = domain.executeQuery(countQuery.toString(), [filter: "%${params.sSearch}%"])
//
//            if ( result ) {
//                dataToRender.iTotalDisplayRecords = result[0]
//            }
//            rows = domain.findAll(
//                    query.toString(),
//                    [filter: "%${params.sSearch}%"],
//                    [max: params.iDisplayLength as int, offset: params.iDisplayStart as int])
//        } else {
//
//            int ma = params.iDisplayLength ? params.iDisplayLength as int : 10;
//            int off = params.iDisplayStart ? params.iDisplayStart as int : 0;
//
//            rows = domain.findAll(query.toString(), [max: ma ,  offset: off])
//        }
//
//        rows?.each { row ->
//            def returnRow = [:]
//            for(int i=0; i<params.int("iColumns"); i++) {
//
//                if(returnFields[params['mDataProp_' + i]]) { //Custom
//                    returnRow[params['mDataProp_' + i]] = Eval.me('row', row, returnFields[params['mDataProp_' + i]]).replace(".", "__")
//                } else {
//                    returnRow[params['mDataProp_' + i]] = Eval.me('row', row, 'row.' + params['mDataProp_' + i].replaceAll("->", "."))
//                }
//            }
//
//            dataToRender.aaData << returnRow
//        }
//        return dataToRender;
//
//    }

}
