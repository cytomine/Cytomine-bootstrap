<html>
<head>
  <title><g:layoutTitle default="Grails" /></title>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/shared/extjs/App.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenLayers.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenURL.js')}" ></script>
  <link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
  <link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/xtheme-gray.css')}" />
  <script type="text/javascript" charset="utf-8">
    Ext.BLANK_IMAGE_URL = "${resource(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
  </script>

  <!-- EXT JS Extensions -->
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/ux/RowEditor.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/ux/TabCloseMenu.js')}" ></script>

  <!-- Views -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/User.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Project.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Project.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Retrieval.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Dashboard.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Scan.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/AnnotationLayer.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Browser.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Admin.js')}" ></script>


  <!-- Langs -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/languages/fr.js')}" ></script>

  <!-- Models -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/models/User.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/models/Project.js')}" ></script>

  <!-- Cytomine -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/Application.js')}" ></script>


  <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'icons.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'ext.css')}" />
  <g:layoutHead />

</head>
<body onload="${pageProperty(name:'body.onload')}">
<div id="loading-mask"></div>
<div id="loading">
  <div class="loading-indicator">
    Chargement...
  </div>
</div>
<g:layoutBody />
<div id="toolbar"></div>
<div id="controls">
  <!--<ul id="controlToggle">
    <li>
      <input type="radio" name="type" value="none" id="noneToggle"
              onclick="Cytomine.currentLayer.toggleControl(this);" checked="checked" />
      <label for="noneToggle">navigate</label>
    </li>
    <li>
      <input type="radio" name="type" value="select" id="selectToggle" onclick="Cytomine.currentLayer.toggleControl(this);" checked="checked" />
      <label for="selectToggle">select</label>
    </li>
    <li>
      <input type="radio" name="type" value="point" id="pointToggle" onclick="Cytomine.currentLayer.toggleControl(this);" />
      <label for="pointToggle">draw point</label>
    </li>
    <li>
      <input type="radio" name="type" value="line" id="lineToggle" onclick="Cytomine.currentLayer.toggleControl(this);" />
      <label for="lineToggle">draw line</label>
    </li>
    <li>
      <input type="radio" name="type" value="polygon" id="polygonToggle" onclick="Cytomine.currentLayer.toggleControl(this);" />
      <label for="polygonToggle">draw polygon</label>
    </li>
    <li>
      <input type="radio" name="type" value="regular" id="regularToggle" onclick="Cytomine.currentLayer.toggleControl(this);" />
      <label for="regularToggle">draw regular polygon</label>
      <label for="sides"> - sides</label>
      <input id="sides" type="text" size="2" maxlength="2"
              name="sides" value="5" onchange="Cytomine.currentLayer.update()" />
      <ul>
        <li>
          ==><input id="irregular" type="checkbox"
                name="irregular" onchange="Cytomine.currentLayer.update()" />
          <label for="irregular">irregular</label>
        </li>
      </ul>
    </li>
    <li>
      <input type="radio" name="type" value="modify" id="modifyToggle"
              onclick="Cytomine.currentLayer.toggleControl(this);" />
      <label for="modifyToggle">modify feature</label>
      <ul>
        <li>
          ==><input id="rotate" type="checkbox"
                name="rotate" onchange="Cytomine.currentLayer.update()" />
          <label for="rotate">allow rotation</label>
        </li>
        <li>
          ==><input id="resize" type="checkbox"
                name="resize" onchange="Cytomine.currentLayer.update()" />
          <label for="resize">allow resizing</label>
          (<input id="keepAspectRatio" type="checkbox"
                name="keepAspectRatio" onchange="Cytomine.currentLayer.update()" checked="checked" />
          <label for="keepAspectRatio">keep aspect ratio</label>)
        </li>
        <li>
          ==><input id="drag" type="checkbox"
                name="drag" onchange="Cytomine.currentLayer.update()" />
          <label for="drag">allow dragging</label>
        </li>
      </ul>
    </li>
  </ul>-->
</div>

</body>
</html>

