// Creates a custom OpenLayers profile based on what's used in the page.
// Copyright 2012 Dan Adams (http://mrdanadams.com)
// License: MIT

; window.OLI = window.OLI || {};
OLI.references = OLI.references || {};
OLI.ordered = OLI.ordered || [];

// Instruments OpenLayers to track API usage
// Note: this needs to be called before any of the OpenLayers API is used but after it is loaded.
OLI.instrument = function() {
  var instances, order;

  // allow it to be run over multiple pages
  instances = this.references;
  order = this.ordered;

  // tracks instance usage
  function hit(name) {
    if (instances[name]) {
      instances[name] = instances[name] + 1;
    } else {
      instances[name] = 1;
      order.push(name);
    }
  }

  // Whether a function is an OpenLayers class constructor
  function isConstructor(f) {
    return typeof f == "function" && (f.prototype.initialize || f.prototype.CLASS_NAME);
  }

  // Instrument a class
  function visitClass(con, name, obj, parent) {
    var full = parent+name, v;
    parent = parent+name+'.';

    for(k in con) {
      v = con[k];
      if (isConstructor(v)) visitClass(v, k, con, parent);
    }

    obj[name] = function(con, full) {
      var v, f;

      f = function() {
        hit(full);
        return con.apply(this, arguments);
      }
      
      for (k in con) f[k] = con[k];

      f.prototype = con.prototype;
      return f;
    }(obj[name], full);
  }

  // Instrument a single function
  function visitFunction (f, name, obj, parent) {
    obj[name] = function() {
      hit(parent);
      return f.apply(this, arguments);
    }
  }

  // Instrument a general object that is not a class
  function visitObject(o, name, parent) {
    var full, v;

    parent = parent || '';
    full = parent+name;
    parent = parent+name+'.';

    for (k in o) {
      v = o[k];

      if (isConstructor(v))
        visitClass(v, k, o, parent);
      else if(typeof v == "function")
        visitFunction(v, k, o, full);
      else if(typeof v == "object" && k.match(/^[A-Z]/))
        visitObject(v, k, parent);
      // else
      //   console.log("skipping "+parent+k+" ("+(typeof v)+")");
    }
  }

  // start instrumentation at the root
  visitObject(OpenLayers, "OpenLayers");

  // automatically dump the profile after the page loads
  window.setTimeout(function() { console.log(OLI.createProfile()); }, 20000);
}

// Dumps a OpenLayers profile to the console.
// This profile can be used for creating a custom build
OLI.createProfile = function() {
  var files = [],
    baseTypes = ['Bounds', 'Class', 'Date', 'Element', 'LonLat', 'Pixel', 'Size'],
    name, count, base, i;
  // console.log(this.ordered);
  // console.log(this.references);

  if (JSON && JSON.stringify) {
    console.log("Use the following for creating profiles across multiple screens:");
    console.log("OLI.ordered = "+JSON.stringify(this.ordered)+";");
    console.log("OLI.references = "+JSON.stringify(this.references)+";");
  }

  for(i=0; i<this.ordered.length; i++) {
    name = this.ordered[i];
    count = this.references[name];
    base = name.replace(/.*\./, '');
    if (baseTypes.indexOf(base) != -1)
      name = "OpenLayers.BaseTypes."+base;

    if (name != "OpenLayers")
      files.push(name.replace(/\./g, '/')+'.js');
  }
  
  return [
    "[first]\n\n[last]\n\n[include]",
    files.join("\n"),
    "\n\n[exclude]\n"
  ].join("\n");
}

// set an existing profile here if you have one
//OLI.ordered = ["OpenLayers.Control.Navigation","OpenLayers.Util","OpenLayers.Events","OpenLayers.Control.PanZoom","OpenLayers.Pixel","OpenLayers.Map","OpenLayers.Size","OpenLayers.Bounds","OpenLayers","OpenLayers.Element","OpenLayers.Function","OpenLayers.Event","OpenLayers.Handler.Click","OpenLayers.Control.DragPan","OpenLayers.Control.ZoomBox","OpenLayers.Kinetic","OpenLayers.Handler.Drag","OpenLayers.Handler.Box","OpenLayers.Handler.MouseWheel","OpenLayers.Control.PinchZoom","OpenLayers.Handler.Pinch","OpenLayers.Events.buttonclick","OpenLayers.Layer.Bing","OpenLayers.Projection","OpenLayers.LonLat","OpenLayers.Animation","OpenLayers.Layer.Image","OpenLayers.Tile.Image","OpenLayers.Layer.Vector","OpenLayers.Renderer.SVG","OpenLayers.StyleMap","OpenLayers.Style","OpenLayers.Control.Panel","OpenLayers.Control.DrawFeature","OpenLayers.Handler.Polygon","OpenLayers.Control.ModifyFeature","OpenLayers.Feature.Vector","OpenLayers.Control.SelectFeature","OpenLayers.Handler.Feature","OpenLayers.Control.DragFeature","OpenLayers.Handler.Keyboard","OpenLayers.Control","OpenLayers.Geometry.Point","OpenLayers.Geometry.LinearRing","OpenLayers.Geometry.Polygon","OpenLayers.String","OpenLayers.Handler.Point"];
//OLI.references = {"OpenLayers.Control.Navigation":3,"OpenLayers.Util":6060,"OpenLayers.Events":106,"OpenLayers.Control.PanZoom":3,"OpenLayers.Pixel":329,"OpenLayers.Map":3,"OpenLayers.Size":104,"OpenLayers.Bounds":557,"OpenLayers":24,"OpenLayers.Element":104,"OpenLayers.Function":330,"OpenLayers.Event":1368,"OpenLayers.Handler.Click":3,"OpenLayers.Control.DragPan":3,"OpenLayers.Control.ZoomBox":3,"OpenLayers.Kinetic":3,"OpenLayers.Handler.Drag":8,"OpenLayers.Handler.Box":3,"OpenLayers.Handler.MouseWheel":3,"OpenLayers.Control.PinchZoom":3,"OpenLayers.Handler.Pinch":3,"OpenLayers.Events.buttonclick":3,"OpenLayers.Layer.Bing":3,"OpenLayers.Projection":11,"OpenLayers.LonLat":217,"OpenLayers.Animation":17,"OpenLayers.Layer.Image":3,"OpenLayers.Tile.Image":2,"OpenLayers.Layer.Vector":3,"OpenLayers.Renderer.SVG":3,"OpenLayers.StyleMap":8,"OpenLayers.Style":36,"OpenLayers.Control.Panel":2,"OpenLayers.Control.DrawFeature":2,"OpenLayers.Handler.Polygon":1,"OpenLayers.Control.ModifyFeature":2,"OpenLayers.Feature.Vector":10,"OpenLayers.Control.SelectFeature":2,"OpenLayers.Handler.Feature":4,"OpenLayers.Control.DragFeature":2,"OpenLayers.Handler.Keyboard":2,"OpenLayers.Control":4,"OpenLayers.Geometry.Point":16,"OpenLayers.Geometry.LinearRing":2,"OpenLayers.Geometry.Polygon":2,"OpenLayers.String":104,"OpenLayers.Handler.Point":1};

// run instrumentation when loaded
OLI.instrument();
