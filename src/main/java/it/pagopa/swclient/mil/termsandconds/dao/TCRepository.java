package it.pagopa.swclient.mil.termsandconds.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *  MongoDB repository to access T&C version associated to the Tax Code Token data, reactive flavor
 */
@ApplicationScoped
public class TCRepository implements ReactivePanacheMongoRepository<TcTaxCodeTokenEntity> {
}