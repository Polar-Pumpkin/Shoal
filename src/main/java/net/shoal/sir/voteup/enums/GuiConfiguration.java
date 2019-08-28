package net.shoal.sir.voteup.enums;

public enum GuiConfiguration {
    MAIN_MENU("MainMenu"),
    CREATE_MENU("CreateMenu"),
    VOTE_LIST("VoteList"),
    VOTE_DETAILS_COMMON("VoteDetails_COMMON"),
    VOTE_DETAILS_REASON("VoteDetails_REASON"),
    VOTE_DETAILS_COMPLETE("VoteDetails_COMPLETE"),
    VOTE_DETAILS_PARTICIPANTS("VoteDetails_PARTICIPANTS");

    private final String fileName;
    GuiConfiguration(String fileName) {
        this.fileName = fileName;
    }
    public String getName() {
        return fileName;
    }
}
