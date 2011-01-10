<html>
      <head>
       <title><g:layoutTitle default="Grails" /></title>
		<link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
		<link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/xtheme-blue.css')}" />
		<script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" ></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}" ></script>
        <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/examples/ux/RowEditor.js')}" ></script>
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${createLinkTo(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
		</script>
		<script type="text/javascript" src="${createLinkTo(dir:'js',file:'be/cytomine/user/rest.js')}" ></script>
          <g:layoutHead />
      </head>
      <body onload="${pageProperty(name:'body.onload')}">
            <div id="container">
			<div id="header">
			</div>
			<div id="content">
				<div id="include">
					<g:layoutBody />
				</div>
			</div>
		</div>
      </body>
</html>