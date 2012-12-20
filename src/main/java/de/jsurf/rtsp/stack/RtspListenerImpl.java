package de.jsurf.rtsp.stack;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class RtspListenerImpl implements RtspListener {

	Logger logger = Logger.getLogger(RtspListenerImpl.class);

	@Override
	public void onRtspRequest(HttpRequest request, Channel chanel) {
       logger.debug("onRtspRequest");
	}

	@Override
	public void onRtspResponse(HttpResponse response) {
       logger.debug("onRtspResponse");
	}

}
