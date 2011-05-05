/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 5/05/11
 * Time: 8:20
 * To change this template use File | Settings | File Templates.
 */
var BeginTransactionModel = Backbone.Model.extend({

	url : function() {
		var base = 'transaction/begin';
		var format = '.json';
		return base + format;
	}
});

var EndTransactionModel = Backbone.Model.extend({

	url : function() {
		var base = 'transaction/end';
		var format = '.json';
		return base + format;
	}
});