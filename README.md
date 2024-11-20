# Q

```
用户有多种支付方式（余额，红包，优惠券，代金券等），假如每种支付方式需要通过实时调用远程服务获取可用性。
在外部资源环境不变情况下，请设计程序以最短响应时间获得尽可能多的可用支付方式列表。
```

# 思考

程序要求在最短的时间内返回尽可能多的可用支付方式列表。因为支付方式的可用性需要通过远程服务获取，因此可以预先获取所有支付方式的可用性，本地缓存可用性结果，后续调用直接从缓存中获取可用支付方式列表。同时需要设计一种缓存更新的方式，确保在支付方式可用性发生变化时，本地缓存能及时更新。
- 需要综合考虑高并发，系统可扩展性，容错性等。
- 需要记录与监控相关指标。

### 远程获取支付方式可用性设计

- 支付方式可用性需要远程调用服务来获取，串行逐个获取会比较慢，因此这里采用并行获取的方式，总的响应的时间就取决于远程服务中响应最慢的那一个服务。
    - 同时考虑远程服务调用时间可能耗时过长或远程服务宕机，因此加上超时的设计，如果超过某个时间没有获取到当前支付方式是否可用，就认为不可用。

### 缓存设计

- 考虑到如果每次请求都需要多次远程调用的话，可能会增加系统压力以及耗时。假设支付方式的可用性在一定时间内不会经常发生变化，因此可以采用本地缓存的方式来存储可用支付方式列表，后续每次请求从缓存中直接获取可用性列表。这样能大大提高响应时间。
- 但该种缓存方式存在一定的问题，例如当可用支付方式发生变化时，缓存不会自动更新，有可能获取到过期的结果。因此需要设计一种缓存更新的方式，确保在支付方式可用性发生变化时，缓存能及时更新：
    - 定时刷新支付方式可用性缓存，例如10s刷新一次。但这种方式还是会有10s的延迟，假设10s内支付方式可用性发生变化，那么将会获取获取到不准确的结果。
    - 因此可以借鉴nacos的服务上下线通知机制，在定时刷新缓存的基础上，增加一种支付服务主动通知不可用的方式。这里可以通过消息队列的方式，消费模式为广播，当支付服务不可用时，发送广播消息，通知所有订阅方，当前服务消费消息，及时更新缓存。
- 初次获取所有支付方式可用性需要全部调用远程服务，为了防止响应时间过长，也需要设置超时时间，只返回在超时时间内获取到的可用支付服务列表。

### 扩展

- 可以额外采集支付方式服务的调用耗时，以便后续根据耗时来合理设置超时时间。
- 采集各支付方式的可用率，方便业务后续调整支付方式做数据上的支撑。

### 代码设计

- [PaymentMethodAvailabilityChecker](src%2Fmain%2Fjava%2Fcom%2Fant%2Fchecker%2FPaymentMethodAvailabilityChecker.java) :
  负责检查支付方式可用性
- [PaymentMethodAvailabilityCache](src%2Fmain%2Fjava%2Fcom%2Fant%2Fcache%2FPaymentMethodAvailabilityCache.java) :
  负责缓存支付方式可用性
- [MessageBroker](src%2Fmain%2Fjava%2Fcom%2Fant%2Fmq%2FMessageBroker.java) : 模拟消息订阅发布
- [PaymentMethodService](src%2Fmain%2Fjava%2Fcom%2Fant%2Fpay%2FPaymentMethodService.java) :
  负责协调支付方式初始化，缓存更新，支付方式可用性获取等流程
- 可以通过调整 [Constant](src%2Fmain%2Fjava%2Fcom%2Fant%2FConstant.java) 中`REMOTE_TIMEOUT_SECONDS`来控制远程调用的超时时间，超时时间越短，则等待时间越短，响应越快。通过调整`REMOTE_REQUEST_AVG_TIME_MILLIS`值来模拟增大或减小远程调用时间，模拟调用超时的场景。

### 运行

- 先clone：`git clone xxxx`，然后进入项目目录
- `mvn package`
- `cd target`
- `java -jar pay-1.0.jar`
- 输出如：
```
刷新缓存...
支付方式可用性检查, 支付方式：余额支付, 是否可用：true 
支付方式可用性检查, 支付方式：代金券支付, 是否可用：false 
支付方式可用性检查, 支付方式：优惠券支付, 是否可用：true 
支付方式可用性检查, 支付方式：红包支付, 是否可用：true 
支付方式可用性缓存初始化完成
【第一次获取所有支付方式可用性】：[PaymentMethod{id='2', name='红包支付', type=RED_PACKET, available=true}, PaymentMethod{id='3', name='优惠券支付', type=COUPON, available=true}, PaymentMethod{id='1', name='余额支付', type=BALANCE, available=true}]
【发布余额支付不可用消息后获取所有支付方式可用性】：[PaymentMethod{id='2', name='红包支付', type=RED_PACKET, available=true}, PaymentMethod{id='3', name='优惠券支付', type=COUPON, available=true}]
【休眠模拟等待缓存自动刷新...】
刷新缓存...
支付方式可用性检查, 支付方式：红包支付, 是否可用：true 
支付方式可用性检查, 支付方式：优惠券支付, 是否可用：true 
支付方式可用性检查, 支付方式：代金券支付, 是否可用：true 
支付方式可用性检查, 支付方式：余额支付, 是否可用：false 
【缓存自动刷新后获取所有支付方式可用性】：[PaymentMethod{id='2', name='红包支付', type=RED_PACKET, available=true}, PaymentMethod{id='4', name='代金券支付', type=VOUCHER, available=true}, PaymentMethod{id='3', name='优惠券支付', type=COUPON, available=true}]
```