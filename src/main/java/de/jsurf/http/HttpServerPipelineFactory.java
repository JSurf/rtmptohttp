package de.jsurf.http;

import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.*;

public class HttpServerPipelineFactory implements ChannelPipelineFactory {
		       public ChannelPipeline getPipeline() throws Exception {

		    	   // Create a default pipeline implementation.
		           ChannelPipeline pipeline = pipeline();
		   
		           // Uncomment the following line if you want HTTPS
		           //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
		           //engine.setUseClientMode(false);
		           //pipeline.addLast("ssl", new SslHandler(engine));
		   
		           pipeline.addLast("decoder", new HttpRequestDecoder());
		           // Uncomment the following line if you don't want to handle HttpChunks.
		           pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		           pipeline.addLast("encoder", new HttpResponseEncoder());
		           // Remove the following line if you don't want automatic content compression.
		           pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());		           
		           pipeline.addLast("handler", new HttpServerHandler());
		           return pipeline;
		       }
}
