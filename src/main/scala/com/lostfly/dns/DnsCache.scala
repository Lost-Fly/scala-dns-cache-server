package com.lostfly.dns

import cats.effect.{IO, Ref}

final class DnsCache private (private val cache: Ref[IO, Map[String, String]]) {

  def get(ipOrDomain: String): IO[Option[String]] =
    cache.get.map { cacheMap =>
      val result = cacheMap.get(ipOrDomain)
      println(s"DnsCache: Looking for $ipOrDomain. Found: ${result.isDefined}")
      result
    }

  def put(ip: String, domain: String): IO[Unit] = {
    cache
      .update { currentCache =>
        currentCache + (ip -> domain) + (domain -> ip)
      }
      .map { _ =>
        println(s"DnsCache: Added PTR record $ip -> $domain to cache")
        println(s"DnsCache: Added A record $domain -> $ip to cache")
      }
  }

}

object DnsCache {
  def of(initialIpToDomainMap: Map[String, String]): IO[DnsCache] =
    Ref.of[IO, Map[String, String]](initialIpToDomainMap).map(new DnsCache(_))
}
