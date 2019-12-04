package src;

public class Threading extends Thread{
    private spider spider;

    public Threading(spider s) {
        this.spider = s;
    }

    public void run() {
        try {
            spider.search();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
