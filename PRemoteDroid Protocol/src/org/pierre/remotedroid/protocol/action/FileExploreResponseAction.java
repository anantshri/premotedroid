package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileExploreResponseAction extends PRemoteDroidAction
{
	public String directory;
	public String[] files;
	
	public FileExploreResponseAction(String directory, String[] files)
	{
		this.directory = directory;
		this.files = files;
	}
	
	public static FileExploreResponseAction parse(DataInputStream dis) throws IOException
	{
		String directory = dis.readUTF();
		
		int filesSize = dis.readInt();
		String[] files = new String[filesSize];
		
		for (int i = 0; i < filesSize; i++)
		{
			files[i] = dis.readUTF();
		}
		
		return new FileExploreResponseAction(directory, files);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(FILE_EXPLORE_RESPONSE);
		
		dos.writeUTF(this.directory);
		dos.writeInt(this.files.length);
		
		for (String s : this.files)
		{
			dos.writeUTF(s);
		}
	}
	
}
