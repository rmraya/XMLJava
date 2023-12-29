# XMLJava

Java library for handling XML in [Maxprograms](https://maxprograms.com) projects.

Although standard XML handling in Java is good, this library has features not available in Java SE:

- Working support for [OASIS XML Catalogs](https://www.oasis-open.org/committees/entity/spec.html)
- Default attribute values resolution when parsing XML files with RelaxNG grammars
- XML indenter
- XML characters validation

Features in development:

- DTD parser
- Common XML Grammar handler for DTD, XML Schema and RelaxNG

## Building

You need Java 21 and [Apache Ant 1.10.14](https://ant.apache.org) or newer

- Point your JAVA_HOME variable to JDK 21
- Checkout this repository
- Run `ant` to compile the source code

``` text
git clone https://github.com/rmraya/XMLJava.git
cd XMLJava
ant
```
