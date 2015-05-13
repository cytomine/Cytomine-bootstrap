/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ImageFiltersPanel = Backbone.View.extend({
    tagName: "div",
    filter: null,
    enabled: false,
    parameters: [],
    browseImageView: null,
    /**
     * Constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require(["text!application/templates/explorer/ImageFiltersPanel.tpl.html"
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
        var el = $("#" + this.browseImageView.divId).find('#imageFiltersPanel' + this.model.get('id'));
        this.model.set({isDesktop: !window.app.view.isMobile});
        el.html(_.template(tpl, this.model.toJSON()));
        el.find("#contrast" + this.model.get("id")).slider({
            min: 0,
            max: 256,
            value: 128,
            change: function (event, ui) {
                self.redraw();
            }
        });
        el.find("#brightness" + this.model.get("id")).slider({
            min: 0,
            max: 256,
            value: 128,
            change: function (event, ui) {
                self.redraw();
            }
        });

        el.find("input[name=invert]").change(function () {
            self.redraw();
        });
        el.find("input[name=filterActive]").change(function () {
            self.enabled = ($(this).is(':checked'));
            self.redraw();
        });
        el.find("a[name=reset]").click(function (event) {
            event.preventDefault();
            el.find("#brightness" + self.model.get("id")).slider("option", "value", 128);
            el.find("#contrast" + self.model.get("id")).slider("option", "value", 128);
            el.find("input[name=invert]").removeAttr("checked");
            return false;
        });
    },

    updateGetURL: function () {
        var self = this;
        var getAdvancedURL = function (bounds) {
            bounds = this.adjustBounds(bounds);
            var res = this.map.getResolution();
            var x = Math.round((bounds.left - this.tileOrigin.lon) / (res * this.tileSize.w));
            var y = Math.round((this.tileOrigin.lat - bounds.top) / (res * this.tileSize.h));
            var z = this.map.getZoom();

            var tileIndex = x + y * this.tierSizeInTiles[z].w + this.tileCountUpToTier[z];
            var path = "TileGroup" + Math.floor((tileIndex) / 256) +
                "/" + z + "-" + x + "-" + y + ".jpg";
            var url = this.url;

            var updatedUrl = _.map(url, function (url) {
                var parametersSTR = "";
                _.each(self.parameters, function (parameter) {
                    parametersSTR += parameter.key;
                    parametersSTR += "=";
                    parametersSTR += parameter.value;
                    parametersSTR += "&";
                });
                var index = url.indexOf("method=");
                if (index == -1) { //Original layer
                    url = "vision/process?method=none&url=" + url;
                    index = url.indexOf("method=");
                }
                return url.substring(0, index) + parametersSTR + url.substring(index, url.length);


            });
            if (OpenLayers.Util.isArray(updatedUrl)) {
                url = this.selectUrl(path, updatedUrl);
            }
            return url + path + "&mimeType="+self.model.get("mime");

        };
        self.browseImageView.map.baseLayer.getURL = getAdvancedURL;
        self.browseImageView.map.baseLayer.redraw();

    },
    redraw: function () {
        if (!this.enabled) {
            this.parameters = [];
            this.updateGetURL();
            return;
        }
        var el = $("#" + this.browseImageView.divId).find("#imageFiltersPanel" + this.model.get("id"));
        var brightness = parseInt(el.find("#brightness" + this.model.get("id")).slider("value"));
        var contrast = parseInt(el.find("#contrast" + this.model.get("id")).slider("value"));
        var invert = (el.find("input[name=invert]").is(':checked'));
        this.parameters = [];
        this.parameters.push({ key: "brightness", value: brightness});
        this.parameters.push({ key: "contrast", value: contrast});
        if (invert) {
            this.parameters.push({ key: "invert", value: true});
        }
        this.updateGetURL();
    }
});
