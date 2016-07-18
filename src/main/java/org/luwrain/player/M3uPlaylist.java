/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
import java.nio.file.*;

import org.luwrain.core.*;

class M3uPlaylist extends PlaylistBase
{
    public boolean load(Path path)
    {
	NullCheck.notNull(path, "path");
	title = "";
	streaming = false;
	hasBookmark = false;
	try {
	    final Path parent = path.getParent();
	    if (parent == null)
		return false;
	final BufferedReader r = new BufferedReader(new InputStreamReader(Files.newInputStream(path)));
	String line;
	while ( (line = r.readLine()) != null )
	{
	    if (line.isEmpty() ||
		(!line.trim().isEmpty() && line.trim().charAt(0) == '#'))
		continue;
	    final Path filePath = parent.resolve(line);
	    if (filePath != null)
		items.add(filePath.toUri().toString());
	}
	return true;
	}
	catch(IOException e)
	{
	    e.printStackTrace();
return false;
	}
    }
}
