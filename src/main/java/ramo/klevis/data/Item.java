package ramo.klevis.data;

import java.io.Serializable;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class Item implements Serializable {
    private String id;
    private String description;
    private double price;

    public Item(String id, String description, double price) {
        this.id = id;
        this.description = description;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
