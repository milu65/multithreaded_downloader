package core;

import constant.FileConstant;

import java.util.concurrent.atomic.AtomicLong;

public class DownloadInfoStatsThread implements Runnable{
    private long size;

    public long finishedSize;

    public AtomicLong finishedSizeSec=new AtomicLong(0);

    public DownloadInfoStatsThread(long size) {
        this.size = size;
    }

    @Override
    public void run() {
        String fileSizeMB = String.format("%.2f", size / (double)FileConstant.MB);
        String finishedSizeMB = String.format("%.2f", finishedSize / (double)FileConstant.MB);
        long currentFinishedSizeSec=finishedSizeSec.get();
        finishedSize+=currentFinishedSizeSec;
        finishedSizeSec.addAndGet(-currentFinishedSizeSec);
        String speedMBPS = String.format("%.2f", currentFinishedSizeSec/(1024*1024.0));
        String downloadProgress = String.format("%.2f",(double)finishedSize / size*100);
        System.out.print("\r");
        System.out.print(speedMBPS+" MB/s "+ downloadProgress+ "% "+finishedSizeMB+" MB/"+fileSizeMB+" MB");
    }
}
