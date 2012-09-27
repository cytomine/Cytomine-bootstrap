var UserModel = Backbone.Model.extend({
    /*initialize: function(spec) {
     if (!spec || !spec.name || !spec.username) {
     throw "InvalidConstructArgs";
     }
     },

     validate: function(attrs) {
     if (attrs.name) {
     if (!_.isString(attrs.name) || attrs.name.length === 0) {
     return "Name must be a string with a length";
     }
     }
     },*/

    url : function() {
        var base = 'api/user';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },

    prettyName : function () {
        return this.get('firstname') + " " + this.get('lastname');
    }
});



var UserFriendCollection = Backbone.Collection.extend({
    model: UserModel,
    url: function() {
        return "api/user/" + this.id + "/friends.json";
    },
    initialize: function (options) {
        this.id = options.id;
    },comparator : function(user) {
        return user.get("username").toLowerCase();
    }
});

var UserOnlineCollection = Backbone.Collection.extend({
    model: UserModel,
    url: function() {
        return "api/project/" + this.project + "/online/user.json";
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

// define our collection
var UserCollection = Backbone.Collection.extend({
    model: UserModel,
    url: function() {
        if (this.project != undefined && this.admin == true) {
            return "api/project/" + this.project + "/admin.json";
        } else if (this.project != undefined && this.creator == true) {
            return "api/project/" + this.project + "/creator.json";
        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/user.json";
        } else if (this.ontology != undefined && this.creator == true) {
            return "api/ontology/" + this.ontology + "/creator.json";
        }else if (this.ontology != undefined) {
            console.log("ontologyYYY="+this.ontology);
            return "api/ontology/" + this.ontology + "/user.json";
        }else {
            return "api/user.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.ontology = options.ontology;
        this.admin = options.admin;
        this.creator = options.creator;
    },comparator : function(user) {
        return user.get("username").toLowerCase();
    }
});


var UserLayerCollection = Backbone.Collection.extend({
    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/userlayer.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});


var UserSecRole =  Backbone.Model.extend({
    url: function() {
        if (this.role != undefined || this.isNew()) {
            return "api/user/" + this.user + "/role.json";
        } else {
            return "api/user/" + this.user + "/role/"+this.role+".json";
        }
    },
    initialize: function (options) {
        this.user = options.user;
        this.role = options.role;
    }
});


// define our collection
var UserJobCollection = Backbone.Collection.extend({
    model: UserModel,
    url: function() {
        if (this.project && !this.tree) {
            return "api/project/" + this.project + "/userjob.json";
        } else if (this.project && this.tree) {
            return "api/project/" + this.project + "/userjob.json?tree=true";
        } else {
            return "api/userjob.json";
        }
    },
    prettyName : function () {
        return this.get('softwareName');
    },
    initialize: function (options) {
        this.project = options.project;
        this.tree = options.tree || false;
    },comparator : function(user) {
        return -user.get("created");
    }
});
