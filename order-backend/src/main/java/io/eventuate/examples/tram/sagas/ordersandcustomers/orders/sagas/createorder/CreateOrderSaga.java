package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.commands.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.commandsandreplies.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.commandsandreplies.RejectOrderCommand;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class CreateOrderSaga implements SimpleSaga<CreateOrderSagaData> {

  private SagaDefinition<CreateOrderSagaData> sagaDefinition =
          step()
            .withCompensation(this::reject)
          .step()
            .invokeParticipant(this::reserveCredit)
          .step()
            .invokeParticipant(this::approve)
          .build();


  @Override
  public SagaDefinition<CreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }


  private CommandWithDestination reserveCredit(CreateOrderSagaData data) {
    long orderId = data.getOrderId(); 
    Long customerId = data.getOrderDetails().getCustomerId();
    
    if(orderId>=2 && orderId<=4){
     customerId = customerId+1;
    }else{
        customerId=customerId;
    }
    
    Money orderTotal = data.getOrderDetails().getOrderTotal();
    return send(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .build();
  }

  public CommandWithDestination reject(CreateOrderSagaData data) {
    return send(new RejectOrderCommand(data.getOrderId()))
            .to("orderService")
            .build();
  }

  private CommandWithDestination approve(CreateOrderSagaData data) {
    return send(new ApproveOrderCommand(data.getOrderId()))
            .to("orderService")
            .build();
  }


}
