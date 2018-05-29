#手动刷新
##当配置信息修改，不关闭应用的情况下访问接口，刷新配置

~~~
 curl -X POST http://127.0.0.1:9999/refresh 
~~~
##FAQ
### spring-cloud 中 config client 调用 /refresh 返回 401

我本来是 想测试 下
在 github 修改配置文件的 value
然后 配置调用客户端 希望看到的是 取到的是最新的 配置参数值

前提 必须是 POST 请求 调用 一下 /refresh

可是 怎么返回下面

chixue@chixue-ThinkPad-Edge-E431:~$ curl -X POST http://127.0.0.1:9999/refresh 
{"timestamp":1527489302433,"status":401,"error":"Unauthorized","message":"Full authentication is required to access this resource.","path":"/refresh"}

####解决办法一
需要在配置文件中关闭security
~~~
management:
  security:
    enabled: false
~~~
####解决版本二
在配置文件配置用户信息，访问时带用户信息请求
~~~
curl -X POST http://user:password/......
~~~
