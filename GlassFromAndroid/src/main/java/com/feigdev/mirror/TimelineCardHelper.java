package com.feigdev.mirror;

import com.feigdev.goauth.AuthPreferences;
import com.feigdev.utils.Lg;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;

/**
 * Created by ejf3 on 2/2/14.
 */
public class TimelineCardHelper {
    private static final String TAG = "TimelineCardHelper";

    /**
     * Insert a new timeline item.
     *
     * @param authPreferences
     * @param id
     * @param appName
     * @param message
     * @param location
     * @param menus
     * @param failedCallback what to do if we fail
     */
    public static void insertTimeline(AuthPreferences authPreferences,
                                      String id,
                                      String appName,
                                      String message,
                                      Location location,
                                      MirrorMenuBuilder menus,
                                      Runnable failedCallback) {
        Lg.d(TAG, "insertTimeline " + appName);
        TimelineItem timelineItem = buildTimelineItem(id, appName, message, location, menus);
        runTimeline(authPreferences, appName, timelineItem, failedCallback);
    }

    private static TimelineItem buildTimelineItem(String id, String appName, String message, Location location, MirrorMenuBuilder menus) {
        // update with random number
        if (null == id)
            id = "21341234";
        if (null == message)
            message = "";
        if (null == appName)
            appName = null;

        String htmlMessage = "<article>\n <section>\n <p class=\"text-auto-size\"> "
                + message + " </p>\n <p>\n </section>\n<footer>"
                + appName + "</footer></p> </article>\n";

        TimelineItem timelineItem = new TimelineItem();
        timelineItem.setId(id);
        timelineItem.setTitle(appName);
        if (!"".equals(message))
            timelineItem.setSpeakableText(message);
        if (null != menus && menus.hasContent())
            timelineItem.setMenuItems(menus.getMenus());
        timelineItem.setSpeakableType("Notification");
        timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

        if (null != htmlMessage)
            timelineItem.setHtml(htmlMessage);

        if (null != location)
            timelineItem.setLocation(location);

        return timelineItem;
    }

    private static void runTimeline(AuthPreferences authPreferences, String appName, TimelineItem timelineItem, Runnable failedCallback) {
        Lg.d(TAG, "runTimeline");

        Mirror service = new Mirror.Builder(new NetHttpTransport(), new AndroidJsonFactory(), null)
                .setApplicationName(appName).build();

        try {
            Lg.d(TAG, "runTimeline with token " + authPreferences.getToken());
            service.timeline().insert(timelineItem).setOauthToken(authPreferences.getToken()).execute();
        } catch (Exception e) {
            Lg.e(TAG, "runTimeline failed", e);
            if (null != failedCallback)
                failedCallback.run();
        }
    }
}