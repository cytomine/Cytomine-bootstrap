Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Project');

Cytomine.Project.Scan = function (urls, scanID, filename, path, metadataUrl) {
    this.urls = urls;
    this.scanID = scanID;
    this.filename = filename;
    this.path = path;
    this.metadataUrl = metadataUrl;
}


Cytomine.Project.Scan.prototype = {
    urls : null,
    imageID : null,
    filename : null,
    path : null,
    map : null,
    initMap : function () {
        //clear previous overview Map
        //document.getElementById("overviewMap"+this.scanID).innerHTML="";
        console.log("metadataURl" + this.metadataUrl);
        console.log("filename" + this.filename);
        console.log("urls" + this.urls);
        console.log("path" + this.path);
        var openURLLayer = new OpenLayers.Layer.OpenURL( this.filename, this.urls, {transitionEffect: 'resize', layername: 'basic', format:'image/jpeg', rft_id: this.path, metadataUrl: this.metadataUrl} );
        console.log("openURLLayer.viewerLevel " + openURLLayer.getViewerLevel());
        var metadata = openURLLayer.getImageMetadata();
        var resolutions = openURLLayer.getResolutions();
        var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
        var tileSize = openURLLayer.getTileSize();
        var lon = metadata.width / 2;
        var lat = metadata.height / 2;
        var overviewBounds = new OpenLayers.Bounds(0,0,metadata.width, metadata.height);
        var mapOptions = {
            maxExtent: overviewBounds,
            maximized : true
        };
        var options = {resolutions: resolutions, maxExtent: maxExtent, tileSize: tileSize, controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.LayerSwitcher({'ascending':false}),
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.OverviewMap({
                div : $('overviewMap'+this.scanID),
                size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                minRatio : 1,
                maxRatio : 1024,
                mapOptions: mapOptions}),
            new OpenLayers.Control.KeyboardDefaults()
        ]};
        this.map = new OpenLayers.Map("map"+this.scanID, options);
        this.map.addLayer(openURLLayer);
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);
    },
    initTools : function (controls) {
        for(var key in controls) {
            this.map.addControl(controls[key]);
        }
    }
}