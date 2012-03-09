var LaunchJobView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    parent : null,
    initialize: function(options) {
        this.width = options.width;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobLaunch.tpl.html"
        ],
               function(JobLaunchTpl) {
                   self.loadResult(JobLaunchTpl);
               });
        return this;
    },
    loadResult : function (JobLaunchTpl) {
        var self = this;
        var content = _.template(JobLaunchTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({ width: width, height: height, modal:true });
        self.printSoftwareParams();
    },
    printSoftwareParams : function() {
        var self = this;
        $(self.el).find("#printSoftwareParams").append(self.software.get('name'));

        _.each(self.software.get('parameters'), function(param) {
            console.log(param);
            $(self.el).find("#printSoftwareParams").append('<br/>' + param.name + "=" + self.getEntryByType(param.type, param.defaultParamValue));
        });

    },
    getEntryByType : function(type, defaultValue) {
        console.log("defaultValue=" + defaultValue);
        if (type == "String") {
            return '<input type="text" class="span3" value="' + defaultValue + '">';
        }
        if (type == "Number") {

            return '<input type="text" class="span3" value="' + defaultValue + '">';
        }
        if (type == "Boolean") {
            var defaultElem = "";
            console.log("defaultValue.toLowerCase()=" + defaultValue.toLowerCase());
            if (defaultValue.toLowerCase() == "true") defaultElem = 'checked="checked"'
            return '<input type="checkbox" class="span3" ' + defaultElem + ' />';
        }

    }
});