package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExploreFileRequest extends PRemoteDroidAction
{
	public String currentDirectoryString;
	public String exploreFileString;
	
	public ExploreFileRequest(String currentDirectoryString, String exploreFileString)
	{
		this.currentDirectoryString = currentDirectoryString;
		this.exploreFileString = exploreFileString;
	}
	
	public static ExploreFileRequest parse(DataInputStream dis) throws IOException
	{
		String currentFileString = dis.readUTF();
		String exploreFileString = dis.readUTF();
		
		return new ExploreFileRequest(currentFileString, exploreFileString);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.write(EXPLORE_FILE_REQUEST);
		dos.writeUTF(this.currentDirectoryString);
		dos.writeUTF(this.exploreFileString);
	}
}
