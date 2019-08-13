package net.shoal.sir.voteup.enums;

public enum ResultType {
    PASS("通过"),
    REJECT("未通过");

    private final String name;
    ResultType(String type) {
        this.name = type;
    }
    public String getName() {
        return name;
    }
}
