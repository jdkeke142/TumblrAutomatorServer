package fr.keke142.tumblrautomatorserver.managers;

import fr.keke142.tumblrautomatorserver.TumblrAutomatorServer;
import fr.keke142.tumblrautomatorserver.tasks.AutopostTask;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutopostManager {
    private final TumblrAutomatorServer server;

    public AutopostManager(TumblrAutomatorServer server) {
        this.server = server;

        server.getConfigManager().getAutopost().forEach((account, blogs) -> {
            blogs.forEach((blog, autopost) -> {
                autopost.getPostingTimes().forEach(postingtime -> {
                    String[] startTime = postingtime.getStartTime().split(":");

                    final ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime nextRun = now.withHour(Integer.parseInt(startTime[0])).withMinute(Integer.parseInt(startTime[1]));

                    if (now.compareTo(nextRun) > 0) {
                        nextRun = nextRun.plusDays(1);
                    }

                    final Duration initialDelay = Duration.between(now, nextRun);

                    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                    scheduler.scheduleAtFixedRate(new AutopostTask(server, account, blog, postingtime.getEndTime(), autopost.getPostsFolder(), autopost.getPostTags()),
                            initialDelay.toMillis(),
                            Duration.ofDays(1).toMillis(),
                            TimeUnit.MILLISECONDS);
                });
            });
        });
    }
}
