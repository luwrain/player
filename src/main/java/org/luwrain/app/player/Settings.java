
package org.luwrain.app.player;

import org.luwrain.core.*;

interface Settings
{
        static final String PLAYLISTS_PATH = "/org/luwrain/player/playlists";
    static final String PLAYER_PATH = "/org/luwrain/player";
    static final String TYPE_VALUE = "type";

    static final String TYPE_DIRECTORY = "directory";
    static final String TYPE_M3U = "m3u";
    static final String TYPE_STREAMING = "streaming";

    interface Base
    {
	String getTitle(String defValue);
	void setTitle(String value);
	String getType(String type);
	void setType(String type);
    }

    interface Bookmark extends Base
    {
	boolean getWithBookmark(boolean defValue);
	void setWithBookmark(boolean value);
	int getTrackNum(int defValue);
	void setTrackNum(int value);
	int getPosSec(int defValue);
	void setPosSec(int value);
    }

    interface StreamingPlaylist extends Base
    {
	String getUrl(String defValue);
	void setUrl(String value);
    }

    interface DirectoryPlaylist extends Bookmark
    {
	String getPath(String defValue);
	void setPath(String  value);
    }

    interface M3uPlaylist extends Bookmark
    {
	String getM3uUrl(String defValue);
	void setM3uUrl(String  value);
    }

    int getVolume(int defValue);
    void setVolume(int value);

    static Settings create(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
		return RegistryProxy.create(registry, PLAYER_PATH, Settings.class);
    }

    static DirectoryPlaylist createDirectoryPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	return RegistryProxy.create(registry, path, DirectoryPlaylist.class);
    }

    static M3uPlaylist createM3uPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	return RegistryProxy.create(registry, path, M3uPlaylist.class);
    }

    static StreamingPlaylist createStreamingPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	return RegistryProxy.create(registry, path, StreamingPlaylist.class);
    }
}
