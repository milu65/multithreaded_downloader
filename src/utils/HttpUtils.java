package utils;

import constant.HTTPConstant;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {
    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL downloadUrl = new URL(url);

        if(downloadUrl == null){
            return null;
        }

        URLConnection urlConnection = downloadUrl.openConnection();
        urlConnection.setRequestProperty("User-Agent", HTTPConstant.USER_AGENT);

        return (HttpURLConnection) urlConnection;

    }

    public static String getHttpFileName(String url){
        return url.substring(url.lastIndexOf('/')+1);
    }

}
