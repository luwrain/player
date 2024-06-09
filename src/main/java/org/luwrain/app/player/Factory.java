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

import com.google.auto.service.*;

import org.luwrain.core.*;

@AutoService(org.luwrain.player.Factory.class)
public class Factory implements org.luwrain.player.Factory
{
    @Override public org.luwrain.player.Player  newPlayer(Params params)
    {
	NullCheck.notNull(params, "params");
	NullCheck.notNull(params.luwrain, "params.luwrain");
	return new Dispatcher(params.luwrain);
    }
}
