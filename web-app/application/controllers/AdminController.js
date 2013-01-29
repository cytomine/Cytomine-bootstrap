var AdminController = Backbone.Router.extend({

    initialize: function () {
    },

    routes: {
        "admin/users": "users",
        "admin/groups": "groups"
    },

    users: function () {
        var selectClass = "authorities" + (new Date()).getTime();
        var secRoles = new SecRoleCollection().fetch({
            success: function (collection, response) {
                var selectElement = function (value, options) {
                    var userAuthorities = value.split(",");
                    var authorities = "";

                    collection.each(function (model) {
                        if (_.contains(userAuthorities, model.get("authority"))) {
                            authorities += _.template("<option value='<%=   id %>' selected><%=   authority %></option>",
                                {id: model.get("id"), authority: model.get("authority")});
                        } else {
                            authorities += _.template("<option value='<%=   id %>' ><%=   authority %></option>",
                                {id: model.get("id"), authority: model.get("authority")});
                        }
                    });
                    var el = _.template('<select class="<%=   selectClass %>" title="" multiple="multiple" name="example-basic" size="5" style="display:none;">' +
                        '<%=   authorities %></select>', { selectClass: selectClass, authorities: authorities });
                    return el;
                }
                var selectValue = function (elem, operation, value) {
                    if (operation === 'get') {
                        var values = []
                        /*$("#authorities").multiselect("widget").find(":checkbox").each(function(){
                         if ($(this).attr("checked") == "checked") {
                         values.push($(this).attr("value"));
                         }
                         });*/
                        return values.join(",");
                    } else if (operation === 'set') {
                        //nothing to do
                    }
                }

                if (window.app.view.components.admin.views.usersGrid == undefined) {
                    var usersGrid = new CrudGridView({
                        el: "#admin > .admin-users",
                        url: "api/user/grid",
                        restURL: "api/user",
                        title: "Users",
                        colName: ['id', 'username', 'firstname', 'email', 'password', 'authorities', 'color'],
                        colModel: [
                            {name: 'id', index: 'id', width: 30},
                            {name: 'username', index: 'username', editable: true, width: 50},
                            {name: 'firstname', index: 'firstname', editable: true, width: 50},
                            {name: 'lastname', index: 'lastname', editable: true, width: 50},
                            {name: 'email', index: 'email', editable: true, width: 80},
                            {name: 'password', index: 'password', editable: true, width: 30, edittype: 'password'},
                            /*{name:'authorities',index:'authorities', editable : true,width:30, edittype:'custom', editoptions:{
                             custom_element: selectElement,
                             custom_value:selectValue}
                             },*/
                            {name: 'color', index: 'color', editable: true, width: 30, edittype: 'custom', editoptions: {
                                custom_element: new CrudGridView({}).customFields.color.colorPickerElement,
                                custom_value: new CrudGridView({}).customFields.color.colorPickerValue}
                            }
                        ]
                    });
                    usersGrid.render();
                    window.app.view.components.admin.views.usersGrid = usersGrid;
                }

                window.app.view.components.admin.show(window.app.view.components.admin.views.usersGrid, "#admin > .sidebar", "users");
                $("#admin-button").attr("href", "#admin/users");
                window.app.view.showComponent(window.app.view.components.admin);
            }});
    },

    groups: function () {
        if (window.app.view.components.admin.views.groupsGrid == undefined) {
            var groupsGrid = new CrudGridView({
                el: "#admin > .admin-groups",
                url: "api/group/grid",
                editurl: "api/group",
                title: "Groups",
                colName: ['id', 'name'],
                colModel: [
                    {name: 'id', index: 'id', width: 20},
                    {name: 'name', index: 'name', editable: true, width: 50}
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