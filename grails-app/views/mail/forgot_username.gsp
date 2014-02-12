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
                            <p class="lead">Someone recently requested to recover your username.</p>

                            <p>
                                Your username is <%= username %>.
                            </p>

                            <p class="callout">
                                If this is a mistake just ignore this email.
                            </p><!-- /Callout Panel -->

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

