package com.iagocanalejas.tests.testobjects;

import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS_AND_ACCESSORS,
        serializeNullObjects = true)
public class CoolCar extends AbstractCar {

    public CoolCar() {
        super();
    }

    public CoolCar(String name) {
        super(4);
        mName = name;
    }


}
