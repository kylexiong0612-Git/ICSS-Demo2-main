package com.icss.service;

import com.icss.mapper.CustomerMapper;
import com.icss.model.Agent;
import com.icss.model.Customer;
import com.icss.model.Policy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;

    public Customer getById(String id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) return null;

        List<Policy> policies = customerMapper.selectPoliciesByCustomerId(id);
        customer.setPolicies(policies);

        if (customer.getAgentId() != null) {
            Agent agent = customerMapper.selectAgentById(customer.getAgentId());
            customer.setAgent(agent);
        }
        return customer;
    }
}
