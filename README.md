# BaiduMapDemo
通过百度地图实现仿美团外卖的地图选点确定收货地址

项目详解请阅读：[https://blog.csdn.net/u011133887/article/details/80372616](https://blog.csdn.net/u011133887/article/details/80372616)

### Demo使用方法：

1. `git clone git@github.com:junerver/BaiduMapDemo.git`
2. 修改 AndroidManifest.xml 文件中第36行，填入你自己申请的AK
3. 在项目根目录放入你自己的 Key 文件
4. 在百度地图控制台填写你的 Key 文件的 SHA1 值（使用指令`keytool -list -v -keystore debug.keystore `）


当前项目使用 Kotlin 编写，所有只有一个方法的接口都用 Lambda 表达式实现，如果有看不懂的地方，可以参照博客里的 Java 代码。
