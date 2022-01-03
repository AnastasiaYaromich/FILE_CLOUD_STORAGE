package server;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class HandlerProvider {
    public ChannelHandler[] getSerializePipeline() {
        return new ChannelHandler[] {
                // Декодер
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                // Инкодер
                new ObjectEncoder(),
                // handler
                new AbstractMessageHandler()
        };
    }

}