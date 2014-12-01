# BB Common
Various Required Packages for Big Data related Projects.  These packages are rather oddly exposed and should likely be given a little more consideration to how they're packaged and distributed.  However, since they're required for building newer versions of [KaBoom](https://github.com/blackberry/KaBoom) and [Klogger](https://github.com/blackberry/klogger).

## com.blackberry.common.conversion
* Low overhead
* Conversion from long to bytes/vicea versa.
* HDFS path template parsing

## com.blackberry.common.props
* Initial version that will evolve into property parsing abstraction layer for config across files and ZK

## com.blackberry.common.threads
* Rather hack-ish implementation of thread notify and listener
* Not really used at the moment except within the load balancer of KaBoom to spawn the read flag writer

## Author(s)
* [Dave Ariens](<mailto:dariens@blackberry.com>) (current maintainer)

## Building
Performing a Maven install produces the JAR required for building dependent packages 

## Contributing
To contribute code to this repository you must be [signed up as an official contributor](http://blackberry.github.com/howToContribute.html).

## Disclaimer
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.