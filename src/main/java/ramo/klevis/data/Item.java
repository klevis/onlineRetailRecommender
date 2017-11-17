package ramo.klevis.data;

import java.io.Serializable;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class Item implements Serializable {
    private int id;
    private String description;
    private double price;
    private int size;

    public Item(int id, String description, double price, int size) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
