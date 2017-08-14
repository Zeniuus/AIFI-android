package com.zeniuus.www.reactiontagging.objects;

import com.zeniuus.www.reactiontagging.types.PromptType;

/**
 * Created by zeniuus on 2017. 8. 14..
 */

public class Prompt {
    PromptType type;
    int time;
    String question;
    boolean shownState;

    public Prompt(PromptType type, int time, String question) {
        this.type = type;
        this.time = time;
        this.question = question;
        shownState = false;
    }

    public PromptType getPromptType() { return type; }

    public int getPromptTime() { return time; }

    public boolean getShownState() { return shownState; }

    public void setShownState(boolean shownState) { this.shownState = shownState; }

    public String getPromptQuestion() { return question; }
}
