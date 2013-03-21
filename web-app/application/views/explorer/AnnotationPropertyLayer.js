var AnnotationPropertyLayer = function (imageID, userID, browseImageView, map) {

    var self = this;
    this.idImage = imageID;
    this.idUser = userID;
   // this.annotationsPropertiesCollection = null; //new AnnotationPropertyTextCollection({idUser: this.idUser, idImage: this.idImage, key: "Key1"}).url().replace("json", "jsonp");


    this.annotationsPropertiesCollection = new AnnotationPropertyTextCollection({idUser: this.idUser, idImage: this.idImage, key: 'null'}).url().replace("json", "jsonp")

    this.browseImageView = browseImageView;
    this.map = map;
    this.vectorLayer = null;

    var styleMap = new OpenLayers.StyleMap({'default':{
        strokeColor: "#00FF00",
        fillColor: "#00FF00",
        //pointRadius: 10,
        label : "value: ${value}",

        fontColor: "red",
        fontSize: "14px",
        fontFamily: "Courier New, monospace",
        fontWeight: "bold",

        graphicZIndex : 15
    }});

    this.vectorLayer = new OpenLayers.Layer.Vector("AnnotationPropertyValue", {
        styleMap : styleMap,
        strategies: [
            new OpenLayers.Strategy.BBOX({resFactor: 1})
        ],
        protocol: new OpenLayers.Protocol.Script({
            url: this.annotationsPropertiesCollection,
            format: new OpenLayers.Format.AnnotationProperty({annotationPropertyLayer: this}),
            callbackKey: "callback"
        })
    });

    this.map.addLayer(this.vectorLayer);
}

OpenLayers.Format.AnnotationProperty = OpenLayers.Class(OpenLayers.Format, {
    read: function (collection) {
        var nestedCollection = collection.collection;
        var features = [];

        _.each(nestedCollection, function (result) {

            var format = new OpenLayers.Format.WKT();
            var point = format.read(result.centre);
            var pointFeature = new OpenLayers.Feature.Vector(point.geometry);
            pointFeature.attributes = { value: result.value};

            features.push(pointFeature);
        });
        return features;
    }
});

AnnotationPropertyLayer.prototype = {

    loadAnnotationProperty : function (key) {

        //Mettre le message au dessus de tout.
        var self = this;
        var layers = this.map.layers;
        _.each(layers,function(layer) {
            layer.setZIndex( 1000 )
        });
        var vecLyr = this.map.getLayersByName('AnnotationPropertyValue')[0];
        vecLyr.setZIndex( 5000 );


        console.log("AnnotationPropertyLayer.vectorLayer : " + this.vectorLayer);
        if (key == "selectedEmpty") {
            this.vectorLayer.removeAllFeatures();
        } else {
            var url = new AnnotationPropertyTextCollection({idUser: this.idUser, idImage: this.idImage, key: key}).url().replace("json", "jsonp");
            console.log("new url" + url);
            this.vectorLayer.refresh({ url : url});
        }
    }
};