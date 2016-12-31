package com.iagocanalejas.tests.testobjects;

import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS_AND_ACCESSORS
        , serializeNullObjects = true)
public class CoolBike extends AbstractMotorBike {
    public CoolBike() {
        super();
    }

    public CoolBike(String name) {
        super(2);
        mName = name;
    }

}
