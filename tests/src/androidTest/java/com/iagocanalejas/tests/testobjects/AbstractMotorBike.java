package com.iagocanalejas.tests.testobjects;

import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public abstract class AbstractMotorBike extends AbstractVehicule {

    public AbstractMotorBike() {
    }

    public AbstractMotorBike(int wheels) {
        mWheels = wheels;
    }

}
