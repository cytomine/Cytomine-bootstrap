<g:render template="/mail/header" model="[]"/>

<!-- BODY -->
<table class="body-wrap">
    <tr>
        <td></td>
        <td class="container" bgcolor="#FFFFFF">

            <div class="content">
                <table>
                    <tr>
                        <td>
                            <h3>Hi,</h3>

                            <p class="lead">
                                The image <%= abstractImageFilename %> is now available on Cytomine.<br/>
                                <ul>
                                    <g:each in="${imagesInstances}">
                                        <li>Click <a href="${it.urlImageInstance}">here</a> to visualize <%= abstractImageFilename %> in workspace ${it.projectName}</li>
                                    </g:each>
                                </ul>
                            </p>

                            <p>
                                <img alt="<%= abstractImageFilename %>" src='cid:<%= cid %>' style="max-width: 400px; max-height: 400px;"/>
                            </p>

                            <!-- social & contact -->
                            <g:render template="/mail/social" model="[]"/>

                        </td>
                    </tr>
                </table>
            </div><!-- /content -->

        </td>
        <td></td>
    </tr>
</table><!-- /BODY -->

<g:render template="/mail/footer" model="[by : by]"/>
