package com.bananasandwich.bananaclaims.claim;

public class ClaimPopupSettings {

    private PopupDisplayMode displayMode = PopupDisplayMode.ACTIONBAR;

    private String enterTitle = "";
    private String enterSubtitle = "";

    private String leaveTitle = "";
    private String leaveSubtitle = "";

    private String enterSound = "";
    private String leaveSound = "";

    public PopupDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(PopupDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public String getEnterTitle() {
        return enterTitle;
    }

    public void setEnterTitle(String enterTitle) {
        this.enterTitle = enterTitle;
    }

    public String getEnterSubtitle() {
        return enterSubtitle;
    }

    public void setEnterSubtitle(String enterSubtitle) {
        this.enterSubtitle = enterSubtitle;
    }

    public String getLeaveTitle() {
        return leaveTitle;
    }

    public void setLeaveTitle(String leaveTitle) {
        this.leaveTitle = leaveTitle;
    }

    public String getLeaveSubtitle() {
        return leaveSubtitle;
    }

    public void setLeaveSubtitle(String leaveSubtitle) {
        this.leaveSubtitle = leaveSubtitle;
    }

    public String getEnterSound() {
        return enterSound;
    }

    public void setEnterSound(String enterSound) {
        this.enterSound = enterSound;
    }

    public String getLeaveSound() {
        return leaveSound;
    }

    public void setLeaveSound(String leaveSound) {
        this.leaveSound = leaveSound;
    }
}