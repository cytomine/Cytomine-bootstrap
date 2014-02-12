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
                            <p class="lead">Someone recently requested that the password be reset for <%= username %>.</p>

                            <p class="callout">
                                If this is a mistake just ignore this email - your password will not be changed.
                            </p><!-- /Callout Panel -->

                            <p>This <a href='<%= by %>/login/loginWithToken?tokenKey=<%= tokenKey %>&username=<%= username %>'>link</a> will log you in and take you to a page where you can set a new password.<br />
                                Please note that this link will expire on <%= expiryDate %>. <br/>
                                You can request a new one by clicking <a href="<%= by %>/#forgotPassword">here</a> and enter your username.</p>

                                <!-- Callout Panel -->
                                <p class="callout">
                                    Trouble clicking? Copy and paste this URL into your browser:
                                    <a href="<%= by %>/login/loginWithToken?tokenKey=<%= tokenKey %>&username=<%= username %>">http://<%= by %>/login/loginWithToken?tokenKey=<%= tokenKey %>&username=<%= username %></a>
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

