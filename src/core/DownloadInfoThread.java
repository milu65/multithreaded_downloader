package core;

import java.util.concurrent.atomic.AtomicLong;

public class DownloadInfoThread implements Runnable{
    private long fileContentLength;

    public long finishedSize;

    public AtomicLong finishedSizeSec=new AtomicLong(0);

    public DownloadInfoThread(long fileContentLength) {
        this.fileContentLength = fileContentLength;
    }

    @Override
    public void run() {
        String fileSizeMB = String.format("%.2f", fileContentLength / (1024 * 1024.0));
        long currentFinishedSizeSec=finishedSizeSec.get();
        finishedSize+=currentFinishedSizeSec;
        finishedSizeSec.addAndGet(-currentFinishedSizeSec);
        String speedMBPS = String.format("%.2f", currentFinishedSizeSec/(1024*1024.0));
        String downloadProgress = String.format("%.2f",(double)finishedSize / fileContentLength*100);
        System.out.print("\r");
        System.out.print(speedMBPS+" MB/s "+ downloadProgress+ "% "+fileSizeMB+" MB total");
    }
}
