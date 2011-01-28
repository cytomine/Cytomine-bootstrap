<%--
  Created by IntelliJ IDEA.
  User: stevben
  Date: 13/01/11
  Time: 13:27
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>${scan.filename}</title>

  <script type="text/javascript">
    function selected (evt) {
     // alert(evt.feature.id + " selected on " + this.name);
    }

    var map, vectorsDrawAnnotations, controls,req;
    function initOpenLayers(){

      /**Load picture */
      var urls = ${urls};
      var metadataUrl = "/cytomine-web/api/image/metadata/${scan.id}";
      var OUlayer = new OpenLayers.Layer.OpenURL( "${scan.filename}", urls, {transitionEffect: 'resize', layername: 'basic', format:'image/jpeg', rft_id:'${scan.getData().path}', metadataUrl: metadataUrl} );
      var metadata = OUlayer.getImageMetadata();
      var resolutions = OUlayer.getResolutions();
      var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
      var tileSize = OUlayer.getTileSize();
      var options = {resolutions: resolutions, maxExtent: maxExtent, tileSize: tileSize, controls: [
        new OpenLayers.Control.Navigation(),
        new OpenLayers.Control.PanZoomBar(),
        new OpenLayers.Control.LayerSwitcher({'ascending':false}),
        new OpenLayers.Control.MousePosition(),
        new OpenLayers.Control.OverviewMap(),
        new OpenLayers.Control.KeyboardDefaults()
      ]};
      map = new OpenLayers.Map( 'map${scan.id}', options);
      map.addLayer(OUlayer);
      map.addControl(new OpenLayers.Control.LayerSwitcher());
      map.addControl(new OpenLayers.Control.MousePosition());


      var lon = metadata.width / 2;
      var lat = metadata.height / 2;
      map.setCenter(new OpenLayers.LonLat(lon, lat), 0);



      /**Load layer for user drawing annotation*/
      vectorsDrawAnnotations = new OpenLayers.Layer.Vector("Vector Layer");
      vectorsDrawAnnotations.events.on({
        'featureadded': onFeatureAdded,
        'beforefeaturemodified': function(evt) {
          console.log("Selected " + evt.feature.id  + " for modification");
        },
        'afterfeaturemodified': onFeatureUpdate
      });
      vectorsDrawAnnotations.events.register("featureselected", vectorsDrawAnnotations, selected);
      map.addLayer(vectorsDrawAnnotations);


      /**Load annotation from databases**/
      req = new XMLHttpRequest();
      req.open("GET", "/cytomine-web/api/annotation/scan/${scan.id}.json", true);
      req.onreadystatechange = decodeAnnotations;   // the handler
      req.send(null);
      map.addLayer(vectorsDrawAnnotations);


      /**Add controls**/
      console.log("controls");
      controls = {
        point: new OpenLayers.Control.DrawFeature(vectorsDrawAnnotations,
                OpenLayers.Handler.Point),
        line: new OpenLayers.Control.DrawFeature(vectorsDrawAnnotations,
                OpenLayers.Handler.Path),
        polygon: new OpenLayers.Control.DrawFeature(vectorsDrawAnnotations,
                OpenLayers.Handler.Polygon),
        regular: new OpenLayers.Control.DrawFeature(vectorsDrawAnnotations,
                OpenLayers.Handler.RegularPolygon,
        {handlerOptions: {sides: 5}}),
        modify: new OpenLayers.Control.ModifyFeature(vectorsDrawAnnotations)
      };
      for(var key in controls) {
        map.addControl(controls[key]);
      }

      /** Triggered when add new feature **/
      function onFeatureAdded(evt) {
        console.log("onFeatureAdded start:"+evt.feature.attributes.idAnnotation);
        /* Check if feature must throw a listener when it is added
         * true: annotation already in database (no new insert!)
         * false: new annotation that just have been draw (need insert)
         * */
        if(evt.feature.attributes.nolistener!='t')
        {
          var format = new OpenLayers.Format.WKT();
          var feature = evt.feature;
          var geomwkt = format.write(feature);
          console.log("add geomwkt="+geomwkt);

          req = new XMLHttpRequest();
             console.log("/cytomine-web/api/annotation/scan/${scan.id}/"+geomwkt);
          req.open("POST", "/cytomine-web/api/annotation/scan/${scan.id}/"+geomwkt+".json", true);
          req.onreadystatechange = decodeNewAnnotation;
          req.send(null);
          //Annotation hasn't any id => -1
          feature.attributes = {idAnnotation: "-1"};
          vectorsDrawAnnotations.removeFeatures([feature]);
          //feature.remove();
          console.log("onFeatureAdded end");
        }
      }

      function onFeatureUpdate(evt) {
        console.log("onFeatureUpdate start");

        var format = new OpenLayers.Format.WKT();
        var feature = evt.feature;
        var geomwkt = format.write(feature);
        console.log("update geomwkt="+geomwkt + " " + feature.attributes.idAnnotation);

        req = new XMLHttpRequest();
        req.open("PUT", "/cytomine-web/api/annotation/"+feature.attributes.idAnnotation+"/"+geomwkt, true);
        req.send(null);

        console.log("onFeatureUpdate end");

      }


      document.getElementById('noneToggle').checked = true;

      printMapInfo();
      var lon = metadata.width / 2;
      var lat = metadata.height / 2;
      map.setCenter(new OpenLayers.LonLat(lon, lat), 0);

    }


    Ext.onReady(function () {
      initOpenLayers();
    });


    function decodeAnnotations()
    {
      var format = new OpenLayers.Format.WKT();
      //vectorsAnnotations = new OpenLayers.Layer.Vector("Overlay");
      var points = [];
      console.log("decodeAnnotations:"+req.readyState);
      if (req.readyState == 4)
      {
        //eval json
        var JSONannotations = eval('(' + req.responseText + ')');
        console.log(JSONannotations.annotations);

        for (i=0;i<JSONannotations.annotations.length;i++)
        {
          console.log(JSONannotations.annotations[i].id);
          //read from wkt to geometry
          var point =  (format.read(JSONannotations.annotations[i].location));
          var geom = point.geometry;

          var feature = new OpenLayers.Feature.Vector(
                  geom,
          {some:'data'},
          {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

          feature.attributes = {idAnnotation: JSONannotations.annotations[i].id, nolistener:'t',importance: 10 };

          vectorsDrawAnnotations.addFeatures(feature);
        }

      }

    }


    function decodeNewAnnotation()
    {
      var format = new OpenLayers.Format.WKT();
      var points = [];
      console.log("decodeNewAnnotation:"+req.readyState);
      if (req.readyState == 4)
      {
        //eval json
        console.log("response:"+req.responseText);
        var JSONannotations = eval('(' + req.responseText + ')');
        console.log(JSONannotations.annotations);

        for (i=0;i<JSONannotations.annotations.length;i++)
        {
          console.log(JSONannotations.annotations[i].id);
          //read from wkt to geometry
          var point =  (format.read(JSONannotations.annotations[i].location));
          var geom = point.geometry;

          var feature = new OpenLayers.Feature.Vector(
                  geom,
          {some:'data'},
          {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});

          feature.attributes = {idAnnotation: JSONannotations.annotations[i].id, nolistener:'t',importance: 10 };

          vectorsDrawAnnotations.addFeatures(feature);
        }

      }

    }












    function update() {
      console.log("update")
      // reset modification mode
      controls.modify.mode = OpenLayers.Control.ModifyFeature.RESHAPE;
      var rotate = document.getElementById("rotate").checked;
      if(rotate) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.ROTATE;
      }
      var resize = document.getElementById("resize").checked;
      if(resize) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.RESIZE;
        var keepAspectRatio = document.getElementById("keepAspectRatio").checked;
        if (keepAspectRatio) {
          controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
        }
      }
      var drag = document.getElementById("drag").checked;
      if(drag) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.DRAG;
      }
      if (rotate || drag) {
        controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
      }
      var sides = parseInt(document.getElementById("sides").value);
      sides = Math.max(3, isNaN(sides) ? 0 : sides);
      controls.regular.handler.sides = sides;
      var irregular =  document.getElementById("irregular").checked;
      controls.regular.handler.irregular = irregular;
    }

    function toggleControl(element) {
      console.log("toggleControl")
      for(key in controls) {
        var control = controls[key];
        if(element.value == key && element.checked) {
          control.activate();
        } else {
          control.deactivate();
        }
      }
    }

    function printMapInfo() {
      console.log("*****PRINT MAP INFO");
      var layers = map.layers;

      for(i=0;i<layers.length;i++)
      {
        var layer = layers[i];
        console.log("***********LAYER: id=|"+layer.id + "|  |" + layer.name + "|  |" + layer.type);
        /*var featuresArray = layer.features;

         for(property in featuresArray) {
         console.log("property="+property);
         }*/


        /*console.log(layer.features.type);
         for(j=0;j<featuresArray.length;j++)
         {
         var feature = featuresArray[j];
         console.log("*******************FEATURE:"+feature.geometry);
         }  */
      }
    }

  </script>

</head>
<body onload="init()">
<div id="map${scan.id}"></div>
<h1 id="title">Annotations controls</h1>

<div id="map" class="smallmap"></div>
<div id="controls">


  <input type="radio" name="type" value="none" id="noneToggle"
          onclick="toggleControl(this);" checked="checked" />
  <label for="noneToggle">navigate</label>

  <input type="radio" name="type" value="point" id="pointToggle" onclick="toggleControl(this);" />
  <label for="pointToggle">draw point</label>

  <input type="radio" name="type" value="line" id="lineToggle" onclick="toggleControl(this);" />
  <label for="lineToggle">draw line</label>

  <input type="radio" name="type" value="polygon" id="polygonToggle" onclick="toggleControl(this);" />
  <label for="polygonToggle">draw polygon</label>

  <input type="radio" name="type" value="regular" id="regularToggle" onclick="toggleControl(this);" />
  <label for="regularToggle">draw regular polygon</label>
  <label for="sides"> - sides</label>
  <input id="sides" type="text" size="2" maxlength="2"
          name="sides" value="5" onchange="update()" />

  <input id="irregular" type="checkbox"
          name="irregular" onchange="update()" />
  <label for="irregular">irregular</label>

  <input type="radio" name="type" value="modify" id="modifyToggle"
          onclick="toggleControl(this);" />
  <label for="modifyToggle">modify feature</label>

  <input id="rotate" type="checkbox"
          name="rotate" onchange="update()" />
  <label for="rotate">allow rotation</label>

  <input id="resize" type="checkbox"
          name="resize" onchange="update()" />
  <label for="resize">allow resizing</label>
  (<input id="keepAspectRatio" type="checkbox"
        name="keepAspectRatio" onchange="update()" checked="checked" />
  <label for="keepAspectRatio">keep aspect ratio</label>)

  <input id="drag" type="checkbox"
          name="drag" onchange="update()" />
  <label for="drag">allow dragging</label>

  <button type="button" onclick="JavaScript: alert('Bouton text')">
    Save all annotations
  </button>


</div>
</body>
</html>