package fr.keke142.tumblrautomatorserver.objects;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AutofollowData {
    private Date lastDate = new Date();
    private int followAmount = 0;
    private List<String> followed = Collections.emptyList();


    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public int getFollowAmount() {
        return followAmount;
    }

    public void setFollowAmount(int followAmount) {
        this.followAmount = followAmount;
    }

    public List<String> getFollowed() {
        return followed;
    }

    public void setFollowed(List<String> followed) {
        this.followed = followed;
    }
}
