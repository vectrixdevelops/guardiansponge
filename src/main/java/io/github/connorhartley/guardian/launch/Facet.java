package io.github.connorhartley.guardian.launch;

import io.github.connorhartley.guardian.launch.exception.FacetException;
import io.github.connorhartley.guardian.launch.message.FacetMessage;

public interface Facet {

    <I extends FacetMessage, O> O handle(final FacetBootstrap.FacetRequest<I, O> facetRequest, I input) throws FacetException;

}
