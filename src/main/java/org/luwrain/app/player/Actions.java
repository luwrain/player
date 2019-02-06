
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

final class Actions
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
	RegistryAlbums.addPlaylist(luwrain.getRegistry(), params);
	listArea.refresh();
	return true;
    }

    boolean onDeletePlaylist(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Object obj = listArea.selected();
	if (obj== null || !(obj instanceof Album))
	    return false;
	final Album album = (Album)obj;
	if (!conversations.confirmPlaylistDeleting(album.getPlaylistTitle()))
	    return true;
	RegistryAlbums.deletePlaylist(luwrain.getRegistry(), album.registryPath);
	return true;
    }

    boolean onAlbumClick(Area playlistArea, Object obj)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	if (obj == null || !(obj instanceof Album))
	    return false;
	final Album album = (Album)obj;
	/*
	if (!playlist.flags.contains(Album.Flags.STREAMING))
	{
	    base.player.play(playlist.toPlaylist(), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS);	    
	    luwrain.setActiveArea(playlistArea);
	} else
	{
	    luwrain.playSound(Sounds.PLAYING);
	    base.player.play(playlist.toPlaylist(), 0, 0, EnumSet.of(org.luwrain.player.Player.Flags.STREAMING));
	}
	*/
	luwrain.xRunHooks("luwrain.player.album.play", new Object[]{album}, true);
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
