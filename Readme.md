# JsonResourceAnnotation

JUnit 5 参数化测试注解，用于从 JSON 文件加载测试数据。

## 功能特性

- ✅ 从 classpath 加载 JSON 测试数据
- ✅ 从文件系统加载 JSON 测试数据
- ✅ 支持单个对象和数组
- ✅ 支持简单的 JSONPath 数据提取
- ✅ 支持自定义 ObjectMapper 配置
- ✅ 默认 UTF-8 编码（可配置）

## 安装

### Maven

```xml
<dependency>
    <groupId>io.github.tisrop</groupId>
    <artifactId>json-resource-annotation</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

## 使用示例

### 基础用法

从 JSON 加载单个对象：

```java
@ParameterizedTest
@JsonResource(resources = "/user.json", type = User.class)
void testUser(User user) {
    assertEquals("xiaoming", user.getName());
    assertEquals(23, user.getAge());
}
```

### 数组支持

加载数组并作为 List 接收：

```java
@ParameterizedTest
@JsonResource(resources = "/users.json", type = User.class)
void testUsers(List<User> users) {
    assertEquals(2, users.size());
}
```

### JSONPath 提取

使用简单的 JSONPath 提取数据：

```java
@ParameterizedTest
@JsonResource(resources = "/nested.json", jsonPath = "data/user", type = User.class)
void testJsonPath(User user) {
    assertEquals("nested_user", user.getName());
}
```

### 文件系统文件

从文件系统加载而非 classpath：

```java
@ParameterizedTest
@JsonResource(resources = "/path/to/file.json", type = User.class, fileSystem = true)
void testFileSystem(User user) {
    // 测试代码
}
```

### 自定义 ObjectMapper

自定义 Jackson ObjectMapper 配置：

```java
public class CustomObjectMapperFactory implements ObjectMapperFactory {
    @Override
    public ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}

@ParameterizedTest
@JsonResource(resources = "/snake.json", type = User.class, mapperFactory = CustomObjectMapperFactory.class)
void testCustomMapper(User user) {
    // 测试代码
}
```

## 发布到 Maven 中央仓库（新方式）

本项目使用最新的 Maven Central Publisher Portal 发布流程。

### 前置要求

1. **登录 Central Portal**
   - 访问 https://central.sonatype.com/ 使用之前的 OSSRH 账号密码登录
   - 确认你的命名空间已迁移成功

2. **生成 Portal 用户令牌**
   - 在 https://central.sonatype.com/ 登录后，导航到 Account → Access Tokens
   - 生成新的用户令牌（User Token），保存好用户名和密码

3. **生成 GPG 密钥**
   ```bash
   gpg --gen-key
   gpg --list-secret-keys --keyid-format=long
   ```

4. **发布公钥到密钥服务器**
   ```bash
   gpg --keyserver hkp://keyserver.ubuntu.com --send-keys <你的密钥ID>
   ```

### 本地 Maven 配置

在 `~/.m2/settings.xml` 中添加服务器配置：

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username><!-- 你的 Portal 用户名 --></username>
            <password><!-- 你的 Portal 密码 --></password>
        </server>
    </servers>
    
    <profiles>
        <profile>
            <id>central</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.passphrase>你的GPG密码</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
```

### 发布步骤

#### 1. 构建并签名

```bash
mvn clean verify
```

#### 2. 发布到 Central Portal

```bash
mvn clean deploy
```

本项目配置了自动发布功能，部署成功后会自动完成发布流程，无需手动操作。

### 配置说明

项目配置了以下自动发布特性：

- `autoPublish: true` - 自动发布，无需手动确认
- `waitUntil: published` - 等待直到完全发布完成

### 使用环境变量（推荐）

也可以使用环境变量配置 GPG：

```bash
export GPG_KEYNAME=你的密钥ID
export GPG_PASSPHRASE=你的GPG密码

mvn clean deploy
```

### 跳过签名（开发测试）

在开发过程中，可以跳过 GPG 签名：

```bash
mvn clean install -Dgpg.skip=true
```

### 手动发布（可选）

如果需要手动确认发布，可以修改 pom.xml 中的配置：

```xml
<plugin>
    <groupId>org.sonatype.central</groupId>
    <artifactId>central-publishing-maven-plugin</artifactId>
    <version>0.10.0</version>
    <extensions>true</extensions>
    <configuration>
        <publishingServerId>central</publishingServerId>
        <!-- 关闭自动发布，需要手动在 Portal 中发布 -->
        <autoPublish>false</autoPublish>
    </configuration>
</plugin>
```

然后在 https://central.sonatype.com/publishing/deployments 中手动发布。

## 许可证

Apache License, Version 2.0
