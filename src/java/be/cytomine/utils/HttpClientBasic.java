package be.cytomine.utils;
//import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.*;
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 8:41
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientBasic
{
    protected URL url;
    protected HttpURLConnection server;
    String loginUser;
    String passwordUser;


    public HttpClientBasic(String url, String loginUser, String passwordUser) throws Exception
    {
        try
        {
            this.loginUser = loginUser;
            this.passwordUser = passwordUser;
            this.url = new URL(url);
        }
        catch (Exception e)
        {
            throw new Exception("Invalid URL");
        }
    }

    /**
     * @param method: String object for client method (POST, GET,...)
     */
    public void connect(String method) throws Exception
    {
        try
        {
            server = (HttpURLConnection)url.openConnection();
            byte[] encodedPassword = ( loginUser + ":" + passwordUser ).getBytes();
//            BASE64Encoder encoder = new BASE64Encoder();
            server.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(encodedPassword)));
            server.setDoInput(true);
            server.setDoOutput(true);
            server.setRequestMethod(method);
            server.setRequestProperty("Content-type","application/x-www-form-urlencoded");
            server.connect();
        }
        catch (Exception e)
        {
            throw new Exception("Connection failed");
        }
    }

    public int getResponseCode() throws Exception
    {
        return server.getResponseCode();
    }

    public void disconnect()
    {
        server.disconnect();
    }

    public String getResponseString() throws Exception
    {
        String response="";

        try
        {
            BufferedReader s = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String line = s.readLine();
            while (line != null)
            {
                response = response+line;
                line = s.readLine();
            }
            s.close();
            return response;
        }
        catch(Exception e)
        {
            throw new Exception("Unable to read input stream");
        }
    }

    public void post(String s) throws Exception
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            bw.write(s, 0, s.length());
            bw.flush();
            bw.close();
        }
        catch(Exception e)
        {
            throw new Exception("Unable to write to output stream");
        }
    }

    public static void main(String argv[])
    {
        if (argv.length == 0)
        {
            System.out.println("Usage: java HttpClient url\r\n");
            System.exit(0);
        }

        try
        {
            HttpClientBasic c = new HttpClientBasic(argv[0],"toto","lehero");
            c.connect("GET");
          //  c.displayResponse();
            c.disconnect();

            c.connect("POST");
            c.post("data=Posted request");
         //   c.displayResponse();
            c.disconnect();

            c.connect("POST");
            c.post("data=2nd request");
           // c.displayResponse();
            c.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
