package mekwars.client.common.campaign.clientutils.protocol.commands;

import mekwars.client.common.campaign.clientutils.protocol.IClient;
import mekwars.common.util.MWLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;

/**
 * AckSignon command
 */
public class AckSignonPCmd extends CProtCommand {
    private static final Logger LOGGER = LogManager.getLogger(AckSignonPCmd.class);

	public AckSignonPCmd(IClient mwclient) {
		super(mwclient);
		name = "ack_signon";
	}

	// execute command
	@Override
	public boolean execute(String input) {
		StringTokenizer stringTokenizer = new StringTokenizer(input, delimiter);
		if (check(stringTokenizer.nextToken()) && stringTokenizer.hasMoreTokens()) {
			input = decompose(input);
			stringTokenizer = new StringTokenizer(input, delimiter);
			client.setUsername(stringTokenizer.nextToken());
			echo(input);
			if (client.isDedicated()) {
				try {
					Thread.sleep(5000);
				} catch (Exception ex) {
					LOGGER.error("Exception: ", ex);
				}

				try {
					client.startHost(true, false, false);
				} catch (Exception ex) {
					LOGGER.error("AckSignonPCmd: Error attempting to start host on signon.", ex);
				}
			}
			
			return true;
		}
		return false;
	}

	@Override
	protected void echo(String input) {
		LOGGER.info("Signon acknowledged");
		LOGGER.error("Signon acknowledged");
	}
}
