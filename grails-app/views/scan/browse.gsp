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

  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'openlayers/OpenLayers.js')}" ></script>
  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'openlayers/OpenURL.js')}" ></script>
  <script type="text/javascript">
    function init(){
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

      var map = new OpenLayers.Map( 'map', options);
      map.addLayer(OUlayer);
      var lon = metadata.width / 2;
      var lat = metadata.height / 2;
      map.setCenter(new OpenLayers.LonLat(lon, lat), 0);
    }
  </script>
</head>
<body onload="init()">
<div id="map"></div>
<div id="overview"></div>

</body>
</html>