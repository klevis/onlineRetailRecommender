package ramo.klevis.data;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class Row {
    private String userId;
    private String userCountry;
    private String itemID;
    private String itemDescription;
    private int itemsBoughNumber;
    private double itemPrice;

    /**
     * Maybe better a builder...
     * @param userId
     * @param userCountry
     * @param itemID
     * @param itemDescription
     * @param itemsBoughNumber
     * @param itemPrice
     */
    public Row(String userId, String userCountry, String itemID, String itemDescription, int itemsBoughNumber, double itemPrice) {
        this.userId = userId;
        this.userCountry = userCountry;
        this.itemID = itemID;
        this.itemDescription = itemDescription;
        this.itemsBoughNumber = itemsBoughNumber;
        this.itemPrice = itemPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public int getItemsBoughNumber() {
        return itemsBoughNumber;
    }

    public void setItemsBoughNumber(int itemsBoughNumber) {
        this.itemsBoughNumber = itemsBoughNumber;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }
}
