package com.postoffice.datamodel;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuthInfo {
    @EqualsAndHashCode.Include
    private User user;
    @EqualsAndHashCode.Include
    private String domain;
    @EqualsAndHashCode.Include
    private boolean legalToken;

    private String domainDesc;
    private String message;
}
