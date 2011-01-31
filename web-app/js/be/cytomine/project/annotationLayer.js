Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Project');
Ext.namespace('Cytomine.Project.Annotation');
//Ext.namespace('Cytomine.Project.Annotation.Layer');

Cytomine.Project.AnnotationLayer = function(name, scanID) {
    this.name = name;
    this.scanID = scanID;
}

Cytomine.Project.AnnotationLayer.prototype = {
    // Name of the layer
    name : null,

    // Id of the scan
    scanID : null,

    //The OpenLayers.Layer.Vector on which we draw annotations
    vectorsDrawAnnotations : null,

    // Request object to call server
    req : null,

    /*Load layer for annotation*/
    loadToMap : function (map) {
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
    },

    /*Load annotation from database on layer */
    loadAnnotations : function (map) {
        req = new XMLHttpRequest();
        req.open("GET", "/cytomine-web/api/annotation/scan/"+this.scanID+".json", true);
        req.onreadystatechange = this.decodeAnnotations;   // the handler
        req.send(null);
        map.addLayer(vectorsDrawAnnotations);
    },

    /*Add annotation in database*/
    addAnnotation : function (feature) {
        console.log("addAnnotation start");
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("add geomwkt="+geomwkt);

        req = new XMLHttpRequest();
        console.log("/cytomine-web/api/annotation/scan/"+this.scanID+"/"+geomwkt);
        req.open("POST", "/cytomine-web/api/annotation/scan/"+this.scanID+"/"+geomwkt+".json", true);
        req.onreadystatechange = this.decodeNewAnnotation;
        req.send(null);
        //Annotation hasn't any id => -1
        //feature.attributes = {idAnnotation: "-1"};
        this.hideAnnotation(feature);
        //feature.remove();
        console.log("onFeatureAdded end");
    },
    hideAnnotation : function(feature) {
        vectorsDrawAnnotations.removeFeatures([feature]);
    },

    /*Remove annotation from database*/
    removeAnnotation : function(feature) {

        console.log("deleteAnnotation start");
        console.log("delete " + "" + feature.attributes.idAnnotation);

        req = new XMLHttpRequest();
        req.open("DELETE", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation, true);
        req.send(null);
        this.hideAnnotation(feature);
        console.log("deleteAnnotation end");
    },

    /*Modifiy annotation on database*/
    updateAnnotation : function(feature)
    {

        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("update geomwkt="+geomwkt + " " + feature.attributes.idAnnotation);

        req = new XMLHttpRequest();
        req.open("PUT", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation+"/"+geomwkt, true);
        req.send(null);

        console.log("onFeatureUpdate end");

    },

    /*Read a list of annotations from server response and add to the layer*/
    decodeAnnotations : function() {
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

    },

    /*Decode a single annotation from server response and add to the layer*/
    decodeNewAnnotation : function() {
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













