package vn.vnpay.sms.smpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.Session;
import org.smpp.SmppObject;
import org.smpp.pdu.PDU;
import org.smpp.util.Queue;

/**
 * Created by hoangdinhdu@gmail.com on 10/11/2014.
 */
public class PDUEventListener extends SmppObject implements ServerPDUEventListener {
    private static Log logger = LogFactory.getLog(PDUEventListener.class);

    Session session;
    final Queue requestEvents = new Queue();

    public PDUEventListener(Session session) {
        this.session = session;
    }

    public void handleEvent(ServerPDUEvent event) {
        PDU pdu = event.getPDU();
        //logger.info("handleEvent: "+ pdu.debugString());
        if (pdu.isRequest() || pdu.isResponse()) {
            synchronized (requestEvents) {
                requestEvents.enqueue(event);
                requestEvents.notify();
            }
        } else {
            logger.error(
                    "pdu of unknown class (not request nor " + "response) received, discarding " + pdu.debugString());
        }
    }

    /**
     * Returns received pdu from the queue. If the queue is empty,
     * the method blocks for the specified timeout.
     */
    public ServerPDUEvent getRequestEvent(long timeout) {
        ServerPDUEvent pduEvent = null;
        synchronized (requestEvents) {
            if (requestEvents.isEmpty()) {
                try {
                    requestEvents.wait(timeout);
                } catch (InterruptedException e) {
                    // ignoring, actually this is what we're waiting for
                }
            }
            if (!requestEvents.isEmpty()) {
                pduEvent = (ServerPDUEvent) requestEvents.dequeue();
                //logger.info("getRequestEvent: "+ pduEvent.getPDU().debugString());
            }
        }
        return pduEvent;
    }
}
