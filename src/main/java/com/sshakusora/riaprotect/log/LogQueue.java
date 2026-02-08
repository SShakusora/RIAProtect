package com.sshakusora.riaprotect.log;

import com.sshakusora.riaprotect.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogQueue {
    private static final ConcurrentLinkedQueue<LogEntry> QUEUE = new ConcurrentLinkedQueue<>();

    public static void push(LogEntry entry) {
        QUEUE.add(entry);
    }

    public static void startWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    if (!QUEUE.isEmpty()) {
                        List<LogEntry> batch = new ArrayList<>();
                        while (!QUEUE.isEmpty() && batch.size() < 100) {
                            batch.add(QUEUE.poll());
                        }
                        DatabaseHandler.saveBatch(batch);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        worker.setName("RIAProtect-Storage-Worker");
        worker.setDaemon(true);
        worker.start();
    }
}
