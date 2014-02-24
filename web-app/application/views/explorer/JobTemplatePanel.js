/**
 * Created with IntelliJ IDEA.
 * User: lrollus
 * Date: 20/02/14
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */

var JobTemplatePanel = SideBarPanel.extend({
    tagName: "div",
    currentAnnotation : null,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/JobTemplatePanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    refresh: function() {
    },
    changeAnnotation : function(idAnnotation) {
        var self = this;
        self.currentAnnotation = idAnnotation;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        panel.find(".jobTemplateInfo").empty();
        panel.find(".jobTemplateInfo").append("Annotation " + idAnnotation + "<br/>");
        panel.find(".jobTemplateInfo").append('<img src="'+window.location.origin+'/api/annotation/'+idAnnotation+ '/crop.png?max_size=128" />');

    },
    linkTemplateToAnntation : function() {
        var self = this;

        var jobTemplate = $('input[name=groupJobTemplate'+self.model.id+']:checked').val();

        new JobTemplateAnnotationModel({annotationIdent: self.currentAnnotation, jobTemplate:jobTemplate}).save({}, {
                success: function (model, response) {
                    window.app.view.message("Job", response.message, "success");
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Job", json.errors, "error");
                }
            }
        );
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        var content =_.template(tpl, {});
        panel.html(content);
        var elContent1 = panel.find(".JobTemplateContent1");
        var sourceEvent1 = panel.find(".toggle-content1");
        this.initToggle(panel, elContent1, sourceEvent1, "JobTemplateContent1");
        var list = panel.find(".jobTemplateList");
        list.empty();

        new JobTemplateCollection({project: self.model.get('project')}).fetch({
            success: function (collection, response) {
                //get all project id/name
                var softwares = {};
                collection.each(function(jobTemplate) {
                    softwares[jobTemplate.get('software')]=jobTemplate.get('softwareName');
                });

                //create div for each software
                for (var prop in softwares) {
                    if (softwares.hasOwnProperty(prop)) {
                        list.append('<ul class="'+prop+'">'+softwares[prop]+'</ul>');
                    }
                }

                //during each, add the template under the good software

                collection.each(function(jobTemplate) {
                    var str = '<li><input type="radio" name="groupJobTemplate'+self.model.get('id')+'" value="'+jobTemplate.get('id')+'"> '+jobTemplate.get('name')+'</li>';
                    list.find("."+jobTemplate.get('software')).append(str);
                });
            }
        });

        panel.find("button.Launch").click(function() {
            self.linkTemplateToAnntation();
        })

    }
});
