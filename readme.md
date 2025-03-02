一、单测报告
1、报告路径：src/test/report/site/jacoco/index.html
2、单测执行命令: mvn clean test

二、压测报告
1、报告路径:src/test/report/transactionsimulation-20250301112504141/index.html
2、压测执行命令：
   步骤1：启动Spring Boot应用
   mvn spring-boot:run
   步骤2：执行Gatling测试
   mvn gatling:test -Dgatling.simulationClass=com.transaction.performance.TransactionSimulation
   性能测试报告示例
   Gatling会自动生成HTML报告，路径为：
   target/gatling/transactionsimulation-<timestamp>/index.html
3、压测场景说明:
   见performance/TransactionSimulation.scala脚本

三、docker
1. 构建镜像
   docker build -t bank-transaction:1.0.0 .
2. 运行容器
   docker run -d -p 8080:8080 --name transaction-service bank-transaction:1.0.0
3. 验证启动
   docker logs -f transaction-service
   预期输出：
   Started TransactionApplication in 3.161 seconds (process running for 4.13)

说明：由于docker镜像内置的maven默认的Mvaven仓库地址拉取依赖很慢，经常超时导致构建失败，所以在dockerfile里面 将本地配置了阿里云仓库的setting文件拷贝了进去，如果使用人员用的是内部网络，影响了构建，可以将dockerfile里面的这句 命令删除: COPY setting.xml /root/.m2/settings.xml
