package com.iagocanalejas.tests.testobjects;

import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public abstract class AbstractCar extends AbstractVehicule {

    public AbstractCar() {

    }

    public AbstractCar(int wheels) {
        mWheels = wheels;
    }

}
