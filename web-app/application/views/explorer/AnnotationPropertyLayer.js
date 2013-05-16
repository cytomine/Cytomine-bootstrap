var AnnotationPropertyLayer = function (imageID, userID, browseImageView, key) {

    var self = this;
    this.idImage = imageID;
    this.idUser = userID;

    this.browseImageView = browseImageView;
    this.map = browseImageView.map;
    this.vectorLayer = null;
    this.key = key;

    this.styleMap = new OpenLayers.StyleMap({'default':{
        label : "${value}",
        fontColor: "red",
        fontSize: "13px",
        fontWeight: "bold",
    }});

    this.vectorLayer = new OpenLayers.Layer.Vector("annotationPropertyValue", {
        styleMap : self.styleMap,
        strategies: [
            new OpenLayers.Strategy.BBOX({resFactor: 1})
        ],
        protocol: new OpenLayers.Protocol.Script({
            url: new AnnotationPropertyTextCollection({idUser: this.idUser, idImage: this.idImage, key: this.key}).url().replace("json", "jsonp"),
            format: new OpenLayers.Format.AnnotationProperty({annotationPropertyLayer: this}),
            callbackKey: "callback"
        })
    });

    this.addToMap = function() {
        this.map.addLayer(this.vectorLayer);
    };

    this.setZIndex = function(index) {
        this.vectorLayer.setZIndex( index );
    }

    this.removeFromMap = function() {
        this.map.removeLayer(this.vectorLayer);
    };
}

OpenLayers.Format.AnnotationProperty = OpenLayers.Class(OpenLayers.Format, {
    read: function (collection) {
        var nestedCollection = collection.collection;
        var features = [];
        var self = this;

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

