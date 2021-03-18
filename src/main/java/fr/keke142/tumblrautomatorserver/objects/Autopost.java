package fr.keke142.tumblrautomatorserver.objects;

import java.util.List;

public class Autopost {
    private String postsFolder;
    private List<String> postTags;
    private List<Postingtime> postingTimes;

    public String getPostsFolder() {
        return postsFolder;
    }

    public List<String> getPostTags() {
        return postTags;
    }

    public List<Postingtime> getPostingTimes() {
        return postingTimes;
    }
}
