package org.pierre.remotedroid.server.connection;

import org.pierre.remotedroid.server.PRemoteDroidServerApp;

public abstract class PRemoteDroidServer
{
	protected PRemoteDroidServerApp application;
	
	public PRemoteDroidServer(PRemoteDroidServerApp application)
	{
		this.application = application;
	}
	
	public abstract void close();
}
