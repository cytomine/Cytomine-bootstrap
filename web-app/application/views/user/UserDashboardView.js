var UserDashboardView = Backbone.View.extend({
    tagName: "div",
    searchProjectPanelElem: "#searchProjectPanel",
    projectListElem: "#projectlist",
    projectList: null,
    addSlideDialog: null,
    initialize: function (options) {
        this.container = options.container;
        this.model = options.model;
        this.el = options.el;
        this.searchProjectPanel = null;
        this.addProjectDialog = null;
    },
    render: function () {
        console.log("UserDashboardView.render");
        var self = this;
        require([
            "text!application/templates/user/UserDashboardComponent.tpl.html",
            "text!application/templates/image/ImageThumbLight.tpl.html",
            "text!application/templates/user/UserStats.tpl.html"
        ],
            function (tpl,tplImg,tpStat) {
                self.doLayout(tpl,tplImg,tpStat);
            });

        return this;
    },
    doLayout: function (tpl,tplImg,tpStat) {
        var self = this;
        self.nbreAnnotation= null,
        self.nbreReviewedAnnotation= null,
        console.log("NBREPROJECT="+$("#userDashboardLastProject").find(".span6").length);
        $("#userdashboard").empty();
        $("#userdashboard").replaceWith(_.template(tpl, {}));
        $("#userdashboard").css("display","inline");
        self.el = $("#userdashboard");
        console.log("NBREPROJECT="+$("#userDashboardLastProject").find(".span6").length);
        self.initStats(tpStat);
        self.initLastAction();
        self.initLastNews();
        self.initGotoProject();
        self.initGotoImage();
        self.initLastOpenedImage(tplImg);

        return this;
    },
    initLastOpenProject : function() {
        var self = this;
        var elem = $(self.el).find("#userDashboardLastProject");
        console.log("initLastOpenProject");
        $.get("/api/project/lastopened?max=6", function(data) {
            console.log("GET PROJECT OK");
            console.log(elem.length);
            var collection = data.collection;
            elem.empty();
            _.each(collection,function(item) {

                var project = window.app.models.projects.get(item.id);

                if(project) {
                    //user has still access to this project
                    var panel = new ProjectPanelView({
                        model: project,
                        projectsPanel: self,
                        container: self,
                        connectionInfo: item
                    }).render();

                    var str = "<div class='span6'>"+$(panel.el).html()+"</div>";
                    elem.append(str);
                }
            });
            elem.find(".span6").css("margin-left","0px");
         });

    },
    initStats : function(tpStat) {
        console.log("initStats");
        var self = this;
        var count = 0;
        var elem = $(self.el).find("#userStatsDashboard");

        var allGetLoaded = function() {
            if(self.nbreAnnotation!=null && self.nbreReviewedAnnotation!=null) {
                elem.empty();
                elem.append(_.template(tpStat,{prettyName:window.app.status.user.model.prettyName(),projectCount:window.app.models.projects.length,annotationCount:self.nbreAnnotation,reviewedCount:self.nbreReviewedAnnotation}));
                self.initLastOpenProject();
            }

        }

        $.get("/api/user/"+window.app.status.user.id+"/userannotation/count", function(data) {
            self.nbreAnnotation = data.total;
            allGetLoaded();
         });
        $.get("/api/user/"+window.app.status.user.id+"/reviewedannotation/count", function(data) {
            self.nbreReviewedAnnotation = data.total;
            allGetLoaded();
         });


    },
    initLastAction : function() {
        var self = this;
        var elem =  $(self.el).find("#userdashboardLastAction");
        $.get("api/commandhistory?user="+window.app.status.user.id+"&max=10&offset=0", function(data) {
            var collection = data;
            elem.find(".alert-info").replaceWith("")
            _.each(collection,function(item) {
                var action = "<li><b>"+window.app.convertLongToDate(item.created) +"</b> : "+ item.prefixAction + " " + item.message; + "</li>"
                elem.append(action);
            });
         });
    },
    initLastNews : function() {
        var self = this;
        var elem =  $(self.el).find("#userdashboardLastNews");


        $.get("api/news?max=10", function(data) {
            elem.find(".alert-info").replaceWith("");
            var collection = data.collection;
            _.each(collection,function(item) {
                var date = window.app.convertLongToDateShort(item.added)
                var today=new Date()
                var newInfo = "";
                var one_day=1000*60*60*24
                var days = Math.ceil((item.added-today.getTime())/(one_day));
                if(days>-30) {
                    newInfo =  '<span class="badge badge-warning">New!</span>&nbsp;';
                }
                var action = "<li>"+newInfo+"<b>"+date +"</b> : "+ item.text + "</li>"
                elem.append(action);
            });
         });
    },
    initGotoProject : function() {
        var maggicProject = [];
        var selectProject = $("#goToProjectUser");
        window.app.models.projects.each(function (project) {
            maggicProject.push(project.get('name'));
        })
        selectProject.typeahead({
            source:maggicProject,
            minLength:0,
            updater: function(selection){
                var id = null;
                window.app.models.projects.each(function (project) {
                    if(project.get('name')==selection) {
                        window.location = "#tabs-dashboard-" + project.id;
                    }
                });

            }});
    },
    initGotoImage : function() {
        var self = this;
        var maggicImage = [];



        $.get("/api/user/"+window.app.status.user.id+"/imageinstance/light.json", function(data) {

            $("#selectImageContainer").empty();
            $("#selectImageContainer").append('<input id="goToImageUser" data-provide="typeahead" style="max-width: 150px;">');
            var selectImage = $("#goToImageUser");

               self.images = data.collection;
               _.each (self.images, function (item){
                   maggicImage.push(item.originalFilename + " in " + item.projectName);
               });

                  console.log(maggicImage.length);
                selectImage.typeahead({
                    source:maggicImage,
                    minLength:0,
                    updater: function(selection){
                        _.each (self.images, function (item){
                            if(item.originalFilename + " in " + item.projectName==selection) {
                                window.location = "#tabs-image-"+item.project+"-"+item.id+"-"
                            }
                        });
                 }});
         });
    },

    initLastOpenedImage : function(tpl) {
        var self = this;
        console.log("initLastOpenedImage");
        //http://localhost:8080/api/imageinstance/lastopened?max=3
        var elem = $(self.el).find("#lastOpenImage");
        $.get("/api/imageinstance/lastopened?max=5", function(data) {
            console.log("get");
            elem.empty();
            _.each (data.collection, function (item){
                item.lastOpen = window.app.convertLongToDate(item.date)
                item.maxW = Math.min(elem.width(),300);


                var maxNumberOfChar = 25;
                var title = item.originalFilename;
                if (title.length > maxNumberOfChar) {
                    title = title.substr(0, maxNumberOfChar) + "...";
                }
                item.originalFilename = title;

                elem.append(_.template(tpl,item));
                elem.append("<hr/>")
            });
        });
    }
});
