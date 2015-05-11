/**
 * Created by hoyoux on 03.11.14.
 */
var SearchEngineFiltersView = Backbone.View.extend({

    tagName: 'select',
    className: 'input-xlarge focused',
    id: 'filterlist',
    initialize: function (options) {
        if(options !== undefined) {
            // Since 1.1.0, Backbone Views no longer automatically attach options passed to the constructor as this.options
            this.options = options;
            this.selectCallback = options.selectCallback;
            this.name = options.name;
        }
    },
    events: {
        change: function(e) {
            var target = e.currentTarget.options[e.currentTarget.options.selectedIndex];
            var value = target.value;
            var name = target.text;
            if((value !== null && value !== undefined && value !== "") || (name !== null && name !== undefined && name !== "")) {
                this.selectCallback(name, value);
            }
        }
    },
    render: function () {
        console.log("render");
        var self = this;
        console.log("render collection");

        this.collection.each(function(filter){
            var filterView = new SearchEngineFilterView({ model: filter });
            self.$el.append(filterView.render().el);
        });
        // returning this for chaining..
        return this;
    }
});

/**
 * Created by hoyoux on 03.11.14.
 */
var SearchEngineFilterView = Backbone.View.extend({

    tagName: 'option',
    render: function () {
        console.log("render");
        this.$el.attr('value', this.model.get( 'id' )).html(this.model.get("name"));
        // returning this for chaining..
        return this;
    }
});