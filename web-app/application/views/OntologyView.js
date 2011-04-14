/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var OntologyView = Backbone.View.extend({
    tagName : "div",
    self : this,
    // template : _.template($('#image-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
    },
    uncheckAll : function() {

                     //$('#ontologytree').jstree('check_all');
                     $('#ontologytree').jstree('unchecked_all');

    },
    checkItem : function(iditem) {

                     //$('#ontologytree').jstree('check_all');
                     $('#ontologytree').jstree('get_checked',null,true).each(function () {
                             console.log("check:"+this.id);
                             if(iditem==this.id) $('#ontologytree').jstree('check_node',this);
                     });
                     $('#ontologytree').jstree('get_unchecked',null,true).each(function () {
                            console.log("uncheck:"+this.id);
                            if(iditem==this.id) $('#ontologytree').jstree('check_node',this);
                            //$('#ontologytree').jstree('check_node',this)
                            //69
                     });

    },
    render: function() {
        console.log("OntologyView.render");

        var self = this;
        var tpl = ich.ontologyviewtpl({}, true);
        $(this.el).html(tpl);
        console.log("html");
        this.model.fetch({
            success: function(){
                console.log("Success");
                console.log("Model size=" + self.model.length);

                var json = self.model.toJSON();
                //console.log("json="+json);
                console.log("json="+JSON.stringify(json));


                $("#ontologytreeaddontologybutton").click(function () {
                    console.log("add ontology");
                    //$("#ontologytree").jstree("Add ontology");
                    $("#ontologytree").jstree("create",-1,false,"New ontology",false,false);

                    //var data1 = $("#ontologytree").jstree('get_json',-1);
                    //var data2 = jQuery.jstree._reference("#ontologytree").get_json(-1,false,false);


                    // var json = jQuery.jstree._reference("#ontologytree").get_json(-1);
                    //var jsonString = JSON.stringify(json.ontology);

                    //console.log("test1:"+jsonString);

                    //console.log("test2="+data);
                });

                $("#ontologytreeaddtermbutton").click(function () {
                    console.log("add term");

                    $("#ontologytree").jstree("create");

                    var json = jQuery.jstree._reference("#ontologytree").get_selected();
                    console.log(json);

                });

                $("#ontologytreeselectednode").click(function () {
                    console.log("selected node");
                    var data = $("#ontologytree").jstree('_get_node');
                });


                $("#ontologytreerenamebutton").click(function () {
                    console.log("rename");
                    $("#ontologytree").jstree("rename");
                });

                $("#ontologytreedeletebutton").click(function () {
                    console.log("remove");
                    $("#ontologytree").jstree("remove");
                });

                 $("#ontologytreeprintselectbutton").click(function () {
                    console.log("print");
                    $('#ontologytreedebug').empty();
                    $('#ontologytreedebug').append("ID Checked");
                    $('#ontologytree').jstree('get_checked').each(function () {
                        //console.log("id="+this.id);
                        $('#ontologytreedebug').append("<BR>" +this.id);
                    });

                      $('#ontologytreedebug').append("<BR>" +this.id);


                     //$('#ontologytree').jstree('close_all');

                     self.checkItem('70');

                });


                $("#ontologytree")
                        .bind("open_node.jstree", function(e) { console.log("Last operation " + e.type);})
                    //.bind("before.jstree", function(e) { console.log("Before operation " + e.type);})
                    /**
                     * Add a JSTREE element
                     */
                        .bind("create_node.jstree", function(e, data) {
                    console.log("create_node.jstree");
                    //Check if it's a ontology (level: 0?)
                    console.log("new name:"+ data.rslt.name);
                    console.log("new postion:"+ data.rslt.position);
                    console.log("id parent:"+ data.rslt.parent);
                    //console.log("id :"+ data.rslt.attr("id"));
                    //console.log("class :"+ data.rslt.attr("class"));


                    //console.log("new name:"+ data.state);
                    //if its an ontology: create ontology


                    //else create term


                })

                    /**
                     * Rename a JSTREE element
                     */
                        .bind("rename.jstree", function(e, data) {
                    console.log("old name:"+ data.rslt.old_name);
                    console.log("new name:"+ data.rslt.new_name);
                    console.log("id:"+ data.rslt.obj.attr("id"));
                    console.log("text:"+ data.inst.get_text());

                    var id = data.rslt.obj.attr("id");
                    var classtype = data.rslt.obj.attr("type");
                    //check if ontology or term
                    console.log("Get id:"+ id);
                    console.log("Type: |"+ classtype + "| " + "|" + window.app.models.ontologies.CLASS_NAME +"|");
                    if(classtype==window.app.models.ontologies.CLASS_NAME) {
                        var currentOntology = window.app.models.ontologies.get({id:id}).fetch({success : function () {
                            console.log("old name" + currentOntology.get('name'));
                            currentOntology.set({name:data.rslt.new_name});
                            console.log("new name" + currentOntology.get('name'));
                            currentOntology.save();
                            console.log("save");

                        }
                        });
                    }
                    else
                    {
                        var currentTerm = window.app.models.terms.get({id:id}).fetch({success : function () {
                            console.log("old name" + currentTerm.get('name'));
                            currentTerm.set({name:data.rslt.new_name});
                            console.log("new name" + currentTerm.get('name'));
                            currentTerm.save();
                            console.log("save");

                        }
                        });
                    }
                })
                    /**
                     * Remove a JSTREE element
                     */
                        .bind("remove.jstree", function(event, data) {
                    var id = data.rslt.obj.attr("id");
                    var classtype = data.rslt.obj.attr("type");
                    //check if it's a term or a ontology
                    if(classtype==window.app.models.ontologies.CLASS_NAME) {
                        //if it is an ontology
                        var currentOntology = window.app.models.ontologies.get({id:id}).fetch({success : function () {
                            console.log("remove " + id);
                            currentOntology.destroy({
                                success:function(model, response){alert("OK!")},
                                error:function(model, response){alert("KO!"); $.jstree.rollback(data.rlbk);}});
                        }
                        });
                    } else
                    {
                        //else if it is a term
                        var currentTerm = window.app.models.terms.get({id:id}).fetch({success : function () {
                            console.log("remove " + id);
                            currentTerm.destroy({
                                success:function(model, response){alert("OK!")},
                                error:function(model, response){alert("KO!"); $.jstree.rollback(data.rlbk);}});
                        }
                        });
                    }


                })

                    /**
                     * select a JSTREE element
                     */
                        .bind("select_node.jstree", function(e, data) {
                    var id = data.rslt.obj.attr("id");
                    console.log("id selected:"+id);
                    //check if it's a term or a ontology
                    var classtype = data.rslt.obj.attr("type");
                    if(classtype==window.app.models.ontologies.CLASS_NAME) {
                        //if it is an ontology
                        var currentOntology = window.app.models.ontologies.get({id:id}).fetch({success : function () {
                            console.log("select " + id);
                            console.log(currentOntology.get("name"));
                            $('#ontologytreedebug').empty();
                            $('#ontologytreedebug').append(currentOntology.get("name") + "<BR><BR>");
                            var selectedJson = currentOntology.toJSON();
                            console.log("json="+JSON.stringify(selectedJson));
                            $('#ontologytreedebug').append(JSON.stringify(selectedJson));
                        }
                        });
                    } else
                    {
                        //else if it is a term
                        var currentTerm = window.app.models.terms.get({id:id}).fetch({success : function () {
                            console.log("select " + id);
                            console.log(currentTerm.get("name"));
                            $('#ontologytreedebug').empty();
                            $('#ontologytreedebug').append(currentTerm.get("name") + "<BR><BR>");
                            var selectedJson = currentTerm.toJSON();
                            console.log("json="+JSON.stringify(selectedJson));
                            $('#ontologytreedebug').append(JSON.stringify(selectedJson));
                        }
                        });
                    }


                })

                        .jstree({
                                    "json_data" : {
                                        "data" : json
                                    },
                                    "plugins" : ["json_data", "ui","themes","crrm", "checkbox"]

                                });


            },
            error: function(error){
                for (property in error) {
                    console.log('error:'+property + ":" + error[property]);
                }
            }
        });




        return this;
    }
});
