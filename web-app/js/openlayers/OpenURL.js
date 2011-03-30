/* Copyright (c) UNC Chapel Hill University Library, created by Hugh A. Cayless
 * and revised by J. Clifford Dyer.  Published under the Clear BSD licence.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license.
 */


/**
 * @requires OpenLayers/Layer/Grid.js
 * @requires OpenLayers/Tile/Image.js
 */

/**
 * Class: OpenLayers.Layer.OpenURL
 *
 * Inherits from:
 *  - <OpenLayers.Layer.Grid>
 */
OpenLayers.Layer.OpenURL = OpenLayers.Class(OpenLayers.Layer.Grid, {

    /**
     * APIProperty: isBaseLayer
     * {Boolean}
     */
    isBaseLayer: true,

    /**
     * APIProperty: tileOrigin
     * {<OpenLayers.Pixel>}
     */
    tileOrigin: null,

    url_ver: 'Z39.88-2004',
    rft_id: null,
    svc_id: "info:lanl-repo/svc/getRegion",
    svc_val_fmt: "info:ofi/fmt:kev:mtx:jpeg2000",
    format: null,
    tileHeight: null,
    viewerLevel : null,

    /**
     * Constructor: OpenLayers.Layer.OpenURL
     *
     * Parameters:
     * name - {String}
     * url - {String}
     * options - {Object} Hashtable of extra options to tag onto the layer
     */
    initialize: function(name, url, options) {

        var newArguments = [];
        newArguments.push(name, url, {}, options);
        OpenLayers.Layer.Grid.prototype.initialize.apply(this, newArguments);
        this.rft_id = options.rft_id;
        this.format = options.format;
        // Get image metadata if it hasn't been set
        if (!options.imgMetadata) {
          var request = OpenLayers.Request.issue({url: options.metadataUrl, async: false});
          this.imgMetadata = eval('(' + request.responseText + ')');
        } else {
          this.imgMetadata = options.imgMetadata;
        }

        var minLevel = this.getMinLevel();

        // viewerLevel is the smallest useful zoom level: i.e., it is the largest level that fits entirely
        // within the bounds of the viewer div.
        /*viewerLevel = Math.ceil(Math.min(minLevel, Math.max(
            (Math.log(this.imgMetadata.width) - Math.log(OpenLayers.Layer.OpenURL.viewerWidth)),
            (Math.log(this.imgMetadata.height) - Math.log(OpenLayers.Layer.OpenURL.viewerHeight)))/
               Math.log(2)));*/

        viewerLevel = Math.ceil(Math.log( this.imgMetadata.width / 256) / Math.log(2));

        if (this.imgMetadata.width / Math.pow(2, viewerLevel) < 150) viewerLevel--;

        this.zoomOffset = minLevel - viewerLevel;

        // width at level viewerLevel
        var w = this.imgMetadata.width / Math.pow(2, viewerLevel);

        // height at level viewerLevel
        var h = this.imgMetadata.height / Math.pow(2, viewerLevel);

        this.resolutions = new Array();
        for (i = viewerLevel; i >= 0; i--) {
          this.resolutions.push(Math.pow(2, i));
        }

        //this.tileSize = new OpenLayers.Size(Math.ceil(w), Math.ceil(h));
        this.tileSize = new OpenLayers.Size(Math.ceil(w), Math.ceil(h));
        this.tileSize = new OpenLayers.Size(Math.ceil(w), Math.ceil(h));


    },
    getViewerLevel : function () {
        return viewerLevel;
    },
    /**
     * APIMethod:destroy
     */
    destroy: function() {
        // for now, nothing special to do here.
        OpenLayers.Layer.Grid.prototype.destroy.apply(this, arguments);
    },


    /**
     * APIMethod: clone
     *
     * Parameters:
     * obj - {Object}
     *
     * Returns:
     * {<OpenLayers.Layer.OpenURL>} An exact clone of this <OpenLayers.Layer.OpenURL>
     */
    clone: function (obj) {

        if (obj == null) {
            obj = new OpenLayers.Layer.OpenURL(this.name,
                                           this.url,
                                           this.options);
        }

        //get all additions from superclasses
        obj = OpenLayers.Layer.Grid.prototype.clone.apply(this, [obj]);

        // copy/set any non-init, non-simple values here

        return obj;
    },

    /**
     * Method: getURL
     *
     * Parameters:
     * bounds - {<OpenLayers.Bounds>}
     *
     * Returns:
     * {String} A string with the layer's url and parameters and also the
     *          passed-in bounds and appropriate tile size specified as
     *          parameters
     */
    getURL: function (bounds) {
        //console.log(bounds.toString());
        bounds = this.adjustBounds(bounds);
        this.calculatePositionAndSize(bounds);
        var z = this.map.getZoom() + this.zoomOffset;
        var path = "?url_ver=" + this.url_ver + "&rft_id=" + this.rft_id +
            "&svc_id=" + this.svc_id + "&svc_val_fmt=" + this.svc_val_fmt + "&svc.format=" +
            this.format + "&svc.level=" + z + "&svc.rotate=0&svc.region=" + this.tilePos.lat + "," +
            this.tilePos.lon + "," + this.imageSize.h + "," + this.imageSize.w;

        var url = this.url;
        if (url instanceof Array) {
            url = this.selectUrl(path, url);
        }
        return url + path;
    },

    /**
     * Method: addTile
     * addTile creates a tile, initializes it, and adds it to the layer div.
     *
     * Parameters:
     * bounds - {<OpenLayers.Bounds>}
     * position - {<OpenLayers.Pixel>}
     *
     * Returns:
     * {<OpenLayers.Tile.Image>} The added OpenLayers.Tile.Image
     */
    addTile:function(bounds,position) {
      this.calculatePositionAndSize(bounds);
      var size = this.size;
      return new OpenLayers.Tile.Image(this, position, bounds,
                                         null, this.imageSize);
    },

    /**
     * APIMethod: setMap
     * When the layer is added to a map, then we can fetch our origin
     *    (if we don't have one.)
     *
     * Parameters:
     * map - {<OpenLayers.Map>}
     */
    setMap: function(map) {
        OpenLayers.Layer.Grid.prototype.setMap.apply(this, arguments);
        if (!this.tileOrigin) {
            this.tileOrigin = new OpenLayers.LonLat(this.map.maxExtent.left,
                                                this.map.maxExtent.bottom);
        }
    },

    calculatePositionAndSize: function(bounds) {
      // Have to recalculate x and y (instead of using bounds and resolution), because resolution will be off.
      // Get number of tiles in image
      var max = this.map.getMaxExtent();
      var xtiles = Math.round( 1 / (this.tileSize.w / max.getWidth()));
      // Find out which tile we're on
      var xpos = Math.round((bounds.left / max.getWidth()) * xtiles);
      // Set x
      var x = xpos * (this.tileSize.w + 1);
      var w,h;
      var xExtent = max.getWidth() / this.map.getResolution();
      if (xpos == xtiles - 1) {
        w = xExtent % (this.tileSize.w + 1);
      } else {
        w = this.tileSize.w;
      }
      // Do the same for y
      var ytiles = Math.round( 1 / (this.tileSize.h / max.getHeight()));
      // Djatoka's coordinate system is top-down, not bottom-up, so invert for y
      var y = max.getHeight() - bounds.top;
      y = y < 0? 0 : y;
      var ypos = Math.round((y / max.getHeight()) * ytiles);
      var y = ypos * (this.tileSize.h + 1);
      var yExtent = max.getHeight() / this.map.getResolution();
      if (ypos == ytiles - 1) {
        h = yExtent % (this.tileSize.h + 1);
      } else {
        h = this.tileSize.h;
      }
      this.tilePos = new OpenLayers.LonLat(x,y);
      this.imageSize = new OpenLayers.Size(w,h);
    },

    getImageMetadata: function() {
      return this.imgMetadata;
    },

    getResolutions: function() {
      return this.resolutions;
    },

    getTileSize: function() {
      return this.tileSize;
    },

    getMinLevel: function() {
        // Versions of djatoka from before 4/17/09 have levels set to the
        // number of levels encoded in the image.  After this date, that
        // number is assigned to the new dwtLevels, and levels contains the
        // number of levels between the full image size and the minimum
        // size djatoka could return.  We want the lesser of these two numbers.

        var levelsInImg;
        var levelsToDjatokaMin;
        if (this.imgMetadata.dwtLevels === undefined) {
            var maxImgDimension = Math.max(this.imgMetadata.width,
                                         this.imgMetadata.height);
            levelsInImg = this.imgMetadata.levels;
            levelsToDjatokaMin = Math.floor((Math.log(maxImgDimension) -
                Math.log(OpenLayers.Layer.OpenURL.minDjatokaLevelDimension)) /
                Math.log(2));
        } else {
            var levelsInImg = this.imgMetadata.dwtLevels;
            var levelsToDjatokaMin = this.imgMetadata.levels;
        }
        return Math.min(levelsInImg, levelsToDjatokaMin);
    },

    CLASS_NAME: "OpenLayers.Layer.OpenURL"
});

/*OpenLayers.Layer.OpenURL.viewerWidth = 512;
OpenLayers.Layer.OpenURL.viewerHeight = 512;*/
OpenLayers.Layer.OpenURL.minDjatokaLevelDimension = 48;
//OpenLayers.Layer.OpenURL.djatokaURL = '/adore-djatoka/resolver';