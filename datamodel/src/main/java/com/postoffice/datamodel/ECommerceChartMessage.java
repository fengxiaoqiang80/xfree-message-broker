package com.postoffice.datamodel;

import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.Date;
import java.util.NoSuchElementException;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ECommerceChartMessage implements IMessage {

    @EqualsAndHashCode.Include
    private long messageID;
    private String deliver;
    private String receiver;
    private String domainID;
    //@JSONField(serialize = false)
    private MessageDirectionEnum messageDirection = MessageDirectionEnum.UNKNOWN;
    //@JSONField(serialize = false)
    private MessageTypeEnum messageType = MessageTypeEnum.UNKNOWN;
    private String content;
    private String deliverInfos;
    private String receiverInfos;
    private Date generateTime;
    private Date deliveryTime;
    private Date readReceiptTime;
    private Date revokeTime;


    /*public int getDirection() {
        return messageDirection.getValue();
    }

    public void setDirection(int messageDirection) {
        this.messageDirection = MessageDirectionEnum.valueOf(messageDirection);
    }

    public int getType() {
        return messageType.getValue();
    }

    public void setType(int messageType) {
        this.messageType = MessageTypeEnum.valueOf(messageType);
    }
*/

    /**
     * 不可修改定义，可追加。
     */
    public enum MessageDirectionEnum{
        /**未知*/
        UNKNOWN(0),
        /**店主到客户*/
        SHOPKEEPER_TO_CUSTOMER(1),
        /**客户到店主*/
        CUSTOMER_TO_SHOPKEEPER(2),
        /**平台通知*/
        PLATFORM_NOTIFICATION(3),
        /**所有用户到平台*/
        ALL_TO_PLATFORM(4);

        private final int value;
        MessageDirectionEnum(int value){
            this.value = value;
        }
        public int getValue() {
            return value;
        }



        public static MessageDirectionEnum valueOf(int value){
            for(MessageDirectionEnum directionEnum : values()){
                if(value == directionEnum.getValue()){
                    return directionEnum;
                }
            }
            throw new NoSuchElementException(String.format("No such MessageDirectionEnum %d",value));
        }

    }

    /**
     * 终端类型，暂未使用
     */
    public enum TermType{
        UNKUNWN(1),
        WXLITE(1<<1),
        WX(1<<2),
        BROWSER(1<<3),
        DINGDING(1<<4),
        SHORTMSG(1<<5),
        APP(1<<6);

        private final int value;
        TermType(int value){
            this.value = value;
        }

    }


    /**
     * 不可修改定义，可追加。
     */
    @JSONType()
    public enum MessageTypeEnum{
        /**未知的*/
        UNKNOWN(1),
        /**普通对话*/
        PLAIN(2),
        /**商品*/
        PRODUCT(3),
        /**订单*/
        ORDER(4),
        /**售后*/
        AFTERSALE(5);


        private final int value;
        MessageTypeEnum(int value){
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        public static MessageTypeEnum valueOf(int value){
            for(MessageTypeEnum type : values()){
                if(value == type.getValue()){
                    return type;
                }
            }
            throw new NoSuchElementException(String.format("No such MessageTypeEnum %d",value));
        }
    }


}
