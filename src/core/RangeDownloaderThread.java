package core;

import constant.FileConstant;
import utils.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

public class RangeDownloaderThread implements Runnable{
    private String url;
    private long begin;
    private long end;
    private CountDownLatch countDownLatch;
    private DownloadInfoStatsThread downloadInfoStatsThread;

    public RangeDownloaderThread(String url, long begin, long end, CountDownLatch countDownLatch, DownloadInfoStatsThread downloadInfoStatsThread) {
        this.url = url;
        this.begin = begin;
        this.end = end;
        this.countDownLatch = countDownLatch;
        this.downloadInfoStatsThread = downloadInfoStatsThread;
    }

    @Override
    public void run() {
//        System.out.println("downloading section"+begin);
        HttpURLConnection httpURLConnection = null;
        String fileLocation = FileConstant.OUTPUT_DIR+begin;

        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);
            if (httpURLConnection == null) {
                throw new IOException();
            }
        } catch (IOException e) {//TODO: retry when thread got a exception
            throw new RuntimeException(e);
        }

        httpURLConnection.setRequestProperty("RANGE","bytes="+begin+"-"+end);

        try(
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ){
            int len=-1;
            byte []buffer=new byte[100* FileConstant.KB];
            while((len=bufferedInputStream.read(buffer))!=-1){
                bufferedOutputStream.write(buffer,0, len);
                downloadInfoStatsThread.finishedSizeSec.addAndGet(len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not exist.");
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            httpURLConnection.disconnect();
        }
//        System.out.println(begin+" - "+end+ " size:"+(end-begin));
        countDownLatch.countDown();
    }
}
