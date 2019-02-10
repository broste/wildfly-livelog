package de.broecheler.tools.wildfly.livelog;

public interface ITextFileTailerListener {

    void onNewTextAvailable(String newText);
    void onFileReset();

}
