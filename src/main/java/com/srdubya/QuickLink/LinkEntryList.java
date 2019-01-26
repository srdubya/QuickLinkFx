package com.srdubya.QuickLink;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import javafx.collections.ModifiableObservableListBase;
import org.hildan.fxgson.FxGson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class LinkEntryList extends ModifiableObservableListBase<LinkEntry> {

    private final List<LinkEntry> delegate = new ArrayList<>();

    public boolean add(LinkEntry linkEntry, Consumer<LinkEntry> callback) {
        for (LinkEntry e : delegate) {
            if (e.equals(linkEntry)) {
                return false;
            }
            if (e.isNearly(linkEntry)) {
                callback.accept(linkEntry);
                return false;
            }
        }
        return delegate.add(linkEntry);
    }

    public int add(List<LinkEntry> linkEntries, BiFunction<List<LinkEntry>, List<LinkEntry>, List<LinkEntry>> callback) {
        var currentLookAlikes = new LinkedList<LinkEntry>();

        for (LinkEntry e : delegate) {
            for (var linkEntry : linkEntries) {
                if (e.isNearly(linkEntry)) {
                    currentLookAlikes.add(e);
                    break;
                }
            }
        }

        var keepers = callback.apply(linkEntries, currentLookAlikes);
        if (keepers == null) {
            return 0;
        }
        for (var e : currentLookAlikes) {
            if (!keepers.contains(e)) {
                delegate.remove(e);
            }
        }

        int count = 0;

        for (var e : keepers) {
            if (!currentLookAlikes.contains(e)) {
                delegate.add(e);
                count++;
            }
        }
        return count;
    }

    @Override
    public LinkEntry get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    protected void doAdd(int index, LinkEntry element) {
        delegate.add(index, element);
    }

    @Override
    protected LinkEntry doSet(int index, LinkEntry element) {
        return delegate.set(index, element);
    }

    @Override
    protected LinkEntry doRemove(int index) {
        return delegate.remove(index);
    }

    public void toFile(String filename) throws IOException {
        Gson gson = FxGson.create();
        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        writer.beginArray();
        writer.setIndent("  ");
        for(LinkEntry entry : delegate) {
            gson.toJson(entry, LinkEntry.class, writer);
        }
        writer.endArray();
        writer.close();
    }

}
