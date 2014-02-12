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
                                You receive this email because <%= senderFirstname %> <%= senderLastname %> (<%=  senderEmail %>) invited you to join Cytomine. Welcome !
                            </p>

                            <p class="callout">
                                Your Cytomine username is <%= username %>
                            </p>

                            <p>
                                Click <a href='<%= by %>/login/loginWithToken?tokenKey=<%= tokenKey %>&username=<%= username %>&redirect=#account'> here</a> to sign in, set your firstname, lastname and change your password. <br />
                                Please note that this link will expire on <%= expiryDate %>. You can request a new one by clicking <a href="<%= by %>/#forgotPassword">here</a> and enter your username.
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
