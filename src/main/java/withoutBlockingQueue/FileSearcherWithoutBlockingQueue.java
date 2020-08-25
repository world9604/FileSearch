package withoutBlockingQueue;

import java.io.File;
import java.io.FileFilter;


public class FileSearcherWithoutBlockingQueue{
    public static void main(String[] args) {
        MyQueue queue = new MyQueue(100);
        FileFilter fileFilter = pathname -> true;

        File root = new File("C:\\Program Files\\GOM\\GOMCam");
        new Thread(new FileCrawler(queue, fileFilter, root)).start();

        int CONSUMER_COUNT = 10;
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            new Thread(new FileIndexer(queue)).start();
        }
    }
}

class FileCrawler implements Runnable{
    private MyQueue fileQueue;
    private File root;
    private FileFilter filter;

    FileCrawler(MyQueue queue, FileFilter filter, File file){
        this.fileQueue = queue;
        this.filter = filter;
        this.root = file;
    }

    @Override
    public void run() {
        try {
            crawl(root);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void crawl(File file) throws InterruptedException {
        File[] entries = file.listFiles(filter);
        if (entries != null) {
            for (File entry : entries)
                if (entry.isDirectory()){
                    crawl(entry);
                }else if(!aleadyIndexed(entry)){
                    fileQueue.put(entry);
                }
        }
    }

    private boolean aleadyIndexed(File entry) {
        return false;
    }
}

class FileIndexer implements Runnable{
    private MyQueue fileQueue;

    FileIndexer(MyQueue queue){
        this.fileQueue = queue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                System.out.printf("큐의 크기 : %s", fileQueue.size());
                System.out.println();
                indexFile(fileQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void indexFile(File file){
        System.out.printf("인데스 함(파일명 : %s)", file.getName());
        System.out.println();
    }
}

class MyQueue {
    private final File[] buffer;
    private int tail;
    private int head;
    private int count;

    public MyQueue(int count) {
        this.buffer = new File[count];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    public synchronized void put(File file) throws InterruptedException {
        while (count >= buffer.length) {   // 버퍼가 가득 차면 대기
            wait();
        }

        buffer[tail] = file;   // 후입
        tail = (tail + 1) % buffer.length;  // Circular 큐라서 tail 의 위치가 바뀜
        count++;
        notifyAll();  // 버퍼의 후입 되었으므로, take 해도 된다고 이벤트 알림
    }

    public synchronized File take() throws InterruptedException {
        while (count <= 0) {   // 버퍼에 아무것도 없으면 대기
            wait();
        }

        File file = buffer[head];  // 선출
        head = (head + 1) % buffer.length;  // Circular 큐라서 header 의 위치가 바뀜
        count--;
        notifyAll();  // 버퍼에서 선출 되었으므로, put 해도 된다고 이벤트 알림
        return file;
    }

    public synchronized int size(){
        return count;
    }
}