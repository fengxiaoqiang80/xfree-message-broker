package com.postoffice.storage.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("idSequence")
public class IDSequenceDBEntity {
    @Id
    private String sequenceName;
    private long sequenceValue;
    private Date generateDate;
}