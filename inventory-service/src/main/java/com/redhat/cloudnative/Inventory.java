package com.redhat.cloudnative;

import java.util.Optional;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class Inventory extends PanacheEntity {

    public String itemId;
    public String location;
    public int quantity;
    public String link;

    public Inventory() {

    }

    public static Optional<Inventory> findByItemId(String itemId){
        return find("itemId", itemId).firstResultOptional();
    }

    // Copies non-Id based fields from other into this entity
    public void setEqual( Inventory other ) {
        itemId = other.itemId;
        location = other.location;
        quantity = other.quantity;
        link = other.link;
    }
}