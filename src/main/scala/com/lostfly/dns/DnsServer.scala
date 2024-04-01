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
        println(
          s"DnsServer: Received request from $clientIp: resolving address $requestedAddress"
        )

        resolver.resolve(requestedAddress).flatMap { domain =>
          IO {
            val responseData = domain.getBytes
            val response = new java.net.DatagramPacket(
              responseData,
              responseData.length,
              request.getAddress,
              request.getPort
            )
            println(s"DnsServer: Sending response: $domain")
            server.send(response)
          }
        }
      }.flatMap(_ => loop())

    loop().as(ExitCode.Success)
  }
}

object DnsServer extends IOApp {

  val initialEntriesIpToDn: Map[String, String] = Map(
    "5.165.2.92" -> "lostfly.ru",
    "188.114.99.234" -> "stackoverflow.com",
    "188.114.99.224" -> "leetcode.com",
    "140.82.121.3" -> "github.com",
    "64.233.164.194" -> "docs.google.com",
    "docs.google.com" -> "64.233.164.194",
    "mail.ru" -> "217.69.139.202"
  )

  override def run(args: List[String]): IO[ExitCode] = {

    val cacheIO: IO[DnsCache] = DnsCache.of(initialEntriesIpToDn)

    cacheIO.flatMap { cache =>
      val googleDnsIP: String = "8.8.8.8"
      val yandexDnsIP: String = "77.88.8.8"
      val externalDnsResolver = new lsExternalDnsResolver()
      val resolver = new DnsResolver(
        cache,
        externalDnsResolver,
        googleDnsIP,
        Some(yandexDnsIP)
      )
      val server = new DnsServer(resolver)

      server.serve(53).as(ExitCode.Success)
    }
  }

}
