import core.DownloadManager;
import utils.HttpUtils;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.print("Downloads from (url): ");
        Scanner sc=new Scanner(System.in);
        String downloadUrl=sc.nextLine();

        if(downloadUrl.equals("")){//default download address
            downloadUrl="https://dldir1.qq.com/qqfile/qq/QQNT/0c23bedd/QQ_v6.9.18.15820.dmg";
        }

        System.out.println("downloading: "+ HttpUtils.getHttpFileName(downloadUrl));

        DownloadManager downloadManager = new DownloadManager(downloadUrl);
        long startTime=System.currentTimeMillis();
        downloadManager.multithreadedDownload();
        System.out.println("\ntime spent: "+(System.currentTimeMillis()-startTime)/1000.0);
    }
}