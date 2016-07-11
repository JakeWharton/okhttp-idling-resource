OkHttp Idling Resource
======================

An Espresso `IdlingResource` for OkHttp.



Usage
-----

With your `OkHttpClient` instance, create an idling resource:
```java
OkHttpClient client = // ...
IdlingResource resource = OkHttp3IdlingResource.create("OkHttp", client);
```

Register the idling resource with `Espresso` before any of your tests.
```java
Espresso.registerIdlingResources(resource);
```



Download
--------

```groovy
androidTestCompile 'com.jakewharton.espresso:okhttp3-idling-resource:1.0.0'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].



License
-------

    Copyright 2016 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
