package gov.nasa.jpl.activity;

import gov.nasa.jpl.engine.Waiter;
import gov.nasa.jpl.time.Time;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface is what Activity objects have copies of in order to move forward (or maybe eventually backward)
 * in simulated time.
 */
public interface WaitProvider {
    void waitUntil(Time t);
    Map waitForSignal(String signalName) throws InterruptedException;
    Waiter waitUntil(Time t, Supplier<Waiter> func);
    Waiter waitForSignal(String signalName, Function<Map,Waiter> func);
    Time getCurrentTime();
}