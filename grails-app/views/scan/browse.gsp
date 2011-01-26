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
    function initOpenLayers(){
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
      var map = new OpenLayers.Map( 'map${scan.id}', options);
      map.addLayer(OUlayer);


     /* var data = new OpenLayers.Layer.Vector("Data");
      var parser = new OpenLayers.Format.WKT();
      var wkt = "POLYGON((5000 1500, 5300 1800, 5700 1900, 5770 1300, 5300 1400))";
      var geometry = parser.read(wkt);
      var feature = new OpenLayers.Feature.Vector(geometry);
      data.addFeatures([feature]);
      map.addLayers([data])          */


    /*var features = new Array(4);
    features[0] = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(-3000, 2000), {type: 500}, {attributes: {name: "pt1"}});
    features[1] = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(3500, -4000), {type: 50}, {attributes: {name: "pt2"}});
    features[2] = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(3000, 7000), {type: 500}, {attributes: {name: "pt3"}});
    features[3] = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(40000, 80000), {type: 5000}, {attributes: {name: "pt4"}});
     alert('Affichage des points');
     var myStyles = new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    pointRadius: "${type}", // sized according to type attribute
                    fillColor: "#ffcc66",
                    strokeColor: "#ff9933",
                    strokeWidth: 2
                }),
                "select": new OpenLayers.Style({
                    fillColor: "#66ccff",
                    strokeColor: "#3399ff"
                })
            });

      // Create a vector layer and give it your style map.
      var points = new OpenLayers.Layer.Vector(
          'Points', {styleMap: myStyles}
      );
      points.addFeatures(features);

      map.addLayer(points) ;    */





      //var parser = new OpenLayers.Format.WKT();
      //var wkt = "POINT(10000,10000)";
      //var geom2 = parser.read(wkt);

      /*var geom2 = GeomFromWKT("POINT(10000,10000)");
      console.log(geom2);
      //var geom2 =  new OpenLayers.Geometry.Point(10000, 10000);
     var vectorLayer2 = new OpenLayers.Layer.Vector("Overlay2");
    var feature2 = new OpenLayers.Feature.Vector(
     geom2,
     {some:'data'},
     {externalGraphic: 'img/marker.png', graphicHeight: 21, graphicWidth: 16});
    vectorLayer2.addFeatures(feature2);
    map.addLayer(vectorLayer2);*/

         var format = new OpenLayers.Format.WKT();
         var points = [];
         points.push(new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(10000, 9000))
        );
        console.log(format.write(points[0]));

        var point =  (format.read("POINT(10000 9000)"));

        console.log(format.write(point));

         var geom = point.geometry;

        // test a point
        var format = new OpenLayers.Format.WKT();


      /*var vectorLayer = new OpenLayers.Layer.Vector("Overlay");
     var feature = new OpenLayers.Feature.Vector(
      new OpenLayers.Geometry.Point(5000, 5000),
      {some:'data'},
      {externalGraphic: 'img/marker.png', graphicHeight: 21, graphicWidth: 16});
     vectorLayer.addFeatures(feature);
     map.addLayer(vectorLayer); */

        var vectorLayer = new OpenLayers.Layer.Vector("Overlay");
     var feature = new OpenLayers.Feature.Vector(
      geom,
      {some:'data'},
      {pointRadius: 10, fillColor: "green", fillOpacity: 0.5, strokeColor: "black"});
     vectorLayer.addFeatures(feature);
     map.addLayer(vectorLayer);







      var lon = metadata.width / 2;
      var lat = metadata.height / 2;
      map.setCenter(new OpenLayers.LonLat(lon, lat), 0);
    }


    Ext.onReady(function () {
      initOpenLayers();
    });


/*function GeomFromWKT(wkt) {
     console.log('GeomFromWKT:'+wkt);
    var geometry = null;
    if (typeof wkt != 'undefined') {
        var geomType = wkt.substring(0, wkt.indexOf('('));
        switch (geomType) {
            case "POLYGON":
                var aPolygon = new Array();
                var aLinearRings = wkt.substring(9, wkt.length - 2).split('), (');
                for (var lr = 0; lr < aLinearRings.length; lr++) {
                    var geomLR = new OpenLayers.Geometry.LinearRing;
                    var aPoints = aLinearRings[lr].split(', ');
                    for (var pt = 0; pt < aPoints.length; pt++) {
                        var aPt = aPoints[pt].split(' ');
                        var geomPt = new OpenLayers.Geometry.Point(aPt[0], aPt[1]);
                        geomLR.addComponent(geomPt, pt);
                    }
                    aPolygon[lr] = geomLR;
                }
                geometry = new OpenLayers.Geometry.Polygon(aPolygon);
                break;
        }
    }
    return geometry;
}
          */




  </script>

</head>
<body>
<div id="map${scan.id}"></div>
</body>
</html>