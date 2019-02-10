package de.broecheler.tools.wildfly.livelog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class TextFileTailerTest {

    private static final String CR = "\n";

    private ITextFileTailerListener listener;
    private TextFileTailer cut;
    private File file;
    private PrintStream fileStream;

    @Before
    public void before() throws IOException {
        listener = mock(ITextFileTailerListener.class);
        file = createTempFile();
        fileStream = new PrintStream(file);
    }

    @After
    public void after() {
        fileStream.close();
    }

    @Test
    public void checkFile_retrieves_new_lines_added_to_log_file() {
        final String line1 = "line1";
        fileStream.println(line1);

        cut = new TextFileTailer(file.getPath(), listener);
        cut.checkFile();
        verify(listener).onNewTextAvailable(line1 + CR);

        final String line2 = "line2";
        fileStream.println(line2);
        cut.checkFile();
        verify(listener).onNewTextAvailable(line2 + CR);

        verifyNoMoreInteractions(listener);
    }


    @Test
    public void checkFile_for_not_existing_file_works_without_errors_and_no_interaction_of_listener() {
        cut = new TextFileTailer("not_existing_file.txt", listener);
        cut.checkFile();
        cut.shutdown();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void checkFile_with_log_file_being_roled_over() throws Exception {
        final String line1 = "lots and lots and lots and lots and lots of stuff";
        fileStream.println(line1);
        cut = new TextFileTailer(file.getPath(), listener);
        cut.checkFile();
        verify(listener).onNewTextAvailable(line1 + CR);

        // roll over
        closeFile();
        final String lineInNewFile = "lineInNewFile";
        fileStream = new PrintStream(file);
        fileStream.println(lineInNewFile);

        // Note: This only works if the length of the rolled over file is smaller
        //       than the previous file.
        cut.checkFile();
        verify(listener).onNewTextAvailable(lineInNewFile + CR);
    }


    void closeFile() {
        assert (file.delete()); // make sure the file really gets deleted
        fileStream.close();
    }

    File createTempFile() throws IOException {
        return File.createTempFile("TextFileTailerText", ".txt");
    }

}