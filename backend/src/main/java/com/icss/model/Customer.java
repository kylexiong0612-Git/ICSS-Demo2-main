package com.icss.model;

import lombok.Data;

import java.util.List;

@Data
public class Customer {
    private String id;
    private String name;
    private String phone;
    private String idNumber;
    /** 普通 | 银卡 | 金卡 | 白金 */
    private String tier;
    private String agentId;
    private Long createdAt;

    // 关联数据（非数据库字段，由 Service 层填充）
    private Agent agent;
    private List<Policy> policies;
}
