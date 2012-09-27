var PingModel = Backbone.Model.extend({
    url : function() {
        var base = 'server/ping';
        var format = '.json';
        return base+format;
    }
});
