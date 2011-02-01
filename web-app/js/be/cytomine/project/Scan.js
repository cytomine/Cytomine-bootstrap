Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Project');

Cytomine.Project.Scan = function (urls, scanID, filename, path) {
    this.urls = urls;
    this.scanID = scanID;
    this.filename = filename;
    this.path = path;
    this.initMap();
}


Cytomine.Project.Scan.prototype = {
    urls : null,
    scanID : null,
    filename : null,
    path : null,
    map : null,
    getMetaDataURL : function () {
        return "/cytomine-web/api/image/metadata/" + this.scanID;
    },
    initMap : function () {
        var openURLLayer = new OpenLayers.Layer.OpenURL( this.filename, this.urls, {transitionEffect: 'resize', layername: 'basic', format:'image/jpeg', rft_id: this.path, metadataUrl: this.getMetaDataURL()} );
        var metadata = openURLLayer.getImageMetadata();
        var resolutions = openURLLayer.getResolutions();
        var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
        var tileSize = openURLLayer.getTileSize();
        var options = {resolutions: resolutions, maxExtent: maxExtent, tileSize: tileSize, controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.LayerSwitcher({'ascending':false}),
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.OverviewMap(),
            new OpenLayers.Control.KeyboardDefaults()
        ]};
        this.map = new OpenLayers.Map("map"+this.scanID, options);
        this.map.addLayer(openURLLayer);

        var lon = metadata.width / 2;
        var lat = metadata.height / 2;
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 0);
    },
    initTools : function (controls) {
        for(var key in controls) {
            this.map.addControl(controls[key]);
        }

    }
}