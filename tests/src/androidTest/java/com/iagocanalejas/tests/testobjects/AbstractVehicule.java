package com.iagocanalejas.tests.testobjects;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public abstract class AbstractVehicule {

    @JsonField
    protected String mName;

    @JsonField
    protected int mWheels;

    public String getName() {
        return mName;
    }

    public int getWheels() {
        return mWheels;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setWheels(int wheels) {
        mWheels = wheels;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractVehicule) {
            if (mName.equals(((AbstractVehicule) o).getName())
                    && mWheels == ((AbstractVehicule) o).getWheels()) {
                return true;
            } else {
                return false;
            }
        } else {
            return super.equals(o);
        }
    }
}
