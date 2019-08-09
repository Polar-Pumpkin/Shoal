package net.shoal.sir.voteup.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public @Data @AllArgsConstructor class Vote {

    private String id;
    private boolean status;
    private VoteType type;
    private int amount;
    private String title;
    private List<String> description;
    private String starter;
    private long startTime;
    private String duration;
    private Map<ChoiceType, Map<String, String>> participant;
    private List<String> autoCast;

    public Map<VoteDataType, Object> data() {
        Map<VoteDataType, Object> voteData = new HashMap<>();

        voteData.put(VoteDataType.ID, id);
        voteData.put(VoteDataType.STATUS, status);
        voteData.put(VoteDataType.TYPE, type);
        voteData.put(VoteDataType.AMOUNT, amount);
        voteData.put(VoteDataType.TITLE, title);
        voteData.put(VoteDataType.DESCRIPTION, description);
        voteData.put(VoteDataType.STARTER, starter);
        voteData.put(VoteDataType.STARTTIME, startTime);
        voteData.put(VoteDataType.DURATION, duration);
        voteData.put(VoteDataType.PARTICIPANT, participant);
        voteData.put(VoteDataType.AUTOCAST, autoCast);

        return voteData;
    }

    public Vote(Map<VoteDataType, Object> data) {
        load(data);
    }

    public void load(Map<VoteDataType, Object> data) {
        this.id = (String) data.get(VoteDataType.ID);
        this.status = (boolean) data.get(VoteDataType.STATUS);
        this.type = (VoteType) data.get(VoteDataType.TYPE);
        this.amount = (int) data.get(VoteDataType.AMOUNT);
        this.title = (String) data.get(VoteDataType.TITLE);
        this.description = (List<String>) data.get(VoteDataType.DESCRIPTION);
        this.starter = (String) data.get(VoteDataType.STARTER);
        this.startTime = (long) data.get(VoteDataType.STARTTIME);
        this.duration = (String) data.get(VoteDataType.DURATION);
        this.participant = (Map<ChoiceType, Map<String, String>>) data.get(VoteDataType.PARTICIPANT);
        this.autoCast = (List<String>) data.get(VoteDataType.AUTOCAST);
    }
}
