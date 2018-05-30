# SpringCloudConfig
SpringCloudConfig E 版本 demo

[TOC]

# Spring Cloud Config

Spring Cloud Config为分布式系统中的外部化配置提供服务器和客户端支持。通过Config Server，您可以在所有环境中管理应用程序的外部属性。客户端和服务器上的概念与Spring Environment和PropertySource抽象一致，所以它们非常适合Spring应用程序，但可以与任何运行在任何语言中的应用程序一起使用。随着应用程序从开发到测试转移到部署流程中，您可以管理这些环境之间的配置，并确保应用程序具有迁移时所需的所有内容。服务器存储后端的默认实现使用git，因此它可以轻松支持配置环境的标签版本，并且可以用于管理内容的各种工具。使用Spring配置很容易添加替代实现并将其插入。

## 使用原因

在单体式应用中，我们通常的做法是将配置文件和代码放在一起，这没有什么不妥。当你的应用变得越来越大从而不得不进行服务化拆分的时候，会发现各种provider实例越来越多，修改某一项配置越来越麻烦，你常常不得不为修改某一项配置而重启某个服务所有的provider实例，甚至为了灰度上线需要更新部分provider的配置。这个时候有一套配置文件集中管理方案就变得十分重要，SpringCloudConfig和SpringCloudBus就是这种问题的解决方案之一，业界也有些知名的同类开源产品，比如百度的disconf。

相比较同类产品，SpringCloudConfig最大的优势是和Spring无缝集成，支持Spring里面`Environment`和`PropertySource`的接口，对于已有的Spring应用程序的迁移成本非常低，在配置获取的接口上是完全一致，结合SpringBoot可使你的项目有更加统一的标准（包括依赖版本和约束规范），避免了应为集成不同开软件源造成的依赖版本冲突。

### ConfigClient 启动顺序##

ConfigClient最好要在ConfigServer之后启动，Spring加载配置文件是有顺序的，靠前的配置文件会覆盖靠后的配置文件中相同键的值，如果ConfigServer先启动可以保证ConfigClient将远程的配置文件加载到最前面，如果使用中没有注意到这一点，有可能导致你本地的配置文件先于远程的加载，导致本地的配置覆盖远程配置。当然，你也可以让本地配置和远程配置完全不重复，这样也可以避免键/值覆盖的问题。



## 快速开始

启动服务器：

```
$ cd spring-cloud-config-server
$ ../mvnw spring-boot:run
```

服务器是Spring Boot应用程序，所以如果您愿意，您可以从IDE运行它（主要类是ConfigServerApplication）。 然后尝试一个客户端：

~~~shell
$ curl localhost:8888/foo/development
{"name":"foo","label":"master","propertySources":[
  {"name":"https://github.com/scratches/config-repo/foo-development.properties","source":{"bar":"spam"}},
  {"name":"https://github.com/scratches/config-repo/foo.properties","source":{"foo":"bar"}}
]}
~~~
定位资源的默认策略是克隆一个git仓库（在`spring.cloud.config.server.git.uri`），并使用它来初始化一个迷你`SpringApplication`。小应用程序的`Environment`用于枚举属性源并通过JSON端点发布。

HT
