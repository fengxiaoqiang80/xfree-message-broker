相关的技术：  
  .Spring WebFlux  
  .Reactive Program  
  .Project Reactor 3  
  .WebSocket  
  .Server Send Event  
  .Reactive Mongodb  
  .Reactive Redis  

如果想要开始学习Reactive编程，可以参考一下。 

Reactive 简单的消息代理，消息永久存储在Mongodb，Redis List中存储待发送列表。  
因为主要是为小程序服务，又因为小程序暂不支持SSE，因此消息的仅仅采用Websocket方式，并未实现SSE。如果要完成SSE，仅仅需要简单包装即可。  

