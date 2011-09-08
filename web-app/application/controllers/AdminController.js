
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
            url : "api/user/grid",
            restURL : "api/user",
            title : "Users",
            colName : ['id','username','firstname', 'email', 'password', 'color'],
            colModel : [
               {name:'id',index:'id', width:20},
               {name:'username',index:'username',editable : true, width:50},
               {name:'firstname',index:'firstname',editable : true, width:50},
               {name:'lastname',index:'lastname',editable : true, width:50},
               {name:'email',index:'email',editable : true, width:80},
               {name:'password',index:'password', editable : true,width:30, edittype : 'password'},
               {name:'color',index:'color',editable : true, width:30, edittype:'custom', editoptions:{
                  custom_element: new CrudGridView({}).customFields.color.colorPickerElement,
                  custom_value:new CrudGridView({}).customFields.color.colorPickerValue}
               }
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
      if (window.app.view.components.admin.views.groupsGrid == undefined) {
         var groupsGrid = new CrudGridView({
            el : "#admin > .admin-groups",
            url : "api/group/grid",
            editurl : "api/group",
            title : "Groups",
            colName : ['id','name'],
            colModel : [
               {name:'id',index:'id', width:20},
               {name:'name',index:'name',editable : true, width:50}
            ]
         });
         groupsGrid.render();
         window.app.view.components.admin.views.groupsGrid = groupsGrid;
      }

      window.app.view.components.admin.show(window.app.view.components.admin.views.groupsGrid, "#admin > .sidebar", "groups");
      $("#admin-button").attr("href", "#admin/groups");
      window.app.view.showComponent(window.app.view.components.admin);
   }
});