package fr.keke142.tumblrautomatorserver.objects;

public class Account {
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String tokenSecret;
    private Proxy proxy;

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public Proxy getProxy() {
        return proxy;
    }

}
