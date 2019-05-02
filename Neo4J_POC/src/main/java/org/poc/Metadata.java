package org.poc;

import org.apache.lucene.util.packed.DirectMonotonicReader;

public class Metadata {

    private String type;
    private Script data; //todo generic


    public Metadata() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Script getData() {
        return data;
    }

    public void setData(Script data) {
        this.data = data;
    }
}
