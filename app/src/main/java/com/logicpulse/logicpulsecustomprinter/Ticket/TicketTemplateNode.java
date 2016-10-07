package com.logicpulse.logicpulsecustomprinter.Ticket;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

@Root
public class TicketTemplateNode {

    @Attribute
    private String key;
    @Element
    private String value;

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
}