/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.player;

import java.io.*;
import java.net.*;

import org.luwrain.core.*;

public class Task
{
public final URL url;
public long startPosMsec = 0;

    public Task(URL url)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.startPosMsec = 0;
    }

    public Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.startPosMsec = startPosMsec;
    }

    InputStream openStream() throws IOException
    {
	return url.openStream();
    }

    public long startPosMsec() {return startPosMsec;}
}
