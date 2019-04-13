package com.postoffice.storage.mongo.entity;

import com.postoffice.datamodel.FriendsRelation;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("friendsRelation")
public class FriendsRelationDBEntity extends FriendsRelation {
    @Transient
    public final static String ID_FRIENDS_RELATION_SEQ_NAME = "FriendsRelation";

    @Id
    @Override
    public Long getRelationID() {
        return super.getRelationID();
    }

    @Id
    @Override
    public void setRelationID(Long relationID) {
        super.setRelationID(relationID);
    }


    @Override
    public Date getLatestTalkTime() {
        return super.getLatestTalkTime();
    }
}
