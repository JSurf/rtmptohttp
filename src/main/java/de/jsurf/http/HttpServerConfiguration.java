package de.jsurf.http;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;

public class HttpServerConfiguration {

   private static final Logger log = Logger.getLogger(HttpServerConfiguration.class);
   private static HttpServerConfiguration config = null; 
   
   private XMLConfiguration confBackend;  
  
   public HttpServerConfiguration() throws ConfigurationException
   {
	   confBackend = new XMLConfiguration("channels.xml");
	   confBackend.setReloadingStrategy(new FileChangedReloadingStrategy());
	   confBackend.setExpressionEngine(new XPathExpressionEngine());
   }
   
   public static synchronized HttpServerConfiguration getConfiguration() throws ConfigurationException {
	   if (config == null)
	      config = new HttpServerConfiguration();
	   return config;
   }
   
   public String getChannelInput(String channelId) {
	      
	      if (channelId == null)
	    	  return null;
	      if (channelId.startsWith("/"))
	    	  channelId = channelId.substring(1);
	      log.debug("Searching input for channel: "+channelId);
	      String input = confBackend.getString("channels/channel[id = '"+channelId+"']/input");
	      log.debug("Channel input: "+input);
	      return input;
	      
	      /*if ("/pro7".equals(channelId))
			   return "rtmp://megaserver.youfreetv.net/live/pro7.stream swfUrl=http://www.youfreetv.net/medien/player.php?file=swf pageUrl=http://www.youfreetv.net/index.php?section=channel&value=pro7 swfVfy=1 live=1";
		   else
			   return null;*/			   
   }
}
