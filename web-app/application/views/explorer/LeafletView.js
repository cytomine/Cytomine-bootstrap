var LeafletView = Backbone.View.extend({
    tagName: "div",
    tileSize: 256,
    review: false,
    divPrefixId: "",
    divId: "",
    currentAnnotation: null,
    userJobForImage: null,
    /**
     * BrowseImageView constructor
     * Accept options used for initialization
     * @param options
     */
    initialize: function (options) {        
        this.initCallback = options.initCallback;
        this.layers = [];
        this.layersLoaded = 0;
        this.baseLayers = [];
        this.broadcastPositionInterval = null;
        this.watchOnlineUsersInterval = null;
        this.annotationsPanel = null;
        this.ontologyPanel = null;
        this.reviewPanel = null;
        this.annotationProperties = null;
        this.map = null;
        //this.nbDigitialZoom = Math.round(Math.log(80 / this.model.get('magnification')) / Math.log(2));//max zoom desired is 80X
        this.nbDigitialZoom = 0; //TMP DISABLED DUE TO OPENLAYERS BUG. http://dev.cytomine.be/jira/browse/CYTO-613
        this.digitalResolutions = [];
        for (var i = 0; i < this.nbDigitialZoom; i++) {
            this.digitalResolutions.push(1 / Math.pow(2, i + 1));
        }
        this.currentAnnotation = null;
        if (options.review != undefined) {
            this.review = options.review;
        }
        if (!this.review) {
            this.divPrefixId = "tabs-image";
        }
        else {
            this.divPrefixId = "tabs-review";
        }


    },
	/**
 	* Grab the layout and call ask for render
 	*/
	render: function () {
    	var self = this;
    	//var template = (this.iPad) ? "text!application/templates/explorer/BrowseImageMobile.tpl.html" : "text!application/templates/explorer/BrowseImage.tpl.html";
    	require(["text!application/templates/explorer/LeafletView.tpl.html"
    	], function (tpl) {
        	self.doLayout(tpl);
    	});
    	return this;
	},

    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
		
        var self = this;
		this.mapID = "map" + this.divPrefixId + this.model.get('id');
		this.divId = "tabs-image-" + window.app.status.currentProject + "-" + this.model.id + "-";
        
		

        var templateData = this.model.toJSON();
        templateData.project = window.app.status.currentProject;
        templateData.type = self.divPrefixId;
        $(this.el).append(_.template(tpl, templateData));
        var shortOriginalFilename = this.model.get('originalFilename');
        if (shortOriginalFilename.length > 25) {
            shortOriginalFilename = shortOriginalFilename.substring(0, 23) + "...";
        }

        var tabTpl =
            "<li>" +
                "<a style='float: left;' id='" + self.divPrefixId + "-<%= idImage %>' rel='tooltip' title='<%= filename %>' href='#" + self.divPrefixId + "-<%= idProject %>-<%= idImage %>-' data-toggle='tab'>" +
                "<i class='icon-search' /> <%= shortOriginalFilename %> " +
                "</a>" +
                "</li>";

        var dataName = 'data-name=<%= idImage %>';

        var tabs = $('#explorer-tab');
        tabs.append(_.template(tabTpl, 
			{ idProject: window.app.status.currentProject, 
			  idImage: this.model.get('id'), 
			  filename: this.model.getVisibleName(window.app.status.currentProjectModel.get('blindMode')),
			  shortOriginalFilename: shortOriginalFilename
			}
		));
		
        var dropdownTpl = '<li class="dropdown"><a href="#" id="' + self.divPrefixId + '-<%= idImage %>-dropdown" class="dropdown-toggle" data-toggle="dropdown"><b class="caret"></b></a><ul class="dropdown-menu"><li><a href="#tabs-dashboard-<%= idProject %>" data-toggle="tab" data-image="<%= idImage %>" class="closeTab" id="closeTab' + self.divPrefixId + '-<%= idImage %>"><i class="icon-remove" /> Close</a></li></ul></li>';
        tabs.append(_.template(dropdownTpl, { idProject: window.app.status.currentProject, idImage: this.model.get('id'), filename: this.model.get('filename')}));       
		
		if (!this.review) {
            this.divPrefixId = "tabs-image";
        }
        else {
            this.divPrefixId = "tabs-review";
        }
		
		this.initMapContainer();
		var map = this.initMap();
		return this;
    },
	initToolbar : function (map) {
		var drawnItems = new L.FeatureGroup();
		map.addLayer(drawnItems);
		var drawControl = new L.Control.Draw({
			draw: {
						position: 'topleft',
						polygon: {
							title: 'Draw a sexy polygon!',
							allowIntersection: false,
							drawError: {
								color: '#b00b00',
								timeout: 1000
							},
							shapeOptions: {
								color: '#bada55'
							}
						},
						circle: {
							shapeOptions: {
								color: '#662d91'
							}
						}
					},
					edit: {
						featureGroup: drawnItems
					}
				});
				map.addControl(drawControl);

				map.on('draw:created', function (e) {
					var type = e.layerType,
						layer = e.layer;

					if (type === 'marker') {
						layer.bindPopup('A popup!');
					}

					drawnItems.addLayer(layer);
				});
	},
	initMapContainer : function() {
        var paddingTop = 77;
        var height = $(window).height() - paddingTop;
		var map = $("#"+this.mapID);
		map.css("height", height);
        map.css("width", "100%");
        $(window).resize(function () {
            var height = $(window).height() - paddingTop;
            map.css("height", height);
        });
	},
    initMap : function() {	
		var self = this;
		var map = L.map(this.mapID, {drawControl: true});
		L.Util.requestAnimFrame(map.invalidateSize,map,!1,map._container);
		map.setView(new L.LatLng(0,0), 0);	
		var imageSize = { width: self.model.get("width"), height: self.model.get("height")};
		new ImageServerUrlsModel({id: self.model.get('baseImage')}).fetch({
            success: function (model, response) {           
				var layer = new ZoomifyLayer(model.get('imageServersURLs'), imageSize);
				map.addLayer(layer);
				self.initToolbar(map);
				L.control.layers({"Original" : layer}, {}).addTo(map);
				
            }
        });	
		return map;
	},	
	show: function (options) {
        var self = this; 
		L.Util.requestAnimFrame(map.invalidateSize,map,!1,map._container);      
    },

});



