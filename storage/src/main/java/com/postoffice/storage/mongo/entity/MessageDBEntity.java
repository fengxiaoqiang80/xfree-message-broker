package com.postoffice.storage.mongo.entity;

import com.postoffice.datamodel.ECommerceChartMessage;
import com.postoffice.datamodel.IMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("message")
public class MessageDBEntity extends ECommerceChartMessage implements IMessage {
    @Transient
    public final static String ID_MESSAGE_SEQ_NAME = "Message";
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
    public MessageDBEntity getAndSetMessageID(long messageID) {
        super.setMessageID(messageID);
        return this;
    }*/
}
