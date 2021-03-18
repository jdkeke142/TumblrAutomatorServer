package fr.keke142.tumblrautomatorserver.tasks;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Blog;
import fr.keke142.tumblrautomatorserver.TumblrAutomatorServer;
import fr.keke142.tumblrautomatorserver.managers.DataManager;
import fr.keke142.tumblrautomatorserver.objects.Account;
import fr.keke142.tumblrautomatorserver.utils.ProxyUtil;

public class AutofollowTask implements Runnable {
    private final TumblrAutomatorServer server;

    public AutofollowTask(TumblrAutomatorServer server) {
        this.server = server;
    }

    @Override
    public void run() {

        server.getConfigManager().getAutofollow().forEach((account, autofollow) -> {
            DataManager dataManager = server.getDataManager();

            if (dataManager.isFollowLimitReached(account)) return;

            for (String accountToFollow : autofollow.getAccountsToFollow()) {

                if (dataManager.isFollowed(account, accountToFollow)) continue;

                Account attributedAccount = server.getConfigManager().getAccounts().get(account);

                String ip = attributedAccount.getProxy().getIp();
                String port = attributedAccount.getProxy().getPort();
                String user = attributedAccount.getProxy().getUser();
                String password = attributedAccount.getProxy().getPassword();

                ProxyUtil.setProxy(ip, port, user, password);

                //Here tumblr following

                try {
                    JumblrClient client = new JumblrClient(attributedAccount.getConsumerKey(), attributedAccount.getConsumerSecret());
                    client.setToken(attributedAccount.getToken(), attributedAccount.getTokenSecret());

                    Blog blog = client.blogInfo(accountToFollow + ".tumblr.com");
                    blog.follow();

                    dataManager.incrementFollowAmount(account);
                    dataManager.addFollowed(account, accountToFollow);

                    TumblrAutomatorServer.getLogger().info("Started following user " + accountToFollow + " on account " + account);

                    if (dataManager.isFollowLimitReached(account))
                        TumblrAutomatorServer.getLogger().warn("Follow limit reached on account " + account);

                    break;
                } catch (JumblrException ex) {
                    dataManager.addFollowed(account, accountToFollow);

                    TumblrAutomatorServer.getLogger().error("Failed to follow user " + accountToFollow + " on account " + account, ex);
                }


                /*Map<String, Integer> options = new HashMap<>();
                options.put("limit", 20);

                for (int offset = 0; offset < client.user().getFollowingCount(); offset += 20) {
                    options.put("offset", offset);

                    List<Blog> blogs = client.userFollowing(options);
                    for (Blog blog : blogs) {
                        System.out.println("\"" + blog.getName() + "\",");
                    }
                }*/
            }


        });
    }
}
