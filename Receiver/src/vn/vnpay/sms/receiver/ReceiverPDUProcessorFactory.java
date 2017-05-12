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

/**
 * Class <code>ReceiverPDUProcessorFactory</code> creates new instances of
 * PropertiesConfig <code>ReceiverPDUProcessor</code>. It's passed to <code>SMSCListener</code>
 * which uses it to create new PDU processors whenewer new connection
 * from client is requested. The PDU processor is passed to
 * instance of <code>SMSCSession</code> which uses the processor to handle
 * client requests and responses.
 *
 * @see PDUProcessorFactory
 * @see PDUProcessorGroup
 * @see ReceiverPDUProcessor
 */

/*
  20-09-01 ticp@logica.com added reference to the DeliveryInfoSender to support
                           automatic sending of delivery info PDUs
*/

public class ReceiverPDUProcessorFactory implements PDUProcessorFactory {
    private PDUProcessorGroup procGroup;
    //private Table routes;


    /**
     * Constructs processor factory with given processor group,
     * message store for storing of the messages and PropertiesConfig table of
     * users for authentication. The message store and users parameters are
     * passed to generated instancies of <code>ReceiverPDUProcessor</code>.
     *
     */
    public ReceiverPDUProcessorFactory() {
        this.procGroup = procGroup;
    }

    /**
     * Creates PropertiesConfig new instance of <code>ReceiverPDUProcessor</code> with
     * parameters provided in construction of th factory.
     *
     * @param session the sessin the PDU processor will work for
     * @return newly created <code>ReceiverPDUProcessor</code>
     */
    public PDUProcessor createPDUProcessor(SMSCSession session) {
        ReceiverPDUProcessor pduProcessor
                = new ReceiverPDUProcessor(session);
        pduProcessor.setGroup(session.getSessionId(), procGroup);
        return pduProcessor;

    }



}
