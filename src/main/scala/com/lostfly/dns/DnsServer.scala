package com.lostfly.dns

import cats.effect.{ExitCode, IO, IOApp}

class DnsServer(resolver: DnsResolver) {

  def serve(port: Int): IO[ExitCode] = {
    val server = new java.net.DatagramSocket(port)
    val buffer = new Array[Byte](1024)

    def loop(): IO[Unit] =
      IO {
        val request = new java.net.DatagramPacket(buffer, buffer.length)
        server.receive(request)
        request
      }.flatMap { request =>
        val clientIp = request.getAddress.getHostAddress
        val requestedAddress = new String(request.getData, 0, request.getLength)
        println(s"DnsServer: Received request from $clientIp: resolving address $requestedAddress")

        resolver.resolve(requestedAddress).flatMap { domain =>
          IO {
            val responseData = domain.getBytes
            val response = new java.net.DatagramPacket(responseData, responseData.length, request.getAddress, request.getPort)
            println(s"DnsServer: Sending response: $domain")
            server.send(response)
          }
        }
      }.flatMap(_ => loop())

    loop().as(ExitCode.Success)
  }
}

object DnsServer extends IOApp {

  val initialEntries: Map[String, String] = Map(
    "93.186.225.194" -> "vk.com",
    "5.165.2.92" -> "lostfly.ru",
    "188.114.99.234" -> "stackoverflow.com",
    "188.114.99.224" -> "leetcode.com",
    "140.82.121.3" -> "github.com",
    "64.233.164.194" -> "docs.google.com",
  )

  override def run(args: List[String]): IO[ExitCode] = {

    val cacheIO: IO[DnsCache] = DnsCache.of(initialEntries)

    cacheIO.flatMap { cache =>
      val externalDnsResolver = new GoogleExternalDnsResolver()
      val resolver = new DnsResolver(cache, externalDnsResolver)
      val server = new DnsServer(resolver)

      server.serve(53).as(ExitCode.Success)
    }
  }

}
