/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 28/01/11
 * Time: 9:03
 * To change this template use File | Settings | File Templates.
 */

function LayerAnnotation (name,scanid) {
    /* Name of the layer */
    this.name=name;
    /* Id of the scan */
    this.scanid=scanid;
    /* Openlayers Layer object*/
    this.vectorsDrawAnnotations;
    /* Request object to call server*/
    this.req;

    /*Load layer for annotation*/
    this.loadToMap= function(map) {
        vectorsDrawAnnotations = new OpenLayers.Layer.Vector("Vector Layer");
        vectorsDrawAnnotations.events.on({
            'featureadded': onFeatureAdded,
            'beforefeaturemodified': function(evt) {
                console.log("Selected " + evt.feature.id  + " for modification");
            },
            'afterfeaturemodified': onFeatureUpdate,
            'onDelete': function(feature) {
                console.log("delete " + feature.id);
            }
        });
        vectorsDrawAnnotations.events.register("featureselected", vectorsDrawAnnotations, selected);
        map.addLayer(vectorsDrawAnnotations);
    }

    /*Load annotation from database on layer */
    this.loadAnnotations=function(map) {
        req = new XMLHttpRequest();
        req.open("GET", "/cytomine-web/api/annotation/scan/"+this.scanid+".json", true);
        req.onreadystatechange = this.decodeAnnotations;   // the handler
        req.send(null);
        map.addLayer(vectorsDrawAnnotations);

    }

    /*Add annotation in database*/
    this.addAnnotation = function(feature)
    {
        console.log("addAnnotation start");
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("add geomwkt="+geomwkt);

        req = new XMLHttpRequest();
        console.log("/cytomine-web/api/annotation/scan/"+this.scanid+"/"+geomwkt);
        req.open("POST", "/cytomine-web/api/annotation/scan/"+this.scanid+"/"+geomwkt+".json", true);
        req.onreadystatechange = this.decodeNewAnnotation;
        req.send(null);
        //Annotation hasn't any id => -1
        //feature.attributes = {idAnnotation: "-1"};
        this.hideAnnotation(feature);
        //feature.remove();
        console.log("onFeatureAdded end");
    }

    this.hideAnnotation = function(feature) {
        vectorsDrawAnnotations.removeFeatures([feature]);
    }
    /*Remove annotation from database*/
    this.removeAnnotation = function(feature) {

        console.log("deleteAnnotation start");
        console.log("delete " + "" + feature.attributes.idAnnotation);

        req = new XMLHttpRequest();
        req.open("DELETE", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation, true);
        req.send(null);
        this.hideAnnotation(feature);
        console.log("deleteAnnotation end");
    }

    /*Modifiy annotation on database*/
    this.updateAnnotation = function(feature)
    {

        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("update geomwkt="+geomwkt + " " + feature.attributes.idAnnotation);

        req = new XMLHttpRequest();
        req.open("PUT", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation+"/"+geomwkt, true);
        req.send(null);

        console.log("onFeatureUpdate end");

    }

    /*Read a list of annotations from server response and add to the layer*/
    this.decodeAnnotations = function()
    {
        var format = new OpenLayers.Format.WKT();
        //vectorsAnnotations = new OpenLayers.Layer.Vector("Overlay");
        var points = [];
        console.log("decodeAnnotations:"+req.readyState);

        if (req.readyState == 4)
        {
            //eval json
            var JSONannotations = eval('(' + req.responseText + ')');
            console.log(JSONannotations.annotations);

            for (i=0;i<JSONannotations.annotations.length;i++)
            {
                console.log(JSONannotations.annotations[i].id);
                //read from wkt to geometry
                var point =  (format.read(JSONannotations.annotations[i].location));
                var geom = point.geometry;

                var feature = new OpenLayers.Feature.Vector(
                        geom,
                {some:'data'},
                {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

                feature.attributes = {idAnnotation: JSONannotations.annotations[i].id, listener:'NO',importance: 10 };

                vectorsDrawAnnotations.addFeatures(feature);
            }

        }

    }

    /*Decode a single annotation from server response and add to the layer*/
    this.decodeNewAnnotation = function()
    {
        var format = new OpenLayers.Format.WKT();
        var points = [];
        console.log("decodeNewAnnotation:"+req.readyState);
        if (req.readyState == 4)
        {
            //eval json
            console.log("response:"+req.responseText);
            var JSONannotations = eval('(' + req.responseText + ')');
            console.log(JSONannotations.annotations);

            for (i=0;i<JSONannotations.annotations.length;i++)
            {
                console.log(JSONannotations.annotations[i].id);
                //read from wkt to geometry
                var point =  (format.read(JSONannotations.annotations[i].location));
                var geom = point.geometry;

                var feature = new OpenLayers.Feature.Vector(
                        geom,
                {some:'data'},
                {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

                feature.attributes = {idAnnotation: JSONannotations.annotations[i].id, listener:'NO',importance: 10 };

                vectorsDrawAnnotations.addFeatures(feature);
            }

        }

    }

}




