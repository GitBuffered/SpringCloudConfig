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

HTTP服务具有以下形式的资源：

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

其中“应用程序”作为`SpringApplication`中的`spring.config.name`注入（即常规的Spring Boot应用程序中通常是“应用程序”），“配置文件”是活动配置文件（或逗号分隔列表的属性），“label”是可选的git标签（默认为“master”）。

Spring Cloud Config服务器从git存储库（必须提供）为远程客户端提供配置：

```properties
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
```

### 客户端使用情况

要在应用程序中使用这些功能，只需将其构建为依赖于spring-cloud-config-client的Spring引导应用程序（例如，查看配置客户端或示例应用程序的测试用例）。添加依赖关系的最方便的方法是通过Spring Boot启动器`org.springframework.cloud:spring-cloud-starter-config`。还有一个Maven用户的父pom和BOM（`spring-cloud-starter-parent`）和用于Gradle和Spring CLI用户的Spring IO版本管理属性文件。示例Maven配置：

的pom.xml

~~~xml
 <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>1.5.10.RELEASE</version>
       <relativePath /> <!-- lookup parent from repository -->
   </parent>

<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>Edgware.SR3</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>

<dependencies>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-config</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
	</dependency>
</dependencies>

<build>
	<plugins>
           <plugin>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-maven-plugin</artifactId>
           </plugin>
	</plugins>
</build>

   <!-- repositories also needed for snapshots and milestones -->
~~~

然后你可以创建一个标准的Spring Boot应用程序，就像这个简单的HTTP服务器一样

```java
@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/")
    public String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```



运行时，它将从端口8888上的默认本地配置服务器接收外部配置（如果它正在运行）。 要修改启动行为，可以使用bootstrap.properties（如application.properties，但是应用程序上下文的引导阶段）来更改配置服务器的位置，例如，

```properties
spring.cloud.config.uri: http://myconfigserver.com
```

bootstrap属性将作为高优先级属性源显示在/ env端点中，例如，

```shell
$ curl localhost:8080/env
{
  "profiles":[],
  "configService:https://github.com/spring-cloud-samples/config-repo/bar.properties":{"foo":"bar"},
  "servletContextInitParams":{},
  "systemProperties":{...},
  ...
}
```

（名为“configService：<远程存储库的URL> / <文件名>”的属性源包含属性“foo”，其值为“bar”并且是最高优先级）。

## Spring Cloud Config服务器

服务器为外部配置（名称 - 值对或同等YAML内容）提供了一个HTTP，基于资源的API。 

pom.xml

~~~properties
 	<!--添加相关依赖-->
 	<dependency>
		<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-server</artifactId>
	</dependency>
~~~



服务器可以使用@EnableConfigServer批注轻松嵌入到Spring Boot应用程序中。 所以这个程序是一个配置服务器：

**ConfigServer.java. **

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServer {
  public static void main(String[] args) {
    SpringApplication.run(ConfigServer.class, args);
  }
}
```

与所有Spring Boot应用程序一样，它默认在端口8080上运行，但您可以通过各种方式将其切换到常规端口8888。 最简单的方法是设置默认的配置库，方法是使用spring.config.name = configserver启动它（在Config Server jar中有一个configserver.yml）。 另一种方法是使用自己的application.properties，例如

**application.properties. **

```properties
server.port: 8888
spring.cloud.config.server.git.uri: file://${user.home}/config-repo
```

其中`${user.home}/config-repo`是包含YAML和属性文件的git仓库。

> 在Windows中，如果文件URL为绝对驱动器前缀，例如`file:///${user.home}/config-repo`，则需要额外的“/”。

以下是上面示例中创建git仓库的方法：

~~~shell
$ cd $HOME
$ mkdir config-repo
$ cd config-repo
$ git init .
$ echo info.foo: bar > application.properties
$ git add -A .
$ git commit -m "Add application.properties"
~~~

> 使用本地文件系统进行git存储库仅用于测试。使用服务器在生产环境中托管配置库。
>
> 如果您只保留文本文件，则配置库的初始克隆将会快速有效。如果您开始存储二进制文件，尤其是较大的文件，则可能会遇到服务器中第一个配置请求和/或内存不足错误的延迟。

## 环境库

您要在哪里存储配置服务器的配置数据？管理此行为的策略是`EnvironmentRepository`，服务于`Environment`对象。此`Environment`是Spring `Environment`（包括`propertySources`作为主要功能）的域的浅层副本。`Environment`资源由三个变量参数化：

- `{application}`映射到客户端的“spring.application.name”;
- `{profile}`映射到客户端上的“spring.profiles.active”（逗号分隔列表）; 和
- `{label}`这是一个服务器端功能，标记“版本”的配置文件集。

存储库实现通常表现得像一个Spring Boot应用程序从“spring.config.name”等于`{application}`参数加载配置文件，“spring.profiles.active”等于`{profiles}`参数。配置文件的优先级规则也与常规启动应用程序相同：活动配置文件优先于默认配置，如果有多个配置文件，则最后一个获胜（例如向`Map`添加条目）。

示例：客户端应用程序具有此引导配置：

bootstrap.yml

```
spring:
  application:
    name: foo
  profiles:
    active: dev,mysql
```

（通常使用Spring Boot应用程序，这些属性也可以设置为环境变量或命令行参数）。

如果存储库是基于文件的，则服务器将从`application.yml`创建`Environment`（在所有客户端之间共享），`foo.yml`（以`foo.yml`优先））。如果YAML文件中有文件指向Spring配置文件，那么应用的优先级更高（按照列出的配置文件的顺序），并且如果存在特定于配置文件的YAML（或属性）文件，那么这些文件也应用于优先级高于默认值。较高优先级转换为`Environment`之前列出的`PropertySource`。（这些规则与独立的Spring Boot应用程序相同。）

### Git后端

`EnvironmentRepository`的默认实现使用Git后端，这对于管理升级和物理环境以及审核更改非常方便。要更改存储库的位置，可以在Config Server中设置“spring.cloud.config.server.git.uri”配置属性（例如`application.yml`）。如果您使用`file:`前缀进行设置，则应从本地存储库中工作，以便在没有服务器的情况下快速方便地启动，但在这种情况下，服务器将直接在本地存储库上进行操作，而不会克隆如果它不是裸机，因为配置服务器永远不会更改“远程”资源库）。要扩展Config Server并使其高度可用，您需要将服务器的所有实例指向同一个存储库，因此只有共享文件系统才能正常工作。即使在这种情况下，最好使用共享文件系统存储库的`ssh:`协议，以便服务器可以将其克隆并使用本地工作副本作为缓存。

该存储库实现将HTTP资源的`{label}`参数映射到git标签（提交ID，分支名称或标签）。如果git分支或标签名称包含斜杠（“/”），则应使用特殊字符串“（_）”指定HTTP URL中的标签，以避免与其他URL路径模糊。例如，如果标签为`foo/bar`，则替换斜杠将导致标签看起来像`foo(__)bar`。包含特殊字符串“（_）”也可以应用于{application}参数。如果您使用像curl这样的命令行客户端（例如使用引号将其从shell中转出来），请小心URL中的方括号。

#### Git URI中的通配符

如果您需要，Spring Cloud Config Server支持带有{application}和{profile}（和{label}）占位符的git存储库URL，但请记住该标签作为git标签应用。 因此，您可以轻松地使用（例如）支持“每个应用程序一个repo”政策：

> 一个微服务一个配置文件,不影响其他的项目

```properties
spring:
  cloud:
    config:
      server:
        git:
           uri: https://github.com/myorg/{application}
```

或使用类似模式但使用`{profile}`的“每个配置文件一个”策略。

此外，在{应用}参数中使用特殊字符串“（_）”可以支持多个组织（例如）：

#### 模式匹配和多个存储库

还有对应用程序和配置文件名称进行模式匹配的更复杂要求的支持。 模式格式是带有通配符的{application} / {profile}名称的逗号分隔列表（其中可能需要引用以通配符开头的模式）。 例：

```properties
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
          repos:
            simple: https://github.com/simple/config-repo
            special:
              pattern: special*/dev*,*special*/dev*
              uri: https://github.com/special/config-repo
            local:
              pattern: local*
              uri: file:/home/configsvc/config-repo
```

如果{application} / {profile}与任何模式都不匹配，它将使用在“spring.cloud.config.server.git.uri”下定义的默认URI。 在上面的示例中，对于“simple”存储库，该模式simple/ *（即它只匹配所有配置文件中名为“简单”的一个应用程序）。 “local”存储库匹配所有配置文件中以“local”开头的所有应用程序名称（/ *后缀自动添加到任何没有配置文件匹配器的模式）。

> 上述“simple”示例中使用的“单行”快捷方式只能在要设置的唯一属性是URI的情况下使用。 如果您需要设置其他任何内容（凭据，模式等），则需要使用完整的表单。

repo中的`pattern`属性实际上是一个数组，因此您可以使用属性文件中的YAML数组（或`[0]`，`[1]`等后缀）绑定到多个模式。如果要运行具有多个配置文件的应用程序，则可能需要执行此操作。例：

```properties
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
          repos:
            development:
              pattern:
                - '*/development'
                - '*/staging'
              uri: https://github.com/development/config-repo
            staging:
              pattern:
                - '*/qa'
                - '*/production'
              uri: https://github.com/staging/config-repo
```

> Spring Cloud会猜测，包含不以*结尾的配置文件的模式意味着您实际上想匹配以此模式开头的配置文件列表（因此* / staging是[“* / staging”，“*”/分期，*“]）。 例如，您需要在本地“开发”配置文件中运行应用程序，而在远程运行“cloud”配置文件时，这也很常见。



每个存储库还可以选择将配置文件存储在子目录中，并且可以将搜索这些目录的模式指定为searchPaths。 例如在顶层：

```properties
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
          searchPaths: foo,bar*
```

在这个例子中，服务器搜索顶层和“foo /”子目录中的配置文件，以及名称以“bar”开头的任何子目录。

默认情况下，服务器在首次请求配置时克隆远程存储库。 可以将服务器配置为在启动时克隆存储库。 例如在顶层：

```properties
spring:
  cloud:
    config:
      server:
        git:
          uri: https://git/common/config-repo.git
          repos:
            team-a:
                pattern: team-a-*
                cloneOnStart: true
                uri: http://git/team-a/config-repo.git
            team-b:
                pattern: team-b-*
                cloneOnStart: false
                uri: http://git/team-b/config-repo.git
            team-c:
                pattern: team-c-*
                uri: http://git/team-a/config-repo.git
```

在这个例子中，服务器在接受任何请求之前，在启动时克隆team-a的config-repo。 除非请求存储库中的配置，
