package com.redhat.cloudnative.service;

import com.redhat.cloudnative.model.Inventory;
import com.redhat.cloudnative.model.Product;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CatalogEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;


    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
            service("inventory:8080")
    //                    .andDelay(2500, TimeUnit.MILLISECONDS).forMethod("GET")
                    .get(startsWith("/api/inventory"))
    //                    .willReturn(serverError())
                   // .willReturn(success(json(new Inventory("9999",9999))))
                   .willReturn(success("[{\"itemId\":\"329199\",\"quantity\":9999}]", "application/json"))

    ));

    @Test
    public void test_retriving_one_product() {
        ResponseEntity<Product> response
                = restTemplate.getForEntity("/api/product/329199", Product.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .returns("329199",Product::getItemId)
                .returns("Forge Laptop Sticker",Product::getName)
//TODO: Add check for Quantity
                .returns(9999,Product::getQuantity)
                .returns(8.50,Product::getPrice);
    }


    @Test
    public void check_that_endpoint_returns_a_correct_list() {

        ResponseEntity<List<Product>> rateResponse =
                restTemplate.exchange("/api/products",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {
                        });

        List<Product> productList = rateResponse.getBody();
        assertThat(productList).isNotNull();
        assertThat(productList).isNotEmpty();
        List<String> names = productList.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(names).contains("Red Fedora","Forge Laptop Sticker","Oculus Rift");

        Product fedora = productList.stream().filter( p -> p.getItemId().equals("329299")).findAny().get();
        assertThat(fedora)
                .returns("329299",Product::getItemId)
                .returns("Red Fedora", Product::getName)
//TODO: Add check for Quantity
                .returns(9999,Product::getQuantity)
                .returns(34.99,Product::getPrice);
    }

    @Test
    public void check_that_save_and_retrieve_works() {
            Product testProd = new Product();
            testProd.setItemId("10");
            testProd.setDesc("Test Desc");
            testProd.setName("Test Name");
            List<Product> prodList = List.of(testProd);

            // Save
            ResponseEntity<List<Product>> postResponse = restTemplate.exchange("/api/products", HttpMethod.POST, 
            new HttpEntity(prodList), new ParameterizedTypeReference<List<Product>>(){}); 
    
            assertThat(postResponse.getBody()).isNotNull();
            assertThat(postResponse.getBody()).isNotEmpty();

            // Look for entity
            ResponseEntity<Product> readResponse =
            restTemplate.exchange("/api/product/10",
                    HttpMethod.GET, null, new ParameterizedTypeReference<Product>() {
                    });

            Product readProduct = readResponse.getBody();
            assertThat(readProduct).isNotNull();
            assertThat(readProduct.getName().equals("Test Name"));
        }

}