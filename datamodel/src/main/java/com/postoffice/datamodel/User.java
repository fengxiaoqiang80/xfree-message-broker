package com.postoffice.datamodel;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class User implements MessageDR {
    private String id;
    private String mobile;
    private String nickname;
    private String headImgUrl;
}
