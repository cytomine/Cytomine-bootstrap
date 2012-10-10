/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var AnnotationListView = Backbone.View.extend({
    tagName:"div",
    self:this,
    alreadyBuild:false,
    initialize:function (options) {
        this.container = options.container;
        this.idAnnotation = options.idAnnotation;
    },

    render:function () {
        var self = this;
        require([
            "text!application/templates/annotation/AnnotationList.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });

        return this;
    },
    doLayout:function (tpl) {


        var self = this;
        $(this.el).html(_.template(tpl, {name:"name", area:"area"}));

        self.model.each(function (annotation) {
            //$("#annotationList").append(annotation.get('name') + " <br>");
            var name = annotation.get('name');
            var area = annotation.get('area');
            //$("#tableImage").append("<tr><th>"+ name +"</th><th>" + area + "</th></tr>");

        });
        // $('#tableImage').dataTable();

        var grid;
        var i = 0;
        var data = [];
        self.model.each(function (image) {
            data[i] = {
                id:image.id,
                filename:image.get('filename'),
                created:''
            };
            i++;
        });
        return this;
    },
    /**
     * Init annotation tabs
     */
    initAnnotation:function () {
        var self = this;


    }
});
