
package org.luwrain.app.player;

import org.luwrain.core.events.KeyboardEvent;

interface Actions
{
    void closeApp();
    Base getBase();
    void goToPlaylists();
    void goToPlaylist();
    void goToControl();
    boolean commonKeys(KeyboardEvent event);
    void pauseResume();
    void stop();
    void nextTrack();
    void prevTrack();
    void refreshPlaylist();
}
