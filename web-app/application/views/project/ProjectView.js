var ProjectView = Backbone.View.extend({
       tagName : "div",
       searchProjectPanelElem : "#searchProjectPanel",
       projectListElem : "#projectlist",
       projectList : null,
       addSlideDialog : null,
        ontologies : null,
       initialize: function(options) {
          this.container = options.container;
          this.model = options.model;
          this.ontologies = options.ontologies;
          this.el = options.el;
          this.searchProjectPanel = null;
          this.addProjectDialog = null;
       },
       render : function () {
          var self = this;
          require([
             "text!application/templates/project/ProjectList.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
              });

          return this;
       },
       doLayout: function(tpl) {
          

          var self = this;
          $(this.el).find("#projectdiv").html(_.template(tpl, {}));

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
          var self = this;
          //TODO: project must be filter by user?
          var idUser =  undefined;
          
          //_.each(self.projectList, function(panel){ panel.refresh(); });
          if(self.addSlideDialog!=null) self.addSlideDialog.refresh();


          new ProjectCollection({user : idUser}).fetch({
                 success : function (collection, response) {
                    self.model = collection;
                    self.render();
                 }});



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
                 el:$("#projectViewNorth"),
                 container : self,
                 projectsPanel : self
              }).render();
       },
       /**
        * Print all project panel
        */
       loadProjectsListing : function() {
          var self = this;
          //clear de list
          $(self.projectListElem).empty();
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
