package ramo.klevis.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class User {
    private String id;
    private String country;

    private List<Item> items = new ArrayList<>();

    public User(String id, String country) {
        this.id = id;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
    }
}
