package gov.nasa.jpl.output.tol;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class just takes a list of TOLRecords and one by one writes them to a StringBuilder - intended for use
 * to be passed into a thread pool by an XMLTOLWriter
 */
public class XMLTOLSnippetThread implements Callable<StringBuilder> {
    private List<TOLRecord> records;

    public XMLTOLSnippetThread(List<TOLRecord> records){
        this.records = records;
    }

    @Override
    public StringBuilder call() throws Exception {
        StringBuilder sb = new StringBuilder();
        for(TOLRecord record: records){
            sb.append(record.toXML());
        }
        return sb;
    }
}
