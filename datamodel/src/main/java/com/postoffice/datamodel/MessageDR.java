package com.postoffice.datamodel;

/**
 * D：Deliver
 * R：Receiver
 * 发送者接受者关系
 */
public interface MessageDR {
    public enum DRType{
        Both,Group
    }
}
