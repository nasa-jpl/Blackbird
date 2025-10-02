package gov.nasa.jpl.input;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static gov.nasa.jpl.input.ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE;
import static gov.nasa.jpl.resource.Resource.formUniqueName;

public class XMLTOLHistoryReader implements HistoryReader {
    private XMLStreamReader reader;
    private String fileName;
    // because we're reading this in chronologically but there may be child activities spawned
    // before their parents, we have to go back after we read everything in and assign parentage
    private HashMap<String, Map.Entry<Activity, String>> mapOfAllIDsToActivitiesAndTheirParentIDs;

    public XMLTOLHistoryReader(String fileName) throws IOException {
        this.fileName = fileName;
        mapOfAllIDsToActivitiesAndTheirParentIDs = new HashMap<>();
    }

    public void readInHistoryOfActivitiesAndResource(boolean areReadInResourcesFrozen, boolean shouldActivitiesDecompose) throws IOException {
        File initialFile = new File(fileName);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(initialFile);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            reader = inputFactory.createXMLStreamReader(inputStream);

            ModelingEngine.getEngine().setCurrentlyReadingInFile(true);
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("TOLrecord") && reader.getAttributeValue(null, "type").equals("RES_VAL")) {
                            addResourceUsageNodeToThatResourceHistory(reader, areReadInResourcesFrozen);
                        }
                        else if (elementName.equals("TOLrecord") && reader.getAttributeValue(null, "type").equals("ACT_START")) {
                            addActivityInstanceToEngineList(reader);
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        break;
                }
            }
            // now we've put all the activities into the list, but we haven't re-built the instance hierarchy
            rebuildInstanceHierarchy(mapOfAllIDsToActivitiesAndTheirParentIDs);
            // hopefully let garbage collection get these references
            mapOfAllIDsToActivitiesAndTheirParentIDs = new HashMap<>();

        }
        catch (XMLStreamException ex) {
            throw new IOException("XML reader could not correctly read file:\n" + ex.getMessage());
        }
        catch (FileNotFoundException ef) {
            throw new IOException("Could not find file " + fileName);
        }
        finally {
            ModelingEngine.getEngine().setCurrentlyReadingInFile(false);

            // close resources
            try {
                reader.close();
            }
            catch (XMLStreamException e) {
            }
            inputStream.close();
        }
    }

    private void addActivityInstanceToEngineList(XMLStreamReader reader) throws IOException {
        StringBuilder activityTypeName = new StringBuilder();
        StringBuilder actID = new StringBuilder();
        StringBuilder parentID = new StringBuilder();
        ArrayList<Object> args = new ArrayList<>();

        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("TimeStamp")) {
                            args.add(new Time(reader.getElementText()));
                        }
                        else if (elementName.equals("Instance")) {
                            parseActivityInstance(reader, activityTypeName, actID, parentID, args);
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        // find the correct activity class
                        ActivityTypeList actList = ActivityTypeList.getActivityList();
                        Class<?> cls = actList.getActivityClass(activityTypeName.toString());
                        Activity activityInstance = (Activity) ConstructorUtils.invokeConstructor(cls, args.toArray());
                        activityInstance.setID(UUID.fromString(actID.toString()));
                        // the constructor has put it in the activity instance list, but we want it in a hashmap
                        // to not look up activities slowly when doing parentage
                        mapOfAllIDsToActivitiesAndTheirParentIDs.put(actID.toString(), new AbstractMap.SimpleEntry<>(activityInstance, parentID.toString()));
                        return;
                }
            }
        }
        catch (XMLStreamException e) {
            return;
        }
        catch (NoSuchMethodException e) {
            throw new IOException("Could not find constructor for activity type " + activityTypeName
                + ": " + e.toString());
        }
        catch (InstantiationException e) {
            throw new IOException("Could not execute constructor for activity type " + activityTypeName);
        }
        catch (IllegalAccessException e) {
            throw new IOException("Did not have permission to execute constructor for activity type " + activityTypeName);
        }
        catch (InvocationTargetException e) {
            throw new IOException("Could not invoke constructor for activity type " + activityTypeName);
        }
    }

    private void addResourceUsageNodeToThatResourceHistory(XMLStreamReader reader, boolean areReadInResourcesFrozen) {
        try {
            Time nodeTime = null;
            String name = "";
            List<String> indices = new ArrayList<>();
            Comparable value = null;


            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Name")) {
                            name = reader.getElementText();
                        }
                        else if (elementName.equals("Index")) {
                            indices.add(reader.getElementText());
                        }
                        else if (elementName.contains("Value")) {
                            value = parseValueFromXML(elementName, reader.getElementText());
                        }
                        else if (elementName.contains("TimeStamp")) {
                            nodeTime = new Time(reader.getElementText());
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        ModelingEngine.getEngine().setTime(nodeTime);
                        String fullName = (indices.isEmpty()) ? name : formUniqueName(name, indices);
                        Resource<Comparable> resourceThisNodeAppendsTo = ((Resource<Comparable>) ResourceList.getResourceList().get(fullName));
                        resourceThisNodeAppendsTo.set(value);
                        resourceThisNodeAppendsTo.setFrozen(areReadInResourcesFrozen);
                        return;
                }
            }
        }
        catch (XMLStreamException e) {
            return;
        }
    }

    private void parseActivityInstance(XMLStreamReader reader, StringBuilder nameBuilder, StringBuilder idBuilder, StringBuilder parentBuilder, List<Object> args) throws IOException {
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Type")) {
                            nameBuilder.append(reader.getElementText());
                        }
                        else if (elementName.equals("ID")) {
                            idBuilder.append(reader.getElementText());
                        }
                        else if (elementName.equals("Parent")) {
                            parentBuilder.append(reader.getElementText());
                        }
                        else if (elementName.equals("Parameters")) {
                            getParametersFromXML(reader, args);
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (reader.getLocalName().equals("Instance")) {
                            return;
                        }
                }
            }
        }
        catch (XMLStreamException e) {
            return;
        }
    }

    public static void rebuildInstanceHierarchy(Map<String, Map.Entry<Activity, String>> parentInformation){
        for (Map.Entry<Activity, String> act : parentInformation.values()) {
            // if the 'parent' String is null or "", we don't have a parent activity and skip the assign step
            if (act.getValue() != null && !act.getValue().isEmpty() && parentInformation.containsKey(act.getValue())) {
                Activity parent = parentInformation.get(act.getValue()).getKey();
                // this is the step that adds to the children list of one activity and sets the parent field of the other
                parent.spawn(act.getKey());
            }
        }
    }

    private void getParametersFromXML(XMLStreamReader reader, List<Object> args) throws IOException {
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Parameter"))
                            args.add(getSingleParameterFromXML(reader));
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        return;
                }
            }
        }
        catch (XMLStreamException e) {
            return;
        }
    }

    private Object getSingleParameterFromXML(XMLStreamReader reader) throws IOException {
        Object parameter = new Object();

        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("StructValue")) {
                            parameter = parseMapFromXML(reader);
                        }
                        else if (elementName.equals("ListValue")) {
                            parameter = parseListFromXML(reader);

                        }
                        else if (elementName.endsWith("Value")) {
                            parameter = parseValueFromXML(elementName, reader.getElementText());
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        // we don't want to quit if we come to the end of the name
                        if (!reader.getLocalName().equals("Name")) {
                            return parameter;
                        }
                }
            }
        }
        catch (XMLStreamException e) {
            throw new IOException("XMLStreamReader failed while reading parameter");
        }
        return null;
    }

    private List<Object> parseListFromXML(XMLStreamReader reader) throws IOException {
        List toReturn = new ArrayList<>();
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Element")) {
                            toReturn.add(getSingleParameterFromXML(reader));
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        return toReturn;
                }
            }
        }
        catch (XMLStreamException e) {
            throw new IOException("XMLStreamReader failed while reading parameter");
        }
        return null;
    }

    private Map<Object, Object> parseMapFromXML(XMLStreamReader reader) throws IOException {
        Map toReturn = new HashMap<>();
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (elementName.equals("Element")) {
                            String key = reader.getAttributeValue(null, "index");
                            toReturn.put(key, getSingleParameterFromXML(reader));
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        return toReturn;
                }
            }
        }
        catch (XMLStreamException e) {
            throw new IOException("XMLStreamReader failed while reading parameter");
        }
        return null;
    }

    // used for this reader and XML incon reader
    static Comparable parseValueFromXML(String elementName, String elementText) {
        String type = elementName.replace("Value", "");
        return (Comparable) ReflectionUtilities.returnValueOf(type.equals("Time") ? ABSOLUTE_TIME_CLASS_PACKAGE : type, elementText, false);
    }

}
