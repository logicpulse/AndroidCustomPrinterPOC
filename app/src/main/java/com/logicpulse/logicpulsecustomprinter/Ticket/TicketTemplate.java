package com.logicpulse.logicpulsecustomprinter.Ticket;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import static android.bluetooth.BluetoothClass.CREATOR;

/**
 * Created by mario.monteiro on 10/10/2016.
 */

class TicketTemplate {
    private ArrayList<TicketTemplateNode> properties;

    public ArrayList<TicketTemplateNode> getProperties() {
        return properties;
    }

}