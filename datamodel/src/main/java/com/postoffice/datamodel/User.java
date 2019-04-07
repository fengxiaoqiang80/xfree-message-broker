package com.postoffice.datamodel;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements MessageDR {
    private String id;
    private String name;
    private String screenName;
    private String imgURL;
}
