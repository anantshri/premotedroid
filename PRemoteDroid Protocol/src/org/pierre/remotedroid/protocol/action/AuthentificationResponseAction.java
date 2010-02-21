package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthentificationResponseAction extends PRemoteDroidAction
{
	public boolean authentificated;
	
	public AuthentificationResponseAction(boolean authentificated)
	{
		this.authentificated = authentificated;
	}
	
	public static AuthentificationResponseAction parse(DataInputStream dis) throws IOException
	{
		boolean authentificated = dis.readBoolean();
		
		return new AuthentificationResponseAction(authentificated);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(AUTHENTIFICATION_RESPONSE);
		dos.writeBoolean(this.authentificated);
	}
}
