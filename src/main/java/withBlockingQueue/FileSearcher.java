package withBlockingQueue;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileSearcher{
    public static void main(String[] args) {
        BlockingQueue<File> queue = new LinkedBlockingQueue<>(100);
        FileFilter fileFilter = pathname -> true;

        File root = new File("C:\\Program Files");
        new Thread(new FileCrawler(queue, fileFilter, root)).start();

        int CONSUMER_COUNT = 10;
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            new Thread(new FileIndexer(queue)).start();
        }
    }
}

class FileCrawler implements Runnable{
    private BlockingQueue<File> fileQueue;
    private File root;
    private FileFilter filter;

    FileCrawler(BlockingQueue<File> queue, FileFilter filter, File file){
        this.fileQueue = queue;
        this.filter = filter;
        this.root = file;
    }

    @Override
    public void run() {
        try {
            crawl(root);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void crawl(File file) throws InterruptedException {
        File[] entries = root.listFiles(filter);
        if (entries != null) {
            for (File entry : entries)
                if (entry.isDirectory())
                    crawl(entry);
                else if(!aleadyIndexed(entry))
                    fileQueue.put(entry);
        }
    }

    private boolean aleadyIndexed(File entry) {
        return false;
    }
}

class FileIndexer implements Runnable{
    private BlockingQueue<File> fileQueue;

    FileIndexer(BlockingQueue<File> queue){
        this.fileQueue = queue;
    }

    @Override
    public void run() {
        try {
            while(true)
                indexFile(fileQueue.take());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void indexFile(File file){
        System.out.printf("인데스 함(파일명 : %s)", file.getName());
        System.out.println();
    }
}
