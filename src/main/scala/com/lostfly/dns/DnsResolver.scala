package com.lostfly.dns

import cats.effect.IO

class DnsResolver(
    cache: DnsCache,
    externalDnsResolver: lsExternalDnsResolver,
    primaryDnsIp: String,
    secondaryDnsIp: Option[String]
) {
  def resolve(ipOrDomain: String): IO[String] = {
    val primaryResolution = for {
      cachedDomain <- cache.get(ipOrDomain)
      domain <- cachedDomain match {
        case Some(domain) =>
          println(s"DnsResolver: Found $ipOrDomain -> $domain in cache")
          IO.pure(domain)
        case None =>
          println(
            s"DnsResolver: $ipOrDomain not found in cache, querying external DNS"
          )
          resolveWithDns(externalDnsResolver, ipOrDomain, primaryDnsIp)
      }
    } yield domain

    secondaryDnsIp match {
      case Some(backupDnsIp) =>
        primaryResolution.handleErrorWith { _ =>
          println(s"DnsResolver: Primary DNS failed, trying with secondary DNS")
          resolveWithDns(externalDnsResolver, ipOrDomain, backupDnsIp)
        }
      case None => primaryResolution
    }
  }

  private def resolveWithDns(
      dnsResolver: lsExternalDnsResolver,
      ipOrDomain: String,
      dnsServerIp: String
  ): IO[String] = {
    dnsResolver.resolve(ipOrDomain, dnsServerIp).flatMap { resIpOrDomain =>
      cache.put(ipOrDomain, resIpOrDomain) *> IO.pure(resIpOrDomain)
    }
  }
}
object DnsResolver {
  def create(
      cache: DnsCache,
      externalDnsResolver: lsExternalDnsResolver,
      primaryDnsIp: String,
      secondaryDnsIp: Option[String]
  ): DnsResolver =
    new DnsResolver(cache, externalDnsResolver, primaryDnsIp, secondaryDnsIp)

}
