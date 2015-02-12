package be.cytomine.test

import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.protocol.ClientContext
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.cookie.Cookie
import org.apache.http.entity.*
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.protocol.BasicHttpContext

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import org.apache.http.client.CookieStore;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext

import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.log4j.Logger;
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 11/02/11
 * Time: 8:18
 * Http client used in test
 */
class HttpClient {

    DefaultHttpClient client
    HttpHost targetHost
    BasicHttpContext localcontext
    URL URL
    HttpResponse response
    int timeout = 300000;

    public List<Cookie> cookies

    private Log log = LogFactory.getLog(HttpClient.class)

    /**
     * Create a connection to a specific URL
     * @param url Url
     * @param username Login
     * @param password Password
     */
    void connect(String url, String username, String password) {
        log.debug("Connection to " + url + " with login=" + username + " and pass=" + password)
        URL = new URL(url)
        targetHost = new HttpHost(URL.getHost(), URL.getPort());
        client = new DefaultHttpClient();
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();

        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        // Set credentials
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password)
        HttpParams params = client.getParams()
        HttpConnectionParams.setConnectionTimeout(params, timeout)
        HttpConnectionParams.setSoTimeout(params, timeout)
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds)
    }

    void connect(String url, int port) {
        log.debug("Connection to " + url + " with port " + port )
        URL = new URL(url)
        targetHost = new HttpHost(URL.getHost(), port);
        InsecureHttpClientFactory ssl = new InsecureHttpClientFactory()
        client = ssl.buildHttpClient() //new DefaultHttpClient();
        localcontext = new BasicHttpContext();
        HttpParams params = client.getParams()
        HttpConnectionParams.setConnectionTimeout(params, timeout)
        HttpConnectionParams.setSoTimeout(params, timeout)


    }

    public List<Cookie> getCookies() {
        List<Cookie> cookies = client.getCookieStore().getCookies();
        return cookies;
    }

    public void putCookies() {
        if(cookies!=null) {
            for(int i=0;i<cookies.size();i++) {
                client.getCookieStore().addCookie(cookies.get(i));
            }
        }
    }

    public void printCookies() {
        System.out.println("############# PRINT COOKIE #############");
        List<Cookie> cookies = client.getCookieStore().getCookies();
        System.out.println(cookies);
        if(cookies != null)
        {
            for(Cookie cookie : cookies)
            {
                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                System.out.println(cookieString);
            }
        }
    }

    public static HttpClient getClientWithCookie(String host,String username, String password) throws Exception {
        HttpClient client = null;
        String suburl = "/j_spring_security_check";
        client = new HttpClient();
        client.connect(host + suburl, username, password);
        client.post("j_username=lrollus&j_email=&j_password=lR%242011&remember_me=on");
        client.cookies = client.client.getCookieStore().getCookies();
        if( client.response!=null && client.response.getEntity() != null ) {
            client.response.getEntity().consumeContent();
        }//if
//        cookies
//
//        String response = client.getResponseData();
//        if(cookies!=null) {
//            for(int i=0;i<cookies.size();i++) {
//                client.getCookieStore().addCookie(cookies.get(i));
//            }
//        }
//        client.disconnect();
//        cookies = client.getCookies();
        return client;
    }

    /**
     * Do get action
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     */
    void get() {
        log.info("Get " + URL.toString())
        HttpGet httpGet = new HttpGet(URL.toString());
        response = client.execute(targetHost, httpGet, localcontext);
    }

    /**
     * Do get action and get data as byte array
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     * @return Data as byte array
     * @throws MalformedURLException
     * @throws IOException
     * @throws Exception
     */
    byte[] getData() throws MalformedURLException, IOException, Exception {
        HttpGet httpGet = new HttpGet(URL.toString());
        httpGet.getParams().setParameter("http.socket.timeout", new Integer(timeout));
        response = client.execute(targetHost, httpGet, localcontext);
        log.info("url=" + URL.toString() + " is " + response.getStatusLine().statusCode);

        boolean isOK = (response.getStatusLine().statusCode == HttpURLConnection.HTTP_OK);
        boolean isFound = (response.getStatusLine().statusCode == HttpURLConnection.HTTP_MOVED_TEMP);
        boolean isErrorServer = (response.getStatusLine().statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR);

        if (!isOK && !isFound & !isErrorServer) throw new IOException(URL.toString() + " cannot be read: " + response.getStatusLine().statusCode);
        HttpEntity entity = response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (entity != null) {
            entity.writeTo(baos)
        }
        return baos.toByteArray()
    }

    /**
     * Do delete action
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     */
    void delete() {
        log.info("Delete " + URL.toString())
        HttpDelete httpDelete = new HttpDelete(URL.toString());
        response = client.execute(targetHost, httpDelete, localcontext);
    }

    /**
     * Do post action
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     * @param data Data for post action
     */
//    void post(def data) {
//        log.debug("Post " + URL.toString())
//        HttpPost httpPost = new HttpPost(URL.toString());
//        log.info("Post send :" + data.replace("\n", ""))
//        //write data
//        ContentProducer cp = new ContentProducer() {
//            public void writeTo(OutputStream outstream) throws IOException {
//                Writer writer = new OutputStreamWriter(outstream, "UTF-8");
//                writer.write(data);
//                writer.flush();
//            }
//        };
//        HttpEntity entity = new EntityTemplate(cp);
//        httpPost.setEntity(entity);
//
//        response = client.execute(targetHost, httpPost, localcontext);
//    }

    public void post(String data, String contentType = null) throws Exception {
        HttpPost httpPost = new HttpPost(URL.toString());
        println URL.toString()
//        println "###"+data
//        httpPost.addHeader("Content-Type","application/json")
//        httpPost.addHeader("host",this.host)
        log.debug("Post send :" + data.replace("\n", ""));

        if(contentType) {
            httpPost.setHeader("Content-type",contentType);
        }


        //write data
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(data.getBytes()));

        BufferedHttpEntity ent = new BufferedHttpEntity(entity);
        //entity.setContentLength((long)data.getBytes().length);
        httpPost.setEntity(ent);
        response = client.execute(targetHost, httpPost, localcontext);
    }


    /**
     * Do put action
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     * @param data Data for put action
     */
    void put(String data) {
        log.debug("Put " + URL.toString())
        HttpPut httpPut = new HttpPut(URL.toString());
        log.debug("Put send :" + data.replace("\n", ""))
        //write data
        ContentProducer cp = new ContentProducer() {
            public void writeTo(OutputStream outstream) throws IOException {
                Writer writer = new OutputStreamWriter(outstream, "UTF-8");
                writer.write(data);
                writer.flush();
            }
        };
        HttpEntity entity = new EntityTemplate(cp);
        httpPut.setEntity(entity);

        response = client.execute(targetHost, httpPut, localcontext);
    }

    public int post(MultipartEntity entity) throws Exception {
        log.debug("POST " + URL.toString());
        HttpPost httpPost = new HttpPost(URL.toString());
        log.debug("Post send :" + entity);
        httpPost.setEntity(entity);
        response = client.execute(targetHost, httpPost, localcontext);
        return response.getStatusLine().getStatusCode();

    }

    /**
     * Do put action
     * Response is saved and can be retrieved with getResponseCode()/getResponseData()
     * @param data Data for put action
     */
    void put(byte[] data) {
        log.debug("Put " + URL.getPath())
        HttpPut httpPut = new HttpPut(URL.getPath()); ;
        log.debug("Put send :" + data.length)

        InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(data), -1);
        reqEntity.setContentType("binary/octet-stream");
        reqEntity.setChunked(false);
        httpPut.setEntity(reqEntity);
        response = client.execute(targetHost, httpPut, localcontext);
    }

    /**
     * Get response data as a String
     * @return response data
     */
    String getResponseData() {
        HttpEntity entityResponse = response.getEntity();
        String content = IOUtils.toString(entityResponse.getContent());
        log.debug("Response :" + content.replace("\n", ""))
        content
    }

    /**
     * Get response code
     * @return response code
     */
    int getResponseCode() {
        log.debug("Code :" + response.getStatusLine().getStatusCode())
        return response.getStatusLine().getStatusCode()
    }

    /**
     * Close connection
     */
    void disconnect() {
        log.debug("Disconnect")
        try {client.getConnectionManager().shutdown();} catch (Exception e) {log.error(e)}
    }


//     public BufferedImage readBufferedImageFromURLWithoutKey(String url, String loginHTTP, String passHTTP) throws MalformedURLException, IOException {
//
//        URL URL = new URL(url);
//        HttpHost targetHost = new HttpHost(URL.getHost(), URL.getPort());
//        DefaultHttpClient client = new DefaultHttpClient();
//        // Create AuthCache instance
//        AuthCache authCache = new BasicAuthCache();
//        // Generate BASIC scheme object and add it to the local
//        // auth cache
//        BasicScheme basicAuth = new BasicScheme();
//        authCache.put(targetHost, basicAuth);
//
//        // Add AuthCache to the execution context
//        BasicHttpContext localcontext = new BasicHttpContext();
//        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
//        // Set credentials
//        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(loginHTTP, passHTTP);
//        client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
//
//        BufferedImage img = null;
//        HttpGet httpGet = new HttpGet(URL.getPath());
//        HttpResponse response = client.execute(targetHost, httpGet, localcontext);
//        int code = response.getStatusLine().getStatusCode();
//        log.info("url=" + url + " is " + code + "(OK=" + HttpURLConnection.HTTP_OK + ",MOVED=" + HttpURLConnection.HTTP_MOVED_TEMP + ")");
//
//        boolean isOK = (code == HttpURLConnection.HTTP_OK);
//        boolean isFound = (code == HttpURLConnection.HTTP_MOVED_TEMP);
//        boolean isErrorServer = (code == HttpURLConnection.HTTP_INTERNAL_ERROR);
//
//        if (!isOK && !isFound & !isErrorServer) throw new IOException(url + " cannot be read: " + code);
//        HttpEntity entity = response.getEntity();
//         log.info "entity="+entity
//        if (entity != null) {
//            img = ImageIO.read(entity.getContent());
//            entity.getContent().close();
//        }
//         log.info "img="+img
//         log.info "img.width="+img?.width
//        try {client.getConnectionManager().shutdown();} catch (Exception e) {log.error(e)}
//
//        return img;
//
//    }

    private HttpEntity readDataFromURLWithoutKey(String url, String loginHTTP, String passHTTP) throws MalformedURLException, IOException {
        String encoded = url;

       URL URL = new URL(encoded);
       HttpHost targetHost = new HttpHost(URL.getHost(), URL.getPort());
       DefaultHttpClient client = new DefaultHttpClient();
       // Create AuthCache instance
       AuthCache authCache = new BasicAuthCache();
       // Generate BASIC scheme object and add it to the local
       // auth cache
       BasicScheme basicAuth = new BasicScheme();
       authCache.put(targetHost, basicAuth);

       // Add AuthCache to the execution context
       BasicHttpContext localcontext = new BasicHttpContext();
       localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
       // Set credentials
       UsernamePasswordCredentials creds = new UsernamePasswordCredentials(loginHTTP, passHTTP);
       client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);

       HttpGet httpGet = new HttpGet(URL.toString());
       HttpResponse response = client.execute(targetHost, httpGet, localcontext);
       int code = response.getStatusLine().getStatusCode();
       log.info("url=" + encoded + " is " + code + "(OK=" + HttpURLConnection.HTTP_OK + ",MOVED=" + HttpURLConnection.HTTP_MOVED_TEMP + ")");

       boolean isOK = (code == HttpURLConnection.HTTP_OK);
       boolean isFound = (code == HttpURLConnection.HTTP_MOVED_TEMP);
       boolean isErrorServer = (code == HttpURLConnection.HTTP_INTERNAL_ERROR);

       if (!isOK && !isFound & !isErrorServer) throw new IOException(url + " cannot be read: " + code);
       HttpEntity entity = response.getEntity();

       return entity;

   }


    public BufferedImage readBufferedImageFromURLWithoutKey(String url, String loginHTTP, String passHTTP) throws MalformedURLException, IOException {
       HttpEntity entity = readDataFromURLWithoutKey(url,loginHTTP,passHTTP);
         log.info "entity="+entity
        BufferedImage img = null;
        if (entity != null) {
            img = ImageIO.read(entity.getContent());
            entity.getContent().close();
        }
        return img;
    }


    public static BufferedImage readBufferedImageFromURLWithRedirect(String url,String loginHTTP, String passHTTP) throws MalformedURLException, IOException {
        //logger.info("readBufferedImageFromURLWithBasicAuth:"+url +" login="+loginHTTP);
        URL URL = new URL(url);
        HttpHost targetHost = new HttpHost(URL.getHost(), URL.getPort());
        DefaultHttpClient client = new DefaultHttpClient();
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        // Set credentials
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(loginHTTP, passHTTP);
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);

        BufferedImage img = null;
        HttpGet httpGet = new HttpGet(URL.toString());
        HttpResponse response = client.execute(targetHost, httpGet, localcontext);
        int code = response.getStatusLine().getStatusCode();
        System.out.println("url="+url + " is " + code + "(OK="+HttpURLConnection.HTTP_OK +",MOVED="+HttpURLConnection.HTTP_MOVED_TEMP+")");

        boolean isOK = (code == HttpURLConnection.HTTP_OK);
        boolean isFound = (code == HttpURLConnection.HTTP_MOVED_TEMP);
        boolean isErrorServer = (code == HttpURLConnection.HTTP_INTERNAL_ERROR);

        if(!isOK && !isFound & !isErrorServer) {
            throw new IOException(url + " cannot be read: "+code);
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            img = ImageIO.read(entity.getContent());
        }
        return img;

    }



}

class InsecureHttpClientFactory {
    protected Logger log = Logger.getLogger(this.getClass());
    public DefaultHttpClient hc

    public DefaultHttpClient buildHttpClient() {
        hc = new DefaultHttpClient();
//        configureProxy();
        configureCookieStore();
        configureSSLHandling();
        return hc
    }

//    private void configureProxy(String host,int port) {
//        HttpHost proxy = new HttpHost("proxy.example.org", 3182);
//        hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//    }

    private void configureCookieStore() {
        CookieStore cStore = new BasicCookieStore();
        hc.setCookieStore(cStore);
    }

    private void configureSSLHandling() {
        Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
        SSLSocketFactory sf = buildSSLSocketFactory();
        Scheme https = new Scheme("https", 443, sf);
        SchemeRegistry sr = hc.getConnectionManager().getSchemeRegistry();
        sr.register(http);
        sr.register(https);
    }

    private SSLSocketFactory buildSSLSocketFactory() {
        TrustStrategy ts = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true; // heck yea!
            }
        };

        SSLSocketFactory sf = null;

        try {
            /* build socket factory with hostname verification turned off. */
            sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (KeyManagementException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (KeyStoreException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (UnrecoverableKeyException e) {
            log.error("Failed to initialize SSL handling.", e);
        }

        return sf;
    }

}