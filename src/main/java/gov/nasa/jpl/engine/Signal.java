package gov.nasa.jpl.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Signal {
    private static final HashMap<String, Signal> listOfSignals = new HashMap<>();
    private final Deque<SignalHandler> handlers;
    String name;

    protected Signal(String signalName) {
        this.name = signalName;
        handlers = new ArrayDeque<>();
        listOfSignals.put(signalName, this);
    }

    static Signal getSignal(String signalName) {
        if (!listOfSignals.containsKey(signalName)) {
            return new Signal(signalName);
        }
        else {
            return listOfSignals.get(signalName);
        }
    }

    // we're going to call this at the end of a modeling run
    static void clearAllSignals() {
        for (String signalName : listOfSignals.keySet()) {
            while (!listOfSignals.get(signalName).handlers.isEmpty()) {
                // we have to interrupt any threads still waiting at the end of the modeling run or else the JVM won't finish running
                listOfSignals.get(signalName).handlers.pop();
            }
        }
    }

    // internal class to hold onto some data
    private static class SignalHandler {
        public final Function<Map, Waiter> func;
        public final boolean willResumeThread;
        public SignalHandler(Function<Map, Waiter> func, boolean willResumeThread) {
            this.func = func;
            this.willResumeThread = willResumeThread;
        }
    }

    void addSignalHandler(boolean willResumeThread, Function<Map,Waiter> func) {
        handlers.push(new SignalHandler(func, willResumeThread));
    }

    // when one thread broadcasts a signal, the signal sees it and responds by adding all the waiting threads back into the engine's stack at the current modeling time
    void broadcast(Map signalContents) {
        while (!handlers.isEmpty()) {
            SignalHandler p = handlers.pop();
            Waiter w = new Waiter(ModelingEngine.getEngine().getCurrentTime(), -1, () -> p.func.apply(signalContents));
            w.willResumeThread(p.willResumeThread);
            // set the Waiters time to execute to now and then places it into the modeling queue
            ModelingEngine.getEngine().insertWaiter(w);
        }
    }

    public static void send(String signalName, Map signalContents) {
        getSignal(signalName).broadcast(signalContents);
    }
}
