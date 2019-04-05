package com.postoffice.storage.mongo;

import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.datamodel.IMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Message extends ECommerceChartMessage implements IMessage {
    @Id
    @Override
    public long getMessageID() {
        return super.getMessageID();
    }

    @Id
    @Override
    public void setMessageID(long messageID) {
        super.setMessageID(messageID);
    }

    /*@Transient
    public Message getAndSetMessageID(long messageID) {
        super.setMessageID(messageID);
        return this;
    }*/
}
