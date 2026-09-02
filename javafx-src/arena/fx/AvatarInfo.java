package arena.fx;

class AvatarInfo {
    private final String styleKey;
    private final String label;
    private final String imagePath;

    AvatarInfo(String styleKey, String label, String imagePath) {
        this.styleKey = styleKey;
        this.label = label;
        this.imagePath = imagePath;
    }

    String getStyleKey() {
        return styleKey;
    }

    String getLabel() {
        return label;
    }

    String getImagePath() {
        return imagePath;
    }
}
