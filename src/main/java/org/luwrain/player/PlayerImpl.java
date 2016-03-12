
package org.luwrain.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.util.RegistryPath;

//import org.luwrain.player.backends.*;

class PlayerImpl implements Player
{
    private final PlayerThread thread = new PlayerThread();
    private Registry registry;

PlayerImpl(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
	thread.startThread();
    }

    @Override public void play(Playlist playlist,
			       int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	thread.run(()->thread.play(playlist, startingTrackNum, startingPosMsec));
    }

    @Override public void stop()
    {
	thread.run(()->thread.stop());
    }

    @Override public void jump(long offsetMsec)
    {
	thread.run(()->thread.jump(offsetMsec));
    }


    @Override public Playlist getCurrentPlaylist()
    {
	return thread.getCurrentPlaylist();
    }

    @Override public int getCurrentTrackNum()
    {
	return thread.getCurrentTrackNum();
    }

    @Override public Playlist[] loadRegistryPlaylists()
    {
	final String dir = "/org/luwrain/player/playlists";//FIXME:
	final String[] dirs = registry.getDirectories(dir); 
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: dirs)
	{
	    final String path = RegistryPath.join(dir, s);
	    final RegistryPlaylist playlist = new RegistryPlaylist(registry);
	    if (playlist.init(path))
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }

    @Override public void addListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	thread.run(()->thread.addListener(listener));
    }

    @Override public void removeListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	thread.run(()->thread.removeListener(listener));
    }
}
