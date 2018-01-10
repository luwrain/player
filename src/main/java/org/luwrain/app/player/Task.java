/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.player;

import java.net.*;

import org.luwrain.core.*;

final class Task
{
    final URL url;
    final long startPosMsec;

    Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	if (startPosMsec < 0)
	    throw new IllegalArgumentException("startPosMsec (" + startPosMsec + ") may not be negative");
	this.url = url;
	this.startPosMsec = startPosMsec;
    }
}
