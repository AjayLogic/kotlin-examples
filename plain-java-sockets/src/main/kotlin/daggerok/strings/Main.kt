package daggerok.strings
// same shit...
fun main(args: Array<String>) {
  SocketServer.start()
  SocketServer.waitForReady()
  SocketClient.publish("hey")
  SocketClient.publish("ho")
  SocketServer.stop()
}
