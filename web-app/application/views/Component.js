var Component = Backbone.View.extend({
       tagName: "div",
       views: {},
       initialize: function (options) {
          this.divId = options.divId;
          this.el = options.el;
          this.template = options.template;
          this.buttonAttr = options.buttonAttr;
          if (options.activate != undefined) {
             this.activate = options.activate;
          }
          if (options.deactivate != undefined) {
             this.deactivate = options.deactivate;
          }
          if (options.show != undefined) {
             this.show = options.show;
          }
       },
       render: function () {
          $(this.el).append(this.template);
          if (this.buttonAttr.elButton) {
             this.addToMenu();
          }
          return this;
       },
       addToMenu: function () {
          var self = this;
          require(["text!application/templates/MenuButton.tpl.html"], function(tpl) {
             var button = _.template(tpl,{
                    id: self.buttonAttr.elButton,
                    route: self.buttonAttr.route,
                    text: self.buttonAttr.buttonText
                 }, true);
             $(self.buttonAttr.buttonWrapper).append(button);
             $("#" + self.buttonAttr.elButton).button({
                    icons: {
                       primary: self.buttonAttr.icon
                    }
                 });
             if (self.buttonAttr.click) {
                $("#" + self.buttonAttr.elButton).click(self.buttonAttr.click);
             }
          });
       },
       activate: function () {
          $("#" + this.divId).show();
          $("#" + this.buttonAttr.elButton).addClass("ui-state-disabled");
       },
       deactivate: function () {
          $("#" + this.divId).hide();
          $("#" + this.buttonAttr.elButton).removeClass("ui-state-disabled");
       },
       show: function (view, scope, name) {
          $(scope).find(".title.active").each(function () {
             $(this).removeClass("active");
          });
          $(scope).find("a[name=" + name + "]").addClass("active");
          for (var i in this.views) {
             var v = this.views[i];
             if (v != view) {
                $(v.el).hide();
             }
          }
          $(view.el).show();
       }
    });