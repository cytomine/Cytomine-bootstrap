<html>
      <head>
       <title><g:layoutTitle default="Grails" /></title>
		<link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
		<link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/xtheme-gray.css')}" />
		<script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" />
		<script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}" />
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${createLinkTo(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
		</script>
		<g:javascript library="application" />
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