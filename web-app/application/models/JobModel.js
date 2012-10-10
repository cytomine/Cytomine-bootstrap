/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var JobModel = Backbone.Model.extend({
    url:function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/project/" + this.project + "/job.json?software=" + this.software;
        } else {
            var base = 'api/job';
            var format = '.json';
            if (this.isNew()) return base + format;
            return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
        }
    },
    initialize:function (options) {
        this.project = options.project;
        this.software = options.software;
        this.light = options.light;
        this.max = options.max;
    },
    isNotLaunch:function () {
        return (this.get('status') == 0)
    },
    isInQueue:function () {
        return (this.get('status') == 1)
    },
    isRunning:function () {
        return (this.get('status') == 2)
    },
    isSuccess:function () {
        return (this.get('status') == 3)
    },
    isFailed:function () {
        return (this.get('status') == 4)
    },
    isIndeterminate:function () {
        return (this.get('status') == 5)
    },
    isWait:function () {
        return (this.get('status') == 6)
    }
});

// define our collection
var JobCollection = Backbone.Collection.extend({
    model:JobModel,

    url:function () {
        if (this.project != undefined && this.software != undefined) {
            var l = this.light == undefined ? "" : "&light=" + this.light;
            var m = this.max == undefined ? "" : "&max=" + this.max;
            return "api/project/" + this.project + "/job.json?software=" + this.software + l + m;
        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/job.json";
        } else {
            return "api/job.json";
        }
    },
    initialize:function (options) {
        this.project = options.project;
        this.software = options.software;
        this.light = options.light;
        this.max = options.max;
    },
    comparator:function (job) {
        return -job.get("id");
    }
});
