package com.icss.model;

import lombok.Data;

@Data
public class Agent {
    private String id;
    private String name;
    private String code;
    private String phone;
    private String branch;
    private Long createdAt;
}
