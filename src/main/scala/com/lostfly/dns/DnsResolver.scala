package com.lostfly.dns

import cats.effect.IO
import org.xbill.DNS._

trait ExternalDnsResolver {
  def resolve(ip: String): IO[String]
}

class DnsResolver(cache: DnsCache, externalDns: ExternalDnsResolver) {

  def resolve(ip: String): IO[String] =

    cache.get(ip).flatMap {
      case Some(domain) =>
        println(s"DnsResolver: Found $ip -> $domain in cache")
        IO.pure(domain)
      case None =>
        println(s"DnsResolver: $ip not found in cache, querying external DNS")
        externalDns.resolve(ip).flatMap { domain =>
          cache.put(ip, domain) *> IO.pure(domain)
        }
    }
}

class GoogleExternalDnsResolver extends ExternalDnsResolver {
  override def resolve(ip: String): IO[String] = {
    IO.defer {
      val addr = Address.getByAddress(ip)
      val name = ReverseMap.fromAddress(addr)

      val resolver = new SimpleResolver("8.8.8.8") // Используем Google DNS сервер
      println(s"DnsResolver: Request to Google DNS, request address $addr: resolving address $name")
      val lookup = new Lookup(name, Type.PTR)
      lookup.setResolver(resolver)

      val result = lookup.run()

      println(s"DnsResolver: Response from Google DNS: ${result.headOption.map(_.toString).getOrElse("No result")}")

      if (result == null) IO.raiseError(new Exception("DnsResolver: DNS lookup failure"))
      else {
        val records = result.collect { case r: PTRRecord => r }
        if (records.isEmpty) IO.raiseError(new Exception("DnsResolver: No DNS records found"))
        else {

          val domain = records.head.rdataToString()

          IO.pure(domain)
        }
      }
    }
  }
}

object DnsResolver {
  def create(cache: DnsCache, externalResolver: ExternalDnsResolver): DnsResolver =
    new DnsResolver(cache, externalResolver)

  def createWithGoogleExternalDns(cache: DnsCache): DnsResolver =
    new DnsResolver(cache, new GoogleExternalDnsResolver())
}
