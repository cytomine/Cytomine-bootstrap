var AddProjectDialog = Backbone.View.extend({
       projectsPanel : null,
       addProjectDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.projectsPanel = options.projectsPanel;
          _.bindAll(this, 'render');
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/project/ProjectAddDialog.tpl.html",
             "text!application/templates/project/OntologiesChoicesRadio.tpl.html",
             "text!application/templates/project/UsersChoices.tpl.html"
          ],
              function(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl) {
                 self.doLayout(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl);
              });
          return this;
       },
       doLayout : function(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl) {

          var self = this;
          var dialog = _.template(projectAddDialogTpl, {});
          $(self.el).append(dialog);

          $("#login-form-add-project").submit(function () {self.createProject(); return false;});
          $("#login-form-add-project").find("input").keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-project").submit();
                return false;
             }
          });


          $("#projectontology").empty();
          window.app.models.ontologies.each(function(ontology){
             var choice = _.template(ontologiesChoicesRadioTpl, {id:ontology.id,name:ontology.get("name")});
             $("#projectontology").append(choice);
          });

          $("#projectuser").empty();
          window.app.models.users.each(function(user){
             var choice = _.template(usersChoicesTpl, {id:user.id,username:user.get("username")});
             $("#projectuser").append(choice);
          });

          //Build dialog
          console.log("AddProjectDialog: build dialog");
          self.addProjectDialog = $("#addproject").dialog({
                 width: 500,
                 autoOpen : false,
                 modal:true,
                 buttons : {
                    "Save" : function() {
                       $("#login-form-add-project").submit();
                    },
                    "Cancel" : function() {
                       $("#addproject").dialog("close");
                    }
                 }
              });
          self.open();
          return this;

       },
       refresh : function() {
       },
       open: function() {
          var self = this;
          self.clearAddProjectPanel();
          self.addProjectDialog.dialog("open") ;
       },
       clearAddProjectPanel : function() {
          var self = this;
          $("#errormessage").empty();
          $("#projecterrorlabel").hide();
          $("#project-name").val("");

          $(self.addProjectCheckedOntologiesRadioElem).attr("checked", false);
          $(self.addProjectCheckedUsersCheckboxElem).attr("checked", false);
       },
       createProject : function() {
          console.log("createProject...");
          var self = this;

          $("#errormessage").empty();
          $("#projecterrorlabel").hide();

          var name =  $("#project-name").val();
          var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
          var users = new Array();

          $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
             users.push($(item).attr("value"))
          });

          //create project
          new ProjectModel({name : name, ontology : ontology}).save({name : name, ontology : ontology},{
                 success: function (model, response) {
                    console.log(response);
                    var id = response.project.id;
                    console.log("project="+id);
                    //create user-project "link"
                    new ProjectUserModel({project: id}).save({project: id, user: users},{
                           success: function (model, response) {
                              new ProjectCollection({user : self.userID}).fetch({
                                     success : function (collection, response) {
                                        self.projectsPanel.refresh();
                                        $("#addproject").dialog("close") ;
                                     }});
                           }});
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    console.log("json.project="+json.errors);

                    $("#projecterrorlabel").show();

                    console.log($("#errormessage").append(json.errors));
                 }
              }
          );
       }
    });