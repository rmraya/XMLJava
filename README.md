# XMLJava

Java library for handling XML in [Maxprograms](https://maxprograms.com) projects.

## Licensing

XMLJava is available under **dual licensing** to serve both open source and commercial users:

### 🆓 Open Source License (AGPL-3.0)

XMLJava is licensed under the [**GNU Affero General Public License v3.0**](LICENSE) for open source use.

**Use AGPL-3.0 for:**

- ✅ Open source projects (AGPL-compatible)
- ✅ Personal and educational use
- ✅ Internal business tools (with source sharing requirements)
- ✅ Research and development

### 💼 Commercial License

For organizations that cannot comply with AGPL-3.0 copyleft requirements, [commercial licenses](LICENSE-COMMERCIAL.md) are available.

**Use Commercial License for:**

- ✅ Proprietary software distribution
- ✅ SaaS applications without source sharing
- ✅ Commercial products embedding XMLJava
- ✅ Closed-source applications

📞 **Commercial Licensing Contact:** [sales@maxprograms.com](mailto:sales@maxprograms.com)

For detailed licensing information, see [LICENSING.md](LICENSING.md).

Although standard XML handling in Java is good, this library has features not available in Java SE:

- Working support for [OASIS XML Catalogs](https://www.oasis-open.org/committees/entity/spec.html)
- Default attribute values resolution when parsing XML files with RelaxNG grammars
- XML indenter
- XML characters validation

Features in development:

- DTD parser
- Common XML Grammar handler for DTD, XML Schema and RelaxNG

## Building

You need Java 21 and [Gradle](https://gradle.org/)

- Point your JAVA_HOME variable to JDK 21
- Checkout this repository
- Run `gradle` to compile the source code

``` text
git clone https://github.com/rmraya/XMLJava.git
cd XMLJava
gradle
```
