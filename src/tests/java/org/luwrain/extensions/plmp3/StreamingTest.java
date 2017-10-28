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
import java.util.*;

import org.junit.*;

import org.luwrain.base.*;

public class StreamingTest extends Assert
{
    @Test public void radioClassics() throws MalformedURLException
    {
	run(new URL("http://music.myradio.com.ua:8000/Classica128.mp3"));
    }

    @Test public void radioEchoOfMoscow() throws MalformedURLException
    {
	run(new URL("http://radio.2kom.ru:8000/moscowecho128.mp3"));
    }

    @Test public void radioNasheRadio() throws MalformedURLException
    {
	run(new URL("http://178.161.130.30:8000/radio"));
    }

    @Test public void radioMayak() throws MalformedURLException
    {
	run(new URL("http://icecast.vgtrk.cdnvideo.ru/mayakfm_mp3_64kbps"));
    }

    @Test public void radioRockFm() throws MalformedURLException
    {
	run(new URL("http://radio.2kom.ru:8000/RockFM"));
    }

    @Test public void radioRussian() throws MalformedURLException
    {
	run(new URL("http://stream.fmprod.ru/rusradio"));
    }

    @Test public void radioHumourFm() throws MalformedURLException
    {
	run(new URL("http://ic2.101.ru:8000/s12"));
}

    @Test public void radioRoadRadio() throws MalformedURLException
    {
	run(new URL("http://radio.2kom.ru:8000/Dorognoe"));
    }

    @Test public void radioVos() throws MalformedURLException
    {
	run(new URL("http://radio.radiovos.ru:8000/radio"));
    }

    @Test public void radioHit80s() throws MalformedURLException
    {
	run(new URL("http://radio.2kom.ru:8000/1hits80s"));
    }

    @Test public void radioDfm() throws MalformedURLException
    {
	run(new URL("http://stream.fmprod.ru/dfm"));
    }

    @Test public void radioJazz() throws MalformedURLException
    {
	run(new URL("http://music.myradio.com.ua:8000/JazzRockFusion128.mp3"));
    }

    @Test public void radioRansis() throws MalformedURLException
    {
	run(new URL("http://radio.ransis.org:8000/ransis"));
    }

    @Test public void radioPremium() throws MalformedURLException
    {
	run(new URL("http://listen.rpfm.ru:9000/premium128"));
    }

    @Test public void radioMir() throws MalformedURLException
    {
	run(new URL("http://icecast.mirtv.cdnvideo.ru:8000/radio_mir128"));
    }

    @Test public void radioShanson() throws MalformedURLException
    {
	run(new URL("http://www.nts-tv.ru:8000/Shanson"));
    }

    @Test public void radioRetroFm() throws MalformedURLException
    {
	run(new URL("http://retroserver.streamr.ru:8043/retro128"));
    }

    @Test public void radioEuropePlus() throws MalformedURLException
    {
	run(new URL("http://radio.2kom.ru:8000/europaplus"));
    }

    private void run(URL url)
    {
	final Factory factory = new Factory();
	final Listener listener = new Listener();
	final MediaResourcePlayer player = factory.newMediaResourcePlayer(listener);
	player.play(url, 0, EnumSet.noneOf(MediaResourcePlayer.Flags.class));
	while(listener.msec < 0)
	{
	    assertFalse(listener.finished);
	    assertTrue(listener.exception == null);
	}
	player.stop();
    }
}
