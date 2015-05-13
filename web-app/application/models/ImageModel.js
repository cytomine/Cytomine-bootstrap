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

var ImageModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/abstractimage';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    downloadUrl : function() {
        if (this.get('id')) {
            return 'api/abstractimage/' + id + "/download";
        } else {
            return null;
        }
    },
    getVisibleName : function(hideName) {
        if(!hideName) {
            return this.get('originalFilename');
        } else {
            return "[BLIND] id=" + this.get('id').toString();
        }
    }
});


var ImageReviewModel = Backbone.Model.extend({
    url: function () {
        if (this.cancel != undefined) {
            return "/api/imageinstance/" + this.id + "/review.json?cancel=" + this.cancel;
        } else {
            return "/api/imageinstance/" + this.id + "/review.json";
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.cancel = options.cancel;
    }
});


var ImageMetadataModel = Backbone.Model.extend({
    initialize: function (options) {
        this.image = options.image;
    },
    url: function () {
        return 'api/abstractimage/' + this.image + "/metadata.json?extract=true";
    }
});

var ImagePropertyCollection = PaginatedCollection.extend({
    initialize: function (options) {
        this.initPaginator(options);
        this.image = options.image;
    },
    url: function () {
        return 'api/domain/be.cytomine.image.AbstractImage/' + this.image + "/property.json";
    },
    comparator: function (model) {
        return model.get("key");
    }
});

// define our collection
var ImageCollection = PaginatedCollection.extend({
    model: ImageModel,
    url: function () {

        var params = "?";
        if(this.project) {
            params = params + "project="+ this.project;
        }
        return "api/abstractimage.json"+params;
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var ImageServerUrlsModel = Backbone.Model.extend({
    url: function () {
        var url = 'api/abstractimage/' + this.id + "/imageservers.json?";
        if(this.merge) {
            url = url+ "&merge="+this.merge;
        }
        if(this.imageinstance) {
            url = url+ "&imageinstance="+this.imageinstance;
        }
        if(this.merge && this.channels) {
            //window.app.mergeChannel [[1,#ff0000],[2,#00ff00],..]
            var chanIds = []
            _.each(this.channels,function(channel) {
                chanIds.push(channel[0]);
            });
            var colorIds = []
            _.each(this.channels,function(channel) {
                colorIds.push(channel[1].replace("#",""));
            });
            url = url+ "&channels="+chanIds.join(",")+"&colors="+colorIds.join(",")

        }



        return url;
    },
    initialize: function (options) {
        this.merge = options.merge;
        this.imageinstance = options.imageinstance;
        this.channels = options.channels;
    }
});






var ImageInstanceModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/imageinstance';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        var otherImage = "";
        if(this.next) {
            otherImage = "/next"
        }
        if(this.previous) {
            otherImage = "/previous"
        }
        var url = base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + otherImage + format;
        console.log(url);
        return url;
    },
    initialize: function (options) {
        this.id = options.id;
        this.next = options.next;
        this.previous = options.previous;
    },
    getVisibleName : function(hideName, isAdmin) {
        if (isAdmin) {
            var result = [this.get('instanceFilename')];
            if(hideName) {
                result.push("[BLIND]" + this.get('id'));
            }
            return result;

        }
        if(!hideName) {
            return this.get('instanceFilename');
        } else {
            return "[BLIND]" + this.get('id');
        }
    }
});

// define our collection
var ImageInstanceCollection = PaginatedCollection.extend({
    model: ImageModel,
    url: function () {
        if (this.tree) {
            return "api/project/" + this.project + "/imageinstance.json?tree=true";
        } else {
            return "api/project/" + this.project + "/imageinstance.json";
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.tree = options.tree != undefined && options.tree == true;
    }
});


var UserPositionModel = Backbone.Model.extend({
    url: function () {
        if (this.user == undefined) {
            return 'api/imageinstance/' + this.image + '/position.json';
        } else {
            return 'api/imageinstance/' + this.image + '/position/' + this.user + ".json";
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
    }
});

var UserOnlineModel = Backbone.Model.extend({
    url: function () {
        return 'api/imageinstance/' + this.image + '/online.json';
    },
    initialize: function (options) {
        this.image = options.image;
    }
});


