#### 使用方法
* gradle
```
allprojects {
    repositories {
        jcenter()
    }
}
```
```
compile 'com.fynn.switcher:switch:1.0.2'
```
* maven
```
<dependency>
  <groupId>com.fynn.switcher</groupId>
  <artifactId>switch</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```
* lvy
```
<dependency org='com.fynn.switcher' name='switch' rev='1.0.2'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```
#### Snapshot
录制的动画有些卡顿，抱歉！

![switch](https://github.com/ifynn/Switch/blob/master/snapshot/switch.gif)

# License

Copyright Fynn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.