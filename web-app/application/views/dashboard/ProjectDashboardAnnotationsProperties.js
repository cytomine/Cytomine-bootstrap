var ProjectDashboardAnnotationsProperties = Backbone.View.extend({

    idAnnotationPropertyCollection : null,
    idAnnotationCollection: null,

    initialize:  function (options) {
        this.idAnnotation = options.idAnnotation;
    },

    render: function () {

        var self = this;

        require(["text!application/templates/dashboard/AnnotationProperties.tpl.html"], function (propertiesTpl) {
            self.doLayout(propertiesTpl);
        });
    },

    refresh: function (annotations) {
        var self = this;
        var select = $(this.el).find("#AnnotationSelect");

        console.log("Passe par le refresh! + AnnotationId: "+ annotations);

        if (annotations != null) {
            console.log("Pour select idAnnot ?! +" + annotations);
            select.val(annotations);
        }

        self.initTableAnnotation();
    },

    doLayout: function (propertiesTpl) {

        var self = this;
        console.log("doLayout");

        var content = _.template(propertiesTpl, {id:self.model.id, name: self.model.get("name")});
        $("#tabs-annotationsproperties-"+self.model.id).append(content);

        $("#addAnnotationProperty").click(function() {
            console.log("click button add");
            self.addPropertyTable();
        });
        $("#AnnotationSelect").click(function() {
            console.log("click select");
            //self.initTableAnnotation();
            self.redirectAnnotation();
        });
        $("#deleteAnnotationProperty").click(function() {
            console.log("click button delete");
            self.deleteAnnotationProperty();
        });

        self.initAnnotationSelect(self.idAnnotation);
    },

    initAnnotationSelect: function (idAnnotation) {
        var self = this;
        var select = $(this.el).find("#AnnotationSelect");
        select.empty();
        select.attr("disabled", "disabled");
        new AnnotationCollection({project: self.model.id}).fetch({
            success: function (collection, response) {
                idAnnotationCollection = collection;
                if (_.size(collection) > 0) {
                    $(select).removeAttr("disabled");
                }
                var isSelected = true
                collection.each(function(options) {
                    var date = new Date(options.get('created'));
                    var option = _.template("<option value='<%= id %>'><%= value %> - <%= created %></option>", { id : options.get('id'), value : options.get('id'), created: date});
                    select.append(option);
                });

                if (idAnnotation != null) {
                    console.log("Pour select idAnnot ?! +" + idAnnotation);
                    select.val(idAnnotation);
                }

                self.initTableAnnotation();
            }
        });
    },

    redirectAnnotation: function () {
        window.location.href ="#tabs-annotationsproperties-" + window.app.status.currentProject + "-" + $("#AnnotationSelect").val();
    },

    initTableAnnotation : function () {
        var tbody = $(this.el).find("#tableAnnotationProperty");
        tbody.empty();
        var idAnnotation = $("#AnnotationSelect").val();
        var i = 0;

        //Add Image
        idAnnotationCollection.each(function(options) {
            if (idAnnotation == options.get('id')) {
                $("#imageSelect").attr("src",options.get('cropURL'));
                $("#loadImageFromAP").attr("href","#tabs-image-" + window.app.status.currentProject + "-" + options.get('image') + "-");
            }
        });

        var drawOption = function(model) {
            var option = _.template("<tr class='ligne<%= id %>' id='<%= id %>'><td class='key<%= id %>' id='<%= id %>'><%= key %></td>" +
                "<td class='value<%= id %>' id='<%= id %>'><%= value %></td>" +
                "<td><input type='checkbox'  id='checkbox<%= id %>'></td></tr>", {id : model.get('id'), key : model.get('key'), value : model.get('value')});
            tbody.append(option);
        }

        new AnnotationPropertyCollection({idAnnotation: idAnnotation }).fetch({
            success: function (collection, response) {
                idAnnotationPropertyCollection = collection;
                collection.each(function(model) {
                    var idForm = "annotationPropertyForm" + i;
                    drawOption(model);

                    $("td.key"+model.id).dblclick(function () {
                        var formEdit = _.template("<form id='<%= idForm %>'><input value='<%= key %>' type='text' id='input_NewKey'></form>", {key : model.get('key'), idForm : idForm});
                        $(this).html(formEdit);
                        var td = $(this);

                        $("#"+idForm).submit(function() {
                            model.save({ key : $("#input_NewKey").val()}, {
                                success: function (model, response) {
                                    td.empty();
                                    td.html(model.get('key'));
                                    return false;
                                },
                                error: function (model, response) {
                                    //alert(model.get('key') + ": Wrong Key!");
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Annotations Property", json.errors, "error");
                                }
                            });
                        });
                    });
                    i++;

                    $("td.value"+model.id).dblclick(function () {
                        var formEdit = _.template("<form id='<%= idForm %>'><input value='<%= value %>' type='text' id='input_NewValue'></form>", {value : model.get('value'), idForm : idForm});
                        $(this).html(formEdit);
                        var td = $(this);

                        $("#"+idForm).submit(function() {
                            model.save({ value : $("#input_NewValue").val()}, {
                                success: function (model, response) {
                                    td.empty();
                                    td.html(model.get('value'));
                                    return false;
                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Annotations Property", json.errors, "error");
                                    //alert(model.get('value') + ": Wrong Value!");
                                }
                            });
                        });
                    });
                    i++;
                });
            }
        });
    },

    addPropertyTable: function() {
        if ($("#input_key").val() != "" && $("#input_value").val() != "") {
            var id;

            //create annotationProperty
            var annotationProperty = new
                AnnotationPropertyModel({annotationIdent : $("#AnnotationSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}).save({annotationIdent : $("#AnnotationSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}, {
                success: function (model, response) {
                    id = model.get('id');
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotations Property", json.errors, "error");
                }
            });

            var table = _.template("<tr class='ligne<%= id %>' id='<%= id %>'><td class='key<%= id %>' id='<%= id %>'><%= key %></td>" +
                "<td class='value<%= id %>' id='<%= id %>'><%= value %></td>" +
                "<td><input type='checkbox' id='checkbox<%= id %>'></td></tr>", {id : id, key : $("#input_key").val(), value : $("#input_value").val()});
            $("#tableAnnotationProperty").append(table);
        }
    },

    deleteAnnotationProperty: function () {
        idAnnotationPropertyCollection.each(function(model) {
            if ($("#checkbox"+model.id).is(':checked')) {
                console.log("Checked ?" + model.id);
                model.destroy({
                    success: function (model, response) {
                        window.app.view.message("Annotations Property", response.message, "success");
                        $("tr.ligne"+model.id).empty();
                    },
                    error : function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotations Property", json.errors, "error");
                    }
                });
            }
        });
    }
});