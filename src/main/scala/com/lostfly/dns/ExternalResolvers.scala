package com.lostfly.dns

import cats.effect.IO
import org.xbill.DNS.{ARecord, Address, Lookup, Name, PTRRecord, ReverseMap, SimpleResolver, Type}

import scala.util.matching.Regex

trait ExternalDnsResolver {
  def resolve(ipOrDomain: String, dnsServerIp: String): IO[String]
}

class lsExternalDnsResolver extends ExternalDnsResolver {

  val iPv4Regex: Regex = """(\d{1,3}\.){3}\d{1,3}""".r

  override def resolve(ipOrDomain: String, dnsServerIp: String): IO[String] = {
    IO.defer {
      if (iPv4Regex.matches(ipOrDomain)){
        val addr = Address.getByAddress(ipOrDomain)
        val name = ReverseMap.fromAddress(addr)

        println(
          s"DnsResolver: PTR-Type request to $dnsServerIp DNS, request address $addr: resolving ipaddress $name"
        )

        resolvePtrRecord(name, dnsServerIp)
      } else {

        println(
          s"DnsResolver: A-Type request to $dnsServerIp DNS, request domain $ipOrDomain"
        )
        resolveARecord(ipOrDomain, dnsServerIp)
      }
    }
  }

  private def resolvePtrRecord(name: Name, dnsServerIp: String): IO[String] = {
    val resolver = new SimpleResolver(dnsServerIp)
    val lookup = new Lookup(name, Type.PTR)
    lookup.setResolver(resolver)

    val result = lookup.run()

    result match {
      case null =>
        println(s"DnsResolver: $dnsServerIp DNS lookup domain failure")
        IO.pure("unknown domain")
      case _ =>
        val domain = result.collectFirst { case r: PTRRecord => r.getTarget.toString }.getOrElse("")
        IO.pure(domain)
    }
  }


  private def resolveARecord(domain: String, dnsServerIp: String): IO[String] = {
    val resolver = new SimpleResolver(dnsServerIp)
    val lookup = new Lookup(domain, Type.A)
    lookup.setResolver(resolver)

    val result = lookup.run()

    result match {
      case null =>
        println(s"DnsResolver: $dnsServerIp DNS lookup ipaddress failure")
        IO.pure("unknown ipaddress")
      case _ =>
        val ip = result.collectFirst { case r: ARecord => r.getAddress.getHostAddress }.getOrElse("")
        IO.pure(ip)
    }

  }

}
