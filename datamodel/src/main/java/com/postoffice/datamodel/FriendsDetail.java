package com.postoffice.datamodel;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class FriendsDetail {
    private long relationID;
    private String friendID;
    private String mobile;
    private String nickname;
    private String headImgUrl;
    boolean deleteFlag;
    private Date generateTime;
    private Date latestTalkTime;
}
