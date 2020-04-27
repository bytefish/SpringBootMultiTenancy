// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy.web.controllers;

import de.bytefish.multitenancy.model.Customer;
import de.bytefish.multitenancy.repositories.ICustomerRepository;
import de.bytefish.multitenancy.web.converter.Converters;
import de.bytefish.multitenancy.web.model.CustomerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class CustomerController {

    private final ICustomerRepository repository;

    @Autowired
    public CustomerController(ICustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    public List<CustomerDto> getAll() {
        // Return the DTO List:
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(Converters::convert)
                .collect(Collectors.toList());
    }

    @GetMapping("/customers/{id}")
    public CustomerDto get(@PathVariable("id") long id) {
        Customer customer = repository.findOne(id);

        // Return the DTO:
        return Converters.convert(customer);
    }

    @PostMapping("/customers")
    public CustomerDto post(@RequestBody CustomerDto customer) {
        // Convert to the Domain Object:
        Customer source = Converters.convert(customer);

        // Store the Entity:
        Customer result = repository.save(source);

        // Return the DTO:
        return Converters.convert(result);
    }

    @DeleteMapping("/customers/{id}")
    public void delete(@PathVariable("id") long id) {
        repository.delete(id);
    }

}
