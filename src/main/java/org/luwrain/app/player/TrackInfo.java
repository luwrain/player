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
import java.net.*;

import com.mpatric.mp3agic.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

final class TrackInfo
{
    static private final String
	charset = "windows-1251";

    final String track, artist, title, album, comment;
    final long length;
    final int bitrate, sampleRate;
    final boolean vbr;

    TrackInfo(URL url) throws IOException
    {
	final Mp3File mp3file;
	try {
	    final File file = Urls.toFile(url);
	    if (file == null)
		throw new IOException(url + " not a local file");
	    mp3file = new Mp3File(file);
	}
	catch(InvalidDataException | UnsupportedTagException e)
	{
	    throw new IOException("Unable to read tags of " + url.toString(), e);
	}
	this.length = mp3file.getLengthInSeconds();
	this.bitrate = mp3file.getBitrate();
	this.vbr = mp3file.isVbr();
	sampleRate = mp3file.getSampleRate();
	String track = null;
	String artist = null;
	String title = "";
	String album = null;
	String comment = null;
        if (mp3file.hasId3v2Tag())
	{
	    final ID3v2 id3v2Tag = mp3file.getId3v2Tag();
	    track = id3v2Tag.getTrack();
	    artist = id3v2Tag.getArtist();
	    title = id3v2Tag.getTitle();
	    album = id3v2Tag.getAlbum();
	    comment = id3v2Tag.getComment();
        }
	if (mp3file.hasId3v1Tag()) 
	{
	    final ID3v1 id3v1Tag = mp3file.getId3v1Tag();
	    if (track == null || track.isEmpty())
		track = id3v1Tag.getTrack();
	    if (artist == null || artist.isEmpty())
		artist = id3v1Tag.getArtist();
	    if (title == null || title.isEmpty())
		title = id3v1Tag.getTitle();
	    if (album == null || album.isEmpty())
		album = id3v1Tag.getAlbum();
	    if (comment == null || comment.isEmpty())
		comment = id3v1Tag.getComment();
        }
	if (artist == null)
	    artist = "";
	if (title == null)
	    title = "";
	if (album == null)
	    album = "";
	if (comment == null)
	    comment = "";
	if (track == null)
	    track = "";
	this.track = transcode(track);
	this.artist = transcode(artist);
	this.title = transcode(title );
	this.album = transcode(album);
	this.comment = transcode(comment);
    }

    private String transcode(String value) throws IOException
    {
	if (charset == null || charset.isEmpty())
	    return value;

	byte[] bytes = value.getBytes("ISO-8859-1");
	return new String(bytes, "windows-1251");
    }
}
