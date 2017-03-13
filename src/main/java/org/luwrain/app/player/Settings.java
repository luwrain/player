
package org.luwrain.app.player;

import org.luwrain.core.*;

interface Settings
{
    static final String PLAYLISTS_PATH = "/org/luwrain/player/playlists";
    static final String TYPE_VALUE = "type";

    interface Base
    {
	String getTitle(String defValue);
	void setTitle(String value);
    }

    interface StreamingPlaylist extends Base
    {
	String getUrl(String defValue);
	void setUrl(String value);
    }

    interface DirectoryPlaylist extends Base
    {
	String getPath(String defValue);
	void setPath(String  value);
    }

    static DirectoryPlaylist createDirectoryPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
return RegistryProxy.create(registry, path, DirectoryPlaylist.class);
    }

    static StreamingPlaylist createStreamingPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
return RegistryProxy.create(registry, path, StreamingPlaylist.class);
    }
}
