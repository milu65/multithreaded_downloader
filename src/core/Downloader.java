package core;

import constant.FileConstant;
import utils.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;

public class Downloader {
    public void download(String url){
        String fileName = HttpUtils.getHttpFileName(url);
        String fileLocation = FileConstant.OUTPUT_DIR+fileName;
        HttpURLConnection httpURLConnection = null;
        try{
             httpURLConnection = HttpUtils.getHttpURLConnection(url);
        }catch(IOException e){
            e.printStackTrace();
        }

        if(httpURLConnection==null){
            return;
        }

        try(
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ){
            int ch=-1;
            while((ch=bufferedInputStream.read())!=-1){
                bufferedOutputStream.write(ch);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not exist.");
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            httpURLConnection.disconnect();
        }
    }
}
