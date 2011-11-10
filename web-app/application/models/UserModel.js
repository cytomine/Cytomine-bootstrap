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


// define our collection
var UserCollection = Backbone.Collection.extend({
   model: UserModel,

   url: function() {
      if (this.project != undefined) {
         return "api/project/" + this.project + "/user.json";
      } else {
         return "api/user.json";
      }
   },
   initialize: function (options) {
      this.project = options.project;
    },comparator : function(user) {
        return user.get("username").toLowerCase();
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
