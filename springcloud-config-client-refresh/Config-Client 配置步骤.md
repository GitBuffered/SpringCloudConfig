# Config-Client 配置步骤--最简单版本

> 前提：我们已经搭建好了统一的配置中心，已经使用统一的配置仓库，本文的例子，我们在git仓库中配置的文件名为[configClient-test.properties](https://github.com/startSnow/config-repo/blob/master/configClient-test.properties) ，规则为{application}-{profile}.properties
>
> 也可以为使用以下规则创建文件
>
> ```
> /{application}/{profile}[/{label}]
> /{application}-{profile}.yml
> /{label}/{application}-{profile}.yml
> /{application}-{profile}.properties
> /{label}/{application}-{profile}.properties
>
> ```
> 其中“application”作为SpringApplication中的spring.config.name/spring.application.name注入（即常规的Spring Boot应用程序中通常是“application”），“profile”是spring.cloud.config.profile（或逗号分隔列表的属性），“label”是可选的git标签（默认为“master”）。

## 步骤1、增加依赖

~~~xml
   <!-- 配置中心客户端的依赖 -->
    	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-config</artifactId>
	</dependency>
    <!-- 这两个是连接配置中心快速失败和重试需要用到的依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.retry</groupId>
        <artifactId>spring-retry</artifactId>
    </dependency>
~~~



## 步骤2、配置修改

### bootstrap.yml或者bootstrap.properties配置文件修改

~~~
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8080
      label: master #当ConfigServer后端存储是GIT默认就是master
      profile: test
      fail-fast: true #失败快速响应
      retry:
        initial-interval: 2000 # 初始重试间隔时间，默认1000ms
        max-attempts: 5 # 配置重试次数，默认为6
        max-interval: 2000 # 最大间隔时间，默认2000ms
        multiplier: 1.1 # 间隔乘数，默认1.1
  application:
    name: configClient
server:
  port: 9999

~~~

## 步骤3、修改类代码@RefreshScope 

~~~java
package org.springcloud.config.client.refresh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope 
public class ConfigClientController {
	@Value("${test}")
	private String profile;
	    
	@RequestMapping("/")
    public String home() {
        return this.profile;
    }
}

~~~



> 所有要放入配置中心的属性的调用类要增加@RefreshScope 注解，方便日后刷新（应用不关，切换属性值）使用

##步骤4、动态刷新配置

更新了Git仓库中的配置文件，让我的config-client能够及时感知到呢？方式很简单，首先在config-client中添加如下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

该依赖中包含了/refresh端点的实现，我们将利用这个端点来刷新配置信息。因暂时没有权限的配置需要在application.properties中配置忽略权限拦截，日后可统一再行配置

```properties
management.security.enabled=false
```

修改Git仓库的文件是属性内容后，需要执行先用POST请求访问http://localhost:2008/refresh地址，再访问应用我们可看到属性值已更改

