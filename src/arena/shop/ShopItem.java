package arena.shop;

/**
 * Describes one purchasable market option.
 */
public class ShopItem {

    private final String name;
    private final String description;
    private final int price;

    public ShopItem(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public String getMenuText() {
        return name + " (" + description + ") - " + price + " Gold";
    }
}
