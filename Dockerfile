# 第一阶段：构建应用（使用 Maven 基础镜像）
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制项目文件（利用 Docker 缓存层优化）
COPY pom.xml .
# 从项目中拷贝setting.xml,指定使用aliyun 镜像源(可选)
COPY setting.xml  /root/.m2/settings.xml
COPY src ./src

# 构建项目并跳过测试（测试已在 Cgi tI 流程完成）
RUN mvn clean package -DskipTests

# 第二阶段：运行应用（使用轻量级 JRE 镜像）
FROM eclipse-temurin:21-jre-alpine

# 设置时区（可选）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 设置工作目录并复制构建产物
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令（支持 Java 21 的模块化参数）
ENTRYPOINT ["java", "-jar", "app.jar"]