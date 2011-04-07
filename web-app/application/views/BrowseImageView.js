var BrowseImageView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {

    },
    render: function() {
        var tpl = ich.browseimagetpl(this.model.toJSON(), true);
        $(this.el).append(tpl);
        var tabs = $(this.el).children('.tabs');
        console.log(this.model.get('filename'));
        this.el.tabs("add","#tabs-"+this.model.get('id'),this.model.get('filename'));
        this.el.css("display","block");
        this.initMap();
        this.initSideBar();
        return this;
    },
    getUserLayer : function () {
        /*console.log("-------this.userLayer is : " + this.imageID);
         return this.userLayer;*/
    },
    initMap : function () {
        var openURLLayer = new OpenLayers.Layer.OpenURL( this.model.get('filename'), this.model.get('imageServerBaseURL'), {transitionEffect: 'resize', layername: 'basic', format:'image/jpeg', rft_id: this.model.get('path'), metadataUrl: this.model.get('metadataUrl')} );
        var metadata = openURLLayer.getImageMetadata();
        var resolutions = openURLLayer.getResolutions();
        var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
        var tileSize = openURLLayer.getTileSize();
        var lon = metadata.width / 2;
        var lat = metadata.height / 2;
        var mapOptions = {
            maxExtent: maxExtent,
            maximized : true
        };
        var options = {resolutions: resolutions, maxExtent: maxExtent, tileSize: tileSize, controls: [
            //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.PanZoomBar(),
            //new OpenLayers.Control.LayerSwitcher({'ascending':false}),
            new OpenLayers.Control.LayerSwitcher({roundedCorner:false,roundedCornerColor: false,'div' :  document.getElementById('layerSwitcher'+this.model.get('id'))}),
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.OverviewMap({
                div : document.getElementById('overviewMap'+this.model.get('id')),
                //size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                minRatio : 1,
                maxRatio : 1024,
                mapOptions: mapOptions}),
            new OpenLayers.Control.KeyboardDefaults()
        ]};
        this.map = new OpenLayers.Map("map"+this.model.get('id'), options);
        this.map.addLayer(openURLLayer);
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);
    },
    initSideBar : function () {
        var toolbar = $('#toolbar' + this.model.get('id'));
        toolbar.find('input[name=select]').button({
            text : false,
            icons: {
                primary: "ui-icon-seek-start"
            }
        });
        toolbar.find('button[name=delete]').button({
            text: false,
            icons: {
                primary: "ui-icon-trash"

            }
        });
        toolbar.find('input[name=rotate]').button();
        toolbar.find('input[name=resize]').button();
        toolbar.find('input[name=drag]').button();
        toolbar.find('input[name=irregular]').button();
        toolbar.find('span[class=draw]').buttonset();

        /*	text: false,
         icons: {
         primary: "ui-icon-seek-start"
         }
         });
         $( "#rewind" ).button({
         text: false,
         icons: {
         primary: "ui-icon-seek-prev"
         }
         });
         $( "#play" ).button({
         text: false,
         icons: {
         primary: "ui-icon-play"
         }
         })
         .click(function() {
         var options;
         if ( $( this ).text() === "play" ) {
         options = {
         label: "pause",
         icons: {
         primary: "ui-icon-pause"
         }
         };
         } else {
         options = {
         label: "play",
         icons: {
         primary: "ui-icon-play"
         }
         };
         }
         $( this ).button( "option", options );
         });
         $( "#stop" ).button({
         text: false,
         icons: {
         primary: "ui-icon-stop"
         }
         })
         .click(function() {
         $( "#play" ).button( "option", {
         label: "play",
         icons: {
         primary: "ui-icon-play"
         }
         });
         });
         $( "#forward" ).button({
         text: false,
         icons: {
         primary: "ui-icon-seek-next"
         }
         });
         $( "#end" ).button({
         text: false,
         icons: {
         primary: "ui-icon-seek-end"
         }
         });
         $( "#shuffle" ).button();
         $( "#repeat" ).buttonset();*/
    },
    initTools : function (controls) {
        for(var key in controls) {
            this.map.addControl(controls[key]);
        }
    }
});
