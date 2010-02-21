package org.pierre.remotedroid.protocol;

import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

public interface PRemoteDroidActionReceiver
{
	public void receiveAction(PRemoteDroidAction action);
}
