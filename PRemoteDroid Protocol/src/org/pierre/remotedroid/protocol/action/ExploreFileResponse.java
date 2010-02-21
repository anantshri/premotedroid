package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExploreFileResponse extends PRemoteDroidAction
{
	public String currentDirectoryString;
	public String[] fileListString;
	
	public ExploreFileResponse(String currentDirectoryString, String[] fileListString)
	{
		this.currentDirectoryString = currentDirectoryString;
		this.fileListString = fileListString;
	}
	
	public static ExploreFileResponse parse(DataInputStream dis) throws IOException
	{
		String currentDirectoryString = dis.readUTF();
		
		int fileListSize = dis.readInt();
		String[] fileListString = new String[fileListSize];
		
		for (int i = 0; i < fileListSize; i++)
		{
			fileListString[i] = dis.readUTF();
		}
		
		return new ExploreFileResponse(currentDirectoryString, fileListString);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.write(EXPLORE_FILE_RESPONSE);
		
		dos.writeUTF(this.currentDirectoryString);
		dos.writeInt(this.fileListString.length);
		
		for (String s : this.fileListString)
		{
			dos.writeUTF(s);
		}
	}
	
}
