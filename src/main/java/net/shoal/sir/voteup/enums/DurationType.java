package net.shoal.sir.voteup.enums;

public enum DurationType {
    DAY("D", 86400),
    HOUR("H", 3600),
    MINUTE("M", 60);

    private final String name;
    private final int time;
    DurationType(String type, int time) {
        this.name = type;
        this.time = time;
    }
    public String getS() {
        return name;
    }
    public int getI() {
        return time;
    }
}
