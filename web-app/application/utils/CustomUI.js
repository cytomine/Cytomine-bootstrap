var CustomUI = {
    customizeUI : function(callback) {
        var self = this;
        var project = "";
        if(window.app.status.currentProject) {
            project = "?project="+window.app.status.currentProject;
        }

        $.get("custom-ui/config.json"+project, function(data){
            window.app.status.customUI = data;
            if(callback) callback();
        });
    },
    mustBeShow : function(id) {
        return window.app.status.customUI[id];
    },
    hideOrShowComponents : function() {
        var self = this;
        console.log("hideOrShowProjectComponents");
        _.each(window.app.status.customUI,function(value,key) {
            console.log(key);
            console.log(".custom-ui-"+key);
            console.log($(".custom-ui-"+key).length);
            if(!self.mustBeShow(key)) {
                $(".custom-ui-"+key).hide();
            } else {
                $(".custom-ui-"+key).show();
            }
        });
    },
    components: [
        //  {componentId: "project-images-tab", componentName: "Image tab"},
        {componentId: "project-annotations-tab", componentName: "Annotation tab"},
        {componentId: "project-images-tab", componentName: "Images tab"}, //TODO: if you need to add a new panel
        {componentId: "project-properties-tab", componentName: "Properties tab"},
        {componentId: "project-jobs-tab", componentName: "Jobs tab"},
        {componentId: "project-configuration-tab", componentName: "Config tab"} //TODO: cannot be hide by project-admin
    ],
    roles:[
        { "authority": "ADMIN_PROJECT","name": "project admin"},
        { "authority": "USER_PROJECT", "name": "project user" },
        {"authority": "GUEST_PROJECT","name": "project guest user"}
    ]
};