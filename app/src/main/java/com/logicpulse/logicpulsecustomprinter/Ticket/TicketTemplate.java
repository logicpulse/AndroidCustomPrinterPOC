package com.logicpulse.logicpulsecustomprinter.Ticket;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

@Root
public class TicketTemplate {

    @ElementList
    private List<TicketTemplateNode> list;
    @Attribute
    private String name;
    @Attribute
    private String charset;

    public String getName() {
        return name;
    }
    public String getCharset() {
        return charset;
    }
    public List getProperties() {
        return list;
    }
}
