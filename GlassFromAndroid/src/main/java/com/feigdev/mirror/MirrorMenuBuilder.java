package com.feigdev.mirror;

import com.google.api.services.mirror.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ejf3 on 2/2/14.
 *
 * For reference, see docs here:
 * https://developers.google.com/glass/v1/reference/timeline#menuItems
 */
public class MirrorMenuBuilder {
    List<MenuItem> menus = new ArrayList<MenuItem>();

    public static final String READ_ALOUD = "READ_ALOUD";
    public static final String REPLY = "REPLY";
    public static final String REPLY_ALL = "REPLY_ALL";
    public static final String DELETE = "DELETE";
    public static final String SHARE = "SHARE";
    public static final String VOICE_CALL = "VOICE_CALL";
    public static final String NAVIGATE = "NAVIGATE";
    public static final String TOGGLE_PINNED = "TOGGLE_PINNED";
    public static final String OPEN_URI = "OPEN_URI";
    public static final String PLAY_VIDEO = "PLAY_VIDEO";

    public MirrorMenuBuilder() {
    }

    public List<MenuItem> getMenus() {
        return menus;
    }

    public boolean hasContent(){
        return (menus.size() > 0);
    }

    /**
     * READ_ALOUD - Read the timeline item's speakableText aloud; if this field is
     * not set, read the text field; if none of those fields are set, this menu item
     * is ignored.
     *
     * @param payload - the text to read aloud
     */
    public MirrorMenuBuilder addReadAloudAction(String payload) {
        menus.add(new MenuItem().setAction(READ_ALOUD).setPayload(payload));
        return this;
    }

    /**
     * REPLY - Initiate a reply to the timeline item using the voice recording UI.
     * The creator attribute must be set in the timeline item for this menu to be
     * available.
     */
    public MirrorMenuBuilder addReplyAction() {
        menus.add(new MenuItem().setAction(REPLY));
        return this;
    }

    /**
     * REPLY_ALL - Same behavior as REPLY. The original timeline item's recipients
     * will be added to the reply item.
     */
    public MirrorMenuBuilder addReplyAllAction() {
        menus.add(new MenuItem().setAction(REPLY_ALL));
        return this;
    }

    /**
     * DELETE - Delete the timeline item.
     */
    public MirrorMenuBuilder addDeleteAction() {
        menus.add(new MenuItem().setAction(DELETE));
        return this;
    }

    /**
     * SHARE - Share the timeline item with the available contacts.
     */
    public MirrorMenuBuilder addShareAction() {
        menus.add(new MenuItem().setAction(SHARE));
        return this;
    }

    /**
     * VOICE_CALL - Initiate a phone call using the timeline item's creator.phone_number
     * attribute as recipient.
     */
    public MirrorMenuBuilder addVoiceCallAction() {
        menus.add(new MenuItem().setAction(VOICE_CALL));
        return this;
    }

    /**
     * NAVIGATE - Navigate to the timeline item's location.
     */
    public MirrorMenuBuilder addNavigateAction() {
        menus.add(new MenuItem().setAction(NAVIGATE));
        return this;
    }

    /**
     * TOGGLE_PINNED - Toggle the isPinned state of the timeline item.
     */
    public MirrorMenuBuilder addTogglePinnedAction() {
        menus.add(new MenuItem().setAction(TOGGLE_PINNED));
        return this;
    }

    /**
     * OPEN_URI - Open the payload of the menu item in the browser.
     *
     * @param payload - URI to open
     */
    public MirrorMenuBuilder addOpenUriAction(String payload) {
        menus.add(new MenuItem().setAction(OPEN_URI).setPayload(payload));
        return this;
    }

    /**
     * PLAY_VIDEO - Open the payload of the menu item in the Glass video player.
     *
     * @param payload - address of the video
     */
    public MirrorMenuBuilder addPlayVideoAction(String payload) {
        menus.add(new MenuItem().setAction(PLAY_VIDEO).setPayload(payload));
        return this;
    }

}
