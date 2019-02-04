
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    final Conversations conversations;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
	this.conversations = new Conversations(luwrain);
    }

    boolean onAddPlaylist(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Conversations.NewPlaylistParams params = conversations.addPlaylist();
	if (params == null)
	    return true;
	RegistryPlaylists.addPlaylist(luwrain.getRegistry(), params);
	listArea.refresh();
	return true;
    }

    boolean onDeletePlaylist(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Object obj = listArea.selected();
	if (obj== null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	if (!conversations.confirmPlaylistDeleting(playlist.getPlaylistTitle()))
	    return true;
	RegistryPlaylists.deletePlaylist(luwrain.getRegistry(), playlist.registryPath);
	return true;
    }

    boolean onPlaylistsClick(Area playlistArea, Object obj)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	if (!playlist.flags.contains(Playlist.Flags.STREAMING))
	{
	    base.player.play(playlist.toGeneralPlaylist(), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS);	    
	    luwrain.setActiveArea(playlistArea);
	} else
	{
	    luwrain.playSound(Sounds.PLAYING);
	    base.player.play(playlist.toGeneralPlaylist(), 0, 0, EnumSet.of(org.luwrain.player.Player.Flags.STREAMING));
	}
	return true;
    }

    boolean commonKeys(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isModified())
	    return false;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case F5:
		return pauseResume();
	    case ESCAPE:
	    case F6:
		return stop();
	    case F7:
		return prevTrack();
	    case F8:
		return nextTrack();
	    default:
		return false;
	    }
	switch(event.getChar())
	{
	case '-':
	    return jump(-5000);
	case '=':
	    return jump(5000);
	case '[':
	    return jump(-60000);
	case ']':
	    return jump(60000);
	default:
	    return false;
	}
    }

    boolean pauseResume()
    {
	return base.player.pauseResume();
    }

    boolean stop()
    {
	return base.player.stop();
    }

    boolean prevTrack()
    {
	return base.player.prevTrack();
    }

    boolean nextTrack()
    {
	return base.player.nextTrack();
    }

    boolean jump(long offsetMsec)
    {
	return base.player.jump(offsetMsec);
    }
}
