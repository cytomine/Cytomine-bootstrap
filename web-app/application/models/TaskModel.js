/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var TaskModel = Backbone.Model.extend({

    url:function () {
        var base = 'api/task';
        var format = '.json';
        if (this.isNew()) return base + format;
        var url = base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
        if(this.project) return url + "?project="+this.project;
        return url;
    },
    initialize:function (options) {
        this.project = options.project;
    }
});
