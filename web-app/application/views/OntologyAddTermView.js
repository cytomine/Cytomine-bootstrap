var OntologyAddTermView = Backbone.View.extend({
       ontologyPanel : null,
       addOntologyDialog : null,
        parents : null,
       initialize: function(options) {
          this.container = options.container;
          this.ontologyPanel = options.ontologyPanel;
           this.parents = options.parents;
          _.bindAll(this, 'render');
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/ontology/OntologyAddTermDialog.tpl.html"
          ],
              function(ontologyAddTermDialogTpl) {
                 self.doLayout(ontologyAddTermDialogTpl);
              });
          return this;
       },
       doLayout : function(ontologyAddTermDialogTpl) {

          var self = this;
          var dialog = _.template(ontologyAddTermDialogTpl, {});
          $(self.el).append(dialog);

           console.log(self.parents);
           $("#termparent").empty()
            _.each(self.parents, function(parent){
                $("#termparent").append("<option value=\""+ parent.data.id +"\">"+parent.data.title +"</option>");
            });



          $("#form-add-ontology-term").submit(function () {self.createOntologyTerm(); return false;});
          $("#form-add-ontology-term").find("input").keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $("#form-add-ontology-term").submit();
                return false;
             }
          });

        console.log("$('#colorSelector')="+$('#colorSelector').length);
        $('#colorSelector').ColorPicker({
            color: '#0000ff',
            onShow: function (colpkr) {
                $(colpkr).fadeIn(500);
                return false;
            },
            onHide: function (colpkr) {
                $(colpkr).fadeOut(500);
                return false;
            },
            onChange: function (hsb, hex, rgb) {
                $('#colorSelector div').css('backgroundColor', '#' + hex);
            }
        });



          //Build dialog
          console.log("AddOntologyTermDialog: build dialog:"+$("#dialog-add-ontology-term").length);
          self.addOntologyDialog = $("#dialog-add-ontology-term").dialog({
                 width: 500,
                 autoOpen : false,
                 modal:true,
                 buttons : {
                    "Save" : function() {
                       $("#form-add-ontology-term").submit();
                    },
                    "Cancel" : function() {
                       $("#dialog-add-ontology-term").dialog("close");
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
          self.clearAddOntologyTermPanel();
          self.addOntologyDialog.dialog("open") ;
       },
       clearAddOntologyTermPanel : function() {
          var self = this;
          $("#ontologytermerrormessage").empty();
          $("#ontologytermerrorlabel").hide();
          $("#termname").val("");

       },
       createOntologyTerm : function() {
          console.log("createOntologyTerm...");
          var self = this;

          $("#ontologytermerrormessage").empty();
          $("#ontologytermerrorlabel").hide();

          var name =  $("#termname").val();
          var idParent = $("#termparent").val();
          console.log("create "+ name + " with parent " + $("#termparent").val());

          console.log(window.app.models.ontologies.get(47));
          console.log(window.app.models.ontologies.get(470));


           new TermModel({name : name, ontology : self.model.id}).save({name : name, ontology :self.model.id,color:"#123456"},{
                     success: function (model, response) {
                        console.log(response);
                        var id = response.project.id;
                        console.log("project="+id);

                         //TODO: link term with its parent!

                         self.ontologyPanel.refresh();
                         $("#dialog-add-ontology-term").dialog("close") ;
                     },
                     error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        console.log("json.project="+json.errors);

                        $("#ontologytermerrorlabel").show();

                        console.log($("#ontologytermerrormessage").append(json.errors));
                     }
                  }
              );




           if(window.app.models.ontologies.get(idParent)==undefined) {
               //parent is not an ontology but a term, so link with the term



           }




       }
    });