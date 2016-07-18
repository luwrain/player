
package org.luwrain.app.player;

public interface Strings
{
static final String NAME = "luwrain.player";

    String appName();
    String treeAreaName();
    String playlistAreaName();
    String controlAreaName(); 
    String opPauseResume();
    String opStop();
    String opNextTrack();
    String opPrevTrack();
    String playlistPropertiesAreaName();
    String playlistPropertiesAreaTitle();
    String playlistPropertiesAreaUrl();
    String badPlaylistPath(String value);
    String errorLoadingPlaylist(String path);
}
