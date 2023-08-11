package core;

import constant.FileConstant;
import utils.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Downloader {

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

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

        DownloadInfoThread downloadInfoThread = new DownloadInfoThread(httpURLConnection.getContentLength());

        scheduledExecutorService.scheduleAtFixedRate(downloadInfoThread,0,1, TimeUnit.SECONDS);

        try(
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ){
            int ch=-1;
            while((ch=bufferedInputStream.read())!=-1){
                bufferedOutputStream.write(ch);
                downloadInfoThread.finishedSizeSec.addAndGet(1);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not exist.");
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            httpURLConnection.disconnect();
            scheduledExecutorService.shutdown();
        }
    }
}
