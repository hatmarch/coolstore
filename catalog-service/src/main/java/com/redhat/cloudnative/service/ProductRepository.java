package com.redhat.cloudnative.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.cloudnative.model.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // Product row mapper 
    private RowMapper<Product> rowMapper = (rs, rowNum) -> new Product(
            rs.getString("itemId"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"));

    // Method for returning all products
    public List<Product> readAll() {
        return jdbcTemplate.query("SELECT * FROM catalog", rowMapper);
    }

    public Product findById(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemId", id);
        return jdbcTemplate.queryForObject("SELECT * FROM catalog WHERE itemId = ':itemId'",params, rowMapper);
    }

    public Boolean hasProduct(Product product) {
        Boolean result = false;
        try{
            result = ( findById(product.getItemId()) == null );
        }
        catch (EmptyResultDataAccessException e)
        {
            Logger logger = LoggerFactory.getLogger(ProductRepository.class);
            logger.info("No product in catalog with itemId of {}", product.getItemId());

            // NOTE: Other exceptions will get thrown
        }

        return result;
    }

    public void save(Product product) throws DataAccessException {

        MapSqlParameterSource parameterSource = createProductParameterSource(product);

        if (!hasProduct(product))
        {
            // insert
            // NOTE: Product IDs are provided by external systems, these are not generated
            // by the catalog database
            this.jdbcTemplate.update(
                "INSERT INTO catalog (itemId, name, description, price)"
                + " VALUES (':itemId, :name, :desc, :price)",
                parameterSource);
        } 
        else 
        {
            // update
            this.jdbcTemplate.update(
                "UPDATE catalog SET name=:name, description=:desc, price=:price, " +
                    "WHERE itemId=:itemId",
                parameterSource);
        }
    }

    /**
     * Creates a {@link MapSqlParameterSource} based on data values from the supplied {@link Product} instance.
     */
    private MapSqlParameterSource createProductParameterSource(Product product) {
        return new MapSqlParameterSource()
            .addValue("itemId", product.getItemId())
            .addValue("name", product.getName())
            .addValue("desc", product.getDesc())
            .addValue("price", product.getPrice());
    }

}