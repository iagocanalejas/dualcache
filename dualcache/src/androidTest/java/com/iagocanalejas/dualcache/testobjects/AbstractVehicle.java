package com.iagocanalejas.dualcache.testobjects;

public abstract class AbstractVehicle {

    protected String mName;

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
        if (o instanceof AbstractVehicle) {
            if (mName.equals(((AbstractVehicle) o).getName())
                    && mWheels == ((AbstractVehicle) o).getWheels()) {
                return true;
            } else {
                return false;
            }
        } else {
            return super.equals(o);
        }
    }
}
