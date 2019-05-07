package com.visearch.model;

import javax.validation.constraints.NotNull;

public class JsonBody {

    @NotNull
    private String magnetLink;

    public String getMagnetLink() {
        return this.magnetLink;
    }
}
