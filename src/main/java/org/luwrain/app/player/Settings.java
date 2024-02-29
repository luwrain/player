/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

import java.io.*;
import java.util.*;

import org.luwrain.core.*;

interface Settings
{
    static final String PLAYER_PATH = "/org/luwrain/player";

    String getAlbums(String defValue);
    void setAlbums(String value);

    static Settings create(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	return RegistryProxy.create(registry, PLAYER_PATH, Settings.class);
    }
}
