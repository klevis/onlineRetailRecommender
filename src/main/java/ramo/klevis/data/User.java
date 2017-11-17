package ramo.klevis.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class User implements Serializable{
    private int id;
    private String country;

    private List<Item> items = new ArrayList<>();
    private List<Item> suggestedItems = new ArrayList<>();

    public User(int id, String country) {
        this.id = id;
        this.country = country;
    }

    public List<Item> getSuggestedItems() {
        return new ArrayList<>(suggestedItems);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String toString() {
        return id+" from "+country;
    }

    public void addSuggestedItem(Item item) {
        suggestedItems.add(item);
    }
}
