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
    var layerAnnotation;

    function initOpenLayers(){
      Ext.namespace('Cytomine');
      var scan = new Cytomine.Project.Scan(${urls}, ${scan.id}, "${scan.filename}", "${scan.getData().path}");
      layerAnnotation = new Cytomine.Project.AnnotationLayer( "totolayer", ${scan.id});
      layerAnnotation.loadToMap(scan);
      layerAnnotation.loadAnnotations(scan);

      Cytomine.scans[${scan.id}] = scan;
      Cytomine.annotationLayers[${scan.id}] = layerAnnotation;
      Cytomine.currentLayer = Cytomine.annotationLayers[${scan.id}];
      document.getElementById('noneToggle').checked = true;
    }

    Ext.onReady(function () {
      initOpenLayers();
    });

  </script>

</head>
<body onload="init()">
<div id="map${scan.id}"></div>
<div id="map" class="smallmap"></div>

</body>
</html>