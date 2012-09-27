var ProjectView = Backbone.View.extend({
    tagName : "div",
    searchProjectPanelElem : "#searchProjectPanel",
    projectListElem : "#projectlist",
    projectList : null,
    addSlideDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.model = options.model;
        this.el = options.el;
        this.searchProjectPanel = null;
        this.addProjectDialog = null;
        this.ontologies = this.getOntologiesChoice();
        this.disciplines = this.getDisciplinesChoice();
    },
    render : function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectList.tpl.html",
            "text!application/templates/project/NewProjectBox.tpl.html"
        ],
                function(tpl, newProjectBoxTpl) {
                    self.doLayout(tpl, newProjectBoxTpl);
                });

        return this;
    },
    doLayout: function(tpl, newProjectBoxTpl) {
        var self = this;
        $(this.el).find("#projectdiv").html(_.template(tpl, {}));

        //clear de list
        $(self.projectListElem).empty();
        $(self.projectListElem).append(_.template(newProjectBoxTpl, {}));
        $("#projectaddbutton").on("click", function(){
            self.showAddProjectPanel();
        });
        //print addProjectPanel
        //self.generateAddProjectPanel();

        //print search panel
        self.loadSearchProjectPanel();

        //print all project panel
        self.loadProjectsListing();

        self.printProjectInfo();

        return this;
    },
    /**
     * Show dialog to add a project
     */
    showAddProjectPanel : function() {

        var self = this;
        $('#addproject').remove();
        self.addProjectDialog = new AddProjectDialog({
            projectsPanel:self,
            el:self.el,
            ontologies : self.ontologies,
            disciplines : self.disciplines
        }).render();
    },
    /**
     * Refresh all project panel
     */
    refresh : function() {
        var self = this;
        var idUser =  undefined;

        //_.each(self.projectList, function(panel){ panel.refresh(); });
        if(self.addSlideDialog!=null) self.addSlideDialog.refresh();


        new ProjectCollection({user : idUser}).fetch({
            success : function (collection, response) {
                self.model = collection;
                self.render();
            }});
    },
    printProjectInfo : function() {
       console.log("printProjectInfo");
       var self = this;
        var allProjectsPanel = $(self.el).find(".projectInfoPanel");

        require([
            "text!application/templates/project/ProjectBigPanel.tpl.html"
        ],
                function(tpl) {
                    allProjectsPanel.hover(function() {

                                $(".projectBigInfo").replaceWith("");

                               var id = $(this).attr('data-id');
                               var project = self.model.get(id);
                               var indexNextRow = self.getIndiceNextRow($(this),allProjectsPanel);

                                console.log('id='+id);
                                console.log('project='+project);
                                console.log('indexNextRow='+indexNextRow);


                                var newRowItem = $(allProjectsPanel).eq(indexNextRow);
                                console.log("newRowItem="+newRowItem.attr('data-id'));


                                //var htmlCode = '<div class="well span2 projectBigInfo" style="padding: 5px;margin: 0 15px 15px 0px;min-width: '+self.getFullWidth()+'px;font-size: 12px;">'+project.get("name")+'</div>';
                                var htmlCode = _.template(tpl,{projectName:project.get('name'),width:self.getFullWidth(),id:project.id});
                                if(newRowItem.attr('data-id')!=undefined) {
                                    newRowItem.parent().before(htmlCode);
                                } else {
                                    //last row
                                    allProjectsPanel.last().after(htmlCode);
                                }


                                new ImageInstanceCollection({project:project.id,inf:0,sup:3}).fetch({
                                    success : function (collection, response) {
                                        collection.each(function(image) {

                                           var str = '<div style="min-height: 128px;max-width: 30%;" class="span6">' +
                                                   '<a href="#tabs-image-'+project.id+'-'+image.id+'-">' +
                                                   '     <img class="lazy" src="'+image.get("thumb")+'" alt="'+image.get("filename")+'" style="max-height:200px; max-width:100%;">' +
                                                   '</a></div>';
                                            $("#imageInfoBigPanel-"+project.id).find(".row").append(str);
                                        })
                                    }});

                                new UserCollection({project:project.id, creator:true}).fetch({
                                        success : function (creator, response) {
                                            $("#userInfoBigPanel-"+project.id).find("#projectCreator").empty();
                                            var list = [];
                                            creator.each(function(user) {
                                                list.push(user.prettyName());
                                            });
                                            $("#userInfoBigPanel-"+project.id).find("#projectCreator").append(list.join(", "));
                                }});

                               new UserCollection({project:project.id, admin:true}).fetch({
                                       success : function (admin, response) {
                                           $("#userInfoBigPanel-"+project.id).find("#projectAdmins").empty();
                                           var list = [];
                                           admin.each(function(user) {
                                               list.push(user.prettyName());
                                           });
                                           $("#userInfoBigPanel-"+project.id).find("#projectAdmins").append(list.join(", "));

                               }});
                                new UserCollection({project:project.id}).fetch({
                                       success : function (users, response) {
                                           $("#userInfoBigPanel-"+project.id).find("#projectUsers").empty();
                                           var list = [];
                                           users.each(function(user) {
                                               list.push(user.prettyName());
                                           });
                                               $("#userInfoBigPanel-"+project.id).find("#projectUsers").append(list.join(", "));

                               }});
                                new UserCollection({project:project.id,online:true}).fetch({
                                       success : function (users, response) {
                                           $("#userInfoBigPanel-"+project.id).find("#projectUsersOnline").empty();
                                           var list = [];
                                           users.each(function(user) {
                                               list.push('<span style="color:green;font-style: bold;">'+user.prettyName()+'</span>');
                                           });
                                               $("#userInfoBigPanel-"+project.id).find("#projectUsersOnline").append(list.join(", "));

                               }});

                           }, function() {
                        //$(".projectBigInfo").replaceWith("");
                    });
                });

    },
    getFullWidth : function () {
        return Math.round($(window).width() - 90);
    },
    getIndiceNextRow : function(projectPanel,allProjectsPanel) {
        var currentY = projectPanel.position().top;

        var i = 0;
        _.each(allProjectsPanel,function(panel) {
            console.log($(panel).position());
            if($(panel).position().top<=currentY) {
                i++;
            }
        });
        return i;
    },
    generateAddProjectPanel : function () {
        var self = this;
        require([
            "text!application/templates/project/AddProjectPanel.tpl.html"
        ],
                function(tpl) {
                    $(self.projectListElem).append(_.template(tpl, {}));
                });

        return this;
    },
    /**
     * Create search project panel
     */
    loadSearchProjectPanel : function() {


        var self = this;
        //create project search panel
        self.searchProjectPanel = new ProjectSearchPanel({
            model : self.model,
            ontologies : self.ontologies,
            disciplines : self.disciplines,
            el:$("#projectViewNorth"),
            container : self,
            projectsPanel : self
        }).render();
    },

    getOntologiesChoice : function() {
        var ontologies = new Backbone.Collection;
        ontologies.comparator = function(item) {
          return item.get("name");
        };
        _.each(this.model.models, function(project) {
            if(ontologies.get(project.get("ontology"))==undefined)
                ontologies.add({id: project.get("ontology"),name: project.get("ontologyName")});
        });
        return ontologies;

    },
    getDisciplinesChoice : function() {
        var disciplines = new Backbone.Collection;
        disciplines.comparator = function(item) {
          return item.get("name");
        };
        _.each(this.model.models, function(project) {
            if(disciplines.get(project.get("discipline"))==undefined && project.get("discipline")!=null)
                disciplines.add({id: project.get("discipline"),name: project.get("disciplineName")});
        });
        return disciplines;
    },

    /**
     * Print all project panel
     */
    loadProjectsListing : function() {
        var self = this;


        /* Create new Project span */


        self.projectList = new Array();

        //print each project panel
        self.model.each(function(project) {
            var panel = new ProjectPanelView({
                model : project,
                projectsPanel : self,
                container : self
            }).render();
            self.projectList.push(panel);
            $(self.projectListElem).append(panel.el);

        });
    },
    /**
     * Show all project from the collection and hide the other
     * @param projectsShow  Project collection
     */
    showProjects : function(projectsShow) {
        var self = this;
        self.model.each(function(project) {
            //if project is in project result list, show it
            if(projectsShow.get(project.id)!=null)

                $(self.projectListElem+project.id).show();
            else
                $(self.projectListElem+project.id).hide();
        });
    }


});
