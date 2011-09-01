
var AdminController = Backbone.Controller.extend({

   initialize : function () {
   },

   routes: {
      "admin/users" : "users",
      "admin/groups" : "groups"
   },

   users : function() {
      if (window.app.view.components.admin.views.usersGrid == undefined) {
         var usersGrid = new CrudGridView({
            el : "#admin > .admin-users",
            model : window.app.models.users,
            title : "Users",
            colName : ['id','username','firstname', 'email', 'password', 'color'],
            colModel : [
               {name:'id',index:'id', width:20},
               {name:'username',index:'username', width:50},
                {name:'firstname',index:'firstname', width:50},
                {name:'email',index:'email', width:80},
                {name:'password',index:'password', width:30},
                {name:'color',index:'color', width:30}
            ]
         });
         usersGrid.render();
         window.app.view.components.admin.views.usersGrid = usersGrid;
      }

      window.app.view.components.admin.show(window.app.view.components.admin.views.usersGrid, "#admin > .sidebar", "users");
      $("#admin-button").attr("href", "#admin/users");
      window.app.view.showComponent(window.app.view.components.admin);
   },

   groups : function() {
      /*if (window.app.view.components.admin.views.groupsGrid == undefined) {
       var groupsGrid = new CrudGridView({
       el : $("#admin > .admin-groups"),
       title : "Groups"
       });
       groupsGrid.render();
       window.app.view.components.admin.views.groupsGrid = groupsGrid;
       }
       window.app.view.components.admin.show(window.app.view.components.admin.views.groupsGrid, "#admin > .sidebar", "groups");
       $("#admin-button").attr("href", "#admin/groups");
       window.app.view.showComponent(window.app.view.components.admin);*/
   }
});