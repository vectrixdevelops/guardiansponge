package io.github.connorhartley.guardian.launch;

import io.github.connorhartley.guardian.launch.exception.ComponentException;
import io.github.connorhartley.guardian.launch.message.ComponentMessage;

public interface Component {

    <I extends ComponentMessage, O> O handle(final Bootstrap.ComponentRequest<I, O> componentRequest, I input) throws ComponentException;

}
