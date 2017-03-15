
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class Actions
{

    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
    }

Action[] getPlaylistsActions()
    {
	return new Action[]{
	    new Action("add-playlist-without-bookmark", strings.actionAddPlaylistWithoutBookmark()),
	    new Action("add-playlist-with-bookmark", strings.actionAddPlaylistWithBookmark()),
	    new Action("add-streaming-playlist", strings.actionAddStreamingPlaylist()),
	};
    }

    Action[] getPlaylistActions()
    {
	return new Action[]{
	    //	    new Action("sort", "Сортировать"),
	};
    }

    boolean onPlaylistsClick(Area playlistArea, Object obj)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	/*
	if (playlist.getFlags().contains(Playlist.Flags.HAS_BOOKMARK) && !playlist.getFlags().contains(Playlist.Flags.STREAMING))
	    base.player.play(playlist, playlist.getStartingTrackNum(), playlist.getStartingPosMsec()); else
	*/
	    base.player.play(playlist, 0, 0);
	luwrain.setActiveArea(playlistArea);
	return true;
    }

    void pauseResume()
    {
	base.player.pauseResume();
    }

    void stop()
    {
	base.player.stop();
    }

    void jump(long offsetMsec)
    {
	base.player.jump(offsetMsec);
    }


}
