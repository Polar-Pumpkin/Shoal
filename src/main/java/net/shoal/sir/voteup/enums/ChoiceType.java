package net.shoal.sir.voteup.enums;

public enum ChoiceType {
    ACCEPT("同意"),
    NEUTRAL("中立"),
    REFUSE("反对");

    final String name;
    ChoiceType(String type) {
        this.name = type;
    }
    public String getName() {
        return name;
    }
}
