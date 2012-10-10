/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
var DisciplineModel = Backbone.Model.extend({
    url:function () {
        var base = 'api/discipline';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

// define our collection
var DisciplineCollection = Backbone.Collection.extend({
    model:DisciplineModel,
    CLASS_NAME:"be.cytomine.project.Discipline",
    url:function () {
        return "api/discipline.json";
    },
    initialize:function (options) {
        if (options != undefined) {
            this.light = options.light;
        }
    }, comparator:function (discipline) {
        return discipline.get("name");
    }
});

