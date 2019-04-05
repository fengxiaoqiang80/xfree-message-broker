package com.postoffice.test.datamodel;

import com.alibaba.fastjson.JSON;
import com.postoffice.datamodel.ECommerceChartMessage;
import org.junit.Assert;
import org.junit.Test;

import static com.postoffice.datamodel.ECommerceChartMessage.MessageDirectionEnum.CUSTOMER_TO_SHOPKEEPER;
import static com.postoffice.datamodel.ECommerceChartMessage.MessageTypeEnum.PLAIN;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ECommerceChartMessageTest {

    @Test
    public void hashAndEquals(){
        ECommerceChartMessage m1 = new ECommerceChartMessage();
        m1.setMessageID(0L);
        m1.setContent("M1");

        ECommerceChartMessage m2 = new ECommerceChartMessage();
        m1.setMessageID(0L);
        m1.setContent("M2");
        Assert.assertEquals(m1.hashCode(),m2.hashCode());
    }

    @Test
    public void enumFieldToJson(){

        ECommerceChartMessage message = new ECommerceChartMessage();
        message.setContent("content");
        message.setMessageID(10L);
        message.setMessageDirection(CUSTOMER_TO_SHOPKEEPER);
        message.setMessageType(PLAIN);

        //序列化
        String jstr = JSON.toJSONString(message);
        //反序列化
        ECommerceChartMessage newMessage = JSON.parseObject(jstr,ECommerceChartMessage.class);

        //校验结果
        assertThat(newMessage.getContent(),is("content"));
        assertThat(newMessage.getMessageID(),is(10L));
        assertThat(newMessage.getMessageDirection(),is(CUSTOMER_TO_SHOPKEEPER));
        assertThat(newMessage.getMessageType(),is(PLAIN));
    }



}
