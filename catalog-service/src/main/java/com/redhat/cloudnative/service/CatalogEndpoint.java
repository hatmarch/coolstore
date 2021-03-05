package com.redhat.cloudnative.service;

import java.util.List;

import javax.validation.Valid;

import com.redhat.cloudnative.model.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class CatalogEndpoint {

    @Autowired
    private CatalogService catalogService;

    @ResponseBody
    @GetMapping("/products")
    @CrossOrigin
    public ResponseEntity<List<Product>> readAll() {
        return new ResponseEntity<List<Product>>(catalogService.readAll(),HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/product/{id}")
    @CrossOrigin
    public ResponseEntity<Product> read(@PathVariable("id") String id) {
        return new ResponseEntity<Product>(catalogService.read(id),HttpStatus.OK);
    }

    // FIXME: Secure this endpoint
    @RequestMapping(value = "/products", method = RequestMethod.POST, produces = "application/json")
    @CrossOrigin
    public ResponseEntity<List<Product>> addProducts(@RequestBody @Valid List<Product> products,  BindingResult bindingResult) throws Exception {
        BindingErrorsResponse errors = new BindingErrorsResponse();
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors() || (products == null)) {
            errors.addAllErrors(bindingResult);
            headers.add("errors", errors.toJSON());
            return new ResponseEntity<List<Product>>(products, headers, HttpStatus.BAD_REQUEST);
        }

        Logger logger = LoggerFactory.getLogger(CatalogEndpoint.class);
        for (Product product : products) {
            logger.info("Got product {}", product.toString());
        }
        // FIXME: Save
        //this.userService.saveUser(user);

        return new ResponseEntity<List<Product>>(products, headers, HttpStatus.CREATED);
    }
}