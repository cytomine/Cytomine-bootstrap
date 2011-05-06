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

        var dialog = ich.addprojectdialogtpl({});
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
            var choice = ich.ontologieschoiceradiotpl({id:ontology.id,name:ontology.get("name")}, true);
            $("#projectontology").append(choice);
        });



        $("#projectuser").empty();
        window.app.models.users.each(function(user){
            var choice = ich.userschoicetpl({id:user.id,username:user.get("username")}, true);
            $("#projectuser").append(choice);
        });

        //Build dialog
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
        return this;

    },
    refresh : function() {

    },
    open: function() {
        var self = this;
        self.clearAddProjectPanel();

        console.log("open");
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
        $("#errormessage").empty();
        $("#projecterrorlabel").hide();

        var self = this;
        var name =  $("#project-name").val();
        var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
        var users = new Array();

        $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
            users.push($(item).attr("value"))
        });

        new ProjectModel({name : name, ontology : ontology}).save({name : name, ontology : ontology},{
            success: function (model, response) {
                console.log(response);
                //var json = $.parseJSON(response);
                var id = response.project.id;
                console.log("project="+id + " user="+users[0])
                new ProjectUserModel({project: id}).save({project: id, user: users},{
                    success: function (model, response) {
                        new ProjectCollection({user : self.userID}).fetch({
                            success : function (collection, response) {
                                self.projectsPanel.printProjects(collection);
                                $("#addproject").dialog("close") ;
                            }});
                    }});

                //$("#addproject").dialog("destroy") ;


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