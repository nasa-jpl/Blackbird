package gov.nasa.jpl.command;

import gov.nasa.jpl.engine.ParameterDeclaration;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.TypeNameConverters;

public class SetParameterCommand implements Command {
    private String className;
    private String fieldName;
    private String newValueAsString;
    private Object oldValue;
    private Object newValue;

    public SetParameterCommand(String commandString) {
        String[] tokens = commandString.split(" ");
        String[] twoPartsOfName = tokens[0].split("\\.");
        className = twoPartsOfName[0];
        fieldName = twoPartsOfName[1];
        newValueAsString = tokens[1];
    }

    @Override
    public void execute() throws CommandException {
        if (oldValue == null || newValue == null) {
            try {
                String type = TypeNameConverters.primitiveToWrapperType(ParameterDeclaration.getTypeOfParameter(className, fieldName));
                oldValue = ParameterDeclaration.getParameterValue(className, fieldName);
                newValue = ReflectionUtilities.returnValueOf(type, newValueAsString, true);

            }
            catch (IllegalAccessException | NullPointerException e) {
                throw new CommandException("Could find or get current value of parameter: " + className + "." + fieldName);
            }
        }
        try {
            ParameterDeclaration.modifyAdaptationParameter(className, fieldName, newValue);
        }
        catch (IllegalAccessException e) {
            throw new CommandException("Could not change value of parameter: " + className + "." + fieldName);
        }
    }

    @Override
    public void unExecute() throws CommandException {
        try {
            ParameterDeclaration.modifyAdaptationParameter(className, fieldName, oldValue);
        }
        catch (IllegalAccessException e) {
            throw new CommandException("Could not change value of parameter: " + className + "." + fieldName);
        }
    }
}
