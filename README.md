## nginx的upstream 动态修改以及持久化
 将 **[cloud-nginx-file](cloud-nginx-file)** 结合开源项目 **[ngx_dynamic_upstram](https://github.com/cubicdaiya/ngx_dynamic_upstream)**，实现对nginx灰度发布功能
 
cloud-nginx-file 地址(java编写)：
>https://github.com/kanhai001/spring-cloud-learning/tree/master/cloud-nginx-file
  
ngx_dynamic_upstram地址(C编写)：
> https://github.com/cubicdaiya/ngx_dynamic_upstream


### 安装及部署

##### ngx_dynamic_upstram 安装
将ngx_dynamic_upstram作为nginx的一个module 添加到nginx
```
 ./configure  --add-module=./ngx_dynamic_upstream-0.1.6
```
并make && make install,并执行，查看是否编译模块成功
```
/usr/local/nginx/sbin/nginx -V 
```

修改nginx.conf 配置server运行访问的IP地址：
```
upstream backends {
    zone zone_for_backends 1m;
    server 127.0.0.1:6001 weight=1 max_fails=2 fail_timeout=30;
    server 127.0.0.1:6002 weight=1 max_fails=2 fail_timeout=30;
    server 127.0.0.1:6003 weight=1 max_fails=2 fail_timeout=30;
}

server {
    listen 6000;

    location /dynamic {
        allow 127.0.0.1;
        deny all;
        dynamic_upstream;
    }

    location / {
        proxy_pass http://backends;
    }
}
```

##### cloud-nginx-file 部署
将项目jar包发布至服务器，并启动，启动方式：

* 1）安装为系统服务
```
$ sudo ln -s /data/apps/cloud-nginx-file.jar /etc/init.d/cloud-nginx-file
```
启动项目并制定读取配置文件的地址 
```
$ /etc/init.d/cloud-nginx-file start -Dspring.file.location=/data/apps/cloud-nginx-file/cnfig/
```
* 2）nohup 后台启动
```
$ nohup java -jar -Dserver.port=8080 -Dspring.file.location=/data/apps/cloud-nginx-file/config ./cloud-nginx-file.jar  >nginx-file.log &
```


## Http API 访问


动态操作upstram

### 查询upstram服务列表

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends"
server 127.0.0.1:6001;
server 127.0.0.1:6002;
server 127.0.0.1:6003;
```

### 显示详细信息

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&verbose="
server 127.0.0.1:6001 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6002 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6003 weight=1 max_fails=1 fail_timeout=10;
```

### 修改参数

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&server=127.0.0.1:6003&weight=10&max_fails=5&fail_timeout=5"
server 127.0.0.1:6001 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6002 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6003 weight=10 max_fails=5 fail_timeout=5;
```

The supported parameters are blow.

 * weight
 * max_fails
 * fail_timeout
 * status 参数四个指 down 下掉指定节点  up 启动指定节点   add 添加节点  remove移除节点

### 动态下掉机器

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&server=127.0.0.1:6003&status=down"
server 127.0.0.1:6001 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6002 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6003 weight=1 max_fails=1 fail_timeout=10 down;
```

### 动态将down的机器up 

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&server=127.0.0.1:6003&status=up"
server 127.0.0.1:6001 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6002 weight=1 max_fails=1 fail_timeout=10;
server 127.0.0.1:6003 weight=1 max_fails=1 fail_timeout=10;
```

### 动态添加节点

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&status=add&server=127.0.0.1:6004"
server 127.0.0.1:6001;
server 127.0.0.1:6002;
server 127.0.0.1:6003;
server 127.0.0.1:6004;
```

### 动态移除节点

```bash
$ curl "http://127.0.0.1:6000/dynamic?upstream=zone_for_backends&remove=&server=127.0.0.1:6003"
server 127.0.0.1:6001;
server 127.0.0.1:6002;
server 127.0.0.1:6004;
```


    


