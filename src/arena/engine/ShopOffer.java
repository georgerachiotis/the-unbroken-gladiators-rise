package arena.engine;

public class ShopOffer {

    public enum Status {
        AVAILABLE,
        NOT_ENOUGH_GOLD,
        LOCKED,
        OWNED_BEST
    }

    private final String name;
    private final String description;
    private final int price;
    private final int fameRequirement;
    private final int owned;
    private final Status status;

    public ShopOffer(String name, String description, int price, int fameRequirement, Status status) {
        this(name, description, price, fameRequirement, -1, status);
    }

    public ShopOffer(String name, String description, int price, int fameRequirement, int owned, Status status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.fameRequirement = fameRequirement;
        this.owned = owned;
        this.status = status;
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

    public int getFameRequirement() {
        return fameRequirement;
    }

    public int getOwned() {
        return owned;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isBuyable() {
        return status == Status.AVAILABLE;
    }
}
