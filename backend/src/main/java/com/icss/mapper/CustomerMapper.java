package com.icss.mapper;

import com.icss.model.Agent;
import com.icss.model.Customer;
import com.icss.model.Policy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {
    Customer selectById(String id);

    Agent selectAgentById(String id);

    List<Policy> selectPoliciesByCustomerId(String customerId);
}
