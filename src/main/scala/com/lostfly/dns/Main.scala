package com.lostfly.dns

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    DnsServer.run(List.empty)
  }

}
