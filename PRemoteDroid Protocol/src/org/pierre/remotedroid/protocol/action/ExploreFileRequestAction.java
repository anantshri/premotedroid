package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExploreFileRequestAction extends PRemoteDroidAction
{
	public String directory;
	public String file;
	
	public ExploreFileRequestAction(String directory, String file)
	{
		this.directory = directory;
		this.file = file;
	}
	
	public static ExploreFileRequestAction parse(DataInputStream dis) throws IOException
	{
		String directory = dis.readUTF();
		String file = dis.readUTF();
		
		return new ExploreFileRequestAction(directory, file);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.write(EXPLORE_FILE_REQUEST);
		dos.writeUTF(this.directory);
		dos.writeUTF(this.file);
	}
}
