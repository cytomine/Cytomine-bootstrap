/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var StatsModel = Backbone.Model.extend({

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/term.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});

var StatsProjectSoftwareModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/project/" + this.project + "/software/" + this.software + "/stats.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
    }
});


var StatsRetrievalSuggestionAVGModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/avg.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/avg.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});

var StatsRetrievalSuggestionMatrixModel = Backbone.Model.extend({
    url: function () {
        console.log("StatsRetrievalSuggestionMatrixModel=" + this.project + "#" + this.software + "#" + this.job);
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/confusionmatrix.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/confusionmatrix.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});

var StatsRetrievalSuggestionWorstTermModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/worstTerm.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/worstTerm.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});

var StatsRetrievalSuggestionWorstTermWithSuggest = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/worstTermWithSuggest.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/worstTermWithSuggest.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});


var StatsRetrievalSuggestionWorstAnnotationModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/worstAnnotation.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/worstAnnotation.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});

var StatsRetrievalSuggestionEvolutionModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined) {
            return "api/stats/retrieval/evolution.json?project=" + this.project + "&software=" + this.software;
        } else if (this.job != undefined) {
            return "api/stats/retrieval/evolution.json?job=" + this.job;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
    }
});

var StatsRetrievalEvolutionModel = Backbone.Model.extend({
    url: function () {
        if (this.project != undefined && this.software != undefined && this.term == undefined) {
            return "api/stats/retrieval-evolution/evolution.json?project=" + this.project + "&software=" + this.software;
        } else if (this.project != undefined && this.software != undefined && this.term != undefined) {
            return "api/stats/retrieval-evolution/evolutionByTerm.json?project=" + this.project + "&software=" + this.software + "&term=" + this.term;
        } else if (this.job != undefined && this.term == undefined) {
            return "api/stats/retrieval-evolution/evolution.json?job=" + this.job;
        } else if (this.job != undefined) {
            return "api/stats/retrieval-evolution/evolutionByTerm.json?job=" + this.job + "&term=" + this.term;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.job = options.job;
        this.term = options.term;
    }
});


// define our collection
var StatsTermCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/term.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});

// define our collection
var StatsUserCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/user.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsUserAnnotationCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/userannotations.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsTermSlideCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/termslide.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsUserSlideCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/userslide.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsAnnotationEvolutionCollection = PaginatedCollection.extend({
    model: StatsModel,

    url: function () {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/annotationevolution.json?daysRange=" + this.daysRange + "&term=" + this.term;
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.daysRange = options.daysRange;
        this.term = options.term;
    }
});


