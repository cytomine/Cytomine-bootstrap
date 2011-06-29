/**
 *  Slider Kit v1.5.1 - Sliding contents with jQuery
 *  http://www.kyrielles.net/sliderkit
 *  
 *  Copyright (c) 2010-2011 Alan Frog
 *  Licensed under the GNU General Public License
 *  See <license.txt> or <http://www.gnu.org/licenses/>
 *  
 *  Requires: jQuery v1.3+ <http://jquery.com/>
 *
 *  ---------------------------------
 *  This file is part of Slider Kit jQuery plugin.
 *  
 *  Slider Kit is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Slider Kit is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  ---------------------------------
 */

(function($){

	/**
	 *	SliderKit object 
	 *	An object representing a slider in action.
	 */	
	var SliderKit = function() {
		
		var self = this;
		
		this._init = function( element, options ) {		
			
			// Passed in options and default options are mixed
			this.options = $.extend({}, this._settings, options);

			// Save the element reference
			this.domObj = $(element); // The main container DOM element

			this.panels = $("."+this.options.cssprefix+"-panel", this.domObj);
			this.nav = $("."+this.options.cssprefix+"-nav", this.domObj);
			this.navClip = $("."+this.options.cssprefix+"-nav-clip", this.nav);
			this.allItems = this.panels.size();
			
			// Check if there is any reason to go further
			this.arePanels = this.allItems > 0 ? 1 : 0;
			this.isNavClip = this.navClip.size() > 0 ? 1 : 0;
			if(!this.arePanels && !this.isNavClip){
				this._errorReport("01", this.options.debug, 1);
			}
			
			// The function stops if there is no height value (unless 'freeheight' setting is true)
			else if(this.domObj.css("height") == "auto" && !this.options.freeheight){
				this._errorReport("02", this.options.debug, 1);
			}	
			
			// By default, the widget should be hidden via CSS. Then shown only if javascript is available :
			this.domObj.css("display","block");
	
			// Variables that will be needed all over the script			
			this.currId = 0;
			this.prevId = 0;
			this.newId = 0;
			this.currPanel = null;
			this.prevPanel = 0;
			this.firstTime = 1;
			this.scrollit = 0;
			this.isPlaying = null;
			this.changeOngoing = false;
			
			// Variables for CSS
			this.cssClassNames = {
				cssSelected: this.options.cssprefix+"-selected",			
				cssActive: this.options.cssprefix+"-panel-active",
				cssOld: this.options.cssprefix+"-panel-old",
				cssBtnDisable:this.options.cssprefix+"-btn-disable",
				cssPanelsWrapper:this.options.cssprefix+"-panels-wrapper",
				cssBtnPause: this.options.cssprefix+"-pause-btn",
				cssPosValue: "+"
			};
			
			if(this.isNavClip){
				this._buildNav();	
			}
			
			this._buildControls();				
			
			if(this.arePanels){
				this.panelsBag = $("."+this.options.cssprefix+"-panels", this.domObj);
				// Panels wrapper : this is only for internal code usage
				// It allows a nice sliding effect in the panels container
				if(this.options.panelfx == "sliding"){
					this._wrapPanels();	
				}
			}

			/*---------------------------------
			 *  Navigation settings
			 *---------------------------------*/
			
			// In carousel mode (no panels), mousewheel and autoscroll should move lines instead of thumbnails
			this.lineScrollDo = !this.arePanels ? 1 : 0;

			// Mousewheel navigation
			if(this.options.mousewheel){
				this.domObj.mousewheel(function(event, delta){
					//delta>0 ? self._change(null, "+=", null, self.lineScrollDo, 1) : self._change(null, "-=", null, self.lineScrollDo, 1);
					delta>0 ? self.stepBackward() : self.stepForward();
					return false;
				});
			}

			// Keyboard navigation (beta)
			if(this.options.keyboard){		
				this.domObj.keyup(function(event){
					// slide left
					if(event.keyCode == 37){
						self.stepBackward();
					}					
					// slide right
					else if (event.keyCode == 39){
						self.stepForward();
					}
				});
			}

			// One-click navigation
			if(this.options.panelclick && this.arePanels){			
				this.panelsBag.click(function(){
					self.stepForward();
					return false;
				});
			}
			
			// Slide for the first time
			this.changeWithId(this.options.start >= this.allItems ? this.allItems-1 : this.options.start, null);

			// Auto-scrolling starter
			if(this.options.auto){
				this.autoScrollStart();
			
				// Stops autoScrolling when mouse is over the slider content
				if(!this.isPlayBtn){
					this.domObj.hover(
						function(){
							if(self.isPlaying!=null){
								self.autoScrollStop();
							}
						},
						function(){
							self.autoScrollStart();
						}
					);
				}
			}
			
			// return this so we can chain/use the bridge with less code.
			return this;

		};
		
		this._settings = {
			cssprefix: "sliderkit",
			start: 0,
			auto: true,
			autospeed: 4000,
			mousewheel: false,
			keyboard: false,
			panelclick: false,
			circular: false,
			shownavitems: 5,
			navitemshover: false,
			navclipcenter: false,
			navcontinuous: false,
			navscrollatend: false,
			navfx: "sliding",
			scroll: null,
			scrollspeed: 600,
			scrolleasing: null,
			panelfx: "fading",
			panelfxspeed: 700,
			panelfxeasing: null,
			panelfxfirst: "none",
			panelfxbefore: function(){},
			panelfxafter: function(){},
			panelbtnshover: false,
			verticalnav: false,
			verticalslide: false,
			tabs: false,
			freeheight: false,
			fastchange: true,
			debug: false
		};
		
		this._errorReport = function( errorCode, debug, stop ) {
			if(debug){
				alert("Slider Kit error! Code #"+errorCode+" (see doc)");
			}
			if(stop){
				return false;
			}
		};
		
		this._buildNav = function() {

			this.navUL = $("ul", this.navClip);
		
			var navLI = $("li", this.navUL);
			var navLINum = navLI.size();
			
			// Check if nav size is equal to panels size (only if there are panels)
			if(this.arePanels && (navLINum != this.allItems) && this.nav.size() == 1){
				this._errorReport("03", this.options.debug, 1);
			}
			
			// If Slider Kit is used as a tabs menu, the nav scroll becomes useless
			if(this.options.tabs){
				this.options.shownavitems = this.allItems;
			}

			// Else we start initializing the carousel
			else{
				// LI margins function: returns the <li> tag margins value in pixels
				function getLImargin(attr){
					attrVal = navLI.css(attr);
					if(attrVal!="auto" && attr!="" && attr!="0px"){
						return parseInt(attrVal);
					}
					else return 0;
				}

				// Nav elements size
				var navSize = this.options.verticalnav ? this.nav.height() : this.nav.width();
				var navLIWidth = navLI.outerWidth(true); // padding + margin + border
				var navLIHeight = navLI.outerHeight(true);
				var navLIextHMarg = getLImargin("margin-left") + getLImargin("margin-right");
				var navLIextVMarg = getLImargin("margin-top") + getLImargin("margin-bottom");

				this.navLIsize = this.options.verticalnav ? navLIHeight : navLIWidth;
				this.navULSize = this.navLIsize * navLINum;
				this.navClipSize = (this.options.shownavitems * this.navLIsize) - (this.options.verticalnav ? navLIextVMarg : navLIextHMarg);// Removes the item side margins to center the nav clip

				// CSS attributes for position/height values
				this.cssPosAttr = this.options.verticalnav ? "top" : "left";
				var cssSizeAttr = this.options.verticalnav ? "height" : "width";
				var cssSizeAttrr = this.options.verticalnav ? "width" : "height";

				// Setting height and width values(px) to Clip, UL & LI tags
				navLI.css({width:navLI.width(), height:navLI.height()});
				this.navUL.css(cssSizeAttr, this.navULSize+"px");
				this.navClip.css({width:this.options.verticalnav ? navLIWidth : this.navClipSize, height:this.options.verticalnav ? this.navClipSize : navLIHeight});

				// Center the navclip in the nav container
				if(this.options.navclipcenter){
					this.navClip.css(this.cssPosAttr,( navSize - this.navClipSize)/2 ).css("margin", "0");
				}

				// Check if linescroll is necessary
				this.allItems = navLINum;
				
				// The nav scrolling is required only if the number of items is greater than the 'visible' param.
				if(this.allItems > this.options.shownavitems){
					this.scrollit = true;

					// Correcting a potentially 'this.options.scroll' wrong value
					if(this.options.scroll == null || this.options.scroll < 0 || this.options.scroll > this.allItems){
						this.options.scroll = this.options.shownavitems;
					}

					// Nav Buttons
					this.navBtns = $("."+this.options.cssprefix+"-nav-btn", this.nav);
					if(this.navBtns.size() > 0){
						this._buildNavButtons();
					}
				}
			}
			
			// Nav <li> links mouse event
			if(this.options.navitemshover && this.arePanels){
				navLI.mouseover(function(){
					self.changeWithId(getIndex(this, "li"), $(this));
				});
			}
			else if(this.arePanels || this.options.navscrollatend){
				navLI.click(function(){
					self.changeWithId(getIndex(this, "li"), $(this));
					return false;
				});
			}
			
			// Get an item index
			function getIndex(item, tag){
				return $(tag, $(item).parent()).index(item);
			}

		};
		
		this._buildNavButtons = function() {
			
			this.scrollBtns = true;
			this.navBtnPrev = $("."+this.options.cssprefix+"-nav-prev", this.nav);
			this.navBtnNext = $("."+this.options.cssprefix+"-nav-next", this.nav);
			
			// Nav Buttons click event
			this.navBtnPrev.click(function(){
				self.navPrev();
				return false;
			});
			this.navBtnNext.click(function(){
				self.navNext();
				return false;
			});

			// Nav Buttons mousedown/up events
			if(this.options.navcontinuous){
				this.navBtnPrev.mouseover(function(){
					self.navPrev(true);
				});
				this.navBtnNext.mouseover(function(){
					self.navNext(true);
				});
				this.navBtns.mouseout(function(){
					self.navStopContinuous();
				});
			}
			
			// Disable first button if not circular
			if(!this.options.circular){
				this.navBtnPrev.toggleClass(this.cssClassNames.cssBtnDisable);
			}
			
		};
		
		this._getNavPos = function() {
			this.navPos = this.options.verticalnav ? this.navUL.position().top : this.navUL.position().left;
			this.LIbefore = Math.ceil( Math.abs(this.navPos) / this.navLIsize );
			this.LIafter = Math.floor(( this.navULSize - Math.abs(this.navPos) - this.navClipSize) / this.navLIsize );
			if(this.LIafter < 0){
				this.LIafter = 0;
			}
		};

		this._buildControls = function() {
			
			this.playBtn = $("."+this.options.cssprefix+"-play-btn", this.domObj);
			this.gBtns = $("."+this.options.cssprefix+"-go-btn", this.domObj);

			this.isPlayBtn = this.playBtn.size() > 0 ? 1 : 0;
			this.goBtns = this.gBtns.size() > 0 ? 1 : 0;
			
			// Play button
			if(this.isPlayBtn){
				
				// If autoscroll is active, the play button is set to 'pause' mode
				if(this.options.auto){
					this.playBtn.addClass(this.cssClassNames.cssBtnPause);
				}
				
				// Button mouse event
				this.playBtn.click(function(){
					if(self.playBtn.hasClass(self.cssClassNames.cssBtnPause)){
						self.playBtnPause();
					}
					else{
						self.playBtnStart();
					}
					return false;
				});
			}

			// Go buttons (prev/next)
			if(this.goBtns){
				this.goBtnPrev = $("."+this.options.cssprefix+"-go-prev", this.domObj);
				this.goBtnNext = $("."+this.options.cssprefix+"-go-next", this.domObj);
				
				// Show/hide buttons on panel mouseover
				if(this.options.panelbtnshover){
					this.gBtns.hide();
					$("."+this.options.cssprefix+"-panels", this.domObj).hover(
						function(){
							self.gBtns.fadeIn();
						},
						function(){
							self.gBtns.fadeOut();
						}
					);
				}
				
				// Button click binding
				this.goBtnPrev.click(function(){
					self.stepBackward($(this));
					return false;
				});
				this.goBtnNext.click(function(){
					self.stepForward($(this));
					return false;
				});
			}
		};

		this._wrapPanels = function(){
			this.panels.wrapAll('<div class="'+this.cssClassNames.cssPanelsWrapper+'"></div>');
			this.panelsWrapper = $("."+this.cssClassNames.cssPanelsWrapper, this.panelsBag);
			this.panelsWrapper.css({"position":"relative"});
		};

		this._change = function( eventSrc, scrollWay, goToId, lineScrolling, stopAuto ) {
			
			// If there is a play button + auto-scrolling running
			if(stopAuto && this.isPlaying!=null && this.isPlayBtn){
				this.playBtnPause();
			}			
			
			// Don't go further if the side is reached and carousel isn't circular
			// The slide is stopped if the button is disable
			if(eventSrc){
				if(eventSrc.hasClass(this.cssClassNames.cssBtnDisable)){
					return false;
				}
			}
			
			// By default, user action is blocked when nav is being animated. This to prevent the function calculation to go mad when the user is switching the items too quickly.
			// This security applies on panels too. However it can be removed using the 'fastchange' option.
			var stopGoing = 0;
			var running = $(":animated", this.options.fastchange ? this.nav : this.domObj).size() > 0 ? 1 : 0;

			if( !running ){
				this.prevId = this.currId;
				
				// Increment the current id, only if linescrolling isn't required
				if(goToId == null && !lineScrolling){
					this.currId = scrollWay == "-=" ? this.currId+1 : this.currId-1;
				}
				// Else if an id is given, we take it
				else if(goToId != null){
					goToId = parseInt(goToId);// make sure it's a number
					this.currId = goToId < 0 ? 0 : goToId > this.allItems-1 ? this.allItems-1 : goToId;// make sure it's in the nav range
					var checkIdRange = eventSrc ? eventSrc.parent().parent().hasClass(this.options.cssprefix+"-nav-clip") ? false : true : true;
				}

				// If panel buttons exist, we activate them
				if(this.goBtns){
					this.gBtns.removeClass(this.cssClassNames.cssBtnDisable);
				}

				// If the carousel isn't circular the controls must be hidden when sides are reached
				if(!this.options.circular){
					// Top/left side is reached
					if(this.currId == -1){
						this.currId = 0;
						stopGoing = 1;
					}
					if(this.currId == 0 && this.goBtns){
						this.goBtnPrev.addClass(this.cssClassNames.cssBtnDisable);
					}

					// Bottom/right side is reached
					if(this.currId == this.allItems){
						this.currId = this.allItems-1;
						stopGoing = 1;
					}
					
					if(this.currId == this.allItems-1){
						if(this.options.auto){
							this.autoScrollStop();
						}
						if(this.goBtns){
							this.goBtnNext.addClass(this.cssClassNames.cssBtnDisable);
						}
					}
				}
				// Otherwise if there is no scroll required, this.currId must be reset when sides are reached
				else if(!this.scrollit){
					if(this.currId == this.allItems){
						this.currId = 0;
					}
					if(this.currId == -1){
						this.currId = this.allItems-1;
					}
				}
				
				// If the slide function isn't triggered from a nav LI event, we must check if the line must be scrolled or not
				//if( (goToId == null || this.firstTime) && this.scrollit && !stopGoing ){
				if( this.scrollit && !stopGoing ){
					this._setNavScroll(lineScrolling, scrollWay, checkIdRange);
				}
					
				// Highlight selected menu
				if(this.isNavClip){
					this.selectThumbnail(this.currId);
				}

				// Switch to the next panel
				if(this.arePanels){
					this._animPanel(this.currId, scrollWay);
				}
			
				// First time cancel
				if(this.firstTime){
					this.firstTime = 0;
				}

			} // else > be patient, the line scroll is running !
		};
		
		this._setNavScroll = function( lineScrolling, scrollWay, checkIdRange ) {

			// Get the current nav position
			this._getNavPos();			
			var jumpToId = 0;

			// No linescrolling required yet : we are going to check the current item position to determine if linescrolling is needed or not.
			if(!lineScrolling){
				// Linescrolling will happen only if navclip sides are reached
				// Number of items from the clip sides:
				var idFromClipStart = Math.abs(this.currId+1 - this.LIbefore);
				var idToClipEnd = this.options.shownavitems - idFromClipStart +1;
				var currIdOnEdge = this.currId == 0 || this.currId == this.allItems-1 ? 1 : 0;
				
				// If 'navscrollatend' option is activated, the line will scroll when navclip edges are reached (except if currId is the first or last item of the nav)
				if( (this.options.navscrollatend && (idToClipEnd == 1 || idFromClipStart == 1)) && !this.firstTime && !currIdOnEdge ){
					jumpToId = this.options.scroll - 1;
					lineScrolling = 1;
				}
				
				// Else the line will scroll when currId is out of the navclip range by -1 or +1
				if(idToClipEnd == 0 || idFromClipStart == 0){
					lineScrolling = 1;
				}
				
				// A target id is specified (using 'changeWithId' method). No direction ('scrollWay = ""').
				// We check here the difference between target and previous Ids
				if(checkIdRange){
					var idDiff = Math.abs(this.prevId - this.currId);
					
					// The nav will scroll if the target id is different from the previous Id
					// The scroll value will then be equal to the 'jumpToId' var, overwriting the 'scroll' option value.
					if(idDiff > 0){
						jumpToId = idDiff;
						lineScrolling = 1;
					}
				}
				
				// Dertermine scroll direction
				if(scrollWay == ""){
					if(this.prevId == this.currId && !currIdOnEdge){
						scrollWay = this.scrollWay == "-=" ? "+=" : "-=";
					}
					else{
						scrollWay = this.prevId < this.currId ? "-=" : "+=";
					}
				}
				this.scrollWay = scrollWay;
			}

			// If line scrolling is required
			if(lineScrolling){
				
				// How many lines will scroll ? By default the answer is 'this.options.scroll' or 'jumpToId'. But we check if there are enough lines left.
				var scrollPower = jumpToId > 0 ? jumpToId : this.options.scroll;
				var LIremain = scrollWay == "-=" ? this.LIafter : this.LIbefore;
				var scrollto = LIremain < scrollPower ? LIremain : scrollPower;
				var scrollSize = scrollto * this.navLIsize;
				
				// Once the nav has scrolled, the <li> tag matching the currId value may not be visible in the nav clip. So we calculate here a new currId regarding to the nav position.
				this.newId = scrollWay == "-=" ? this.LIbefore+scrollto : this.LIbefore-scrollto+this.options.shownavitems-1;
				if( (scrollWay == "-=" && this.newId > this.currId) || (scrollWay == "+=" && this.newId < this.currId) ){
					this.currId = this.newId;
				}
				
				// Circular option is active
				if(this.options.circular){
					if(this.LIbefore <= 0 && scrollWay == "+="){
						scrollWay = "-=";
						this.currId = this.allItems-1;
						scrollSize = ( this.LIafter/this.options.scroll )*( this.navLIsize*this.options.scroll );
					}
					else if(this.LIafter == 0 && scrollWay == "-="){
						scrollWay = "+=";
						this.currId = 0;
						scrollSize = Math.abs(this.navPos);
					}								
				}
		
				// Finally, the scroll animation
				this._animNav(scrollWay, scrollSize);
			}
		};
		
		this._animPanel = function( currId, scrollWay ) {

			// Current panel index
			this.currPanel = this.panels.eq(currId);
	
			// Slide panel (only if not already active)
			if(!this.currPanel.hasClass( this.cssClassNames.cssActive )){				
				
				// First panel display (no effect)
				if(this.firstTime){
					this.panelTransition = this.options.panelfxfirst;
					var FirstTime = 1;
				}
				
				// Else we check for the transition effect
				else{
					// No effect
					var freeheightfx = this.options.freeheight && this.options.panelfx == "fading" ? "tabsfading" : "none";
					this.panelTransition = this.options.freeheight ? freeheightfx : this.options.panelfx;
				}

				// Call the before function is it exists
				if(self.options.panelfxbefore) self.options.panelfxbefore();

				// Call the transition function
				this._panelTransitions[ this.panelTransition ](scrollWay, FirstTime);
			}
		};
			
		this._animNav = function( scrollWay, scrollSize ) {
			var navComplete = function(){
				// If the nav isn't circular, buttons are disabled when start or end is reached
				if(!self.options.circular && self.scrollBtns){
					self.navBtns.removeClass(self.cssClassNames.cssBtnDisable);

					// Get the nav position
					self._getNavPos();

					if(self.LIbefore <= 0){
						self.navBtnPrev.addClass(self.cssClassNames.cssBtnDisable);
					}
					else if(self.LIafter <= 0){
						self.navBtnNext.addClass(self.cssClassNames.cssBtnDisable);
					}
				}
				// Reload the animation if scrollcontinue option is true
				if(self.scrollcontinue){
					setTimeout(function(){ self.scrollcontinue == "-=" ? self.navPrev() : self.navNext() }, 0);
				}
			};

			this.navTransition = this.options.navfx;
			this._navTransitions[ this.navTransition ](scrollWay, scrollSize, navComplete);
		};

		this._panelTransitions = {
			
			none : function(scrollWay, FirstTime) {
				self.panels.removeClass(self.cssClassNames.cssActive).hide();
				self.currPanel.addClass(self.cssClassNames.cssActive).show();
				if(self.options.panelfxafter){self.options.panelfxafter();}
			},

			sliding : function(scrollWay, FirstTime) {
			
				// Slide direction
				if(scrollWay == ""){
					scrollWay = self.prevPanel < self.currId ? "-=" : "+=";
				}
				self.prevPanel = self.currId;
				
				// Position/Size values for CSS
				var cssPosValue = scrollWay == "-=" ? "+" : "-";
				var cssSlidePosAttr = self.options.verticalslide ? "top" : "left";
				var domObjSize = self.options.verticalnav ? self.domObj.height() : self.domObj.width();
				var slideScrollValue = cssSlidePosAttr == "top" ? {top: scrollWay+domObjSize} : {left: scrollWay+domObjSize};

				// Panels selection
				self.oldPanel = $("."+self.cssClassNames.cssOld, self.domObj);
				self.activePanel = $("."+self.cssClassNames.cssActive, self.domObj);
				
				// Panels CSS properties
				self.panels.css(cssSlidePosAttr, "0");
				self.oldPanel.removeClass(self.cssClassNames.cssOld).hide();		
				self.activePanel.removeClass(self.cssClassNames.cssActive).addClass(self.cssClassNames.cssOld);
				self.currPanel.addClass(self.cssClassNames.cssActive).css(cssSlidePosAttr, cssPosValue+domObjSize + "px").show();
			
				// Wrapper animation
				self.panelsWrapper.stop(true, true).css(cssSlidePosAttr, "0").animate(
					slideScrollValue, 
					self.options.panelfxspeed, 
					self.options.panelfxeasing,
					function(){
						if(self.options.panelfxafter) self.options.panelfxafter();
					}
				);
			},
			
			fading: function(scrollWay, FirstTime) {	
				if(FirstTime){
					self.panels.hide();
				}
				else self.currPanel.css("display","none");
				
				$("."+self.cssClassNames.cssOld, self.domObj).removeClass(self.cssClassNames.cssOld);				
				$("."+self.cssClassNames.cssActive, self.domObj).stop(true, true).removeClass(self.cssClassNames.cssActive).addClass(self.cssClassNames.cssOld);
				
				self.currPanel.addClass(self.cssClassNames.cssActive)
				.animate(
					{"opacity":"show"},
					self.options.panelfxspeed, 
					self.options.panelfxeasing, 
					function(){
						if(self.options.panelfxafter){self.options.panelfxafter();}
					}
				);				
			},
			
			tabsfading : function(scrollWay, FirstTime) {
				self.panels.removeClass(self.cssClassNames.cssActive).hide();
				self.currPanel.fadeIn(self.options.panelfxspeed, function(){
					if(self.options.panelfxafter){self.options.panelfxafter();}		
				});
			}
		
		};

		this._navTransitions = {
			
			none : function(scrollWay, scrollSize, navComplete) {
				var newScrollSize = scrollWay == "-=" ? self.navPos-scrollSize : self.navPos+scrollSize;
				self.navUL.css( self.cssPosAttr, newScrollSize +"px" );
				navComplete();
			},
			
			sliding: function(scrollWay, scrollSize, navComplete) {
				self.navUL.animate(
					self.cssPosAttr == "left" ? {left:scrollWay+scrollSize} : {top:scrollWay+scrollSize}
					, self.options.scrollspeed, self.options.scrolleasing
					, function(){
						navComplete();
					}
				);	
			}
		};
		
		this.playBtnPause = function() {
			this.playBtn.removeClass(this.cssClassNames.cssBtnPause);
			this.autoScrollStop();
		};

		this.playBtnStart = function() {
			this.playBtn.addClass(self.cssClassNames.cssBtnPause);
			this.autoScrollStart();
		};
		
		this.autoScrollStart = function() {
			var self = this;
			this.isPlaying = setInterval(function(){
				self._change(null, "-=", null, self.lineScrollDo, null);
			}, self.options.autospeed);
		};

		this.autoScrollStop = function() {
			clearTimeout(this.isPlaying);
			this.isPlaying = null;
		};
						
		this.changeWithId = function( id, eventSrc ) {
			this._change(eventSrc, "", id, 0, 1);
		};

		this.stepBackward = function(eventSrc) {
			this._change(eventSrc, "+=", null, self.lineScrollDo, 1);
		};

		this.stepForward = function(eventSrc) {
			this._change(eventSrc, "-=", null, self.lineScrollDo, 1);
		};
		
		this.navPrev = function(c) {
			if(c){self.scrollcontinue = "-=";}
			this._change(this.navBtnPrev, "+=", null, 1, 1);
		};
		
		this.navNext = function(c) {
			if(c){self.scrollcontinue = "+=";}
			this._change(this.navBtnNext, "-=", null, 1, 1);
		};
		
		this.navStopContinuous = function() {
			self.scrollcontinue = "";
		};

		this.selectThumbnail = function( currId ){
			$("ul li."+this.cssClassNames.cssSelected, this.nav).removeClass(this.cssClassNames.cssSelected);
			$("ul li:eq("+currId+")", this.nav).addClass(this.cssClassNames.cssSelected);
		};
	
		this.addTransition = function( name, fn ) {
			_transitions[name] = fn;
		};
	};

	$.fn.sliderkit = function(options){
		// Don't act on absent elements -via Paul Irish's advice
		if ( this.length ) {
			return this.each(function(){
				// Create a new sliderkit object
				var mySliderkit = new SliderKit();

				// Run the initialization function of the sliderkit
				mySliderkit._init( this, options ); // `this` refers to the element

				// Save the instance of the sliderkit object in the element's data store
				$.data(this, "sliderkit", mySliderkit);
			});
		}
	};
	
})(jQuery);