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
compile 'com.fynn.switcher:switch:1.0.0'
```
* maven
```
<dependency>
  <groupId>com.fynn.switcher</groupId>
  <artifactId>switch</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```
* lvy
```
<dependency org='com.fynn.switcher' name='switch' rev='1.0.0'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```
#### Snapshot
录制的动画有些卡顿，抱歉！

![switch](https://github.com/ifynn/Switch/blob/master/snapshot/switch.gif)
![switch on](https://github.com/ifynn/Switch/blob/master/snapshot/switch_on.png)
![switch off](https://github.com/ifynn/Switch/blob/master/snapshot/switch_off.png)
