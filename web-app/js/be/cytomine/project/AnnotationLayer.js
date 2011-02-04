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
    vectorsLayer : null,

    // Request object to call server
    req : null,

    controls : null,
    /*Load layer for annotation*/
    loadToMap : function (scan) {
        vectorsLayer = new OpenLayers.Layer.Vector("Vector Layer");
        var alias = this;
        vectorsLayer.events.on({
            'featureadded': function (evt) {
                console.log("onFeatureAdded start:"+evt.feature.attributes.idAnnotation);
                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if(evt.feature.attributes.listener!='NO')
                {
                    console.log("add " + evt.feature);
                    alias.addAnnotation(evt.feature);
                }
            },
            'beforefeaturemodified': function(evt) {
                console.log("Selected " + evt.feature.id  + " for modification");
            },
            'afterfeaturemodified': function (evt) {
                console.log("onFeatureUpdate start");
                alias.updateAnnotation(evt.feature);
            },
            'onDelete': function(feature) {
                console.log("delete " + feature.id);
            }
        });
        vectorsLayer.events.register("featureselected", vectorsLayer, selected);
        controls = {
            point: new OpenLayers.Control.DrawFeature(vectorsLayer,
                    OpenLayers.Handler.Point),
            line: new OpenLayers.Control.DrawFeature(vectorsLayer,
                    OpenLayers.Handler.Path),
            polygon: new OpenLayers.Control.DrawFeature(vectorsLayer,
                    OpenLayers.Handler.Polygon),
            regular: new OpenLayers.Control.DrawFeature(vectorsLayer,
                    OpenLayers.Handler.RegularPolygon, {handlerOptions: {sides: 5}}),
            modify: new OpenLayers.Control.ModifyFeature(vectorsLayer)
        }
        scan.initTools(controls);
        scan.map.addLayer(vectorsLayer);
    },

    /*Load annotation from database on layer */
    loadAnnotations : function (scan) {
        req = new XMLHttpRequest();
        req.open("GET", "/cytomine-web/api/annotation/scan/"+this.scanID+".json", true);
        req.onreadystatechange = this.decodeAnnotations;   // the handler
        req.send(null);
        scan.map.addLayer(vectorsLayer);
    },

    /*Add annotation in database*/
    addAnnotation : function (feature) {
        console.log("addAnnotation start");
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("add geomwkt="+geomwkt);

        req = new XMLHttpRequest();
        //console.log("/cytomine-web/api/annotation/scan/"+this.scanID+"/"+geomwkt);
        //req.open("POST", "/cytomine-web/api/annotation/scan/"+this.scanID+"/"+geomwkt+".json", true);
        req.open("POST", "/cytomine-web/api/annotation.json", true);
        req.onreadystatechange = this.decodeNewAnnotation;

        var json = {annotation: {"class":"be.cytomine.project.Annotation",name:"test",location:geomwkt,scan:this.scanID}}; //class is a reserved word in JS !

        req.send(JSON.stringify(json));
        //Annotation hasn't any id => -1
        //feature.attributes = {idAnnotation: "-1"};
        this.hideAnnotation(feature);
        //feature.remove();
        console.log("onFeatureAdded end");
    },
    hideAnnotation : function(feature) {
        vectorsLayer.removeFeatures([feature]);
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
        req.open("PUT", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation+".json", true);

        var json = {annotation: {"id":feature.attributes.idAnnotation,"class":"be.cytomine.project.Annotation",name:"test",location:geomwkt,scan:this.scanID}}; //class is a reserved word in JS !

        req.send(JSON.stringify(json));

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
            console.log(JSONannotations);

            for (i=0;i<JSONannotations.length;i++)
            {
                console.log(JSONannotations[i].id);
                //read from wkt to geometry
                var point =  (format.read(JSONannotations[i].location));
                var geom = point.geometry;

                var feature = new OpenLayers.Feature.Vector(
                        geom,
                {some:'data'},
                {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

                feature.attributes = {idAnnotation: JSONannotations[i].id, listener:'NO',importance: 10 };

                vectorsLayer.addFeatures(feature);
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
            console.log(JSONannotations.annotation);

            console.log(JSONannotations.annotation.id);
            //read from wkt to geometry
            var point =  (format.read(JSONannotations.annotation.location));
            var geom = point.geometry;

            var feature = new OpenLayers.Feature.Vector(
                    geom,
            {some:'data'},
            {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

            feature.attributes = {idAnnotation: JSONannotations.annotation.id, listener:'NO',importance: 10 };

            vectorsLayer.addFeatures(feature);

        }

    },
    /** Triggered when add new feature **/
    /*onFeatureAdded : function (evt) {
     console.log("onFeatureAdded start:"+evt.feature.attributes.idAnnotation);
     // Check if feature must throw a listener when it is added
     // true: annotation already in database (no new insert!)
     // false: new annotation that just have been draw (need insert)
     //
     if(evt.feature.attributes.listener!='NO')
     {
     console.log("add " + evt.feature);
     alias.addAnnotation(evt.feature);
     }
     },*/

    /** Triggered when update feature **/
    /* onFeatureUpdate : function (evt) {
     console.log("onFeatureUpdate start");

     this.updateAnnotation(evt.feature);
     },*/
    update : function() {
        console.log("update")
        // reset modification mode
        controls.modify.mode = OpenLayers.Control.ModifyFeature.RESHAPE;
        var rotate = document.getElementById("rotate").checked;
        if(rotate) {
            controls.modify.mode |= OpenLayers.Control.ModifyFeature.ROTATE;
        }
        var resize = document.getElementById("resize").checked;
        if(resize) {
            controls.modify.mode |= OpenLayers.Control.ModifyFeature.RESIZE;
            var keepAspectRatio = document.getElementById("keepAspectRatio").checked;
            if (keepAspectRatio) {
                controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
            }
        }
        var drag = document.getElementById("drag").checked;
        if(drag) {
            controls.modify.mode |= OpenLayers.Control.ModifyFeature.DRAG;
        }
        if (rotate || drag) {
            controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
        }
        var sides = parseInt(document.getElementById("sides").value);
        sides = Math.max(3, isNaN(sides) ? 0 : sides);
        controls.regular.handler.sides = sides;
        var irregular =  document.getElementById("irregular").checked;
        controls.regular.handler.irregular = irregular;
    },
    toggleControl :
       function (element) {
           console.log("toggleControl")
           for(key in controls) {
               var control = controls[key];
               if(element.value == key && element.checked) {
                   control.activate();
               } else {
                   control.deactivate();
               }
           }
       }
}













