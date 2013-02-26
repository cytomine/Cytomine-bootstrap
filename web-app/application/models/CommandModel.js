/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/05/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var CommandModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/command';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


// define our collection
var CommandCollection = PaginatedCollection.extend({
    model: CommandModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/last/" + this.max + ".json";
        } else {
            return "api/command.json";
        }
    },
    initialize: function (options) {
        if (!options) {
            return;
        }
        if (options.project != undefined) {
            this.project = options.project;
        }
        if (options.max != undefined) {
            this.max = options.max;
        }
    }
});

CommandCollection.comparator = function (command) {
    return command.get("created");
};