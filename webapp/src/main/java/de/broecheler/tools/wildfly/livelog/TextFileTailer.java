package de.broecheler.tools.wildfly.livelog;

import java.io.IOException;
import java.io.RandomAccessFile;

class TextFileTailer {

    private static final int MAX_BUFFER_SIZE = 50000;
    private final String filePath;
    private volatile ITextFileTailerListener listener;
    private long lastFilePointer;

    TextFileTailer(String filePath, ITextFileTailerListener listener) {
        this.filePath = filePath;
        this.listener = listener;
    }

    void checkFile() {
        // FIXME We simply trail the file by looking at it length. There are corner cases where
        //       this yields wrong results, but for what is typically expected in a dev enivornment this is hopefully good enough
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
            if (randomAccessFile.length() == lastFilePointer) {
                // file length has not changed so we assume there is no new content available
                return;
            } else {
                if (randomAccessFile.length() > lastFilePointer) {
                    // log file has grown so start reading from where we left off previously
                    randomAccessFile.seek(lastFilePointer);
                }
                StringBuilder buffer = new StringBuilder();
                try {
                    String line;
                    // FIXME: RandomAccessFile.readLine() corrupts strings with umlauts because of limited unicode support, this is a JDK limitation 
                    while ((line = randomAccessFile.readLine()) != null) {
                        buffer.append(line).append("\n");
                        if (buffer.length() > MAX_BUFFER_SIZE) {
                            notifyListenerOnNewTextAvailable(buffer);
                            buffer = new StringBuilder();
                            lastFilePointer = randomAccessFile.getFilePointer();
                        }
                    }
                    notifyListenerOnNewTextAvailable(buffer);
                    lastFilePointer = randomAccessFile.getFilePointer();
                } catch (IOException ex) {
                    if (listener != null) {
                        notifyListenerOnNewTextAvailable(buffer);
                        listener.onFileReset();
                    }
                }
            }
        } catch (IOException e) {
            lastFilePointer = 0;
        }
    }

    private void notifyListenerOnNewTextAvailable(StringBuilder buffer) {
        // listener may be null when shutdown was issued which naturally triggers a new log line so there might really be no listener
        if (buffer.length() > 0 && listener != null) {
            listener.onNewTextAvailable(buffer.toString());
        }
    }

    void shutdown() {
        listener = null;
    }

}
