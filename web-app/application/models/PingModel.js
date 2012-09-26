var PingModel = Backbone.Model.extend({
    url : function() {
        console.log("url method");
        var base = 'server/ping';
        var format = '.json';
        return base+format;
    }
});
