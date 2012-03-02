var DraggablePanelView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.iPad = ( navigator.userAgent.match(/iPad/i) != null );
        this.el = options.el;
        this.template = options.template;
        this.className = options.className;
    },
    render: function() {
        var self = this;
        $(this.el).html(this.template);
        if (this.iPad) return this;
        var width = $(this.el).width();
        var height = $(this.el).height();
        var minizedWidth = width;
        var minizedHeight = height;
        var windowWidth = $(window).width();
        var minimized = false;

        var minimize = function() {
            var minizedEl = $("."+self.className).find(".minimized");
            $("."+self.className).children().hide();
            minizedEl.show();
            minizedWidth = Math.max(58, minizedEl.width());
            minizedHeight = Math.max(18, minizedEl.height());
        };

        var maximize = function() {
            var minizedEl = $("."+self.className).find(".minimized");
            $("."+self.className).children().show();
            $("."+self.className).css("padding-left", "0px");
            minizedEl.hide();
        };

        var resizeDraggablePanel = function(el) {
            if (minimized) {
                $(el).css("width", minizedWidth);
                $(el).css("height", minizedHeight);
            } else {
                $(el).css("width", width);
                $(el).css("height", height);
            }
        }

        var mousenterDraggablePanel = function(context) {
            if (minimized) {
                maximize();
                $("."+context.className).css("width", width);
                $("."+context.className).css("height", height);
                $("."+context.className).css("padding-left", "80px");
            }
        }

        var mouseleaveDraggablePanel = function(context) {
            if (minimized) {
                minimize();
                $("."+context.className).css("padding-left", "0px");
                $("."+context.className).css("width", minizedWidth);
                $("."+context.className).css("height", minizedHeight);

            }
        }

        var drag = function(el, position, context) {
            var leftPositionValue = parseInt(position.left);
            if (!minimized && leftPositionValue <= 0) {  //left minimize
                minimized = true;
                minimize();
                resizeDraggablePanel(el);
            } else if (leftPositionValue > 0 && minimized) {
                minimized = false;
                maximize();
                resizeDraggablePanel(el);
            }
        }

        var start = function(el, position, context) {
            $(el).css("overflow", "hidden");
            if (!minimized) {
                width = $(context.el).width();
                height = $(context.el).height();
            }
            windowWidth = $(window).width();
            $(context.el).off("mouseenter", function(){
                mousenterDraggablePanel(self);
            });
            $(context.el).off("mouseleave", function(){
                mouseleaveDraggablePanel(self);
            });
        }

        var stop = function(el, position, context) {
            $(el).css("overflow", "auto");
            var leftPosition = Math.max(0, parseInt(position.left));
            var topPosition = Math.max(0, parseInt(position.top));
            if (context.className) window.app.view.updatePosition(self.className, { left : leftPosition, top : topPosition}, false);
            $(context.el).on("mouseenter", function(){
                mousenterDraggablePanel(self);
            });
            $(context.el).on("mouseleave", function(){
                mouseleaveDraggablePanel(self);
            });
            resizeDraggablePanel(el);
        }

        $(this.el).draggable({
            cancel: 'input olControlOverviewMapElement',
            handle: '.dragHandle',
            start: function(event, ui) {
                start(this, ui.helper.position(), self);

            },
            stop : function(event, ui) {
                stop(this,  ui.helper.position(), self);
            },
            drag: function (event, ui) {
                drag(this, ui.helper.position(), self);
            }
        });
        $(this.el).draggable( "option", "opacity", 0.35 );

        $(this.el).bind("movedProgramatically", function(e){
            var left = $(self.el).css("left");
            left = (left == undefined || left == "auto") ? undefined : parseInt(left.substr(0, left.length - 2));
            var right = $(self.el).css("right");
            right = (right == undefined || right == "auto") ? undefined : parseInt(right.substr(0, right.length - 2));
            var top = $(self.el).css("top");
            top = (top == undefined || top == "auto") ? undefined : parseInt(top.substr(0, top.length - 2));
            var bottom = $(self.el).css("bottom");
            bottom = (bottom == undefined || bottom == "auto") ? undefined : parseInt(bottom.substr(0, bottom.length - 2));

            var panelWidth = $("."+self.className).width();
            var panelHeight = $("."+self.className).height();
            var windowWidth = $(window).width();
            var windowHeight = $(window).height();

            var position = undefined;
            if (left != undefined && top != undefined) {
                position = { left : left, top : top };
            } if (left != undefined && bottom != undefined) {
                position = { left : left, top : windowHeight - (bottom + panelHeight) };
            } else if (right != undefined && top != undefined) {
                position = { left : windowWidth - ( right + panelWidth), top : top} ;
            } else if (right != undefined && bottom != undefined) {
                position = { left : windowWidth - ( right + panelWidth),top : windowHeight - (bottom + panelHeight) } ;
            }

            if(position != undefined) {
                start($("."+self.className), position, self);
                drag($("."+self.className), position, self);
                stop($("."+self.className), position, self);
            }

        });
        return this;
    }
});