package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.activity.Activity.now;

public class ExampleCustomResource extends Resource<Double>{
    Resource one;
    Resource two;

    public ExampleCustomResource(Resource one, Resource two){
        super();
        this.one = one;
        this.two = two;
    }

    public Double profile(Time t) {
        return Math.pow((double) one.currentval(), 3) - Math.sqrt((double) two.currentval());
    }

    public void update(){
        set(profile(now()));
    }
}

