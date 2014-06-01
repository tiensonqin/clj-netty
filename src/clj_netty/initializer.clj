(ns clj-netty.initializer
  (:import (io.netty.channel ChannelInitializer)
           (io.netty.handler.codec.protobuf ProtobufDecoder
                                            ProtobufEncoder
                                            ProtobufVarint32FrameDecoder
                                            ProtobufVarint32LengthFieldPrepender)
           (io.netty.channel.socket SocketChannel)
           Rpc$Request
           Rpc$Response
           ))

(defn ^ChannelInitializer server-channel-initializer [handler]
  (proxy [ChannelInitializer] []
    (initChannel [^SocketChannel ch]
      (.. ch
          pipeline
          (addLast "frameDecoder" (ProtobufVarint32FrameDecoder.))
          (addLast "protobufDecoder" (ProtobufDecoder. (Rpc$Request/getDefaultInstance)))
          (addLast "frameEncoder" (ProtobufVarint32LengthFieldPrepender.))
          (addLast "protobufEncoder" (ProtobufEncoder.))
          (addLast "handler" (handler))))))

(defn ^ChannelInitializer client-channel-initializer [handler]
  (proxy [ChannelInitializer] []
    (initChannel [^SocketChannel ch]
      (.. ch
          pipeline
          (addLast "frameDecoder" (ProtobufVarint32FrameDecoder.))
          (addLast "protobufDecoder" (ProtobufDecoder. (Rpc$Response/getDefaultInstance)))
          (addLast "frameEncoder" (ProtobufVarint32LengthFieldPrepender.))
          (addLast "protobufEncoder" (ProtobufEncoder.))
          (addLast "handler" (handler))))))
