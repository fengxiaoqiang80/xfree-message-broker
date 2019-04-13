package com.postoffice.datamodel;

import lombok.*;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class FriendsRelation {
    private Long relationID;
    private String domainID;
    private String personID;
    private String friendID;
    private Date generateTime;
    private Date latestTalkTime;
}
