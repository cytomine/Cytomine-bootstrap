var DraggablePanelView = Backbone.View.extend({
    tagName:"div",
    initialize:function (options) {
        this.iPad = ( navigator.userAgent.match(/iPad/i) != null );
        this.el = options.el;
        this.template = options.template;
        this.className = options.className;
    },
    render:function () {
        var self = this;
        console.log("DraggablePanelView.render");
        $(this.el).html(this.template);
        //put above the main menu
        $(this.el).css("z-index", "1000000");
        if (this.iPad) return this;
        var width = null;
        var height = null;
        var minizedWidth = null;
        var minizedHeight = null;
        var windowWidth = $(window).width();
        var minimized = false;
        var dragInProgress = false;
        var minimize = function (el, context) {
            var minizedEl = $("." + self.className).find(".minimized");
            $("." + self.className).children().hide();
            minizedEl.show();
            minizedWidth = Math.max(58, minizedEl.width());
            minizedHeight = Math.max(18, minizedEl.height());
            resizeDraggablePanel(el);
        };

        var maximize = function (el, context) {
            var minizedEl = $("." + self.className).find(".minimized");
            $("." + self.className).children().show();
            $("." + self.className).css("padding-left", "0px");
            minizedEl.hide();
            resizeDraggablePanel(el);
        };

        var resizeDraggablePanel = function (el) {
            if (minimized) {
                $(el).css("width", minizedWidth);
                $(el).css("height", minizedHeight);
            } else {
                $(el).css("width", width);
                $(el).css("height", height);
            }
        }

        var mousenterDraggablePanel = function (context) {
            if (dragInProgress) return;
            if (minimized) {
                window.app.view.hideFloatingPanels();
                maximize(context);
                var floatingPanel = $("." + context.className);
                floatingPanel.show();
                floatingPanel.css("width", width);
                floatingPanel.css("height", width);
            }
        }

        var mouseleaveDraggablePanel = function (context) {
            if (dragInProgress) return;
            if (minimized) {
                window.app.view.showFloatingPanels();
                minimize(context);
                var floatingPanel = $("." + context.className);
                floatingPanel.css("width", minizedWidth);
                floatingPanel.css("height", minizedHeight);
            }
        }

        var drag = function (el, position, context) {

        }

        var start = function (el, position, context) {
            dragInProgress = true;
            $(el).css("overflow", "hidden");
            /*window.app.view.showFloatingPanels();
             if (!minimized) {
             width = $(context.el).width();
             height = $(context.el).height();
             } else {
             minizedWidth =  $(context.el).width();
             minizedHeight = $(context.el).height();
             }
             windowWidth = $(window).width();
             if (minimized) {
             maximize(el, context);
             }*/
        }

        var stop = function (el, position, context) {
            dragInProgress = false;
            /*var leftPositionValue = parseInt(position.left);
             if (!minimized && leftPositionValue <= 0) {  //left minimize
             minimized = true;
             minimize(el, context);
             } else if (leftPositionValue > 0 && minimized) {
             minimized = false;
             maximize(el, context);
             }*/
            $(el).css("overflow", "auto");
            var leftPosition = Math.max(0, parseInt(position.left));
            var topPosition = Math.max(0, parseInt(position.top));
            if (context.className) window.app.view.updatePosition(self.className, { left:leftPosition, top:topPosition}, false);

        }

        $(this.el).draggable({
            cancel:'input select olControlOverviewMapElement',
            handle:'.dragHandle',
            start:function (event, ui) {
                start(this, ui.helper.position(), self);
            },
            stop:function (event, ui) {
                stop(this, ui.helper.position(), self);
            },
            drag:function (event, ui) {
                drag(this, ui.helper.position(), self);
            }
        });
        $(this.el).on("mouseenter", function () {
            mousenterDraggablePanel(self);
        });
        $(this.el).on("mouseleave", function () {
            mouseleaveDraggablePanel(self);
        });
        $(this.el).draggable("option", "opacity", 0.35);


        $("." + this.className).bind("movedProgramatically", function (e) {
            var left = $(self.el).css("left");
            left = (left == undefined || left == "auto") ? undefined : parseInt(left.substr(0, left.length - 2));
            var right = $(self.el).css("right");
            right = (right == undefined || right == "auto") ? undefined : parseInt(right.substr(0, right.length - 2));
            var top = $(self.el).css("top");
            top = (top == undefined || top == "auto") ? undefined : parseInt(top.substr(0, top.length - 2));
            var bottom = $(self.el).css("bottom");
            bottom = (bottom == undefined || bottom == "auto") ? undefined : parseInt(bottom.substr(0, bottom.length - 2));

            var panelWidth = $("." + self.className).width();
            var panelHeight = $("." + self.className).height();
            var windowWidth = $(window).width();
            var windowHeight = $(window).height();

            var position = undefined;
            if (left != undefined && top != undefined) {
                position = { left:left, top:top };
            }
            if (left != undefined && bottom != undefined) {
                position = { left:left, top:windowHeight - (bottom + panelHeight) };
            } else if (right != undefined && top != undefined) {
                position = { left:windowWidth - ( right + panelWidth), top:top};
            } else if (right != undefined && bottom != undefined) {
                position = { left:windowWidth - ( right + panelWidth), top:windowHeight - (bottom + panelHeight) };
            }

            if (position != undefined) {
                start($("." + self.className), position, self);
                drag($("." + self.className), position, self);
                stop($("." + self.className), position, self);
            }

        });

        return this;
    }
});