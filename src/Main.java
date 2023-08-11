import core.Downloader;
import utils.HttpUtils;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.print("Input download url: ");
        Scanner sc=new Scanner(System.in);
        String downloadUrl=sc.nextLine();
        if(downloadUrl.equals("")){
            downloadUrl="https://dldir1.qq.com/qqfile/qq/QQNT/0c23bedd/QQ_v6.9.18.15820.dmg";
        }
        System.out.println("downloading: "+ HttpUtils.getHttpFileName(downloadUrl));

        Downloader downloader = new Downloader();
        downloader.download(downloadUrl);
    }
}