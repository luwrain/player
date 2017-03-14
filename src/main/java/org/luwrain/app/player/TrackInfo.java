
package org.luwrain.app.player;

import java.io.*;
import java.net.*;

import com.mpatric.mp3agic.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

class TrackInfo
{
    final String track;
    final String artist;
    final String title;
    final String album;
    final String comment;

    final long length;
    final int bitrate;
    final boolean vbr;
    final int sampleRate;

    TrackInfo(URL url) throws IOException
    {
	final Mp3File mp3file;
	try {
	    mp3file = new Mp3File(Urls.toFile(url));
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
	this.track = track;
	this.artist = artist;
	this.title = title ;
	this.album = album;
	this.comment = comment;
    }
}