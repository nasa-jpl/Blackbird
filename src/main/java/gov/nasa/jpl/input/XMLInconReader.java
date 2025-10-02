package gov.nasa.jpl.input;

import gov.nasa.jpl.engine.InitialConditionList;
import gov.nasa.jpl.time.Time;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static gov.nasa.jpl.input.XMLTOLHistoryReader.parseValueFromXML;
import static gov.nasa.jpl.resource.Resource.formUniqueName;

public class XMLInconReader implements InconReader {
    private XMLStreamReader reader;
    private String inconName;

    public XMLInconReader(String inconName) {
        this.inconName = inconName;
    }

    @Override
    public InitialConditionList getInitialConditions() throws IOException {
        File initialFile = new File(inconName);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(initialFile);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            reader = inputFactory.createXMLStreamReader(inputStream);

            InitialConditionList incon = new InitialConditionList();
            Time inconTime = new Time();
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        // TOLrecord is actually a top-level tag in an XMLTOL
                        if (elementName.equals("TOLrecord") && reader.getAttributeValue(null, "type").equals("RES_FINAL_VAL")) {
                            // now that we know we're in a RES_FINAL_VAL block, the next tag should be "Timestamp"
                            reader.next(); //it captures a newline
                            reader.next(); // then "<TimeStamp>"
                            // if the time has not been initialized (it is zero), then assign it
                            if (inconTime.equals(new Time())) {
                                inconTime.valueOf(reader.getElementText());
                            }
                            // this captures the end of "timestamp"
                            reader.next(); // captures a newline
                            reader.next(); // captures end of <TimeStamp>
                            addXMLBlockToInconList(reader, incon);
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        break;
                }
            }
            incon.setInconTime(inconTime);
            return incon;
        }
        catch (XMLStreamException | IOException e) {
            throw new IOException("Could not open or read XML incon file:\n" + e.getMessage());
        }
        finally {
            try {
                reader.close();
            }
            catch (XMLStreamException e) {
            }
            inputStream.close();
        }
    }

    private void addXMLBlockToInconList(XMLStreamReader reader, InitialConditionList incon) {
        try {
            String name = "";
            List<String> indices = new ArrayList<>();

            Comparable value = null;

            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Name"))
                            name = reader.getElementText();
                        else if (elementName.equals("Index"))
                            indices.add(reader.getElementText());
                        else if (elementName.contains("Value"))
                            value = parseValueFromXML(elementName, reader.getElementText());
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        String fullName = (indices.isEmpty()) ? name : formUniqueName(name, indices);
                        incon.addToInconList(fullName, value);
                        return;
                }
            }
        }
        catch (XMLStreamException e) {
            return;
        }
    }
}
