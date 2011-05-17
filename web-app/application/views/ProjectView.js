var ProjectView = Backbone.View.extend({
    tagName : "div",
    model : null,
    el : null,
    container : null,
    searchProjectPanel : null,
    searchProjectPanelElem : "#searchProjectPanel",
    projectListElem : "#projectlist",
    addProjectDialog : null,

    initialize: function(options) {
        this.container = options.container;
        this.model = options.model;
        this.el = options.el;
    },
    render: function() {
        console.log("ProjectView: render");

        var self = this;
        $(this.el).html(ich.projectsviewtpl({}, true));

        //print search panel
        self.loadSearchProjectPanel();

        //print all project panel
        self.loadProjectsListing();

        return this;
    },
    /**
     * Refresh all project panel
     */
    refresh : function() {
        console.log("ProjectView: refresh");
        //TODO: project must be filter by user?
        var idUser =  undefined;
        new ProjectCollection({user : idUser}).fetch({
            success : function (collection, response) {
                self.model = collection;
                this.render();
            }});


    },
    /**
     * Create search project panel
     */
    loadSearchProjectPanel : function() {
        console.log("ProjectView: searchProjectPanel");

        var self = this;
        //create project search panel
        self.searchProjectPanel = new ProjectSearchPanel({
            model : self.model,
            ontologies : window.app.models.ontologies,
            el:$("#projectViewNorth"),
            container : self
        }).render();
    },
    /**
     * Print all project panel
     */
    loadProjectsListing : function() {
        var self = this;
        //clear de list
        $(self.projectListElem).empty();

        //print each project panel
        self.model.each(function(project) {
            var panel = new ProjectPanelView({
                model : project,
                projectsPanel : self
            }).render();

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
