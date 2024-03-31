package com.lostfly.dns

import cats.effect.{IO, Ref}

final class DnsCache private (private val cache: Ref[IO, Map[String, String]]) {

  def get(ip: String): IO[Option[String]] =
    cache.get.map { cacheMap =>
      val result = cacheMap.get(ip)
      println(s"DnsCache: Looking for $ip. Found: ${result.isDefined}")
      result
    }

  def put(ip: String, domain: String): IO[Unit] =
    cache.update(_ + (ip -> domain)).map { _ =>
      println(s"DnsCache: Added $ip -> $domain to cache")
    }
}

object DnsCache {
  def of(initialIpToDomainMap: Map[String, String]): IO[DnsCache] =
    Ref.of[IO, Map[String, String]](initialIpToDomainMap).map(new DnsCache(_))
}

