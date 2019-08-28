package net.shoal.sir.voteup.enums;

public enum VoteUpPerm {
    ALL("VoteUp.*"),
    ADMIN("VoteUp.admin"),
    USER("VoteUp.user"),
    NOTICE("VoteUp.notice"),
    VIEW("VoteUp.view"),
    CREATE("VoteUp.create"),
    CREATE_CUSTOM_ALL("VoteUp.create.*"),
    CREATE_CUSTOM_TITLE("VoteUp.create.title"),
    CREATE_CUSTOM_TYPE("VoteUp.create.type"),
    CREATE_CUSTOM_AMOUNT("VoteUp.create.amount"),
    CREATE_CUSTOM_DESCRIPTION("VoteUp.create.description"),
    CREATE_CUSTOM_DURATION("VoteUp.create.duration"),
    CREATE_CUSTOM_CHOICE("VoteUp.create.choice"),
    CREATE_CUSTOM_AUTOCAST("VoteUp.create.autocast"),
    CREATE_CUSTOM_AUTOCAST_BYPASS("VoteUp.create.autocast.bypass"),
    CREATE_CUSTOM_RESULT("VoteUp.create.result"),
    CREATE_SIMPLE("VoteUp.create.simple"),
    VOTE("VoteUp.vote.*"),
    VOTE_ACCEPT("VoteUp.vote.accept"),
    VOTE_NEUTRAL("VoteUp.vote.neutral"),
    VOTE_REFUSE("VoteUp.vote.refuse"),
    VOTE_REASON("VoteUp.vote.reason");

    private final String node;
    VoteUpPerm(String type) {
        this.node = type;
    }
    public String perm() {
        return node;
    }
}
