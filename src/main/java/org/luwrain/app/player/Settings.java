
package org.luwrain.app.player;

import org.luwrain.core.*;

interface Settings
{
    interface Base
    {
	String getTitle(String defValue);
	void setTitle(String value);
    }

    interface StreamingPlaylist extends Base
    {
	String getStreamingUrl(String defValue);
	void setStreamingUrl(String value);
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
}
