/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
var DescriptionModel = Backbone.Model.extend({
    url: function () {
        return 'api/domain/'+ this.domainClassName + '/' + this.domainIdent + '/description.json'
    },
    initialize: function (options) {
        console.log("options.id="+options.id);
        this.domainIdent = options.domainIdent;
        this.domainClassName = options.domainClassName;
    }
});


