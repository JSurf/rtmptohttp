/*
   * Copyright 2012 The Netty Project
   *
   * The Netty Project licenses this file to you under the Apache License,
   * version 2.0 (the "License"); you may not use this file except in compliance
   * with the License. You may obtain a copy of the License at:
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   * License for the specific language governing permissions and limitations
   * under the License.
   */
package de.jsurf.http;
  
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.ClosedChannelException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.Watchdog;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.jboss.netty.util.CharsetUtil;

  public class HttpServerHandler extends SimpleChannelUpstreamHandler {
   
	  private static final Logger log = Logger.getLogger(HttpServerHandler.class);

      /** Buffer that stores the response content */
      private final StringBuilder buf = new StringBuilder();
  
      @Override
      public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	  
    	  HttpRequest request = (HttpRequest) e.getMessage();    	  
    	  HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
          response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
          response.setHeader(CONTENT_TYPE, "video/mpts");
          /*response.setChunked(true);
          response.setHeader(Names.TRANSFER_ENCODING, Values.CHUNKED);*/
          
          Channel c = e.getChannel();
          
          // create a media reader
          String inputStream =  HttpServerConfiguration.getConfiguration().getChannelInput(request.getUri());
          
          if (inputStream == null)
          {
        	  response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
        	  ChannelFuture future = c.write(response);
              future.addListener(ChannelFutureListener.CLOSE);
              return;        	  
          }
          
          String path = new java.io.File(".").getCanonicalPath();
          log.debug("Current execution path: "+path);
          
          String[] parameters = new String[] { "-loglevel","error", "-i", inputStream, "-vcodec", "copy", "-acodec", "copy", "-vbsf", "h264_mp4toannexb","-f","mpegts","pipe:1" };

          CommandLine cmdLine = CommandLine.parse("ffmpeg.exe");
          cmdLine.addArguments(parameters); 
          DefaultExecutor executor = new DefaultExecutor();
          final ExecuteWatchdog watchDog = new ExecuteWatchdog(86400000); // One day timeout          
          executor.setWatchdog(watchDog);
          
          PipedInputStream pin = new PipedInputStream();
          PipedOutputStream pout = new PipedOutputStream(pin);
          
          PumpStreamHandler streamHandler = new PumpStreamHandler(pout,System.err);
          executor.setStreamHandler(streamHandler);
          
          DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
          executor.execute(cmdLine,resultHandler);          
          
          c.write(response);
          InputStream in = new BufferedInputStream(pin);
          ChannelFuture future = c.write(new ChunkedStream(in));

          future.addListener(new ChannelFutureListener() {
              @Override
              public void operationComplete(ChannelFuture future) throws Exception {
            	  try {
                	  log.debug("operationComplete: closeChannel");
                      future.getChannel().close();                  
            	  } catch (Exception e) {
            		  
            	  }
            	  log.debug("operationComplete: Destroy ffmpeg process");
            	  watchDog.destroyProcess();            	  
              }
          });
    }
  
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
             throws Exception {
    	 log.debug(e.getCause().getMessage());
    	 log.trace("Exception caught!",e.getCause());
         e.getChannel().close();
     }
}
  