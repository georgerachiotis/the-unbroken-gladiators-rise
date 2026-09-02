package arena.fx;

import arena.characters.Enemy;
import arena.characters.Rival;
import arena.enums.GladiatorClass;

class AvatarRegistry {
    private static final String AVATAR_ROOT = "/arena/fx/assets/avatars/";

    AvatarInfo playerAvatar(GladiatorClass gladiatorClass) {
        String key = "player-" + styleKey(gladiatorClass.name());
        return new AvatarInfo(key, displayName(gladiatorClass.name()),
                AVATAR_ROOT + "players/" + key + ".png");
    }

    AvatarInfo enemyAvatar(Enemy enemy) {
        if (enemy.getName().equals("Aurelius the Unbroken")) {
            return new AvatarInfo("champion-aurelius-the-unbroken", "Aurelius",
                    AVATAR_ROOT + "champions/champion-aurelius-the-unbroken.png");
        }

        if (enemy instanceof Rival) {
            return rivalAvatar(enemy.getName());
        }

        String key = "enemy-" + styleKey(enemy.getName());
        return new AvatarInfo(key, displayName(enemy.getName()),
                AVATAR_ROOT + "enemies/" + key + ".png");
    }

    private AvatarInfo rivalAvatar(String name) {
        String key = "rival-" + styleKey(name);
        return new AvatarInfo(key, rivalShortName(name),
                AVATAR_ROOT + "rivals/" + key + ".png");
    }

    private String rivalShortName(String name) {
        if (name.equals("Titus the Butcher")) {
            return "Titus";
        } else if (name.equals("Cassius the Giant")) {
            return "Cassius";
        } else if (name.equals("The Red Wolf")) {
            return "Red Wolf";
        } else if (name.equals("Viper of the Sands")) {
            return "Viper";
        }

        return name;
    }

    private String styleKey(String value) {
        return value.toLowerCase()
                .replace("'", "")
                .replace("_", "-")
                .replace(" ", "-");
    }

    private String displayName(String value) {
        String[] parts = value.toLowerCase().replace('-', ' ').replace('_', ' ').split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return builder.toString();
    }
}
