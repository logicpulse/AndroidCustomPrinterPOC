package com.logicpulse.logicpulsecustomprinter.Ticket;

import android.content.Context;

import com.logicpulse.logicpulsecustomprinter.R;
import com.logicpulse.logicpulsecustomprinter.Utils;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Ticket {

    public Ticket(Context context) {
        Serializer serializer = new Persister();
        //File source = new File();
        InputStream inputStream = Utils.getInputStreamFromRawResource(context, R.raw.template_ticket);
        //File source = new File("example.xml");
        TicketTemplate ticketTemplate = null;

        try {
            ticketTemplate = serializer.read(TicketTemplate.class, inputStream/*source*/);

            //assert example.getVersion() == 0;
            //assert example.getName() == null;
            //assert example.getId() == 10;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
