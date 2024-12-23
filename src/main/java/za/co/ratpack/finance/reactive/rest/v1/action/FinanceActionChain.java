package za.co.ratpack.finance.reactive.rest.v1.action;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.func.Action;
import ratpack.handling.Chain;

/**
 * @author markmngoma
 * @created at 19:00 on 22/12/2024
 */
@Slf4j
@Singleton
public class FinanceActionChain implements Action<Chain> {

  @Override
  public void execute(Chain chain) throws Exception {
  }
}
