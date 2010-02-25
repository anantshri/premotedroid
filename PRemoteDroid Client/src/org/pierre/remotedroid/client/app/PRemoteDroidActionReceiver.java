package org.pierre.remotedroid.client.app;

import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

public interface PRemoteDroidActionReceiver
{
	public void receiveAction(PRemoteDroidAction action);
}
