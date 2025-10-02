package gov.nasa.jpl.input;

import gov.nasa.jpl.time.Time;

import java.util.regex.Pattern;

public class RegexUtilities {

    // because this is a utilities class with just static fields, we never want anyone to instantiate
    // an object of this, so we hide the public constructor by making a private one
    private RegexUtilities() {
    }
    // UUID regex
    public static final String UUID_REGEX_STRING = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
    public static final Pattern UUID_REGEX = Pattern.compile(UUID_REGEX_STRING);

    // Command names
    public static final String EDIT_ACTIVITY = "EDIT_ACTIVITY";
    public static final String INCON = "INCON";
    public static final String LOAD_KERNELS = "LOAD_KERNELS";
    public static final String MOVE_ACTIVITY = "MOVE_ACTIVITY";
    public static final String NEW_ACTIVITY = "NEW_ACTIVITY";
    public static final String OPEN_FILE = "OPEN_FILE";
    public static final String QUIT = "QUIT";
    public static final String REDECOMPOSE = "REDECOMPOSE";
    public static final String REDO = "REDO";
    public static final String REMODEL = "REMODEL";
    public static final String REMOVE_ACTIVITY = "REMOVE_ACTIVITY";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String SET_PARAMETER = "SET_PARAMETER";
    public static final String SEVER_ACTIVITY = "SEVER_ACTIVITY";
    public static final String UNDO = "UNDO";
    public static final String WRITE = "WRITE";
    public static final String CREATE_DICTIONARY = "CREATE_DICTIONARY";
    public static final String SEQUENCE = "SEQUENCE";
    public static final String INCLUDE_STRING = "INCLUDE";
    public static final String EXCLUDE_STRING = "EXCLUDE";

    // regex needed for command strings
    public static final Pattern COMMAND_ACT_PARAM_PATTERN = Pattern.compile("^\\s*(?<name>[a-zA-Z_0-9\\.]+)\\s*\\((?<params>.*)\\)\\s*$");
    public static final Pattern COMMAND_ID_PATTERN = Pattern.compile("^\\s*(?<id>" + UUID_REGEX_STRING + ")\\s*$");
    public static final Pattern COMMAND_ID_PARAM_PATTERN = Pattern.compile("^\\s*(?<id>" + UUID_REGEX_STRING + ")\\s*\\((?<params>.*)\\)\\s*$");
    public static final Pattern PARAMETER_TYPE_VALUE_PATTERN = Pattern.compile("^\\s*(?<type>[a-zA-Z0-9\\.]+)\\s+(?<value>.*)$");
    public static final Pattern PARAMETER_NAME_VALUE_PATTERN = Pattern.compile("^\\s*(?<name>[a-zA-Z0-9_]+)\\s+(?<value>.*)$");
    public static final Pattern START_TIME_PATTERN = Pattern.compile("START\\s+(?<time>" + Time.TIME_REGEX + ")");
    public static final Pattern END_TIME_PATTERN = Pattern.compile("END\\s+(?<time>" + Time.TIME_REGEX + ")");

    public static final String WRITE_ACTIVITIES_STRING = "ACTIVITIES\\s+(?<action>" + INCLUDE_STRING +
            "|" + EXCLUDE_STRING + ")\\s+\\(\\s*(?<activities>(?:[a-zA-Z0-9_]+\\s*?)+)\\s*\\)";
    public static final Pattern WRITE_ACTIVITIES_PATTERN = Pattern.compile(WRITE_ACTIVITIES_STRING);

    public static final String WRITE_RESOURCES_STRING = "RESOURCES\\s+(?<action>" + INCLUDE_STRING +
            "|" + EXCLUDE_STRING + ")\\s+\\(\\s*(?<resources>(?:[a-zA-Z0-9_\\[\\]]+\\s*?)+)\\s*\\)";
    public static final Pattern WRITE_RESOURCES_PATTERN = Pattern.compile(WRITE_RESOURCES_STRING);


    public static final String WRITE_CONSTRAINTS_STRING = "CONSTRAINTS\\s+(?<action>" + INCLUDE_STRING +
            "|" + EXCLUDE_STRING + ")\\s+\\(\\s*(?<constraints>(?:[a-zA-Z0-9_]+\\s*?)+)\\s*\\)";
    public static final Pattern WRITE_CONSTRAINTS_PATTERN = Pattern.compile(WRITE_CONSTRAINTS_STRING);

    public static final String WRITE_START_STRING = "START\\s+(?<time>" + Time.TIME_REGEX + ")";
    public static final String WRITE_END_STRING = "END\\s+(?<time>" + Time.TIME_REGEX + ")";
    public static final Pattern WRITE_START_PATTERN = Pattern.compile(WRITE_START_STRING);
    public static final Pattern WRITE_END_PATTERN = Pattern.compile(WRITE_END_STRING);

    // regex used to check that a WRITE command contains only expected options, and in expected format
    // replaceAll gets rid of all named regex groups to prevent collisions
    public static final Pattern WRITE_COMMAND_PATTERN = Pattern.compile(("^([^\\s]+)(\\s+(((" + WRITE_START_STRING + ")|(" + WRITE_END_STRING + ")|(" + WRITE_ACTIVITIES_STRING + ")|(" + WRITE_RESOURCES_STRING + ")|(" + WRITE_CONSTRAINTS_STRING + "))\\s*)*)?$").replaceAll("\\?<[a-zA-Z0-9]+>", ""));

    // regex used to check that a CREATE_DICTIONARY command contains only expected options, and in expected format
    // replaceAll gets rid of all named regex groups to prevent collisions
    public static final Pattern CREATE_DICTIONARY_PATTERN = Pattern.compile(("^([^\\s]+)(\\s+(((" + WRITE_ACTIVITIES_STRING + ")|(" + WRITE_RESOURCES_STRING + ")|(" + WRITE_CONSTRAINTS_STRING + "))\\s*)*)?$").replaceAll("\\?<[a-zA-Z0-9]+>", ""));

    // regex for sequence command requires start and end times
    // replaceAll gets rid of all named regex groups to prevent collisions for start/end named regex group "time"
    public static final Pattern SEQUENCE_COMMAND_PATTERN = Pattern.compile(("^\\s*" + WRITE_START_STRING + " " + WRITE_END_STRING + "\\s*$").replaceAll("\\?<[a-zA-Z0-9]+>", ""));

    // regex for all built-in wrapper data types, needed for calling valueOf correctly
    // may need to put in case for primitive data types
    public static final Pattern STRING_DATA_TYPE_REGEX = Pattern.compile("^\\s*(?:java.lang.)?(?<type>String)\\s*$");
    public static final Pattern STRING_VALUE_REGEX = Pattern.compile("^\\s*\"(?<value>.*)\"\\s*$");
    public static final Pattern WRAPPER_DATA_TYPE_REGEX = Pattern.compile("^\\s*(?:java.lang.)?(?<type>String|Boolean|Integer|Long|Float|Double)\\s*$");

    // regex needed for maps and lists
    // types
    public static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^\\s*(?:java.util.)?List<(?<genericType>.*)>\\s*$");
    public static final Pattern MAP_TYPE_PATTERN = Pattern.compile("^\\s*(?:java.util.)?Map<(?<genericTypes>.*)>\\s*$");

    // values
    public static final Pattern MAP_PATTERN = Pattern.compile("^\\s*\\{(?<contents>.*)\\}\\s*$");
    public static final Pattern MAP_KEY_VALUE_PAIR_PATTERN = Pattern.compile("^\\s*(?<key>[^\\s]+)\\s*=\\s*(?<value>[^\\s]+)\\s*$");
    public static final Pattern LIST_PATTERN = Pattern.compile("^\\s*\\[(?<contents>.*)\\]\\s*$");
    public static final Pattern EMPTY_PATTERN = Pattern.compile("^\\s*$");

    // combined type and value
    public static final Pattern LIST_TYPE_VALUE_PATTERN = Pattern.compile("^\\s*(?<type>(?:java.util.)?List<(?<genericType>.*)>)\\s*\\[(?<contents>.*)\\]\\s*$");
    public static final Pattern MAP_TYPE_VALUE_PATTERN = Pattern.compile("^\\s*(?<type>(?:java.util.)?Map<(?<genericTypes>.*)>)\\s*\\{(?<contents>.*)\\}\\s*$");

    // regex useful for checking output
    public static final Pattern XML_DISALLOWED_CHARACTERS = Pattern.compile("<|>|&");

    // Strings for creating the same folders in directory output
    public static final String ACT = "activities";
    public static final String RES = "resources";
    public static final String CON = "constraints";
}
