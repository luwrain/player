package test;

import java.io.File;
import java.net.URL;

public class Main
{

	public static void main(String[] args) throws Exception
	{
		MpegAudioImpl s = new MpegAudioImpl();
		s.setSource(new File("a.mp3"));
		//s.setSource(new URL("http://promodj.com/download/5783493/Lesha%20Kubik%20-%20Mother%20Earth%20%28promodj.com%29.mp3"));
		System.out.println("play from 60 to 69");
		s.play(0,69000);
		System.out.println("wait 3 sec");
		Thread.sleep(10000);
		System.out.println("play from 0 to 6");
		s.play(30000,36000);
		System.out.println("wait 4 sec");
		Thread.sleep(4000);
		System.out.println("pause");
		s.stop();
		System.out.println("wait 1 sec");
		Thread.sleep(1000);
		System.out.println("resume");
		s.resume();
		System.out.println("wait 10 sec");
		Thread.sleep(10000);
		System.out.println("terminate");
	}
}
