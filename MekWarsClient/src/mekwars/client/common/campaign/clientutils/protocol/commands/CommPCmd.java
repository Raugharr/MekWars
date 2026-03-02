package mekwars.client.common.campaign.clientutils.protocol.commands;

import java.util.StringTokenizer;

import mekwars.client.common.campaign.clientutils.protocol.IClient;
import mekwars.common.campaign.clientutils.protocol.TransportCodec;

/**
 * Comm command
 */

public class CommPCmd extends CProtCommand
{
	public CommPCmd(IClient mwclient) 
	{
		super(mwclient);
		name = "comm";
	}

	// execute command
	@Override
	public boolean execute(String input) {
		
		StringTokenizer ST = new StringTokenizer(input, delimiter);
		if (check(ST.nextToken()) && ST.hasMoreTokens()) {
			input = TransportCodec.unescape(ST.nextToken());
			client.doParseDataInput(input);
			return true;
		}
		return false; 
	}

	// echo command in GUI
	@Override
	protected void echo(String input) {}
}
