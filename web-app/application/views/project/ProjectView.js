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

var ProjectView = Backbone.View.extend({
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
        this.ontologies = this.getOntologiesChoice();
        this.disciplines = this.getDisciplinesChoice();
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectList.tpl.html",
            "text!application/templates/project/NewProjectBox.tpl.html"
        ],
            function (tpl, newProjectBoxTpl) {
                self.doLayout(tpl, newProjectBoxTpl);
            });

        return this;
    },
    doLayout: function (tpl, newProjectBoxTpl) {
        var self = this;
        $(this.el).find("#projectdiv").html(_.template(tpl, {}));
        //clear de list
        $(self.projectListElem).empty();
        $(self.projectListElem).append(_.template(newProjectBoxTpl, {}));
        $("#projectaddbutton").on("click", function () {
            self.showAddProjectPanel();
        });
        //print addProjectPanel
        //self.generateAddProjectPanel();

        //print search panel
        self.loadSearchProjectPanel();

        //print all project panel
        self.loadProjectsListing();

        return this;
    },
    /**
     * Show dialog to add a project
     */
    showAddProjectPanel: function () {

        var self = this;
        $('#addproject').remove();
        self.addProjectDialog = new AddProjectDialog({
            projectsPanel: self,
            el: self.el,
            ontologies: self.ontologies,
            disciplines: self.disciplines
        }).render();
    },
    /**
     * Refresh all project panel
     */
    refresh: function () {
        var self = this;
        var idUser = undefined;

        //_.each(self.projectList, function(panel){ panel.refresh(); });
        if (self.addSlideDialog != null) {
            self.addSlideDialog.refresh();
        }


        new ProjectCollection({user: idUser}).fetch({
            success: function (collection, response) {
                self.model = collection;
                self.render();
            }});
    },
    getFullWidth: function () {
        return Math.round($(window).width() - 90);
    },
    generateAddProjectPanel: function () {
        var self = this;

            require([
                "text!application/templates/project/AddProjectPanel.tpl.html"
            ],
                function (tpl) {
                    $(self.projectListElem).append(_.template(tpl, {}));
                });

            return this;

    },
    /**
     * Create search project panel
     */
    loadSearchProjectPanel: function () {


        var self = this;
        //create project search panel
        self.searchProjectPanel = new ProjectSearchPanel({
            model: self.model,
            ontologies: self.ontologies,
            disciplines: self.disciplines,
            el: $("#projectViewNorth"),
            container: self,
            projectsPanel: self
        }).render();
    },

    getOntologiesChoice: function () {
        var ontologies = new Backbone.Collection;
        ontologies.comparator = function (item) {
            return item.get("name");
        };
        _.each(this.model.models, function (project) {
            if (ontologies.get(project.get("ontology")) == undefined) {
                ontologies.add({id: project.get("ontology"), name: project.get("ontologyName")});
            }
        });
        return ontologies;

    },
    getDisciplinesChoice: function () {
        var disciplines = new Backbone.Collection;
        disciplines.comparator = function (item) {
            return item.get("name");
        };
        _.each(this.model.models, function (project) {
            if (disciplines.get(project.get("discipline")) == undefined && project.get("discipline") != null) {
                disciplines.add({id: project.get("discipline"), name: project.get("disciplineName")});
            }
        });
        return disciplines;
    },

    /**
     * Print all project panel
     */
    loadProjectsListing: function () {
        var self = this;


        /* Create new Project span */


        self.projectList = [];

        //print each project panel

        self.model.each(function (project) {
            var panel = new ProjectPanelView({
                model: project,
                projectsPanel: self,
                container: self
            }).render();
            self.projectList.push(panel);
            $(self.projectListElem).append(panel.el);

        });

        if(!window.app.status.user.model.get("adminByNow")) {
            if(window.app.status.user.model.get('guest')) {
                $("#newProjectListing").remove();
                $(".editProject").remove();
                $(".deleteProject").remove();
                $(".infoProject").remove();
                $(".addSlide").remove();
            }
            else {
                var projectsWhereAdmin;
                $.get("api/user/"+window.app.status.user.id+"/project/light.json?admin=true", function(data) {
                    projectsWhereAdmin = $.map(data.collection, function (a) {
                        return a.id.toString();
                    });

                    $(".editProject").each(function(i, x){
                        x.id = x.id.replace("editProjectButton","");
                        if($.inArray(x.id, projectsWhereAdmin) < 0){
                            x.remove();
                        }
                    });

                    $(".deleteProject").each(function(i, x){
                        x.id = x.id.replace("deleteProjectButton","");
                        if($.inArray(x.id, projectsWhereAdmin) < 0){
                            x.remove();
                        }
                    });
                });
            }
        }
    },
    /**
     * Show all project from the collection and hide the other
     * @param projectsShow  Project collection
     */
    showProjects: function (projectsShow) {
        var self = this;
        self.model.each(function (project) {
            //if project is in project result list, show it
            if (projectsShow.get(project.id) != null) {
                $(self.el).find(self.projectListElem + project.id).show();
            }
            else {
                $(self.el).find(self.projectListElem + project.id).hide();
            }
        });
    }


});
