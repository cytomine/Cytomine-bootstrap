var CustomUI = {
    retrieveGlobalConfig : function(callback) {
        $.get("custom-ui/global.json", function(data){
            window.app.status.customUI = {global:data};
            callback();
        });
    },
    mustBeShow : function(id) {
        return window.app.status.customUI.global[id];
    },
    retrieveProjectConfig : function(callback) {
        $.get( "custom-ui/project/"+window.app.status.currentProject+"/flag.json", function(data){
            window.app.status.customUI.project = data;
            callback();
        });
    },
    projectComponentMustBeShow : function(id) {
        console.log(id + "=>" + window.app.status.customUI.project[id]);
        if(window.app.status.customUI.project[id]==undefined || window.app.status.customUI.project[id]==null) {
            return true
        } else return window.app.status.customUI.project[id];
    },
    hideOrShowProjectComponents : function() {
        var self = this;
        console.log("hideOrShowProjectComponents");
        var fn = function() {
            console.log(self.components);
            _.each(self.components,function(item) {
                if(!self.projectComponentMustBeShow(item.componentId)) {
                    $("#"+item.componentId).hide();
                } else {
                    $("#"+item.componentId).show();
                }
            });
        }
        this.retrieveProjectConfig(fn);
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