package org.jmik.asterisk.gui;

import org.apache.log4j.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

public class HelloAgiScript extends BaseAgiScript
{
	private static Logger logger = Logger.getLogger(HelloAgiScript.class);

    public void service(AgiRequest request, AgiChannel channel)
            throws AgiException
    {
    	logger.info("HelloAgiScript service");
        // Answer the channel...
        answer();
        logger.info("answer()");
                
        // ...say hello...
        streamFile("welcome");
        logger.info("streamFile welcome");
                
        // ...and hangup.
        hangup();
        logger.info("hangup()");
        
    }
}