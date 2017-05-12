/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package vn.vnpay.sms.receiver;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Simple container to hold set of somehow related processors.
 *
 * @see PDUProcessor
 */
public class PDUProcessorGroup {
    //private Vector processors = null;
    private Hashtable processors = null;

    /**
     * Initialises the underlying container.
     */
    public PDUProcessorGroup() {
        //processors = new Vector();
        processors = new Hashtable();
    }

    /**
     * Initialises the underlying container to the given size.
     */
    public PDUProcessorGroup(int initSize) {
        //processors = new Vector(initSize);
        processors = new Hashtable(initSize);
    }


    public void add(String sessionId, PDUProcessor p) {
        synchronized (processors) {
            if (!processors.contains(p)) {
                //processors.add(p);
                processors.put(sessionId, p);
            }
        }
    }

    /**
     * Removes single processor from the group.
     *
     * @param sessionId the processor to remove
     * @see #add(String, PDUProcessor)
     */
    public void remove(String sessionId) {
        synchronized (processors) {
            processors.remove(sessionId);

        }
    }

    /**
     * Returns the count of the processors currently in the group.
     *
     * @return current count of processors in the group
     */
    public int count() {
        synchronized (processors) {
            return processors.size();
        }
    }

    /**
     * Returns <code>i</code>th processor in the group.
     *
     * @return the processor on the given position
     */
    public Enumeration elements() {
        synchronized (processors) {
            return processors.elements();
        }
    }

    public PDUProcessor get(String sessionId) {
        synchronized (processors) {
            return (PDUProcessor) processors.get(sessionId);
        }
    }

    public PDUProcessor getbyUser(String username) {
        synchronized (processors) {
            PDUProcessor proc = null;
            for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                PDUProcessor processor = (PDUProcessor) e.nextElement();
                if (processor.systemId != null && processor.systemId.equals(username)) {
                    proc = processor;
                    break;
                }
            }
            return proc;
        }
    }
}
