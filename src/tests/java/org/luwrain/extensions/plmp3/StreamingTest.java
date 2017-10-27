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

package org.luwrain.extensions.plmp3;

import java.net.*;

import org.junit.*;

import org.luwrain.base.*;

public class StreamingTest extends Assert
{
    @Test public void proba()
    {
	run(null);
    }

    private void run(URL url)
    {
	final Factory factory = new Factory();
	final Listener listener = new Listener();
	//	final MediaResourcePlayer player = factory.newMediaResourcePlayer(listener);
	assertTrue(trueg);
    }
}
