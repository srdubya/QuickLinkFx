package com.srdubya.QuickLink;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import javafx.collections.ModifiableObservableListBase;
import org.hildan.fxgson.FxGson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

class LinkEntryList extends ModifiableObservableListBase<LinkEntry> {

    private final List<LinkEntry> delegate = new ArrayList<>();

    @Override
    public boolean add(LinkEntry linkEntry) {
        for (LinkEntry e : delegate) {
            if(e.equals(linkEntry)) {
                return true;
            }
        }
        return super.add(linkEntry);
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
