package com.srdubya.QuickLink;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationContext {

    private boolean isError = false;
    private List<Consumer<Boolean>> consumers = new LinkedList<>();

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
        onChange();
    }

    Consumer<Boolean> addChangeListener(Consumer<Boolean> consumer) {
        consumers.add(consumer);
        return consumer;
    }

    void removeChangeListener(Consumer<Boolean> consumer) {
        consumers.remove(consumer);
    }

    void onChange() {
        for (var consumer : consumers) {
            consumer.accept(isError);
        }
    }
}
