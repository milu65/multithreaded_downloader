package core;

import constant.FileConstant;
import utils.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadManager {
    private long sectionLength;
    private long size = 0;
    private String url;
    private CountDownLatch countDownLatch;

    public DownloadManager(String url) {
        this(url, FileConstant.MB*25);
    }

    public DownloadManager(String url, long sectionLength){
        this.sectionLength=sectionLength;
        this.url = url;
        HttpURLConnection connection = null;
        try {
            connection=HttpUtils.getHttpURLConnection(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (connection != null) {
            size = connection.getContentLength();
            connection.disconnect();
        }
    }

    public void download(){
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

        DownloadInfoStatsThread downloadInfoStatsThread = new DownloadInfoStatsThread(size);

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(downloadInfoStatsThread,0,1, TimeUnit.SECONDS);

        try(
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ){
            int len=-1;
            byte []buffer=new byte[100*FileConstant.KB];
            while((len=bufferedInputStream.read(buffer))!=-1){
                bufferedOutputStream.write(buffer,0, len);
                downloadInfoStatsThread.finishedSizeSec.addAndGet(len);
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

    public void multithreadedDownload(){
        int numThread=(int)(size/sectionLength);
        if(size%sectionLength!=0){
            numThread++;
        }

        System.out.println("number of section: "+numThread+" section length (bytes): "+sectionLength);

        countDownLatch=new CountDownLatch(numThread);

        DownloadInfoStatsThread downloadInfoStatsThread = new DownloadInfoStatsThread(size);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(downloadInfoStatsThread,0,1, TimeUnit.SECONDS);

        //Number of threads = Number of Available Cores * (1 + Wait time / Service time)
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                11,
                11,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                new ThreadPoolExecutor.AbortPolicy());

        long offset=0;
        for(int i=0;i<numThread;i++){
            long end = offset+sectionLength-1;
            if(i==numThread-1){
                end=size;
            }
            RangeDownloaderThread rangeDownloaderThread = new RangeDownloaderThread(
                    url,
                    offset,
                    end,
                    countDownLatch,
                    downloadInfoStatsThread);
            offset+=sectionLength;
            threadPoolExecutor.submit(rangeDownloaderThread);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            scheduledExecutorService.shutdown();
            threadPoolExecutor.shutdown();
        }

        System.out.println("\ndownload completed.");
        System.out.println("merging sections.");

        ArrayList<String> sections = new ArrayList<>();
        offset=0;
        for(int i=0;i<numThread;i++){
            sections.add(String.valueOf(offset));
            offset+=sectionLength;
        }
        mergeSections(HttpUtils.getHttpFileName(url),sections);
        System.out.println("merge completed.");
    }

    public void mergeSections(String filename, List<String> sections){
        String fileLocation = FileConstant.OUTPUT_DIR+filename;

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileLocation);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for(String section:sections){
            String sectionFileLocation = FileConstant.OUTPUT_DIR+section;
            File sectionFile= new File(sectionFileLocation);
            try(
                    FileInputStream fileInputStream = new FileInputStream(sectionFile);
            ){
                byte [] buffer = new byte[100 * FileConstant.KB];
                int len = 0;
                while((len = fileInputStream.read(buffer))!=-1){
                    fileOutputStream.write(buffer,0,len);
                }
            }catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!sectionFile.delete()) {
                System.out.println("Fail to delete temporary file: "+sectionFile.getAbsoluteFile());
            }
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
