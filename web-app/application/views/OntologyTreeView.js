/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 13/04/11
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
var OntologyTreeView = Backbone.View.extend({
    tagName : "div",

    events : {
        "click .jstree-checkbox":          "click"
    },

    click : function() {
        console.log("click...");
    },

    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.idImage = options.idImage;
        this.idAnnotation  = null;
    },
    render: function() {
        $(this.el).html(ich.imageontologyviewtpl({}, true));
        this.tree = $(this.el).find('.tree');
        var self = this;

        $(this.el).find('.tree').jstree({
            "json_data" : {
                "data" :this.model.toJSON()
            },
            "plugins" : ["json_data", "ui","themes","crrm", "checkbox"]

        });

        var ontologyPanelWidth = $(this.el).width();
        var ontologyPanelHeight = $(this.el).height();
        $(this.el).draggable({
            drag: function(event, ui) {
                $(this).css("width", ontologyPanelWidth);
                $(this).css("height", ontologyPanelHeight);
            }
        });

        this.initBindings();

        return this;
    },
    clear : function() {
        this.tree.jstree('uncheck_all');
    },
    refresh: function(idAnnotation) {
        var self = this;
        this.removeBindings();
        this.idAnnotation = idAnnotation;
        var refreshTree = function(model , response) {
            self.clear();
            self.tree.jstree('get_unchecked',null,true).each(function () {
                if (model.get(this.id) == undefined) return;
                self.tree.jstree('check_node',this);
            });
        }
        this.initBindings();
        new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:refreshTree});
    },
    getTermsChecked : function() {
        //add annotation-term
        var terms = [];
        this.tree.jstree('get_checked',null,true).each(function () {
            if($(this).attr("type") != window.app.models.terms.CLASS_NAME) return; //not a term node
            terms.push(this.id);
        });
        return terms;
    },
    linkTerm : function(idTerm) {
        new AnnotationTermModel({annotation : this.idAnnotation}).save({annotation : this.idAnnotation, term : idTerm});
    },
    unlinkTerm : function(idTerm) {
        new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).destroy();
    },
    removeBindings : function() {
        this.tree.unbind("check_node.jstree");
        this.tree.unbind("uncheck_node.jstree");
    },
    initBindings : function () {
        var self = this;
        this.tree.bind("check_node.jstree", function(event, data) {

            if (self.idAnnotation == null) return;

            var idTerm = data.rslt.obj.attr("id");
            self.linkTerm(idTerm);


        });
        this.tree.bind("uncheck_node.jstree", function(event, data) {

            if (self.idAnnotation == null) return;

            var idTerm = data.rslt.obj.attr("id");
            self.unlinkTerm(idTerm);
        });
    }
});
