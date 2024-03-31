# Caching DNS Server in Scala <img src="https://github.com/devicons/devicon/blob/master/icons/scala/scala-original.svg" title="scala" alt="scala" width="40" height="40"/>&nbsp;
This project implements a caching DNS server written in Scala using the cats-effect library. It provides functionalities for resolving domain names from IP addresses and leverages a cache to improve performance by storing previously resolved entries.

### Project's components:

### 1) DnsCache:

- Represents the in-memory cache for storing resolved domain names.
- Uses Ref[IO, Map[String, String]] from cats-effect to manage concurrent access to the cache data.
- Provides get(ip: String) to retrieve a cached domain name for an IP address.
- Offers put(ip: String, domain: String) to add a domain name mapping to the cache.
### 2) DnsResolver:

- Acts as the core logic for resolving domain names.
- Checks the cache first using cache.get(ip).
- If the domain name is found in the cache (Some(domain)), it returns the cached value directly.
Otherwise, it queries the external DNS server (externalDns.resolve(ip)) using GoogleExternalDnsResolver.
- Upon successful external resolution, it updates the cache (cache.put(ip, domain)) and returns the resolved domain name.
### 3) GoogleExternalDnsResolver:
  
- Uses the org.xbill.DNS library for interacting with external DNS servers.
- Performs a reverse DNS lookup for the given IP address.
- Handles potential errors like DNS lookup failures or no records found.
### 4) DnsServer:

- Represents the main server component that listens for incoming DNS requests.
- Creates a DatagramSocket on port 53 (default DNS port).
- Handles incoming requests:
  - Extracts the requested IP address from the datagram packet.
  - Uses resolver.resolve(ip) to resolve the domain name.
  - Sends the resolved domain name back to the client.
### 5) Main:

- The entry point for the application. Starts the server by calling server.serve(53).

## Advantages of the Code:

- Functional Programming: Utilizes cats-effect for a functional approach, leading to potentially cleaner code and improved error handling.
- Caching: Improves performance by reusing previously resolved entries, reducing load on external DNS servers.
- Modular Design: Components like DnsCache and DnsResolver are separate classes, promoting code reusability and maintainability.
### Libraries and Technologies:

- Scala: Functional programming language for building the server application.
- Cats Effect: Library for handling asynchronous and concurrent operations in Scala.
- org.xbill.DNS: Library for interacting with DNS servers and processing DNS records.
## Example Run:

The provided log snippet demonstrates the server's behavior:

The client makes a request with IP 217.69.139.200 (not yet in cache)
```
DnsServer: Received request from 127.0.0.1: resolving address 217.69.139.200
DnsCache: Looking for 217.69.139.200. Found: false
DnsResolver: 217.69.139.200 not found in cache, querying external DNS
DnsResolver: Request to Google DNS, request address 217.69.139.200/217.69.139.200: resolving address 200.139.69.217.in-addr.arpa.
DnsResolver: Response from Google DNS: 200.139.69.217.in-addr.arpa.	938	IN	PTR	mail.ru.
DnsCache: Added 217.69.139.200 -> mail.ru. to cache
DnsServer: Sending response: mail.ru.
```

The client again makes a request with IP 217.69.139.200 (already in cache)
```
DnsServer: Received request from 127.0.0.1: resolving address 217.69.139.200
DnsCache: Looking for 217.69.139.200. Found: true
DnsResolver: Found 217.69.139.200 -> mail.ru. in cache
DnsServer: Sending response: mail.ru.
```
This project showcases a basic functional caching DNS server in Scala using cats-effect. It demonstrates potential benefits like improved performance through caching and utilizes libraries designed for asynchronous programming.
