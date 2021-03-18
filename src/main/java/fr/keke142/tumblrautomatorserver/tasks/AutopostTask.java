package fr.keke142.tumblrautomatorserver.tasks;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import fr.keke142.tumblrautomatorserver.TumblrAutomatorServer;
import fr.keke142.tumblrautomatorserver.objects.Account;
import fr.keke142.tumblrautomatorserver.utils.ProxyUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutopostTask implements Runnable {
    private final TumblrAutomatorServer server;
    private final String account;
    private final String blog;
    private final String endTime;
    private final String postsFolder;
    private final List<String> postTags;

    public AutopostTask(TumblrAutomatorServer server, String account, String blog, String endTime, String postsFolder, List<String> postTags) {
        this.server = server;
        this.account = account;
        this.blog = blog;
        this.endTime = endTime;
        this.postsFolder = postsFolder;
        this.postTags = postTags;
    }

    @Override
    public void run() {
        String[] endTimeSplitted = endTime.split(":");

        Calendar nowDate = Calendar.getInstance();

        Calendar endTimeDate = Calendar.getInstance();
        endTimeDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTimeSplitted[0]));
        endTimeDate.set(Calendar.MINUTE, Integer.parseInt(endTimeSplitted[1]));
        endTimeDate.set(Calendar.SECOND, 0);

        long diffInMillies = Math.abs(endTimeDate.getTime().getTime() - nowDate.getTime().getTime());
        int diffInSeconds = (int) TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.schedule(() -> {
            Account attributedAccount = server.getConfigManager().getAccounts().get(account);

            String ip = attributedAccount.getProxy().getIp();
            String port = attributedAccount.getProxy().getPort();
            String user = attributedAccount.getProxy().getUser();
            String password = attributedAccount.getProxy().getPassword();

            ProxyUtil.setProxy(ip, port, user, password);

            try {
                JumblrClient client = new JumblrClient(attributedAccount.getConsumerKey(), attributedAccount.getConsumerSecret());
                client.setToken(attributedAccount.getToken(), attributedAccount.getTokenSecret());


                File postsFolderF = new File(postsFolder);

                if (!postsFolderF.exists()) {
                    TumblrAutomatorServer.getLogger().error("The provided posts folder path " + postsFolder + " is not found on account " + account);
                    return;
                }

                FilenameFilter filter = (f, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");

                String lowestFileName = null;
                int lastLowest = 9999;

                for (String fileName : postsFolderF.list(filter)) {
                    int attributedNumber = Integer.parseInt(fileName.substring(fileName.indexOf("[") + 1, fileName.indexOf("]")));

                    if (attributedNumber < lastLowest) {
                        lastLowest = attributedNumber;
                        lowestFileName = fileName;
                    }
                }

                if (lowestFileName == null) {
                    TumblrAutomatorServer.getLogger().error("Haven't found any photo to post for account " + account);
                    return;
                }

                String caption = lowestFileName.substring(0, lowestFileName.lastIndexOf('.')).replaceAll("\\[" + lastLowest + "]", "").trim();

                File postFile = new File(postsFolder + "/" + lowestFileName);

                PhotoPost post = client.newPost(blog, PhotoPost.class);
                post.setPhoto(new Photo(postFile));
                post.setCaption(caption);
                post.setTags(postTags);
                post.save();

                String archivedFolder = postsFolder + "/archived";
                new File(archivedFolder).mkdir();

                postFile.renameTo(new File(archivedFolder + "/" + lowestFileName));

                TumblrAutomatorServer.getLogger().info("Posted " + lowestFileName + " on blog " + blog + " on account " + account);
            } catch (JumblrException | IllegalAccessException | InstantiationException | IOException ex) {
                TumblrAutomatorServer.getLogger().error("Failed to autopost on account " + account, ex);
            }
        }, new Random().nextInt(diffInSeconds), TimeUnit.SECONDS);

        executor.shutdown();
    }
}
