# java-sockets [![Build Status](https://travis-ci.org/autermann/java-sockets.png?branch=master)](https://travis-ci.org/autermann/java-sockets) #

Java Socket Utility Classes

## Build

```bash
git clone --recursive -q -b master https://github.com/autermann/java-sockets.git
chmod +x java-sockets/deps.sh && ./java-sockets/deps.sh
mvn -q -f java-sockets/pom.xml clean source:jar javadoc:jar install
```

## License

```
Copyright (C) 2013 by it's authors.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
