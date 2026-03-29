package com.icss.controller;

import com.icss.dto.ApiResponse;
import com.icss.model.Customer;
import com.icss.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}")
    public ApiResponse<Customer> getById(@PathVariable String id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
        return ApiResponse.success(customer);
    }
}
