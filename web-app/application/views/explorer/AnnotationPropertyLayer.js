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
        fontColor: "green",
        fontSize: "48pt",
        fontWeight: "bold"
    }});

    this.vectorLayer = new OpenLayers.Layer.Vector("annotationPropertyValue", {
        styleMap : self.styleMap,
        onFeatureInsert: function(	feature	) {$("text > tspan").attr("font-size","30px")}, //="48pt"
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
    };

    this.removeFromMap = function() {
        this.map.removeLayer(this.vectorLayer);
    };
}

OpenLayers.Format.AnnotationProperty = OpenLayers.Class(OpenLayers.Format, {
    read: function (collection) {
        var nestedCollection = collection.collection;

        var self = this;

        var featuresMap = {}

        _.each(nestedCollection, function (result) {
            var samePointValue = featuresMap[result.x + "_" + result.y];
            if(samePointValue) {
                featuresMap[result.x + "_" + result.y] = samePointValue + " ; " + result.value
            } else {
                featuresMap[result.x + "_" + result.y] = result.value
            }
        });
        console.log("featuresMap");
        console.log(featuresMap);

        var features = [];

              for (var prop in featuresMap) {
                 // important check that this is objects own property
                 // not from prototype prop inherited
                 if(featuresMap.hasOwnProperty(prop)){
                     var x = prop.split("_")[0];
                     var y = prop.split("_")[1];
                     var value = featuresMap[prop];
                     var format = new OpenLayers.Format.WKT();
                     var geom = "POINT("+x+" " + (y+100)+")";
                     var pointFeature = new OpenLayers.Feature.Vector(format.read(geom).geometry);
                     pointFeature.attributes = { value: value};
                     features.push(pointFeature);
                 }
              }
        console.log("features");
        console.log(features);



        return features;




    }
});

