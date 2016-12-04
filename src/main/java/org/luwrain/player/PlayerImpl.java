
package org.luwrain.player;

import java.util.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.player.backends.*;

class PlayerImpl implements Player, org.luwrain.player.backends.Listener
{
    private final Registry registry;
    private final Vector<Listener> listeners = new Vector<Listener>();

    private Playlist currentPlaylist = null;
    private BackEnd currentPlayer = null;
    private int currentTrackNum = -1;
    private long currentPos = 0;

    PlayerImpl(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    @Override public synchronized Result play(Playlist playlist,
			   int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (playlist.getPlaylistItems() == null)
	    return Result.INVALID_PLAYLIST;
	if (startingTrackNum < 0 || startingTrackNum >= playlist.getPlaylistItems().length)
	    return Result.INVALID_PLAYLIST;
	    stop();
	currentPlaylist = playlist;
	currentTrackNum = startingTrackNum;
	currentPos = startingPosMsec;
	final Result res = runPlayer();
	if (res != Result.OK)
	    return res;
	notifyListeners((l)->l.onNewPlaylist(playlist));
    notifyListeners((l)->l.onNewTrack(playlist, currentTrackNum));
    return Result.OK;
    }

    @Override public synchronized void stop()
    {
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	notifyListeners((listener)->listener.onPlayerStop());
	currentPlayer = null;
	currentPlaylist = null;
    }

    @Override public synchronized void pauseResume()
    {
	if (currentPlayer != null)
	{
	    //pausing
	    currentPlayer.stop();
	    currentPlayer = null;
	    return;
	}
	if (currentPlaylist == null)
	    return;
	//resuming
	final Task task = createTask();
	if (task == null)
	    return;
	task.startPosMsec = currentPos;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPlayer.play(task);
    }

    @Override public synchronized void jump(long offsetMsec)
    {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentPlayer != null)
	    {
			currentPlayer.stop();
	currentPlayer = null;
	    }
	final Task task = createTask();
	if (task == null)
	    return;
	currentPos += offsetMsec;
	if (currentPos < 0)
	    currentPos = 0;
	task.startPosMsec = currentPos;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
		currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPlayer.play(task); 
    }

	@Override public synchronized void nextTrack()
			  {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentTrackNum + 1 >= currentPlaylist.getPlaylistItems().length)
	    return;
	++currentTrackNum;
	if (currentPlayer != null)
	currentPlayer.stop();
	final Task task = createTask();
	if (task == null)
	    return;
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPos = 0;
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
	currentPlayer.play(task); 
			  }

    @Override public synchronized void prevTrack()
    {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentTrackNum <= 0)
	    return;
	--currentTrackNum;
	if (currentPlayer != null)
	currentPlayer.stop();
	final Task task = createTask();
	if (task == null)
	    return;
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPos = 0;
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
	currentPlayer.play(task); 
    }

    @Override public synchronized void onPlayerBackEndTime(long msec)
    {
	if (currentPlaylist == null || currentPlayer == null)
	    return;
	if (currentPos <= msec && msec < currentPos + 50)
	    return;
	currentPos = msec;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
	currentPlaylist.updateStartingPos(currentTrackNum, currentPos);
    }

    @Override public synchronized void onPlayerBackEndFinish()
    {
	if (currentPlaylist == null)
	    return;
    }

    @Override public synchronized Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    @Override public synchronized int getCurrentTrackNum()
    {
	return currentTrackNum;
    } 

    @Override public synchronized void addListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(Listener l: listeners)
	    if (l == listener)
		return;
	listeners.add(listener);
    }

    @Override public synchronized void removeListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(int i = 0;i < listeners.size();++i)
	    if (listeners.get(i) == listener)
	    {
		listeners.remove(i);
		break;
	    }
    }

    @Override public Playlist[] loadRegistryPlaylists()
    {
	final String dir = "/org/luwrain/player/playlists";//FIXME:
	final String[] dirs = registry.getDirectories(dir); 
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: dirs)
	{
	    final String path = Registry.join(dir, s);
	    final RegistryPlaylist playlist = new RegistryPlaylist(registry);
	    if (playlist.init(path))
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }

    private void notifyListeners(ListenerNotification notification)
    {
	NullCheck.notNull(notification, "notification");
	for(Listener l: listeners)
	    notification.notify(l);
    }

    private Task createTask()
    {
	final String[] items = currentPlaylist.getPlaylistItems();
	if (items == null || currentTrackNum < 0 || currentTrackNum >= items.length)
	    return null;
	for(int i = 0;i < items.length;++i)
	    if (items[i] == null)
		return null;
	    final String url = items[currentTrackNum];
	try {
	    return new Task(new URL(url), currentPos);
	}
	catch (Exception e)
	{
	    Log.error("player", "unable to create the URL object for " + url + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
	}
    }

    private Result runPlayer()
    {
	final Task task = createTask();
	if (task == null)
	    return Result.INVALID_PLAYLIST;
	Log.debug("player", "starting playing " + task.url.toString() + " from " + task.startPosMsec);
	final String fileName = task.url.getFile();
	if (fileName.toLowerCase().endsWith(".mp3"))
	    currentPlayer = BackEnd.createBackEnd(this, "jlayer"); else
	{
	    Log.error("player", "unable to play due to unsupported format:" + task.url.toString());
	    return Result.UNSUPPORTED_FORMAT_STARTING_TRACK;
	}
	currentPlayer.play(task);
	return Result.OK;
    }

    private interface ListenerNotification
    {
	void notify(Listener listener);
    }
}
