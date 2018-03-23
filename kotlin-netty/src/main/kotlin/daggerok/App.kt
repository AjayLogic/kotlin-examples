package daggerok

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.ChannelHandlerContext

fun Array<String>.toProps() = this
    .toList()
    .flatMap { it.split("\\s+".toRegex()) }
    .filter { it.startsWith("--") }
    .filter { it.contains("=") }
    .map { it.split("=") }
    .map { (it[0] to it[1]) }
    .map {

      val (key, value) = it
      val propertyName = key.substring(2)
      val propertyValues = value.split(",", ", ", ";", "; ")

      (propertyName to propertyValues)
    }
    .toMap()

class TimeServerHandler : ChannelInboundHandlerAdapter() {

  override fun channelActive(ctx: ChannelHandlerContext) {
    val time = ctx.alloc().buffer(4)
    time.writeInt((System.currentTimeMillis() / 1000L + 2208988800L).toInt())

    val channelFuture = ctx.writeAndFlush(time)
    channelFuture.addListener { future ->
      assert(channelFuture === future)
      ctx.close()
    }
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    println("oops")
    cause.printStackTrace()
    ctx.close()
  }
}

class NettyClient {
  fun create() {

    val workers = NioEventLoopGroup(4)
    val bootstrap = Bootstrap()
        .group(workers)
        .channel(NioSocketChannel::class.java)
        .option(ChannelOption.SO_KEEPALIVE, true)

    try {

      val channelFuture = bootstrap
          .handler(TimeServerHandler())
          .connect("127.0.0.1", 8080)
          .sync()

      channelFuture
          .channel()
          .closeFuture()
          .sync()

    } finally {
      workers.shutdownGracefully()
    }
  }
}

fun main(args: Array<String>) {
  val props = args.toProps()
  NettyServer.start(props.getOrDefault("port", 8080) as Int)
  ////FIXME:
  //NettyClient().create()
  println(props)
}
