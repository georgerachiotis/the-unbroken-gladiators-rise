package arena.shop;

import arena.characters.Player;
import arena.enums.GladiatorClass;
import arena.items.Armor;
import arena.items.Weapon;

/**
 * Builds market items from the player's fame and class.
 */
public class Shop {

    private static final int STAMINA_MEAL_PRICE = 24;
    private static final int STAMINA_MEAL_BONUS = 8;
    private static final int HEALING_SALVE_PRICE = 18;
    private static final int STAMINA_DRAUGHT_PRICE = 16;
    private static final int ANTIDOTE_PRICE = 14;
    private static final int WHETSTONE_PRICE = 22;
    private static final int[] WEAPON_BONUSES = {3, 5, 7, 9};
    private static final int[] WEAPON_PRICES = {35, 55, 85, 120};
    private static final int[] ARMOR_BONUSES = {2, 3, 5, 7};
    private static final int[] ARMOR_PRICES = {30, 48, 75, 105};
    private static final int[] FAME_REQUIREMENTS = {0, 50, 150, 300};

    public ShopItem getWeaponItem(Player player) {
        Weapon weapon = createClassWeapon(player);
        return new ShopItem(weapon.getName(), "+" + weapon.getStrengthBonus() + " Strength",
                getRankWeaponPrice(player));
    }

    public ShopItem getNextWeaponItem(Player player) {
        int tier = getNextWeaponTier(player);

        if (tier == -1) {
            return null;
        }

        Weapon weapon = createClassWeaponForTier(player, tier);
        return new ShopItem(weapon.getName(), "+" + weapon.getStrengthBonus() + " Strength",
                WEAPON_PRICES[tier]);
    }

    public Weapon createNextClassWeapon(Player player) {
        int tier = getNextWeaponTier(player);

        if (tier == -1) {
            return null;
        }

        return createClassWeaponForTier(player, tier);
    }

    public int getNextWeaponFameRequirement(Player player) {
        int tier = getNextWeaponTier(player);

        if (tier == -1) {
            return -1;
        }

        return FAME_REQUIREMENTS[tier];
    }

    public ShopItem getArmorItem(Player player) {
        Armor armor = createRankArmor(player);
        return new ShopItem(armor.getName(), "+" + armor.getDefenseBonus() + " Defense",
                getRankArmorPrice(player));
    }

    public ShopItem getNextArmorItem(Player player) {
        int tier = getNextArmorTier(player);

        if (tier == -1) {
            return null;
        }

        Armor armor = createRankArmorForTier(tier);
        return new ShopItem(armor.getName(), "+" + armor.getDefenseBonus() + " Defense",
                ARMOR_PRICES[tier]);
    }

    public Armor createNextRankArmor(Player player) {
        int tier = getNextArmorTier(player);

        if (tier == -1) {
            return null;
        }

        return createRankArmorForTier(tier);
    }

    public int getNextArmorFameRequirement(Player player) {
        int tier = getNextArmorTier(player);

        if (tier == -1) {
            return -1;
        }

        return FAME_REQUIREMENTS[tier];
    }

    public ShopItem getStaminaMealItem() {
        return new ShopItem("Stamina Meal", "+" + STAMINA_MEAL_BONUS + " Max Stamina", STAMINA_MEAL_PRICE);
    }

    public ShopItem getHealingSalveItem() {
        return new ShopItem("Healing Salve", "Use in battle for +30 HP", HEALING_SALVE_PRICE);
    }

    public ShopItem getStaminaDraughtItem() {
        return new ShopItem("Stamina Draught", "Use in battle for +25 stamina", STAMINA_DRAUGHT_PRICE);
    }

    public ShopItem getAntidoteItem() {
        return new ShopItem("Antidote", "Use in battle to cure poison", ANTIDOTE_PRICE);
    }

    public ShopItem getWhetstoneItem() {
        return new ShopItem("Whetstone", "Use in battle for +1 Strength", WHETSTONE_PRICE);
    }

    public Weapon createClassWeapon(Player player) {
        String material = getRankGearMaterial(player);
        int bonus = getRankWeaponBonus(player);

        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return new Weapon(material + " Gladius", bonus);
            case RETIARIUS:
                return new Weapon(material + " Trident", bonus);
            case DIMACHAERUS:
                return new Weapon(material + " Dual Blades", bonus);
            case THRAEX:
                return new Weapon(material + " Sica", bonus);
            default:
                return new Weapon(material + " Weapon", bonus);
        }
    }

    public Armor createRankArmor(Player player) {
        return new Armor(getRankArmorName(player), getRankArmorBonus(player));
    }

    public int getStaminaMealBonus() {
        return STAMINA_MEAL_BONUS;
    }

    private int getNextWeaponTier(Player player) {
        int currentBonus = player.getWeapon().getStrengthBonus();

        for (int i = 0; i < WEAPON_BONUSES.length; i++) {
            if (WEAPON_BONUSES[i] > currentBonus) {
                return i;
            }
        }

        return -1;
    }

    private int getNextArmorTier(Player player) {
        int currentBonus = player.getArmor().getDefenseBonus();

        for (int i = 0; i < ARMOR_BONUSES.length; i++) {
            if (ARMOR_BONUSES[i] > currentBonus) {
                return i;
            }
        }

        return -1;
    }

    private Weapon createClassWeaponForTier(Player player, int tier) {
        String material = getGearMaterialForTier(tier);
        int bonus = WEAPON_BONUSES[tier];

        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return new Weapon(material + " Gladius", bonus);
            case RETIARIUS:
                return new Weapon(material + " Trident", bonus);
            case DIMACHAERUS:
                return new Weapon(material + " Dual Blades", bonus);
            case THRAEX:
                return new Weapon(material + " Sica", bonus);
            default:
                return new Weapon(material + " Weapon", bonus);
        }
    }

    private Armor createRankArmorForTier(int tier) {
        return new Armor(getArmorNameForTier(tier), ARMOR_BONUSES[tier]);
    }

    private String getGearMaterialForTier(int tier) {
        switch (tier) {
            case 3:
                return "Champion's Steel";
            case 2:
                return "Tempered Iron";
            case 1:
                return "Iron";
            case 0:
            default:
                return "Bronze";
        }
    }

    private String getArmorNameForTier(int tier) {
        switch (tier) {
            case 3:
                return "Champion's Cuirass";
            case 2:
                return "Tempered Armor";
            case 1:
                return "Iron Armor";
            case 0:
            default:
                return "Bronze Guard";
        }
    }

    private String getRankGearMaterial(Player player) {
        if (player.getFame() >= 300) {
            return "Champion's Steel";
        } else if (player.getFame() >= 150) {
            return "Tempered Iron";
        } else if (player.getFame() >= 50) {
            return "Iron";
        } else {
            return "Bronze";
        }
    }

    private String getRankArmorName(Player player) {
        if (player.getFame() >= 300) {
            return "Champion's Cuirass";
        } else if (player.getFame() >= 150) {
            return "Tempered Armor";
        } else if (player.getFame() >= 50) {
            return "Iron Armor";
        } else {
            return "Bronze Guard";
        }
    }

    private int getRankWeaponBonus(Player player) {
        if (player.getFame() >= 300) {
            return 9;
        } else if (player.getFame() >= 150) {
            return 7;
        } else if (player.getFame() >= 50) {
            return 5;
        } else {
            return 3;
        }
    }

    private int getRankArmorBonus(Player player) {
        if (player.getFame() >= 300) {
            return 7;
        } else if (player.getFame() >= 150) {
            return 5;
        } else if (player.getFame() >= 50) {
            return 3;
        } else {
            return 2;
        }
    }

    private int getRankWeaponPrice(Player player) {
        if (player.getFame() >= 300) {
            return 120;
        } else if (player.getFame() >= 150) {
            return 85;
        } else if (player.getFame() >= 50) {
            return 55;
        } else {
            return 35;
        }
    }

    private int getRankArmorPrice(Player player) {
        if (player.getFame() >= 300) {
            return 105;
        } else if (player.getFame() >= 150) {
            return 75;
        } else if (player.getFame() >= 50) {
            return 48;
        } else {
            return 30;
        }
    }
}
