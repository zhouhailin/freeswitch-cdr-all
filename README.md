# FreeSWITCH CDR ALL

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4af5b27cd33340f990d01b009ff15a55)](https://app.codacy.com/gh/zhouhailin/freeswitch-cdr-all?utm_source=github.com&utm_medium=referral&utm_content=zhouhailin/freeswitch-cdr-all&utm_campaign=Badge_Grade_Settings)
[![Jdk Version](https://img.shields.io/badge/JDK-1.8-green.svg)](https://img.shields.io/badge/JDK-1.8-green.svg)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/link.thingscloud/freeswitch-cdr-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/link.thingscloud/freeswitch-esl-all/)


## 使用

POM 引入

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>link.thingscloud</groupId>
                <artifactId>freeswitch-cdr</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>


    <dependency>
        <groupId>link.thingscloud</groupId>
        <artifactId>freeswitch-cdr-spring-boot-starter</artifactId>
    </dependency>


实现

    实现 ：link.thingscloud.freeswitch.cdr.handler.CdrHandler
    
    参考 ：link.thingscloud.freeswitch.cdr.spring.boot.starter.example.ExampleCdrHandler

    日志输出：
    
    l.t.f.cdr.service.impl.CdrServiceImpl    : freeswitch cdr add cdrHandler : [class link.thingscloud.freeswitch.cdr.spring.boot.starter.example.ExampleCdrHandler].


## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) Apache Software Foundation
