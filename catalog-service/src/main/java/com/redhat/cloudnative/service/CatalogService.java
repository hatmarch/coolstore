package com.redhat.cloudnative.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.redhat.cloudnative.client.InventoryClient;
import com.redhat.cloudnative.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    @Autowired
    private ProductRepository repository;

    //TODO: Autowire Inventory Client
    @Autowired
    InventoryClient inventoryClient;

    public Product read(String id) {
        Product product = repository.findById(id);
        //TODO: Update the quantity for the product by calling the Inventory service
        JSONArray jsonArray = new JSONArray(inventoryClient.getInventoryStatus(product.getItemId()));
        List<String> quantity = IntStream.range(0, jsonArray.length())
            .mapToObj(index -> ((JSONObject)jsonArray.get(index))
            .optString("quantity")).collect(Collectors.toList());
        product.setQuantity(Integer.parseInt(quantity.get(0)));
        return product;
    }

    public List<Product> readAll() 
    {
        List<Product> productList = repository.readAll();
        for ( Product p : productList ) {

            JSONArray jsonArray = new JSONArray(inventoryClient.getInventoryStatus(p.getItemId()));

            // NOTE: quantity on the product is 0 to start with, so if the item can't be found in inventory
            // just let that default value pass through
            if(!jsonArray.isEmpty())
            {
                List<String> quantity = IntStream.range(0, jsonArray.length())
                .mapToObj(index -> ((JSONObject)jsonArray.get(index))
                .optString("quantity")).collect(Collectors.toList());
                p.setQuantity(Integer.parseInt(quantity.get(0)));
            }
            else
            {
                Logger logger = LoggerFactory.getLogger(CatalogService.class);
                logger.info("Couldn't find quantity of {} due to there being no inventory record for {}",
                    p.getName(), p.getItemId());
            }
        }
        return productList; 
    }

    @Transactional
    public void saveProduct(Product product) throws DataAccessException
    {
        this.repository.save(product);
    } 

    //TODO: Add Callback Factory Component


}