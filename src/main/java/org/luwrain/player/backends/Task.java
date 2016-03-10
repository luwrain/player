
package org.luwrain.player.backends;

import java.nio.file.*;
import java.net.*;

import org.luwrain.core.NullCheck;

public class Task
{
    private Path path;
    private URL url;
    private long startPosMsec = 0;

    public Task(Path path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
	this.url = url;
	this.startPosMsec = 0;
    }

    public Task(Path path, long startPosMsec)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
	this.url = url;
	this.startPosMsec = startPosMsec;
    }

    public Task(URL url)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.path = null;
	this.startPosMsec = 0;
    }

    public Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.path = null;
	this.startPosMsec = startPosMsec;
    }

    public boolean isPath() {return path != null;}
	public boolean isUrl() {return url != null;}
    public Path path() {return path;}
    public URL url() {return url;}
}
